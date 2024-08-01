package com.neurospark.nerdnudge.useractivity.controller;

import com.neurospark.nerdnudge.useractivity.dto.UserQuizFlexSubmissionEntity;
import com.neurospark.nerdnudge.useractivity.dto.UserShotsSubmissionEntity;
import com.neurospark.nerdnudge.useractivity.response.ApiResponse;
import com.neurospark.nerdnudge.useractivity.service.UserActivityService;
import com.neurospark.nerdnudge.useractivity.utils.Constants;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.http.HttpRequest;

@RestController
@RequestMapping("/api/nerdnudge/useractivity")
public class UserActivityController {

    @Autowired
    UserActivityService userActivityService;

    @PutMapping("/quizflexSubmission")
    public ApiResponse<String> updateUserQuizflexSubmission(@RequestBody UserQuizFlexSubmissionEntity userQuizFlexSubmissionEntity) {
        long startTime = System.currentTimeMillis();
        userActivityService.updateUserQuizflexSubmission(userQuizFlexSubmissionEntity);
        long endTime = System.currentTimeMillis();
        return new ApiResponse<>(Constants.SUCCESS, "User activity updated successfully", Constants.SUCCESS, (endTime - startTime));
    }

    @PutMapping("/shotsSubmission")
    public ApiResponse<String> updateUserShotsSubmission(@RequestBody UserShotsSubmissionEntity userShotsSubmissionEntity) {
        long startTime = System.currentTimeMillis();
        userActivityService.updateUserShotsSubmission(userShotsSubmissionEntity);
        long endTime = System.currentTimeMillis();
        return new ApiResponse<>(Constants.SUCCESS, "User activity updated successfully", Constants.SUCCESS, (endTime - startTime));
    }
}
