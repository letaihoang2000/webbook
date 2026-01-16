$(document).ready(function() {
    const csrfToken = $('meta[name="_csrf"]').attr('content');
    const csrfHeader = $('meta[name="_csrf_header"]').attr('content');

    // Show toast notification
    function showToast(message, isError = false) {
        let $toast = $('#add-to-cart-toast');

        // Create toast if it doesn't exist
        if ($toast.length === 0) {
            $('body').append(`
                <div id="add-to-cart-toast" class="cart-notification-toast">
                    <i class="icon toast-icon"></i>
                    <span class="toast-message"></span>
                </div>
            `);
            $toast = $('#add-to-cart-toast');
        }

        $toast.removeClass('show success error');

        if (isError) {
            $toast.addClass('error');
            $toast.find('.toast-icon').removeClass('icon-check-circle').addClass('icon-close-circle');
        } else {
            $toast.addClass('success');
            $toast.find('.toast-icon').removeClass('icon-close-circle').addClass('icon-check-circle');
        }

        $toast.find('.toast-message').text(message);
        $toast.addClass('show');

        setTimeout(() => {
            $toast.removeClass('show');
        }, 3000);
    }

    // Update navbar cart display with INLINE badges
    function updateNavbarCart(cartSummary) {
        if (cartSummary) {
            const itemCount = cartSummary.itemCount || 0;

            // Update top navbar inline count
            const $topInlineCount = $('.cart.for-buy .inline-count.cart-count');
            if (itemCount > 0) {
                if ($topInlineCount.length === 0) {
                    $('.cart.for-buy').append(`<span class="inline-count cart-count">${itemCount}</span>`);
                } else {
                    $topInlineCount.text(itemCount).addClass('updated');
                    setTimeout(() => $topInlineCount.removeClass('updated'), 500);
                }
            } else {
                $topInlineCount.remove();
            }

            // Update navigation menu inline count
            const $navLink = $('.menu-list a[href="/cart"]');
            $navLink.find('.nav-inline-count').remove();
            if (itemCount > 0) {
                $navLink.append(`<span class="nav-inline-count">${itemCount}</span>`);
            }
        }
    }

    // Get book ID from cart button or its parent elements
    function getBookId($button) {
        const $productItem = $button.closest('.product-item, .wishlist-item');
        let bookId = null;

        // Method 1: From wishlist button in same product-actions div
        bookId = $productItem.find('.wishlist-btn').data('book-id');

        // Method 2: From remove-wishlist button (wishlist page)
        if (!bookId) {
            bookId = $productItem.find('.remove-wishlist-btn').data('book-id');
        }

        // Method 3: From parent wishlist-item data attribute
        if (!bookId) {
            bookId = $productItem.attr('data-book-id');
        }

        // Method 4: From any sibling element with data-book-id
        if (!bookId) {
            const $siblingWithId = $button.siblings('[data-book-id]');
            if ($siblingWithId.length > 0) {
                bookId = $siblingWithId.first().data('book-id');
            }
        }

        // Method 5: From closest element with data-book-id
        if (!bookId) {
            const $parent = $button.closest('[data-book-id]');
            if ($parent.length > 0) {
                bookId = $parent.data('book-id');
            }
        }

        return bookId;
    }

    // Handle Add to Cart button click
    $(document).on('click', '.cart-btn', function(e) {
        e.preventDefault();
        e.stopPropagation();

        const $button = $(this);
        const bookId = getBookId($button);

        console.log('Cart button clicked, Book ID:', bookId);

        if (!bookId) {
            console.error('ERROR: No book ID found!');
            showToast('Error: Book ID not found', true);
            return;
        }

        // Disable button during request
        $button.prop('disabled', true);
        $button.css('opacity', '0.6');

        $.ajax({
            url: '/cart/add',
            method: 'POST',
            headers: {
                [csrfHeader]: csrfToken
            },
            data: {
                bookId: bookId
            },
            success: function(response) {
                console.log('Add to cart response:', response);

                if (response.success) {
                    showToast(response.message);

                    // Update navbar cart
                    if (response.cartSummary) {
                        updateNavbarCart(response.cartSummary);
                    }

                    // Add animation to button
                    $button.addClass('added');
                    setTimeout(() => {
                        $button.removeClass('added');
                    }, 1000);
                } else {
                    // Book already in cart or other error
                    const isDuplicate = response.action === 'duplicate';
                    showToast(response.message || 'Failed to add to cart', isDuplicate ? false : true);
                }
            },
            error: function(xhr) {
                console.error('Add to cart error:', xhr);
                const message = xhr.responseJSON?.message || 'Error adding to cart';
                showToast(message, true);
            },
            complete: function() {
                // Re-enable button
                $button.prop('disabled', false);
                $button.css('opacity', '1');
            }
        });
    });

    // Load initial cart summary on page load
    function loadCartSummary() {
        $.ajax({
            url: '/cart/summary',
            method: 'GET',
            success: function(response) {
                updateNavbarCart(response);
            },
            error: function() {
                console.log('Failed to load cart summary');
            }
        });
    }

    // Load cart summary when page loads
    loadCartSummary();
});