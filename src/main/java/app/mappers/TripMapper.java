package app.mappers;

import app.dtos.TripDTO;
import app.entities.Guide;
import app.entities.Trip;

public class TripMapper {
    public static TripDTO toDTO(Trip trip) {
        if(trip == null) return null;

        TripDTO tripDTO = new TripDTO();
        tripDTO.setId(trip.getId());
        tripDTO.setName(trip.getName());
        tripDTO.setCategory(trip.getCategory());
        tripDTO.setLatitude(trip.getLatitude());
        tripDTO.setLongitude(trip.getLongitude());
        tripDTO.setStartTime(trip.getStartTime());
        tripDTO.setEndTime(trip.getEndTime());
        tripDTO.setPrice(trip.getPrice());
        tripDTO.setGuideId(trip.getGuide().getId());
        tripDTO.setGuideName(trip.getGuide().getName());

        if (trip.getGuide() != null) {
            tripDTO.setGuideId(trip.getGuide().getId());
            tripDTO.setGuideName(trip.getGuide().getName());
        }
        return tripDTO;
    }
    public static Trip toEntity(TripDTO tripDTO, Guide guide) {
        if(tripDTO == null) return null;

        Trip trip = new Trip();
        trip.setId(tripDTO.getId());
        trip.setName(tripDTO.getName());
        trip.setCategory(tripDTO.getCategory());
        trip.setLatitude(tripDTO.getLatitude());
        trip.setLongitude(tripDTO.getLongitude());
        trip.setStartTime(tripDTO.getStartTime());
        trip.setEndTime(tripDTO.getEndTime());
        trip.setPrice(tripDTO.getPrice());

        trip.setGuide(guide);
        return trip;
    }
}
