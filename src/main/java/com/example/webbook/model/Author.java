package com.example.webbook.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "authors")
@Getter
@Setter
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "CHAR(36)")
    private UUID id;

    private String name;
    private String description;

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private List<Book> authors = new ArrayList<>();
}
