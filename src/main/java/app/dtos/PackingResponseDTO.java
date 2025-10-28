package app.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PackingResponseDTO {
    @JsonProperty("items")
    private List<PackingItemDTO> items;
}
