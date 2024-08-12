package com.neurospark.nerdnudge.useractivity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserQuizFlexSubmissionEntity {
    String userId;
    long time;
    Map<String, Map<String, List<String>>> quizflex;
    List<String> likes;
    List<String> dislikes;
    Map<String, Map<String, List<String>>> favorites;
    List<String> shares;
}
