package io.gurm.demo.profile;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class ProfileResponse {

    @JsonProperty("app-name")
    private final String applicationName;

    @JsonProperty("profiles")
    private final List<String> activeProfiles;

    public ProfileResponse(String applicationName, List<String> activeProfiles) {
        this.applicationName = applicationName;
        this.activeProfiles = activeProfiles;
    }
}
