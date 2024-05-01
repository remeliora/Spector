// Функция для загрузки списка устройств с сервера
function fetchDevices() {
    fetch('/rest-api/v1/devices')
        .then(response => response.json())
        .then(devices => {
            displayDevices(devices);
        })
        .catch(error => console.error('Error fetching devices:', error));
}

// Функция для отображения списка устройств на странице
function displayDevices(devices) {
    const deviceListTable = document.createElement('table'); // Создаем элемент таблицы
    const deviceListDiv = document.getElementById('device-list');
    deviceListDiv.innerHTML = ''; // Очищаем содержимое элемента

    devices.forEach(device => {
        const row = deviceListTable.insertRow(); // Вставляем новую строку в таблицу

        // Добавляем ячейки с данными устройства
        row.insertCell().textContent = device.name;
        row.insertCell().textContent = device.ipAddress;
        row.insertCell().textContent = device.description;
        row.insertCell().textContent = device.period;
        row.insertCell().textContent = device.alarmType;
        row.insertCell().textContent = device.isEnable ? 'Yes' : 'No';

        // Добавляем кнопки для редактирования и удаления устройства
        const editButton = document.createElement('button');
        editButton.textContent = 'Edit';
        editButton.addEventListener('click', () => editDevice(device.id)); // Вызываем функцию редактирования устройства

        const deleteButton = document.createElement('button');
        deleteButton.textContent = 'Delete';
        deleteButton.addEventListener('click', () => deleteDevice(device.id)); // Вызываем функцию удаления устройства

        const actionsCell = row.insertCell();
        actionsCell.appendChild(editButton);
        actionsCell.appendChild(deleteButton);
    });

    // Добавляем таблицу в контейнер
    deviceListDiv.appendChild(deviceListTable);

    // Добавляем кнопку "Create Device"
    const createButton = document.createElement('button');
    createButton.textContent = 'Create Device';
    createButton.addEventListener('click', () => createDevice()); // Вызываем функцию создания нового устройства
    deviceListTable.parentNode.appendChild(createButton); // Добавляем кнопку после таблицы
}

// Функция для перехода на страницу создания нового устройства
function createDevice() {
    window.location.href = '/devices/new'; // Перенаправляем пользователя на страницу создания нового устройства
}

// Функция для редактирования устройства
function editDevice(deviceId) {
    // Перенаправляем пользователя на страницу редактирования устройства
    window.location.href = `/devices/${deviceId}`;
}

// Функция для удаления устройства
function deleteDevice(deviceId) {
    // Отправляем DELETE запрос на сервер для удаления устройства по его ID
    fetch(`/rest-api/v1/devices/${deviceId}`, {
        method: 'DELETE'
    })
        .then(response => {
            if (response.ok) {
                // Перезагружаем список устройств после успешного удаления
                fetchDevices();
            } else {
                throw new Error('Failed to delete device');
            }
        })
        .catch(error => console.error('Error deleting device:', error));
}

// После загрузки страницы загружаем список устройств с сервера
window.onload = fetchDevices;
