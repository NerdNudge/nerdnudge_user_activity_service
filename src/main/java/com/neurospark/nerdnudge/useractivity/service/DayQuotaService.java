package com.neurospark.nerdnudge.useractivity.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.neurospark.nerdnudge.couchbase.service.NerdPersistClient;
import com.neurospark.nerdnudge.useractivity.dto.UserQuizFlexSubmissionEntity;
import com.neurospark.nerdnudge.useractivity.dto.UserShotsSubmissionEntity;
import com.neurospark.nerdnudge.useractivity.utils.Commons;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;
import java.util.Map;

public class DayQuotaService {

    @Autowired
    private NerdPersistClient userProfilesPersist;

    private JsonObject nerdConfig;
    private static final long numSecsPerDay = 86400;

    public void updateDayQuota(JsonObject userData, UserQuizFlexSubmissionEntity userQuizFlexSubmissionEntity, JsonObject nerdConfig, NerdPersistClient userProfilesPersist) {
        this.userProfilesPersist = userProfilesPersist;
        this.nerdConfig = nerdConfig;
        int quotaRetentionDays = nerdConfig.get("dayQuotaRetentionDays").getAsInt();
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

        JsonArray currentDayArray;
        if(currentDayQuotaEle == null || currentDayQuotaEle.isJsonNull()) {
            currentDayArray = new JsonArray();
            currentDayArray.add(0);
            currentDayArray.add(0);

            dayQuotaObject.add(currentDay, currentDayArray);
            updateCurrentDayUserCount(currentDay);
        } else {
            currentDayArray = currentDayQuotaEle.getAsJsonArray();
        }

        return currentDayArray;
    }

    private void updateCurrentDayUserCount(String currentDay) {
        int numRetentionDays = nerdConfig.get("userCountsRetentionDays").getAsInt();
        userProfilesPersist.incr(currentDay + "_user_counts", 1, (int) (numRetentionDays * numSecsPerDay));
    }

    public void updateDayQuota(JsonObject userData, UserShotsSubmissionEntity userShotsSubmissionEntity, int quotaRetentionDays, NerdPersistClient userProfilesPersist) {
        this.userProfilesPersist = userProfilesPersist;
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
