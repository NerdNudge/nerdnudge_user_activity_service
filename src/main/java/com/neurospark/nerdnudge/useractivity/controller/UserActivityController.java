package com.neurospark.nerdnudge.useractivity.controller;

import com.neurospark.nerdnudge.metrics.metrics.Metric;
import com.neurospark.nerdnudge.useractivity.dto.*;
import com.neurospark.nerdnudge.useractivity.response.ApiResponse;
import com.neurospark.nerdnudge.useractivity.service.UserActivityService;
import com.neurospark.nerdnudge.useractivity.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/nerdnudge/useractivity")
public class UserActivityController {

    @Autowired
    UserActivityService userActivityService;

    @PutMapping("/quizflexSubmission")
    public ApiResponse<String> updateUserQuizflexSubmission(@RequestBody UserQuizFlexSubmissionEntity userQuizFlexSubmissionEntity) {
        long startTime = System.currentTimeMillis();
        log.info("Submit Quizflex for user: {}", userQuizFlexSubmissionEntity.getUserId());
        userActivityService.updateUserQuizflexSubmission(userQuizFlexSubmissionEntity);
        long endTime = System.currentTimeMillis();
        new Metric.MetricBuilder().setName("quizSubmit").setUnit(Metric.Unit.MILLISECONDS).setValue((endTime - startTime)).build();
        return new ApiResponse<>(Constants.SUCCESS, "User activity updated successfully", Constants.SUCCESS, (endTime - startTime));
    }

    @PutMapping("/shotsSubmission")
    public ApiResponse<String> updateUserShotsSubmission(@RequestBody UserShotsSubmissionEntity userShotsSubmissionEntity) {
        long startTime = System.currentTimeMillis();
        log.info("Submit Shots for user: {}", userShotsSubmissionEntity.getUserId());
        userActivityService.updateUserShotsSubmission(userShotsSubmissionEntity);
        long endTime = System.currentTimeMillis();
        new Metric.MetricBuilder().setName("shotsSubmit").setUnit(Metric.Unit.MILLISECONDS).setValue((endTime - startTime)).build();
        return new ApiResponse<>(Constants.SUCCESS, "User activity updated successfully", Constants.SUCCESS, (endTime - startTime));
    }

    @PutMapping("/favoritesSubmission")
    public ApiResponse<String> updateUserFavoritesSubmission(@RequestBody UserFavoritesSubmissionEntity userFavoritesSubmissionEntity) {
        long startTime = System.currentTimeMillis();
        log.info("Submit favorites for user: {}", userFavoritesSubmissionEntity.getUserId());
        userActivityService.updateUserFavoritesSubmission(userFavoritesSubmissionEntity);
        long endTime = System.currentTimeMillis();
        new Metric.MetricBuilder().setName("favSubmit").setUnit(Metric.Unit.MILLISECONDS).setValue((endTime - startTime)).build();
        return new ApiResponse<>(Constants.SUCCESS, "User activity updated successfully", Constants.SUCCESS, (endTime - startTime));
    }

    @PutMapping("/favoriteQuoteSubmission")
    public ApiResponse<String> updateUserFavoriteQuoteSubmission(@RequestBody UserFavoriteQuoteEntity userFavoriteQuoteEntity) {
        log.info("Submit favorite quote for user: {}", userFavoriteQuoteEntity.getUserId());
        long startTime = System.currentTimeMillis();
        userActivityService.updateUserFavoriteQuoteSubmission(userFavoriteQuoteEntity);
        long endTime = System.currentTimeMillis();
        new Metric.MetricBuilder().setName("favQuoteSubmit").setUnit(Metric.Unit.MILLISECONDS).setValue((endTime - startTime)).build();
        return new ApiResponse<>(Constants.SUCCESS, "User Favorite Quote updated successfully", Constants.SUCCESS, (endTime - startTime));
    }

    @PutMapping("/userFeedbackSubmission")
    public ApiResponse<String> updateUserFeedbackSubmission(@RequestBody UserFeedbackSubmissionEntity userFeedbackSubmissionEntity) {
        log.info("Submit feedback for user: {}", userFeedbackSubmissionEntity.getUserId());
        long startTime = System.currentTimeMillis();
        userActivityService.updateUserFeedbackSubmission(userFeedbackSubmissionEntity);
        long endTime = System.currentTimeMillis();
        new Metric.MetricBuilder().setName("feedbackSubmit").setUnit(Metric.Unit.MILLISECONDS).setValue((endTime - startTime)).build();
        return new ApiResponse<>(Constants.SUCCESS, "User Feedback updated successfully", Constants.SUCCESS, (endTime - startTime));
    }

    @PutMapping("/deleteUserAccount")
    public ApiResponse<String> deleteUserAccount(@RequestBody UserTerminationEntity userTerminationEntity) {
        log.info("Delete account for user: {}", userTerminationEntity.getEmail());
        long startTime = System.currentTimeMillis();
        userActivityService.deleteUserAccount(userTerminationEntity.getEmail());
        long endTime = System.currentTimeMillis();
        new Metric.MetricBuilder().setName("deleteAccount").setUnit(Metric.Unit.MILLISECONDS).setValue((endTime - startTime)).build();
        return new ApiResponse<>(Constants.SUCCESS, "User Account deleted successfully", Constants.SUCCESS, (endTime - startTime));
    }

    @GetMapping("/health")
    public ApiResponse<String> healthCheck() {
        return new ApiResponse<>(Constants.SUCCESS, "Health Check Pass", Constants.SUCCESS, 0);
    }
}
