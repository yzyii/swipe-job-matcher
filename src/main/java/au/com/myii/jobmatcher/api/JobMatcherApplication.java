package au.com.myii.jobmatcher.api;

import javax.ws.rs.client.Client;

import au.com.myii.jobmatcher.api.resources.JobMatcherResource;
import io.dropwizard.Application;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Environment;

/**
 * Entry point to start the Job Matcher service. All Managed resources should be initialised in this class.
 */
public class JobMatcherApplication extends Application<JobMatcherConfiguration> {
	
    public static void main(String[] args) throws Exception {
        new JobMatcherApplication().run(args);
    }
    
    @Override
    public void run(JobMatcherConfiguration configuration, Environment environment) throws Exception {
        Client client = new JerseyClientBuilder(environment)
        		.using(configuration.getJerseyClientConfiguration())
        		.build(getName());
        
        JobMatcherClientService jobMatcherClientService = new JobMatcherClientService(client, configuration.jobsAPIAddress, configuration.workersAPIAddress);
        
        // Register Jersey APIs
        JobMatcherResource jobMatcherResource = new JobMatcherResource(jobMatcherClientService);
        
        environment.jersey().register(jobMatcherResource);	
    }
}
