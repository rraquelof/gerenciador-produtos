
package com.example.gerenciador_produtos;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;
    private final ProdutoPostgresRepository postgresRepository;

    public ProdutoController(
            ProdutoService produtoService,
            ProdutoPostgresRepository postgresRepository) {

        this.produtoService = produtoService;
        this.postgresRepository = postgresRepository;
    }

    // CENARIO 1
    @PostMapping("/cenario1")
    public Produto cadastrarPostgresPrimeiro(
            @RequestBody Produto produto,
            @RequestParam(defaultValue = "false")
            boolean falharMongo) {

        return produtoService.cadastrarPostgresPrimeiro(
                produto,
                falharMongo
        );
    }

    // CENARIO 2
    @PostMapping("/cenario2")
    public Produto cadastrarMongoPrimeiro(
            @RequestBody Produto produto,
            @RequestParam(defaultValue = "false")
            boolean falharPostgres) {

        return produtoService.cadastrarMongoPrimeiro(
                produto,
                falharPostgres
        );
    }

    // CENARIO 3: Atualizar produto e verificar consistência
    @PutMapping("/{id}/cenario3")
    public Produto atualizarProduto(
            @PathVariable Long id,
            @RequestBody Produto produto,
            @RequestParam(defaultValue = "false") boolean falharMongo) {

        return produtoService.atualizarProduto(id, produto, falharMongo);
    }

    // LISTAR PRODUTOS
    @GetMapping
    public List<Produto> listarProdutos() {

        return postgresRepository.findAll();
    }
}
