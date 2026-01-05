// Toggle wishlist from all books page
$(document).on('click', '.wishlist-btn', function(e) {
    e.preventDefault();
    e.stopPropagation();

    console.log('=== Wishlist Button Clicked ===');

    const button = $(this);
    const bookId = button.data('book-id');
    const isInWishlist = button.hasClass('in-wishlist');

    console.log('Book ID:', bookId);
    console.log('Currently in wishlist:', isInWishlist);

    if (!bookId) {
        console.error('ERROR: No book ID found!');
        showNotification('Error: No book ID', 'error');
        return;
    }

    $.ajax({
        url: `/wishlist/toggle/${bookId}`,
        method: 'POST',
        beforeSend: function(xhr) {
            console.log('Sending AJAX request...');
        },
        success: function(response) {
            console.log('SUCCESS Response:', response);

            if (response.success) {
                if (response.action === 'added') {
                    button.addClass('in-wishlist');
                    button.attr('title', 'Remove from Wishlist');

                    // Change icon from heart to X
                    button.find('svg').replaceWith(`
                        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 384 512" class="x-icon">
                            <path d="M342.6 150.6c12.5-12.5 12.5-32.8 0-45.3s-32.8-12.5-45.3 0L192 210.7 86.6 105.4c-12.5-12.5-32.8-12.5-45.3 0s-12.5 32.8 0 45.3L146.7 256 41.4 361.4c-12.5 12.5-12.5 32.8 0 45.3s32.8 12.5 45.3 0L192 301.3 297.4 406.6c12.5 12.5 32.8 12.5 45.3 0s12.5-32.8 0-45.3L237.3 256 342.6 150.6z"/>
                        </svg>
                    `);

                    showNotification('Added to wishlist', 'success');
                    console.log('Book added to wishlist');
                } else {
                    button.removeClass('in-wishlist');
                    button.attr('title', 'Add to Wishlist');

                    // Change icon from X to heart
                    button.find('svg').replaceWith(`
                        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" class="heart-icon">
                            <path d="M47.6 300.4L228.3 469.1c7.5 7 17.4 10.9 27.7 10.9s20.2-3.9 27.7-10.9L464.4 300.4c30.4-28.3 47.6-68 47.6-109.5v-5.8c0-69.9-50.5-129.5-119.4-141C347 36.5 300.6 51.4 268 84L256 96 244 84c-32.6-32.6-79-47.5-124.6-39.9C50.5 55.6 0 115.2 0 185.1v5.8c0 41.5 17.2 81.2 47.6 109.5z"/>
                        </svg>
                    `);

                    showNotification('Removed from wishlist', 'info');
                    console.log('Book removed from wishlist');
                }

                // Update wishlist count in navbar if exists
                updateWishlistCount(response.count);
            } else {
                console.error('Response success=false:', response.message);
                showNotification(response.message, 'error');
            }
        },
        error: function(xhr, status, error) {
            console.error('AJAX ERROR');
            console.error('Status:', status);
            console.error('Response:', xhr.responseText);

            let errorMessage = 'Failed to update wishlist';
            if (xhr.status === 403) {
                errorMessage = 'Access denied';
            } else if (xhr.status === 404) {
                errorMessage = 'Wishlist endpoint not found';
            } else if (xhr.status === 405) {
                errorMessage = 'Method not allowed';
            } else if (xhr.status === 500) {
                errorMessage = 'Server error';
            }

            showNotification(errorMessage, 'error');
        }
    });
});