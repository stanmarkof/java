document.addEventListener('DOMContentLoaded', function () {
    // Получаем все необходимые элементы
    const menuBtn = document.querySelector('.menu-btn');
    const optionsBtn = document.querySelector('.options-btn');
    const addBtn = document.querySelector('.add-btn');
    const actionButtonsContainer = document.querySelector('.action-buttons-container');
    const actionButtons = document.querySelectorAll('.action-button');
    const sideMenu = document.querySelector('.side-menu');
    const mainContainer = document.querySelector('.main-container');
    const header = document.querySelector('.header');

    // Обработка клика по кнопке меню
    menuBtn.addEventListener('click', function() {
        console.log('Меню нажато');
        sideMenu.classList.toggle('active');
        mainContainer.classList.toggle('menu-active');
        header.classList.toggle('menu-active');
    });

    // Закрытие меню при клике вне его
    document.addEventListener('click', function(event) {
        if (!sideMenu.contains(event.target) && !menuBtn.contains(event.target)) {
            sideMenu.classList.remove('active');
            mainContainer.classList.remove('menu-active');
            header.classList.remove('menu-active');
        }
    });

    // Обработка клика по кнопке опций
    optionsBtn.addEventListener('click', function() {
        console.log('Опции нажаты');
        // Здесь можно добавить логику для открытия меню опций
    });

    // Обработка клика по кнопке добавления
    addBtn.addEventListener('click', function() {
        const isVisible = actionButtonsContainer.style.display === 'flex';
        actionButtonsContainer.style.display = isVisible ? 'none' : 'flex';
    });

    // Обработка кликов по кнопкам действий
    actionButtons.forEach(button => {
        button.addEventListener('click', function() {
            console.log('Выбрано действие:', this.textContent);
            actionButtonsContainer.style.display = 'none';
        });
    });

    // Остальной код календаря остается без изменений
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
});

