document.addEventListener('DOMContentLoaded', function () {
  const updateCategoryModal = document.getElementById('updateCategoryForm');
  const updateForm = document.getElementById('updateCategoryFormElement');
  const idInput = document.getElementById('updateCategoryId');
  const nameInput = document.getElementById('updateCategoryName');
  const messageDiv = document.getElementById('updateCategoryMessage');

  if (!updateCategoryModal || !updateForm || !idInput || !nameInput) {
    console.warn('Update category modal elements not found');
    return;
  }

  function showMessage(type, text) {
    if (!messageDiv) return;
    messageDiv.style.display = 'block';
    messageDiv.className = 'alert alert-' + type;
    messageDiv.textContent = text;
  }

  function hideMessage() {
    if (!messageDiv) return;
    messageDiv.style.display = 'none';
  }

  // Khi bấm nút Edit → fill data vào modal
  document.addEventListener('click', function (e) {
    const btn = e.target.closest('.edit-category-btn');
    if (!btn) return;

    hideMessage();

    const id = btn.getAttribute('data-id');
    const name = btn.getAttribute('data-name');

    idInput.value = id || '';
    nameInput.value = name || '';
  });

  // Submit form bằng AJAX
  updateForm.addEventListener('submit', function (e) {
    e.preventDefault();
    hideMessage();

    const id = idInput.value.trim();
    const name = nameInput.value.trim();

    if (!id || !name) {
      showMessage('danger', 'ID và tên category không được để trống.');
      return;
    }

    const formData = new FormData(updateForm);

    const submitBtn = updateForm.querySelector('button[type="submit"]');
    const originalText = submitBtn.textContent;
    submitBtn.disabled = true;
    submitBtn.textContent = 'Saving...';

    fetch('/category/update', {
      method: 'POST',
      body: formData
    })
      .then(resp => resp.json())
      .then(data => {
        if (data.success) {
          showMessage('success', data.message || 'Cập nhật category thành công!');

          // Đóng modal + reload trang để cập nhật list
          setTimeout(function () {
            $('#updateCategoryForm').modal('hide'); // dùng jQuery + Bootstrap
            window.location.reload();
          }, 1000);
        } else {
          showMessage('danger', data.message || 'Cập nhật category thất bại.');
        }
      })
      .catch(err => {
        console.error(err);
        showMessage('danger', 'Đã xảy ra lỗi khi cập nhật category.');
      })
      .finally(() => {
        submitBtn.disabled = false;
        submitBtn.textContent = originalText;
      });
  });
});
