package app.dtos;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GuideDTO {
    private Integer id;
    private String name;
    private String email;
    private String phoneNumber;
    private int yearsOfExperience;
}
