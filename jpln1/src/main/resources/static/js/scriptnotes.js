console.log('Script file loaded');

function navigateTo(url) {
    window.location.href = url;
}

function openEditModal(element) {
    console.log('Opening edit modal');
    const id = element.getAttribute("data-id");
    const title = element.getAttribute("data-title");
    const content = element.getAttribute("data-content");

    const noteId = document.getElementById("noteId");
    const noteTitle = document.getElementById("noteTitle");
    const noteContent = document.getElementById("noteContent");
    const noteModal = document.getElementById("noteModal");

    if (noteId) noteId.value = id;
    if (noteTitle) noteTitle.value = title;
    if (noteContent) noteContent.innerHTML = content;
    if (noteModal) noteModal.style.display = "flex";
}

document.addEventListener('DOMContentLoaded', function () {
    console.log('DOM Content Loaded');
    
    // Получаем все необходимые элементы
    const addButton = document.querySelector('.add-btn');
    const menuButton = document.querySelector('.menu-btn');
    const sideMenu = document.querySelector('.side-menu');
    const mainContainer = document.querySelector('.main-container');
    const header = document.querySelector('.header');
    const noteModal = document.getElementById('noteModal');
    const backBtn = document.getElementById('backBtn');
    const saveBtn = document.getElementById('saveBtn');
    const paletteButton = document.querySelector('[data-action="palette"]');
    const colorPalette = document.getElementById('colorPalette');
    const noteContent = document.querySelector('.note-content');
    const noteTextArea = document.getElementById('noteContent');
    const noteTitle = document.querySelector('.note-title');
    const form = document.querySelector('form');

    // Проверяем наличие элементов
    console.log('Elements found:', {
        addButton: !!addButton,
        menuButton: !!menuButton,
        sideMenu: !!sideMenu,
        mainContainer: !!mainContainer,
        header: !!header,
        noteModal: !!noteModal,
        backBtn: !!backBtn,
        saveBtn: !!saveBtn,
        paletteButton: !!paletteButton,
        colorPalette: !!colorPalette,
        noteContent: !!noteContent,
        noteTextArea: !!noteTextArea,
        noteTitle: !!noteTitle,
        form: !!form
    });
    
    // Создаем скрытое поле для контента, если его нет
    let hiddenContent = document.getElementById('hiddenNoteContent');
    if (!hiddenContent && form) {
        hiddenContent = document.createElement('input');
        hiddenContent.type = 'hidden';
        hiddenContent.name = 'content';
        hiddenContent.id = 'hiddenNoteContent';
        form.appendChild(hiddenContent);
    }

    // Проверяем наличие всех необходимых элементов
    if (!noteTextArea || !noteTitle || !hiddenContent || !form) {
        console.error('Required elements not found');
        return;
    }

    // Обработчик сохранения заметки
    if (saveBtn) {
        saveBtn.addEventListener('click', function (event) {
            event.preventDefault();
            
            // Проверяем наличие элементов перед использованием
            if (!noteTitle || !noteTextArea || !hiddenContent || !form) {
                return;
            }

            // Безопасное получение значений с проверкой на undefined
            const title = (noteTitle.value || '').trim();
            const content = (noteTextArea.innerHTML || '').trim();

            if (!title && !content) {
                noteModal.style.display = 'none';
                return;
            }

            hiddenContent.value = content || ' ';
            form.submit();
        });
    }

    addButton.addEventListener('click', function (event) {
        event.stopPropagation();
        noteModal.style.display = 'flex';
        // Очищаем поля при создании новой заметки
        const noteId = document.getElementById("noteId");
        const noteTitle = document.getElementById("noteTitle");
        const noteContent = document.getElementById("noteContent");
        if (noteId) noteId.value = "";
        if (noteTitle) noteTitle.value = "";
        if (noteContent) noteContent.innerHTML = "";
    });

    if (noteTextArea) {
        noteTextArea.addEventListener('focus', function () {
            if (this.textContent === '') {
                this.textContent = '';
            }
        });

        noteTextArea.addEventListener('blur', function () {
            if (this.textContent === '') {
                this.innerHTML = '';
            }
        });
    }

    // Обработчик клика по кнопке меню
    menuButton.addEventListener('click', function(event) {
        console.log('Клик по кнопке меню');
        event.stopPropagation();
        sideMenu.classList.toggle('active');
        mainContainer.classList.toggle('menu-active');
        header.classList.toggle('menu-active');
    });

    // Закрытие меню при клике вне его
    document.addEventListener('click', function(event) {
        if (!sideMenu.contains(event.target) && !menuButton.contains(event.target)) {
            sideMenu.classList.remove('active');
            mainContainer.classList.remove('menu-active');
            header.classList.remove('menu-active');
        }
    });

    backBtn.addEventListener('click', function () {
        noteModal.style.display = 'none';
    });

    paletteButton.addEventListener('click', function (event) {
        event.stopPropagation();
        colorPalette.classList.toggle('active');
    });

    colorPalette.addEventListener('click', function (event) {
        if (event.target.classList.contains('color-option')) {
            const selectedColor = event.target.getAttribute('data-color');
            noteContent.style.backgroundColor = selectedColor;
            colorPalette.classList.remove('active');
        }
    });

    window.addEventListener('click', function () {
        colorPalette.classList.remove('active');
    });

    const boldButton = document.querySelector('[data-action="bold"]');
    boldButton.addEventListener('click', function () {
        document.execCommand('bold', false, null);
        noteTextArea.focus();
        updateButtonState();
    });

    const italicButton = document.querySelector('[data-action="italic"]');
    italicButton.addEventListener('click', function () {
        document.execCommand('italic', false, null);
        noteTextArea.focus();
        updateButtonState();
    });

    const underlineButton = document.querySelector('[data-action="underline"]');
    underlineButton.addEventListener('click', function () {
        document.execCommand('underline', false, null);
        noteTextArea.focus();
        updateButtonState();
    });

    const hiliteColorButton = document.querySelector('[data-action="hiliteColor"]');
    let isHighlightActive = false;
    const highlightColor = 'yellow';

    function toggleHighlight() {
        document.execCommand('backColor', false, isHighlightActive ? 'transparent' : highlightColor);
        isHighlightActive = !isHighlightActive;
        hiliteColorButton.classList.toggle('active', isHighlightActive);
        noteTextArea.focus();
    }

    hiliteColorButton.addEventListener('click', toggleHighlight);

    function updateButtonState() {
        if (!noteTextArea) return;
        
        const isBold = document.queryCommandState('bold');
        const isItalic = document.queryCommandState('italic');
        const isUnderline = document.queryCommandState('underline');

        setButtonState('bold', isBold);
        setButtonState('italic', isItalic);
        setButtonState('underline', isUnderline);
    }

    function setButtonState(action, isActive) {
        const button = document.querySelector(`[data-action="${action}"]`);
        if (button) {
            button.classList.toggle('active', isActive);
        }
    }

    const deleteBtn = document.getElementById("deleteNoteBtn");

    deleteBtn.addEventListener("click", function () {
        const id = document.getElementById("noteId").value;
        if (!id) {
            return;
        }
        if (confirm("Удалить эту заметку?")) {
            const form = document.createElement('form');
            form.method = 'POST';
            form.action = '/notes';

            const actionInput = document.createElement('input');
            actionInput.type = 'hidden';
            actionInput.name = 'action';
            actionInput.value = 'delete';

            const idInput = document.createElement('input');
            idInput.type = 'hidden';
            idInput.name = 'id';
            idInput.value = id;

            form.appendChild(actionInput);
            form.appendChild(idInput);
            document.body.appendChild(form);
            form.submit();
        }
    });
});

