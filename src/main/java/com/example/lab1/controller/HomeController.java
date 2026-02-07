package com.example.lab1.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index(Model model) {
        // Здесь можно изменить информацию о студенте
        model.addAttribute("studentName", "Введите ваше имя");
        model.addAttribute("studentGroup", "Введите вашу группу");
        return "index";
    }
}

