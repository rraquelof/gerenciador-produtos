
package com.example.gerenciador_produtos;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ProdutoMongoRepository
        extends MongoRepository<ProdutoMongo, String> {

    Optional<ProdutoMongo> findByProdutoId(Long produtoId);

    void deleteByProdutoId(Long produtoId);
}
