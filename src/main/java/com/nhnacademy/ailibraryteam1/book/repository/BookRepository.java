package com.nhnacademy.ailibraryteam1.book.repository;

import com.nhnacademy.ailibraryteam1.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}
