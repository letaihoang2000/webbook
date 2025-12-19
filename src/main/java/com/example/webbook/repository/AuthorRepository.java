package com.example.webbook.repository;

import com.example.webbook.dto.AuthorInfo;
import com.example.webbook.model.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthorRepository extends JpaRepository<Author, UUID> {
    Optional<Author> findByName(String name);

    Page<Author> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Custom query for AuthorInfo DTO
    @Query("SELECT new com.example.webbook.dto.AuthorInfo(" +
            "CAST(a.id AS string), " +
            "a.name, " +
            "a.image, " +
            "a.description, " +
            "SIZE(a.books)) " +
            "FROM Author a")
    Page<AuthorInfo> findAllAuthorsInfo(Pageable pageable);

    @Query("SELECT new com.example.webbook.dto.AuthorInfo(" +
            "CAST(a.id AS string), " +
            "a.name, " +
            "a.image, " +
            "a.description, " +
            "SIZE(a.books)) " +
            "FROM Author a " +
            "WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<AuthorInfo> findAllAuthorsInfoBySearch(@Param("search") String search, Pageable pageable);

    @Query("SELECT new com.example.webbook.dto.AuthorInfo(" +
            "CAST(a.id AS string), " +
            "a.name, " +
            "a.image, " +
            "a.description, " +
            "SIZE(a.books)) " +
            "FROM Author a " +
            "WHERE a.id = :id")
    Optional<AuthorInfo> findAuthorInfoById(@Param("id") UUID id);

    // Count books for an author
    @Query("SELECT COUNT(b) FROM Book b WHERE b.author.id = :authorId")
    long countBooksByAuthorId(@Param("authorId") UUID authorId);
}