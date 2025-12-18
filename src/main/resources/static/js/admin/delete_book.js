$(document).ready(function() {
    console.log('=== delete_book.js loaded ===');

    // Get CSRF token from meta tags
    function getCsrfToken() {
        const meta = document.querySelector('meta[name="_csrf"]');
        return meta ? meta.getAttribute('content') : null;
    }

    function getCsrfHeader() {
        const meta = document.querySelector('meta[name="_csrf_header"]');
        return meta ? meta.getAttribute('content') : null;
    }

    // Handle delete button click - populate modal with book data
    $(document).on('click', '.delete-book-btn', function() {
        const bookId = $(this).data('book-id');
        const bookTitle = $(this).data('book-title');

        // Get additional data from the row
        const $row = $(this).closest('tr');
        const bookImage = $row.find('img').attr('src');
        const bookAuthor = $row.find('td:eq(2)').text().trim();
        const bookPrice = $row.find('td:eq(3)').text().trim();

        console.log('Delete button clicked for book:', bookId);
        console.log('Title:', bookTitle);

        // Populate modal with book data
        $('#delete_book_id').val(bookId);
        $('#delete_book_title').text(bookTitle);
        $('#delete_book_author').text(bookAuthor);
        $('#delete_book_price').text(bookPrice);
        $('#delete_book_image').attr('src', bookImage);

        // Show the modal
        $('#deleteBookModal').modal('show');
    });

    // Handle confirm delete button click
    $('#confirmDeleteBtn').on('click', function() {
        deleteBook();
    });

    function deleteBook() {
        console.log('=== Delete Book Function Started ===');

        // Prevent double submission
        const $deleteBtn = $('#confirmDeleteBtn');
        if ($deleteBtn.prop('disabled')) {
            return;
        }

        const bookId = $('#delete_book_id').val();
        const bookTitle = $('#delete_book_title').text();

        console.log('Deleting book:', bookId, '-', bookTitle);

        // Show loading state
        const originalText = $deleteBtn.html();
        $deleteBtn.prop('disabled', true)
                  .html('<span class="spinner-border spinner-border-sm me-2"></span>Deleting...');

        // Get CSRF token
        const csrfToken = getCsrfToken();
        const csrfHeader = getCsrfHeader();


        // Prepare headers
        const headers = {
            'Content-Type': 'application/json'
        };
        if (csrfToken && csrfHeader) {
            headers[csrfHeader] = csrfToken;
        }

        // Send delete request
        fetch('/book/delete/' + bookId, {
            method: 'POST',
            headers: headers
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
                $('#deleteBookModal').modal('hide');

                // Show success message
                showSuccessMessage(result.data.message || 'Book deleted successfully!');

                // Reload page after short delay
                setTimeout(function() {
                    window.location.reload();
                }, 1500);
            } else {
                showErrorMessage(result.data.message || 'Failed to delete book');
                $deleteBtn.prop('disabled', false).html(originalText);
            }
        })
        .catch(error => {
            showErrorMessage('An error occurred while deleting the book: ' + error.message);
            $deleteBtn.prop('disabled', false).html(originalText);
        });
    }

    function showSuccessMessage(message) {
        const alertHtml = `
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                <i class="fas fa-check-circle me-2"></i>${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        `;
        $('#content .container.mt-4').prepend(alertHtml);

        // Auto dismiss after 3 seconds
        setTimeout(function() {
            $('.alert-success').fadeOut(function() {
                $(this).remove();
            });
        }, 3000);
    }

    function showErrorMessage(message) {
        const alertHtml = `
            <div class="alert alert-danger alert-dismissible fade show mt-3" role="alert">
                <i class="fas fa-exclamation-circle me-2"></i>${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        `;

        // Show error inside modal
        $('#deleteBookModal .modal-body').prepend(alertHtml);

        // Auto dismiss after 5 seconds
        setTimeout(function() {
            $('#deleteBookModal .alert-danger').fadeOut(function() {
                $(this).remove();
            });
        }, 5000);
    }

    // Reset modal when closed
    $('#deleteBookModal').on('hidden.bs.modal', function() {
        // Remove any error alerts
        $('#deleteBookModal .alert').remove();

        // Re-enable button
        const $deleteBtn = $('#confirmDeleteBtn');
        $deleteBtn.prop('disabled', false)
                  .html('<i class="fas fa-trash-alt me-1"></i>Delete Book');
    });
});