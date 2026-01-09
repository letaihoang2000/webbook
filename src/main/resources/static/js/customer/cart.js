$(document).ready(function() {
    const csrfToken = $('meta[name="_csrf"]').attr('content');
    const csrfHeader = $('meta[name="_csrf_header"]').attr('content');

    // Show toast notification
    function showToast(message, isError = false) {
        const $toast = $('#cart-toast');
        $toast.removeClass('show error');

        if (isError) {
            $toast.addClass('error');
            $toast.find('.toast-icon').removeClass('icon-check-circle').addClass('icon-close-circle');
        } else {
            $toast.find('.toast-icon').removeClass('icon-close-circle').addClass('icon-check-circle');
        }

        $toast.find('.toast-message').text(message);
        $toast.addClass('show');

        setTimeout(() => {
            $toast.removeClass('show');
        }, 3000);
    }

    // Update cart summary
    function updateCartSummary(totalValue) {
        const subtotal = parseFloat(totalValue) || 0;
        const tax = subtotal * 0.1; // 10% tax
        const total = subtotal + tax;

        $('#summary-subtotal').text(subtotal.toFixed(2));
        $('#summary-tax').text(tax.toFixed(2));
        $('#summary-total').text(total.toFixed(2));
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

            // Update cart item count in page
            $('#cart-item-count').text(itemCount);
            $('#summary-item-count').text(itemCount);
        }
    }

    // Handle remove item
    $('.btn-remove-item').on('click', function() {
        const $cartItem = $(this).closest('.cart-item');
        const bookId = $(this).data('book-id');

        if (confirm('Remove this book from cart?')) {
            $cartItem.addClass('removing');

            $.ajax({
                url: '/cart/remove',
                method: 'POST',
                headers: {
                    [csrfHeader]: csrfToken
                },
                data: {
                    bookId: bookId
                },
                success: function(response) {
                    if (response.success) {
                        // Remove item from DOM
                        $cartItem.slideUp(400, function() {
                            $(this).remove();

                            // Check if cart is empty
                            if ($('.cart-item').length === 0) {
                                location.reload();
                            }
                        });

                        // Update cart summary
                        if (response.cartSummary) {
                            updateCartSummary(response.cartSummary.totalValue);
                            updateNavbarCart(response.cartSummary);
                        }

                        showToast('Book removed from cart');
                    } else {
                        $cartItem.removeClass('removing');
                        showToast(response.message || 'Failed to remove book', true);
                    }
                },
                error: function() {
                    $cartItem.removeClass('removing');
                    showToast('Error removing book', true);
                }
            });
        }
    });

    // Handle clear cart
    $('#clear-cart-btn').on('click', function() {
        if (confirm('Are you sure you want to clear your entire cart?')) {
            $.ajax({
                url: '/cart/clear',
                method: 'POST',
                headers: {
                    [csrfHeader]: csrfToken
                },
                success: function(response) {
                    if (response.success) {
                        showToast('Cart cleared');
                        setTimeout(() => {
                            location.reload();
                        }, 1000);
                    } else {
                        showToast(response.message || 'Failed to clear cart', true);
                    }
                },
                error: function() {
                    showToast('Error clearing cart', true);
                }
            });
        }
    });

    // Handle checkout (placeholder)
    $('#checkout-btn').on('click', function() {
        // TODO: Implement checkout functionality
        showToast('Checkout feature coming soon!');
    });

    // Initialize cart summary on page load
    const initialTotal = parseFloat($('#summary-subtotal').text()) || 0;
    updateCartSummary(initialTotal);
});