package org.ecocompass.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.ecocompass.api.utility.user.UserPreferences;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileResponse {

    @JsonProperty("userPreferences")
    private UserPreferences userPreferences;

    public void setUserPreferences(UserPreferences userPreferences) {
        this.userPreferences = userPreferences;
    }
}
