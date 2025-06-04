// Глобальные функции для работы с датами
function isNumberOrMinus(event) {
    const charCode = (event.which) ? event.which : event.keyCode;
    if (charCode > 31 && (charCode < 48 || charCode > 57) && charCode !== 45) {
        return false;
    }
    return true;
}

function formatDateInput(input) {
    let value = input.value.replace(/\D/g, '');
    
    if (value.length > 0) {
        // Добавляем дефисы автоматически
        if (value.length > 2 && value.length <= 4) {
            value = value.slice(0, 2) + '-' + value.slice(2);
        } else if (value.length > 4) {
            value = value.slice(0, 2) + '-' + value.slice(2, 4) + '-' + value.slice(4, 8);
        }
    }
    
    input.value = value;
    
    // Валидация только при полном вводе даты
    if (value.length === 10) {
        validateDateFormat(input);
    }
}

function validateDateFormat(input) {
    const value = input.value;
    if (!value) return true;

    // Проверяем формат ДД-ММ-ГГГГ
    const dateRegex = /^\d{2}-\d{2}-\d{4}$/;
    if (!dateRegex.test(value)) {
        alert('Пожалуйста, введите дату в формате ДД-ММ-ГГГГ');
        input.value = '';
        return false;
    }

    // Разбираем дату
    const [day, month, year] = value.split('-').map(Number);
    
    // Проверяем диапазоны
    if (year < 1900 || year > 2100) {
        alert('Год должен быть в диапазоне от 1900 до 2100');
        input.value = '';
        return false;
    }
    if (month < 1 || month > 12) {
        alert('Месяц должен быть от 01 до 12');
        input.value = '';
        return false;
    }
    if (day < 1 || day > 31) {
        alert('День должен быть от 01 до 31');
        input.value = '';
        return false;
    }

    // Проверяем количество дней в месяце
    const daysInMonth = new Date(year, month, 0).getDate();
    if (day > daysInMonth) {
        alert(`В ${month} месяце ${year} года ${daysInMonth} дней`);
        input.value = '';
        return false;
    }

    return true;
}

function formatDate(date) {
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();
    return `${day}-${month}-${year}`;
}

function validateYear(year) {
    return year >= 1900 && year <= 2100;
}

function handleDateChange(event) {
    const input = event.target;
    const date = new Date(input.value);
    
    if (isNaN(date.getTime())) {
        input.value = '';
        return;
    }
    
    if (!validateYear(date.getFullYear())) {
        alert('Год должен быть в диапазоне от 1900 до 2100');
        input.value = '';
        return;
    }
    
    // Форматируем дату в формат DD-MM-YYYY для отображения
    const formattedDate = formatDate(date);
    input.value = formattedDate;
}

function formatTimeInput(input) {
    let value = input.value.replace(/\D/g, '');
    
    if (value.length > 0) {
        // Добавляем дефис автоматически
        if (value.length > 2) {
            value = value.slice(0, 2) + '-' + value.slice(2, 4);
        }
    }
    
    input.value = value;
    
    // Валидация только при полном вводе времени
    if (value.length === 5) {
        validateTimeFormat(input);
    }
}

function validateTimeFormat(input) {
    const value = input.value;
    if (!value) return true;

    // Проверяем формат ЧЧ-ММ
    const timeRegex = /^\d{2}-\d{2}$/;
    if (!timeRegex.test(value)) {
        alert('Пожалуйста, введите время в формате ЧЧ-ММ');
        input.value = '';
        return false;
    }

    // Разбираем время
    const [hours, minutes] = value.split('-').map(Number);
    
    // Проверяем диапазоны
    if (hours < 0 || hours > 23) {
        alert('Часы должны быть от 00 до 23');
        input.value = '';
        return false;
    }
    if (minutes < 0 || minutes > 59) {
        alert('Минуты должны быть от 00 до 59');
        input.value = '';
        return false;
    }

    return true;
}

document.addEventListener('DOMContentLoaded', function () {
    const addButton = document.querySelector('.add-btn');
    const menuButton = document.querySelector('.menu-btn');
    const sideMenu = document.querySelector('.side-menu');
    const mainContainer = document.querySelector('.main-container');
    const header = document.querySelector('.header');
    const taskModal = document.getElementById('taskModal');
    const backBtn = document.getElementById('backBtn');
    const saveBtn = document.getElementById('saveBtn');
    const deleteTaskBtn = document.getElementById('deleteTaskBtn');
    const taskForm = document.getElementById('taskForm');
    const taskId = document.getElementById('taskId');
    const taskTitle = document.getElementById('taskTitle');
    const taskDescription = document.getElementById('taskDescription');
    const taskStartTime = document.getElementById('taskStartTime');
    const taskEndTime = document.getElementById('taskEndTime');
    const taskPriority = document.getElementById('taskPriority');
    const taskStatus = document.getElementById('taskStatus');

    // Загрузка задач при открытии страницы
    loadTasks();

    // Обработчики событий
    addButton.addEventListener('click', function() {
        taskId.value = '';
        taskForm.reset();
        taskDescription.innerHTML = '';
        openTaskModal();
    });

    menuButton.addEventListener('click', function(event) {
        console.log('Клик по кнопке меню');
        event.stopPropagation(); // Останавливаем всплытие события
        sideMenu.classList.toggle('active');
        mainContainer.classList.toggle('menu-active');
        header.classList.toggle('menu-active');
    });

    // Закрытие меню при клике вне его
    document.addEventListener('click', function(event) {
        console.log('Клик по документу');
        console.log('Цель клика:', event.target);
        console.log('Меню содержит цель:', sideMenu.contains(event.target));
        console.log('Кнопка меню содержит цель:', menuButton.contains(event.target));
        
        if (!sideMenu.contains(event.target) && !menuButton.contains(event.target)) {
            console.log('Закрываем меню');
            sideMenu.classList.remove('active');
            mainContainer.classList.remove('menu-active');
            header.classList.remove('menu-active');
        }
    });

    backBtn.addEventListener('click', function() {
        closeTaskModal();
    });

    deleteTaskBtn.addEventListener('click', function() {
        const id = taskId.value;
        if (id) {
            if (confirm('Вы уверены, что хотите удалить эту задачу?')) {
                fetch(`/tasks/${id}`, {
                    method: 'DELETE',
                    headers: {
                        'Accept': 'application/json'
                    }
                })
                .then(response => {
                    console.log('Получен ответ:', response.status, response.statusText);
                    if (!response.ok) {
                        return response.text().then(text => {
                            console.error('Ошибка сервера:', text);
                            throw new Error('Ошибка сервера: ' + response.status);
                        });
                    }
                    return response.json();
                })
                .then(result => {
                    console.log('Результат удаления:', result);
                    if (result.success) {
                        closeTaskModal();
                        loadTasks();
                    } else {
                        alert('Ошибка при удалении задачи: ' + (result.message || 'Неизвестная ошибка'));
                    }
                })
                .catch(error => {
                    console.error('Ошибка при удалении задачи:', error);
                    alert('Произошла ошибка при удалении задачи: ' + error.message);
                });
            }
        }
    });

    taskForm.addEventListener('submit', function(e) {
        e.preventDefault();
        saveTask();
    });

    // Функции
    function loadTasks() {
        console.log('Загрузка задач...');
        fetch('/tasks', {
            headers: {
                'Accept': 'application/json',
                'Cache-Control': 'no-cache'
            }
        })
        .then(response => {
            console.log('Получен ответ:', response.status, response.statusText);
            console.log('Content-Type:', response.headers.get('Content-Type'));
            if (!response.ok) {
                return response.text().then(text => {
                    console.error('Ошибка сервера:', text);
                    throw new Error('Ошибка сервера: ' + response.status);
                });
            }
            return response.text().then(text => {
                console.log('Получен текст ответа:', text);
                try {
                    return JSON.parse(text);
                } catch (e) {
                    console.error('Ошибка парсинга JSON:', e);
                    throw new Error('Неверный формат JSON');
                }
            });
        })
        .then(data => {
            console.log('Данные получены:', data);
            if (data.success) {
                const mainContainer = document.querySelector('.main-container');
                const taskList = document.querySelector('.notes-grid');
                
                // Очищаем только список задач
                taskList.innerHTML = '';
                
                data.tasks.forEach(task => {
                    const taskElement = createTaskElement(task);
                    taskList.appendChild(taskElement);
                });
            } else {
                console.error('Ошибка при загрузке задач:', data.message);
                alert('Ошибка при загрузке задач: ' + data.message);
            }
        })
        .catch(error => {
            console.error('Ошибка при загрузке задач:', error);
            alert('Ошибка при загрузке задач: ' + error.message);
        });
    }

    function createTaskElement(task) {
        const div = document.createElement('div');
        div.className = 'note-item';
        div.setAttribute('data-id', task.id);
        div.setAttribute('data-title', task.title);
        div.setAttribute('data-content', task.description || '');
        div.onclick = function() { openEditModal(this); };
        
        div.innerHTML = `
            <h3 class="note-title">${task.title}</h3>
            <div class="note-content">${task.description || ''}</div>
            <div class="task-info">
                <span class="task-date">Начало: ${formatDateTime(task.startTime)}</span>
                ${task.endTime ? `<span class="task-date">Окончание: ${formatDateTime(task.endTime)}</span>` : ''}
                <span class="task-priority">Приоритет: ${getPriorityText(task.priority)}</span>
                <div class="task-status">
                    <input type="checkbox" ${task.completed ? 'checked' : ''} onclick="event.stopPropagation(); toggleTaskStatus(${task.id})" />
                    <span>${task.completed ? 'Выполнено' : 'В процессе'}</span>
                </div>
            </div>
        `;
        return div;
    }

    function openTaskModal() {
        taskModal.style.display = 'flex';
        taskTitle.focus();
    }

    function closeTaskModal() {
        taskModal.style.display = 'none';
        taskForm.reset();
        taskDescription.innerHTML = '';
        document.getElementById('taskId').value = '';
    }

    function formatDateTime(dateTimeStr) {
        if (!dateTimeStr) return '';
        // Если дата уже в формате ДД-ММ-ГГГГ, возвращаем как есть
        if (/^\d{2}-\d{2}-\d{4}$/.test(dateTimeStr)) {
            return dateTimeStr;
        }
        // Иначе конвертируем из ISO формата
        const date = new Date(dateTimeStr);
        const day = String(date.getDate()).padStart(2, '0');
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const year = date.getFullYear();
        return `${day}-${month}-${year}`;
    }

    function formatDateTimeForInput(dateTimeStr) {
        if (!dateTimeStr) return '';
        // Если дата уже в формате ДД-ММ-ГГГГ, возвращаем как есть
        if (/^\d{2}-\d{2}-\d{4}$/.test(dateTimeStr)) {
            return dateTimeStr;
        }
        // Иначе конвертируем из ISO формата
        const date = new Date(dateTimeStr);
        const day = String(date.getDate()).padStart(2, '0');
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const year = date.getFullYear();
        return `${day}-${month}-${year}`;
    }

    function saveTask() {
        const taskData = {
            id: taskId.value,
            title: taskTitle.value,
            startTime: convertToISODateTime(taskDate.value, taskStartTime.value),
            endTime: convertToISODateTime(taskDate.value, taskEndTime.value),
            priority: taskPriority.value,
            completed: taskStatus.value === 'true',
            description: taskDescription.innerHTML,
            action: taskId.value ? 'update' : 'create'
        };

        function convertToISODateTime(dateStr, timeStr) {
            if (!dateStr || !timeStr) return null;
            // Разбиваем строку на компоненты даты (ДД-ММ-ГГГГ)
            const [day, month, year] = dateStr.split('-');
            // Разбиваем строку на компоненты времени (ЧЧ-ММ)
            const [hours, minutes] = timeStr.split('-');
            // Создаем полную дату и время в формате ISO
            return `${year}-${month}-${day}T${hours}:${minutes}:00`;
        }

        console.log('Отправка данных задачи:', taskData);

        fetch('/tasks', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(taskData)
        })
        .then(response => {
            console.log('Получен ответ:', response.status, response.statusText);
            if (!response.ok) {
                return response.text().then(text => {
                    console.error('Ошибка сервера:', text);
                    throw new Error('Ошибка сервера: ' + response.status);
                });
            }
            return response.json();
        })
        .then(result => {
            console.log('Результат сохранения:', result);
            if (result.success) {
                closeTaskModal();
                loadTasks();
            } else {
                alert('Ошибка при сохранении задачи: ' + (result.message || 'Неизвестная ошибка'));
            }
        })
        .catch(error => {
            console.error('Ошибка при сохранении задачи:', error);
            alert('Произошла ошибка при сохранении задачи: ' + error.message);
        });
    }

    // Функция для открытия модального окна редактирования
    window.openEditModal = function(element) {
        const id = element.getAttribute('data-id');
        const title = element.getAttribute('data-title');
        const content = element.getAttribute('data-content');
        
        console.log('Открытие модального окна для задачи:', id);
        
        fetch(`/tasks/${id}`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                'Cache-Control': 'no-cache'
            }
        })
        .then(response => {
            console.log('Получен ответ:', response.status, response.statusText);
            console.log('Content-Type:', response.headers.get('Content-Type'));
            if (!response.ok) {
                return response.text().then(text => {
                    console.error('Ошибка сервера:', text);
                    throw new Error('Ошибка сервера: ' + response.status);
                });
            }
            return response.text().then(text => {
                console.log('Получен текст ответа:', text);
                try {
                    return JSON.parse(text);
                } catch (e) {
                    console.error('Ошибка парсинга JSON:', e);
                    throw new Error('Неверный формат JSON');
                }
            });
        })
        .then(data => {
            console.log('Данные получены:', data);
            if (data.success && data.task) {
                taskId.value = data.task.id;
                taskTitle.value = data.task.title;
                taskDescription.innerHTML = data.task.description || '';
                
                // Форматируем дату и время для отображения в форме
                if (data.task.startTime) {
                    const startDateTime = new Date(data.task.startTime);
                    const day = String(startDateTime.getDate()).padStart(2, '0');
                    const month = String(startDateTime.getMonth() + 1).padStart(2, '0');
                    const year = startDateTime.getFullYear();
                    const hours = String(startDateTime.getHours()).padStart(2, '0');
                    const minutes = String(startDateTime.getMinutes()).padStart(2, '0');
                    
                    taskDate.value = `${day}-${month}-${year}`;
                    taskStartTime.value = `${hours}-${minutes}`;
                }
                
                if (data.task.endTime) {
                    const endDateTime = new Date(data.task.endTime);
                    const hours = String(endDateTime.getHours()).padStart(2, '0');
                    const minutes = String(endDateTime.getMinutes()).padStart(2, '0');
                    taskEndTime.value = `${hours}-${minutes}`;
                }
                
                taskPriority.value = data.task.priority;
                taskStatus.value = data.task.completed.toString();
                openTaskModal();
            } else {
                throw new Error('Данные задачи не получены');
            }
        })
        .catch(error => {
            console.error('Ошибка при получении задачи:', error);
            alert('Ошибка при получении задачи: ' + error.message);
        });
    };

    function toggleTaskStatus(taskId) {
        fetch(`/tasks/${taskId}/toggle`, {
            method: 'POST',
            headers: {
                'Accept': 'application/json'
            }
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Ошибка сети');
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                location.reload();
            } else {
                alert('Ошибка при изменении статуса: ' + data.message);
            }
        })
        .catch(error => {
            console.error('Ошибка при изменении статуса:', error);
            alert('Произошла ошибка при изменении статуса');
        });
    }

    function getPriorityText(priority) {
        const priorities = {
            'LOW': 'Низкий',
            'MEDIUM': 'Средний',
            'HIGH': 'Высокий'
        };
        return priorities[priority] || priority;
    }

    // Функции для работы с форматированием текста
    function toggleColorPalette() {
        const palette = document.getElementById('colorPalette');
        if (palette) {
            palette.style.display = palette.style.display === 'none' ? 'flex' : 'none';
        }
    }

    function applyColor(color) {
        document.execCommand('hiliteColor', false, color);
        toggleColorPalette();
    }
});
