package com.neurospark.nerdnudge.useractivity.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;

public class Commons {

    private static Commons instance;

    private Commons() {}

    public static Commons getInstance() {
        if (instance == null) {
            synchronized (Commons.class) {
                if (instance == null) {
                    instance = new Commons();
                }
            }
        }
        return instance;
    }

    public void housekeepJsonObjectEntries(JsonObject jsonObject, int retentionEntries) {
        Set<Map.Entry <String , JsonElement>> dailyQuotaKeys = jsonObject.entrySet();
        if(dailyQuotaKeys.size() <= retentionEntries)
            return;

        TreeSet<String> sortedKeys = new TreeSet<>();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            sortedKeys.add(entry.getKey());
        }

        while(jsonObject.entrySet().size() > retentionEntries) {
            String oldestKey = sortedKeys.pollFirst();
            if (oldestKey != null) {
                jsonObject.remove(oldestKey);
            }
        }
    }

    public void housekeepJsonArray(JsonArray jsonArray, int retentionEntries) {
        while(jsonArray.size() > retentionEntries) {
            jsonArray.remove(0);
        }
    }

    public JsonArray getUpdatedArray(JsonElement arrayElement, int totalCount, int correctCount) {
        JsonArray totalArray = (arrayElement == null || arrayElement.isJsonNull()) ? new JsonArray() : arrayElement.getAsJsonArray();
        JsonArray newArray = new JsonArray();

        if(totalArray.size() > 0) {
            newArray.add(new JsonPrimitive(totalArray.get(0).getAsInt() + totalCount));
            newArray.add(new JsonPrimitive(totalArray.get(1).getAsInt() + correctCount));
        } else {
            newArray.add(new JsonPrimitive(totalCount));
            newArray.add(new JsonPrimitive(correctCount));
        }

        return newArray;
    }

    public String getDaystamp() {
        LocalDate date = LocalDate.now();
        int dayOfYear = date.getDayOfYear();
        int year = date.getYear() % 100;
        String dayOfYearStr = String.format("%03d", dayOfYear);
        String yearStr = String.format("%02d", year);
        return dayOfYearStr + yearStr;
    }

    public String getWeekstamp() {
        LocalDate now = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int weekOfYear = now.get(weekFields.weekOfYear());
        int year = now.getYear();
        return String.format("%04d%02d", year, weekOfYear); // e.g. 202538
    }

    public String getMonthStamp() {
        LocalDate date = LocalDate.now();
        int month = date.getMonthValue();
        int year = date.getYear() % 100; // last two digits
        String monthStr = String.format("%02d", month);
        String yearStr = String.format("%02d", year);
        return monthStr + yearStr;
    }

    public boolean arrayContains(JsonArray array, String value) {
        for (JsonElement element : array) {
            if (element.getAsString().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
