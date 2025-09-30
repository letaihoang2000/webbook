document.addEventListener('DOMContentLoaded', function() {
    const searchInput = document.getElementById('userSearch');
    let searchTimeout;

    // Handle search input with debounce
    searchInput.addEventListener('input', function() {
        clearTimeout(searchTimeout);

        searchTimeout = setTimeout(function() {
            const searchQuery = searchInput.value.trim();
            const currentUrl = new URL(window.location);

            if (searchQuery) {
                currentUrl.searchParams.set('search', searchQuery);
            } else {
                currentUrl.searchParams.delete('search');
            }

            // Reset to first page when searching
            currentUrl.searchParams.set('page', '0');

            // Navigate to new URL
            window.location.href = currentUrl.toString();
        }, 500); // 500ms debounce
    });

    // Handle Enter key
    searchInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            clearTimeout(searchTimeout);

            const searchQuery = searchInput.value.trim();
            const currentUrl = new URL(window.location);

            if (searchQuery) {
                currentUrl.searchParams.set('search', searchQuery);
            } else {
                currentUrl.searchParams.delete('search');
            }

            currentUrl.searchParams.set('page', '0');
            window.location.href = currentUrl.toString();
        }
    });
});