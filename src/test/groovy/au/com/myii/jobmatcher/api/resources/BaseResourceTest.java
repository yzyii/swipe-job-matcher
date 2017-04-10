package au.com.myii.jobmatcher.api.resources;

import static org.mockito.Mockito.mock;

import org.glassfish.jersey.servlet.ServletProperties;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.TestProperties;

import au.com.myii.jobmatcher.api.JobMatcherClientService;
import io.dropwizard.jersey.DropwizardResourceConfig;

/**
 * A Base Test class for all resources. Should include authentication and basic setup for resource tests via extension of {@link DropwizardResourceConfig}
 */
public abstract class BaseResourceTest extends JerseyTest {
	
	protected static JobMatcherClientService jobMatcherClientService = mock(JobMatcherClientService.class);
	
    @Override
    protected DeploymentContext configureDeployment() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
        
        return ServletDeploymentContext.builder(new ResourceConfig())
            .initParam(ServletProperties.JAXRS_APPLICATION_CLASS, DropwizardResourceConfig.class.getName())
            .build();
    }
    
    private static class ResourceConfig extends DropwizardResourceConfig {
    	
    	public ResourceConfig() {
    		register(new JobMatcherResource(jobMatcherClientService));
    	}
    }
}
