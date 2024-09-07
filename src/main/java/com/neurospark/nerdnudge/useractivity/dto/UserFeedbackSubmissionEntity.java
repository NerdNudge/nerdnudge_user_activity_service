package com.neurospark.nerdnudge.useractivity.dto;

import lombok.Data;

@Data
public class UserFeedbackSubmissionEntity {
    String userId;
    String feedbackType;
    String feedback;
}
