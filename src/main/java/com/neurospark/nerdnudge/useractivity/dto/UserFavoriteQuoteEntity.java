package com.neurospark.nerdnudge.useractivity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserFavoriteQuoteEntity {
    String userId;
    String quoteId;
}
