package com.mercadofacil.dto.request;

import java.util.List;

public record AutorizarRequest(
        String email,
        String senha,
        List<String> perfisPermitidos
) {
}
