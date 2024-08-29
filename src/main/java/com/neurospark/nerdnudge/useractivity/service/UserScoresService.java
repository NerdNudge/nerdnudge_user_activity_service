package com.neurospark.nerdnudge.useractivity.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.neurospark.nerdnudge.couchbase.service.NerdPersistClient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class UserScoresService {

    private static final String GLOBAL = "global";
    private static JsonObject topicNameToTopicCodeMapping = null;
    private NerdPersistClient configPersist;

    public UserScoresService(NerdPersistClient configPersist) {
        this.configPersist = configPersist;
        if(topicNameToTopicCodeMapping == null)
            updateTopicCodeMap();
    }

    private void updateTopicCodeMap() {
        System.out.println("Updating topic code map.");
        JsonObject topicCodeToTopicNameMapping = configPersist.get("collection_topic_mapping");
        topicNameToTopicCodeMapping = new JsonObject();
        Iterator<Map.Entry<String, JsonElement>> topicsIterator = topicCodeToTopicNameMapping.entrySet().iterator();
        while(topicsIterator.hasNext()) {
            Map.Entry<String, JsonElement> thisEntry = topicsIterator.next();
            topicNameToTopicCodeMapping.addProperty(thisEntry.getValue().getAsString(), thisEntry.getKey());
        }

        System.out.println(topicNameToTopicCodeMapping);
    }

    public void updateUserScores(JsonObject userData, UserActivityCounts counts) {
        JsonElement scoresEle = userData.get("scores");
        JsonObject scoresObject = (scoresEle == null || scoresEle.isJsonNull()) ? new JsonObject() : scoresEle.getAsJsonObject();
        userData.add("scores", scoresObject);

        Map<String, Integer> topicScores = counts.getTopicScores();
        for(String topic: topicScores.keySet()) {
            double existingTopicScore = scoresObject.has(topic) ? scoresObject.get(topic).getAsDouble() : 0.0;
            scoresObject.addProperty(topicNameToTopicCodeMapping.get(topic).getAsString(), existingTopicScore + topicScores.get(topic));

            double existingGlobalScore = scoresObject.has(GLOBAL) ? scoresObject.get(GLOBAL).getAsDouble() : 0.0;
            scoresObject.addProperty(GLOBAL, existingGlobalScore + topicScores.get(topic));
        }
    }
}
