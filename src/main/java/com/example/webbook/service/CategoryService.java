package com.example.webbook.service;

import com.example.webbook.dto.AddCategoryForm;
import com.example.webbook.model.Category;
import com.example.webbook.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    // Lấy tất cả categories
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // Lấy category theo ID
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    // Thêm category mới
    public Category addCategory(AddCategoryForm form) {
        Category category = new Category();
        category.setName(form.getName());
        return categoryRepository.save(category);
    }

    // Cập nhật category
    public Category updateCategory(Long id, AddCategoryForm form) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        category.setName(form.getName());
        return categoryRepository.save(category);
    }

    // Xóa category
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}
