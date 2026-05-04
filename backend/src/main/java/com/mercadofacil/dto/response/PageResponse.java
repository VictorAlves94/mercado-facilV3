package com.mercadofacil.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
    List<T> content,
    int pagina,
    int tamanhoPagina,
    long totalElementos,
    int totalPaginas,
    boolean primeira,
    boolean ultima
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast()
        );
    }
}
