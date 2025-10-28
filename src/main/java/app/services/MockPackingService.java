package app.services;

import app.dtos.PackingItemDTO;
import app.dtos.PackingResponseDTO;

import java.util.List;

public class MockPackingService extends PackingService {

    @Override
    public PackingResponseDTO getPackingItems(String category) {
        PackingItemDTO tent = new PackingItemDTO();
        tent.setName("Tent");
        tent.setQuantity(1);
        tent.setWeightInGrams(2500);

        PackingItemDTO backpack = new PackingItemDTO();
        backpack.setName("Backpack");
        backpack.setQuantity(1);
        backpack.setWeightInGrams(800);

        PackingResponseDTO mock = new PackingResponseDTO();
        mock.setItems(List.of(tent, backpack));
        return mock;
    }

    @Override
    public int getTotalPackingWeight(String category) {
        return 3300; // predictable mock value for test verification
    }
}
