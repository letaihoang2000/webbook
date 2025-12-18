$(document).ready(function() {
    console.log('=== add_book_form.js loaded ===');

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
    $('#imageFile').on('change', function(e) {
        const file = e.target.files[0];
        if (file) {
            // Validate file type
            if (!file.type.startsWith('image/')) {
                showFieldError('#imageFile', 'Please select an image file');
                $(this).val('');
                return;
            }

            // Validate file size (10MB)
            if (file.size > 10 * 1024 * 1024) {
                showFieldError('#imageFile', 'Image size must be less than 10MB');
                $(this).val('');
                return;
            }

            // Show preview
            const reader = new FileReader();
            reader.onload = function(e) {
                $('#imagePreview img').attr('src', e.target.result);
                $('#imagePreview').show();
            };
            reader.readAsDataURL(file);
        } else {
            $('#imagePreview').hide();
        }
    });

    // PDF file handler
    $('#contentFile').on('change', function(e) {
        const file = e.target.files[0];
        if (file) {
            // Validate file type
            if (file.type !== 'application/pdf') {
                showFieldError('#contentFile', 'Please select a PDF file');
                $(this).val('');
                return;
            }

            // Validate file size (50MB)
            if (file.size > 50 * 1024 * 1024) {
                showFieldError('#contentFile', 'PDF size must be less than 50MB');
                $(this).val('');
                return;
            }

            // Show file info
            const sizeMB = (file.size / (1024 * 1024)).toFixed(2);
            $('#pdfName').text(file.name);
            $('#pdfSize').text(`(${sizeMB} MB)`);
            $('#pdfInfo').show();
        } else {
            $('#pdfInfo').hide();
        }
    });

    // Handle Create Book form submission
    $('#bookForm').on('submit', function(e) {
        e.preventDefault();
        createBook();
    });

    // Handle Create Book button click
    $('#addBookForm .modal-footer .btn-primary').on('click', function(e) {
        e.preventDefault();
        e.stopPropagation();
        createBook();
    });

    function createBook() {
        console.log('=== Create Book Function Started ===');

        // Prevent double submission
        const $submitBtn = $('#addBookForm .modal-footer .btn-primary');
        if ($submitBtn.prop('disabled')) {
            return;
        }

        // Clear previous errors
        $('.is-invalid').removeClass('is-invalid');
        $('.invalid-feedback').remove();
        $('.alert').remove();

        // Get form values
        const title = $('#title').val().trim();
        const author = $('#author').val();
        const category_type = $('#category_type').val();
        const published_date = $('#published_date').val();
        const page = $('#page').val();
        const price = $('#price').val();
        const description = $('#description').val().trim();
        const imageFile = $('#imageFile')[0].files[0];
        const contentFile = $('#contentFile')[0].files[0];

        console.log('Form data:');
        console.log('- Title:', title);
        console.log('- Author:', author);
        console.log('- Category:', category_type);
        console.log('- Published Date:', published_date);
        console.log('- Page:', page);
        console.log('- Price:', price);
        console.log('- Description:', description);
        console.log('- Image File:', imageFile ? imageFile.name : 'none');
        console.log('- Content File:', contentFile ? contentFile.name : 'none');

        // Basic validation
        let isValid = true;

        if (!title) {
            showFieldError('#title', 'Title is required');
            isValid = false;
        }

        if (!author) {
            showFieldError('#author', 'Author is required');
            isValid = false;
        }

        if (!category_type) {
            showFieldError('#category_type', 'Category is required');
            isValid = false;
        }

        if (!isValid) {
            return;
        }


        // Create FormData for file upload
        const formData = new FormData();
        formData.append('title', title);
        formData.append('author', author);
        formData.append('category_type', category_type);

        if (published_date) {
            formData.append('published_date', published_date);
        }
        if (page) {
            formData.append('page', page);
        }
        if (price) {
            formData.append('price', price);
        }
        if (description) {
            formData.append('description', description);
        }
        if (imageFile) {
            formData.append('imageFile', imageFile);
        }
        if (contentFile) {
            formData.append('contentFile', contentFile);
        }

        // Show loading state
        const originalText = $submitBtn.html();
        $submitBtn.prop('disabled', true)
                  .html('<span class="spinner-border spinner-border-sm me-2"></span>Creating...');

        // Get CSRF token
        const csrfToken = getCsrfToken();
        const csrfHeader = getCsrfHeader();

        console.log('CSRF Token:', csrfToken ? 'Present' : 'Missing');

        // Prepare headers (don't set Content-Type for FormData)
        const headers = {};
        if (csrfToken && csrfHeader) {
            headers[csrfHeader] = csrfToken;
        }

        // Send request using Fetch API
        fetch('/book/add', {
            method: 'POST',
            headers: headers,
            body: formData
        })
        .then(response => {
            console.log('Response status:', response.status);
            console.log('Response ok:', response.ok);
            return response.json().then(data => ({
                status: response.status,
                ok: response.ok,
                data: data
            }));
        })
        .then(result => {

            if (result.ok && result.data.success) {
                // Close modal
                $('#addBookForm').modal('hide');

                // Reset form
                resetForm();

                // Show success message
                showSuccessMessage(result.data.message || 'Book created successfully!');

                // Reload page after short delay
                setTimeout(function() {
                    window.location.reload();
                }, 1500);
            } else {
                showErrorMessage(result.data.message || 'Failed to create book');
                $submitBtn.prop('disabled', false).html(originalText);
            }
        })
        .catch(error => {
            showErrorMessage('An error occurred while creating the book: ' + error.message);
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

        // Show error inside modal
        $('#addBookForm .modal-body').prepend(alertHtml);
    }

    function resetForm() {
        $('#bookForm')[0].reset();
        $('.is-invalid').removeClass('is-invalid');
        $('.invalid-feedback').remove();
        $('.alert').remove();
        $('#imagePreview').hide();
        $('#pdfInfo').hide();

        // Re-enable button
        const $submitBtn = $('#addBookForm .modal-footer .btn-primary');
        $submitBtn.prop('disabled', false)
                  .html('<i class="fas fa-plus me-1"></i>Create Book');
    }

    // Reset form when modal is closed
    $('#addBookForm').on('hidden.bs.modal', function() {
        resetForm();
    });
});