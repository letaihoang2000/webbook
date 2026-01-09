$(document).ready(function() {
    const editProfileModal = new bootstrap.Modal(document.getElementById('editProfileModal'));
    const successToast = new bootstrap.Toast(document.getElementById('successToast'), {
        autohide: true,
        delay: 3000
    });

    const csrfToken = $('meta[name="_csrf"]').attr('content');
    const csrfHeader = $('meta[name="_csrf_header"]').attr('content');

    const passwordValidation = {
        minLength: 8,
        hasUpperCase: /[A-Z]/,
        hasLowerCase: /[a-z]/,
        hasNumber: /[0-9]/,
        hasSpecialChar: /[!@#$%^&*(),.?":{}|<>]/
    };

    $('#editProfileBtn').on('click', function() {
        $('#editUserId').val(currentUserData.id);
        $('#editFirstName').val(currentUserData.first_name);
        $('#editLastName').val(currentUserData.last_name);
        $('#editEmail').val(currentUserData.email);
        $('#editMobile').val(currentUserData.mobile || '');
        $('#editAddress').val(currentUserData.address || '');
        clearPasswordFields();
        const avatarUrl = currentUserData.image || '/images/default-avatar.png';
        $('#editAvatarPreview').attr('src', avatarUrl);
        editProfileModal.show();
    });

    function clearPasswordFields() {
        $('#editCurrentPassword').val('');
        $('#editPassword').val('');
        $('#editConfirmPassword').val('');
        $('.password-match-feedback').hide().removeClass('match no-match');
        $('#passwordStrength').hide();
        $('.password-strength-bar').removeClass('weak medium strong');
        $('#passwordRequirements').removeClass('valid invalid').text('Must contain: 8+ characters, uppercase, lowercase, number, and special character');
        $('#currentPasswordError').hide();
        // Reset all password fields to type="password" and eye icons to closed
        $('.toggle-password i').removeClass('icon-eye-off').addClass('icon-eye');
        $('.password-input-wrapper input[type="text"]').attr('type', 'password');
    }

    $(document).on('click', '.toggle-password', function(e) {
        e.preventDefault();
        const targetId = $(this).data('target');
        const input = $('#' + targetId);
        const icon = $(this).find('i');
        if (input.attr('type') === 'password') {
            input.attr('type', 'text');
            icon.removeClass('icon-eye').addClass('icon-eye-off');
        } else {
            input.attr('type', 'password');
            icon.removeClass('icon-eye-off').addClass('icon-eye');
        }
    });

    function validatePasswordStrength(password) {
        if (!password) return { isValid: false, strength: '', message: '' };
        let strength = 0;
        let messages = [];
        if (password.length >= passwordValidation.minLength) strength++; else messages.push('at least 8 characters');
        if (passwordValidation.hasUpperCase.test(password)) strength++; else messages.push('uppercase letter');
        if (passwordValidation.hasLowerCase.test(password)) strength++; else messages.push('lowercase letter');
        if (passwordValidation.hasNumber.test(password)) strength++; else messages.push('number');
        if (passwordValidation.hasSpecialChar.test(password)) strength++; else messages.push('special character');
        const isValid = strength === 5;
        let strengthLevel = strength <= 2 ? 'weak' : strength <= 4 ? 'medium' : 'strong';
        return {
            isValid: isValid,
            strength: strengthLevel,
            message: messages.length > 0 ? 'Missing: ' + messages.join(', ') : 'Strong password'
        };
    }

    $('#editPassword').on('keyup', function() {
        const password = $(this).val();
        const strengthIndicator = $('#passwordStrength');
        const strengthBar = $('.password-strength-bar');
        const requirements = $('#passwordRequirements');
        if (password.length > 0) {
            const validation = validatePasswordStrength(password);
            strengthIndicator.show();
            strengthBar.removeClass('weak medium strong').addClass(validation.strength);
            if (validation.isValid) {
                requirements.removeClass('invalid').addClass('valid').text('✓ ' + validation.message);
            } else {
                requirements.removeClass('valid').addClass('invalid').text('✗ ' + validation.message);
            }
        } else {
            strengthIndicator.hide();
            requirements.removeClass('valid invalid').text('Must contain: 8+ characters, uppercase, lowercase, number, and special character');
        }
    });

    $('#editPassword, #editConfirmPassword').on('keyup', function() {
        const newPassword = $('#editPassword').val();
        const confirmPassword = $('#editConfirmPassword').val();
        const feedback = $('.password-match-feedback');
        if (newPassword.length > 0 || confirmPassword.length > 0) {
            if (confirmPassword.length > 0) {
                if (newPassword === confirmPassword) {
                    feedback.removeClass('no-match').addClass('match').html('<i class="icon icon-check"></i> Passwords match').show();
                } else {
                    feedback.removeClass('match').addClass('no-match').html('<i class="icon icon-close"></i> Passwords do not match').show();
                }
            } else {
                feedback.hide();
            }
        } else {
            feedback.hide();
        }
    });

    $('#editCurrentPassword').on('input', function() {
        $('#currentPasswordError').hide();
    });

    $('#editImageFile').on('change', function(e) {
        const file = e.target.files[0];
        if (file) {
            if (file.size > 5 * 1024 * 1024) {
                showAlert('File size must be less than 5MB', 'danger');
                $(this).val('');
                return;
            }
            const validTypes = ['image/jpeg', 'image/png', 'image/gif'];
            if (!validTypes.includes(file.type)) {
                showAlert('Please select a valid image file (JPG, PNG, or GIF)', 'danger');
                $(this).val('');
                return;
            }
            const reader = new FileReader();
            reader.onload = function(e) {
                $('#editAvatarPreview').attr('src', e.target.result);
            };
            reader.readAsDataURL(file);
        }
    });

    $('#saveProfileBtn').on('click', function() {
        const firstName = $('#editFirstName').val().trim();
        const lastName = $('#editLastName').val().trim();
        if (!firstName || !lastName) {
            showAlert('First name and last name are required', 'danger');
            return;
        }

        const currentPassword = $('#editCurrentPassword').val();
        const newPassword = $('#editPassword').val();
        const confirmPassword = $('#editConfirmPassword').val();

        // Check if ANY password field is filled
        if (currentPassword || newPassword || confirmPassword) {
            // If any field is filled, ALL THREE must be filled
            if (!currentPassword) {
                showAlert('Please enter your current password', 'danger');
                $('#editCurrentPassword').focus();
                return;
            }
            if (!newPassword) {
                showAlert('Please enter a new password', 'danger');
                $('#editPassword').focus();
                return;
            }
            if (!confirmPassword) {
                showAlert('Please confirm your new password', 'danger');
                $('#editConfirmPassword').focus();
                return;
            }

            // Validate password strength
            const validation = validatePasswordStrength(newPassword);
            if (!validation.isValid) {
                showAlert('Password does not meet requirements: ' + validation.message, 'danger');
                $('#editPassword').focus();
                return;
            }

            // Validate passwords match
            if (newPassword !== confirmPassword) {
                showAlert('New password and confirmation do not match', 'danger');
                $('#editConfirmPassword').focus();
                return;
            }
        }

        const formData = new FormData();
        formData.append('id', $('#editUserId').val());
        formData.append('first_name', firstName);
        formData.append('last_name', lastName);
        formData.append('mobile', $('#editMobile').val());
        formData.append('address', $('#editAddress').val());

        // Only send password fields if ALL THREE are provided
        if (currentPassword && newPassword && confirmPassword) {
            formData.append('currentPassword', currentPassword);
            formData.append('password', newPassword);
        }

        const imageFile = $('#editImageFile')[0].files[0];
        if (imageFile) {
            formData.append('imageFile', imageFile);
        }
        showLoadingState(true);
        $.ajax({
            url: '/customer/profile/update',
            type: 'POST',
            data: formData,
            processData: false,
            contentType: false,
            headers: { [csrfHeader]: csrfToken },
            success: function(response) {
                if (response.success) {
                    updateProfileDisplay(response.user);
                    showAlert(response.message, 'success');
                    setTimeout(function() {
                        editProfileModal.hide();
                        $('#editProfileForm')[0].reset();
                        clearPasswordFields();
                        hideAlert();
                        setTimeout(function() { showToast(response.message); }, 300);
                    }, 800);
                } else {
                    showAlert(response.message, 'danger');
                    if (response.message.toLowerCase().includes('current password is incorrect')) {
                        $('#currentPasswordError').show();
                        $('#editCurrentPassword').focus();
                    }
                }
            },
            error: function(xhr) {
                let errorMessage = 'Failed to update profile';
                if (xhr.responseJSON && xhr.responseJSON.message) {
                    errorMessage = xhr.responseJSON.message;
                    if (errorMessage.toLowerCase().includes('current password is incorrect')) {
                        $('#currentPasswordError').show();
                        $('#editCurrentPassword').focus();
                    }
                }
                showAlert(errorMessage, 'danger');
            },
            complete: function() {
                showLoadingState(false);
            }
        });
    });

    function showLoadingState(isLoading) {
        const saveBtn = $('#saveProfileBtn');
        const btnText = saveBtn.find('.btn-text');
        const btnLoading = saveBtn.find('.btn-loading');
        if (isLoading) {
            saveBtn.prop('disabled', true);
            btnText.hide();
            btnLoading.show();
        } else {
            saveBtn.prop('disabled', false);
            btnText.show();
            btnLoading.hide();
        }
    }

    function updateProfileDisplay(user) {
        const avatarUrl = user.image || '/images/default-avatar.png';
        $('#displayAvatar').fadeOut(200, function() {
            $(this).attr('src', avatarUrl).fadeIn(200);
        });
        const fullName = user.first_name + ' ' + user.last_name;
        $('#displayFullName').text(fullName);
        $('#displayFirstName').text(user.first_name);
        $('#displayLastName').text(user.last_name);
        $('#displayEmail').text(user.email);
        $('#displayMobile').text(user.mobile || 'Not provided');
        $('#displayAddress').text(user.address || 'Not provided');
        currentUserData.first_name = user.first_name;
        currentUserData.last_name = user.last_name;
        currentUserData.mobile = user.mobile;
        currentUserData.address = user.address;
        currentUserData.image = user.image;
    }

    function showToast(message) {
        $('#toastMessage').text(message);
        successToast.show();
    }

    function showAlert(message, type) {
        const alertDiv = $('#profileAlert');
        const alertMessage = $('#profileAlertMessage');
        alertDiv.removeClass('alert-success alert-danger show');
        alertDiv.addClass('alert-' + type + ' show');
        alertMessage.text(message);
        alertDiv.show();
        if (type === 'success') {
            setTimeout(function() { hideAlert(); }, 3000);
        }
    }

    function hideAlert() {
        $('#profileAlert').removeClass('show').hide();
    }

    $('#editProfileModal').on('hidden.bs.modal', function() {
        hideAlert();
        showLoadingState(false);
        clearPasswordFields();
    });
});