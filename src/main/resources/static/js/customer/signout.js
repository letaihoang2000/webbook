document.addEventListener('DOMContentLoaded', function() {
    const logoutModal = document.getElementById('logoutModal');
    const confirmLogoutBtn = document.getElementById('confirmLogoutBtn');

    // Auto-hide alerts functionality
    autoHideAlerts();

    document.addEventListener('click', function(e) {
        if (e.target.closest('.logout-btn')) {

            if (logoutModal && typeof $ !== 'undefined') {
                $('#logoutModal').modal('show');
            } else {
                console.error('Modal element or jQuery not found');
            }
        }
    });

    // Handle confirm logout button click
    if (confirmLogoutBtn) {
        confirmLogoutBtn.addEventListener('click', function() {

            // Disable button during request
            const originalText = this.innerHTML;
            this.disabled = true;
            this.innerHTML = '<i class="fas fa-spinner fa-spin me-1"></i> Signing out...';

            // Call logout function
            performLogout(this, originalText);
        });
    }
});

/**
 * Auto-hide success and error messages after 7 seconds
 */
function autoHideAlerts() {
    // Find all alert messages (both success and error)
    const alerts = document.querySelectorAll('.alert');

    if (alerts.length > 0) {
        console.log(`Found ${alerts.length} alert(s) to auto-hide`);

        alerts.forEach(function(alert) {
            // Add fade-out animation after 7 seconds
            setTimeout(function() {
                console.log('Auto-hiding alert after 7 seconds');
                // Add a fade-out effect
                alert.style.transition = 'opacity 0.5s ease-out';
                alert.style.opacity = '0';

                // Remove the element from DOM after fade-out completes
                setTimeout(function() {
                    if (alert.parentNode) {
                        alert.parentNode.removeChild(alert);
                        console.log('Alert removed from DOM');
                    }
                }, 500); // Wait for fade-out animation to complete

            }, 7000); // 7 seconds delay
        });

        // Add click-to-dismiss functionality
        alerts.forEach(function(alert) {
            // Make alerts clickable to dismiss
            alert.style.cursor = 'pointer';
            alert.title = 'Click to dismiss';

            alert.addEventListener('click', function() {
                console.log('Alert clicked - dismissing immediately');
                this.style.transition = 'opacity 0.3s ease-out';
                this.style.opacity = '0';

                setTimeout(() => {
                    if (this.parentNode) {
                        this.parentNode.removeChild(this);
                        console.log('Alert dismissed by click');
                    }
                }, 300);
            });
        });
    } else {
        console.log('No alerts found on page');
    }
}

function performLogout(button, originalText) {
    try {

        // Hide modal first
        if (typeof $ !== 'undefined') {
            $('#logoutModal').modal('hide');
        }

        // Submit the logout form to Spring Security POST /logout
        const logoutForm = document.getElementById('logoutForm');
        if (logoutForm) {
            console.log('Submitting logout form via POST');
            logoutForm.submit();
        } else {
            console.log('No logout form found, creating one dynamically');
            // Create a form dynamically for POST request
            createAndSubmitLogoutForm();
        }
    } catch (error) {
        console.error('Error during logout:', error);
        // Fallback: create form for POST request
        createAndSubmitLogoutForm();
    } finally {
        // Re-enable button
        if (button) {
            button.disabled = false;
            button.innerHTML = originalText;
        }
    }
}

function createAndSubmitLogoutForm() {
    console.log('Creating dynamic logout form');

    // Create a form element
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/logout';

    // Try to get CSRF token from meta tags first
    const csrfToken = document.querySelector('meta[name="_csrf"]');

    if (csrfToken && csrfToken.getAttribute('content')) {
        const csrfInput = document.createElement('input');
        csrfInput.type = 'hidden';
        csrfInput.name = '_csrf';
        csrfInput.value = csrfToken.getAttribute('content');
        form.appendChild(csrfInput);
        console.log('Added CSRF token from meta tag to form');
    } else {
        // Fallback: try to get CSRF token from existing hidden input (like in login form)
        const existingCsrfInput = document.querySelector('input[name="_csrf"]');
        if (existingCsrfInput) {
            const csrfInput = document.createElement('input');
            csrfInput.type = 'hidden';
            csrfInput.name = '_csrf';
            csrfInput.value = existingCsrfInput.value;
            form.appendChild(csrfInput);
            console.log('Added CSRF token from existing input to form');
        } else {
            console.warn('No CSRF token found - logout may fail with 403 error');
        }
    }

    // Add form to body and submit
    document.body.appendChild(form);
    console.log('Submitting dynamic logout form');
    form.submit();
}

// Clear modal data when it's hidden
if (typeof $ !== 'undefined') {
    $('#logoutModal').on('hidden.bs.modal', function() {
        console.log('Logout modal closed');
        // Reset confirm button if needed
        const confirmLogoutBtn = document.getElementById('confirmLogoutBtn');
        if (confirmLogoutBtn) {
            confirmLogoutBtn.disabled = false;
            confirmLogoutBtn.innerHTML = '<i class="fa fa-sign-out me-1"></i> Sign Out';
        }
    });
}