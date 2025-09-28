document.addEventListener('DOMContentLoaded', function() {
    const addUserModal = document.getElementById('addUserForm');
    const form = addUserModal.querySelector('form');
    const passwordField = document.getElementById('password');
    const confirmPasswordField = document.getElementById('confirm_password');
    const imageFileInput = document.getElementById('imageFile');

    // Add error message div to modal header
    const modalBody = addUserModal.querySelector('.modal-body');
    const errorDiv = document.createElement('div');
    errorDiv.id = 'formErrorMessage';
    errorDiv.className = 'alert alert-danger';
    errorDiv.style.display = 'none';
    modalBody.insertBefore(errorDiv, modalBody.firstChild);

    // Add success message div
    const successDiv = document.createElement('div');
    successDiv.id = 'formSuccessMessage';
    successDiv.className = 'alert alert-success';
    successDiv.style.display = 'none';
    modalBody.insertBefore(successDiv, modalBody.firstChild);

    // Add image preview
    const imagePreview = document.createElement('div');
    imagePreview.id = 'imagePreview';
    imagePreview.className = 'mt-2';
    imagePreview.style.display = 'none';
    imageFileInput.parentNode.appendChild(imagePreview);

    // Add password strength indicator
    const passwordStrengthDiv = document.createElement('div');
    passwordStrengthDiv.id = 'passwordStrength';
    passwordStrengthDiv.className = 'password-strength mt-1';
    passwordField.parentNode.appendChild(passwordStrengthDiv);

    // Add confirm password validation message
    const confirmPasswordFeedback = document.createElement('div');
    confirmPasswordFeedback.className = 'invalid-feedback';
    confirmPasswordFeedback.textContent = 'Passwords do not match.';
    confirmPasswordField.parentNode.appendChild(confirmPasswordFeedback);

    // CSS styles for validation and image preview
    const style = document.createElement('style');
    style.textContent = `
        .password-strength {
            font-size: 0.875em;
            margin-top: 0.25rem;
        }
        .strength-weak { color: #dc3545; }
        .strength-medium { color: #ffc107; }
        .strength-strong { color: #28a745; }

        .form-control.is-invalid {
            border-color: #dc3545;
            padding-right: calc(1.5em + 0.75rem);
            background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 12 12' width='12' height='12' fill='none' stroke='%23dc3545'%3e%3ccircle cx='6' cy='6' r='4.5'/%3e%3cpath d='m5.8 3.6h.4L6 6.5z'/%3e%3ccircle cx='6' cy='8.2' r='.6' fill='%23dc3545' stroke='none'/%3e%3c/svg%3e");
            background-repeat: no-repeat;
            background-position: right calc(0.375em + 0.1875rem) center;
            background-size: calc(0.75em + 0.375rem) calc(0.75em + 0.375rem);
        }
        .form-control.is-valid {
            border-color: #28a745;
            padding-right: calc(1.5em + 0.75rem);
            background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 8 8'%3e%3cpath fill='%2328a745' d='m2.3 6.73.94-.94 1.86 1.86 3.5-3.5.94.94-4.44 4.44z'/%3e%3c/svg%3e");
            background-repeat: no-repeat;
            background-position: right calc(0.375em + 0.1875rem) center;
            background-size: calc(0.75em + 0.375rem) calc(0.75em + 0.375rem);
        }
        .invalid-feedback {
            display: none;
            width: 100%;
            margin-top: 0.25rem;
            font-size: 0.875em;
            color: #dc3545;
        }
        .form-control.is-invalid ~ .invalid-feedback {
            display: block;
        }

        .image-preview {
            max-width: 150px;
            max-height: 150px;
            border: 1px solid #ddd;
            border-radius: 8px;
            padding: 5px;
            object-fit: cover;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }

        .image-preview-container {
            display: flex;
            flex-direction: column;
            align-items: center;
            text-align: center;
        }

        .btn-loading {
            position: relative;
            pointer-events: none;
        }
        .btn-loading:after {
            content: "";
            position: absolute;
            width: 16px;
            height: 16px;
            margin: auto;
            border: 2px solid transparent;
            border-top-color: #ffffff;
            border-radius: 50%;
            animation: button-loading-spinner 1s ease infinite;
            top: 0;
            left: 0;
            bottom: 0;
            right: 0;
        }
        @keyframes button-loading-spinner {
            from { transform: rotate(0turn); }
            to { transform: rotate(1turn); }
        }
    `;
    document.head.appendChild(style);

    // Image file validation and preview
    function validateImageFile(file) {
        const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
        const maxSize = 5 * 1024 * 1024; // 5MB

        if (!allowedTypes.includes(file.type)) {
            return 'Please select a valid image file (JPG, PNG, GIF, WEBP).';
        }

        if (file.size > maxSize) {
            return 'Image file size must be less than 5MB.';
        }

        return null;
    }

    function showImagePreview(file) {
        const reader = new FileReader();
        reader.onload = function(e) {
            imagePreview.innerHTML = `
                <div class="image-preview-container">
                    <img src="${e.target.result}" alt="Avatar Preview" class="image-preview">
                    <p class="text-muted mt-2 mb-0 small">${file.name}</p>
                    <p class="text-muted mb-0 small">${(file.size / 1024).toFixed(1)} KB</p>
                </div>
            `;
            imagePreview.style.display = 'block';
        };
        reader.readAsDataURL(file);
    }

    // Image file input change handler
    imageFileInput.addEventListener('change', function(e) {
        const file = e.target.files[0];

        if (file) {
            const errorMessage = validateImageFile(file);

            if (errorMessage) {
                setFieldInvalid(imageFileInput);
                let feedback = imageFileInput.parentNode.querySelector('.invalid-feedback');
                if (!feedback) {
                    feedback = document.createElement('div');
                    feedback.className = 'invalid-feedback';
                    imageFileInput.parentNode.appendChild(feedback);
                }
                feedback.textContent = errorMessage;
                imagePreview.style.display = 'none';
            } else {
                setFieldValid(imageFileInput);
                showImagePreview(file);
            }
        } else {
            imagePreview.style.display = 'none';
            imageFileInput.classList.remove('is-valid', 'is-invalid');
        }
    });

    // Function to show error message
    function showError(message) {
        errorDiv.textContent = message;
        errorDiv.style.display = 'block';
        successDiv.style.display = 'none';
        modalBody.scrollTop = 0;
    }

    // Function to show success message
    function showSuccess(message) {
        successDiv.textContent = message;
        successDiv.style.display = 'block';
        errorDiv.style.display = 'none';
        modalBody.scrollTop = 0;
    }

    // Function to hide messages
    function hideMessages() {
        errorDiv.style.display = 'none';
        successDiv.style.display = 'none';
    }

    // Password validation functions
    function calculatePasswordStrength(password) {
        let score = 0;
        const feedback = [];

        if (password.length >= 8) {
            score += 1;
        } else {
            feedback.push('At least 8 characters');
        }

        if (/[a-z]/.test(password)) score += 1;
        else feedback.push('lowercase letter');

        if (/[A-Z]/.test(password)) score += 1;
        else feedback.push('uppercase letter');

        if (/[0-9]/.test(password)) score += 1;
        else feedback.push('number');

        if (/[^A-Za-z0-9]/.test(password)) score += 1;
        else feedback.push('special character');

        return { score, feedback };
    }

    function validatePassword() {
        const password = passwordField.value;
        const strength = calculatePasswordStrength(password);

        passwordStrengthDiv.innerHTML = '';

        if (password.length > 0) {
            const strengthText = strength.score < 2 ? 'Weak' :
                               strength.score < 4 ? 'Medium' : 'Strong';
            const strengthClass = strength.score < 2 ? 'strength-weak' :
                                 strength.score < 4 ? 'strength-medium' : 'strength-strong';

            passwordStrengthDiv.innerHTML = `
                <div class="${strengthClass}">Strength: ${strengthText}</div>
                <small class="text-muted">${strength.feedback.join(', ')}</small>
            `;
        }

        if (password.length >= 8) {
            setFieldValid(passwordField);
            return true;
        } else if (password.length > 0) {
            setFieldInvalid(passwordField);
            return false;
        }
        return true;
    }

    function validatePasswordMatch() {
        const password = passwordField.value;
        const confirmPassword = confirmPasswordField.value;

        if (confirmPassword.length > 0) {
            if (password === confirmPassword && password.length >= 8) {
                setFieldValid(confirmPasswordField);
                return true;
            } else {
                setFieldInvalid(confirmPasswordField);
                if (password !== confirmPassword) {
                    confirmPasswordFeedback.textContent = 'Passwords do not match.';
                } else if (password.length < 8) {
                    confirmPasswordFeedback.textContent = 'Password must be at least 8 characters.';
                }
                return false;
            }
        }
        return true;
    }

    function setFieldValid(field) {
        field.classList.remove('is-invalid');
        field.classList.add('is-valid');
    }

    function setFieldInvalid(field) {
        field.classList.remove('is-valid');
        field.classList.add('is-invalid');
    }

    function validateEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    }

    function validateForm() {
        let isValid = true;

        // Get form fields
        const firstName = document.getElementById('firstName');
        const lastName = document.getElementById('lastName');
        const emailField = document.getElementById('emailAddress');
        const mobileField = document.getElementById('mobile');

        // Add validation feedback divs if they don't exist
        [firstName, lastName, emailField, mobileField].forEach(field => {
            if (!field.parentNode.querySelector('.invalid-feedback')) {
                const feedback = document.createElement('div');
                feedback.className = 'invalid-feedback';
                field.parentNode.appendChild(feedback);
            }
        });

        // Validate required fields
        if (!firstName.value.trim()) {
            setFieldInvalid(firstName);
            firstName.parentNode.querySelector('.invalid-feedback').textContent = 'First name is required.';
            isValid = false;
        } else {
            setFieldValid(firstName);
        }

        if (!lastName.value.trim()) {
            setFieldInvalid(lastName);
            lastName.parentNode.querySelector('.invalid-feedback').textContent = 'Last name is required.';
            isValid = false;
        } else {
            setFieldValid(lastName);
        }

        if (!emailField.value.trim()) {
            setFieldInvalid(emailField);
            emailField.parentNode.querySelector('.invalid-feedback').textContent = 'Email is required.';
            isValid = false;
        } else if (!validateEmail(emailField.value)) {
            setFieldInvalid(emailField);
            emailField.parentNode.querySelector('.invalid-feedback').textContent = 'Please enter a valid email.';
            isValid = false;
        } else {
            setFieldValid(emailField);
        }

        // Validate mobile (optional but if provided should be valid)
        if (mobileField.value.trim() && !/^[0-9]{10,}$/.test(mobileField.value)) {
            setFieldInvalid(mobileField);
            mobileField.parentNode.querySelector('.invalid-feedback').textContent = 'Mobile should be at least 10 digits.';
            isValid = false;
        } else if (mobileField.value.trim()) {
            setFieldValid(mobileField);
        }

        // Validate image file if selected
        if (imageFileInput.files.length > 0) {
            const errorMessage = validateImageFile(imageFileInput.files[0]);
            if (errorMessage) {
                setFieldInvalid(imageFileInput);
                let feedback = imageFileInput.parentNode.querySelector('.invalid-feedback');
                if (!feedback) {
                    feedback = document.createElement('div');
                    feedback.className = 'invalid-feedback';
                    imageFileInput.parentNode.appendChild(feedback);
                }
                feedback.textContent = errorMessage;
                isValid = false;
            }
        }

        // Validate password
        if (!passwordField.value || passwordField.value.length < 8) {
            setFieldInvalid(passwordField);
            isValid = false;
        }

        // Validate password match
        if (!validatePasswordMatch()) {
            isValid = false;
        }

        return isValid;
    }

    // AJAX form submission
    function submitForm(formData) {
        const submitButton = form.querySelector('button[type="submit"]');
        const originalText = submitButton.textContent;

        // Show loading state
        submitButton.textContent = 'Creating...';
        submitButton.classList.add('btn-loading');
        submitButton.disabled = true;

        fetch('/admin/add', {
            method: 'POST',
            body: formData
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showSuccess(data.message);
                setTimeout(() => {
                    $('#addUserForm').modal('hide');
                    window.location.reload();
                }, 3000);
            } else {
                showError(data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showError('An error occurred while creating the user. Please try again.');
        })
        .finally(() => {
            // Reset button state
            submitButton.textContent = originalText;
            submitButton.classList.remove('btn-loading');
            submitButton.disabled = false;
        });
    }

    // Event listeners
    passwordField.addEventListener('input', () => {
        validatePassword();
        if (confirmPasswordField.value) {
            validatePasswordMatch();
        }
    });

    confirmPasswordField.addEventListener('input', validatePasswordMatch);

    // Clear email validation when user starts typing
    document.getElementById('emailAddress').addEventListener('input', function() {
        if (this.classList.contains('is-invalid')) {
            this.classList.remove('is-invalid');
            const feedback = this.parentNode.querySelector('.invalid-feedback');
            if (feedback) {
                feedback.textContent = '';
            }
        }
    });

    // Form submission with AJAX
    form.addEventListener('submit', function(e) {
        e.preventDefault();

        hideMessages();

        if (!validateForm()) {
            showError('Please fix the validation errors before submitting.');
            return false;
        }

        // Create FormData object for file upload
        const formData = new FormData(form);

        // Submit via AJAX
        submitForm(formData);
    });

    // Clear validation and messages on modal show
    $('#addUserForm').on('show.bs.modal', function() {
        form.reset();
        form.querySelectorAll('.form-control').forEach(field => {
            field.classList.remove('is-valid', 'is-invalid');
        });
        passwordStrengthDiv.innerHTML = '';
        imagePreview.style.display = 'none';
        hideMessages();
    });
});