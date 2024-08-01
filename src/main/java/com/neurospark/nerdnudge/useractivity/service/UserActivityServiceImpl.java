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
import lombok.Data;
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
        housekeepDayJsonObject(dayQuotaObject, nerdConfig.get("dayQuotaRetentionDays").getAsInt());
        userData.add("dayQuota", dayQuotaObject);
    }

    private void housekeepDayJsonObject(JsonObject jsonObject, int retentionEntries) {
        Set<Map.Entry <String ,JsonElement>> dailyQuotaKeys = jsonObject.entrySet();
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

    private String getDaystamp() {
        LocalDate date = LocalDate.now();
        int dayOfYear = date.getDayOfYear();
        int year = date.getYear() % 100;
        String dayOfYearStr = String.format("%03d", dayOfYear);
        String yearStr = String.format("%02d", year);
        return dayOfYearStr + yearStr;
    }

    private void updateUserSummary(JsonObject userData, UserQuizFlexSubmissionEntity userQuizFlexSubmissionEntity) {
        CountsFromUserActivity counts = getCountsFromUserActivity(userQuizFlexSubmissionEntity);
        updateOverallUserSummary(userData, counts);
        updateLast30DaysUserSummary(userData, counts);
    }

    private CountsFromUserActivity getCountsFromUserActivity(UserQuizFlexSubmissionEntity userQuizFlexSubmissionEntity) {
        CountsFromUserActivity counts = new CountsFromUserActivity();
        Map<String, Map<String, List<String>>> currentQuizflexes = userQuizFlexSubmissionEntity.getQuizflex();
        for(String thisTopic: currentQuizflexes.keySet()) {
            Map<String, List<String>> allQuizflexes = currentQuizflexes.get(thisTopic);
            for(String questionId: allQuizflexes.keySet()) {
                List<String> thisQuizflex = allQuizflexes.get(questionId);
                counts.currentTotalCount ++;
                if(thisQuizflex.get(1).equals("easy")) {
                    counts.currentEasyCount ++;
                    counts.currentEasyCorrect = thisQuizflex.get(2).equals("Y") ? counts.currentEasyCorrect + 1 : counts.currentEasyCorrect;
                } else if(thisQuizflex.get(1).equals("medium")) {
                    counts.currentMedCount ++;
                    counts.currentMedCorrect = thisQuizflex.get(2).equals("Y") ? counts.currentMedCorrect + 1 : counts.currentMedCorrect;
                } else {
                    counts.currentHardCount ++;
                    counts.currentHardCorrect = thisQuizflex.get(2).equals("Y") ? counts.currentHardCorrect + 1 : counts.currentHardCorrect;
                }

                counts.currentTotalCorrect = thisQuizflex.get(2).equals("Y") ? counts.currentTotalCorrect + 1 : counts.currentTotalCorrect;
            }
        }
        return counts;
    }

    private void updateOverallUserSummary(JsonObject userData, CountsFromUserActivity counts) {

        JsonElement summaryEle = userData.get("Summary");
        JsonObject summaryObject = (summaryEle == null || summaryEle.isJsonNull()) ? new JsonObject() : summaryEle.getAsJsonObject();

        JsonElement overallSummaryEle = summaryObject.get("overallSummary");
        JsonObject overallSummaryObject = (overallSummaryEle == null || overallSummaryEle.isJsonNull()) ? new JsonObject() : overallSummaryEle.getAsJsonObject();

        JsonArray totalArray = getUpdatedArray(overallSummaryObject.get("total"), counts.currentTotalCount, counts.currentTotalCorrect);
        JsonArray easyArray = getUpdatedArray(overallSummaryObject.get("easy"), counts.currentEasyCount, counts.currentEasyCorrect);
        JsonArray medArray = getUpdatedArray(overallSummaryObject.get("medium"), counts.currentMedCount, counts.currentMedCorrect);
        JsonArray hardArray = getUpdatedArray(overallSummaryObject.get("hard"), counts.currentHardCount, counts.currentHardCorrect);

        overallSummaryObject.add("total", totalArray);
        overallSummaryObject.add("easy", easyArray);
        overallSummaryObject.add("medium", medArray);
        overallSummaryObject.add("hard", hardArray);

        summaryObject.add("overallSummary", overallSummaryObject);
        userData.add("Summary", summaryObject);
    }

    private JsonArray getUpdatedArray(JsonElement arrayElement, int totalCount, int correctCount) {
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

    private void updateLast30DaysUserSummary(JsonObject userData, CountsFromUserActivity counts) {
        JsonElement summaryEle = userData.get("Summary");
        JsonObject summaryObject = (summaryEle == null || summaryEle.isJsonNull()) ? new JsonObject() : summaryEle.getAsJsonObject();

        JsonElement last30DaysEle = summaryObject.get("last30Days");
        JsonObject last30DaysObject = (last30DaysEle == null || last30DaysEle.isJsonNull()) ? new JsonObject() : last30DaysEle.getAsJsonObject();

        String currentDayStamp = getDaystamp();
        JsonElement currentDayEle = last30DaysObject.get(currentDayStamp);
        JsonObject currentDayObject = (currentDayEle == null || currentDayEle.isJsonNull()) ? new JsonObject() : currentDayEle.getAsJsonObject();

        JsonArray easyArray = getUpdatedArray(currentDayObject.get("easy"), counts.currentEasyCount, counts.currentEasyCorrect);
        JsonArray medArray = getUpdatedArray(currentDayObject.get("medium"), counts.currentMedCount, counts.currentMedCorrect);
        JsonArray hardArray = getUpdatedArray(currentDayObject.get("hard"), counts.currentHardCount, counts.currentHardCorrect);

        currentDayObject.add("easy", easyArray);
        currentDayObject.add("medium", medArray);
        currentDayObject.add("hard", hardArray);

        last30DaysObject.add(currentDayStamp, currentDayObject);
        housekeepDayJsonObject(last30DaysObject, 30);

        summaryObject.add("last30Days", last30DaysObject);
        userData.add("Summary", summaryObject);
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

        System.out.println("user data returned: " + userData);
        return userData;
    }
}

@Data
class CountsFromUserActivity {
    int currentTotalCount = 0;
    int currentTotalCorrect = 0;
    int currentEasyCount = 0;
    int currentEasyCorrect = 0;
    int currentMedCount = 0;
    int currentMedCorrect = 0;
    int currentHardCount = 0;
    int currentHardCorrect = 0;
}
