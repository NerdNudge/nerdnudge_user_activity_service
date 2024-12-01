package com.neurospark.nerdnudge.useractivity.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.neurospark.nerdnudge.useractivity.utils.Commons;

public class UserSummaryService {
    public void updateUserSummary(JsonObject userData, UserActivityCounts counts) {
        updateOverallUserSummary(userData, counts);
        updateLast30DaysUserSummary(userData, counts);
    }

    private void updateOverallUserSummary(JsonObject userData, UserActivityCounts counts) {
        JsonElement summaryEle = userData.get("Summary");
        JsonObject summaryObject = (summaryEle == null || summaryEle.isJsonNull()) ? new JsonObject() : summaryEle.getAsJsonObject();

        JsonElement overallSummaryEle = summaryObject.get("overallSummary");
        JsonObject overallSummaryObject = (overallSummaryEle == null || overallSummaryEle.isJsonNull()) ? new JsonObject() : overallSummaryEle.getAsJsonObject();

        JsonArray totalArray = Commons.getInstance().getUpdatedArray(overallSummaryObject.get("total"), counts.getCurrentTotalCount(), counts.getCurrentTotalCorrect());
        JsonArray easyArray = Commons.getInstance().getUpdatedArray(overallSummaryObject.get("easy"), counts.getCurrentEasyCount(), counts.getCurrentEasyCorrect());
        JsonArray medArray = Commons.getInstance().getUpdatedArray(overallSummaryObject.get("medium"), counts.getCurrentMedCount(), counts.getCurrentMedCorrect());
        JsonArray hardArray = Commons.getInstance().getUpdatedArray(overallSummaryObject.get("hard"), counts.getCurrentHardCount(), counts.getCurrentHardCorrect());

        overallSummaryObject.add("total", totalArray);
        overallSummaryObject.add("easy", easyArray);
        overallSummaryObject.add("medium", medArray);
        overallSummaryObject.add("hard", hardArray);

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

        JsonArray easyArray = Commons.getInstance().getUpdatedArray(currentDayObject.get("easy"), counts.getCurrentEasyCount(), counts.getCurrentEasyCorrect());
        JsonArray medArray = Commons.getInstance().getUpdatedArray(currentDayObject.get("medium"), counts.getCurrentMedCount(), counts.getCurrentMedCorrect());
        JsonArray hardArray = Commons.getInstance().getUpdatedArray(currentDayObject.get("hard"), counts.getCurrentHardCount(), counts.getCurrentHardCorrect());

        currentDayObject.add("easy", easyArray);
        currentDayObject.add("medium", medArray);
        currentDayObject.add("hard", hardArray);

        last30DaysObject.add(currentDayStamp, currentDayObject);
        Commons.getInstance().housekeepDayJsonObject(last30DaysObject, 30);

        summaryObject.add("last30Days", last30DaysObject);
        userData.add("Summary", summaryObject);
    }
}
