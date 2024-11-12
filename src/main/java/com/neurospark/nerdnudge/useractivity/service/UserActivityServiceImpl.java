package com.neurospark.nerdnudge.useractivity.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.neurospark.nerdnudge.couchbase.service.NerdPersistClient;
import com.neurospark.nerdnudge.useractivity.dto.*;
import com.neurospark.nerdnudge.useractivity.utils.Commons;
import com.neurospark.nerdnudge.useractivity.utils.LRUCache;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;

@Service
public class UserActivityServiceImpl implements UserActivityService {
    private LRUCache<String, JsonObject> userDataCache;
    private NerdPersistClient configPersist;
    private NerdPersistClient userProfilesPersist;
    private NerdPersistClient shotsStatsPersist;
    private NerdPersistClient userFeedbackPersist;
    private NerdPersistClient terminatedUsersPersist;
    private JsonObject nerdConfig;
    public static JsonObject topicNameToTopicCodeMapping = null;
    public static JsonObject topicCodeToTopicNameMapping = null;

    @Autowired
    public void UserActivityServiceImpl(@Qualifier("configPersist") NerdPersistClient configPersist,
                                        @Qualifier("userProfilesPersist") NerdPersistClient userProfilesPersist,
                                        @Qualifier("shotsStatsPersist") NerdPersistClient shotsStatsPersist,
                                        @Qualifier("userFeedbackPersist") NerdPersistClient userFeedbackPersist,
                                        @Qualifier("terminatedUsersPersist") NerdPersistClient terminatedUsersPersist) {
        this.configPersist = configPersist;
        this.userProfilesPersist = userProfilesPersist;
        this.shotsStatsPersist = shotsStatsPersist;
        this.userFeedbackPersist = userFeedbackPersist;
        this.terminatedUsersPersist = terminatedUsersPersist;
    }

    @PostConstruct
    public void initialize() {
        nerdConfig = configPersist.get("nerd_config");
        userDataCache = new LRUCache<>(nerdConfig.get("numCacheEntries").getAsInt(), 0.75f);
        if(topicNameToTopicCodeMapping == null)
            updateTopicCodeMaps();
    }

    private void updateTopicCodeMaps() {
        System.out.println("Updating topic code map.");
        JsonObject topicCodeToTopicNameMappingObject = configPersist.get("collection_topic_mapping");
        topicNameToTopicCodeMapping = new JsonObject();
        topicCodeToTopicNameMapping = new JsonObject();

        Iterator<Map.Entry<String, JsonElement>> topicsIterator = topicCodeToTopicNameMappingObject.entrySet().iterator();
        while(topicsIterator.hasNext()) {
            Map.Entry<String, JsonElement> thisEntry = topicsIterator.next();
            topicNameToTopicCodeMapping.addProperty(thisEntry.getValue().getAsString(), thisEntry.getKey());
            topicCodeToTopicNameMapping.addProperty(thisEntry.getKey(), thisEntry.getValue().getAsString());
        }

        System.out.println("Topic Name To Codes Mapping: " + topicNameToTopicCodeMapping);
        System.out.println("Topic Code To Names Mapping: " + topicCodeToTopicNameMapping);
    }

    @Override
    public void updateUserQuizflexSubmission(UserQuizFlexSubmissionEntity userQuizFlexSubmissionEntity) {
        JsonObject userData = getUserProfileDocument(userQuizFlexSubmissionEntity.getUserId());
        UserActivityCounts counts = new UserActivityCounts(userQuizFlexSubmissionEntity);

        new DayQuotaService().updateDayQuota(userData, userQuizFlexSubmissionEntity, nerdConfig, userProfilesPersist);
        new UserSummaryService().updateUserSummary(userData, counts);
        new TopicwiseSummaryService().updateTopicwiseSummary(userData, counts, shotsStatsPersist);
        new RealworldChallengeService().updateRealworldChallenge(userData, userQuizFlexSubmissionEntity, counts);
        new UserScoresService().updateUserScores(userData, counts);
        new DayStatsService().updateDayStats(userData, counts);
        new StreaksService().updateStreak(userData, userQuizFlexSubmissionEntity);
        new CountersService().updateCounters(userData, userQuizFlexSubmissionEntity, shotsStatsPersist);
        new PeerComparisonService().updatePeerComparisonData(counts, shotsStatsPersist);

        if(userQuizFlexSubmissionEntity.getUserFullName() != null && ! userQuizFlexSubmissionEntity.getUserFullName().isEmpty())
            userData.addProperty("userFullName", userQuizFlexSubmissionEntity.getUserFullName());

        saveUserProfileDocument(userQuizFlexSubmissionEntity.getUserId(), userData);
        System.out.println("Re-fetch: " + userProfilesPersist.get(userQuizFlexSubmissionEntity.getUserId()));
        System.out.println("from cache: " + getUserProfileDocument(userQuizFlexSubmissionEntity.getUserId()));
    }

    @Override
    public void updateUserShotsSubmission(UserShotsSubmissionEntity userShotsSubmissionEntity) {
        JsonObject userData = getUserProfileDocument(userShotsSubmissionEntity.getUserId());
        new DayQuotaService().updateDayQuota(userData, userShotsSubmissionEntity, nerdConfig, userProfilesPersist);
        new CountersService().updateCounters(userData, userShotsSubmissionEntity, shotsStatsPersist);

        if(userShotsSubmissionEntity.getUserFullName() != null && ! userShotsSubmissionEntity.getUserFullName().isEmpty())
            userData.addProperty("userFullName", userShotsSubmissionEntity.getUserFullName());

        saveUserProfileDocument(userShotsSubmissionEntity.getUserId(), userData);
        System.out.println("Re-fetch: " + userProfilesPersist.get(userShotsSubmissionEntity.getUserId()));
        System.out.println("from cache: " + getUserProfileDocument(userShotsSubmissionEntity.getUserId()));
    }

    @Override
    public void updateUserFavoritesSubmission(UserFavoritesSubmissionEntity userFavoritesSubmissionEntity) {
        JsonObject userData = getUserProfileDocument(userFavoritesSubmissionEntity.getUserId());
        new CountersService().updateCounters(userData, userFavoritesSubmissionEntity, shotsStatsPersist);
        saveUserProfileDocument(userFavoritesSubmissionEntity.getUserId(), userData);
        System.out.println("Re-fetch: " + userProfilesPersist.get(userFavoritesSubmissionEntity.getUserId()));
        System.out.println("from cache: " + getUserProfileDocument(userFavoritesSubmissionEntity.getUserId()));
    }

    @Override
    public void updateUserFavoriteQuoteSubmission(UserFavoriteQuoteEntity userFavoriteQuoteEntity) {
        JsonObject userData = getUserProfileDocument(userFavoriteQuoteEntity.getUserId());
        JsonElement favoritesEle = userData.get("favorites");
        JsonObject favoritesObject = (favoritesEle == null || favoritesEle.isJsonNull()) ? new JsonObject() : favoritesEle.getAsJsonObject();
        userData.add("favorites", favoritesObject);

        JsonElement favoriteQuotesEle = favoritesObject.get("quotes");
        JsonArray favoriteQuotesArray = (favoriteQuotesEle == null || favoriteQuotesEle.isJsonNull()) ? new JsonArray() : favoriteQuotesEle.getAsJsonArray();
        favoritesObject.add("quotes", favoriteQuotesArray);

        if(! Commons.getInstance().arrayContains(favoriteQuotesArray, userFavoriteQuoteEntity.getQuoteId())) {
            favoriteQuotesArray.add(new JsonPrimitive(userFavoriteQuoteEntity.getQuoteId()));
        }
        Commons.getInstance().housekeepJsonArray(favoriteQuotesArray, 20);
        saveUserProfileDocument(userFavoriteQuoteEntity.getUserId(), userData);
    }

    @Override
    public void updateUserFeedbackSubmission(UserFeedbackSubmissionEntity userFeedbackSubmissionEntity) {
        JsonObject feedbackObject = new JsonObject();
        long timestamp = System.currentTimeMillis();
        feedbackObject.addProperty("userId", userFeedbackSubmissionEntity.getUserId());
        feedbackObject.addProperty("feedbackType", userFeedbackSubmissionEntity.getFeedbackType());
        feedbackObject.addProperty("feedback", userFeedbackSubmissionEntity.getFeedback());
        feedbackObject.addProperty("status", "NEW");
        feedbackObject.addProperty("timestamp", timestamp);
        feedbackObject.addProperty("docType", "userFeedback");

        userFeedbackPersist.set(userFeedbackSubmissionEntity.getUserId() + "_" + timestamp, feedbackObject);
    }

    @Override
    public void deleteUserAccount(String userId) {
        JsonObject userData = getUserProfileDocument(userId);
        userData.addProperty("terminationDate", System.currentTimeMillis());
        terminatedUsersPersist.set(userId, userData);
        userProfilesPersist.delete(userId);

        JsonObject userTrendsData = userProfilesPersist.get(userId + "-trends");
        if(userTrendsData != null) {
            terminatedUsersPersist.set(userId + "-trends", userTrendsData);
            userProfilesPersist.delete(userId + "-trends");
        }
    }

    private void saveUserProfileDocument(String userId, JsonObject userDocument) {
        userDataCache.put(userId, userDocument);
        userProfilesPersist.set(userId, userDocument);
        System.out.println("setting the data as: " + userDocument + " for user: " + userId);
    }


    private JsonObject getUserProfileDocument(String userId) {
        if(userDataCache.containsKey(userId))
            return userDataCache.get(userId);

        JsonObject userData = userProfilesPersist.get(userId);
        if(userData == null) {
            userData = new JsonObject();
            userData.addProperty("registrationDate", Instant.now().getEpochSecond());
            userData.addProperty("type", "userProfile");
            userData.addProperty("accountType", "freemium");
            userData.addProperty("accountStartDate", Commons.getInstance().getDaystamp());
        }

        System.out.println("user data returned: " + userData);
        return userData;
    }
}
