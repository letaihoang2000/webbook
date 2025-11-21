package com.example.webbook.controller;

import com.example.webbook.dto.AddCategoryForm;
import com.example.webbook.model.Category;
import org.springframework.ui.Model;
import com.example.webbook.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    // Hiển thị danh sách categories
    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "users/admin/category_index"; // Trả về view list.html
    }

    // Hiển thị form thêm category
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("categoryForm", new AddCategoryForm());
        return "category/add"; // Trả về view add.html
    }

    // Xử lý thêm category
    @PostMapping("/add")
    public String addCategory(@ModelAttribute AddCategoryForm form, RedirectAttributes redirectAttributes) {
        categoryService.addCategory(form);
        redirectAttributes.addFlashAttribute("message", "Thêm category thành công!");
        return "redirect:/category";
    }

    // Hiển thị form sửa category
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        AddCategoryForm form = new AddCategoryForm();
        form.setName(category.getName());

        model.addAttribute("categoryForm", form);
        model.addAttribute("categoryId", id);
        return "category/edit"; // Trả về view edit.html
    }

    // Xử lý cập nhật category
    @PostMapping("/edit/{id}")
    public String updateCategory(@PathVariable Long id, @ModelAttribute AddCategoryForm form,
                                 RedirectAttributes redirectAttributes) {
        categoryService.updateCategory(id, form);
        redirectAttributes.addFlashAttribute("message", "Cập nhật category thành công!");
        return "redirect:/category";
    }

    // Xóa category
    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        categoryService.deleteCategory(id);
        redirectAttributes.addFlashAttribute("message", "Xóa category thành công!");
        return "redirect:/category";
    }
}
