package com.nhnacademy.ailibraryteam1.book.controller;

import com.nhnacademy.ailibraryteam1.book.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Controller
@RequestMapping
public class BookController {
    private final BookService bookService;
}
