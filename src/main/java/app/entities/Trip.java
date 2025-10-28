package app.entities;

import app.enums.Category;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private double longitude;
    private double latitude;
    private double price;
    @Enumerated(EnumType.STRING)
    private Category category;

    @ManyToOne
    private Guide guide;
}
