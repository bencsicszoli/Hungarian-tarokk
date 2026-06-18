package com.codecool.tarokkgame.controller.restcontroller;

import com.codecool.tarokkgame.model.dto.restdto.FeedbackDTO;
import com.codecool.tarokkgame.service.FeedbackService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> sendFeedback(@RequestBody FeedbackDTO request) {
        return feedbackService.sendFeedback(request);
    }
}
