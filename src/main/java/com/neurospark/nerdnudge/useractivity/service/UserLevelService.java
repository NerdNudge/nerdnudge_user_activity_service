package com.neurospark.nerdnudge.useractivity.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.neurospark.nerdnudge.useractivity.dto.UserQuizFlexSubmissionEntity;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.Map;

@Slf4j
public class UserLevelService {
    public void updateUserLevel(JsonObject userData, UserQuizFlexSubmissionEntity userQuizFlexSubmissionEntity, UserActivityCounts counts) {
        if(!userQuizFlexSubmissionEntity.isLevelCompleted())
            return;

        JsonElement topicwiseEle = userData.get("topicwise");
        JsonObject topicwiseObject = (topicwiseEle == null || topicwiseEle.isJsonNull()) ? new JsonObject() : topicwiseEle.getAsJsonObject();
        userData.add("topicwise", topicwiseObject);

        JsonElement topicwiseOverallEle = topicwiseObject.get("overall");
        JsonObject topicwiseOverallObject = (topicwiseOverallEle == null || topicwiseOverallEle.isJsonNull()) ? new JsonObject() : topicwiseOverallEle.getAsJsonObject();
        topicwiseObject.add("overall", topicwiseOverallObject);

        String topic = getTopic(counts);
        JsonElement topicEle = topicwiseOverallObject.get(UserActivityServiceImpl.topicNameToTopicCodeMapping.get(topic).getAsString());
        JsonObject topicObject = (topicEle == null || topicEle.isJsonNull()) ? new JsonObject() : topicEle.getAsJsonObject();
        topicwiseOverallObject.add(topic, topicObject);

        int currentTopicLevel = topicObject.has("level") ? topicObject.get("level").getAsInt() : 0;
        topicObject.addProperty("level", currentTopicLevel + 1);

        JsonElement subtopicLevelsEle = topicObject.get("subtopicLevels");
        JsonObject subtopicLevelsObject = (subtopicLevelsEle == null || subtopicLevelsEle.isJsonNull()) ? new JsonObject() : subtopicLevelsEle.getAsJsonObject();
        topicObject.add("subtopicLevels", subtopicLevelsObject);

        String levelsUpdateField = (userQuizFlexSubmissionEntity.isRandom()) ? "random" : getSubtopicToUpdate(counts, topic);
        String currentSubtopicLevel = (subtopicLevelsObject.has(levelsUpdateField)) ? subtopicLevelsObject.get(levelsUpdateField).getAsString() : "Novice";
        String nextLevel = getNextLevel(currentSubtopicLevel);
        log.info("Field to update the level: {}: {}", levelsUpdateField, nextLevel);
        subtopicLevelsObject.addProperty(levelsUpdateField, nextLevel);
    }

    private String getTopic(UserActivityCounts counts) {
        Map<String, int[]> topicsCorrectCounts = counts.getTopicsCorrectnessCounts();
        for (String topic : topicsCorrectCounts.keySet()) {
            return topic;
        }
        return null;
    }

    private String getNextLevel(String level) {
        switch (level) {
            case "Novice": return "Beginner";
            case "Beginner": return "Intermediate";
            case "Intermediate": return "Advanced";
            case "Advanced": return "Pro";
            default: return "Pro ++";
        }
    }

    private String getSubtopicToUpdate(UserActivityCounts counts, String topic) {
        Map<String, Map<String, int[]>> subtopicCounts = counts.getSubtopicsCounts();
        Map<String, int[]> currentTopicSubtopics = subtopicCounts.get(topic);
        Iterator<Map.Entry<String, int[]>> subtopicsIterator = currentTopicSubtopics.entrySet().iterator();
        log.info("topic name to topic mapping: {}", UserActivityServiceImpl.topicNameToTopicCodeMapping);
        while(subtopicsIterator.hasNext()) {
            String subtopic = subtopicsIterator.next().getKey();
            log.info("Subtopic: {}", subtopic);
            return subtopic;

        }
        return null;
    }
}
