package com.neurospark.nerdnudge.useractivity.persist.config;

import com.neurospark.nerdnudge.couchbase.service.NerdPersistClient;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PersistClusterConfig {

    @Getter
    @Value("${persist.connection-string}")
    private String persistConnectionString;

    @Getter
    @Value("${persist.username}")
    private String persistUsername;

    @Getter
    @Value("${persist.password}")
    private String persistPassword;

    @Value("${persist.config.bucket}")
    private String persistConfigBucketName;

    @Value("${persist.config.scope}")
    private String persistConfigScopeName;

    @Value("${persist.config.collection}")
    private String persistConfigCollectionName;

    @Bean(name = "configPersist")
    public NerdPersistClient configPersist() {
        return new NerdPersistClient(persistConnectionString, persistUsername, persistPassword, persistConfigBucketName, persistConfigScopeName, persistConfigCollectionName);
    }

    @Bean(name = "userProfilesPersist")
    public NerdPersistClient userProfilesPersist() {
        return new NerdPersistClient(persistConnectionString, persistUsername, persistPassword, "users", "users", "userProfiles");
    }

    @Bean(name = "shotsStatsPersist")
    public NerdPersistClient shotsStatsPersist() {
        return new NerdPersistClient(persistConnectionString, persistUsername, persistPassword, "content", "shotstats", "stats");
    }

    @Bean(name = "userFeedbackPersist")
    public NerdPersistClient userFeedbackPersist() {
        return new NerdPersistClient(persistConnectionString, persistUsername, persistPassword, "users", "users", "feedback");
    }

    @Bean(name = "terminatedUsersPersist")
    public NerdPersistClient terminatedUsersPersist() {
        return new NerdPersistClient(persistConnectionString, persistUsername, persistPassword, "users", "users", "terminated");
    }
}
