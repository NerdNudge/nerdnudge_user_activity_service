package com.neurospark.nerdnudge.useractivity.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.neurospark.nerdnudge.useractivity.dto.UserQuizFlexSubmissionEntity;
import com.neurospark.nerdnudge.useractivity.utils.Commons;

import java.util.Map;

public class RealworldChallengeService {
    public void updateRealworldChallenge(JsonObject userData, UserQuizFlexSubmissionEntity userQuizFlexSubmissionEntity, UserActivityCounts counts) {
        if(! userQuizFlexSubmissionEntity.isRWC())
            return;

        JsonElement rwcEle = userData.get("rwc");
        JsonObject rwcObject = (rwcEle == null || rwcEle.isJsonNull()) ? new JsonObject() : rwcEle.getAsJsonObject();
        userData.add("rwc", rwcObject);

        Map<String, int[]> topicsCorrectCounts = counts.getTopicsCorrectnessCounts();
        for (String topic : topicsCorrectCounts.keySet()) {
            if (!UserActivityServiceImpl.topicNameToTopicCodeMapping.has(topic))
                continue;

            String topicCode = UserActivityServiceImpl.topicNameToTopicCodeMapping.get(topic).getAsString();
            JsonElement thisTopicRwcUserDataEle = rwcObject.get(topicCode);
            JsonObject thisTopicRwcUserDataObject = (thisTopicRwcUserDataEle == null || thisTopicRwcUserDataEle.isJsonNull()) ? new JsonObject() : thisTopicRwcUserDataEle.getAsJsonObject();
            rwcObject.add(topicCode, thisTopicRwcUserDataObject);

            int[] thisTopicCurrentCounts = topicsCorrectCounts.get(topic);
            JsonArray thisTopicArray = new JsonArray();
            thisTopicArray.add(new JsonPrimitive(thisTopicCurrentCounts[0]));
            thisTopicArray.add(new JsonPrimitive(thisTopicCurrentCounts[1]));

            thisTopicRwcUserDataObject.add(Commons.getInstance().getDaystamp(), thisTopicArray);
            Commons.getInstance().housekeepDayJsonObject(thisTopicRwcUserDataObject, 15);
        }
    }
}
