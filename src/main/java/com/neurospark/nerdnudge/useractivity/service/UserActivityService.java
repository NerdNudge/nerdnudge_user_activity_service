package com.neurospark.nerdnudge.useractivity.service;

import com.neurospark.nerdnudge.useractivity.dto.UserFavoritesSubmissionEntity;
import com.neurospark.nerdnudge.useractivity.dto.UserQuizFlexSubmissionEntity;
import com.neurospark.nerdnudge.useractivity.dto.UserShotsSubmissionEntity;

public interface UserActivityService {
    public void updateUserQuizflexSubmission(UserQuizFlexSubmissionEntity userQuizFlexSubmissionEntity);

    public void updateUserShotsSubmission(UserShotsSubmissionEntity userShotsSubmissionEntity);

    public void updateUserFavoritesSubmission(UserFavoritesSubmissionEntity userFavoritesSubmissionEntity);
}
