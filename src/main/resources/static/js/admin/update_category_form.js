// /js/admin/update_category_form.js
$(document).ready(function () {
    $('.edit-category-btn').on('click', function () {
        const id = $(this).data('category-id');
        const name = $(this).data('category-name');

        $('#updateCategoryId').val(id);
        $('#updateCategoryName').val(name);
    });

    // Tạm thời chỉ log dữ liệu khi submit (vì chưa có backend)
    $('#updateCategoryFormElement').on('submit', function (e) {
        e.preventDefault();
        const data = {
            id: $('#updateCategoryId').val(),
            name: $('#updateCategoryName').val()
        };
        console.log('Update Category (dummy):', data);
        // Sau này gọi AJAX hoặc submit form thật ở đây

        // Đóng modal lại cho vui
        $('#updateCategoryForm').modal('hide');
    });
});
