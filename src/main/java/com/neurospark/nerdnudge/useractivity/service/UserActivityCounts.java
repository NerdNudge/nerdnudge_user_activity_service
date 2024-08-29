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

    private Map<String, Map<String, int[]>> subtopicsCounts = new HashMap<>();
    private Map<String, int[]> topicsSummaryCounts = new HashMap<>();
    private Map<String, int[]> topicsCorrectnessCounts = new HashMap<>();
    private Map<String, Integer> topicScores = new HashMap<>();

    UserActivityCounts(UserQuizFlexSubmissionEntity userQuizFlexSubmissionEntity) {
        System.out.println("Updating counts from user activity now.");
        Map<String, Map<String, List<String>>> currentQuizflexes = userQuizFlexSubmissionEntity.getQuizflex();
        if(currentQuizflexes == null)
            return;

        for (String thisTopic : currentQuizflexes.keySet()) {
            Map<String, List<String>> allQuizflexes = currentQuizflexes.get(thisTopic);
            int[] topicSummaryCounts = getTopicsSummaryCounts().getOrDefault(thisTopic, new int[3]);
            int[] topicCorrectnessCountsArray = getTopicsCorrectnessCounts().getOrDefault(thisTopic, new int[2]);
            int currentTopicScore = getTopicScores().getOrDefault(thisTopic, 0);
            for (String questionId : allQuizflexes.keySet()) {
                List<String> thisQuizflex = allQuizflexes.get(questionId);
                currentTotalCount++;
                Map<String, int[]> thisTopicsSubtopic = getSubtopicsCounts().getOrDefault(thisTopic, new HashMap<>());
                int[] subtopicsCounts = thisTopicsSubtopic.getOrDefault(thisQuizflex.get(0), new int[2]);
                subtopicsCounts[0]++;
                subtopicsCounts[1] = thisQuizflex.get(2).equals("Y") ? subtopicsCounts[1] + 1 : subtopicsCounts[1];
                topicCorrectnessCountsArray[0] ++;
                topicCorrectnessCountsArray[1] = thisQuizflex.get(2).equals("Y") ? topicCorrectnessCountsArray[1] + 1 : topicCorrectnessCountsArray[1];
                if (thisQuizflex.get(1).equals("easy")) {
                    topicSummaryCounts[0] ++;
                    currentEasyCount++;
                    currentEasyCorrect = thisQuizflex.get(2).equals("Y") ? currentEasyCorrect + 1 : currentEasyCorrect;
                    currentTopicScore += getCurrentQuizflexScore(thisQuizflex.get(2).equals("Y"), "easy");
                } else if (thisQuizflex.get(1).equals("medium")) {
                    topicSummaryCounts[1] ++;
                    currentMedCount++;
                    currentMedCorrect = thisQuizflex.get(2).equals("Y") ? currentMedCorrect + 1 : currentMedCorrect;
                    currentTopicScore += getCurrentQuizflexScore(thisQuizflex.get(2).equals("Y"), "medium");
                } else {
                    topicSummaryCounts[2] ++;
                    currentHardCount ++;
                    currentHardCorrect = thisQuizflex.get(2).equals("Y") ? currentHardCorrect + 1 : currentHardCorrect;
                    currentTopicScore += getCurrentQuizflexScore(thisQuizflex.get(2).equals("Y"), "hard");
                }

                currentTotalCorrect = thisQuizflex.get(2).equals("Y") ? currentTotalCorrect + 1 : currentTotalCorrect;
                thisTopicsSubtopic.put(thisQuizflex.get(0), subtopicsCounts);
                getSubtopicsCounts().put(thisTopic, thisTopicsSubtopic);
                getTopicScores().put(thisTopic, currentTopicScore);
            }
            getTopicsCorrectnessCounts().put(thisTopic, topicCorrectnessCountsArray);
            getTopicsSummaryCounts().put(thisTopic, topicSummaryCounts);
        }
        System.out.println("Counts: ------------ " + this);
    }

    private double getCurrentQuizflexScore(boolean isCorrect, String difficulty) {
        return isCorrect ? getCorrectScore(difficulty) : getIncorrectScore(difficulty);
    }

    private double getCorrectScore(String difficulty) {
        switch(difficulty) {
            case "easy":
                return 1.0;
            case "medium":
                return 1.2;
            case "hard":
                return 1.4;
        }
        return 0;
    }

    private double getIncorrectScore(String difficulty) {
        switch(difficulty) {
            case "easy":
                return 0.6;
            case "medium":
                return 0.4;
            case "hard":
                return 0.2;
        }
        return 0;
    }
}
