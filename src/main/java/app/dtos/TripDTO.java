package app.dtos;

import app.enums.Category;
import lombok.*;

import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TripDTO {
    private Integer id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private double longitude;
    private double latitude;
    private double price;
    private Category category;

    // lightweight reference to Guide
    private Integer guideId;
    private String guideName;

    //FOR FETCHING EXTERNAL API
    private List<PackingItemDTO> packingItems;
}
