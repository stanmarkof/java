// Файл для форматирования текста в модальных окнах
// Здесь будет ваша логика форматирования 

// Функции для форматирования текста
function makeBold() {
    console.log('Попытка сделать текст жирным');
    const editor = document.querySelector('#noteContent, #taskDescription');
    if (editor) {
        editor.focus();
        document.execCommand('bold', false, null);
        console.log('Форматирование применено');
    }
}

function makeItalic() {
    console.log('Попытка сделать текст курсивом');
    const editor = document.querySelector('#noteContent, #taskDescription');
    if (editor) {
        editor.focus();
        document.execCommand('italic', false, null);
        console.log('Форматирование применено');
    }
}

function makeUnderline() {
    console.log('Попытка сделать текст подчеркнутым');
    const editor = document.querySelector('#noteContent, #taskDescription');
    if (editor) {
        editor.focus();
        document.execCommand('underline', false, null);
        console.log('Форматирование применено');
    }
}

// Инициализация форматирования при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    // Добавляем обработчики для кнопок форматирования
    const formatButtons = document.querySelectorAll('.editor-btn');
    formatButtons.forEach(button => {
        button.addEventListener('mousedown', function(e) {
            e.preventDefault(); // Предотвращаем потерю фокуса
            const command = this.getAttribute('title').toLowerCase();
            const editor = document.querySelector('#noteContent, #taskDescription');
            if (editor) {
                editor.focus();
                switch(command) {
                    case 'жирный':
                        document.execCommand('bold', false, null);
                        break;
                    case 'курсив':
                        document.execCommand('italic', false, null);
                        break;
                    case 'подчеркнутый':
                        document.execCommand('underline', false, null);
                        break;
                }
            }
        });
    });

    // Добавляем обработчики для полей редактирования
    const editableFields = document.querySelectorAll('.note-text');
    editableFields.forEach(field => {
        field.addEventListener('click', function() {
            console.log('Поле редактирования получило фокус:', this.id);
        });
        
        field.addEventListener('focus', function() {
            console.log('Поле редактирования в фокусе:', this.id);
        });
        
        field.addEventListener('blur', function() {
            console.log('Поле редактирования потеряло фокус:', this.id);
        });
    });
}); 