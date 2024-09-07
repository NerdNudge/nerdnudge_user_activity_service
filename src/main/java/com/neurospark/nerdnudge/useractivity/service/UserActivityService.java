package com.neurospark.nerdnudge.useractivity.service;

import com.neurospark.nerdnudge.useractivity.dto.*;

public interface UserActivityService {
    public void updateUserQuizflexSubmission(UserQuizFlexSubmissionEntity userQuizFlexSubmissionEntity);

    public void updateUserShotsSubmission(UserShotsSubmissionEntity userShotsSubmissionEntity);

    public void updateUserFavoritesSubmission(UserFavoritesSubmissionEntity userFavoritesSubmissionEntity);

    public void updateUserFavoriteQuoteSubmission(UserFavoriteQuoteEntity userFavoriteQuoteEntity);

    public void updateUserFeedbackSubmission(UserFeedbackSubmissionEntity userFeedbackSubmissionEntity);

    public void deleteUserAccount(String userId);
}
