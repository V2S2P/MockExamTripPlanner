package app.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PackingItemDTO {
    private String name;
    private int weightInGrams;
    private int quantity;
    private String description;
    private String category;
    private String createdAt;
    private String updatedAt;
    private List<BuyingOptionDTO> buyingOptions;
}

