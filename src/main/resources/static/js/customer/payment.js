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
    $('.payment-option-card').on('click', function() {
        if (!$(this).hasClass('disabled')) {
            const radio = $(this).find('input[type="radio"]');
            radio.prop('checked', true);
            $('.payment-option-card').removeClass('active');
            $(this).addClass('active');
        }
    });

    // Confirm payment button
    $('#confirm-payment-btn').on('click', function() {
        const selectedMethod = $('input[name="paymentMethod"]:checked').val();

        if (selectedMethod === 'paypal') {
            // Show loading state
            $(this).prop('disabled', true).html('<span class="spinner-border spinner-border-sm me-2"></span>Redirecting...');

            // Create form and submit
            const form = $('<form>', {
                'method': 'POST',
                'action': '/cart/checkout'
            });

            form.append($('<input>', {
                'type': 'hidden',
                'name': '_csrf',
                'value': csrfToken
            }));

            $('body').append(form);
            form.submit();
        }
    });

    // Check for payment status from URL
    const urlParams = new URLSearchParams(window.location.search);
    const paymentStatus = urlParams.get('payment');

    console.log('Payment status:', paymentStatus); // DEBUG

    if (paymentStatus === 'success') {
        const orderId = urlParams.get('orderId');
        const amount = urlParams.get('amount');

        console.log('Success! Order:', orderId, 'Amount:', amount); // DEBUG

        $('#modal-success-order-id').text(orderId || '-');
        $('#modal-success-amount').text(parseFloat(amount || 0).toFixed(2));

        // Show success modal
        const successModal = new bootstrap.Modal(document.getElementById('paymentSuccessModal'));
        successModal.show();

        console.log('Success modal should be showing'); // DEBUG

        // Clean URL after showing modal
        setTimeout(() => {
            window.history.replaceState({}, document.title, '/cart');
        }, 1000);

    } else if (paymentStatus === 'failed') {
        const reason = urlParams.get('reason');
        let message = 'Your payment could not be processed. Please try again.';

        if (reason === 'empty') {
            message = 'Your cart is empty. Please add items before checkout.';
        } else if (reason === 'capture') {
            message = 'Payment capture failed. Please try again or contact support.';
        } else if (reason === 'error') {
            message = 'A system error occurred. Please try again later.';
        }

        console.log('Failed! Reason:', reason); // DEBUG

        $('#failed-reason').text(message);

        const failedModal = new bootstrap.Modal(document.getElementById('paymentFailedModal'));
        failedModal.show();

        // Clean URL
        setTimeout(() => {
            window.history.replaceState({}, document.title, '/cart');
        }, 1000);

    } else if (paymentStatus === 'cancelled') {
        console.log('Cancelled!'); // DEBUG

        const cancelledModal = new bootstrap.Modal(document.getElementById('paymentCancelledModal'));
        cancelledModal.show();

        // Clean URL
        setTimeout(() => {
            window.history.replaceState({}, document.title, '/cart');
        }, 1000);
    }
});