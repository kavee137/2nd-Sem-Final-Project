package lk.ijse.aadbackend.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class AdSearchRequestDTO {
    private String keyword;
    private UUID categoryId;
    private UUID districtId;
    private UUID cityId;
}
