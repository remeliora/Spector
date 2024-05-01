// Функция для отправки данных формы на сервер для создания или редактирования устройства
function submitDeviceForm(event) {
    event.preventDefault(); // Предотвращаем отправку формы по умолчанию

    const formData = new FormData(document.getElementById('device-form'));
    const deviceData = {
        name: formData.get('name'),
        ipAddress: formData.get('ipAddress'),
        description: formData.get('description'),
        period: formData.get('period'),
        alarmType: formData.get('alarmType'),
        isEnable: formData.get('isEnable') === 'on' // Преобразуем значение isEnable в булево
    };

    // Определяем метод (POST или PUT) и URL в зависимости от того, создаем ли мы новое устройство или редактируем существующее
    const method = deviceData.id ? 'PUT' : 'POST';
    const url = deviceData.id ? `/rest-api/v1/devices/${deviceData.id}` : '/rest-api/v1/devices';

    // Отправляем запрос на сервер
    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(deviceData)
    })
        .then(response => {
            if (response.ok) {
                // Перенаправляем пользователя на страницу со списком устройств после успешного создания или редактирования
                window.location.href = '/devices';
            } else {
                throw new Error('Failed to submit form');
            }
        })
        .catch(error => console.error('Error submitting form:', error));
}

// Добавляем обработчик события для отправки формы на сервер
document.getElementById('device-form').addEventListener('submit', submitDeviceForm);
