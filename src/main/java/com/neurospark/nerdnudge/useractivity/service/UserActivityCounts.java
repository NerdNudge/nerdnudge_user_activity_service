package com.neurospark.nerdnudge.useractivity.service;

import com.neurospark.nerdnudge.useractivity.dto.UserQuizFlexSubmissionEntity;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class UserActivityCounts {
    private int currentTotalCount = 0;
    private int currentTotalCorrect = 0;
    private int currentEasyCount = 0;
    private int currentEasyCorrect = 0;
    private int currentMedCount = 0;
    private int currentMedCorrect = 0;
    private int currentHardCount = 0;
    private int currentHardCorrect = 0;

    Map<String, Map<String, int[]>> subtopicsCounts = new HashMap<>();
    Map<String, int[]> topicsSummaryCounts = new HashMap<>();

    UserActivityCounts(UserQuizFlexSubmissionEntity userQuizFlexSubmissionEntity) {
        System.out.println("Updating counts from user activity now.");
        Map<String, Map<String, List<String>>> currentQuizflexes = userQuizFlexSubmissionEntity.getQuizflex();
        for (String thisTopic : currentQuizflexes.keySet()) {
            Map<String, List<String>> allQuizflexes = currentQuizflexes.get(thisTopic);
            int[] topicSummaryCounts = getTopicsSummaryCounts().getOrDefault(thisTopic, new int[3]);
            for (String questionId : allQuizflexes.keySet()) {
                List<String> thisQuizflex = allQuizflexes.get(questionId);
                currentTotalCount++;
                Map<String, int[]> thisTopicsSubtopic = getSubtopicsCounts().getOrDefault(thisTopic, new HashMap<>());
                int[] subtopicsCounts = thisTopicsSubtopic.getOrDefault(thisQuizflex.get(0), new int[2]);
                subtopicsCounts[0]++;
                subtopicsCounts[1] = thisQuizflex.get(2).equals("Y") ? subtopicsCounts[1] + 1 : subtopicsCounts[1];
                if (thisQuizflex.get(1).equals("easy")) {
                    topicSummaryCounts[0]++;
                    currentEasyCount++;
                    currentEasyCorrect = thisQuizflex.get(2).equals("Y") ? currentEasyCorrect + 1 : currentEasyCorrect;
                } else if (thisQuizflex.get(1).equals("medium")) {
                    topicSummaryCounts[1]++;
                    currentMedCount++;
                    currentMedCorrect = thisQuizflex.get(2).equals("Y") ? currentMedCorrect + 1 : currentMedCorrect;
                } else {
                    topicSummaryCounts[2]++;
                    currentHardCount++;
                    currentHardCorrect = thisQuizflex.get(2).equals("Y") ? currentHardCorrect + 1 : currentHardCorrect;
                }

                currentTotalCorrect = thisQuizflex.get(2).equals("Y") ? currentTotalCorrect + 1 : currentTotalCorrect;
                thisTopicsSubtopic.put(thisQuizflex.get(0), subtopicsCounts);
                getSubtopicsCounts().put(thisTopic, thisTopicsSubtopic);
            }
            getTopicsSummaryCounts().put(thisTopic, topicSummaryCounts);
        }
        System.out.println("Counts: ------------ " + this);
    }
}
