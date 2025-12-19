package com.example.webbook.service;

import com.example.webbook.dto.AddAuthorForm;
import com.example.webbook.dto.AuthorInfo;
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
    public Author updateAuthor(UUID id, AddAuthorForm form) throws IOException {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Author not found"));

        // Validate name
        if (form.getName() == null || form.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Author name is required");
        }

        // Check if name is being changed and if new name already exists
        if (!author.getName().equals(form.getName())) {
            if (authorRepository.findByName(form.getName()).isPresent()) {
                throw new IllegalArgumentException("Author with name '" + form.getName() + "' already exists");
            }

            // If name changed and has image, need to re-upload with new name
            if (form.getImage_file() == null && author.getImage() != null && !author.getImage().isEmpty()) {
                // Delete old image
                supabaseStorageService.deleteFile(author.getImage());

                // Note: Can't re-upload existing image automatically since we don't have the file
                // User should upload new image or leave it blank
                author.setImage(null);
            }

            author.setName(form.getName());
        }

        author.setDescription(form.getDescription());

        // Update image if new file provided
        if (form.getImage_file() != null && !form.getImage_file().isEmpty()) {
            // Delete old image if exists
            if (author.getImage() != null && !author.getImage().isEmpty()) {
                supabaseStorageService.deleteFile(author.getImage());
            }

            // Upload new image
            String imageUrl = supabaseStorageService.uploadAuthorImage(
                    form.getImage_file(),
                    form.getName()
            );
            author.setImage(imageUrl);
        }

        return authorRepository.save(author);
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
