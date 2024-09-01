package com.neurospark.nerdnudge.useractivity.service;

import com.neurospark.nerdnudge.couchbase.service.NerdPersistClient;

import java.util.Map;

public class PeerComparisonService {
    public void updatePeerComparisonData(UserActivityCounts counts, NerdPersistClient shotsStatsPersist) {
        Map<String, Map<String, int[]>> topicDifficultyCounts = counts.getTopicDifficultyCounts();
        int totalAttempts = 0;
        int totalCorrect = 0;
        for(String topic: topicDifficultyCounts.keySet()) {
            int topicTotalAttempts = 0;
            int topicTotalCorrect = 0;
            String topicCode = UserActivityServiceImpl.topicNameToTopicCodeMapping.get(topic).getAsString();

            Map<String, int[]> currentDifficultyCounts = topicDifficultyCounts.get(topic);
            for(String difficulty: currentDifficultyCounts.keySet()) {
                int[] difficultyCounts = currentDifficultyCounts.get(difficulty);
                shotsStatsPersist.incr(topicCode + "_" + difficulty + "_attempts", difficultyCounts[0]);
                shotsStatsPersist.incr(topicCode + "_" + difficulty + "_correct", difficultyCounts[1]);

                shotsStatsPersist.incr("global_" + difficulty + "_attempts", difficultyCounts[0]);
                shotsStatsPersist.incr("global_" + difficulty + "_correct", difficultyCounts[1]);

                totalAttempts += difficultyCounts[0];
                totalCorrect += difficultyCounts[1];

                topicTotalAttempts += difficultyCounts[0];
                topicTotalCorrect += difficultyCounts[1];
            }

            shotsStatsPersist.incr(topicCode + "_attempts", topicTotalAttempts);
            shotsStatsPersist.incr(topicCode + "_correct", topicTotalCorrect);
        }

        shotsStatsPersist.incr("global_attempts", totalAttempts);
        shotsStatsPersist.incr("global_correct", totalCorrect);
    }
}
