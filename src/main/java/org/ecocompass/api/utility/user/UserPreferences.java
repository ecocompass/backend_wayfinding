package org.ecocompass.api.utility.user;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "user_preferences")
public class UserPreferences {

    private boolean publicTransportPreferred;
    private boolean greenerAlternativesPreferred;
    private boolean bicyclePreferred;
    private boolean walkingPreferred;

    @Id
    private Long user_id;

    public UserPreferences() {
        this.user_id = 0L;
        this.publicTransportPreferred = false;
        this.greenerAlternativesPreferred = false;
        this.bicyclePreferred = false;
        this.walkingPreferred = false;
    }

    public UserPreferences(Long user_id, boolean publicTransportPreferred, boolean greenerAlternativesPreferred,
                           boolean bicyclePreferred, boolean walkingPreferred) {
        this.user_id = user_id;
        this.publicTransportPreferred = publicTransportPreferred;
        this.greenerAlternativesPreferred = greenerAlternativesPreferred;
        this.bicyclePreferred = bicyclePreferred;
        this.walkingPreferred = walkingPreferred;
    }

    // Getters and setters for each preference

    public boolean isPublicTransportPreferred() {
        return publicTransportPreferred;
    }

    public void setPublicTransportPreferred(boolean publicTransportPreferred) {
        this.publicTransportPreferred = publicTransportPreferred;
    }

    public boolean isGreenerAlternativesPreferred() {
        return greenerAlternativesPreferred;
    }

    public void setGreenerAlternativesPreferred(boolean greenerAlternativesPreferred) {
        this.greenerAlternativesPreferred = greenerAlternativesPreferred;
    }

    public boolean isBicyclePreferred() {
        return bicyclePreferred;
    }

    public void setBicyclePreferred(boolean bicyclePreferred) {
        this.bicyclePreferred = bicyclePreferred;
    }

    public boolean isWalkingPreferred() {
        return walkingPreferred;
    }

    public void setWalkingPreferred(boolean walkingPreferred) {
        this.walkingPreferred = walkingPreferred;
    }

    public void setUser_id(Long userId) {
        this.user_id = userId;
    }

    public Long getUser_id() {
        return user_id;
    }
}
