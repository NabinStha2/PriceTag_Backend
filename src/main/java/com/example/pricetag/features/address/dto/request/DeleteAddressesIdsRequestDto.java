package com.example.pricetag.features.address.dto.request;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeleteAddressesIdsRequestDto {

    @NotNull(message = "Address ids are required")
    @Size(min = 1,
            message = "At least one address id is required")
    private Long[] ids;
}
