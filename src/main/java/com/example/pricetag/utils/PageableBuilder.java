package com.example.pricetag.utils;

import com.example.pricetag.dto.PaginationRequestDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageableBuilder {
    public static Pageable buildPageable(PaginationRequestDto paginationRequestDto) {
        int page =
                paginationRequestDto == null ? 0 : Math.max(0, paginationRequestDto.getPage() - 1);
        int size = paginationRequestDto == null ? 5 : Math.max(1, paginationRequestDto.getLimit());

        String sortBy = (paginationRequestDto == null || paginationRequestDto.getSortBy() == null ||
                         paginationRequestDto
                                 .getSortBy()
                                 .isBlank())
                        ? "createdAt"
                        : paginationRequestDto.getSortBy();

        Sort.Direction direction =
                (paginationRequestDto != null &&
                 "desc".equalsIgnoreCase(paginationRequestDto.getOrder()))
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return PageRequest.of(page, size, direction, sortBy);
    }
}
