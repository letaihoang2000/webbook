$(document).ready(function() {
    const csrfToken = $('meta[name="_csrf"]').attr('content');
    const csrfHeader = $('meta[name="_csrf_header"]').attr('content');

    // When payment method modal opens, populate order summary
    $('#paymentMethodModal').on('show.bs.modal', function () {
        const itemCount = $('#cart-item-count').text();
        const subtotal = parseFloat($('#summary-subtotal').text()) || 0;
        const tax = subtotal * 0.1;
        const total = subtotal + tax;

        $('#modal-item-count').text(itemCount);
        $('#modal-subtotal').text(subtotal.toFixed(2));
        $('#modal-tax').text(tax.toFixed(2));
        $('#modal-total').text(total.toFixed(2));
    });

    // Handle payment method selection
    $('.payment-option').on('click', function() {
        if (!$(this).hasClass('disabled')) {
            const radio = $(this).find('input[type="radio"]');
            radio.prop('checked', true);
            $('.payment-option').removeClass('selected');
            $(this).addClass('selected');
        }
    });

    // Confirm payment button
    $('#confirm-payment-btn').on('click', function() {
        const selectedMethod = $('input[name="paymentMethod"]:checked').val();

        if (selectedMethod === 'paypal') {
            // Show loading state
            $(this).prop('disabled', true).html('<span class="spinner-border spinner-border-sm me-2"></span>Redirecting...');

            // Create a form and submit it via POST
            const form = $('<form>', {
                'method': 'POST',
                'action': '/cart/checkout'
            });

            // Add CSRF token
            form.append($('<input>', {
                'type': 'hidden',
                'name': '_csrf',
                'value': csrfToken
            }));

            // Append to body and submit
            $('body').append(form);
            form.submit();
        }
    });

    // Check for payment success from URL parameters
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('payment') === 'success') {
        const orderId = urlParams.get('orderId');
        const amount = urlParams.get('amount');

        $('#success-order-id').text(orderId);
        $('#success-amount').text(parseFloat(amount).toFixed(2));

        // Show success modal
        $('#paymentSuccessModal').modal('show');

        // Clean URL after showing modal
        window.history.replaceState({}, document.title, '/cart');
    }
});