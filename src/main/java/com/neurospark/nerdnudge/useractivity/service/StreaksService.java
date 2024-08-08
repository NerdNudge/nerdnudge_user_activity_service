package com.neurospark.nerdnudge.useractivity.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.neurospark.nerdnudge.useractivity.dto.UserQuizFlexSubmissionEntity;
import java.util.List;
import java.util.Map;

public class StreaksService {
    public void updateStreak(JsonObject userData, UserQuizFlexSubmissionEntity userQuizFlexSubmissionEntity) {
        JsonElement streakEle = userData.get("streak");
        JsonObject streakObject = (streakEle == null || streakEle.isJsonNull()) ? new JsonObject() : streakEle.getAsJsonObject();

        int current = streakObject.has("current") ? streakObject.get("current").getAsInt() : 0;
        int highest = streakObject.has("highest") ? streakObject.get("highest").getAsInt() : 0;


        Map<String, Map<String, List<String>>> currentQuizflexes = userQuizFlexSubmissionEntity.getQuizflex();
        for (String thisTopic : currentQuizflexes.keySet()) {
            Map<String, List<String>> allQuizflexes = currentQuizflexes.get(thisTopic);
            for (String questionId : allQuizflexes.keySet()) {
                List<String> thisQuizflex = allQuizflexes.get(questionId);
                if (thisQuizflex.get(2).equals("Y")) {
                    current++;
                    highest = (current > highest) ? current : highest;
                } else {
                    current = 0;
                }
            }
        }
        streakObject.addProperty("current", current);
        streakObject.addProperty("highest", highest);

        userData.add("streak", streakObject);
    }
}
