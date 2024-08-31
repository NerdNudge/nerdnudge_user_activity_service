package com.neurospark.nerdnudge.useractivity.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;

public class UserScoresService {

    private static final String GLOBAL = "global";

    public void updateUserScores(JsonObject userData, UserActivityCounts counts) {
        JsonElement scoresEle = userData.get("scores");
        JsonObject scoresObject = (scoresEle == null || scoresEle.isJsonNull()) ? new JsonObject() : scoresEle.getAsJsonObject();
        userData.add("scores", scoresObject);

        Map<String, Integer> topicScores = counts.getTopicScores();
        for(String topic: topicScores.keySet()) {
            double existingTopicScore = scoresObject.has(topic) ? scoresObject.get(topic).getAsDouble() : 0.0;
            scoresObject.addProperty(UserActivityServiceImpl.topicNameToTopicCodeMapping.get(topic).getAsString(), existingTopicScore + topicScores.get(topic));

            double existingGlobalScore = scoresObject.has(GLOBAL) ? scoresObject.get(GLOBAL).getAsDouble() : 0.0;
            scoresObject.addProperty(GLOBAL, existingGlobalScore + topicScores.get(topic));
        }
    }
}
