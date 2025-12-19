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

    // Button placeholders (will be implemented later with modals)
    $('.view-author-btn, .edit-author-btn, .delete-author-btn').on('click', function() {
        alert('Modal functionality will be added later');
    });

    $('button:contains("Add Author")').on('click', function() {
        alert('Add Author modal will be added later');
    });
});