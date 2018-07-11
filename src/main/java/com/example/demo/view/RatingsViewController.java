package com.example.demo.view;

import com.example.demo.repository.RatingsRepository;
import com.example.demo.repository.TalksRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/view/ratings")
public class RatingsViewController {

    @Autowired
    TalksRepository talksRepository;

    @Autowired
    RatingsRepository ratingsRepository;

    @GetMapping("/")
    String index() {
        return "index";
    }


}
