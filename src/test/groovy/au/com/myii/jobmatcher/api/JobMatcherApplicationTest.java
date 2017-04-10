package au.com.myii.jobmatcher.api;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;

import au.com.myii.jobmatcher.api.JobMatcherApplication;
import au.com.myii.jobmatcher.api.JobMatcherConfiguration;
import au.com.myii.jobmatcher.api.resources.JobMatcherResource;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.jetty.setup.ServletEnvironment;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Environment;

/**
 * Tests that {@link JobMatcherApplication} correctly registered all required bundles, resources, database migrations etc.
 */
public class JobMatcherApplicationTest {
	
    private final Environment environment = mock(Environment.class);
    private final JerseyEnvironment jersey = mock(JerseyEnvironment.class);
    private final LifecycleEnvironment lifecycle =  spy(new LifecycleEnvironment());
    private final HealthCheckRegistry healthchecks = spy(new HealthCheckRegistry());
    private final JobMatcherApplication application = new JobMatcherApplication();
    private final JobMatcherConfiguration config = new JobMatcherConfiguration();
    private final MetricRegistry metrics = spy(new MetricRegistry());
    private final ServletEnvironment servlets = mock(ServletEnvironment.class);
    
    @Before
    public void setup() throws Exception {
        config.jobsAPIAddress = "";
        config.workersAPIAddress = "";
        when(environment.lifecycle()).thenReturn(lifecycle);
        when(environment.jersey()).thenReturn(jersey);
        when(environment.healthChecks()).thenReturn(healthchecks);
        when(environment.metrics()).thenReturn(metrics);
        when(environment.servlets()).thenReturn(servlets);
    }
    
    @Test
    public void registersAllResources() throws Exception {
        application.run(config, environment);
        
        verify(jersey).register(isA(JobMatcherResource.class));
    }
}
