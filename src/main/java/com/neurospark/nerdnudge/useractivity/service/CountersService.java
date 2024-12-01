package com.neurospark.nerdnudge.useractivity.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.neurospark.nerdnudge.couchbase.service.NerdPersistClient;
import com.neurospark.nerdnudge.useractivity.dto.UserFavoritesSubmissionEntity;
import com.neurospark.nerdnudge.useractivity.dto.UserQuizFlexSubmissionEntity;
import com.neurospark.nerdnudge.useractivity.dto.UserShotsSubmissionEntity;
import com.neurospark.nerdnudge.useractivity.utils.Commons;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class CountersService {

    private NerdPersistClient shotsStatsPersist;
    private static final String LIKES_SUFFIX = "-Likes";
    private static final String DISLIKES_SUFFIX = "-Dislikes";
    private static final String FAVS_SUFFIX = "-Favs";
    private static final String SHARES_SUFFIX = "-Shares";

    public void updateCounters(JsonObject userData, UserQuizFlexSubmissionEntity userQuizFlexSubmissionEntity, NerdPersistClient shotsStatsPersist) {
        this.shotsStatsPersist = shotsStatsPersist;
        log.info("Updating quizflex counters for user: {}", userQuizFlexSubmissionEntity.getUserId());
        updateCounter(userQuizFlexSubmissionEntity.getLikes(), LIKES_SUFFIX);
        updateCounter(userQuizFlexSubmissionEntity.getDislikes(), DISLIKES_SUFFIX);
        updateCounter(userQuizFlexSubmissionEntity.getShares(), SHARES_SUFFIX);
        updateFavorites(userData, userQuizFlexSubmissionEntity.getFavorites());
    }

    public void updateCounters(JsonObject userData, UserShotsSubmissionEntity userShotsSubmissionEntity, NerdPersistClient shotsStatsPersist) {
        this.shotsStatsPersist = shotsStatsPersist;
        log.info("Updating shots counters for user: {}", userShotsSubmissionEntity.getUserId());
        updateCounter(userShotsSubmissionEntity.getLikes(), LIKES_SUFFIX);
        updateCounter(userShotsSubmissionEntity.getDislikes(), DISLIKES_SUFFIX);
        updateCounter(userShotsSubmissionEntity.getShares(), SHARES_SUFFIX);
        updateFavorites(userData, userShotsSubmissionEntity.getFavorites());
    }

    public void updateCounters(JsonObject userData, UserFavoritesSubmissionEntity userFavoritesSubmissionEntity, NerdPersistClient shotsStatsPersist) {
        this.shotsStatsPersist = shotsStatsPersist;
        log.info("Updating favorites counters for user: {}", userFavoritesSubmissionEntity.getUserId());
        updateCounter(userFavoritesSubmissionEntity.getLikes(), LIKES_SUFFIX);
        updateCounter(userFavoritesSubmissionEntity.getDislikes(), DISLIKES_SUFFIX);
        updateCounter(userFavoritesSubmissionEntity.getShares(), SHARES_SUFFIX);
        deleteFavorites(userData, userFavoritesSubmissionEntity.getFavoritesToDelete());
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

                    if(! Commons.getInstance().arrayContains(recentArray, currentQuizFlexId)) {
                        recentArray.add(currentQuizFlexId);
                    }

                    if(! Commons.getInstance().arrayContains(thisSubtopicUserDataArray, currentQuizFlexId)) {
                        thisSubtopicUserDataArray.add(currentQuizFlexId);
                    }
                }
                Commons.getInstance().housekeepJsonArray(thisSubtopicUserDataArray, 20);
            }
        }

        Commons.getInstance().housekeepJsonArray(recentArray, 20);
    }

    private void deleteFavorites(JsonObject userData, Map<String, Map<String, List<String>>> favoritesToDelete) {
        if (favoritesToDelete == null)
            return;

        JsonElement favoritesEle = userData.get("favorites");
        if (favoritesEle == null || favoritesEle.isJsonNull())
            return;

        JsonObject favoritesObject = favoritesEle.getAsJsonObject();

        JsonElement recentEle = favoritesObject.get("recent");
        if (recentEle == null || recentEle.isJsonNull())
            return;

        JsonArray recentArray = recentEle.getAsJsonArray();

        JsonElement topicWiseEle = favoritesObject.get("topicwise");
        if (topicWiseEle == null || topicWiseEle.isJsonNull())
            return;

        JsonObject topicWiseUserDataObject = topicWiseEle.getAsJsonObject();

        for (String topic : favoritesToDelete.keySet()) {
            Map<String, List<String>> subtopicFavs = favoritesToDelete.get(topic);

            JsonElement thisTopicUserDataEle = topicWiseUserDataObject.get(topic);
            if (thisTopicUserDataEle == null || thisTopicUserDataEle.isJsonNull())
                continue;

            JsonObject thisTopicUserDataObject = thisTopicUserDataEle.getAsJsonObject();
            for (String subtopic : subtopicFavs.keySet()) {
                JsonElement thisSubtopicUserDataEle = thisTopicUserDataObject.get(subtopic);
                if (thisSubtopicUserDataEle == null || thisSubtopicUserDataEle.isJsonNull())
                    continue;

                JsonArray thisSubtopicUserDataArray = thisSubtopicUserDataEle.getAsJsonArray();

                List<String> quizFlexIds = subtopicFavs.get(subtopic);
                for (int i = 0; i < quizFlexIds.size(); i++) {
                    String currentQuizFlexId = quizFlexIds.get(i);
                    for (int j = 0; j < recentArray.size(); j++) {
                        if (recentArray.get(j).getAsString().equals(currentQuizFlexId))
                            recentArray.remove(j);
                    }

                    for (int j = 0; j < thisSubtopicUserDataArray.size(); j++) {
                        if (thisSubtopicUserDataArray.get(j).getAsString().equals(currentQuizFlexId))
                            thisSubtopicUserDataArray.remove(j);
                    }
                }
            }
        }
    }
}
