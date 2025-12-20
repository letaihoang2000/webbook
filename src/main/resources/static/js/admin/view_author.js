$(document).ready(function() {
    console.log('=== view_author.js loaded ===');

    // Handle view button click - populate modal with author data
    $(document).on('click', '.view-author-btn', function() {
        const authorId = $(this).data('author-id');
        const name = $(this).data('name');
        const image = $(this).data('image');
        const description = $(this).data('description');
        const booksCount = $(this).data('books');

        console.log('View button clicked for author:', authorId);
        console.log('Name:', name);
        console.log('Books count:', booksCount);

        // Store author data in modal for edit button
        $('#viewAuthorModal').data('author-id', authorId);
        $('#viewAuthorModal').data('author-name', name);
        $('#viewAuthorModal').data('author-image', image);
        $('#viewAuthorModal').data('author-description', description);

        // Populate author details
        $('#view_author_name').text(name);
        $('#view_author_books').text(booksCount + ' book(s)');

        // Set author image
        if (image && image.trim() !== '') {
            $('#view_author_image').attr('src', image);
        } else {
            $('#view_author_image').attr('src', 'https://via.placeholder.com/300x350?text=No+Image');
        }

        // Set description
        if (description && description.trim() !== '') {
            $('#view_author_description').text(description);
        } else {
            $('#view_author_description').html('<em class="text-muted">No description available</em>');
        }

        // Show modal
        $('#viewAuthorModal').modal('show');
    });

    // Handle edit button click from view modal
    $('#editAuthorFromView').on('click', function() {
        const authorId = $('#viewAuthorModal').data('author-id');
        const name = $('#viewAuthorModal').data('author-name');
        const image = $('#viewAuthorModal').data('author-image');
        const description = $('#viewAuthorModal').data('author-description');

        console.log('Edit from view modal:', authorId);

        // Close view modal
        $('#viewAuthorModal').modal('hide');

        // Wait for view modal to close, then open edit modal
        $('#viewAuthorModal').on('hidden.bs.modal', function() {
            // Populate edit form
            $('#update_author_id').val(authorId);
            $('#update_author_name').val(name);
            $('#update_author_description').val(description);

            // Display current image
            if (image && image.trim() !== '') {
                $('#currentAuthorImage').attr('src', image);
                $('#currentAuthorImageContainer').show();
                $('#noCurrentAuthorImage').hide();
            } else {
                $('#currentAuthorImageContainer').hide();
                $('#noCurrentAuthorImage').show();
            }

            // Clear file input and preview
            $('#update_author_image').val('');
            $('#updateAuthorImagePreview').hide();

            // Show update modal
            $('#updateAuthorForm').modal('show');

            // Remove this event handler to prevent multiple bindings
            $('#viewAuthorModal').off('hidden.bs.modal');
        });
    });

    // Reset modal when closed
    $('#viewAuthorModal').on('hidden.bs.modal', function() {
        $('#viewAuthorModal').removeData();
    });
});