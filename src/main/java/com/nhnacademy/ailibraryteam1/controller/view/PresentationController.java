package com.nhnacademy.ailibraryteam1.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/presentation")
public class PresentationController {
    @GetMapping
    public String getPresentation() {
        return "redirect:/presentation/slide1.html";
    }
}
