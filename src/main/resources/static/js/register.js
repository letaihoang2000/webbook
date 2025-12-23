const eye = document.getElementById('eye');
const password = document.getElementById('pwd');

if (eye && password) {
    eye.addEventListener('click', function() {
        if (password.type === 'password') {
            password.type = 'text';
            eye.classList.remove('fa-eye');
            eye.classList.add('fa-eye-slash');
        } else {
            password.type = 'password';
            eye.classList.remove('fa-eye-slash');
            eye.classList.add('fa-eye');
        }
    });
}

// Toggle confirm password visibility
const eyeConfirm = document.getElementById('eye-confirm');
const confirmPassword = document.getElementById('confirm-pwd');

if (eyeConfirm && confirmPassword) {
    eyeConfirm.addEventListener('click', function() {
        if (confirmPassword.type === 'password') {
            confirmPassword.type = 'text';
            eyeConfirm.classList.remove('fa-eye');
            eyeConfirm.classList.add('fa-eye-slash');
        } else {
            confirmPassword.type = 'password';
            eyeConfirm.classList.remove('fa-eye-slash');
            eyeConfirm.classList.add('fa-eye');
        }
    });
}

// Real-time password match validation
if (confirmPassword) {
    confirmPassword.addEventListener('input', function() {
        const errorSpan = document.getElementById('confirm-password-error');
        if (password.value !== confirmPassword.value) {
            errorSpan.style.display = 'block';
            confirmPassword.style.borderColor = '#dc3545';
        } else {
            errorSpan.style.display = 'none';
            confirmPassword.style.borderColor = '';
        }
    });
}

// File input handling
const fileInput = document.getElementById('avatar');
const fileName = document.getElementById('file-name');
const imagePreview = document.getElementById('image-preview');
const previewImg = document.getElementById('preview-img');

if (fileInput) {
    fileInput.addEventListener('change', function(e) {
        const file = e.target.files[0];

        if (file) {
            // Update file name display
            fileName.textContent = file.name;

            // Show image preview
            const reader = new FileReader();
            reader.onload = function(e) {
                previewImg.src = e.target.result;
                imagePreview.style.display = 'block';
            };
            reader.readAsDataURL(file);
        } else {
            fileName.textContent = 'Choose Avatar (Optional)';
            imagePreview.style.display = 'none';
        }
    });
}

// Form validation
const form = document.querySelector('form');
if (form) {
    form.addEventListener('submit', function(e) {
        const email = document.querySelector('input[type="email"]');
        const firstName = document.querySelector('input[name="first_name"]');
        const lastName = document.querySelector('input[name="last_name"]');
        const mobile = document.querySelector('input[type="tel"]');

        let isValid = true;
        let errorMessage = '';

        // Validate first name
        if (firstName && firstName.value.trim().length < 2) {
            isValid = false;
            errorMessage += 'First name must be at least 2 characters.\n';
        }

        // Validate last name
        if (lastName && lastName.value.trim().length < 2) {
            isValid = false;
            errorMessage += 'Last name must be at least 2 characters.\n';
        }

        // Validate email
        const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (email && !emailPattern.test(email.value)) {
            isValid = false;
            errorMessage += 'Please enter a valid email address.\n';
        }

        // Validate password
        if (password && password.value.length < 6) {
            isValid = false;
            errorMessage += 'Password must be at least 6 characters.\n';
        }

        // Validate password match
        if (password && confirmPassword && password.value !== confirmPassword.value) {
            isValid = false;
            errorMessage += 'Passwords do not match.\n';
        }

        // Validate mobile
        const mobilePattern = /^[0-9]{10,15}$/;
        if (mobile && !mobilePattern.test(mobile.value.replace(/[\s-]/g, ''))) {
            isValid = false;
            errorMessage += 'Please enter a valid mobile number (10-15 digits).\n';
        }

        // Validate file size if file is selected
        if (fileInput && fileInput.files.length > 0) {
            const file = fileInput.files[0];
            const maxSize = 5 * 1024 * 1024; // 5MB

            if (file.size > maxSize) {
                isValid = false;
                errorMessage += 'Avatar image must be less than 5MB.\n';
            }

            // Check file type
            const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
            if (!allowedTypes.includes(file.type)) {
                isValid = false;
                errorMessage += 'Avatar must be a valid image file (JPG, PNG, GIF, WEBP).\n';
            }
        }

        if (!isValid) {
            e.preventDefault();
            alert(errorMessage);
        }
    });
}