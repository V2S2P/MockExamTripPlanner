package app.mappers;

import app.dtos.GuideDTO;
import app.entities.Guide;
import app.entities.Trip;

public class GuideMapper {
    public static GuideDTO toDTO(Guide guide){
        if(guide == null)  return null;

        GuideDTO guideDTO = new GuideDTO();
        guideDTO.setId(guide.getId());
        guideDTO.setName(guide.getName());
        guideDTO.setEmail(guide.getEmail());
        guideDTO.setPhoneNumber(guide.getPhoneNumber());
        guideDTO.setYearsOfExperience(guide.getYearsOfExperience());
        return guideDTO;
    }
    public static Guide toEntity(GuideDTO guideDTO){
        if(guideDTO == null)  return null;

        Guide guide = new Guide();
        guide.setId(guideDTO.getId());
        guide.setName(guideDTO.getName());
        guide.setEmail(guideDTO.getEmail());
        guide.setPhoneNumber(guideDTO.getPhoneNumber());
        guide.setYearsOfExperience(guideDTO.getYearsOfExperience());
        return guide;
    }
}
