package com.example.gerenciador_produtos;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoPostgresRepository
        extends JpaRepository<Produto, Long> {
}
