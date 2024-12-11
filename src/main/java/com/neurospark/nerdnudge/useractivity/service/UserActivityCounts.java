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
    private int currentEasyCount = 0;
    private int currentEasyCorrect = 0;
    private int currentMedCount = 0;
    private int currentMedCorrect = 0;
    private int currentHardCount = 0;
    private int currentHardCorrect = 0;

    private Map<String, Map<String, int[]>> subtopicsCounts = new HashMap<>();
    private Map<String, int[]> topicsSummaryCounts = new HashMap<>();
    private Map<String, int[]> topicsCorrectnessCounts = new HashMap<>();
    private Map<String, Double> topicScores = new HashMap<>();
    private Map<String, Map<String, int[]>> topicDifficultyCounts = new HashMap<>();

    UserActivityCounts(UserQuizFlexSubmissionEntity userQuizFlexSubmissionEntity) {
        log.info("Updating counts from user activity: {}", userQuizFlexSubmissionEntity.getUserId());
        Map<String, Map<String, List<String>>> currentQuizflexes = userQuizFlexSubmissionEntity.getQuizflex();
        if(currentQuizflexes == null)
            return;

        for (String thisTopic : currentQuizflexes.keySet()) {
            Map<String, List<String>> allQuizflexes = currentQuizflexes.get(thisTopic);
            int[] topicSummaryCounts = getTopicsSummaryCounts().getOrDefault(thisTopic, new int[3]);
            int[] topicCorrectnessCountsArray = getTopicsCorrectnessCounts().getOrDefault(thisTopic, new int[2]);
            double currentTopicScore = getTopicScores().getOrDefault(thisTopic, 0.0);
            for (String questionId : allQuizflexes.keySet()) {
                List<String> thisQuizflex = allQuizflexes.get(questionId);
                currentTotalCount++;
                Map<String, int[]> thisTopicsSubtopic = getSubtopicsCounts().getOrDefault(thisTopic, new HashMap<>());
                int[] subtopicsCounts = thisTopicsSubtopic.getOrDefault(thisQuizflex.get(0), new int[2]);
                subtopicsCounts[0]++;
                subtopicsCounts[1] = thisQuizflex.get(2).equals("Y") ? subtopicsCounts[1] + 1 : subtopicsCounts[1];
                topicCorrectnessCountsArray[0] ++;
                topicCorrectnessCountsArray[1] = thisQuizflex.get(2).equals("Y") ? topicCorrectnessCountsArray[1] + 1 : topicCorrectnessCountsArray[1];
                updateTopicDifficultyCounts(thisTopic, thisQuizflex);

                if (thisQuizflex.get(1).equalsIgnoreCase("easy")) {
                    topicSummaryCounts[0] ++;
                    currentEasyCount++;
                    currentEasyCorrect = thisQuizflex.get(2).equals("Y") ? currentEasyCorrect + 1 : currentEasyCorrect;
                    double currentQFScore = getCurrentQuizflexScore(thisQuizflex.get(2).equals("Y"), "easy");
                    currentTopicScore += currentQFScore;
                    log.info("Current QF Score returned for easy: {}, updated score: {}", currentQFScore, currentTopicScore);
                } else if (thisQuizflex.get(1).equalsIgnoreCase("medium")) {
                    topicSummaryCounts[1] ++;
                    currentMedCount++;
                    currentMedCorrect = thisQuizflex.get(2).equals("Y") ? currentMedCorrect + 1 : currentMedCorrect;
                    double currentQFScore = getCurrentQuizflexScore(thisQuizflex.get(2).equals("Y"), "medium");
                    currentTopicScore += currentQFScore;
                    log.info("Current QF Score returned for med: {}, updated score: {}", currentQFScore, currentTopicScore);
                } else {
                    topicSummaryCounts[2] ++;
                    currentHardCount ++;
                    currentHardCorrect = thisQuizflex.get(2).equals("Y") ? currentHardCorrect + 1 : currentHardCorrect;
                    double currentQFScore = getCurrentQuizflexScore(thisQuizflex.get(2).equals("Y"), "hard");
                    currentTopicScore += currentQFScore;
                    log.info("Current QF Score returned for hard: {}, updated score: {}", currentQFScore, currentTopicScore);
                }

                currentTotalCorrect = thisQuizflex.get(2).equals("Y") ? currentTotalCorrect + 1 : currentTotalCorrect;
                thisTopicsSubtopic.put(thisQuizflex.get(0), subtopicsCounts);
                getSubtopicsCounts().put(thisTopic, thisTopicsSubtopic);
                getTopicScores().put(thisTopic, currentTopicScore);
            }
            getTopicsCorrectnessCounts().put(thisTopic, topicCorrectnessCountsArray);
            getTopicsSummaryCounts().put(thisTopic, topicSummaryCounts);
        }
        log.info("Updated counts: {}", this);
    }

    private void updateTopicDifficultyCounts(String thisTopic, List<String> thisQuizflex) {
        Map<String, int[]> currentTopicDifficultyCounts = getTopicDifficultyCounts().getOrDefault(thisTopic, new HashMap<>());
        int[] currentDifficultyCounts = currentTopicDifficultyCounts.getOrDefault(thisQuizflex.get(1), new int[2]);
        currentDifficultyCounts[0] ++;
        currentDifficultyCounts[1] = thisQuizflex.get(2).equals("Y") ? currentDifficultyCounts[1] + 1 : currentDifficultyCounts[1];

        currentTopicDifficultyCounts.put(thisQuizflex.get(1), currentDifficultyCounts);
        getTopicDifficultyCounts().put(thisTopic, currentTopicDifficultyCounts);
    }

    private double getCurrentQuizflexScore(boolean isCorrect, String difficulty) {
        return isCorrect ? getCorrectScore(difficulty) : -1 * getIncorrectScore(difficulty);
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
