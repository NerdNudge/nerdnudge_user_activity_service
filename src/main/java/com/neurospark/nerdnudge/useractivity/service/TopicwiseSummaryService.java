package com.neurospark.nerdnudge.useractivity.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.neurospark.nerdnudge.useractivity.utils.Commons;

import java.util.Map;

public class TopicwiseSummaryService {
    public void updateTopicwiseSummary(JsonObject userData, UserActivityCounts counts) {
        updateTopicwiseOverallSummary(userData, counts);
        updateTopicwiseLast30DaysSummary(userData, counts);
    }

    private void updateTopicwiseOverallSummary(JsonObject userData, UserActivityCounts counts) {
        try {
            System.out.println("Updating topicwise summary now.");
            JsonElement topicwiseEle = userData.get("topicwise");
            JsonObject topicwiseObject = (topicwiseEle == null || topicwiseEle.isJsonNull()) ? new JsonObject() : topicwiseEle.getAsJsonObject();

            JsonElement topicwiseOverallEle = topicwiseObject.get("overall");
            JsonObject topicwiseOverallObject = (topicwiseOverallEle == null || topicwiseOverallEle.isJsonNull()) ? new JsonObject() : topicwiseOverallEle.getAsJsonObject();
            topicwiseObject.add("overall", topicwiseOverallObject);

            Map<String, int[]> topicSummaryCounts = counts.getTopicsSummaryCounts();
            for (String topic : topicSummaryCounts.keySet()) {
                JsonElement thisTopicDetailsFromUserDataEle = topicwiseOverallObject.get(topic);
                JsonObject thisTopicDetailsFromUserDataObject = (thisTopicDetailsFromUserDataEle == null || thisTopicDetailsFromUserDataEle.isJsonNull()) ? new JsonObject() : thisTopicDetailsFromUserDataEle.getAsJsonObject();
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
            }
            userData.add("topicwise", topicwiseObject);
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateTopicwiseLast30DaysSummary(JsonObject userData, UserActivityCounts counts) {

    }
}
