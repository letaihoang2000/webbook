$(document).ready(function() {
    console.log('=== delete_category.js loaded ===');

    // Get CSRF token from meta tags
    function getCsrfToken() {
        const meta = document.querySelector('meta[name="_csrf"]');
        return meta ? meta.getAttribute('content') : null;
    }

    function getCsrfHeader() {
        const meta = document.querySelector('meta[name="_csrf_header"]');
        return meta ? meta.getAttribute('content') : null;
    }

    // Global handler for notification close buttons using event delegation
    $(document).on('click', '.notification-close-btn', function() {
        console.log('Close button clicked');
        const alertElement = $(this).closest('.alert')[0];
        if (alertElement) {
            alertElement.style.opacity = '0';
            alertElement.style.transition = 'opacity 0.4s ease';
            setTimeout(function() {
                alertElement.remove();
            }, 400);
        }
    });

    // Handle delete button click - populate modal with category data
    $(document).on('click', '.delete-category-btn', function() {
        const categoryId = $(this).data('id');
        const categoryName = $(this).data('name');

        console.log('Delete button clicked for category:', categoryId);
        console.log('Name:', categoryName);

        // Populate modal with category data
        $('#delete_category_id').val(categoryId);
        $('#delete_category_id_display').text(categoryId);
        $('#delete_category_name').text(categoryName);

        // Show the modal
        $('#deleteCategoryModal').modal('show');
    });

    // Handle confirm delete button click
    $('#confirmDeleteCategoryBtn').on('click', function() {
        deleteCategory();
    });

    function deleteCategory() {
        console.log('=== Delete Category Function Started ===');

        // Prevent double submission
        const $deleteBtn = $('#confirmDeleteCategoryBtn');
        if ($deleteBtn.prop('disabled')) {
            return;
        }

        const categoryId = $('#delete_category_id').val();
        const categoryName = $('#delete_category_name').text();

        console.log('Deleting category:', categoryId, '-', categoryName);

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
        fetch('/category/delete/' + categoryId, {
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
                $('#deleteCategoryModal').modal('hide');

                // Show success message
                showSuccessMessage(result.data.message || 'Category deleted successfully!');

                // Reload page after short delay
                setTimeout(function() {
                    window.location.reload();
                }, 1500);
            } else {
                showErrorMessage(result.data.message || 'Failed to delete category');
                $deleteBtn.prop('disabled', false).html(originalText);
            }
        })
        .catch(error => {
            console.error('Error deleting category:', error);
            showErrorMessage('An error occurred while deleting the category: ' + error.message);
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
        $('#deleteCategoryModal .modal-body').prepend(alertHtml);

        // Auto dismiss after 5 seconds
        setTimeout(function() {
            const alerts = $('#deleteCategoryModal .alert-danger');
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
    $('#deleteCategoryModal').on('hidden.bs.modal', function() {
        // Remove any error alerts
        $('#deleteCategoryModal .alert').remove();

        // Re-enable button and reset text
        const $deleteBtn = $('#confirmDeleteCategoryBtn');
        $deleteBtn.prop('disabled', false)
                  .html('<i class="fas fa-trash-alt me-1"></i>Delete Category');
    });
});