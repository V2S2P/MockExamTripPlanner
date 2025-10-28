package app.services;

import app.dtos.PackingResponseDTO;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class PackingService {

    private final HttpClient client;
    private final ObjectMapper objectMapper;

    public PackingService() {
        this.client = HttpClient.newHttpClient(); // thread-safe, reusable
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public PackingResponseDTO getPackingItems(String category) {
        String url = "https://packingapi.cphbusinessapps.dk/packinglist/" + category.toLowerCase();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to fetch packing items: HTTP " + response.statusCode());
            }

            return objectMapper.readValue(response.body(), PackingResponseDTO.class);

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error fetching packing items: " + e.getMessage(), e);
        }
    }

    public int getTotalPackingWeight(String category) {
        PackingResponseDTO packingResponse = getPackingItems(category);
        return packingResponse.getItems().stream()
                .mapToInt(item -> item.getWeightInGrams() * item.getQuantity())
                .sum();
    }
}
