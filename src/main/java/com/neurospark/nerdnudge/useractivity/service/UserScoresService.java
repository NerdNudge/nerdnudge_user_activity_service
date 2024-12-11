package com.neurospark.nerdnudge.useractivity.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class UserScoresService {

    private static final String GLOBAL = "global";

    public void updateUserScores(JsonObject userData, UserActivityCounts counts) {
        JsonElement scoresEle = userData.get("scores");
        JsonObject scoresObject = (scoresEle == null || scoresEle.isJsonNull()) ? new JsonObject() : scoresEle.getAsJsonObject();
        userData.add("scores", scoresObject);
        log.info("Scores Object: {}", scoresObject);

        Map<String, Double> topicScores = counts.getTopicScores();
        for(String topic: topicScores.keySet()) {
            String topicCode = UserActivityServiceImpl.topicNameToTopicCodeMapping.get(topic).getAsString();
            double existingTopicScore = scoresObject.has(topicCode) ? scoresObject.get(topicCode).getAsDouble() : 0.0;
            scoresObject.addProperty(topicCode, existingTopicScore + topicScores.get(topic));
            log.info("Current topic: {},, topic code: {}, existing topic score: {}, updated score: {}", topic, topicCode, existingTopicScore, existingTopicScore + topicScores.get(topic));

            double existingGlobalScore = scoresObject.has(GLOBAL) ? scoresObject.get(GLOBAL).getAsDouble() : 0.0;
            double topicScore = topicScores.get(topic);
            existingGlobalScore = Double.parseDouble(String.format("%.2f", existingGlobalScore));
            topicScore = Double.parseDouble(String.format("%.2f", topicScore));
            scoresObject.addProperty(GLOBAL, existingGlobalScore + topicScore);
        }
    }
}
