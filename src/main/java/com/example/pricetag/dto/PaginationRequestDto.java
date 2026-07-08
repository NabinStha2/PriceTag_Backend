package com.example.pricetag.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaginationRequestDto {
    private int page;
    private int limit;
    private String sortBy;
    private String order;
    private String search;
}
