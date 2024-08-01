package com.neurospark.nerdnudge.useractivity.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.neurospark.nerdnudge.couchbase.service.NerdPersistClient;
import com.neurospark.nerdnudge.useractivity.dto.UserQuizFlexSubmissionEntity;
import com.neurospark.nerdnudge.useractivity.dto.UserShotsSubmissionEntity;
import com.neurospark.nerdnudge.useractivity.utils.LRUCache;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
public class UserActivityServiceImpl implements UserActivityService {
    private LRUCache<String, JsonObject> userDataCache;
    private NerdPersistClient configPersist;
    private NerdPersistClient userProfilesPersist;
    private JsonObject nerdConfig;

    @Autowired
    public void UserActivityServiceImpl(@Qualifier("configPersist") NerdPersistClient configPersist,
                                        @Qualifier("userProfilesPersist") NerdPersistClient userProfilesPersist) {
        this.configPersist = configPersist;
        this.userProfilesPersist = userProfilesPersist;
    }

    @PostConstruct
    public void initialize() {
        nerdConfig = configPersist.get("nerd_config");
        userDataCache = new LRUCache<>(nerdConfig.get("numCacheEntries").getAsInt(), 0.75f);
    }

    @Override
    public void updateUserQuizflexSubmission(UserQuizFlexSubmissionEntity userQuizFlexSubmissionEntity) {
        JsonObject userData = getUserProfileDocument(userQuizFlexSubmissionEntity.getUserId());
        updateDayQuota(userData, userQuizFlexSubmissionEntity);
        updateUserSummary(userData, userQuizFlexSubmissionEntity);
        updateTopicwiseSummary(userData, userQuizFlexSubmissionEntity);
        updateDayStats(userData, userQuizFlexSubmissionEntity);
        updateStreak(userData, userQuizFlexSubmissionEntity);

        saveUserProfileDocument(userQuizFlexSubmissionEntity.getUserId(), userData);
        System.out.println("Re-fetch: " + userProfilesPersist.get(userQuizFlexSubmissionEntity.getUserId()));
        System.out.println("from cache: " + getUserProfileDocument(userQuizFlexSubmissionEntity.getUserId()));
    }

    private void updateDayQuota(JsonObject userData, UserQuizFlexSubmissionEntity userQuizFlexSubmissionEntity) {
        Map<String, Map<String, List<String>>> currentQuizflexes = userQuizFlexSubmissionEntity.getQuizflex();
        int currentQuizflexQuotaUsed = 0;
        for(String thisTopic: currentQuizflexes.keySet()) {
            Map<String, List<String>> allQuizflexes = currentQuizflexes.get(thisTopic);
            currentQuizflexQuotaUsed += allQuizflexes.size();
        }
        JsonElement dayQuotaEle = userData.get("dayQuota");
        JsonObject dayQuotaObject = (dayQuotaEle == null || dayQuotaEle.isJsonNull()) ? new JsonObject() : dayQuotaEle.getAsJsonObject();

        JsonElement currentDayQuotaEle = dayQuotaObject.get(getDaystamp());
        JsonArray currentDayArray = (currentDayQuotaEle == null || currentDayQuotaEle.isJsonNull()) ? new JsonArray() : currentDayQuotaEle.getAsJsonArray();

        if (currentDayArray.size() == 0) {
            currentDayArray.add(0);
            currentDayArray.add(0);
        }

        currentDayArray.set(0, new JsonPrimitive(currentDayArray.get(0).getAsInt() + currentQuizflexQuotaUsed));
        dayQuotaObject.add(getDaystamp(), currentDayArray);
        housekeepDayQuota(dayQuotaObject);
        userData.add("dayQuota", dayQuotaObject);
    }

    private void housekeepDayQuota(JsonObject dayQuotaObject) {
        int dayQuotaRetentionDays = nerdConfig.get("dayQuotaRetentionDays").getAsInt();
        Set<Map.Entry <String ,JsonElement>> dailyQuotaKeys = dayQuotaObject.entrySet();
        if(dailyQuotaKeys.size() <= dayQuotaRetentionDays)
            return;

        TreeSet<String> sortedKeys = new TreeSet<>();
        for (Map.Entry<String, JsonElement> entry : dayQuotaObject.entrySet()) {
            sortedKeys.add(entry.getKey());
        }

        while(dayQuotaObject.entrySet().size() > dayQuotaRetentionDays) {
            String oldestKey = sortedKeys.pollFirst();
            if (oldestKey != null) {
                dayQuotaObject.remove(oldestKey);
            }
        }
    }
    private String getDaystamp() {
        LocalDate date = LocalDate.now();
        int dayOfYear = date.getDayOfYear();
        int year = date.getYear() % 100;
        String dayOfYearStr = String.format("%03d", dayOfYear);
        String yearStr = String.format("%02d", year);
        return dayOfYearStr + yearStr;
    }

    private void updateUserSummary(JsonObject userData, UserQuizFlexSubmissionEntity userQuizFlexSubmissionEntity) {
        updateOverallUserSummary(userData, userQuizFlexSubmissionEntity);
        updateLast30DaysUserSummary(userData, userQuizFlexSubmissionEntity);
    }

    private void updateOverallUserSummary(JsonObject userData, UserQuizFlexSubmissionEntity userQuizFlexSubmissionEntity) {
        
    }

    private void updateLast30DaysUserSummary(JsonObject userData, UserQuizFlexSubmissionEntity userQuizFlexSubmissionEntity) {

    }

    private void updateTopicwiseSummary(JsonObject userData, UserQuizFlexSubmissionEntity userQuizFlexSubmissionEntity) {
        updateTopicwiseOverallSummary(userData, userQuizFlexSubmissionEntity);
        updateTopicwiseLast30DaysSummary(userData, userQuizFlexSubmissionEntity);
    }

    private void updateTopicwiseOverallSummary(JsonObject userData, UserQuizFlexSubmissionEntity userQuizFlexSubmissionEntity) {

    }

    private void updateTopicwiseLast30DaysSummary(JsonObject userData, UserQuizFlexSubmissionEntity userQuizFlexSubmissionEntity) {

    }

    private void updateDayStats(JsonObject userData, UserQuizFlexSubmissionEntity userQuizFlexSubmissionEntity) {

    }

    private void updateStreak(JsonObject userData, UserQuizFlexSubmissionEntity userQuizFlexSubmissionEntity) {

    }

    @Override
    public void updateUserShotsSubmission(UserShotsSubmissionEntity userShotsSubmissionEntity) {
        JsonObject userData = getUserProfileDocument(userShotsSubmissionEntity.getUserId());
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
        }

        return userData;
    }
}
