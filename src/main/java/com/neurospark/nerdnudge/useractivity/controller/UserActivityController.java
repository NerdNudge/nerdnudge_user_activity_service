package com.neurospark.nerdnudge.useractivity.controller;

import com.google.gson.JsonObject;
import com.neurospark.nerdnudge.useractivity.dto.*;
import com.neurospark.nerdnudge.useractivity.response.ApiResponse;
import com.neurospark.nerdnudge.useractivity.service.UserActivityService;
import com.neurospark.nerdnudge.useractivity.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/nerdnudge/useractivity")
public class UserActivityController {

    @Autowired
    UserActivityService userActivityService;

    @PutMapping("/quizflexSubmission")
    public ApiResponse<String> updateUserQuizflexSubmission(@RequestBody UserQuizFlexSubmissionEntity userQuizFlexSubmissionEntity) {
        long startTime = System.currentTimeMillis();
        System.out.println("Data in request: " + userQuizFlexSubmissionEntity);
        userActivityService.updateUserQuizflexSubmission(userQuizFlexSubmissionEntity);
        long endTime = System.currentTimeMillis();
        return new ApiResponse<>(Constants.SUCCESS, "User activity updated successfully", Constants.SUCCESS, (endTime - startTime));
    }

    @PutMapping("/shotsSubmission")
    public ApiResponse<String> updateUserShotsSubmission(@RequestBody UserShotsSubmissionEntity userShotsSubmissionEntity) {
        long startTime = System.currentTimeMillis();
        System.out.println("Request: " + userShotsSubmissionEntity);
        userActivityService.updateUserShotsSubmission(userShotsSubmissionEntity);
        long endTime = System.currentTimeMillis();
        return new ApiResponse<>(Constants.SUCCESS, "User activity updated successfully", Constants.SUCCESS, (endTime - startTime));
    }

    @PutMapping("/favoritesSubmission")
    public ApiResponse<String> updateUserFavoritesSubmission(@RequestBody UserFavoritesSubmissionEntity userFavoritesSubmissionEntity) {
        long startTime = System.currentTimeMillis();
        System.out.println("Request: " + userFavoritesSubmissionEntity);
        userActivityService.updateUserFavoritesSubmission(userFavoritesSubmissionEntity);
        long endTime = System.currentTimeMillis();
        return new ApiResponse<>(Constants.SUCCESS, "User activity updated successfully", Constants.SUCCESS, (endTime - startTime));
    }

    @PutMapping("/favoriteQuoteSubmission")
    public ApiResponse<String> updateUserFavoriteQuoteSubmission(@RequestBody UserFavoriteQuoteEntity userFavoriteQuoteEntity) {
        System.out.println("Adding user favorite quote for user: " + userFavoriteQuoteEntity);
        long startTime = System.currentTimeMillis();
        userActivityService.updateUserFavoriteQuoteSubmission(userFavoriteQuoteEntity);
        long endTime = System.currentTimeMillis();
        return new ApiResponse<>(Constants.SUCCESS, "User Favorite Quote updated successfully", Constants.SUCCESS, (endTime - startTime));
    }

    @PutMapping("/userFeedbackSubmission")
    public ApiResponse<String> updateUserFeedbackSubmission(@RequestBody UserFeedbackSubmissionEntity userFeedbackSubmissionEntity) {
        System.out.println("Adding user feedback for user: " + userFeedbackSubmissionEntity);
        long startTime = System.currentTimeMillis();
        userActivityService.updateUserFeedbackSubmission(userFeedbackSubmissionEntity);
        long endTime = System.currentTimeMillis();
        return new ApiResponse<>(Constants.SUCCESS, "User Feedback updated successfully", Constants.SUCCESS, (endTime - startTime));
    }

    @PutMapping("/deleteUserAccount")
    public ApiResponse<String> deleteUserAccount(@RequestBody UserTerminationEntity userTerminationEntity) {
        System.out.println("Deleting user account: " + userTerminationEntity);
        long startTime = System.currentTimeMillis();
        userActivityService.deleteUserAccount(userTerminationEntity.getEmail());
        long endTime = System.currentTimeMillis();
        return new ApiResponse<>(Constants.SUCCESS, "User Account deleted successfully", Constants.SUCCESS, (endTime - startTime));
    }

    @GetMapping("/health")
    public ApiResponse<String> healthCheck() {
        return new ApiResponse<>(Constants.SUCCESS, "Health Check Pass", Constants.SUCCESS, 0);
    }
}
