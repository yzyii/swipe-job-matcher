package au.com.myii.jobmatcher.api;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;

/**
 * Specifies the config parameters that must be supplied by the .yml file on service startup.
 */
public class JobMatcherConfiguration extends Configuration {
	
    @JsonProperty
    @NotEmpty
    public String jobsAPIAddress;
    
    @JsonProperty
    @NotEmpty
    public String workersAPIAddress;
    
    @Valid
    @NotNull
    @JsonProperty("jerseyClient")
    private JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();
    
    public JerseyClientConfiguration getJerseyClientConfiguration() {
        return jerseyClient;
    }
    
    public JobMatcherConfiguration() {
        jerseyClient.setGzipEnabled(false);
        jerseyClient.setGzipEnabledForRequests(false);
    }
}
