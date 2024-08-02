package com.neurospark.nerdnudge.useractivity.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.neurospark.nerdnudge.useractivity.dto.UserQuizFlexSubmissionEntity;
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
        JsonElement dayQuotaEle = userData.get("dayQuota");
        JsonObject dayQuotaObject = (dayQuotaEle == null || dayQuotaEle.isJsonNull()) ? new JsonObject() : dayQuotaEle.getAsJsonObject();

        JsonElement currentDayQuotaEle = dayQuotaObject.get(Commons.getInstance().getDaystamp());
        JsonArray currentDayArray = (currentDayQuotaEle == null || currentDayQuotaEle.isJsonNull()) ? new JsonArray() : currentDayQuotaEle.getAsJsonArray();

        if (currentDayArray.size() == 0) {
            currentDayArray.add(0);
            currentDayArray.add(0);
        }

        currentDayArray.set(0, new JsonPrimitive(currentDayArray.get(0).getAsInt() + currentQuizflexQuotaUsed));
        dayQuotaObject.add(Commons.getInstance().getDaystamp(), currentDayArray);
        Commons.getInstance().housekeepDayJsonObject(dayQuotaObject, quotaRetentionDays);
        userData.add("dayQuota", dayQuotaObject);
    }
}
