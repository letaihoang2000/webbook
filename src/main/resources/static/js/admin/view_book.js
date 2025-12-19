$(document).ready(function() {
    console.log('=== view_book.js loaded ===');

    // Handle view button click - populate modal with book data
    $(document).on('click', '.view-book-btn', function(e) {
        e.preventDefault();

        const bookId = $(this).data('book-id');
        const title = $(this).data('title');
        const image = $(this).data('image');
        const description = $(this).data('description');
        const publishedDate = $(this).data('published-date');
        const page = $(this).data('page');
        const price = $(this).data('price');
        const bookContent = $(this).data('book-content');
        const author = $(this).data('author');
        const category = $(this).data('category');

        console.log('View button clicked for book:', bookId);
        console.log('Title:', title);
        console.log('PDF URL:', bookContent);

        // Store book ID in hidden field for "View Full Details" button
        $('#current_book_id').val(bookId);

        // Populate book details
        $('#view_book_title').text(title || 'Untitled');
        $('#view_book_author').text(author || 'Unknown');
        $('#view_book_category').text(category || 'Uncategorized');
        $('#view_book_published_date').text(publishedDate || 'N/A');
        $('#view_book_page').text(page || 'N/A');
        $('#view_book_price').text('$' + (price || '0.00'));

        // Set book image
        if (image && image.trim() !== '') {
            $('#view_book_image').attr('src', image);
        } else {
            $('#view_book_image').attr('src', 'https://via.placeholder.com/400x600?text=No+Image');
        }

        // Set description
        if (description && description.trim() !== '') {
            $('#view_book_description').text(description).show();
            $('#no_description').hide();
        } else {
            $('#view_book_description').hide();
            $('#no_description').show();
        }

        // Handle PDF preview
        if (bookContent && bookContent.trim() !== '') {
            console.log('Loading PDF preview for:', bookContent);

            // Show PDF preview container
            $('#pdf_preview_container').show();
            $('#no_pdf_content').hide();

            // Set iframe src to display PDF
            $('#pdf_preview_iframe').attr('src', bookContent);

            // Set download link
            $('#view_pdf_download').attr('href', bookContent).show();
        } else {
            console.log('No PDF content available');
            $('#pdf_preview_container').hide();
            $('#no_pdf_content').show();
            $('#view_pdf_download').hide();
        }

        // Set the "View Full Details" button link
        $('#view_full_details_btn').attr('href', '/book/view/' + bookId);

        // Show the modal
        $('#viewBookModal').modal('show');
    });

    // Clear iframe when modal is closed to stop loading
    $('#viewBookModal').on('hidden.bs.modal', function() {
        console.log('Modal closed, clearing PDF preview');
        $('#pdf_preview_iframe').attr('src', '');
        $('#current_book_id').val('');
        $('#view_full_details_btn').attr('href', '#');
    });
});