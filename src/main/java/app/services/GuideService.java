package app.services;

import app.daos.GuideDAO;
import app.daos.TripDAO;
import app.dtos.GuideDTO;
import app.entities.Guide;
import app.exceptions.ApiException;
import app.mappers.GuideMapper;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class GuideService {
    private final TripDAO tripDAO;
    private final GuideDAO guideDAO;

    public GuideService(EntityManagerFactory emf) {
        this.tripDAO = new TripDAO(emf);
        this.guideDAO = new GuideDAO(emf);
    }
    public GuideDTO createGuide(GuideDTO guideDTO) {
        Guide guide = GuideMapper.toEntity(guideDTO);
        guideDAO.create(guide);
        return GuideMapper.toDTO(guide);
    }
    public GuideDTO getById(int id) {
        Guide guide = guideDAO.getById(id);
        return GuideMapper.toDTO(guide);
    }
    public List<GuideDTO> getAll() {
        return guideDAO.getAll().stream()
                .map(GuideMapper::toDTO)
                .collect(Collectors.toList());
    }
    public GuideDTO update(GuideDTO guideDTO, int id) {
        Guide existing = guideDAO.getById(id);
        if (existing == null) {
            throw new ApiException(404, "Guide with Id: " + id + " does not exist");
        }
        existing.setName(guideDTO.getName());
        existing.setEmail(guideDTO.getEmail());
        existing.setPhoneNumber(guideDTO.getPhoneNumber());
        existing.setYearsOfExperience(guideDTO.getYearsOfExperience());

        Guide updated = guideDAO.update(id, existing);
        return GuideMapper.toDTO(updated);
    }
    public void delete(int id) {
        boolean deleted = guideDAO.deleteById(id);
        if (!deleted) {
            throw new ApiException(404, "Guide with Id: " + id + " does not exist");
        }
    }
}
