package com.neurospark.nerdnudge.useractivity.service;

import com.google.gson.JsonObject;
import com.neurospark.nerdnudge.couchbase.service.NerdPersistClient;
import com.neurospark.nerdnudge.useractivity.dto.UserQuizFlexSubmissionEntity;
import com.neurospark.nerdnudge.useractivity.dto.UserShotsSubmissionEntity;
import com.neurospark.nerdnudge.useractivity.utils.LRUCache;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class UserActivityServiceImpl implements UserActivityService {
    private LRUCache<String, JsonObject> userDataCache;

    private NerdPersistClient configPersist;
    private NerdPersistClient userProfilesPersist;

    @Autowired
    public void UserActivityServiceImpl(@Qualifier("configPersist") NerdPersistClient configPersist,
                                        @Qualifier("userProfilesPersist") NerdPersistClient userProfilesPersist) {
        this.configPersist = configPersist;
        this.userProfilesPersist = userProfilesPersist;
    }

    @PostConstruct
    public void initialize() {
        JsonObject nerdConfig = configPersist.get("nerd_config");
        userDataCache = new LRUCache<>(nerdConfig.get("numCacheEntries").getAsInt(), 0.75f);
    }

    @Override
    public void updateUserQuizflexSubmission(UserQuizFlexSubmissionEntity userQuizFlexSubmissionEntity) {
        JsonObject userData = getUserDocument(userQuizFlexSubmissionEntity.getUserId());
        
    }

    @Override
    public void updateUserShotsSubmission(UserShotsSubmissionEntity userShotsSubmissionEntity) {
        JsonObject userData = getUserDocument(userShotsSubmissionEntity.getUserId());
    }

    private JsonObject getUserDocument(String userId) {
        if(userDataCache.containsKey(userId))
            return userDataCache.get(userId);

        JsonObject userData = userProfilesPersist.get(userId);
        if(userData == null) {
            userData = new JsonObject();
            userData.addProperty("startDate", System.currentTimeMillis());
        }

        return userData;
    }
}
