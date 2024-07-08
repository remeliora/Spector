function fetchParameterValues() {
    $.ajax({
        url: '/devices-data/values', // URL эндпоинта для получения значений параметров
        method: 'GET',
        success: function (data) {
            console.log('Fetched data:', data); // Логирование полученных данных
            // Обновление значений параметров на странице
            for (let key in data) {
                let element = document.querySelector(`[data-parameter-key="${key}"]`);
                if (element) {
                    console.log(`Updating ${key} to ${data[key]}`); // Логирование обновления элементов
                    element.textContent = data[key];
                } else {
                    console.warn(`Element with data-parameter-key="${key}" not found`);
                }
            }
        },
        error: function (xhr, status, error) {
            console.error('Failed to fetch updated parameters:', error);
            console.log('Response:', xhr.responseText);
        }
    });
}

// Устанавливаем интервал для обновления данных каждые 5 секунд
setInterval(fetchParameterValues, 5000);
