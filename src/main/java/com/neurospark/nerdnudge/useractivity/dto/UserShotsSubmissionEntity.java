package com.neurospark.nerdnudge.useractivity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserShotsSubmissionEntity {
    String userId;
    long time;
    Map<String, Map<String, Integer>> shots;
    List<String> likes;
    List<String> dislikes;
    List<String> favorites;
    List<String> shares;
}
