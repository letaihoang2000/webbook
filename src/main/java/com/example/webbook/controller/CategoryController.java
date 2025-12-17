package com.example.webbook.controller;

import com.example.webbook.dto.AddCategoryForm;
import com.example.webbook.model.Category;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import com.example.webbook.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    // Hiển thị danh sách categories
    @GetMapping
    public String listCategories(Model model) {

        // Danh sách category
        model.addAttribute("categories", categoryService.getAllCategories());

        // QUAN TRỌNG: đảm bảo luôn có categoryForm trong model
        if (!model.containsAttribute("categoryForm")) {
            model.addAttribute("categoryForm", new AddCategoryForm());
        }

        return "users/admin/category_index"; // Trả về view list.html
    }


    @PostMapping("/add")
    public String addCategory(@ModelAttribute("categoryForm") AddCategoryForm form,
                              RedirectAttributes redirectAttributes) {
        categoryService.addCategory(form);
        redirectAttributes.addFlashAttribute("message", "Thêm category thành công!");
        // để modal Add tiếp tục dùng được khi reload
        redirectAttributes.addFlashAttribute("categoryForm", new AddCategoryForm());
        return "redirect:/category";
    }

    // Hiển thị form sửa category
    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCategoryAjax(
            @RequestParam("id") Long id,
            @RequestParam("name") String name) {

        Map<String, Object> response = new HashMap<>();

        try {
            AddCategoryForm form = new AddCategoryForm();
            form.setName(name);

            categoryService.updateCategory(id, form);

            response.put("success", true);
            response.put("message", "Category updated successfully!");
            response.put("id", id);
            response.put("name", name);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message",
                    e.getMessage() != null ? e.getMessage()
                            : "An unexpected error occurred while updating the category.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Xóa category
    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        categoryService.deleteCategory(id);
        redirectAttributes.addFlashAttribute("message", "Xóa category thành công!");
        return "redirect:/category";
    }
}
