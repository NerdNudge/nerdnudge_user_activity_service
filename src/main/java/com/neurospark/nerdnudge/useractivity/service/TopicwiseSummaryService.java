package com.neurospark.nerdnudge.useractivity.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.neurospark.nerdnudge.couchbase.service.NerdPersistClient;
import com.neurospark.nerdnudge.useractivity.utils.Commons;

import java.util.Map;

public class TopicwiseSummaryService {
    public void updateTopicwiseSummary(JsonObject userData, UserActivityCounts counts, NerdPersistClient shotsStatsPersist) {
        JsonElement topicwiseEle = userData.get("topicwise");
        JsonObject topicwiseObject = (topicwiseEle == null || topicwiseEle.isJsonNull()) ? new JsonObject() : topicwiseEle.getAsJsonObject();
        userData.add("topicwise", topicwiseObject);

        updateTopicwiseOverallSummary(topicwiseObject, counts, shotsStatsPersist);
        updateTopicwiseLast30DaysSummary(topicwiseObject, counts);
    }

    private void updateTopicwiseOverallSummary(JsonObject topicwiseObject, UserActivityCounts counts, NerdPersistClient shotsStatsPersist) {
        try {
            System.out.println("Updating topicwise summary now.");
            JsonElement topicwiseOverallEle = topicwiseObject.get("overall");
            JsonObject topicwiseOverallObject = (topicwiseOverallEle == null || topicwiseOverallEle.isJsonNull()) ? new JsonObject() : topicwiseOverallEle.getAsJsonObject();
            topicwiseObject.add("overall", topicwiseOverallObject);

            Map<String, int[]> topicSummaryCounts = counts.getTopicsSummaryCounts();
            Map<String, int[]> topicsCorrectCounts = counts.getTopicsCorrectnessCounts();
            for (String topic : topicSummaryCounts.keySet()) {
                JsonElement thisTopicDetailsFromUserDataEle = topicwiseOverallObject.get(topic);
                JsonObject thisTopicDetailsFromUserDataObject = null;
                if(thisTopicDetailsFromUserDataEle == null || thisTopicDetailsFromUserDataEle.isJsonNull()) {
                    thisTopicDetailsFromUserDataObject = new JsonObject();
                    shotsStatsPersist.incr(topic + "_user_count", 1);
                }
                else {
                    thisTopicDetailsFromUserDataObject = thisTopicDetailsFromUserDataEle.getAsJsonObject();
                }

                topicwiseOverallObject.add(topic, thisTopicDetailsFromUserDataObject);

                JsonElement summaryArrayEle = thisTopicDetailsFromUserDataObject.get("summary");
                JsonArray summaryArray = (summaryArrayEle == null || summaryArrayEle.isJsonNull()) ? new JsonArray() : summaryArrayEle.getAsJsonArray();
                thisTopicDetailsFromUserDataObject.add("summary", summaryArray);

                int[] currentCounts = topicSummaryCounts.get(topic);
                if (summaryArray.size() > 0) {
                    summaryArray.set(0, new JsonPrimitive(summaryArray.get(0).getAsInt() + currentCounts[0]));
                    summaryArray.set(1, new JsonPrimitive(summaryArray.get(1).getAsInt() + currentCounts[1]));
                    summaryArray.set(2, new JsonPrimitive(summaryArray.get(2).getAsInt() + currentCounts[2]));
                } else {
                    summaryArray.add(new JsonPrimitive(currentCounts[0]));
                    summaryArray.add(new JsonPrimitive(currentCounts[1]));
                    summaryArray.add(new JsonPrimitive(currentCounts[2]));
                }

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
        }
    }

    private void updateTopicwiseLast30DaysSummary(JsonObject topicwiseObject, UserActivityCounts counts) {
        System.out.println("Updating topicwise last 30 days now.");
        JsonElement topicwiseLast30DaysEle = topicwiseObject.get("last30Days");
        JsonObject topicwiseLast30DaysObject = (topicwiseLast30DaysEle == null || topicwiseLast30DaysEle.isJsonNull()) ? new JsonObject() : topicwiseLast30DaysEle.getAsJsonObject();
        topicwiseObject.add("last30Days", topicwiseLast30DaysObject);

        Map<String, int[]> topicSummaryCounts = counts.getTopicsSummaryCounts();
        for (String topic : topicSummaryCounts.keySet()) {
            JsonElement thisTopicDetailsFromUserDataEle = topicwiseLast30DaysObject.get(topic);
            JsonObject thisTopicDetailsFromUserDataObject = (thisTopicDetailsFromUserDataEle == null || thisTopicDetailsFromUserDataEle.isJsonNull()) ? new JsonObject() : thisTopicDetailsFromUserDataEle.getAsJsonObject();
            topicwiseLast30DaysObject.add(topic, thisTopicDetailsFromUserDataObject);

            updateLast30DaysSummary(thisTopicDetailsFromUserDataObject, topic, topicSummaryCounts);
            updateLast30DaysSubtopics(thisTopicDetailsFromUserDataObject, topic, counts.getSubtopicsCounts().get(topic));
        }
    }

    private void updateLast30DaysSubtopics(JsonObject thisTopicDetailsFromUserDataObject, String topic, Map<String, int[]> subtopicCounts) {
        JsonElement subtopicsEle = thisTopicDetailsFromUserDataObject.get("subtopics");
        JsonObject subtopicsObject = (subtopicsEle == null || subtopicsEle.isJsonNull()) ? new JsonObject() : subtopicsEle.getAsJsonObject();
        thisTopicDetailsFromUserDataObject.add("subtopics", subtopicsObject);

        String currentDay = Commons.getInstance().getDaystamp();
        for(String subtopic: subtopicCounts.keySet()) {
            JsonElement subtopicEle = subtopicsObject.get(subtopic);
            JsonObject subtopicObject = (subtopicEle == null || subtopicEle.isJsonNull()) ? new JsonObject() : subtopicEle.getAsJsonObject();
            subtopicsObject.add(subtopic, subtopicObject);

            JsonArray updatedArray = Commons.getInstance().getUpdatedArray(subtopicObject.get(currentDay), subtopicCounts.get(subtopic)[0], subtopicCounts.get(subtopic)[1]);
            subtopicObject.add(currentDay, updatedArray);

            Commons.getInstance().housekeepDayJsonObject(subtopicObject, 30);
        }
    }

    private void updateLast30DaysSummary(JsonObject thisTopicDetailsFromUserDataObject, String topic, Map<String, int[]> topicSummaryCounts) {
        JsonElement summaryEle = thisTopicDetailsFromUserDataObject.get("summary");
        JsonObject summaryObject = (summaryEle == null || summaryEle.isJsonNull()) ? new JsonObject() : summaryEle.getAsJsonObject();
        thisTopicDetailsFromUserDataObject.add("summary", summaryObject);

        String currentDay = Commons.getInstance().getDaystamp();
        JsonElement currentDayEle = summaryObject.get(currentDay);
        JsonArray currentDayArray = (currentDayEle == null || currentDayEle.isJsonNull()) ? new JsonArray() : currentDayEle.getAsJsonArray();
        summaryObject.add(currentDay, currentDayArray);

        int[] currentCounts = topicSummaryCounts.get(topic);
        if (currentDayArray.size() > 0) {
            currentDayArray.set(0, new JsonPrimitive(currentDayArray.get(0).getAsInt() + currentCounts[0]));
            currentDayArray.set(1, new JsonPrimitive(currentDayArray.get(1).getAsInt() + currentCounts[1]));
            currentDayArray.set(2, new JsonPrimitive(currentDayArray.get(2).getAsInt() + currentCounts[2]));
        } else {
            currentDayArray.add(new JsonPrimitive(currentCounts[0]));
            currentDayArray.add(new JsonPrimitive(currentCounts[1]));
            currentDayArray.add(new JsonPrimitive(currentCounts[2]));
        }
        Commons.getInstance().housekeepDayJsonObject(summaryObject, 30);
    }
}
