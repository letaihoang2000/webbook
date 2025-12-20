$(document).ready(function() {
    console.log('=== delete_author.js loaded ===');

    // Get CSRF token from meta tags
    function getCsrfToken() {
        const meta = document.querySelector('meta[name="_csrf"]');
        return meta ? meta.getAttribute('content') : null;
    }

    function getCsrfHeader() {
        const meta = document.querySelector('meta[name="_csrf_header"]');
        return meta ? meta.getAttribute('content') : null;
    }

    // Handle delete button click - populate modal with author data
    $(document).on('click', '.delete-author-btn', function() {
        const authorId = $(this).data('author-id');
        const authorName = $(this).data('author-name');
        const booksCount = $(this).data('books-count');

        // Get image from the table row
        const $row = $(this).closest('tr');
        const authorImage = $row.find('.author-image').attr('src');

        console.log('Delete button clicked for author:', authorId);
        console.log('Name:', authorName);
        console.log('Books count:', booksCount);

        // Check if author has books BEFORE opening modal
        if (parseInt(booksCount) > 0) {
            // Show notification instead of opening modal
            showCannotDeleteNotification(authorName, booksCount);
            return; // Don't open the modal
        }

        // Author has no books - proceed with delete modal
        $('#delete_author_id').val(authorId);
        $('#delete_author_books_count').val(booksCount);
        $('#delete_author_name').text(authorName);
        $('#delete_author_books_display').text(booksCount + ' book(s)');
        $('#delete_author_image').attr('src', authorImage);

        // Hide warnings (should already be hidden for no books)
        $('#hasBooksWarning').hide();
        $('#noBooksWarning').show();
        $('#confirmDeleteAuthorBtn').prop('disabled', false).removeClass('disabled');

        // Show the modal
        $('#deleteAuthorModal').modal('show');
    });

    // Global handler for notification close buttons using event delegation
    $(document).on('click', '.notification-close-btn', function() {
        console.log('Close button clicked');
        const alertElement = $(this).closest('.alert')[0];
        if (alertElement) {
            // Add fade-out class
            alertElement.style.opacity = '0';
            alertElement.style.transition = 'opacity 0.4s ease';

            // Remove after animation
            setTimeout(function() {
                alertElement.remove();
            }, 400);
        }
    });

    // Function to show notification when author cannot be deleted
    function showCannotDeleteNotification(authorName, booksCount) {
        // Create unique ID for this notification
        const notificationId = 'notification-' + Date.now();

        const notificationHtml = `
            <div id="${notificationId}" class="alert alert-warning alert-dismissible fade show cannot-delete-notification"
                 role="alert"
                 style="position: fixed; top: 80px; right: 20px; z-index: 9999; min-width: 350px; max-width: 400px; box-shadow: 0 4px 12px rgba(0,0,0,0.15); transition: opacity 0.4s ease;">
                <div class="d-flex align-items-start">
                    <div class="me-3">
                        <i class="fas fa-exclamation-triangle fa-2x text-warning"></i>
                    </div>
                    <div class="flex-grow-1 pe-3">
                        <h6 class="alert-heading mb-2">
                            <i class="fas fa-ban me-1"></i>Cannot Delete Author
                        </h6>
                        <p class="mb-2">
                            <strong>${authorName}</strong> cannot be deleted because this author has
                            <strong class="text-danger">${booksCount} book(s)</strong> associated.
                        </p>
                        <hr class="my-2">
                        <small class="text-muted">
                            <i class="fas fa-info-circle me-1"></i>
                            Please remove or reassign all books before deleting this author.
                        </small>
                    </div>
                    <button type="button" class="btn-close notification-close-btn" aria-label="Close"></button>
                </div>
            </div>
        `;

        // Remove any existing notifications
        $('.cannot-delete-notification').remove();

        // Add notification to body
        $('body').append(notificationHtml);

        // Auto dismiss after 5 seconds
        setTimeout(function() {
            console.log('Auto-closing notification after 5 seconds');
            const element = document.getElementById(notificationId);
            if (element) {
                element.style.opacity = '0';
                setTimeout(function() {
                    element.remove();
                }, 400);
            }
        }, 5000);
    }

    // Handle confirm delete button click
    $('#confirmDeleteAuthorBtn').on('click', function() {
        deleteAuthor();
    });

    function deleteAuthor() {
        console.log('=== Delete Author Function Started ===');

        // Prevent double submission
        const $deleteBtn = $('#confirmDeleteAuthorBtn');
        if ($deleteBtn.prop('disabled')) {
            return;
        }

        const authorId = $('#delete_author_id').val();
        const authorName = $('#delete_author_name').text();
        const booksCount = parseInt($('#delete_author_books_count').val());

        console.log('Deleting author:', authorId, '-', authorName);

        // Double check books count (should never happen as modal won't open)
        if (booksCount > 0) {
            showErrorMessage('Cannot delete author with associated books!');
            return;
        }

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
        fetch('/author/delete/' + authorId, {
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
                $('#deleteAuthorModal').modal('hide');

                // Show success message
                showSuccessMessage(result.data.message || 'Author deleted successfully!');

                // Reload page after short delay
                setTimeout(function() {
                    window.location.reload();
                }, 1500);
            } else {
                showErrorMessage(result.data.message || 'Failed to delete author');
                $deleteBtn.prop('disabled', false).html(originalText);
            }
        })
        .catch(error => {
            console.error('Error deleting author:', error);
            showErrorMessage('An error occurred while deleting the author: ' + error.message);
            $deleteBtn.prop('disabled', false).html(originalText);
        });
    }

    function showSuccessMessage(message) {
        const notificationId = 'success-' + Date.now();

        const alertHtml = `
            <div id="${notificationId}" class="alert alert-success alert-dismissible fade show success-notification"
                 role="alert"
                 style="position: fixed; top: 80px; right: 20px; z-index: 9999; min-width: 350px; max-width: 400px; box-shadow: 0 4px 12px rgba(0,0,0,0.15); transition: opacity 0.4s ease;">
                <div class="pe-3">
                    <i class="fas fa-check-circle me-2"></i>${message}
                </div>
                <button type="button" class="btn-close notification-close-btn" aria-label="Close"></button>
            </div>
        `;

        $('body').append(alertHtml);

        // Auto dismiss after 3 seconds
        setTimeout(function() {
            console.log('Auto-closing success notification after 3 seconds');
            const element = document.getElementById(notificationId);
            if (element) {
                element.style.opacity = '0';
                setTimeout(function() {
                    element.remove();
                }, 400);
            }
        }, 3000);
    }

    function showErrorMessage(message) {
        const alertHtml = `
            <div class="alert alert-danger alert-dismissible fade show mt-3" role="alert" style="transition: opacity 0.4s ease;">
                <i class="fas fa-exclamation-circle me-2"></i>${message}
                <button type="button" class="btn-close notification-close-btn" aria-label="Close"></button>
            </div>
        `;

        // Show error inside modal
        $('#deleteAuthorModal .modal-body').prepend(alertHtml);

        // Auto dismiss after 5 seconds
        setTimeout(function() {
            const alerts = $('#deleteAuthorModal .alert-danger');
            if (alerts.length > 0) {
                const element = alerts[0];
                element.style.opacity = '0';
                setTimeout(function() {
                    element.remove();
                }, 400);
            }
        }, 5000);
    }

    // Reset modal when closed
    $('#deleteAuthorModal').on('hidden.bs.modal', function() {
        // Remove any error alerts
        $('#deleteAuthorModal .alert').remove();

        // Re-enable button and reset text
        const $deleteBtn = $('#confirmDeleteAuthorBtn');
        $deleteBtn.prop('disabled', false)
                  .removeClass('disabled')
                  .html('<i class="fas fa-trash-alt me-1"></i>Delete Author');

        // Hide warnings
        $('#hasBooksWarning').hide();
        $('#noBooksWarning').hide();
    });
});