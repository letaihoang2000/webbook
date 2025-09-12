// update_user_form.js

document.addEventListener('DOMContentLoaded', function() {
    const updateUserModal = document.getElementById('updateUserForm');
    const updateForm = updateUserModal.querySelector('form');
    const updatePasswordField = document.getElementById('updatePassword');
    const updateConfirmPasswordField = document.getElementById('updateConfirmPassword');
    const updateImageFileInput = document.getElementById('updateImageFile');

    // Add error and success message divs to update modal
    const updateModalBody = updateUserModal.querySelector('.modal-body');

    const updateErrorDiv = document.createElement('div');
    updateErrorDiv.id = 'updateFormErrorMessage';
    updateErrorDiv.className = 'alert alert-danger';
    updateErrorDiv.style.display = 'none';
    updateModalBody.insertBefore(updateErrorDiv, updateModalBody.firstChild);

    const updateSuccessDiv = document.createElement('div');
    updateSuccessDiv.id = 'updateFormSuccessMessage';
    updateSuccessDiv.className = 'alert alert-success';
    updateSuccessDiv.style.display = 'none';
    updateModalBody.insertBefore(updateSuccessDiv, updateModalBody.firstChild);

    // Add image preview for update modal
    const updateImagePreview = document.createElement('div');
    updateImagePreview.id = 'updateImagePreview';
    updateImagePreview.className = 'mt-2';
    updateImagePreview.style.display = 'none';
    updateImageFileInput.parentNode.appendChild(updateImagePreview);

    // Add password strength indicator for update modal
    const updatePasswordStrengthDiv = document.createElement('div');
    updatePasswordStrengthDiv.id = 'updatePasswordStrength';
    updatePasswordStrengthDiv.className = 'password-strength mt-1';
    updatePasswordField.parentNode.appendChild(updatePasswordStrengthDiv);

    // Add confirm password validation for update modal
    const updateConfirmPasswordFeedback = document.createElement('div');
    updateConfirmPasswordFeedback.className = 'invalid-feedback';
    updateConfirmPasswordFeedback.textContent = 'Passwords do not match.';
    updateConfirmPasswordField.parentNode.appendChild(updateConfirmPasswordFeedback);

    // Reuse functions from add_user_form.js
    function showUpdateError(message) {
        updateErrorDiv.textContent = message;
        updateErrorDiv.style.display = 'block';
        updateSuccessDiv.style.display = 'none';
        updateModalBody.scrollTop = 0;
    }

    function showUpdateSuccess(message) {
        updateSuccessDiv.textContent = message;
        updateSuccessDiv.style.display = 'block';
        updateErrorDiv.style.display = 'none';
        updateModalBody.scrollTop = 0;
    }

    function hideUpdateMessages() {
        updateErrorDiv.style.display = 'none';
        updateSuccessDiv.style.display = 'none';
    }

    function setFieldValid(field) {
        field.classList.remove('is-invalid');
        field.classList.add('is-valid');
    }

    function setFieldInvalid(field) {
        field.classList.remove('is-valid');
        field.classList.add('is-invalid');
    }

    // Handle edit button clicks
    document.addEventListener('click', function(e) {
        if (e.target.closest('.edit-user-btn')) {
            const button = e.target.closest('.edit-user-btn');
            hideUpdateMessages();

            // Get data from button attributes
            const userId = button.getAttribute('data-user-id');
            const first_name = button.getAttribute('data-firstName');
            const last_name = button.getAttribute('data-lastName');
            const email = button.getAttribute('data-email');
            const mobile = button.getAttribute('data-mobile');
            const address = button.getAttribute('data-address');
            const image = button.getAttribute('data-image');

            // Populate form fields
            document.getElementById('updateUserId').value = userId;
            document.getElementById('updateFirstName').value = first_name;
            document.getElementById('updateLastName').value = last_name;
            document.getElementById('updateEmailAddress').value = email;
            document.getElementById('updateMobile').value = mobile;
            document.getElementById('updateAddress').value = address;

            // Show current avatar if exists
            if (image && image.trim() !== '') {
                document.getElementById('currentAvatar').src = image;
                document.getElementById('currentAvatarRow').style.display = 'block';
            } else {
                document.getElementById('currentAvatarRow').style.display = 'none';
            }

            // Clear password fields
            document.getElementById('updatePassword').value = '';
            document.getElementById('updateConfirmPassword').value = '';

            // Clear image input and preview
            document.getElementById('updateImageFile').value = '';
            updateImagePreview.style.display = 'none';

            // Clear validation states
            updateForm.querySelectorAll('.form-control').forEach(field => {
                field.classList.remove('is-valid', 'is-invalid');
            });

            updatePasswordStrengthDiv.innerHTML = '';
        }
    });

    // Image validation and preview functions
    function validateUpdateImageFile(file) {
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

    function showUpdateImagePreview(file) {
        const reader = new FileReader();
        reader.onload = function(e) {
            updateImagePreview.innerHTML = `
                <div class="image-preview-container">
                    <img src="${e.target.result}" alt="New Avatar Preview" class="image-preview">
                    <p class="text-muted mt-2 mb-0 small">${file.name}</p>
                    <p class="text-muted mb-0 small">${(file.size / 1024).toFixed(1)} KB</p>
                </div>
            `;
            updateImagePreview.style.display = 'block';
        };
        reader.readAsDataURL(file);
    }

    // Update image file input handler
    updateImageFileInput.addEventListener('change', function(e) {
        const file = e.target.files[0];

        if (file) {
            const errorMessage = validateUpdateImageFile(file);

            if (errorMessage) {
                setFieldInvalid(updateImageFileInput);
                let feedback = updateImageFileInput.parentNode.querySelector('.invalid-feedback');
                if (!feedback) {
                    feedback = document.createElement('div');
                    feedback.className = 'invalid-feedback';
                    updateImageFileInput.parentNode.appendChild(feedback);
                }
                feedback.textContent = errorMessage;
                updateImagePreview.style.display = 'none';
            } else {
                setFieldValid(updateImageFileInput);
                showUpdateImagePreview(file);
            }
        } else {
            updateImagePreview.style.display = 'none';
            updateImageFileInput.classList.remove('is-valid', 'is-invalid');
        }
    });

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

    function validateUpdatePassword() {
        const password = updatePasswordField.value;

        if (password.length === 0) {
            updatePasswordStrengthDiv.innerHTML = '';
            updatePasswordField.classList.remove('is-valid', 'is-invalid');
            return true;
        }

        const strength = calculatePasswordStrength(password);
        updatePasswordStrengthDiv.innerHTML = '';

        const strengthText = strength.score < 2 ? 'Weak' :
                           strength.score < 4 ? 'Medium' : 'Strong';
        const strengthClass = strength.score < 2 ? 'strength-weak' :
                             strength.score < 4 ? 'strength-medium' : 'strength-strong';

        updatePasswordStrengthDiv.innerHTML = `
            <div class="${strengthClass}">Strength: ${strengthText}</div>
            <small class="text-muted">${strength.feedback.join(', ')}</small>
        `;

        if (password.length >= 8) {
            setFieldValid(updatePasswordField);
            return true;
        } else {
            setFieldInvalid(updatePasswordField);
            return false;
        }
    }

    function validateUpdatePasswordMatch() {
        const password = updatePasswordField.value;
        const confirmPassword = updateConfirmPasswordField.value;

        if (confirmPassword.length === 0 && password.length === 0) {
            updateConfirmPasswordField.classList.remove('is-valid', 'is-invalid');
            return true;
        }

        if (password === confirmPassword) {
            if (password.length === 0 || password.length >= 8) {
                setFieldValid(updateConfirmPasswordField);
                return true;
            }
        } else {
            setFieldInvalid(updateConfirmPasswordField);
            updateConfirmPasswordFeedback.textContent = 'Passwords do not match.';
            return false;
        }
        return true;
    }

    // Event listeners for password validation
    updatePasswordField.addEventListener('input', () => {
        validateUpdatePassword();
        if (updateConfirmPasswordField.value) {
            validateUpdatePasswordMatch();
        }
    });

    updateConfirmPasswordField.addEventListener('input', validateUpdatePasswordMatch);

    // Form validation
    function validateUpdateForm() {
        let isValid = true;

        const firstName = document.getElementById('updateFirstName');
        const lastName = document.getElementById('updateLastName');
        const mobileField = document.getElementById('updateMobile');

        // Add validation feedback divs if they don't exist
        [firstName, lastName, mobileField].forEach(field => {
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

        // Validate mobile
        if (mobileField.value.trim() && !/^[0-9]{10,}$/.test(mobileField.value)) {
            setFieldInvalid(mobileField);
            mobileField.parentNode.querySelector('.invalid-feedback').textContent = 'Mobile should be at least 10 digits.';
            isValid = false;
        } else if (mobileField.value.trim()) {
            setFieldValid(mobileField);
        }

        // Validate image file if selected
        if (updateImageFileInput.files.length > 0) {
            const errorMessage = validateUpdateImageFile(updateImageFileInput.files[0]);
            if (errorMessage) {
                isValid = false;
            }
        }

        // Validate passwords
        if (!validateUpdatePassword()) {
            isValid = false;
        }

        if (!validateUpdatePasswordMatch()) {
            isValid = false;
        }

        return isValid;
    }

    // Form submission
    function submitUpdateForm(formData) {
        const submitButton = updateForm.querySelector('button[type="submit"]');
        const originalText = submitButton.textContent;

        submitButton.textContent = 'Updating...';
        submitButton.classList.add('btn-loading');
        submitButton.disabled = true;

        fetch('/user/update', {
            method: 'POST',
            body: formData
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showUpdateSuccess(data.message);
                setTimeout(() => {
                    $('#updateUserForm').modal('hide');
                    window.location.reload();
                }, 2000);
            } else {
                showUpdateError(data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showUpdateError('An error occurred while updating the user. Please try again.');
        })
        .finally(() => {
            submitButton.textContent = originalText;
            submitButton.classList.remove('btn-loading');
            submitButton.disabled = false;
        });
    }

    // Form submission event listener
    updateForm.addEventListener('submit', function(e) {
        e.preventDefault();

        hideUpdateMessages();

        if (!validateUpdateForm()) {
            showUpdateError('Please fix the validation errors before submitting.');
            return false;
        }

        const formData = new FormData(updateForm);
        submitUpdateForm(formData);
    });

    // Clear validation on modal show
    $('#updateUserForm').on('show.bs.modal', function() {
        hideUpdateMessages();
    });
});