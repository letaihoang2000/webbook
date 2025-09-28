document.addEventListener('DOMContentLoaded', function() {
    // Handle view button click
    document.addEventListener('click', function(e) {
        if (e.target.closest('.view-user-btn')) {
            const button = e.target.closest('.view-user-btn');

            // Get user data from button attributes
            const userId = button.getAttribute('data-user-id');
            const firstName = button.getAttribute('data-first-name');
            const lastName = button.getAttribute('data-last-name');
            const email = button.getAttribute('data-email');
            const mobile = button.getAttribute('data-mobile');
            const address = button.getAttribute('data-address');
            const image = button.getAttribute('data-image');
            const role = button.getAttribute('data-role');
            const lastUpdated = button.getAttribute('data-last-updated');

            console.log('View button clicked for user:', userId); // Debug log

            if (!userId) {
                showErrorMessage('Cannot find user to view!');
                return;
            }

            // Populate modal with user data and show
            populateAndShowModal({
                user_id: userId,
                first_name: firstName,
                last_name: lastName,
                email: email,
                mobile: mobile,
                address: address,
                image: image,
                role_name: role,
                last_updated: lastUpdated
            });
        }
    });

    // Handle edit button click in view modal
    document.addEventListener('click', function(e) {
        if (e.target.closest('#editUserBtn')) {
            const userId = document.getElementById('viewUserId').textContent;

            // Close view modal
            $('#viewUserModal').modal('hide');

            // Find the edit button for this user and click it
            setTimeout(() => {
                const editButton = document.querySelector(`[data-user-id="${userId}"].edit-user-btn`);
                if (editButton) {
                    editButton.click();
                } else {
                    showErrorMessage('Cannot find edit button for this user');
                }
            }, 300);
        }
    });

    // Handle delete button click in view modal
    document.addEventListener('click', function(e) {
        if (e.target.closest('#deleteUserBtn')) {
            const userId = document.getElementById('viewUserId').textContent;

            // Close view modal
            $('#viewUserModal').modal('hide');

            // Find the delete button for this user and click it
            setTimeout(() => {
                const deleteButton = document.querySelector(`[data-user-id="${userId}"].delete-user-btn`);
                if (deleteButton) {
                    deleteButton.click();
                } else {
                    showErrorMessage('Cannot find delete button for this user');
                }
            }, 300);
        }
    });
});

function populateAndShowModal(userData) {
    // Populate basic info
    const fullName = `${userData.first_name || ''} ${userData.last_name || ''}`.trim() || 'N/A';
    document.getElementById('viewUserFullName').textContent = fullName;

    // Populate first_name
    document.getElementById('viewUserFirstName').textContent = userData.first_name || 'N/A';

    // Populate last_name
    document.getElementById('viewUserLastName').textContent = userData.last_name || 'N/A';

    // Populate email
    document.getElementById('viewUserEmail').textContent = userData.email || 'N/A';

    // Populate mobile
    document.getElementById('viewUserMobile').textContent = userData.mobile || 'N/A';

    // Populate address
    const address = userData.address || 'N/A';
    document.getElementById('viewUserAddress').textContent = address;

    // Populate role
    const role = userData.role_name || 'N/A';
    document.getElementById('viewUserRole').textContent = role;
    document.getElementById('viewUserRoleBadge').textContent = role;

    // Set role badge color
    const roleBadge = document.getElementById('viewUserRoleBadge');
    roleBadge.className = 'badge role-badge';
    if (role === 'USER') {
        roleBadge.classList.add('bg-primary');
    } else {
        roleBadge.classList.add('bg-secondary');
    }

    // Populate user ID
    document.getElementById('viewUserId').textContent = userData.user_id || 'N/A';

    // Populate last updated
    document.getElementById('viewUserLastUpdated').textContent = userData.last_updated || 'N/A';

    // Populate avatar
    const avatarImg = document.getElementById('viewUserAvatar');
    if (userData.image && userData.image.trim() !== '') {
        avatarImg.src = userData.image;
        avatarImg.onerror = function() {
            this.src = 'https://res.cloudinary.com/dso24g1vf/image/upload/v1759069674/default-avatar_m9jq5j.jpg';
        };
    } else {
        avatarImg.src = 'https://res.cloudinary.com/dso24g1vf/image/upload/v1759069674/default-avatar_m9jq5j.jpg';
    }

    // Show the modal
    $('#viewUserModal').modal('show');
}

// Helper functions for showing messages
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

// Clear modal data when it's hidden
if (typeof $ !== 'undefined') {
    $('#viewUserModal').on('hidden.bs.modal', function() {
        // Reset to default values
        document.getElementById('viewUserFullName').textContent = '';
        document.getElementById('viewUserFirstName').textContent = '-';
        document.getElementById('viewUserLastName').textContent = '-';
        document.getElementById('viewUserEmail').textContent = '-';
        document.getElementById('viewUserMobile').textContent = '-';
        document.getElementById('viewUserAddress').textContent = '-';
        document.getElementById('viewUserRole').textContent = '';
        document.getElementById('viewUserRoleBadge').textContent = 'USER';
        document.getElementById('viewUserId').textContent = '-';
        document.getElementById('viewUserLastUpdated').textContent = '-';
        document.getElementById('viewUserAvatar').src = '';
    });
}