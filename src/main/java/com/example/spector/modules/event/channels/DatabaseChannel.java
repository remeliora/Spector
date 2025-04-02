package com.example.spector.modules.event.channels;

import com.example.spector.domain.enums.AlarmType;
import com.example.spector.domain.enums.EventType;
import com.example.spector.modules.event.EventMessage;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.*;

@Component
public class DatabaseChannel implements EventChannel {

    private static final long INITIAL_BACKOFF_MS = 1000;
    private final DataSource firebirdDataSource;
    private final BlockingQueue<EventMessage> eventQueue = new LinkedBlockingQueue<>();
    private final ExecutorService dbWorker = Executors.newSingleThreadExecutor();

    // Максимальное число попыток при deadlock
    private static final int MAX_RETRIES = 3;

    public DatabaseChannel(@Qualifier("firebirdDataSource") DataSource firebirdDataSource) {
        this.firebirdDataSource = firebirdDataSource;
    }

    @PostConstruct
    public void init() {
        checkConnection();
        startQueueProcessor();
    }

    private void startQueueProcessor() {
        dbWorker.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    EventMessage event = eventQueue.take(); // Блокирующее ожидание события
                    processEvent(event);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    private boolean checkConnection() {
        try (Connection conn = firebirdDataSource.getConnection()) {
            if (conn == null || conn.isClosed()) {
                System.out.println("Firebird: соединение отсутствует или закрыто.");

                return false;
            }

            return true;
        } catch (SQLException e) {
            System.out.println("Firebird: соединение недоступно: " + e.getMessage());

            return false;
        }
    }

    @Override
    public void handle(EventMessage event) {
        // Канал заблокирован, ничего не делаем
        if (!event.isAlarmActive()) return;
        // Если AlarmType = INACTION, то не пишем в базу
        if (event.getEventType() == EventType.DB && event.getAlarmType() == AlarmType.INACTION) return;

        if (!checkConnection()) {
            System.out.println("Пропуск записи в Firebird: соединение недоступно");

            return;
        }
        // Добавляем событие в очередь для последовательной обработки
        eventQueue.offer(event);
    }

    private void processEvent(EventMessage event) {
        int calculatedTimeout = event.getPeriod() != null ? (int) (event.getPeriod() * 1.6) : 45;
        String message = event.getMessage().replace("'", "''");
        String pattern = buildPattern(message);

        String sqlCheck = "SELECT FIRST 1 ID FROM EVCH$ERRORS WHERE (ADDSECOND(STOP_TIME, TIMEOUT) > CURRENT_TIMESTAMP) AND (MESSAGE_SHORT LIKE ?)";

        String sqlInsert = "INSERT INTO EVCH$ERRORS (ID, START_TIME, STOP_TIME, DEVICEID, PARAMID, TIMEOUT, PRIORITY, MESSAGE_SHORT, MESSAGE_LONG, HELP_FILE_NAME) " +
                           "VALUES (0, CURRENT_TIMESTAMP, ADDSECOND(CURRENT_TIMESTAMP, ?), NULL, NULL, 5, 2, ?, ' ', ' ')";

        String sqlUpdate = "UPDATE EVCH$ERRORS SET STOP_TIME = ADDSECOND(CURRENT_TIMESTAMP, ?), MESSAGE_SHORT = ?, MESSAGE_LONG = ' ' WHERE ID = ?";

        int attempt = 0;
        boolean success = false;

        while (!success && attempt < MAX_RETRIES) {
            attempt++;
            try (Connection conn = firebirdDataSource.getConnection()) {
                conn.setAutoCommit(false);
                // Проверяем, существует ли ошибка
                try (PreparedStatement stmtCheck = conn.prepareStatement(sqlCheck)) {
                    stmtCheck.setString(1, pattern);
//                    System.out.println("Executing query CHECK");

                    try (ResultSet rs = stmtCheck.executeQuery()) {
                        if (rs.next()) {
                            // Если ошибка существует, обновляем ее
                            int errId = rs.getInt(1);
                            try (PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdate)) {
                                stmtUpdate.setInt(1, calculatedTimeout);
                                stmtUpdate.setString(2, message);
                                stmtUpdate.setInt(3, errId);
                                System.out.println("Executing query UPDATE");
                                stmtUpdate.executeUpdate();
                            }
                        } else {
                            // Если ошибки нет, вставляем новую
                            try (PreparedStatement stmtInsert = conn.prepareStatement(sqlInsert)) {
                                stmtInsert.setInt(1, calculatedTimeout);
                                stmtInsert.setString(2, message);
                                System.out.println("Executing query INSERT");
                                stmtInsert.executeUpdate();
                            }
                        }
                    }
                }
                conn.commit(); // Явно фиксируем транзакцию
                success = true;
            } catch (SQLException e) {
                System.out.println("Ошибка записи в Firebird (попытка " + attempt + "): " + e.getMessage());
                if (isDeadlockException(e)) {
                    long backoff = (long) (INITIAL_BACKOFF_MS * Math.pow(2, attempt - 1));
                    System.out.println("Deadlock обнаружен, выполняется повторная попытка...");
                    try {
                        Thread.sleep(backoff); // Задержка увеличивается с каждой попыткой
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    break; // Если ошибка не deadlock, выходим из цикла
                }
            }
        }
        if (!success) {
            System.out.println("Операция записи в Firebird не удалась после " + MAX_RETRIES + " попыток.");
        }
    }

    private String buildPattern(String message) {
        // Проверяем, содержит ли сообщение двоеточие
        if (message.contains(":")) {
            String[] parts = message.split(":", 2); // делим на две части по первому двоеточию
            String devicePart = parts[0].trim(); // название устройства
            String remainder = parts[1].trim();  // остальная часть сообщения
            String[] words = remainder.split(" ");
            if (words.length >= 2) {
                return devicePart + ": " + words[0] + " " + words[1] + " " + words[2] + " " + "%";
            } else {
                return devicePart + ": " + remainder + "%";
            }
        } else {
            // Если двоеточия нет, то берем первые два слова всего сообщения
            String[] words = message.split(" ");
            if (words.length >= 2) {
                return words[0] + " " + words[1] + "%";
            } else {
                return message + "%";
            }
        }
    }

    private boolean isDeadlockException(SQLException e) {
        String message = e.getMessage().toLowerCase();
        return message.contains("deadlock") || message.contains("concurrent update");
    }

    @PreDestroy
    public void shutdown() {
        dbWorker.shutdown();
        try {
            if (!dbWorker.awaitTermination(5, TimeUnit.SECONDS)) {
                dbWorker.shutdownNow();
            }
        } catch (InterruptedException e) {
            dbWorker.shutdownNow();
        }
    }
}
