package com.neurospark.nerdnudge.useractivity.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.neurospark.nerdnudge.useractivity.utils.Commons;

import java.util.Map;

public class UserSummaryService {
    public void updateUserSummary(JsonObject userData, UserActivityCounts counts) {
        updateOverallUserSummary(userData, counts);
        updateLast30DaysUserSummary(userData, counts);
        updateWeeklySummary(userData, counts);
    }

    private void updateWeeklySummary(JsonObject userData, UserActivityCounts counts) {
        JsonElement summaryEle = userData.get("Summary");
        JsonObject summaryObject = (summaryEle == null || summaryEle.isJsonNull()) ? new JsonObject() : summaryEle.getAsJsonObject();

        JsonElement weekSummaryEle = summaryObject.get("weekly");
        JsonObject weekSummaryObject = (weekSummaryEle == null || weekSummaryEle.isJsonNull()) ? new JsonObject() : weekSummaryEle.getAsJsonObject();
        summaryObject.add("weekly", weekSummaryObject);

        String currentWeek = Commons.getInstance().getWeekstamp();
        JsonElement weekArrayEle = weekSummaryObject.get(currentWeek);
        JsonArray weekArray = (weekArrayEle == null || weekArrayEle.isJsonNull()) ? new JsonArray() : weekArrayEle.getAsJsonArray();
        if (weekArray.size() > 0) {
            weekArray.set(0, new JsonPrimitive(weekArray.get(0).getAsInt() + counts.getCurrentTotalCount()));
        } else {
            weekArray.add(counts.getCurrentTotalCount());
        }
        weekSummaryObject.add(currentWeek, weekArray);
    }

    private void updateOverallUserSummary(JsonObject userData, UserActivityCounts counts) {
        JsonElement summaryEle = userData.get("Summary");
        JsonObject summaryObject = (summaryEle == null || summaryEle.isJsonNull()) ? new JsonObject() : summaryEle.getAsJsonObject();

        JsonElement overallSummaryEle = summaryObject.get("overallSummary");
        JsonObject overallSummaryObject = (overallSummaryEle == null || overallSummaryEle.isJsonNull()) ? new JsonObject() : overallSummaryEle.getAsJsonObject();

        JsonArray totalArray = Commons.getInstance().getUpdatedArray(overallSummaryObject.get("total"), counts.getCurrentTotalCount(), counts.getCurrentTotalCorrect());
        overallSummaryObject.add("total", totalArray);

        summaryObject.add("overallSummary", overallSummaryObject);
        userData.add("Summary", summaryObject);
    }

    private void updateLast30DaysUserSummary(JsonObject userData, UserActivityCounts counts) {
        JsonElement summaryEle = userData.get("Summary");
        JsonObject summaryObject = (summaryEle == null || summaryEle.isJsonNull()) ? new JsonObject() : summaryEle.getAsJsonObject();

        JsonElement last30DaysEle = summaryObject.get("last30Days");
        JsonObject last30DaysObject = (last30DaysEle == null || last30DaysEle.isJsonNull()) ? new JsonObject() : last30DaysEle.getAsJsonObject();

        String currentDayStamp = Commons.getInstance().getDaystamp();
        JsonElement currentDayEle = last30DaysObject.get(currentDayStamp);
        JsonObject currentDayObject = (currentDayEle == null || currentDayEle.isJsonNull()) ? new JsonObject() : currentDayEle.getAsJsonObject();
        currentDayObject = getUpdatedDay(currentDayObject, counts);

        last30DaysObject.add(currentDayStamp, currentDayObject);
        Commons.getInstance().housekeepJsonObjectEntries(last30DaysObject, 30);

        summaryObject.add("last30Days", last30DaysObject);
        userData.add("Summary", summaryObject);
    }

    public JsonObject getUpdatedDay(JsonObject currentDayObject, UserActivityCounts counts) {
        int totalCountForToday = (currentDayObject.has("total")) ? currentDayObject.get("total").getAsInt() : 0;
        int totalCorrectCountForToday = (currentDayObject.has("totalCorrect")) ? currentDayObject.get("totalCorrect").getAsInt() : 0;

        currentDayObject.addProperty("total", totalCountForToday + counts.getCurrentTotalCount());
        currentDayObject.addProperty("totalCorrect", totalCorrectCountForToday + counts.getCurrentTotalCorrect());

        return currentDayObject;
    }
}
