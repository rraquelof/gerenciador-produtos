
package com.example.gerenciador_produtos;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

@Service
public class ProdutoService {

    private final ProdutoPostgresRepository produtoPostgresRepository;
    private final ProdutoMongoRepository produtoMongoRepository;

    public ProdutoService(
            ProdutoPostgresRepository produtoPostgresRepository,
            ProdutoMongoRepository produtoMongoRepository) {

        this.produtoPostgresRepository = produtoPostgresRepository;
        this.produtoMongoRepository = produtoMongoRepository;
    }

    // CENARIO 1: PostgreSQL primeiro, MongoDB depois
    public Produto cadastrarPostgresPrimeiro(
            Produto produto,
            boolean simularFalhaMongo) {

        Produto produtoSalvo = produtoPostgresRepository.save(produto);

        try {

            ProdutoMongo produtoMongo = new ProdutoMongo(
                    produtoSalvo.getId(),
                    produtoSalvo.getNome(),
                    produtoSalvo.getDescricao(),
                    produtoSalvo.getPreco(),
                    produtoSalvo.getQuantidadeEstoque()
            );

            if (simularFalhaMongo) {
                throw new RuntimeException(
                        "Falha simulada no MongoDB"
                );
            }

            produtoMongoRepository.save(produtoMongo);

            return produtoSalvo;

        } catch (Exception e) {

            produtoPostgresRepository.deleteById(produtoSalvo.getId());

            throw new RuntimeException(
                    "Falha no MongoDB. PostgreSQL foi revertido.",
                    e
            );
        }
    }

    // CENARIO 2: MongoDB primeiro, PostgreSQL depois
    public Produto cadastrarMongoPrimeiro(
            Produto produto,
            boolean simularFalhaPostgres) {

        ProdutoMongo produtoMongo = new ProdutoMongo(
                null,
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getQuantidadeEstoque()
        );

        ProdutoMongo produtoMongoSalvo =
                produtoMongoRepository.save(produtoMongo);

        try {

            if (simularFalhaPostgres) {
                throw new RuntimeException(
                        "Falha simulada no PostgreSQL"
                );
            }

            Produto produtoSalvo =
                    produtoPostgresRepository.save(produto);

            produtoMongoSalvo.setProdutoId(
                    produtoSalvo.getId()
            );

            produtoMongoRepository.save(produtoMongoSalvo);

            return produtoSalvo;

        } catch (Exception e) {

            // ROLLBACK: excluir do MongoDB
            produtoMongoRepository.delete(produtoMongoSalvo);

            throw new RuntimeException(
                    "Falha no PostgreSQL. MongoDB foi revertido.",
                    e
            );
        }
    }

    // CENARIO 3: Atualizar os dois bancos
    public Produto atualizarProduto(
            Long id,
            Produto dados,
            boolean simularFalhaMongo) {

        // Busca o produto no PostgreSQL
        Produto produto = produtoPostgresRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Produto não encontrado no PostgreSQL"
                ));

        // Busca o produto no MongoDB
        ProdutoMongo produtoMongo =
                produtoMongoRepository.findByProdutoId(id)
                        .orElseThrow(() -> new RuntimeException(
                                "Produto não encontrado no MongoDB"
                        ));

        // Guarda os dados antigos do PostgreSQL
        String nomeAntigo = produto.getNome();
        String descricaoAntiga = produto.getDescricao();
        BigDecimal precoAntigo = produto.getPreco();
        Integer quantidadeAntiga = produto.getQuantidadeEstoque();

        // Guarda os dados antigos do MongoDB
        String nomeMongoAntigo = produtoMongo.getNome();
        String descricaoMongoAntiga = produtoMongo.getDescricao();
        BigDecimal precoMongoAntigo = produtoMongo.getPreco();
        Integer quantidadeMongoAntiga =
                produtoMongo.getQuantidadeEstoque();

        try {

            // Atualiza os dados no PostgreSQL
            produto.setNome(dados.getNome());
            produto.setDescricao(dados.getDescricao());
            produto.setPreco(dados.getPreco());
            produto.setQuantidadeEstoque(
                    dados.getQuantidadeEstoque()
            );

            produtoPostgresRepository.save(produto);

            if (simularFalhaMongo) {
                throw new RuntimeException(
                        "Falha simulada durante a atualização do MongoDB"
                );
            }

            // Atualiza os dados no MongoDB
            produtoMongo.setNome(dados.getNome());
            produtoMongo.setDescricao(dados.getDescricao());
            produtoMongo.setPreco(dados.getPreco());
            produtoMongo.setQuantidadeEstoque(
                    dados.getQuantidadeEstoque()
            );

            produtoMongoRepository.save(produtoMongo);

            // Verifica se os dados estão iguais nos dois bancos
            boolean consistente = verificarConsistencia(id);

            if (!consistente) {
                throw new RuntimeException(
                        "Os bancos ficaram inconsistentes"
                );
            }

            return produto;

        } catch (Exception e) {

            // ROLLBACK: no PostgreSQL
            produto.setNome(nomeAntigo);
            produto.setDescricao(descricaoAntiga);
            produto.setPreco(precoAntigo);
            produto.setQuantidadeEstoque(quantidadeAntiga);

            produtoPostgresRepository.save(produto);

            // ROLLBACK: no MongoDB
            produtoMongo.setNome(nomeMongoAntigo);
            produtoMongo.setDescricao(descricaoMongoAntiga);
            produtoMongo.setPreco(precoMongoAntigo);
            produtoMongo.setQuantidadeEstoque(
                    quantidadeMongoAntiga
            );

            produtoMongoRepository.save(produtoMongo);

            throw new RuntimeException(
                    "Falha na atualização. Os dados anteriores foram restaurados.",
                    e
            );
        }
    }

    public boolean verificarConsistencia(Long id) {

        for (int tentativa = 0; tentativa < 100; tentativa++) {

            Produto produtoPostgres =
                    produtoPostgresRepository.findById(id)
                            .orElse(null);

            ProdutoMongo produtoMongo =
                    produtoMongoRepository.findByProdutoId(id)
                            .orElse(null);

            if (produtoPostgres != null && produtoMongo != null) {

                boolean iguais =
                        produtoPostgres.getNome().equals(
                                produtoMongo.getNome())
                                && produtoPostgres.getDescricao().equals(
                                produtoMongo.getDescricao())
                                && produtoPostgres.getPreco().compareTo(
                                produtoMongo.getPreco()) == 0
                                && produtoPostgres.getQuantidadeEstoque().equals(
                                produtoMongo.getQuantidadeEstoque());

                if (iguais) {
                    return true;
                }
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        return false;
    }
}
