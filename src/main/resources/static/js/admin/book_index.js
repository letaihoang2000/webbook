document.addEventListener('DOMContentLoaded', function() {
    const titleSearch = document.getElementById('titleSearch');
    const authorSearch = document.getElementById('authorSearch');
    const categoryFilter = document.getElementById('categoryFilter');
    let searchTimeout;

    // Handle title search
    titleSearch.addEventListener('input', function() {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(function() {
            performSearch('title', titleSearch.value.trim());
        }, 500);
    });

    // Handle author search
    authorSearch.addEventListener('input', function() {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(function() {
            performSearch('author', authorSearch.value.trim());
        }, 500);
    });

    // Handle category filter
    categoryFilter.addEventListener('change', function() {
        const categoryId = this.value;
        const currentUrl = new URL(window.location);

        if (categoryId) {
            currentUrl.searchParams.set('categoryId', categoryId);
        } else {
            currentUrl.searchParams.delete('categoryId');
        }

        currentUrl.searchParams.set('page', '0');
        window.location.href = currentUrl.toString();
    });

    function performSearch(searchBy, searchQuery) {
        const currentUrl = new URL(window.location);

        if (searchQuery) {
            currentUrl.searchParams.set('searchBy', searchBy);
            currentUrl.searchParams.set('search', searchQuery);
        } else {
            currentUrl.searchParams.delete('searchBy');
            currentUrl.searchParams.delete('search');
        }

        currentUrl.searchParams.set('page', '0');
        window.location.href = currentUrl.toString();
    }
});