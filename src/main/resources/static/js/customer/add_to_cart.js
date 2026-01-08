// Add to Cart functionality - add-to-cart.js (Updated with navbar badge support)

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

    // Update navbar cart display
    function updateNavbarCart(cartSummary) {
        if (cartSummary) {
            const itemCount = cartSummary.itemCount || 0;
            const totalValue = cartSummary.totalValue || 0;

            // Update cart text
            $('.cart.for-buy .cart-count').text(itemCount);
            $('.cart.for-buy .cart-total').text(totalValue.toFixed(2));

            // Update top navbar badge
            const $topBadge = $('.cart.for-buy .cart-badge');
            if (itemCount > 0) {
                if ($topBadge.length === 0) {
                    $('.cart.for-buy').append(`<span class="count-badge cart-badge">${itemCount}</span>`);
                } else {
                    $topBadge.text(itemCount).addClass('updated');
                    setTimeout(() => $topBadge.removeClass('updated'), 400);
                }
            } else {
                $topBadge.remove();
            }

            // Update navigation menu badge
            const $navBadge = $('.menu-list .nav-link .nav-count-badge');
            $('.menu-list a[href="/cart"]').find('.nav-count-badge').remove();
            if (itemCount > 0) {
                $('.menu-list a[href="/cart"]').append(`<span class="nav-count-badge">${itemCount}</span>`);
            }
        }
    }

    // Handle Add to Cart button click
    $(document).on('click', '.cart-btn', function(e) {
        e.preventDefault();
        e.stopPropagation();

        const $button = $(this);
        const $productItem = $button.closest('.product-item, .wishlist-item');

        // Try to get book ID from wishlist button first, then from remove button
        let bookId = $productItem.find('.wishlist-btn').data('book-id');
        if (!bookId) {
            bookId = $productItem.find('.remove-wishlist-btn').data('book-id');
        }

        if (!bookId) {
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
                    // Book already in cart
                    showToast(response.message || 'Failed to add to cart', response.action === 'duplicate');
                }
            },
            error: function(xhr) {
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