document.addEventListener('DOMContentLoaded', function() {
    const registroForm = document.getElementById('registroForm');
    
    if (registroForm) {
        registroForm.addEventListener('submit', function(event) {
            const password = document.getElementById('password').value;
            const errorMensaje = document.getElementById('passwordError');
            
            // Expresión regular: 8 caracteres, 1 número, 1 símbolo especial
            const regex = /^(?=.*[0-9])(?=.*[!@#$%^&*])[a-zA-Z0-9!@#$%^&*]{8,}$/;

            if (!regex.test(password)) {
                event.preventDefault(); // Detener envío
                errorMensaje.textContent = " La contraseña debe tener 8 caracteres, un número y un símbolo.";
                errorMensaje.style.color = "#dc3545";
            } else {
                errorMensaje.textContent = "";
            }
        });
    }
});