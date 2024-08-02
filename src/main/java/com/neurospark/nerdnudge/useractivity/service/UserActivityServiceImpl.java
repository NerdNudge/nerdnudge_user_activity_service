package com.neurospark.nerdnudge.useractivity.service;

import com.google.gson.JsonObject;
import com.neurospark.nerdnudge.couchbase.service.NerdPersistClient;
import com.neurospark.nerdnudge.useractivity.dto.UserQuizFlexSubmissionEntity;
import com.neurospark.nerdnudge.useractivity.dto.UserShotsSubmissionEntity;
import com.neurospark.nerdnudge.useractivity.utils.Commons;
import com.neurospark.nerdnudge.useractivity.utils.LRUCache;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import java.time.Instant;

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
        UserActivityCounts counts = new UserActivityCounts(userQuizFlexSubmissionEntity);

        new DayQuotaService().updateDayQuota(userData, userQuizFlexSubmissionEntity, nerdConfig.get("dayQuotaRetentionDays").getAsInt());
        new UserSummaryService().updateUserSummary(userData, counts);
        new TopicwiseSummaryService().updateTopicwiseSummary(userData, counts);
        new DayStatsService().updateDayStats(userData, userQuizFlexSubmissionEntity);
        new StreaksService().updateStreak(userData, userQuizFlexSubmissionEntity);

        saveUserProfileDocument(userQuizFlexSubmissionEntity.getUserId(), userData);
        System.out.println("Re-fetch: " + userProfilesPersist.get(userQuizFlexSubmissionEntity.getUserId()));
        System.out.println("from cache: " + getUserProfileDocument(userQuizFlexSubmissionEntity.getUserId()));
    }

    @Override
    public void updateUserShotsSubmission(UserShotsSubmissionEntity userShotsSubmissionEntity) {

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
