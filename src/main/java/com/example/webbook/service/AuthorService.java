package com.example.webbook.service;

import com.example.webbook.model.Author;
import com.example.webbook.repository.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthorService {
    @Autowired
    private AuthorRepository authorRepository;

    // Get all authors
    public List<Author> getAllAuthors() {
        return authorRepository.findAll(Sort.by("name").ascending());
    }

    // Get author by ID
    public Optional<Author> getAuthorById(UUID id) {
        return authorRepository.findById(id);
    }

    // Get author by name
    public Optional<Author> getAuthorByName(String name) {
        return authorRepository.findByName(name);
    }
}
