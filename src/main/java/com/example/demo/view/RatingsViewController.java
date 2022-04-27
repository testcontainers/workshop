package com.example.demo.view;

import com.example.demo.repository.RatingsRepository;
import com.example.demo.repository.TalksRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/view/ratings")
public class RatingsViewController {

    private final TalksRepository talksRepository;

    private final RatingsRepository ratingsRepository;

    public RatingsViewController(TalksRepository talksRepository, RatingsRepository ratingsRepository) {
        this.talksRepository = talksRepository;
        this.ratingsRepository = ratingsRepository;
    }

    @GetMapping("/")
    String index() {
        return "index";
    }


}
