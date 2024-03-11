package org.ecocompass.api.utility.database;

import org.ecocompass.api.utility.user.UserPreferences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseUtility {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DatabaseUtility(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public UserPreferences getUserPreferences(Long userId) {
        String sql = "SELECT * FROM user_preferences WHERE user_id = ?";
        return jdbcTemplate.queryForObject(sql, (resultSet, rowNum) -> {
            return new UserPreferences(
                    resultSet.getLong("user_id"),
                    resultSet.getBoolean("public_transport_preferred"),
                    resultSet.getBoolean("greener_alternatives_preferred"),
                    resultSet.getBoolean("bicycle_preferred"),
                    resultSet.getBoolean("walking_preferred")
            );
        }, userId);
    }

    public void setUserPreferences(UserPreferences userPreferences, Long userId) {
        String sql = "UPDATE user_preferences SET " +
                "user_id = ?, " +
                "public_transport_preferred = ?, " +
                "greener_alternatives_preferred = ?, " +
                "bicycle_preferred = ?, " +
                "walking_preferred = ? ";

        jdbcTemplate.update(sql,
                userId,
                userPreferences.isPublicTransportPreferred(),
                userPreferences.isGreenerAlternativesPreferred(),
                userPreferences.isBicyclePreferred(),
                userPreferences.isWalkingPreferred());
    }
}
