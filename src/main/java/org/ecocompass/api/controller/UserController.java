package org.ecocompass.api.controller;

import org.ecocompass.api.response.UserProfileResponse;
import org.ecocompass.api.utility.database.DatabaseUtility;
import org.ecocompass.api.utility.user.UserPreferences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final DatabaseUtility databaseUtility;

    @Autowired
    public UserController(DatabaseUtility databaseUtility) {
        this.databaseUtility = databaseUtility;
    }

    @GetMapping("/profile")
    public UserProfileResponse getUserProfile(@RequestParam("userId") Long userId) {

        UserProfileResponse userProfileResponse = new UserProfileResponse();

        UserPreferences userPreferences = databaseUtility.getUserPreferences(userId);

        if (userPreferences != null) {
            userProfileResponse.setUserPreferences(userPreferences);
        } else {
            userProfileResponse.setUserPreferences(new UserPreferences());
        }

        return userProfileResponse;
    }
}
