#!/bin/bash
set -e

# Путь к файлу бэкапа внутри контейнера
BACKUP_FILE="/docker-entrypoint-initdb.d/SPECTOR_STABLE_08_12_2025.backup"

# Проверяем маркер инициализации в томе данных
INIT_MARKER="/var/lib/postgresql/data/restore_completed.marker"

if [ ! -f "$INIT_MARKER" ]; then
    echo "База данных не инициализирована или восстановление еще не выполнялось. Восстанавливаем из $BACKUP_FILE..."

    # Убедимся, что файл бэкапа существует
    if [ ! -f "$BACKUP_FILE" ]; then
        echo "Файл бэкапа $BACKUP_FILE не найден внутри контейнера!" >&2
        exit 1
    fi

    pg_restore -U postgres -d spector -v --clean --if-exists "$BACKUP_FILE"

    # Создаем маркер, чтобы не восстанавливать на последующих запусках
    touch "$INIT_MARKER"
    echo "Восстановление из бэкапа завершено. Маркер создан: $INIT_MARKER"
else
    echo "Восстановление уже было выполнено ранее (маркер $INIT_MARKER найден). Пропускаем восстановление."
fi

# Запуск основного процесса PostgreSQL
exec "$@"