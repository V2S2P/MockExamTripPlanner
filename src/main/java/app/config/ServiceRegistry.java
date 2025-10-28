package app.config;

import app.services.GuideService;
import app.services.TripService;
import jakarta.persistence.EntityManagerFactory;

public class ServiceRegistry {

    public final GuideService guideService;
    public  TripService tripService;

    public ServiceRegistry(EntityManagerFactory emf) {
        this.guideService = new GuideService(emf);
        this.tripService = new TripService(emf);
    }
    public void setTripService(TripService tripService) {
        this.tripService = tripService;
    }
}
