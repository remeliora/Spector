# Основные изменения COMMIT_6

### (09.02.2024)

Реализовал скрипт-опрос SNMP-GET. 
На данный момент скрипт опрашивает устройства по необходимым параметрам, но записывает он данные пока в Json-файлы и немного не в той структуре, что была ранее намечена.
Также данные пока записываются не в преобразованном виде (логику для этого пока что пишу).
Из следующих задач, это записывание данных непосредственно во вторую БД (MongoDB), а Json-файлы переделывание в стандартные файлы логов для каждого из устройств.
Также необходимо будет для взаимодействия с программой создать отдельные классы для работы с логами и второй БД.
