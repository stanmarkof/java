console.log('Script file loaded');

function navigateTo(url) {
    window.location.href = url;
}

function openEditModal(element) {
    console.log('Opening edit modal');
    const id = element.getAttribute("data-id");
    const title = element.getAttribute("data-title");
    const content = element.getAttribute("data-content");
    const folderId = element.getAttribute("data-folder-id");

    console.log('Note data from element:', {
        id: id,
        title: title,
        content: content,
        folderId: folderId
    });

    const noteId = document.getElementById("noteId");
    const noteTitle = document.getElementById("noteTitle");
    const noteContent = document.getElementById("noteContent");
    const noteFolder = document.getElementById("noteFolder");
    const noteModal = document.getElementById("noteModal");

    if (noteId) noteId.value = id;
    if (noteTitle) noteTitle.value = title;
    if (noteContent) noteContent.innerHTML = content;
    if (noteFolder) {
        // Сначала загружаем список папок
        loadFolderSelect().then(() => {
            // После загрузки папок устанавливаем значение
            noteFolder.value = folderId;
            console.log('Set folder value to:', folderId);
        });
    }
    if (noteModal) noteModal.style.display = "flex";
}

// Функция для загрузки заметок
function loadNotes() {
    console.log('Loading notes...');
    fetch('/api/notes/', {
        method: 'GET',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        credentials: 'same-origin'
    })
    .then(response => {
        console.log('Notes response status:', response.status);
        if (response.status === 401) {
            window.location.href = '/login';
            throw new Error('Unauthorized');
        }
        if (!response.ok) {
            return response.text().then(text => {
                console.error('Server response:', text);
                throw new Error(`HTTP error! status: ${response.status}, message: ${text}`);
            });
        }
        return response.json();
    })
    .then(notes => {
        console.log('Loaded notes:', notes);
        const notesContainer = document.querySelector('.notes-grid');
        if (!notesContainer) {
            console.error('Notes container not found');
            return;
        }
        notesContainer.innerHTML = '';
        
        if (Array.isArray(notes)) {
            notes.forEach(note => {
                const noteElement = createNoteElement(note);
                notesContainer.appendChild(noteElement);
            });
        } else {
            console.error('Invalid notes data received:', notes);
        }
    })
    .catch(error => {
        console.error('Error loading notes:', error);
        if (error.message !== 'Unauthorized') {
            const notesContainer = document.querySelector('.notes-grid');
            if (notesContainer) {
                notesContainer.innerHTML = '<div class="error-message">Не удалось загрузить заметки. Пожалуйста, попробуйте позже.</div>';
            }
        }
    });
}

// Функция для создания элемента заметки
function createNoteElement(note) {
    console.log('Creating note element:', note);
    const div = document.createElement('div');
    div.className = 'note-item';
    
    // Безопасное получение значений с проверкой на null/undefined
    const noteId = note.id || '';
    const noteTitle = note.title || '';
    const noteContent = note.content || '';
    const noteFolderId = note.folderId || '';

    console.log('Note data for element:', {
        id: noteId,
        title: noteTitle,
        content: noteContent,
        folderId: noteFolderId
    });

    div.setAttribute('data-id', noteId);
    div.setAttribute('data-title', noteTitle);
    div.setAttribute('data-content', noteContent);
    div.setAttribute('data-folder-id', noteFolderId);
    div.onclick = () => openEditModal(div);

    const title = document.createElement('h3');
    title.className = 'note-title';
    title.textContent = noteTitle;

    const content = document.createElement('div');
    content.className = 'note-content';
    content.innerHTML = noteContent;

    div.appendChild(title);
    div.appendChild(content);
    return div;
}

// Функция для сохранения заметки
function saveNote(event) {
    event.preventDefault();
    
    const noteId = document.getElementById('noteId').value;
    const noteTitle = document.getElementById('noteTitle').value;
    const noteContent = document.getElementById('noteContent').innerHTML;
    const folderId = document.getElementById('noteFolder').value;
    
    const note = {
        id: noteId ? parseInt(noteId) : null,
        title: noteTitle,
        content: noteContent,
        folderId: folderId ? parseInt(folderId) : null
    };

    const method = noteId ? 'PUT' : 'POST';
    const url = noteId ? `/api/notes/${noteId}` : '/api/notes';

    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(note)
    })
    .then(response => response.json())
    .then(() => {
        document.getElementById('noteModal').style.display = 'none';
        loadNotes();
    })
    .catch(error => console.error('Ошибка при сохранении заметки:', error));
}

// Функция для удаления заметки
function deleteNote() {
    const noteId = document.getElementById('noteId').value;
    if (!noteId) return;

    if (confirm('Вы уверены, что хотите удалить эту заметку?')) {
        fetch(`/api/notes/${noteId}`, {
            method: 'DELETE',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            credentials: 'same-origin'
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Ошибка при удалении заметки');
            }
            document.getElementById('noteModal').style.display = 'none';
            loadNotes();
        })
        .catch(error => {
            console.error('Ошибка при удалении заметки:', error);
            alert('Не удалось удалить заметку');
        });
    }
}

// Функция для открытия модального окна создания папки
function openFolderModal() {
    const folderModal = document.getElementById('folderModal');
    folderModal.style.display = 'flex';
}

// Функция для загрузки папок в выпадающий список
function loadFolderSelect() {
    const folderSelect = document.getElementById('noteFolder');
    if (!folderSelect) {
        console.error('Folder select element not found');
        return Promise.reject('Folder select element not found');
    }

    return fetch('/api/folders/', {
        method: 'GET',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        credentials: 'same-origin'
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Ошибка при загрузке папок');
        }
        return response.json();
    })
    .then(folders => {
        console.log('Loaded folders:', folders);
        // Очищаем список, оставляя только опцию "Без папки"
        folderSelect.innerHTML = '<option value="">Без папки</option>';
        
        // Добавляем папки в список
        if (Array.isArray(folders)) {
            folders.forEach(folder => {
                const option = document.createElement('option');
                option.value = folder.id;
                option.textContent = folder.name;
                folderSelect.appendChild(option);
            });
        }
        return folders;
    })
    .catch(error => {
        console.error('Error loading folders:', error);
        throw error;
    });
}

document.addEventListener('DOMContentLoaded', function() {
    console.log('Script loaded');
    
    // Получаем все необходимые элементы
    const menuBtn = document.querySelector('.menu-btn');
    const sideMenu = document.querySelector('.side-menu');
    const mainContainer = document.querySelector('.main-container');
    const addBtn = document.querySelector('.add-btn');
    const createFolderBtn = document.querySelector('.create-folder-btn');
    const noteModal = document.getElementById('noteModal');
    const folderModal = document.getElementById('folderModal');
    const noteForm = document.getElementById('noteForm');
    const folderForm = document.getElementById('folderForm');
    const backBtn = document.getElementById('backBtn');
    const folderBackBtn = document.getElementById('folderBackBtn');
    const deleteNoteBtn = document.getElementById('deleteNoteBtn');
    const saveBtn = document.getElementById('saveBtn');
    const folderSaveBtn = document.getElementById('folderSaveBtn');
    const noteContent = document.getElementById('noteContent');
    const colorPalette = document.getElementById('colorPalette');
    const paletteBtn = document.querySelector('[data-action="palette"]');
    const noteFolder = document.getElementById('noteFolder');

    // Загружаем папки при загрузке страницы
    if (noteFolder) {
        loadFolderSelect();
    }

    // Обработчик для кнопки меню
    menuBtn.addEventListener('click', function(e) {
        e.stopPropagation();
        sideMenu.classList.toggle('active');
        mainContainer.classList.toggle('menu-active');
        document.querySelector('.header').classList.toggle('menu-active');
    });

    // Закрытие меню при клике вне его
    document.addEventListener('click', function(e) {
        if (!e.target.closest('.side-menu') && !e.target.closest('.menu-btn')) {
            sideMenu.classList.remove('active');
            mainContainer.classList.remove('menu-active');
            document.querySelector('.header').classList.remove('menu-active');
        }
    });

    // Обработчик для кнопки добавления заметки
    addBtn.addEventListener('click', function() {
        const noteId = document.getElementById('noteId');
        const noteTitle = document.getElementById('noteTitle');
        const noteContent = document.getElementById('noteContent');
        const noteFolder = document.getElementById('noteFolder');
        const noteModal = document.getElementById('noteModal');

        if (noteId) noteId.value = '';
        if (noteTitle) noteTitle.value = '';
        if (noteContent) noteContent.innerHTML = '';
        if (noteFolder) noteFolder.value = '';
        if (noteModal) noteModal.style.display = 'block';
        
        loadFolderSelect();
    });

    // Обработчик для кнопки "Назад"
    backBtn.addEventListener('click', function() {
        noteModal.style.display = 'none';
    });

    // Обработчик для кнопки удаления
    deleteNoteBtn.addEventListener('click', function() {
        const noteId = document.getElementById('noteId').value;
        if (noteId && confirm('Вы уверены, что хотите удалить эту заметку?')) {
            fetch(`/api/notes/${noteId}`, {
                method: 'DELETE'
            })
            .then(response => {
                if (response.ok) {
                    noteModal.style.display = 'none';
                    loadNotes();
                }
            })
            .catch(error => console.error('Ошибка при удалении заметки:', error));
        }
    });

    // Обработчик для кнопки сохранения
    saveBtn.addEventListener('click', function(event) {
        event.preventDefault();
        const noteId = document.getElementById('noteId');
        const noteTitle = document.getElementById('noteTitle');
        const noteContent = document.getElementById('noteContent');
        const noteFolder = document.getElementById('noteFolder');
        const noteModal = document.getElementById('noteModal');

        if (!noteTitle || !noteContent || !noteModal) {
            console.error('Required elements not found');
            return;
        }

        const title = noteTitle.value.trim();
        const content = noteContent.innerHTML.trim();
        const folderId = noteFolder ? noteFolder.value : '';

        if (!title) {
            alert('Пожалуйста, введите заголовок заметки');
            return;
        }

        // Создаем объект заметки
        const noteData = {
            title: title,
            content: content,
            folderId: folderId ? parseInt(folderId) : null
        };

        // Добавляем ID только если он существует
        if (noteId && noteId.value) {
            noteData.id = parseInt(noteId.value);
        }

        console.log('Sending note data:', noteData);

        const method = noteData.id ? 'PUT' : 'POST';
        const url = noteData.id ? `/api/notes/${noteData.id}` : '/api/notes';

        fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            credentials: 'same-origin',
            body: JSON.stringify(noteData)
        })
        .then(response => {
            console.log('Response status:', response.status);
            if (!response.ok) {
                return response.text().then(text => {
                    console.error('Server response:', text);
                    throw new Error(`HTTP error! status: ${response.status}, message: ${text}`);
                });
            }
            return response.json();
        })
        .then(data => {
            console.log('Success:', data);
            noteModal.style.display = 'none';
            loadNotes();
        })
        .catch(error => {
            console.error('Error saving note:', error);
            alert('Ошибка при сохранении заметки: ' + error.message);
        });
    });

    // Обработчик для кнопки палитры
    paletteBtn.addEventListener('click', function(e) {
        e.stopPropagation();
        colorPalette.classList.toggle('active');
    });

    // Обработчик для выбора цвета
    colorPalette.addEventListener('click', function(e) {
        if (e.target.classList.contains('color-option')) {
            const color = e.target.getAttribute('data-color');
            noteContent.style.backgroundColor = color;
            colorPalette.classList.remove('active');
        }
    });

    // Закрытие палитры при клике вне её
    document.addEventListener('click', function(e) {
        if (!e.target.closest('[data-action="palette"]') && !e.target.closest('.color-palette')) {
        colorPalette.classList.remove('active');
        }
    });

    // Обработчик для кнопки "Назад" в модальном окне папки
    folderBackBtn.addEventListener('click', function() {
        folderModal.style.display = 'none';
    });

    // Обработчик для кнопки сохранения папки
    folderSaveBtn.addEventListener('click', async () => {
        const folderName = document.getElementById('folderName').value.trim();
        const folderDescription = document.getElementById('folderDescription').textContent.trim();
        const folderId = document.getElementById('folderId').value;
        
        console.log('Save button clicked. Current data:', {
            id: folderId,
            name: folderName,
            description: folderDescription
        });
        
        if (!folderName) {
            alert('Пожалуйста, введите название папки');
            return;
        }

        try {
            let response;
            let folderData;

            if (folderId) {
                // Редактирование существующей папки
                folderData = {
                    id: parseInt(folderId),
                    name: folderName,
                    description: folderDescription
                };
                
                console.log('Sending PUT request to update folder:', folderData);

                response = await fetch(`/api/folders/${folderId}`, {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json'
                    },
                    credentials: 'same-origin',
                    body: JSON.stringify(folderData)
                });
            } else {
                // Создание новой папки
                folderData = {
                    name: folderName,
                    description: folderDescription
                };
                
                console.log('Sending POST request to create folder:', folderData);

                response = await fetch('/api/folders/', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json'
                    },
                    credentials: 'same-origin',
                    body: JSON.stringify(folderData)
                });
            }

            console.log('Response status:', response.status);
            const responseText = await response.text();
            console.log('Response text:', responseText);

            if (!response.ok) {
                throw new Error(`Ошибка при ${folderId ? 'обновлении' : 'создании'} папки: ${response.status} ${responseText}`);
            }

            const folder = JSON.parse(responseText);
            console.log(`Successfully ${folderId ? 'updated' : 'created'} folder:`, folder);
            
            // Закрываем модальное окно
            document.getElementById('folderModal').style.display = 'none';
            
            // Очищаем форму
            document.getElementById('folderName').value = '';
            document.getElementById('folderDescription').textContent = '';
            document.getElementById('folderId').value = '';
            
            // Обновляем список папок
            loadFolders();
        } catch (error) {
            console.error(`Error ${folderId ? 'updating' : 'creating'} folder:`, error);
            alert(`Не удалось ${folderId ? 'обновить' : 'создать'} папку: ` + error.message);
        }
    });

    // Обработчик для кнопки создания папки
    createFolderBtn.addEventListener('click', function() {
        const folderName = document.getElementById('folderName');
        const folderDescription = document.getElementById('folderDescription');
        const folderModal = document.getElementById('folderModal');
        
        if (folderName) folderName.value = '';
        if (folderDescription) folderDescription.textContent = '';
        if (folderModal) folderModal.style.display = 'flex';
    });

    // Функция для создания элемента папки
    function createFolderElement(folder) {
        const div = document.createElement('div');
        div.className = 'folder-item';
        div.setAttribute('data-id', folder.id);

        const header = document.createElement('div');
        header.className = 'folder-header';

        const icon = document.createElement('i');
        icon.className = 'fas fa-folder folder-icon';

        const title = document.createElement('h3');
        title.className = 'folder-title';
        title.textContent = folder.name;

        const actions = document.createElement('div');
        actions.className = 'folder-actions';

        const editBtn = document.createElement('button');
        editBtn.className = 'folder-action-btn edit';
        editBtn.innerHTML = '<i class="fas fa-edit"></i>';
        editBtn.onclick = (e) => {
            e.stopPropagation();
            openEditFolderModal(folder);
        };

        const deleteBtn = document.createElement('button');
        deleteBtn.className = 'folder-action-btn delete';
        deleteBtn.innerHTML = '<i class="fas fa-trash"></i>';
        deleteBtn.onclick = (e) => {
            e.stopPropagation();
            if (confirm('Вы уверены, что хотите удалить эту папку?')) {
                deleteFolder(folder.id);
            }
        };

        actions.appendChild(editBtn);
        actions.appendChild(deleteBtn);

        header.appendChild(icon);
        header.appendChild(title);
        header.appendChild(actions);

        const description = document.createElement('div');
        description.className = 'folder-description';
        description.textContent = folder.description || '';

        const content = document.createElement('div');
        content.className = 'folder-content';

        const notes = document.createElement('div');
        notes.className = 'folder-notes';
        content.appendChild(notes);

        div.appendChild(header);
        div.appendChild(description);
        div.appendChild(content);

        // Обработчик клика для раскрытия/скрытия папки
        div.addEventListener('click', () => {
            div.classList.toggle('expanded');
            if (div.classList.contains('expanded')) {
                loadFolderNotes(folder.id, notes);
            }
        });

        return div;
    }

    // Функция для загрузки заметок папки
    function loadFolderNotes(folderId, container) {
        console.log('Loading notes for folder:', folderId);
        fetch(`/api/notes/?folderId=${folderId}`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            credentials: 'same-origin'
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Ошибка при загрузке заметок папки');
            }
            return response.json();
        })
        .then(notes => {
            console.log('Loaded folder notes:', notes);
            container.innerHTML = '';
            if (Array.isArray(notes) && notes.length > 0) {
                notes.forEach(note => {
                    const noteElement = createNoteElement(note);
                    container.appendChild(noteElement);
                });
            } else {
                container.innerHTML = '<div class="empty-message">В этой папке пока нет заметок</div>';
            }
        })
        .catch(error => {
            console.error('Error loading folder notes:', error);
            container.innerHTML = '<div class="error-message">Не удалось загрузить заметки</div>';
        });
    }

    // Функция для удаления папки
    function deleteFolder(folderId) {
        fetch(`/api/folders/${folderId}`, {
            method: 'DELETE',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            credentials: 'same-origin'
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Ошибка при удалении папки');
            }
            loadFolders();
        })
        .catch(error => {
            console.error('Error deleting folder:', error);
            alert('Не удалось удалить папку');
        });
    }

    // Функция для открытия модального окна редактирования папки
    function openEditFolderModal(folder) {
        console.log('Opening edit modal for folder:', folder);
        const modal = document.getElementById('folderModal');
        const folderName = document.getElementById('folderName');
        const folderDescription = document.getElementById('folderDescription');
        const folderId = document.getElementById('folderId');
        
        if (folderName) folderName.value = folder.name;
        if (folderDescription) folderDescription.textContent = folder.description || '';
        if (folderId) folderId.value = folder.id;
        
        const saveBtn = document.getElementById('folderSaveBtn');
        const originalClickHandler = saveBtn.onclick;
        
        saveBtn.onclick = async () => {
            const folderName = document.getElementById('folderName').value.trim();
            const folderDescription = document.getElementById('folderDescription').textContent.trim();
            const folderId = document.getElementById('folderId').value;
            
            console.log('Save button clicked. Current folder data:', {
                id: folderId,
                name: folderName,
                description: folderDescription
            });
            
            if (!folderName) {
                alert('Пожалуйста, введите название папки');
                return;
            }

            try {
                const folderData = {
                    id: parseInt(folderId),
                    name: folderName,
                    description: folderDescription
                };
                
                console.log('Sending PUT request to update folder:', folderData);

                const response = await fetch(`/api/folders/${folderId}`, {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json'
                    },
                    credentials: 'same-origin',
                    body: JSON.stringify(folderData)
                });

                console.log('Update response status:', response.status);
                const responseText = await response.text();
                console.log('Update response text:', responseText);

                if (!response.ok) {
                    throw new Error(`Ошибка при обновлении папки: ${response.status} ${responseText}`);
                }

                const updatedFolder = JSON.parse(responseText);
                console.log('Successfully updated folder:', updatedFolder);

                modal.style.display = 'none';
                loadFolders();
            } catch (error) {
                console.error('Error updating folder:', error);
                alert('Не удалось обновить папку: ' + error.message);
            } finally {
                saveBtn.onclick = originalClickHandler;
            }
        };

        modal.style.display = 'flex';
    }

    // Обновляем функцию loadFolders
    function loadFolders() {
        console.log('Loading folders...');
        fetch('/api/folders/', {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            credentials: 'same-origin'
        })
        .then(response => {
            console.log('Folders response status:', response.status);
            if (response.status === 401) {
                window.location.href = '/login';
                throw new Error('Unauthorized');
            }
            if (!response.ok) {
                throw new Error('Ошибка при загрузке папок: ' + response.status);
            }
            return response.json();
        })
        .then(folders => {
            console.log('Loaded folders:', folders);
            const foldersContainer = document.querySelector('.folders-container');
            if (!foldersContainer) {
                console.error('Folders container not found');
                return;
            }
            foldersContainer.innerHTML = '';
            
            if (Array.isArray(folders)) {
                folders.forEach(folder => {
                    const folderElement = createFolderElement(folder);
                    foldersContainer.appendChild(folderElement);
                });
            } else {
                console.error('Invalid folders data received');
            }
        })
        .catch(error => {
            console.error('Error loading folders:', error);
            if (error.message !== 'Unauthorized') {
                const foldersContainer = document.querySelector('.folders-container');
                if (foldersContainer) {
                    foldersContainer.innerHTML = '<div class="error-message">Не удалось загрузить папки. Пожалуйста, попробуйте позже.</div>';
                }
            }
        });
    }

    // Загружаем папки при загрузке страницы
    loadFolders();

    // Загружаем заметки при загрузке страницы
    loadNotes();
});

