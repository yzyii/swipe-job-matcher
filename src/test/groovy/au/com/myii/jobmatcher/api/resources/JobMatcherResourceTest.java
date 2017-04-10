package au.com.myii.jobmatcher.api.resources;

import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import au.com.myii.jobmatcher.api.representations.Job;
import au.com.myii.jobmatcher.api.representations.JobSearchAddress;
import au.com.myii.jobmatcher.api.representations.Location;
import au.com.myii.jobmatcher.api.representations.Worker;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.FixtureHelpers;

public class JobMatcherResourceTest extends BaseResourceTest {
	
	private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
	
    @Before
    public void setup() {
    }
    
    @Test
    public void getJobs_BaseCase_Retrieved() throws Exception {
    	JavaType jobListType = MAPPER.getTypeFactory().constructCollectionType(List.class, Job.class);
    	List<Job> jobs = MAPPER.readValue(FixtureHelpers.fixture("fixtures/jobs.json"), jobListType);
    	when(jobMatcherClientService.getJobs()).thenReturn(jobs);
    	
    	JavaType workerListType = MAPPER.getTypeFactory().constructCollectionType(List.class, Worker.class);
    	List<Worker> workers = MAPPER.readValue(FixtureHelpers.fixture("fixtures/workers.json"), workerListType);
    	when(jobMatcherClientService.getWorkers()).thenReturn(workers);
    	
    	String result = target("/jobmatcher/21/").request().get(String.class);
    	String expected = MAPPER.writeValueAsString(MAPPER.readValue(FixtureHelpers.fixture("fixtures/result.json"), jobListType));
    	
    	// Note that this particular equality check will fail often if the matching algorithm is improved.
    	Assert.assertEquals(result, expected);
    	
    	List<Job> jobResults = MAPPER.readValue(result, jobListType);
    	
    	Assert.assertTrue(jobResults.size() <= 3);
    }
    
    @Test
    public void isWithinDistance_Within_Pass() throws Exception {
    	JobMatcherResource resource = new JobMatcherResource(null);
    	
    	Job job = new Job();
    	Location location = new Location();
    	location.setLatitude("-33.796001");
    	location.setLongitude("151.178788");
    	job.setLocation(location);
    	
    	Worker worker = new Worker();
    	JobSearchAddress jobSearchAddress = new JobSearchAddress();
    	jobSearchAddress.setMaxJobDistance(10);
    	jobSearchAddress.setLatitude("-33.783316");
    	jobSearchAddress.setLongitude("151.176222");
    	worker.setJobSearchAddress(jobSearchAddress);
    	
    	Assert.assertTrue(resource.isWithinDistance(job, worker));
    }
    
    @Test
    public void isWithinDistance_Outside_Fail() throws Exception {
    	JobMatcherResource resource = new JobMatcherResource(null);
    	
    	Job job = new Job();
    	Location location = new Location();
    	location.setLatitude("-33.796001");
    	location.setLongitude("151.178788");
    	job.setLocation(location);
    	
    	Worker worker = new Worker();
    	JobSearchAddress jobSearchAddress = new JobSearchAddress();
    	jobSearchAddress.setMaxJobDistance(10);
    	jobSearchAddress.setLatitude("-33.897122");
    	jobSearchAddress.setLongitude("151.209417");
    	worker.setJobSearchAddress(jobSearchAddress);
    	
    	Assert.assertFalse(resource.isWithinDistance(job, worker));
    }
}
