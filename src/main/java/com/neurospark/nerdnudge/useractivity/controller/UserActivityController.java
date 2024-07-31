package com.neurospark.nerdnudge.useractivity.controller;

import com.neurospark.nerdnudge.useractivity.dto.UserQuizFlexSubmissionEntity;
import com.neurospark.nerdnudge.useractivity.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.http.HttpRequest;

@RestController
@RequestMapping("/api/nerdnudge/useractivity")
public class UserActivityController {

    @PutMapping("/quizflexSubmission")
    public ApiResponse<String> updateUserQuizflexSubmission(HttpServletRequest request,
                                                            @RequestBody UserQuizFlexSubmissionEntity userQuizFlexSubmissionEntity) {

        return null;
    }
}
