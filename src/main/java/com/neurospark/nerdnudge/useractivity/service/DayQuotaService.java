package com.neurospark.nerdnudge.useractivity.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.neurospark.nerdnudge.useractivity.dto.UserQuizFlexSubmissionEntity;
import com.neurospark.nerdnudge.useractivity.dto.UserShotsSubmissionEntity;
import com.neurospark.nerdnudge.useractivity.utils.Commons;

import java.util.List;
import java.util.Map;

public class DayQuotaService {
    public void updateDayQuota(JsonObject userData, UserQuizFlexSubmissionEntity userQuizFlexSubmissionEntity, int quotaRetentionDays) {
        Map<String, Map<String, List<String>>> currentQuizflexes = userQuizFlexSubmissionEntity.getQuizflex();
        int currentQuizflexQuotaUsed = 0;
        for(String thisTopic: currentQuizflexes.keySet()) {
            Map<String, List<String>> allQuizflexes = currentQuizflexes.get(thisTopic);
            currentQuizflexQuotaUsed += allQuizflexes.size();
        }

        JsonObject dayQuotaObject = getDayQuotaObject(userData);
        JsonArray currentDayArray = getCurrentDayArray(dayQuotaObject);
        currentDayArray.set(0, new JsonPrimitive(currentDayArray.get(0).getAsInt() + currentQuizflexQuotaUsed));

        Commons.getInstance().housekeepDayJsonObject(dayQuotaObject, quotaRetentionDays);
        userData.add("dayQuota", dayQuotaObject);
    }

    private JsonObject getDayQuotaObject(JsonObject userData) {
        JsonElement dayQuotaEle = userData.get("dayQuota");
        JsonObject dayQuotaObject = (dayQuotaEle == null || dayQuotaEle.isJsonNull()) ? new JsonObject() : dayQuotaEle.getAsJsonObject();
        userData.add("dayQuota", dayQuotaObject);
        return dayQuotaObject;
    }

    private JsonArray getCurrentDayArray(JsonObject dayQuotaObject) {
        String currentDay = Commons.getInstance().getDaystamp();
        JsonElement currentDayQuotaEle = dayQuotaObject.get(currentDay);
        JsonArray currentDayArray = (currentDayQuotaEle == null || currentDayQuotaEle.isJsonNull()) ? new JsonArray() : currentDayQuotaEle.getAsJsonArray();
        dayQuotaObject.add(currentDay, currentDayArray);

        if (currentDayArray.size() == 0) {
            currentDayArray.add(0);
            currentDayArray.add(0);
        }

        return currentDayArray;
    }

    public void updateDayQuota(JsonObject userData, UserShotsSubmissionEntity userShotsSubmissionEntity, int quotaRetentionDays) {
        Map<String, Map<String, Integer>> currentShots = userShotsSubmissionEntity.getShots();
        int currentShotsQuotaUsed = 0;
        for(String thisTopic: currentShots.keySet()) {
            Map<String, Integer> subtopicWiseShots = currentShots.get(thisTopic);
            for(String thisSubtopic: subtopicWiseShots.keySet()) {
                currentShotsQuotaUsed += subtopicWiseShots.get(thisSubtopic);
            }
        }
        JsonObject dayQuotaObject = getDayQuotaObject(userData);
        JsonArray currentDayArray = getCurrentDayArray(dayQuotaObject);
        currentDayArray.set(1, new JsonPrimitive(currentDayArray.get(1).getAsInt() + currentShotsQuotaUsed));

        Commons.getInstance().housekeepDayJsonObject(dayQuotaObject, quotaRetentionDays);
        userData.add("dayQuota", dayQuotaObject);
    }
}
