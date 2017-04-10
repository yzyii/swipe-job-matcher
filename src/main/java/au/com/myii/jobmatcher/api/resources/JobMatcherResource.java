package au.com.myii.jobmatcher.api.resources;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;

import au.com.myii.jobmatcher.api.JobMatcherClientService;
import au.com.myii.jobmatcher.api.representations.Job;
import au.com.myii.jobmatcher.api.representations.JobSearchAddress;
import au.com.myii.jobmatcher.api.representations.Location;
import au.com.myii.jobmatcher.api.representations.Worker;

/**
 * Resource to match jobs to workers
 */
@Path("/jobmatcher")
@Produces(MediaType.APPLICATION_JSON)
public class JobMatcherResource {
	
	private static final int MAX_JOBS_RETURNED = 3;
	private static final Response NOT_FOUND = Response.status(Status.NOT_FOUND).build();

	private final JobMatcherClientService clientService;
	
	public JobMatcherResource(JobMatcherClientService clientService) {
		this.clientService = clientService;
	}
	
	/**
	 * Takes a workerId and returns no more than three appropriate jobs for that Worker.
	 */
	@GET
    @Timed
    @ExceptionMetered
    @Path("/{workerId}")
    public Response getJobs(@PathParam("workerId") int workerId) {
		Response response = NOT_FOUND;
		
		List<Job> jobs = clientService.getJobs();
		List<Worker> workers = clientService.getWorkers();
		
		// Possible to cache this into a HashMap if this data is slow moving and update periodically.
		// Otherwise, the API on the other side should really be changed to allow specific worker lookup.
		Optional<Worker> workerOptional = workers.stream().filter(w -> w.getUserId() == workerId).findFirst();
		
		if (workerOptional.isPresent()) {
			Worker worker = workerOptional.get();
			
			List<Job> filteredJobs = jobs.stream()
					.filter(driverLicenseFilter(worker))
					.filter(certificateFilter(worker))
					.filter(locationFilter(worker))
					.collect(Collectors.toList());
			// It seems like we should be able to filter by availability and job start date as well but their local time zone is not specified.
			// Although, if we really want to, it is possible to derive it from their longitude and latitude 
			
			filteredJobs.sort(new PayDistanceComparator(worker));
			
			GenericEntity<List<Job>> entity = new GenericEntity<List<Job>>(filteredJobs.subList(0, MAX_JOBS_RETURNED)) {};
			response = Response.ok(entity).build();
		}
		
		return response;
    }
	
	private Predicate<Job> driverLicenseFilter(Worker worker) {
		return j -> !j.getDriverLicenseRequired() || (j.getDriverLicenseRequired() && worker.getHasDriversLicense());
	}
	
	private Predicate<Job> certificateFilter(Worker worker) {
		return j -> worker.getCertificates().containsAll(j.getRequiredCertificates());
	}
	
	private Predicate<Job> locationFilter(Worker worker) {
		return j -> isWithinDistance(j, worker);
	}
	
	protected boolean isWithinDistance(Job job, Worker worker) {
		JobSearchAddress workerLocation = worker.getJobSearchAddress();
		Location jobLocation = job.getLocation();
		
		// TODO: This should be refactored into the actual POJOs so that Jackson can deserialize it directly into GeoLocation and we don't have to recreate GeoLocations everywhere.
		GeoLocation workerGeoLocation = GeoLocation.fromDegrees(Double.valueOf(workerLocation.getLatitude()), Double.valueOf(workerLocation.getLongitude()));
		GeoLocation jobGeoLocation = GeoLocation.fromDegrees(Double.valueOf(jobLocation.getLatitude()), Double.valueOf(jobLocation.getLongitude()));
		
		return workerGeoLocation.distanceToInKm(jobGeoLocation) <= workerLocation.getMaxJobDistance();
	}
	
	/**
	 * Sorts jobs for a worker ordering first by pay, then distance.
	 * Ideally should be replaced with a single sorting heuristic that weights both variables.
	 */
	// TODO: This class should probably be tested - particularly if it grows in complexity.
	private class PayDistanceComparator implements Comparator<Job> {
		
		private final GeoLocation workerGeoLocation;
		
		public PayDistanceComparator(Worker worker) {
			JobSearchAddress workerLocation = worker.getJobSearchAddress();
			workerGeoLocation = GeoLocation.fromDegrees(Double.valueOf(workerLocation.getLatitude()), Double.valueOf(workerLocation.getLongitude()));
		}
		
	    public int compare(Job job1, Job job2) {
	    	// TODO: As with locations, this should also be refactored into the actual POJOs.
	    	// This compare method is currently fairly inefficient due to needing to do string replacements and BigDecimal / Geolocation creations constantly.
	        int returnValue = new BigDecimal(job2.getBillRate().replace("$","")).compareTo(new BigDecimal(job1.getBillRate().replace("$","")));
	        
	        if (returnValue == 0) {
	        	Location job1Location = job1.getLocation();
	        	Location job2Location = job2.getLocation();
	        	GeoLocation job1GeoLocation = GeoLocation.fromDegrees(Double.valueOf(job1Location.getLatitude()), Double.valueOf(job1Location.getLongitude()));
	        	GeoLocation job2GeoLocation = GeoLocation.fromDegrees(Double.valueOf(job2Location.getLatitude()), Double.valueOf(job2Location.getLongitude()));
	        	
	        	returnValue = workerGeoLocation.distanceToInKm(job1GeoLocation) > workerGeoLocation.distanceToInKm(job2GeoLocation) ? -1 : 1;
	        }
	        
	        return returnValue;
	    }
	}
}
