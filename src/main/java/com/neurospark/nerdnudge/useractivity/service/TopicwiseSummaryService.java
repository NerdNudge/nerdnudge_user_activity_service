package com.neurospark.nerdnudge.useractivity.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.neurospark.nerdnudge.couchbase.service.NerdPersistClient;
import com.neurospark.nerdnudge.useractivity.utils.Commons;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class TopicwiseSummaryService {

    public void updateTopicwiseSummary(JsonObject userData, UserActivityCounts counts, NerdPersistClient shotsStatsPersist) {
        JsonElement topicwiseEle = userData.get("topicwise");
        JsonObject topicwiseObject = (topicwiseEle == null || topicwiseEle.isJsonNull()) ? new JsonObject() : topicwiseEle.getAsJsonObject();
        userData.add("topicwise", topicwiseObject);

        updateTopicwiseOverallSummary(topicwiseObject, counts, shotsStatsPersist);
        updateTopicwiseLast30DaysSummary(topicwiseObject, counts);
        updateTopicWiseWeeklySummary(topicwiseObject, counts);
    }

    private void updateTopicWiseWeeklySummary(JsonObject topicwiseObject, UserActivityCounts counts) {
        try {
            JsonElement topicwiseWeeklyEle = topicwiseObject.get("weekly");
            JsonObject topicwiseWeeklyObject = (topicwiseWeeklyEle == null || topicwiseWeeklyEle.isJsonNull()) ? new JsonObject() : topicwiseWeeklyEle.getAsJsonObject();
            topicwiseObject.add("weekly", topicwiseWeeklyObject);

            // Current week stamp like 202538
            String currentWeek = Commons.getInstance().getWeekstamp();

            Map<String, int[]> topicsCorrectCounts = counts.getTopicsCorrectnessCounts();
            for (String topic : topicsCorrectCounts.keySet()) {
                if (!UserActivityServiceImpl.topicNameToTopicCodeMapping.has(topic))
                    continue;

                String topicCode = UserActivityServiceImpl.topicNameToTopicCodeMapping.get(topic).getAsString();

                // Topic object inside "weekly"
                JsonElement topicWeeklyEle = topicwiseWeeklyObject.get(topicCode);
                JsonObject topicWeeklyObj = (topicWeeklyEle == null || topicWeeklyEle.isJsonNull()) ? new JsonObject() : topicWeeklyEle.getAsJsonObject();
                topicwiseWeeklyObject.add(topicCode, topicWeeklyObj);

                // Current counts for this topic
                int[] thisTopicCounts = topicsCorrectCounts.get(topic);

                JsonElement weekArrayEle = topicWeeklyObj.get(currentWeek);
                JsonArray weekArray = (weekArrayEle == null || weekArrayEle.isJsonNull()) ? new JsonArray() : weekArrayEle.getAsJsonArray();

                if (weekArray.size() > 0) {
                    // Update existing week entry
                    weekArray.set(0, new JsonPrimitive(weekArray.get(0).getAsInt() + thisTopicCounts[0]));
                    weekArray.set(1, new JsonPrimitive(weekArray.get(1).getAsInt() + thisTopicCounts[1]));
                } else {
                    // Create new week entry
                    weekArray.add(new JsonPrimitive(thisTopicCounts[0]));
                    weekArray.add(new JsonPrimitive(thisTopicCounts[1]));
                }

                topicWeeklyObj.add(currentWeek, weekArray);

                // Optional: housekeeping to limit to last 24 weeks
                Commons.getInstance().housekeepJsonObjectEntries(topicWeeklyObj, 30);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("Issue updating topicwise weekly summary: {}", ex.getMessage());
        }
    }

    private void updateTopicwiseOverallSummary(JsonObject topicwiseObject, UserActivityCounts counts, NerdPersistClient shotsStatsPersist) {
        try {
            JsonElement topicwiseOverallEle = topicwiseObject.get("overall");
            JsonObject topicwiseOverallObject = (topicwiseOverallEle == null || topicwiseOverallEle.isJsonNull()) ? new JsonObject() : topicwiseOverallEle.getAsJsonObject();
            topicwiseObject.add("overall", topicwiseOverallObject);

            //Map<String, int[]> topicSummaryCounts = counts.getTopicsSummaryCounts();
            Map<String, int[]> topicsCorrectCounts = counts.getTopicsCorrectnessCounts();
            for (String topic : topicsCorrectCounts.keySet()) {
                if(! UserActivityServiceImpl.topicNameToTopicCodeMapping.has(topic))
                    continue;

                String topicCode = UserActivityServiceImpl.topicNameToTopicCodeMapping.get(topic).getAsString();
                JsonElement thisTopicDetailsFromUserDataEle = topicwiseOverallObject.get(topicCode);
                JsonObject thisTopicDetailsFromUserDataObject;
                if(thisTopicDetailsFromUserDataEle == null || thisTopicDetailsFromUserDataEle.isJsonNull()) {
                    thisTopicDetailsFromUserDataObject = new JsonObject();
                    shotsStatsPersist.incr(topicCode + "_user_count", 1);
                }
                else {
                    thisTopicDetailsFromUserDataObject = thisTopicDetailsFromUserDataEle.getAsJsonObject();
                }

                topicwiseOverallObject.add(topicCode, thisTopicDetailsFromUserDataObject);
                JsonElement subtopicsEle = thisTopicDetailsFromUserDataObject.get("subtopics");
                JsonObject subtopicsObject = (subtopicsEle == null || subtopicsEle.isJsonNull()) ? new JsonObject() : subtopicsEle.getAsJsonObject();
                thisTopicDetailsFromUserDataObject.add("subtopics", subtopicsObject);

                Map<String, int[]> subtopicCounts = counts.getSubtopicsCounts().get(topic);
                for (String subtopic : subtopicCounts.keySet()) {
                    int[] thisSubtopicCounts = subtopicCounts.get(subtopic);
                    JsonElement thisSubtopicEle = subtopicsObject.get(subtopic);
                    JsonArray thisSubtopicUpdatedArray = Commons.getInstance().getUpdatedArray(thisSubtopicEle, thisSubtopicCounts[0], thisSubtopicCounts[1]);

                    subtopicsObject.add(subtopic, thisSubtopicUpdatedArray);
                }

                //Correct counts for topics:
                JsonElement topicsCorrectArrayEle = thisTopicDetailsFromUserDataObject.get("correct");
                JsonArray topicsCorrectArray = (topicsCorrectArrayEle == null || topicsCorrectArrayEle.isJsonNull()) ? new JsonArray() : topicsCorrectArrayEle.getAsJsonArray();
                thisTopicDetailsFromUserDataObject.add("correct", topicsCorrectArray);

                int[] thisTopicCurrentCounts = topicsCorrectCounts.get(topic);
                if(topicsCorrectArray.size() > 0) {
                    topicsCorrectArray.set(0, new JsonPrimitive(topicsCorrectArray.get(0).getAsInt() + thisTopicCurrentCounts[0]));
                    topicsCorrectArray.set(1, new JsonPrimitive(topicsCorrectArray.get(1).getAsInt() + thisTopicCurrentCounts[1]));
                }
                else {
                    topicsCorrectArray.add(new JsonPrimitive(thisTopicCurrentCounts[0]));
                    topicsCorrectArray.add(new JsonPrimitive(thisTopicCurrentCounts[1]));
                }

                thisTopicDetailsFromUserDataObject.addProperty("lastTaken", System.currentTimeMillis());
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
            log.error("Issue updating topicwise overall summary: {}", ex.getMessage());
        }
    }

    private void updateTopicwiseLast30DaysSummary(JsonObject topicwiseObject, UserActivityCounts counts) {
        JsonElement topicwiseLast30DaysEle = topicwiseObject.get("last30Days");
        JsonObject topicwiseLast30DaysObject = (topicwiseLast30DaysEle == null || topicwiseLast30DaysEle.isJsonNull()) ? new JsonObject() : topicwiseLast30DaysEle.getAsJsonObject();
        topicwiseObject.add("last30Days", topicwiseLast30DaysObject);

        Map<String, int[]> topicsCorrectCounts = counts.getTopicsCorrectnessCounts();
        //Map<String, int[]> topicSummaryCounts = counts.getTopicsSummaryCounts();
        for (String topic : topicsCorrectCounts.keySet()) {
            String topicCode = UserActivityServiceImpl.topicNameToTopicCodeMapping.get(topic).getAsString();
            JsonElement thisTopicDetailsFromUserDataEle = topicwiseLast30DaysObject.get(topicCode);
            JsonObject thisTopicDetailsFromUserDataObject = (thisTopicDetailsFromUserDataEle == null || thisTopicDetailsFromUserDataEle.isJsonNull()) ? new JsonObject() : thisTopicDetailsFromUserDataEle.getAsJsonObject();
            topicwiseLast30DaysObject.add(topicCode, thisTopicDetailsFromUserDataObject);

            updateLast30DaysSummary(thisTopicDetailsFromUserDataObject, topic, topicsCorrectCounts);
        }
    }

    private void updateLast30DaysSummary(JsonObject thisTopicDetailsFromUserDataObject, String topic, Map<String, int[]> topicsCorrectCounts) {
        JsonElement summaryEle = thisTopicDetailsFromUserDataObject.get("summary");
        JsonObject summaryObject = (summaryEle == null || summaryEle.isJsonNull()) ? new JsonObject() : summaryEle.getAsJsonObject();
        thisTopicDetailsFromUserDataObject.add("summary", summaryObject);

        String currentDay = Commons.getInstance().getDaystamp();
        JsonElement currentDayEle = summaryObject.get(currentDay);
        JsonArray currentDayArray = (currentDayEle == null || currentDayEle.isJsonNull()) ? new JsonArray() : currentDayEle.getAsJsonArray();
        summaryObject.add(currentDay, currentDayArray);

        int[] currentCounts = topicsCorrectCounts.get(topic);
        if (currentDayArray.size() > 0) {
            currentDayArray.set(0, new JsonPrimitive(currentDayArray.get(0).getAsInt() + currentCounts[0]));
            currentDayArray.set(1, new JsonPrimitive(currentDayArray.get(1).getAsInt() + currentCounts[1]));
        } else {
            currentDayArray.add(new JsonPrimitive(currentCounts[0]));
            currentDayArray.add(new JsonPrimitive(currentCounts[1]));
        }
        Commons.getInstance().housekeepJsonObjectEntries(summaryObject, 30);
    }
}
