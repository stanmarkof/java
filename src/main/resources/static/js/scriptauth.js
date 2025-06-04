document.getElementById('login-form').addEventListener('submit', async function (event) {
    event.preventDefault();

    const email = event.target.querySelector('input[type="email"]').value;
    const password = event.target.querySelector('input[type="password"]').value;

    try {
        const response = await fetch('/api/auth/login', { // Исправлен путь
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password }),
        });

        const data = await response.json();

        if (response.ok) {
            console.log('Токен:', data.token);
            window.location.href = '/pages/calendar.html';
        } else {
            alert(data.message || 'Ошибка при входе.');
        }
    } catch (error) {
        console.error('Ошибка:', error);
        alert('Ошибка соединения с сервером.');
    }
});



