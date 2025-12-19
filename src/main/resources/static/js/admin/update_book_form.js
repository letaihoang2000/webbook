$(document).ready(function() {
    console.log('=== update_book_form.js loaded ===');

    // Get CSRF token from meta tags
    function getCsrfToken() {
        const meta = document.querySelector('meta[name="_csrf"]');
        return meta ? meta.getAttribute('content') : null;
    }

    function getCsrfHeader() {
        const meta = document.querySelector('meta[name="_csrf_header"]');
        return meta ? meta.getAttribute('content') : null;
    }

    // Handle edit button click - populate form with book data
    $(document).on('click', '.edit-book-btn', function() {
        const bookId = $(this).data('book-id');
        const title = $(this).data('title');
        const description = $(this).data('description');
        const publishedDate = $(this).data('published-date');
        const page = $(this).data('page');
        const price = $(this).data('price');
        const image = $(this).data('image');
        const bookContent = $(this).data('book-content');
        const authorName = $(this).data('author-name');
        const categoryId = $(this).data('category-id');

        console.log('Edit button clicked for book:', bookId);
        console.log('Current image:', image);
        console.log('Current content:', bookContent);

        // Populate form fields
        $('#update_book_id').val(bookId);
        $('#update_title').val(title);
        $('#update_description').val(description);
        $('#update_published_date').val(publishedDate);
        $('#update_page').val(page);
        $('#update_price').val(price);

        // Set author and category
        $('#update_author').val(authorName);
        $('#update_category_type').val(categoryId);

        // Display current book image
        if (image && image.trim() !== '') {
            $('#currentBookImage').attr('src', image);
            $('#currentImageContainer').show();
            $('#noCurrentImage').hide();
        } else {
            $('#currentImageContainer').hide();
            $('#noCurrentImage').show();
        }

        // Display current PDF content
        if (bookContent && bookContent.trim() !== '') {
            // Extract filename from URL
            const fileName = extractFileNameFromUrl(bookContent);
            $('#currentPdfName').text(fileName);
            $('#currentPdfLink').attr('href', bookContent);
            $('#currentContentContainer').show();
            $('#noCurrentContent').hide();
        } else {
            $('#currentContentContainer').hide();
            $('#noCurrentContent').show();
        }

        // Clear file inputs and new file previews
        $('#update_imageFile').val('');
        $('#update_contentFile').val('');
        $('#updateImagePreview').hide();
        $('#updatePdfInfo').hide();
    });

    // Helper function to extract filename from URL
    function extractFileNameFromUrl(url) {
        if (!url) return 'document.pdf';

        try {
            // Decode URL to handle special characters
            const decodedUrl = decodeURIComponent(url);

            // Extract filename from URL
            const parts = decodedUrl.split('/');
            const filename = parts[parts.length - 1];

            return filename || 'document.pdf';
        } catch (e) {
            return 'document.pdf';
        }
    }

    // Image preview handler for new image
    $('#update_imageFile').on('change', function(e) {
        const file = e.target.files[0];
        if (file) {
            // Validate file type
            if (!file.type.startsWith('image/')) {
                showFieldError('#update_imageFile', 'Please select an image file');
                $(this).val('');
                return;
            }

            // Validate file size (10MB)
            if (file.size > 10 * 1024 * 1024) {
                showFieldError('#update_imageFile', 'Image size must be less than 10MB');
                $(this).val('');
                return;
            }

            // Show preview of new image
            const reader = new FileReader();
            reader.onload = function(e) {
                $('#updateImagePreview img').attr('src', e.target.result);
                $('#updateImagePreview').show();
            };
            reader.readAsDataURL(file);
        } else {
            $('#updateImagePreview').hide();
        }
    });

    // PDF file handler for new PDF
    $('#update_contentFile').on('change', function(e) {
        const file = e.target.files[0];
        if (file) {
            // Validate file type
            if (file.type !== 'application/pdf') {
                showFieldError('#update_contentFile', 'Please select a PDF file');
                $(this).val('');
                return;
            }

            // Validate file size (50MB)
            if (file.size > 50 * 1024 * 1024) {
                showFieldError('#update_contentFile', 'PDF size must be less than 50MB');
                $(this).val('');
                return;
            }

            // Show file info
            const sizeMB = (file.size / (1024 * 1024)).toFixed(2);
            $('#updatePdfName').text(file.name);
            $('#updatePdfSize').text(`(${sizeMB} MB)`);
            $('#updatePdfInfo').show();
        } else {
            $('#updatePdfInfo').hide();
        }
    });

    // Handle Update Book form submission
    $('#updateBookFormElement').on('submit', function(e) {
        e.preventDefault();
        updateBook();
    });

    // Handle Update Book button click
    $('#updateBookForm .modal-footer .btn-primary').on('click', function(e) {
        e.preventDefault();
        e.stopPropagation();
        updateBook();
    });

    function updateBook() {
        console.log('=== Update Book Function Started ===');

        // Prevent double submission
        const $submitBtn = $('#updateBookForm .modal-footer .btn-primary');
        if ($submitBtn.prop('disabled')) {
            return;
        }

        // Clear previous errors
        $('.is-invalid').removeClass('is-invalid');
        $('.invalid-feedback').remove();
        $('.alert').remove();

        // Get form values
        const bookId = $('#update_book_id').val();
        const title = $('#update_title').val().trim();
        const author = $('#update_author').val();
        const category_type = $('#update_category_type').val();
        const published_date = $('#update_published_date').val();
        const page = $('#update_page').val();
        const price = $('#update_price').val();
        const description = $('#update_description').val().trim();
        const imageFile = $('#update_imageFile')[0].files[0];
        const contentFile = $('#update_contentFile')[0].files[0];

        console.log('Update data:');
        console.log('- Book ID:', bookId);
        console.log('- Title:', title);
        console.log('- Author:', author);
        console.log('- New Image File:', imageFile ? imageFile.name : 'none (keep current)');
        console.log('- New Content File:', contentFile ? contentFile.name : 'none (keep current)');

        // Basic validation
        let isValid = true;

        if (!title) {
            showFieldError('#update_title', 'Title is required');
            isValid = false;
        }

        if (!author) {
            showFieldError('#update_author', 'Author is required');
            isValid = false;
        }

        if (!category_type) {
            showFieldError('#update_category_type', 'Category is required');
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
                  .html('<span class="spinner-border spinner-border-sm me-2"></span>Updating...');

        // Get CSRF token
        const csrfToken = getCsrfToken();
        const csrfHeader = getCsrfHeader();

        // Prepare headers
        const headers = {};
        if (csrfToken && csrfHeader) {
            headers[csrfHeader] = csrfToken;
        }

        // Send request using Fetch API
        fetch('/book/update/' + bookId, {
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
                $('#updateBookForm').modal('hide');

                // Reset form
                resetUpdateForm();

                // Show success message
                showSuccessMessage(result.data.message || 'Book updated successfully!');

                // Reload page after short delay
                setTimeout(function() {
                    window.location.reload();
                }, 2500);
            } else {
                showErrorMessage(result.data.message || 'Failed to update book');
                $submitBtn.prop('disabled', false).html(originalText);
            }
        })
        .catch(error => {
            showErrorMessage('An error occurred while updating the book: ' + error.message);
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
        $('#updateBookForm .modal-body').prepend(alertHtml);
    }

    function resetUpdateForm() {
        $('#updateBookFormElement')[0].reset();
        $('.is-invalid').removeClass('is-invalid');
        $('.invalid-feedback').remove();
        $('.alert').remove();
        $('#updateImagePreview').hide();
        $('#updatePdfInfo').hide();
        $('#currentImageContainer').hide();
        $('#currentContentContainer').hide();
        $('#noCurrentImage').hide();
        $('#noCurrentContent').hide();

        // Re-enable button
        const $submitBtn = $('#updateBookForm .modal-footer .btn-primary');
        $submitBtn.prop('disabled', false)
                  .html('<i class="fas fa-save me-1"></i>Update Book');
    }

    // Reset form when modal is closed
    $('#updateBookForm').on('hidden.bs.modal', function() {
        resetUpdateForm();
    });
});