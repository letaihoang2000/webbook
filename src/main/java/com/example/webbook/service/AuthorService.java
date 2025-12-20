package com.example.webbook.service;

import com.example.webbook.dto.AddAuthorForm;
import com.example.webbook.dto.AuthorInfo;
import com.example.webbook.dto.UpdateAuthorForm;
import com.example.webbook.model.Author;
import com.example.webbook.repository.AuthorRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class AuthorService {
    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private SupabaseStorageService supabaseStorageService;

    // Get all authors (for dropdowns - returns entity list)
    public List<Author> getAllAuthors() {
        return authorRepository.findAll(Sort.by("name").ascending());
    }

    // Get authors info with pagination and search (returns DTO)
    public Page<AuthorInfo> getAuthorsInfoPaginated(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        if (search != null && !search.trim().isEmpty()) {
            return authorRepository.findAllAuthorsInfoBySearch(search, pageable);
        }
        return authorRepository.findAllAuthorsInfo(pageable);
    }

    // Get single author info by ID (returns DTO)
    public AuthorInfo getAuthorInfoById(UUID id) {
        return authorRepository.findAuthorInfoById(id)
                .orElseThrow(() -> new IllegalArgumentException("Author not found"));
    }

    // Get author entity by ID (for internal use)
    public Author getAuthorById(UUID id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Author not found"));
    }

    // Get author by name (for internal use)
    public Optional<Author> getAuthorByName(String name) {
        return authorRepository.findByName(name);
    }

    // Create author
    @Transactional
    public Author createAuthor(AddAuthorForm form) throws IOException {
        // Validate name
        if (form.getName() == null || form.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Author name is required");
        }

        // Check if author already exists
        if (authorRepository.findByName(form.getName()).isPresent()) {
            throw new IllegalArgumentException("Author with name '" + form.getName() + "' already exists");
        }

        Author author = new Author();
        author.setName(form.getName());
        author.setDescription(form.getDescription());

        // Upload image if provided
        if (form.getImage_file() != null && !form.getImage_file().isEmpty()) {
            String imageUrl = supabaseStorageService.uploadAuthorImage(
                    form.getImage_file(),
                    form.getName()
            );
            author.setImage(imageUrl);
        }

        return authorRepository.save(author);
    }

    // Update author
    @Transactional
    public Author updateAuthor(UUID id, UpdateAuthorForm form) throws IOException {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Author not found"));

        System.out.println("\n=== UPDATE AUTHOR OPERATION ===");
        System.out.println("Author ID: " + id);
        System.out.println("Old Name: " + author.getName());
        System.out.println("New Name: " + form.getName());

        String oldImageUrl = author.getImage();
        String oldName = author.getName();
        boolean nameChanged = !oldName.equals(form.getName());

        // Validate name
        if (form.getName() == null || form.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Author name is required");
        }

        // Check if name is being changed and if new name already exists
        if (nameChanged) {
            if (authorRepository.findByName(form.getName()).isPresent()) {
                throw new IllegalArgumentException("Author with name '" + form.getName() + "' already exists");
            }
        }

        // Update name
        author.setName(form.getName());
        author.setDescription(form.getDescription());

        // Handle image update
        if (form.getImage_file() != null && !form.getImage_file().isEmpty()) {
            System.out.println("--- IMAGE UPDATE: New image uploaded ---");

            // Delete old image FIRST if exists
            if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                System.out.println("Deleting old image...");
                supabaseStorageService.deleteFile(oldImageUrl);
                try { Thread.sleep(500); } catch (InterruptedException e) {}
            }

            // Upload new image
            System.out.println("Uploading new image...");
            String newImageUrl = supabaseStorageService.uploadAuthorImage(
                    form.getImage_file(),
                    form.getName()
            );
            author.setImage(newImageUrl);
            System.out.println("New Image URL: " + newImageUrl);

        } else if (nameChanged && oldImageUrl != null && !oldImageUrl.isEmpty()) {
            System.out.println("--- IMAGE UPDATE: Name changed, renaming image ---");
            try {
                // Extract extension from old filename
                String oldFilename = extractFilenameFromUrl(oldImageUrl);
                String extension = getFileExtension(oldFilename);

                // Download old image
                byte[] imageData = supabaseStorageService.downloadFile(oldImageUrl);

                if (imageData != null && imageData.length > 0) {
                    // Delete old file
                    supabaseStorageService.deleteFile(oldImageUrl);
                    try { Thread.sleep(500); } catch (InterruptedException e) {}

                    // Re-upload with new name
                    String contentType = "image/" + extension.replace(".", "");
                    String newImageUrl = supabaseStorageService.uploadAuthorImageFromBytes(
                            imageData,
                            form.getName(),
                            extension,
                            contentType
                    );
                    author.setImage(newImageUrl);
                    System.out.println("Image renamed successfully");
                }
            } catch (Exception e) {
                System.err.println("Failed to rename image: " + e.getMessage());
                // Keep old image URL if rename fails
            }
        }

        System.out.println("=== END UPDATE AUTHOR OPERATION ===\n");
        return authorRepository.save(author);
    }

    // Helper methods
    private String extractFilenameFromUrl(String url) {
        if (url == null || url.isEmpty()) return "";
        String[] parts = url.split("/");
        String encodedFilename = parts[parts.length - 1];
        return java.net.URLDecoder.decode(encodedFilename, StandardCharsets.UTF_8);
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".jpg";
        return filename.substring(filename.lastIndexOf("."));
    }

    // Delete author
    @Transactional
    public void deleteAuthor(UUID id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Author not found"));

        // Check if author has books using count query
        long bookCount = authorRepository.countBooksByAuthorId(id);

        if (bookCount > 0) {
            throw new IllegalArgumentException(
                    "Cannot delete author. This author has " + bookCount + " book(s) associated."
            );
        }

        // Delete image from Supabase
        if (author.getImage() != null && !author.getImage().isEmpty()) {
            supabaseStorageService.deleteFile(author.getImage());
        }

        authorRepository.delete(author);
    }

    // Get pagination info
    public Map<String, Object> getPaginationInfo(int page, int size, String search) {
        Page<AuthorInfo> authorPage = getAuthorsInfoPaginated(page, size, search);

        Map<String, Object> info = new HashMap<>();
        info.put("totalAuthors", authorPage.getTotalElements());
        info.put("totalPages", authorPage.getTotalPages());
        info.put("currentPage", page);
        info.put("pageSize", size);
        info.put("hasPrevious", authorPage.hasPrevious());
        info.put("hasNext", authorPage.hasNext());

        return info;
    }
}
