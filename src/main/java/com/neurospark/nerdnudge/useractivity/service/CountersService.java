package com.neurospark.nerdnudge.useractivity.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.neurospark.nerdnudge.couchbase.service.NerdPersistClient;
import com.neurospark.nerdnudge.useractivity.dto.UserQuizFlexSubmissionEntity;
import com.neurospark.nerdnudge.useractivity.dto.UserShotsSubmissionEntity;
import com.neurospark.nerdnudge.useractivity.utils.Commons;

import java.util.List;
import java.util.Map;

public class CountersService {

    private NerdPersistClient shotsStatsPersist;
    private static final String LIKES_SUFFIX = "-Likes";
    private static final String DISLIKES_SUFFIX = "-Dislikes";
    private static final String FAVS_SUFFIX = "-Favs";
    private static final String SHARES_SUFFIX = "-Shares";

    public void updateCounters(JsonObject userData, UserQuizFlexSubmissionEntity userQuizFlexSubmissionEntity, NerdPersistClient shotsStatsPersist) {
        this.shotsStatsPersist = shotsStatsPersist;
        System.out.println("updating stats now for flexes..");
        updateCounter(userQuizFlexSubmissionEntity.getLikes(), LIKES_SUFFIX);
        updateCounter(userQuizFlexSubmissionEntity.getDislikes(), DISLIKES_SUFFIX);
        updateCounter(userQuizFlexSubmissionEntity.getShares(), SHARES_SUFFIX);
        updateFavorites(userData, userQuizFlexSubmissionEntity.getFavorites());
    }

    public void updateCounters(JsonObject userData, UserShotsSubmissionEntity userShotsSubmissionEntity, NerdPersistClient shotsStatsPersist) {
        this.shotsStatsPersist = shotsStatsPersist;
        System.out.println("updating stats now for shots..");
        updateCounter(userShotsSubmissionEntity.getLikes(), LIKES_SUFFIX);
        updateCounter(userShotsSubmissionEntity.getDislikes(), DISLIKES_SUFFIX);
        updateCounter(userShotsSubmissionEntity.getShares(), SHARES_SUFFIX);
        updateFavorites(userData, userShotsSubmissionEntity.getFavorites());
    }

    private void updateCounter(List<String> counterArray, String suffix) {
        if(counterArray == null)
            return;

        for(String currentShot : counterArray) {
            shotsStatsPersist.incr(currentShot + suffix, 1);
        }
    }

    private void updateFavorites(JsonObject userData, Map<String, Map<String, List<String>>> favorites) {
        if(favorites == null)
            return;

        JsonElement favoritesEle = userData.get("favorites");
        JsonObject favoritesObject = (favoritesEle == null || favoritesEle.isJsonNull()) ? new JsonObject() : favoritesEle.getAsJsonObject();
        userData.add("favorites", favoritesObject);

        JsonElement recentEle = favoritesObject.get("recent");
        JsonArray recentArray = (recentEle == null || recentEle.isJsonNull()) ? new JsonArray() : recentEle.getAsJsonArray();
        favoritesObject.add("recent", recentArray);

        JsonElement topicWiseEle = favoritesObject.get("topicwise");
        JsonObject topicWiseUserDataObject = (topicWiseEle == null || topicWiseEle.isJsonNull()) ? new JsonObject() : topicWiseEle.getAsJsonObject();
        favoritesObject.add("topicwise", topicWiseUserDataObject);

        for(String topic: favorites.keySet()) {
            Map<String, List<String>> subtopicFavs = favorites.get(topic);

            JsonElement thisTopicUserDataEle = topicWiseUserDataObject.get(topic);
            JsonObject thisTopicUserDataObject = (thisTopicUserDataEle == null || thisTopicUserDataEle.isJsonNull()) ? new JsonObject() : thisTopicUserDataEle.getAsJsonObject();
            topicWiseUserDataObject.add(topic, thisTopicUserDataObject);

            for(String subtopic: subtopicFavs.keySet()) {
                JsonElement thisSubtopicUserDataEle = thisTopicUserDataObject.get(subtopic);
                JsonArray thisSubtopicUserDataArray = (thisSubtopicUserDataEle == null || thisSubtopicUserDataEle.isJsonNull()) ? new JsonArray() : thisSubtopicUserDataEle.getAsJsonArray();
                thisTopicUserDataObject.add(subtopic, thisSubtopicUserDataArray);

                List<String> quizFlexIds = subtopicFavs.get(subtopic);
                for(int i = 0; i < quizFlexIds.size(); i ++) {
                    String currentQuizFlexId = quizFlexIds.get(i);
                    shotsStatsPersist.incr(currentQuizFlexId + FAVS_SUFFIX, 1);

                    if(! arrayContains(recentArray, currentQuizFlexId)) {
                        recentArray.add(currentQuizFlexId);
                    }

                    if(! arrayContains(thisSubtopicUserDataArray, currentQuizFlexId)) {
                        thisSubtopicUserDataArray.add(currentQuizFlexId);
                    }
                }
                Commons.getInstance().housekeepJsonArray(thisSubtopicUserDataArray, 30);
            }
        }

        Commons.getInstance().housekeepJsonArray(recentArray, 30);
    }

    private boolean arrayContains(JsonArray array, String value) {
        for (JsonElement element : array) {
            if (element.getAsString().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
