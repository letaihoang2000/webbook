$(document).ready(function() {
    console.log('Wishlist.js loaded successfully');
    console.log('jQuery version:', $.fn.jquery);

    // Update wishlist count in navbar with INLINE badges
    function updateWishlistCount(count) {
        console.log('Updating wishlist count:', count);

        // Update top navbar inline count
        const $topInlineCount = $('.wishlist.for-buy .inline-count.wishlist-count');
        if (count > 0) {
            if ($topInlineCount.length === 0) {
                $('.wishlist.for-buy').append(`<span class="inline-count wishlist-count">${count}</span>`);
            } else {
                $topInlineCount.text(count).addClass('updated');
                setTimeout(() => $topInlineCount.removeClass('updated'), 500);
            }
        } else {
            $topInlineCount.remove();
        }

        // Update navigation menu inline count
        const $navLink = $('.menu-list a[href="/wishlist"]');
        $navLink.find('.nav-inline-count').remove();
        if (count > 0) {
            $navLink.append(`<span class="nav-inline-count">${count}</span>`);
        }
    }

    // Show notification
    function showNotification(message, type) {
        console.log('Showing notification:', message, type);

        // Remove any existing notifications first
        $('.wishlist-notification').remove();

        // Create notification element
        const notification = $(`
            <div class="wishlist-notification ${type}">
                <i class="icon icon-${type === 'success' ? 'check' : type === 'error' ? 'close' : 'info'}"></i>
                <span>${message}</span>
            </div>
        `);

        // Append to body
        $('body').append(notification);

        // Show notification with slight delay for animation
        setTimeout(function() {
            notification.addClass('show');
        }, 10);

        // Hide and remove notification
        setTimeout(function() {
            notification.removeClass('show');
            setTimeout(function() {
                notification.remove();
            }, 300);
        }, 3000);
    }

    // Toggle wishlist (works for all books page AND book detail page)
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

                        // For all_books page - change SVG icon from heart to X
                        if (button.find('svg').length > 0) {
                            button.find('svg').replaceWith(`
                                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 384 512" class="x-icon">
                                    <path d="M342.6 150.6c12.5-12.5 12.5-32.8 0-45.3s-32.8-12.5-45.3 0L192 210.7 86.6 105.4c-12.5-12.5-32.8-12.5-45.3 0s-12.5 32.8 0 45.3L146.7 256 41.4 361.4c-12.5 12.5-12.5 32.8 0 45.3s32.8 12.5 45.3 0L192 301.3 297.4 406.6c12.5 12.5 32.8 12.5 45.3 0s12.5-32.8 0-45.3L237.3 256 342.6 150.6z"/>
                                </svg>
                            `);
                        }

                        // For book_detail page - change text
                        if (button.find('span').length > 0) {
                            button.find('span').text('Remove from Wishlist');
                        }

                        showNotification('Added to wishlist', 'success');
                        console.log('Book added to wishlist');
                    } else {
                        button.removeClass('in-wishlist');
                        button.attr('title', 'Add to Wishlist');

                        // For all_books page - change SVG icon from X to heart
                        if (button.find('svg').length > 0) {
                            button.find('svg').replaceWith(`
                                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" class="heart-icon">
                                    <path d="M47.6 300.4L228.3 469.1c7.5 7 17.4 10.9 27.7 10.9s20.2-3.9 27.7-10.9L464.4 300.4c30.4-28.3 47.6-68 47.6-109.5v-5.8c0-69.9-50.5-129.5-119.4-141C347 36.5 300.6 51.4 268 84L256 96 244 84c-32.6-32.6-79-47.5-124.6-39.9C50.5 55.6 0 115.2 0 185.1v5.8c0 41.5 17.2 81.2 47.6 109.5z"/>
                                </svg>
                            `);
                        }

                        // For book_detail page - change text
                        if (button.find('span').length > 0) {
                            button.find('span').text('Add to Wishlist');
                        }

                        showNotification('Removed from wishlist', 'info');
                        console.log('Book removed from wishlist');
                    }

                    // Update wishlist count in navbar WITH INLINE BADGES
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

    // Remove from wishlist (on wishlist page)
    $(document).on('click', '.remove-wishlist-btn', function(e) {
        e.preventDefault();
        e.stopPropagation();

        console.log('=== Remove Wishlist Button Clicked ===');

        const button = $(this);
        const bookId = button.data('book-id');
        const wishlistItem = button.closest('.wishlist-item').parent();

        console.log('Book ID:', bookId);

        // Confirm removal
        if (!confirm('Remove this book from your wishlist?')) {
            console.log('User cancelled removal');
            return;
        }

        $.ajax({
            url: `/wishlist/remove/${bookId}`,
            method: 'POST',
            success: function(response) {
                console.log('Remove SUCCESS:', response);

                if (response.success) {
                    // Animate removal
                    wishlistItem.find('.wishlist-item').addClass('removing');

                    setTimeout(function() {
                        wishlistItem.remove();

                        // Update count WITH INLINE BADGES
                        updateWishlistCount(response.count);

                        // Check if wishlist is now empty
                        if ($('.wishlist-item').length === 0) {
                            console.log('Wishlist now empty, reloading...');
                            location.reload();
                        } else {
                            // Update results info
                            const count = response.count;
                            const text = count === 1 ? 'book' : 'books';
                            $('.results-info strong').first().text(count);
                            $('.results-info span').text(text);
                        }

                        showNotification('Removed from wishlist', 'success');
                    }, 500);
                } else {
                    console.error('Remove failed:', response.message);
                    showNotification(response.message, 'error');
                }
            },
            error: function(xhr, status, error) {
                console.error('Remove AJAX ERROR');
                console.error('Status:', status);
                console.error('Error:', error);
                console.error('Response:', xhr.responseText);

                showNotification('Failed to remove from wishlist', 'error');
            }
        });
    });
});