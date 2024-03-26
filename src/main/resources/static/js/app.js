// app.js

// Получение списка типов устройств
function getDeviceTypes() {
    fetch('/rest-api/v1/device-types')
        .then(response => response.json())
        .then(deviceTypes => {
            const deviceTypesList = document.getElementById('deviceTypesList');
            deviceTypesList.innerHTML = '';
            deviceTypes.forEach(deviceType => {
                const li = document.createElement('li');
                li.textContent = deviceType.name;
                li.addEventListener('click', () => showDeviceTypeDetails(deviceType.id));
                deviceTypesList.appendChild(li);
            });
        })
        .catch(error => console.error('Error:', error));
}

// Показать детали конкретного типа устройства
function showDeviceTypeDetails(deviceTypeId) {
    fetch(`/rest-api/v1/device-types/${deviceTypeId}`)
        .then(response => response.json())
        .then(deviceType => {
            const deviceTypeDetails = document.getElementById('deviceTypeDetails');
            deviceTypeDetails.innerHTML = `
                <p><strong>Name:</strong> ${deviceType.name}</p>
                <p><strong>Description:</strong> ${deviceType.description}</p>
            `;
        })
        .catch(error => console.error('Error:', error));
}

// Отправка формы для создания нового типа устройства
function submitDeviceTypeForm(event) {
    event.preventDefault();
    const form = event.target;
    const formData = new FormData(form);
    const deviceTypeData = {
        name: formData.get('name'),
        description: formData.get('description')
    };

    fetch('/rest-api/v1/device-types', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(deviceTypeData)
    })
        .then(response => {
            if (response.ok) {
                // Очистить форму после успешного отправления
                form.reset();
                // Обновить список типов устройств
                getDeviceTypes();
            }
        })
        .catch(error => console.error('Error:', error));
}

// Получить список типов устройств при загрузке страницы
window.onload = function () {
    getDeviceTypes();

    // Назначаем обработчик события для отправки формы
    const form = document.getElementById('deviceTypeForm');
    form.addEventListener('submit', submitDeviceTypeForm);
};
