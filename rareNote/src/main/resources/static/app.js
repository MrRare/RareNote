document.addEventListener('DOMContentLoaded', () => {
    const chatInput = document.getElementById('chat-input');
    const sendButton = document.getElementById('send-button');
    const chatBox = document.getElementById('chat-box');

    sendButton.addEventListener('click', handleSendMessage);
    chatInput.addEventListener('keypress', event => {
        if (event.key === 'Enter') {
            handleSendMessage();
        }
    });

    function handleSendMessage() {
        const message = chatInput.value.trim();
        if (message === '') return;

        addMessageToChat('user-message', message);
        chatInput.value = '';
        sendMessageToServer(message);
    }

    function addMessageToChat(className, message) {
        const messageDiv = document.createElement('div');
        messageDiv.className = className;
        messageDiv.innerText = message;
        chatBox.appendChild(messageDiv);

        // Add menu to each message
        const menuDiv = document.createElement('div');
        menuDiv.className = 'menu';
        menuDiv.innerText = '⋮';
        menuDiv.addEventListener('click', () => toggleMenu(menuDiv));
        messageDiv.appendChild(menuDiv);

        const menuOptionsDiv = document.createElement('div');
        menuOptionsDiv.className = 'menu-options';
        const editOption = document.createElement('a');
        editOption.innerText = 'Edit';
        editOption.addEventListener('click', () => makeMessageEditable(messageDiv));
        menuOptionsDiv.appendChild(editOption);

        messageDiv.appendChild(menuOptionsDiv);
        chatBox.scrollTop = chatBox.scrollHeight;
    }

    function toggleMenu(menuDiv) {
        const menuOptions = menuDiv.nextElementSibling;
        menuOptions.style.display = menuOptions.style.display === 'block' ? 'none' : 'block';
    }

    function makeMessageEditable(messageDiv) {
        const messageText = messageDiv.innerText.replace('⋮Edit', '').trim();
        messageDiv.innerHTML = `<input type="text" value="${messageText}" class="editable-message"><button onclick="saveMessage(this)">Save</button>`;
    }

    window.saveMessage = async function(button) {
        const messageDiv = button.parentElement;
        const newMessage = messageDiv.querySelector('.editable-message').value;

        // Update message in backend
        const response = await fetch('/api/chat', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ message: `edit:${messageDiv.dataset.id}:${newMessage}` })
        });

        const data = await response.json();
        if (data.response === 'Note updated successfully.') {
            messageDiv.innerHTML = `${newMessage}<div class="menu">⋮</div>`;
            addMessageToChat('bot-message', 'Note updated successfully.');
        } else {
            addMessageToChat('bot-message', 'Error updating note.');
        }
    };

    async function sendMessageToServer(message) {
        try {
            const response = await fetch('/api/chat', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ message })
            });
            const data = await response.json();
            addMessageToChat('bot-message', data.response);
        } catch (error) {
            console.error('Error:', error);
            addMessageToChat('bot-message', 'Error processing your message.');
        }
    }
});
