package com.neurospark.nerdnudge.useractivity.service;

import com.neurospark.nerdnudge.useractivity.dto.UserQuizFlexSubmissionEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Data
public class UserActivityCounts {
    private int currentTotalCount = 0;
    private int currentTotalCorrect = 0;

    private Map<String, Map<String, int[]>> subtopicsCounts = new HashMap<>();
    private Map<String, int[]> topicsSummaryCounts = new HashMap<>();
    private Map<String, int[]> topicsCorrectnessCounts = new HashMap<>();

    UserActivityCounts(UserQuizFlexSubmissionEntity userQuizFlexSubmissionEntity) {
        log.info("Updating counts from user activity: {}", userQuizFlexSubmissionEntity.getUserId());
        Map<String, Map<String, List<String>>> currentQuizflexes = userQuizFlexSubmissionEntity.getQuizflex();
        if(currentQuizflexes == null)
            return;

        for (String thisTopic : currentQuizflexes.keySet()) {
            Map<String, List<String>> allQuizflexes = currentQuizflexes.get(thisTopic);
            int[] topicSummaryCounts = getTopicsSummaryCounts().getOrDefault(thisTopic, new int[3]);
            int[] topicCorrectnessCountsArray = getTopicsCorrectnessCounts().getOrDefault(thisTopic, new int[2]);
            for (String questionId : allQuizflexes.keySet()) {
                List<String> thisQuizflex = allQuizflexes.get(questionId);
                currentTotalCount++;
                Map<String, int[]> thisTopicsSubtopic = getSubtopicsCounts().getOrDefault(thisTopic, new HashMap<>());
                int[] subtopicsCounts = thisTopicsSubtopic.getOrDefault(thisQuizflex.get(0), new int[2]);
                subtopicsCounts[0]++;
                subtopicsCounts[1] = thisQuizflex.get(2).equals("Y") ? subtopicsCounts[1] + 1 : subtopicsCounts[1];
                topicCorrectnessCountsArray[0] ++;
                topicCorrectnessCountsArray[1] = thisQuizflex.get(2).equals("Y") ? topicCorrectnessCountsArray[1] + 1 : topicCorrectnessCountsArray[1];

                currentTotalCorrect = thisQuizflex.get(2).equals("Y") ? currentTotalCorrect + 1 : currentTotalCorrect;
                thisTopicsSubtopic.put(thisQuizflex.get(0), subtopicsCounts);
                getSubtopicsCounts().put(thisTopic, thisTopicsSubtopic);
            }
            getTopicsCorrectnessCounts().put(thisTopic, topicCorrectnessCountsArray);
            getTopicsSummaryCounts().put(thisTopic, topicSummaryCounts);
        }
        log.info("Updated counts: {}", this);
    }
}
