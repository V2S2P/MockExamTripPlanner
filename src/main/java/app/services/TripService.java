package app.services;

import app.daos.GuideDAO;
import app.daos.TripDAO;
import app.dtos.TripDTO;
import app.entities.Guide;
import app.entities.Trip;
import app.enums.Category;
import app.exceptions.ApiException;
import app.mappers.TripMapper;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TripService {
    private final TripDAO tripDAO;
    private final GuideDAO guideDAO;
    private final PackingService packingService;
    public TripService(EntityManagerFactory emf, PackingService packingService) {
        this.tripDAO = new TripDAO(emf);
        this.guideDAO = new GuideDAO(emf);
        this.packingService = packingService;
    }
    public TripService(EntityManagerFactory emf) {
        this(emf, new PackingService());
    }
    public TripDTO create(TripDTO tripDTO) {
        //Fetch guide entity from DB
        Guide guide = guideDAO.getById(tripDTO.getGuideId());
        if (guide == null) {
            throw new ApiException(400, "Guide with ID " +  tripDTO.getGuideId() + " not found");
        }

        //Map DTO to Entity, passing the guide
        Trip trip = TripMapper.toEntity(tripDTO, guide);

        //Save to DB
        tripDAO.create(trip);
        //Map back to DTO for response
        return TripMapper.toDTO(trip);
    }
    public TripDTO getById(int id) {
        Trip trip = tripDAO.getById(id);
        return TripMapper.toDTO(trip);
    }
    public List<TripDTO> getAll() {
        return tripDAO.getAll().stream()
                .map(TripMapper::toDTO)
                .collect(Collectors.toList());
    }
    public TripDTO update(TripDTO tripDTO, int id) {
        Trip existing = tripDAO.getById(id);
        if (existing == null) {
            throw new ApiException(404, "Trip with Id: " + id + " does not exist");
        }

        // Update fields from DTO
        existing.setName(tripDTO.getName());
        existing.setCategory(tripDTO.getCategory());
        existing.setLatitude(tripDTO.getLatitude());
        existing.setLongitude(tripDTO.getLongitude());
        existing.setStartTime(tripDTO.getStartTime());
        existing.setEndTime(tripDTO.getEndTime());
        existing.setPrice(tripDTO.getPrice());

        // Update guide if guideId is present
        if (tripDTO.getGuideId() != null) {
            Guide guide = guideDAO.getById(tripDTO.getGuideId());
            existing.setGuide(guide);
        }

        Trip updated = tripDAO.update(id, existing);
        return TripMapper.toDTO(updated);
    }
    public void delete(int id) {
        try {
            boolean deleted = tripDAO.deleteById(id);
            if (!deleted) {
                throw new ApiException(404, "Trip with Id: " + id + " does not exist");
            }
        }catch (NoResultException e){
            throw new ApiException(404, "Trip with Id: " + id + " does not exist");
        }
    }
    public TripDTO linkGuideToTrip(int tripId, int guideId) {
        Trip trip = tripDAO.getById(tripId);
        Guide guide = guideDAO.getById(guideId);

        trip.setGuide(guide);
        Trip updated = tripDAO.update(tripId, trip);

        return TripMapper.toDTO(updated);
    }
    public List<TripDTO> filterTripsByCategory(String category) {
        Category categoryEnum;
        try {
            categoryEnum = Category.valueOf(category.toUpperCase());
        }catch (IllegalArgumentException e) {
            throw new ApiException(400, "Invalid category: " + category);
        }
        List<Trip> allTrips = tripDAO.getAll();
        List<TripDTO> filtered = allTrips.stream()
                .filter(trip -> trip.getCategory() == categoryEnum)
                .map(TripMapper::toDTO)
                .collect(Collectors.toList());
        return filtered;
    }
    public Map<Integer,Double> getTotalPriceByGuide(){
        return tripDAO.getAll().stream()
                .collect(Collectors.groupingBy(
                        trip -> trip.getGuide().getId(),
                        Collectors.summingDouble(Trip::getPrice)
                ));
    }

    // FOR FETCHING EXTERNAL API
    public TripDTO getByIdWithPacking(int tripId) {
        Trip trip = tripDAO.getById(tripId);
        TripDTO tripDTO = TripMapper.toDTO(trip);

        // Fetch packing items using HttpClient-based PackingService
        tripDTO.setPackingItems(packingService.getPackingItems(trip.getCategory().name()).getItems());
        return tripDTO;
    }

    public int getTotalPackingWeight(int tripId) {
        Trip trip = tripDAO.getById(tripId);
        return packingService.getTotalPackingWeight(trip.getCategory().name());
    }

}
