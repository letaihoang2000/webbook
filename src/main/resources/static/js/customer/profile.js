$(document).ready(function() {
    const editProfileModal = new bootstrap.Modal(document.getElementById('editProfileModal'));
    const successToast = new bootstrap.Toast(document.getElementById('successToast'), {
        autohide: true,
        delay: 3000
    });

    // Get CSRF token
    const csrfToken = $('meta[name="_csrf"]').attr('content');
    const csrfHeader = $('meta[name="_csrf_header"]').attr('content');

    // Open edit modal and populate with current user data
    $('#editProfileBtn').on('click', function() {
        // Populate form with current user data
        $('#editUserId').val(currentUserData.id);
        $('#editFirstName').val(currentUserData.first_name);
        $('#editLastName').val(currentUserData.last_name);
        $('#editEmail').val(currentUserData.email);
        $('#editMobile').val(currentUserData.mobile || '');
        $('#editAddress').val(currentUserData.address || '');
        $('#editPassword').val('');

        // Set avatar preview
        const avatarUrl = currentUserData.image || '/images/default-avatar.png';
        $('#editAvatarPreview').attr('src', avatarUrl);

        // Show modal
        editProfileModal.show();
    });

    // Image preview on file select
    $('#editImageFile').on('change', function(e) {
        const file = e.target.files[0];

        if (file) {
            // Validate file size (5MB)
            if (file.size > 5 * 1024 * 1024) {
                showAlert('File size must be less than 5MB', 'danger');
                $(this).val('');
                return;
            }

            // Validate file type
            const validTypes = ['image/jpeg', 'image/png', 'image/gif'];
            if (!validTypes.includes(file.type)) {
                showAlert('Please select a valid image file (JPG, PNG, or GIF)', 'danger');
                $(this).val('');
                return;
            }

            // Preview image
            const reader = new FileReader();
            reader.onload = function(e) {
                $('#editAvatarPreview').attr('src', e.target.result);
            };
            reader.readAsDataURL(file);
        }
    });

    // Save profile changes
    $('#saveProfileBtn').on('click', function() {
        const firstName = $('#editFirstName').val().trim();
        const lastName = $('#editLastName').val().trim();

        if (!firstName || !lastName) {
            showAlert('First name and last name are required', 'danger');
            return;
        }

        // Prepare form data
        const formData = new FormData();
        formData.append('id', $('#editUserId').val());
        formData.append('first_name', firstName);
        formData.append('last_name', lastName);
        formData.append('mobile', $('#editMobile').val());
        formData.append('address', $('#editAddress').val());

        const password = $('#editPassword').val();
        if (password) {
            formData.append('password', password);
        }

        // Add image file if selected
        const imageFile = $('#editImageFile')[0].files[0];
        if (imageFile) {
            formData.append('imageFile', imageFile);
        }

        // Show loading state
        showLoadingState(true);

        // Send AJAX request
        $.ajax({
            url: '/customer/profile/update',
            type: 'POST',
            data: formData,
            processData: false,
            contentType: false,
            headers: {
                [csrfHeader]: csrfToken
            },
            success: function(response) {
                if (response.success) {
                    // Update display with new data
                    updateProfileDisplay(response.user);

                    // Show success message in modal briefly
                    showAlert(response.message, 'success');

                    // Close modal after 800ms
                    setTimeout(function() {
                        editProfileModal.hide();
                        // Clear form
                        $('#editProfileForm')[0].reset();
                        hideAlert();

                        // Show toast notification after modal closes
                        setTimeout(function() {
                            showToast(response.message);
                        }, 300);
                    }, 800);
                } else {
                    showAlert(response.message, 'danger');
                }
            },
            error: function(xhr, status, error) {
                let errorMessage = 'Failed to update profile';
                if (xhr.responseJSON && xhr.responseJSON.message) {
                    errorMessage = xhr.responseJSON.message;
                }
                showAlert(errorMessage, 'danger');
            },
            complete: function() {
                // Hide loading state
                showLoadingState(false);
            }
        });
    });

    // Show/hide loading state on save button
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

    // Update profile display after successful update
    function updateProfileDisplay(user) {
        // Update avatar with smooth transition
        const avatarUrl = user.image || '/images/default-avatar.png';
        $('#displayAvatar').fadeOut(200, function() {
            $(this).attr('src', avatarUrl).fadeIn(200);
        });

        // Update name
        const fullName = user.first_name + ' ' + user.last_name;
        $('#displayFullName').text(fullName);
        $('#displayFirstName').text(user.first_name);
        $('#displayLastName').text(user.last_name);

        // Update email
        $('#displayEmail').text(user.email);

        // Update mobile
        $('#displayMobile').text(user.mobile || 'Not provided');

        // Update address
        $('#displayAddress').text(user.address || 'Not provided');

        // Update global user data
        currentUserData.first_name = user.first_name;
        currentUserData.last_name = user.last_name;
        currentUserData.mobile = user.mobile;
        currentUserData.address = user.address;
        currentUserData.image = user.image;
    }

    // Show toast notification
    function showToast(message) {
        $('#toastMessage').text(message);
        successToast.show();
    }

    // Show alert in modal
    function showAlert(message, type) {
        const alertDiv = $('#profileAlert');
        const alertMessage = $('#profileAlertMessage');

        alertDiv.removeClass('alert-success alert-danger show');
        alertDiv.addClass('alert-' + type + ' show');
        alertMessage.text(message);
        alertDiv.show();

        // Auto-hide after 3 seconds
        setTimeout(function() {
            hideAlert();
        }, 3000);
    }

    // Hide alert
    function hideAlert() {
        $('#profileAlert').removeClass('show').hide();
    }

    // Clear alert when modal is hidden
    $('#editProfileModal').on('hidden.bs.modal', function() {
        hideAlert();
        // Reset loading state
        showLoadingState(false);
    });
});