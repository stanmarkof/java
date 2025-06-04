// Глобальные функции для форматирования времени и даты
function formatTimeInput(input) {
    let value = input.value.replace(/\D/g, '');
    
    // Разделяем на часы и минуты
    let hours = value.slice(0, 2);
    let minutes = value.slice(2, 4);
    
    // Валидация часов
    if (hours.length === 2) {
        let hoursNum = parseInt(hours);
        if (hoursNum > 23) {
            hours = '23';
        }
    }
    
    // Валидация минут
    if (minutes.length === 2) {
        let minutesNum = parseInt(minutes);
        if (minutesNum > 59) {
            minutes = '59';
        }
    }
    
    // Форматируем значение
    if (value.length > 2) {
        value = hours + '-' + minutes;
    }
    
    input.value = value;
}

function formatDateInput(input) {
    let value = input.value.replace(/\D/g, '');
    if (value.length > 2) {
        value = value.slice(0, 2) + '-' + value.slice(2, 4);
    }
    if (value.length > 5) {
        value = value.slice(0, 5) + '-' + value.slice(5, 9);
    }
    input.value = value;
}

function isNumberOrMinus(event) {
    const charCode = (event.which) ? event.which : event.keyCode;
    if (charCode > 31 && (charCode < 48 || charCode > 57) && charCode !== 45) {
        return false;
    }
    return true;
}

document.addEventListener('DOMContentLoaded', function () {
    console.log('Скрипт календаря загружен');
    console.log('DOM загружен, инициализация календаря');
    // Получаем все необходимые элементы
    const menuBtn = document.querySelector('.menu-btn');
    const optionsBtn = document.querySelector('.options-btn');
    const addBtn = document.querySelector('.add-btn');
    const sideMenu = document.querySelector('.side-menu');
    const mainContainer = document.querySelector('.main-container');
    const header = document.querySelector('.header');
    const actionButtonsContainer = document.querySelector('.action-buttons-container');
    const actionButtons = document.querySelectorAll('.action-button');

    // Обработчик для кнопки "+"
    addBtn.addEventListener('click', function (event) {
        event.stopPropagation();
        actionButtonsContainer.classList.toggle('visible');
    });

    // Закрытие контейнера с кнопками при клике вне его области
    window.addEventListener('click', function (event) {
        if (!event.target.closest('.add-btn') && !event.target.closest('.action-buttons-container')) {
            actionButtonsContainer.classList.remove('visible');
        }
    });

    // Обработчик для кнопок действия
    actionButtons.forEach(button => {
        button.addEventListener('click', function() {
            const action = this.textContent.trim();
            if (action === 'Задачи') {
                openTaskModal();
            } else if (action === 'Заметки') {
                openNoteModal();
            }
            actionButtonsContainer.classList.remove('visible');
        });
    });

    // Обработчик для кнопки меню
    menuBtn.addEventListener('click', function () {
        sideMenu.classList.toggle('active');
        mainContainer.classList.toggle('menu-active');
        header.classList.toggle('menu-active');
    });

    // Закрытие меню при клике вне его области
    document.addEventListener('click', function(event) {
        if (!event.target.closest('.side-menu') && !event.target.closest('.menu-btn')) {
            sideMenu.classList.remove('active');
            mainContainer.classList.remove('menu-active');
            header.classList.remove('menu-active');
        }
    });

    // Обработка клика по кнопке опций
    optionsBtn.addEventListener('click', function() {
        console.log('Опции нажаты');
    });

    // Код календаря
    const calendar = document.querySelector('.calendar');
    const prevBtn = document.querySelector('.nav-arrow.left');
    const nextBtn = document.querySelector('.nav-arrow.right');
    const currentDate = new Date();
    let currentYear = currentDate.getFullYear();
    let currentMonth = currentDate.getMonth();
    let selectedDay = currentDate.getDate();

    const selectedDateElement = document.getElementById('selected-date');

    // Русские названия месяцев
    const monthNames = [
        'Январь', 'Февраль', 'Март', 'Апрель', 'Май', 'Июнь',
        'Июль', 'Август', 'Сентябрь', 'Октябрь', 'Ноябрь', 'Декабрь'
    ];

    // Склонённые названия месяцев в родительном падеже
    const monthNamesGenitive = [
        'Января', 'Февраля', 'Марта', 'Апреля', 'Мая', 'Июня',
        'Июля', 'Августа', 'Сентября', 'Октября', 'Ноября', 'Декабря'
    ];

    // Русские названия дней недели, начиная с понедельника
    const daysOfWeek = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];

    function renderCalendar(year, month) {
        const firstDay = (new Date(year, month, 1).getDay() + 6) % 7;
        const daysInMonth = new Date(year, month + 1, 0).getDate();
        const daysInPrevMonth = new Date(year, month, 0).getDate();

        const monthElement = calendar.querySelector('.month');
        const yearElement = calendar.querySelector('.year');
        const daysContainer = calendar.querySelector('tbody');

        monthElement.textContent = monthNames[month];
        yearElement.textContent = year;

        daysContainer.innerHTML = '';

        let row = document.createElement('tr');
        daysOfWeek.forEach(day => {
            const th = document.createElement('th');
            th.textContent = day;
            row.appendChild(th);
        });
        daysContainer.appendChild(row);

        row = document.createElement('tr');
        let dayCounter = 1;
        let prevMonthDayCounter = daysInPrevMonth - firstDay + 1;

        for (let i = 0; i < firstDay; i++) {
            const cell = document.createElement('td');
            cell.textContent = prevMonthDayCounter++;
            cell.classList.add('prev-month');
            row.appendChild(cell);
        }

        for (let i = firstDay; i < 7; i++) {
            const cell = document.createElement('td');
            cell.textContent = dayCounter++;
            if (year === currentYear && month === currentMonth && cell.textContent == selectedDay) {
                cell.classList.add('selected');
            }
            cell.addEventListener('click', function () {
                selectDay(this.textContent);
            });
            row.appendChild(cell);
        }

        daysContainer.appendChild(row);

        while (dayCounter <= daysInMonth) {
            row = document.createElement('tr');
            for (let i = 0; i < 7; i++) {
                if (dayCounter > daysInMonth) {
                    const cell = document.createElement('td');
                    cell.textContent = dayCounter - daysInMonth;
                    cell.classList.add('next-month');
                    row.appendChild(cell);
                    dayCounter++;
                } else {
                    const cell = document.createElement('td');
                    cell.textContent = dayCounter++;
                    if (year === currentYear && month === currentMonth && cell.textContent == selectedDay) {
                        cell.classList.add('selected');
                    }
                    cell.addEventListener('click', function () {
                        selectDay(this.textContent);
                    });
                    row.appendChild(cell);
                }
            }
            daysContainer.appendChild(row);
        }

        while (daysContainer.querySelectorAll('tr').length < 7) {
            row = document.createElement('tr');
            for (let i = 0; i < 7; i++) {
                const cell = document.createElement('td');
                cell.textContent = dayCounter - daysInMonth;
                cell.classList.add('next-month');
                row.appendChild(cell);
                dayCounter++;
            }
            daysContainer.appendChild(row);
        }

        updateSelectedDate();
    }

    function selectDay(day) {
        selectedDay = day;
        document.querySelectorAll('td.selected').forEach(td => td.classList.remove('selected'));
        document.querySelectorAll('td').forEach(td => {
            if (td.textContent == day) {
                td.classList.add('selected');
            }
        });
        updateSelectedDate();
    }

    function updateSelectedDate() {
        selectedDateElement.textContent = `${selectedDay} ${monthNamesGenitive[currentMonth]} ${currentYear}`;
        loadTasksForDate();
    }

    function loadTasksForDate() {
        const date = new Date(currentYear, currentMonth, selectedDay);
        const formattedDate = date.toISOString().split('T')[0];
        
        fetch(`/tasks/date/${formattedDate}`)
            .then(response => response.json())
            .then(data => {
                const taskContainer = document.querySelector('.notes-grid');
                taskContainer.innerHTML = '';
                
                if (data.success && data.tasks) {
                    data.tasks.forEach(task => {
                        const taskElement = createTaskElement(task);
                        taskContainer.appendChild(taskElement);
                    });
                }
            })
            .catch(error => {
                console.error('Ошибка при загрузке задач:', error);
            });
    }

    function createTaskElement(task) {
        const div = document.createElement('div');
        div.className = 'note-item';
        div.setAttribute('data-id', task.id);
        div.setAttribute('data-title', task.title);
        div.setAttribute('data-content', task.description || '');
        
        div.innerHTML = `
            <h3 class="note-title">${task.title}</h3>
            <div class="note-content">${task.description || ''}</div>
            <div class="task-info">
                <span class="task-priority">Приоритет: ${getPriorityText(task.priority)}</span>
                <div class="task-status">
                    <input type="checkbox" ${task.completed ? 'checked' : ''} onclick="event.stopPropagation(); toggleTaskStatus(${task.id})" />
                    <span>${task.completed ? 'Выполнено' : 'В процессе'}</span>
                </div>
            </div>
        `;
        return div;
    }

    function getPriorityText(priority) {
        const priorities = {
            'LOW': 'Низкий',
            'MEDIUM': 'Средний',
            'HIGH': 'Высокий'
        };
        return priorities[priority] || priority;
    }

    function toggleTaskStatus(taskId) {
        fetch(`/tasks/${taskId}/toggle`, {
            method: 'POST',
            headers: {
                'Accept': 'application/json'
            }
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                loadTasksForDate();
            }
        })
        .catch(error => {
            console.error('Ошибка при изменении статуса:', error);
        });
    }

    prevBtn.addEventListener('click', function () {
        currentMonth--;
        if (currentMonth < 0) {
            currentMonth = 11;
            currentYear--;
        }
        renderCalendar(currentYear, currentMonth);
    });

    nextBtn.addEventListener('click', function () {
        currentMonth++;
        if (currentMonth > 11) {
            currentMonth = 0;
            currentYear++;
        }
        renderCalendar(currentYear, currentMonth);
    });

    renderCalendar(currentYear, currentMonth);

    // Функции для работы с модальными окнами
    function openTaskModal() {
        const modal = document.getElementById('taskModal');
        modal.style.display = 'block';
        
        // Установка текущей даты
        const dateInput = document.getElementById('taskDate');
        const today = new Date();
        dateInput.value = `${today.getDate().toString().padStart(2, '0')}-${(today.getMonth() + 1).toString().padStart(2, '0')}-${today.getFullYear()}`;
    }

    function openNoteModal() {
        const modal = document.getElementById('noteModal');
        modal.style.display = 'block';
    }

    // Обработчики закрытия модальных окон
    document.getElementById('backBtn').addEventListener('click', function() {
        document.getElementById('taskModal').style.display = 'none';
    });

    document.getElementById('noteBackBtn').addEventListener('click', function() {
        document.getElementById('noteModal').style.display = 'none';
    });

    // Обработчики форм
    document.getElementById('taskForm').addEventListener('submit', function(e) {
        e.preventDefault();
        const formData = new FormData(this);
        
        function convertToISODateTime(dateStr, timeStr) {
            if (!dateStr || !timeStr) return null;
            // Разбиваем строку на компоненты даты (ДД-ММ-ГГГГ)
            const [day, month, year] = dateStr.split('-');
            // Разбиваем строку на компоненты времени (ЧЧ-ММ)
            const [hours, minutes] = timeStr.split('-');
            // Создаем полную дату и время в формате ISO
            return `${year}-${month}-${day}T${hours}:${minutes}:00`;
        }
        
        const taskData = {
            title: formData.get('title'),
            description: document.getElementById('taskDescription').innerHTML,
            startTime: convertToISODateTime(formData.get('date'), formData.get('startTime')),
            endTime: convertToISODateTime(formData.get('date'), formData.get('endTime')),
            priority: formData.get('priority'),
            completed: formData.get('completed') === 'true'
        };

        console.log('Отправляемые данные:', taskData);

        fetch('/tasks', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(taskData)
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                // Закрываем модальное окно
                document.getElementById('taskModal').style.display = 'none';
                // Очищаем форму
                this.reset();
                // Показываем сообщение об успехе
                alert('Задача успешно сохранена');
                // Обновляем список задач
                loadTasksForDate();
            } else {
                console.error('Ошибка при сохранении задачи:', data.message);
                alert('Ошибка при сохранении задачи: ' + data.message);
            }
        })
        .catch(error => {
            console.error('Ошибка при сохранении задачи:', error);
            alert('Произошла ошибка при сохранении задачи: ' + error.message);
        });
    });

    document.getElementById('noteForm').addEventListener('submit', function(e) {
        e.preventDefault();
        console.log('Начало обработки формы заметки');
        
        // Получаем элементы формы
        const noteContentElement = document.querySelector('#noteModal .note-text');
        const noteTitleElement = document.querySelector('#noteModal .note-title');
        
        // Проверяем существование элементов
        if (!noteContentElement || !noteTitleElement) {
            console.error('Не найдены элементы формы:', {
                content: noteContentElement,
                title: noteTitleElement
            });
            alert('Ошибка: не найдены элементы формы');
            return;
        }
        
        // Получаем значения
        const noteContent = noteContentElement.innerHTML;
        const noteTitle = noteTitleElement.value;
        
        console.log('Заголовок заметки:', noteTitle);
        console.log('Содержимое заметки:', noteContent);
        
        // Проверяем, что поля не пустые
        if (!noteTitle.trim()) {
            alert('Пожалуйста, введите заголовок заметки');
            return;
        }
        
        if (!noteContent.trim()) {
            alert('Пожалуйста, введите содержимое заметки');
            return;
        }
        
        // Создаем FormData
        const formData = new FormData();
        formData.append('title', noteTitle);
        formData.append('content', noteContent);
        formData.append('returnTo', 'calendar');
        
        // Логируем данные
        console.log('Отправляемые данные:');
        for (let pair of formData.entries()) {
            console.log(pair[0] + ': ' + pair[1]);
        }

        // Отправляем запрос
        console.log('Отправка запроса...');
        fetch('/notes', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: new URLSearchParams(formData)
        })
        .then(response => {
            console.log('Получен ответ от сервера');
            console.log('Статус:', response.status);
            
            // Если статус 200, считаем что заметка сохранена
            if (response.status === 200) {
                // Закрываем модальное окно
                document.getElementById('noteModal').style.display = 'none';
                // Очищаем форму
                this.reset();
                // Показываем сообщение об успехе
                alert('Заметка успешно сохранена');
            } else {
                throw new Error('Ошибка при сохранении заметки');
            }
        })
        .catch(error => {
            console.error('Ошибка:', error);
            alert('Произошла ошибка при сохранении заметки: ' + error.message);
        });
    });

    // Восстанавливаем консоль при загрузке страницы
    window.addEventListener('load', function() {
        const savedLogs = localStorage.getItem('noteSaveLogs');
        if (savedLogs) {
            console.log('Логи предыдущего сохранения заметки:');
            JSON.parse(savedLogs).forEach(([type, message]) => {
                if (type === 'LOG') {
                    console.log(message);
                } else {
                    console.error(message);
                }
            });
            localStorage.removeItem('noteSaveLogs');
        }
    });

    // Обработчики кнопок удаления
    document.getElementById('deleteTaskBtn').addEventListener('click', function() {
        const taskId = document.getElementById('taskId').value;
        if (taskId) {
            if (confirm('Вы уверены, что хотите удалить эту задачу?')) {
                fetch(`/tasks/${taskId}`, {
                    method: 'DELETE',
                    headers: {
                        'Accept': 'application/json'
                    }
                })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        document.getElementById('taskModal').style.display = 'none';
                        loadTasksForDate();
                    }
                })
                .catch(error => {
                    console.error('Ошибка при удалении задачи:', error);
                });
            }
        }
    });

    document.getElementById('deleteNoteBtn').addEventListener('click', function() {
        const noteId = document.getElementById('noteId').value;
        if (noteId) {
            if (confirm('Вы уверены, что хотите удалить эту заметку?')) {
                fetch(`/notes/${noteId}`, {
                    method: 'DELETE',
                    headers: {
                        'Accept': 'application/json'
                    }
                })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        document.getElementById('noteModal').style.display = 'none';
                    }
                })
                .catch(error => {
                    console.error('Ошибка при удалении заметки:', error);
                });
            }
        }
    });
}); 