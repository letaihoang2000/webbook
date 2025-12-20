$(document).ready(function() {
    console.log('=== add_author_form.js loaded ===');

    // Get CSRF token from meta tags
    function getCsrfToken() {
        const meta = document.querySelector('meta[name="_csrf"]');
        return meta ? meta.getAttribute('content') : null;
    }

    function getCsrfHeader() {
        const meta = document.querySelector('meta[name="_csrf_header"]');
        return meta ? meta.getAttribute('content') : null;
    }

    // Image preview handler
    $('#add_author_image').on('change', function(e) {
        const file = e.target.files[0];
        if (file) {
            // Validate file type
            if (!file.type.startsWith('image/')) {
                showFieldError('#add_author_image', 'Please select an image file');
                $(this).val('');
                return;
            }

            // Validate file size (5MB)
            if (file.size > 5 * 1024 * 1024) {
                showFieldError('#add_author_image', 'Image size must be less than 5MB');
                $(this).val('');
                return;
            }

            // Show preview
            const reader = new FileReader();
            reader.onload = function(e) {
                $('#addAuthorImagePreview img').attr('src', e.target.result);
                $('#addAuthorImagePreview').show();
            };
            reader.readAsDataURL(file);
        } else {
            $('#addAuthorImagePreview').hide();
        }
    });

    // Handle form submission
    $('#addAuthorFormElement').on('submit', function(e) {
        e.preventDefault();
        addAuthor();
    });

    function addAuthor() {
        console.log('=== Add Author Function Started ===');

        // Prevent double submission
        const $submitBtn = $('#addAuthorForm .modal-footer .btn-primary');
        if ($submitBtn.prop('disabled')) {
            return;
        }

        // Clear previous errors
        $('.is-invalid').removeClass('is-invalid');
        $('.invalid-feedback').remove();
        $('.alert').remove();

        // Get form values
        const name = $('#add_author_name').val().trim();
        const description = $('#add_author_description').val().trim();
        const imageFile = $('#add_author_image')[0].files[0];

        console.log('Add Author data:');
        console.log('- Name:', name);
        console.log('- Has Image:', imageFile ? 'YES' : 'NO');
        console.log('- Description length:', description.length);

        // Validation
        let isValid = true;

        if (!name) {
            showFieldError('#add_author_name', 'Author name is required');
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
                  .html('<span class="spinner-border spinner-border-sm me-2"></span>Adding...');

        // Get CSRF token
        const csrfToken = getCsrfToken();
        const csrfHeader = getCsrfHeader();

        // Prepare headers
        const headers = {};
        if (csrfToken && csrfHeader) {
            headers[csrfHeader] = csrfToken;
        }

        // Send request
        fetch('/author/add', {
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
                $('#addAuthorForm').modal('hide');

                // Reset form
                resetAddForm();

                // Show success message
                showSuccessMessage(result.data.message || 'Author added successfully!');

                // Reload page after short delay
                setTimeout(function() {
                    window.location.reload();
                }, 1500);
            } else {
                showErrorMessage(result.data.message || 'Failed to add author');
                $submitBtn.prop('disabled', false).html(originalText);
            }
        })
        .catch(error => {
            console.error('Error adding author:', error);
            showErrorMessage('An error occurred while adding the author: ' + error.message);
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
        $('#addAuthorForm .modal-body').prepend(alertHtml);
    }

    function resetAddForm() {
        $('#addAuthorFormElement')[0].reset();
        $('.is-invalid').removeClass('is-invalid');
        $('.invalid-feedback').remove();
        $('.alert').remove();
        $('#addAuthorImagePreview').hide();

        const $submitBtn = $('#addAuthorForm .modal-footer .btn-primary');
        $submitBtn.prop('disabled', false)
                  .html('<i class="fas fa-plus me-1"></i>Add Author');
    }

    // Reset form when modal is closed
    $('#addAuthorForm').on('hidden.bs.modal', function() {
        resetAddForm();
    });
});