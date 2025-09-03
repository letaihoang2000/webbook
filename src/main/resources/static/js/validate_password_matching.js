
document.addEventListener('DOMContentLoaded', function() {
    const addUserModal = document.getElementById('addUserForm');
    const form = addUserModal.querySelector('form');
    const passwordField = document.getElementById('password');
    const confirmPasswordField = document.getElementById('confirm_password');

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

    // CSS styles for validation
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
    `;
    document.head.appendChild(style);

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

    async function submitUser(userData) {
        try {
            const response = await fetch('/api/users', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: JSON.stringify(userData)
            });

            if (response.ok) {
                const result = await response.json();
                alert('User created successfully!');
                form.reset();

                // Clear validation classes
                form.querySelectorAll('.form-control').forEach(field => {
                    field.classList.remove('is-valid', 'is-invalid');
                });
                passwordStrengthDiv.innerHTML = '';

                // Close modal (Bootstrap 4 syntax as per your original code)
                $('#addUserForm').modal('hide');

                // Optionally reload the page or update user list
                // window.location.reload();

            } else {
                const error = await response.json();
                if (typeof error === 'object') {
                    // Handle field-specific errors
                    Object.keys(error).forEach(field => {
                        const fieldElement = document.querySelector(`[name="${field}"], #${field}`);
                        if (fieldElement) {
                            setFieldInvalid(fieldElement);
                            const feedback = fieldElement.parentNode.querySelector('.invalid-feedback');
                            if (feedback) {
                                feedback.textContent = error[field];
                            }
                        }
                    });
                } else {
                    alert('Error: ' + (error.message || 'Failed to create user'));
                }
            }
        } catch (error) {
            console.error('Error:', error);
            alert('Network error occurred. Please try again.');
        }
    }

    // Event listeners
    passwordField.addEventListener('input', () => {
        validatePassword();
        if (confirmPasswordField.value) {
            validatePasswordMatch();
        }
    });

    confirmPasswordField.addEventListener('input', validatePasswordMatch);

    // Form submission
    form.addEventListener('submit', function(e) {
        e.preventDefault();

        if (!validateForm()) {
            return;
        }

        // Prepare user data
        const userData = {
            first_name: document.getElementById('firstName').value.trim(),
            last_name: document.getElementById('lastName').value.trim(),
            email: document.getElementById('emailAddress').value.trim(),
            mobile: document.getElementById('mobile').value.trim(),
            address: document.getElementById('address').value.trim(),
            password: passwordField.value
        };

        submitUser(userData);
    });

    // Clear validation on modal show
    $('#addUserForm').on('show.bs.modal', function() {
        form.reset();
        form.querySelectorAll('.form-control').forEach(field => {
            field.classList.remove('is-valid', 'is-invalid');
        });
        passwordStrengthDiv.innerHTML = '';
    });
});