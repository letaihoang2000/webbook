$(document).ready(function() {
    console.log('=== update_author_form.js loaded ===');

    // Get CSRF token from meta tags
    function getCsrfToken() {
        const meta = document.querySelector('meta[name="_csrf"]');
        return meta ? meta.getAttribute('content') : null;
    }

    function getCsrfHeader() {
        const meta = document.querySelector('meta[name="_csrf_header"]');
        return meta ? meta.getAttribute('content') : null;
    }

    // Handle edit button click - populate form with author data
    $(document).on('click', '.edit-author-btn', function() {
        const authorId = $(this).data('author-id');
        const name = $(this).data('name');
        const image = $(this).data('image');
        const description = $(this).data('description');

        console.log('Edit button clicked for author:', authorId);
        console.log('Name:', name);
        console.log('Current image:', image);

        // Populate form fields
        $('#update_author_id').val(authorId);
        $('#update_author_name').val(name);
        $('#update_author_description').val(description);

        // Display current author image
        if (image && image.trim() !== '') {
            $('#currentAuthorImage').attr('src', image);
            $('#currentAuthorImageContainer').show();
            $('#noCurrentAuthorImage').hide();
        } else {
            $('#currentAuthorImageContainer').hide();
            $('#noCurrentAuthorImage').show();
        }

        // Clear file input and new image preview
        $('#update_author_image').val('');
        $('#updateAuthorImagePreview').hide();

        // Show modal
        $('#updateAuthorForm').modal('show');
    });

    // Image preview handler for new image
    $('#update_author_image').on('change', function(e) {
        const file = e.target.files[0];
        if (file) {
            // Validate file type
            if (!file.type.startsWith('image/')) {
                showFieldError('#update_author_image', 'Please select an image file');
                $(this).val('');
                return;
            }

            // Validate file size (5MB)
            if (file.size > 5 * 1024 * 1024) {
                showFieldError('#update_author_image', 'Image size must be less than 5MB');
                $(this).val('');
                return;
            }

            // Show preview of new image
            const reader = new FileReader();
            reader.onload = function(e) {
                $('#updateAuthorImagePreview img').attr('src', e.target.result);
                $('#updateAuthorImagePreview').show();
            };
            reader.readAsDataURL(file);
        } else {
            $('#updateAuthorImagePreview').hide();
        }
    });

    // Handle form submission
    $('#updateAuthorFormElement').on('submit', function(e) {
        e.preventDefault();
        updateAuthor();
    });

    function updateAuthor() {
        console.log('=== Update Author Function Started ===');

        // Prevent double submission
        const $submitBtn = $('#updateAuthorForm .modal-footer .btn-warning');
        if ($submitBtn.prop('disabled')) {
            return;
        }

        // Clear previous errors
        $('.is-invalid').removeClass('is-invalid');
        $('.invalid-feedback').remove();
        $('.alert').remove();

        // Get form values
        const authorId = $('#update_author_id').val();
        const name = $('#update_author_name').val().trim();
        const description = $('#update_author_description').val().trim();
        const imageFile = $('#update_author_image')[0].files[0];

        console.log('Update Author data:');
        console.log('- Author ID:', authorId);
        console.log('- Name:', name);
        console.log('- New Image:', imageFile ? 'YES' : 'NO (keep current)');
        console.log('- Description length:', description.length);

        // Validation
        let isValid = true;

        if (!name) {
            showFieldError('#update_author_name', 'Author name is required');
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Create FormData
        const formData = new FormData();
        formData.append('name', name);

        if (description) {
            formData.append('description', description);
        }

        if (imageFile) {
            formData.append('image_file', imageFile);
        }

        // Show loading state
        const originalText = $submitBtn.html();
        $submitBtn.prop('disabled', true)
                  .html('<span class="spinner-border spinner-border-sm me-2"></span>Updating...');

        // Get CSRF token
        const csrfToken = getCsrfToken();
        const csrfHeader = getCsrfHeader();

        // Prepare headers
        const headers = {};
        if (csrfToken && csrfHeader) {
            headers[csrfHeader] = csrfToken;
        }

        // Send request
        fetch('/author/update/' + authorId, {
            method: 'POST',
            headers: headers,
            body: formData
        })
        .then(response => {
            console.log('Response status:', response.status);
            return response.json().then(data => ({
                status: response.status,
                ok: response.ok,
                data: data
            }));
        })
        .then(result => {
            if (result.ok && result.data.success) {
                // Close modal
                $('#updateAuthorForm').modal('hide');

                // Reset form
                resetUpdateForm();

                // Show success message
                showSuccessMessage(result.data.message || 'Author updated successfully!');

                // Reload page after short delay
                setTimeout(function() {
                    window.location.reload();
                }, 1500);
            } else {
                showErrorMessage(result.data.message || 'Failed to update author');
                $submitBtn.prop('disabled', false).html(originalText);
            }
        })
        .catch(error => {
            console.error('Error updating author:', error);
            showErrorMessage('An error occurred while updating the author: ' + error.message);
            $submitBtn.prop('disabled', false).html(originalText);
        });
    }

    function showFieldError(fieldSelector, message) {
        const $field = $(fieldSelector);
        $field.addClass('is-invalid');
        $field.after(`<div class="invalid-feedback d-block">${message}</div>`);
    }

    function showSuccessMessage(message) {
        const alertHtml = `
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                <i class="fas fa-check-circle me-2"></i>${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        `;
        $('#content .container.mt-4').prepend(alertHtml);
    }

    function showErrorMessage(message) {
        const alertHtml = `
            <div class="alert alert-danger alert-dismissible fade show mt-3" role="alert">
                <i class="fas fa-exclamation-circle me-2"></i>${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        `;
        $('#updateAuthorForm .modal-body').prepend(alertHtml);
    }

    function resetUpdateForm() {
        $('#updateAuthorFormElement')[0].reset();
        $('.is-invalid').removeClass('is-invalid');
        $('.invalid-feedback').remove();
        $('.alert').remove();
        $('#updateAuthorImagePreview').hide();
        $('#currentAuthorImageContainer').hide();
        $('#noCurrentAuthorImage').hide();

        const $submitBtn = $('#updateAuthorForm .modal-footer .btn-warning');
        $submitBtn.prop('disabled', false)
                  .html('<i class="fas fa-save me-1"></i>Update Author');
    }

    // Reset form when modal is closed
    $('#updateAuthorForm').on('hidden.bs.modal', function() {
        resetUpdateForm();
    });
});