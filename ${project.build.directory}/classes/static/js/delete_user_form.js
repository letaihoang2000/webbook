document.addEventListener('DOMContentLoaded', function() {
    const deleteModal = document.getElementById('deleteConfirmModal');
    const deleteUserNameSpan = document.getElementById('deleteUserName');
    const deleteUserEmailSpan = document.getElementById('deleteUserEmail');
    const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');

    // Handle delete button click
    document.addEventListener('click', function(e) {
        if (e.target.closest('.delete-user-btn')) {
            const button = e.target.closest('.delete-user-btn');

            // Get data from button attributes
            const userId = button.getAttribute('data-user-id');
            const userName = button.getAttribute('data-user-name');
            const userEmail = button.getAttribute('data-user-email');

            console.log('Delete button clicked:', { userId, userName, userEmail }); // Debug log

            // Set user info in modal
            if (deleteUserNameSpan) {
                deleteUserNameSpan.textContent = userName || 'N/A';
            }
            if (deleteUserEmailSpan) {
                deleteUserEmailSpan.textContent = userEmail || 'N/A';
            }

            // Store user ID for deletion
            if (confirmDeleteBtn) {
                confirmDeleteBtn.setAttribute('data-user-id', userId);
            }

            // Show modal using Bootstrap 4 jQuery method
            if (deleteModal && typeof $ !== 'undefined') {
                $('#deleteConfirmModal').modal('show');
            } else {
                console.error('Modal element or jQuery not found');
            }
        }
    });

    // Handle confirm delete button click
    if (confirmDeleteBtn) {
        confirmDeleteBtn.addEventListener('click', function() {
            const userId = this.getAttribute('data-user-id');

            if (!userId) {
                showErrorMessage('Cannot find user to delete!');
                return;
            }

            // Disable button during request
            const originalText = this.innerHTML;
            this.disabled = true;
            this.innerHTML = '<i class="fas fa-spinner fa-spin me-1"></i> Đang xóa...';

            // Call delete function
            deleteUser(userId, this, originalText);
        });
    }
});

function deleteUser(userId, button, originalText) {
    console.log('Attempting to delete user:', userId); // Debug log

    // Make AJAX call to delete endpoint
    fetch(`/user/delete/${userId}`, {
        method: 'DELETE',
        headers: {
            'X-Requested-With': 'XMLHttpRequest',
            'Content-Type': 'application/json',
            // Add CSRF token if you're using Spring Security
            // 'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').getAttribute('content')
        }
    })
    .then(response => {
        console.log('Response status:', response.status); // Debug log
        if (!response.ok) {
            return response.json().then(err => Promise.reject(err));
        }
        return response.json();
    })
    .then(data => {
        console.log('Delete success:', data); // Debug log
        
        // Hide modal using Bootstrap 4 jQuery method
        if (typeof $ !== 'undefined') {
            $('#deleteConfirmModal').modal('hide');
        }

        // Show success message
        showSuccessMessage(data.message || 'User deleted successfully!');

        // Reload page after a short delay
        setTimeout(() => {
            location.reload();
        }, 1500);
    })
    .catch(error => {
        console.error('Delete error:', error); // Debug log
        
        // Hide modal using Bootstrap 4 jQuery method
        if (typeof $ !== 'undefined') {
            $('#deleteConfirmModal').modal('hide');
        }

        // Show error message
        const errorMessage = error.message || 'Error occurs while deleting user!';
        showErrorMessage(errorMessage);
    })
    .finally(() => {
        // Re-enable button
        button.disabled = false;
        button.innerHTML = originalText;
    });
}

// Helper functions for showing messages
function showSuccessMessage(message) {
    // Create and show success alert
    const alert = document.createElement('div');
    alert.className = 'alert alert-success alert-dismissible fade show position-fixed';
    alert.style.cssText = 'top: 20px; right: 20px; z-index: 9999; min-width: 300px;';
    alert.innerHTML = `
        <i class="fas fa-check-circle me-2"></i>
        ${message}
        <button type="button" class="close" data-dismiss="alert" aria-label="Close">
            <span aria-hidden="true">&times;</span>
        </button>
    `;

    document.body.appendChild(alert);

    // Auto remove after 5 seconds
    setTimeout(() => {
        if (alert && alert.parentNode) {
            alert.remove();
        }
    }, 5000);
}

function showErrorMessage(message) {
    // Create and show error alert
    const alert = document.createElement('div');
    alert.className = 'alert alert-danger alert-dismissible fade show position-fixed';
    alert.style.cssText = 'top: 20px; right: 20px; z-index: 9999; min-width: 300px;';
    alert.innerHTML = `
        <i class="fas fa-exclamation-circle me-2"></i>
        ${message}
        <button type="button" class="close" data-dismiss="alert" aria-label="Close">
            <span aria-hidden="true">&times;</span>
        </button>
    `;

    document.body.appendChild(alert);

    // Auto remove after 7 seconds
    setTimeout(() => {
        if (alert && alert.parentNode) {
            alert.remove();
        }
    }, 7000);
}

// Optional: Clear modal data when it's hidden (Bootstrap 4 syntax)
if (typeof $ !== 'undefined') {
    $('#deleteConfirmModal').on('hidden.bs.modal', function() {
        // Clear user info from modal
        const deleteUserNameSpan = document.getElementById('deleteUserName');
        const deleteUserEmailSpan = document.getElementById('deleteUserEmail');
        const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');

        if (deleteUserNameSpan) deleteUserNameSpan.textContent = '-';
        if (deleteUserEmailSpan) deleteUserEmailSpan.textContent = '-';
        if (confirmDeleteBtn) confirmDeleteBtn.removeAttribute('data-user-id');
    });
}