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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;

@Slf4j
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
        log.info("Updating topic code map.");
        JsonObject topicCodeToTopicNameMappingObject = configPersist.get("collection_topic_mapping");
        topicNameToTopicCodeMapping = new JsonObject();
        topicCodeToTopicNameMapping = new JsonObject();

        Iterator<Map.Entry<String, JsonElement>> topicsIterator = topicCodeToTopicNameMappingObject.entrySet().iterator();
        while(topicsIterator.hasNext()) {
            Map.Entry<String, JsonElement> thisEntry = topicsIterator.next();
            topicNameToTopicCodeMapping.addProperty(thisEntry.getValue().getAsString(), thisEntry.getKey());
            topicCodeToTopicNameMapping.addProperty(thisEntry.getKey(), thisEntry.getValue().getAsString());
        }

        log.info("Topic Name To Codes Mapping: {}", topicNameToTopicCodeMapping);
        log.info("Topic Code To Names Mapping: {}", topicCodeToTopicNameMapping);
    }

    @Override
    public void updateUserQuizflexSubmission(UserQuizFlexSubmissionEntity userQuizFlexSubmissionEntity) {
        JsonObject userData = getUserProfileDocument(userQuizFlexSubmissionEntity.getUserId());
        UserActivityCounts counts = new UserActivityCounts(userQuizFlexSubmissionEntity);

        new DayQuotaService().updateDayQuota(userData, userQuizFlexSubmissionEntity, nerdConfig, userProfilesPersist);
        new UserSummaryService().updateUserSummary(userData, counts);
        new TopicwiseSummaryService().updateTopicwiseSummary(userData, counts, shotsStatsPersist);
        new UserLevelService().updateUserLevel(userData, userQuizFlexSubmissionEntity, counts);
        new DayStatsService().updateDayStats(userData, counts);
        new StreaksService().updateStreak(userData, userQuizFlexSubmissionEntity);
        new CountersService().updateCounters(userData, userQuizFlexSubmissionEntity, shotsStatsPersist);

        if(userQuizFlexSubmissionEntity.getUserFullName() != null && ! userQuizFlexSubmissionEntity.getUserFullName().isEmpty())
            userData.addProperty("userFullName", userQuizFlexSubmissionEntity.getUserFullName());

        saveUserProfileDocument(userQuizFlexSubmissionEntity.getUserId(), userData);
    }

    @Override
    public void updateUserShotsSubmission(UserShotsSubmissionEntity userShotsSubmissionEntity) {
        JsonObject userData = getUserProfileDocument(userShotsSubmissionEntity.getUserId());
        new DayQuotaService().updateDayQuota(userData, userShotsSubmissionEntity, nerdConfig, userProfilesPersist);
        new CountersService().updateCounters(userData, userShotsSubmissionEntity, shotsStatsPersist);
        updateWeeklyShotsSummary(userData, userShotsSubmissionEntity);

        if(userShotsSubmissionEntity.getUserFullName() != null && ! userShotsSubmissionEntity.getUserFullName().isEmpty())
            userData.addProperty("userFullName", userShotsSubmissionEntity.getUserFullName());

        saveUserProfileDocument(userShotsSubmissionEntity.getUserId(), userData);
    }

    private void updateWeeklyShotsSummary(JsonObject userData, UserShotsSubmissionEntity userShotsSubmissionEntity) {
        JsonElement summaryEle = userData.get("Summary");
        JsonObject summaryObject = (summaryEle == null || summaryEle.isJsonNull()) ? new JsonObject() : summaryEle.getAsJsonObject();

        Map<String, Map<String, Integer>> currentShots = userShotsSubmissionEntity.getShots();
        int currentShotsQuotaUsed = 0;
        for(String thisTopic: currentShots.keySet()) {
            Map<String, Integer> subtopicWiseShots = currentShots.get(thisTopic);
            for(String thisSubtopic: subtopicWiseShots.keySet()) {
                currentShotsQuotaUsed += subtopicWiseShots.get(thisSubtopic);
            }
        }

        JsonElement weekSummaryEle = summaryObject.get("weekly");
        JsonObject weekSummaryObject = (weekSummaryEle == null || weekSummaryEle.isJsonNull()) ? new JsonObject() : weekSummaryEle.getAsJsonObject();

        String currentWeek = Commons.getInstance().getWeekstamp();
        JsonElement weekArrayEle = weekSummaryObject.get(currentWeek);
        JsonArray weekArray = (weekArrayEle == null || weekArrayEle.isJsonNull()) ? new JsonArray() : weekArrayEle.getAsJsonArray();
        if (weekArray.size() > 1) {
            weekArray.set(1, new JsonPrimitive(weekArray.get(0).getAsInt() + currentShotsQuotaUsed));
        } else if (weekArray.size() > 0) {
            weekArray.add(currentShotsQuotaUsed);
        }
        else if (weekArray.size() == 0) {
            weekArray.add(0);
            weekArray.add(currentShotsQuotaUsed);
        }
    }

    @Override
    public void updateUserFavoritesSubmission(UserFavoritesSubmissionEntity userFavoritesSubmissionEntity) {
        JsonObject userData = getUserProfileDocument(userFavoritesSubmissionEntity.getUserId());
        new CountersService().updateCounters(userData, userFavoritesSubmissionEntity, shotsStatsPersist);
        saveUserProfileDocument(userFavoritesSubmissionEntity.getUserId(), userData);
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
    }

    private void saveUserProfileDocument(String userId, JsonObject userDocument) {
        userDataCache.put(userId, userDocument);
        userProfilesPersist.set(userId, userDocument);
        log.info("Setting the data for user: {}", userId);
    }


    private JsonObject getUserProfileDocument(String userId) {
        if(userDataCache.containsKey(userId)) {
            log.info("Returning user data from cache: {}", userId);
            return userDataCache.get(userId);
        }

        JsonObject userData = userProfilesPersist.get(userId);
        if(userData == null) {
            log.info("User does not exist, creating a new user: {}", userId);
            userData = new JsonObject();
            userData.addProperty("registrationDate", Instant.now().getEpochSecond());
            userData.addProperty("type", "userProfile");
            userData.addProperty("accountType", "freemium");
            userData.addProperty("accountStartDate", Commons.getInstance().getDaystamp());
        }

        return userData;
    }
}
