package com.nhnacademy.ailibraryteam1.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class BookController {

    @GetMapping("/books/{id}")
    public String bookDetail(@PathVariable Long id,
                             Model model) {

        model.addAttribute("bookId", id);

        return "book-detail";
    }
}