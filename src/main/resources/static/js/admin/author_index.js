$(document).ready(function() {
    console.log('=== author_index.js loaded ===');

    // Search functionality
    let searchTimeout;
    $('#nameSearch').on('input', function() {
        clearTimeout(searchTimeout);
        const searchValue = $(this).val().trim();

        searchTimeout = setTimeout(function() {
            if (searchValue) {
                window.location.href = `/author/authors?search=${encodeURIComponent(searchValue)}`;
            } else {
                window.location.href = '/author/authors';
            }
        }, 500);
    });
});