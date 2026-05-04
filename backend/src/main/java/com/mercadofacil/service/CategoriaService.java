package com.mercadofacil.service;

import com.mercadofacil.dto.response.CategoriaResponse;
import com.mercadofacil.entity.Categoria;
import com.mercadofacil.exception.BusinessException;
import com.mercadofacil.exception.ResourceNotFoundException;
import com.mercadofacil.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public List<CategoriaResponse> listarTodas() {
        return categoriaRepository.findAllByOrderByNomeAsc()
                .stream().map(CategoriaResponse::from).toList();
    }

    public CategoriaResponse buscarPorId(Long id) {
        return CategoriaResponse.from(findOrThrow(id));
    }

    @Transactional
    public CategoriaResponse criar(String nome, String descricao) {
        if (categoriaRepository.existsByNome(nome)) {
            throw new BusinessException("Já existe uma categoria com o nome: " + nome);
        }
        Categoria cat = Categoria.builder().nome(nome).descricao(descricao).build();
        return CategoriaResponse.from(categoriaRepository.save(cat));
    }

    @Transactional
    public CategoriaResponse atualizar(Long id, String nome, String descricao) {
        Categoria cat = findOrThrow(id);
        cat.setNome(nome);
        cat.setDescricao(descricao);
        return CategoriaResponse.from(categoriaRepository.save(cat));
    }

    @Transactional
    public void excluir(Long id) {
        Categoria cat = findOrThrow(id);
        if (cat.getProdutos() != null && !cat.getProdutos().isEmpty()) {
            throw new BusinessException("Não é possível excluir categoria com produtos vinculados.");
        }
        categoriaRepository.delete(cat);
    }

    private Categoria findOrThrow(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", id));
    }
}
