# Gerenciador de Produtos

## Descrição

O Gerenciador de Produtos é uma aplicação desenvolvida em Java com Spring Boot, utilizando PostgreSQL e MongoDB para armazenamento de dados.

O projeto tem como objetivo implementar operações de cadastro e atualização de produtos em dois bancos de dados, buscando manter a consistência das informações mesmo diante de falhas durante as operações.

A aplicação implementa três cenários de gerenciamento de dados:

- **Cenário 1:** Cadastro do produto primeiro no PostgreSQL e depois no MongoDB.
- **Cenário 2:** Cadastro do produto primeiro no MongoDB e depois no PostgreSQL.
- **Cenário 3:** Atualização do produto nos dois bancos de dados, com verificação de consistência a cada 10 milissegundos.

Em caso de falha durante as operações, são realizadas ações de rollback compensatório para tentar restaurar os dados anteriores ou remover os registros inseridos.

## Tecnologias utilizadas

- Java 27
- Spring Boot
- Maven
- PostgreSQL 16
- MongoDB 7
- Spring Data JPA
- Spring Data MongoDB
- Docker e Docker Compose

## Requisitos para execução

Antes de executar o projeto, é necessário ter instalado:

- Java 27
- Docker Desktop
- Git

O Docker Desktop deve estar iniciado para disponibilizar os bancos de dados PostgreSQL e MongoDB.

## Instalação e execução

### 1. Clonar o repositório

```bash
git clone https://github.com/rraquelof/gerenciador-produtos.git
```

### 2. Entrar na pasta do projeto

```bash
cd gerenciador-produtos
```

### 3. Iniciar os bancos de dados

Com o Docker Desktop aberto, execute:

```bash
docker compose up -d
```

Esse comando inicia os containers do PostgreSQL e MongoDB.

### 4. Configurar o MongoDB

O MongoDB utiliza um replica set chamado `rs0` para permitir as operações necessárias ao projeto.

Na primeira execução, inicialize o replica set:

```bash
docker exec -it gerenciador-mongodb mongosh
```

No terminal do MongoDB, execute:

```javascript
rs.initiate({
    _id: "rs0",
    members: [
        {
            _id: 0,
            host: "localhost:27017"
        }
    ]
})
```

Caso o replica set já esteja inicializado, não é necessário executar novamente.

Para verificar:

```javascript
rs.status()
```

### 5. Configurar a aplicação

As configurações de conexão com os bancos de dados estão no arquivo:

```text
src/main/resources/application.properties
```

Configure as credenciais do PostgreSQL e a conexão com o MongoDB conforme o ambiente de execução.

### 6. Executar a aplicação

No terminal, dentro da pasta do projeto, execute:

```bash
.\mvnw.cmd spring-boot:run
```

A aplicação será iniciada em:

```text
http://localhost:8080
```

## Endpoints

### Cenário 1 - PostgreSQL primeiro

Cadastrar um produto primeiro no PostgreSQL e depois no MongoDB.

**POST**

```text
http://localhost:8080/produtos/cenario1
```

Exemplo de corpo da requisição:

```json
{
    "nome": "Notebook",
    "descricao": "Notebook para estudos",
    "preco": 3500.00,
    "quantidadeEstoque": 10
}
```

Para simular uma falha no MongoDB:

```text
POST http://localhost:8080/produtos/cenario1?falharMongo=true
```

### Cenário 2 - MongoDB primeiro

Cadastrar um produto primeiro no MongoDB e depois no PostgreSQL.

**POST**

```text
http://localhost:8080/produtos/cenario2
```

Para simular uma falha no PostgreSQL:

```text
POST http://localhost:8080/produtos/cenario2?falharPostgres=true
```

### Cenário 3 - Atualização dos bancos

Atualizar um produto nos dois bancos de dados.

**PUT**

```text
http://localhost:8080/produtos/{id}/cenario3
```

Exemplo:

```text
http://localhost:8080/produtos/5/cenario3
```

Corpo da requisição:

```json
{
    "nome": "Notebook Atualizado",
    "descricao": "Notebook atualizado",
    "preco": 4000.00,
    "quantidadeEstoque": 15
}
```

Para simular uma falha durante a atualização:

```text
PUT http://localhost:8080/produtos/5/cenario3?falharMongo=true
```

### Consultar produtos

**GET**

```text
http://localhost:8080/produtos
```

Retorna os produtos cadastrados no PostgreSQL.

## Testes de consistência

O projeto permite testar operações de cadastro e atualização nos dois bancos de dados.

São realizados testes de:

- Cadastro com PostgreSQL primeiro.
- Cadastro com MongoDB primeiro.
- Atualização de produtos nos dois bancos.
- Simulação de falhas durante as operações.
- Rollback compensatório.
- Verificação de consistência dos dados a cada 10 milissegundos durante o período de verificação.

## Encerramento

Para interromper e remover os containers:

```bash
docker compose down
```

Para remover também os volumes e os dados armazenados nos bancos:

```bash
docker compose down -v
```

**Atenção:** o comando `docker compose down -v` apaga os dados persistidos nos volumes do PostgreSQL e MongoDB.

