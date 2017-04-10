package au.com.myii.jobmatcher.api;

import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import au.com.myii.jobmatcher.api.representations.Job;
import au.com.myii.jobmatcher.api.representations.Worker;

/**
 * Service to read from external jobs and workers APIs
 */
@Path("/jobmatcher")
@Produces(MediaType.APPLICATION_JSON)
public class JobMatcherClientService {

	private final Client client;
	private final String jobsAPIAddress;
	private final String workersAPIAddress;
	
	public JobMatcherClientService(Client client, String jobsAPIAddress, String workersAPIAddress) {
		this.client = client;
		this.jobsAPIAddress = jobsAPIAddress;
		this.workersAPIAddress = workersAPIAddress;
	}

	public List<Job> getJobs() {
		return client.target(jobsAPIAddress)
				.request(MediaType.APPLICATION_JSON)
				.get()
				.readEntity(new GenericType<List<Job>>() {});
	}

	public List<Worker> getWorkers() {
		return client.target(workersAPIAddress)
				.request(MediaType.APPLICATION_JSON)
				.get()
				.readEntity(new GenericType<List<Worker>>() {});
	}
	
	
}
