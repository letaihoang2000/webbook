$(document).ready(function() {

    // Handle book badge click
    $(document).on('click', '.books-badge-clickable', function() {
        const authorId = $(this).data('author-id');
        const authorName = $(this).data('author-name');

        if (!authorId) {
            alert('Author ID not found');
            return;
        }

        loadAuthorBooks(authorId, authorName);
    });

    function loadAuthorBooks(authorId, authorName) {
        // Set author name in modal
        $('#modalAuthorName').text(authorName);

        // Show loading spinner
        $('#booksLoadingSpinner').show();
        $('#booksErrorMessage').hide();
        $('#booksListContainer').hide();
        $('#noBooksMessage').hide();

        // Show modal
        $('#authorBooksModal').modal('show');

        // Fetch books
        fetch(`/author/books/${authorId}`)
            .then(response => response.json())
            .then(data => {
                $('#booksLoadingSpinner').hide();

                if (data.success && data.books) {
                    if (data.books.length === 0) {
                        $('#noBooksMessage').show();
                    } else {
                        displayBooks(data.books);
                        $('#booksListContainer').show();
                    }
                } else {
                    showError(data.message || 'Failed to load books');
                }
            })
            .catch(error => {
                console.error('Error loading books:', error);
                $('#booksLoadingSpinner').hide();
                showError('An error occurred while loading books');
            });
    }

    function displayBooks(books) {
        const tbody = $('#authorBooksList');
        tbody.empty();

        books.forEach(book => {
            const imageUrl = book.image && book.image.trim() !== ''
                ? book.image
                : 'https://via.placeholder.com/60x80?text=No+Image';

            const publishedDate = book.published_date || 'N/A';
            const pages = book.page || 'N/A';
            const price = book.price ? '$' + book.price.toFixed(2) : 'N/A';

            const row = `
                <tr>
                    <td>
                        <img src="${imageUrl}"
                             alt="${book.title}"
                             class="book-thumbnail"
                             style="width: 60px; height: 80px; object-fit: cover; border-radius: 4px;">
                    </td>
                    <td>
                        <strong>${book.title}</strong>
                        ${book.description ? '<br><small class="text-muted">' + truncateText(book.description, 80) + '</small>' : ''}
                    </td>
                    <td>${publishedDate}</td>
                    <td>${pages}</td>
                    <td class="text-success fw-bold">${price}</td>
                    <td class="text-center">
                        <a href="/book/view/${book.book_id}"
                           class="btn btn-sm btn-outline-primary"
                           title="View Full Details">
                            <i class="fas fa-external-link-alt"></i>
                        </a>
                    </td>
                </tr>
            `;
            tbody.append(row);
        });
    }

    function truncateText(text, maxLength) {
        if (!text) return '';
        if (text.length <= maxLength) return text;
        return text.substring(0, maxLength) + '...';
    }

    function showError(message) {
        $('#booksErrorText').text(message);
        $('#booksErrorMessage').show();
        $('#booksListContainer').hide();
    }
});