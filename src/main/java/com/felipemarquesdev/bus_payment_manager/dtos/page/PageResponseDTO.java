package com.felipemarquesdev.bus_payment_manager.dtos.page;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public record PageResponseDTO<T> (
        List<T> content,

        Integer pageNumber,

        Integer pageSize,

        Long totalElements,

        Integer totalPages,

        Boolean last
){

    public static <T, E> PageResponseDTO<T> fromPage(Page<E> page, Function<E, T> mapper) {
        List<T> content = page.getContent()
                .stream()
                .map(mapper)
                .toList();

        return new PageResponseDTO<T>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    };
}
