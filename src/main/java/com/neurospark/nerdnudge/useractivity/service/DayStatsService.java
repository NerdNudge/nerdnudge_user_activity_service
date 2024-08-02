package com.neurospark.nerdnudge.useractivity.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.neurospark.nerdnudge.useractivity.utils.Commons;

public class DayStatsService {
    public void updateDayStats(JsonObject userData, UserActivityCounts counts) {
        JsonElement dayStatsEle = userData.get("dayStats");
        JsonObject dayStatsObject = (dayStatsEle == null || dayStatsEle.isJsonNull()) ? getDefaultDayStatsObject() : dayStatsEle.getAsJsonObject();
        int currentDay = Integer.parseInt(Commons.getInstance().getDaystamp());

        if(dayStatsObject.get("day").getAsInt() == currentDay) {
            dayStatsObject.add("current", new JsonPrimitive(dayStatsObject.get("current").getAsInt() + counts.getCurrentTotalCount()));
            dayStatsObject.add("currentCorrect", new JsonPrimitive(dayStatsObject.get("currentCorrect").getAsInt() + counts.getCurrentTotalCorrect()));
        }
        else {
            dayStatsObject.add("current", new JsonPrimitive(counts.getCurrentTotalCount()));
            dayStatsObject.add("currentCorrect", new JsonPrimitive(counts.getCurrentTotalCorrect()));
            dayStatsObject.add("day", new JsonPrimitive(currentDay));
        }

        if(dayStatsObject.get("current").getAsInt() > dayStatsObject.get("highest").getAsInt())
            dayStatsObject.add("highest", new JsonPrimitive(dayStatsObject.get("current").getAsInt()));

        if(dayStatsObject.get("currentCorrect").getAsInt() > dayStatsObject.get("highestCorrect").getAsInt())
            dayStatsObject.add("highestCorrect", new JsonPrimitive(dayStatsObject.get("currentCorrect").getAsInt()));

        userData.add("dayStats", dayStatsObject);
    }

    private JsonObject getDefaultDayStatsObject() {
        JsonObject dayStatsObject = new JsonObject();
        dayStatsObject.addProperty("highest", 0);
        dayStatsObject.addProperty("highestCorrect", 0);
        dayStatsObject.addProperty("day", 0);
        dayStatsObject.addProperty("current", 0);
        dayStatsObject.addProperty("currentCorrect", 0);

        return dayStatsObject;
    }
}
