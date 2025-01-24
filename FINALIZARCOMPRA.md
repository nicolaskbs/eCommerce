Ficamos na dúvida se era necessário cobrir com testes o método finalizarCompra além do calcularCustoTotal.

No final, a nossa tentativa de cobrir finalizarCompra não deu certo. Mesmo assim, decidimos deixar a nossa falha como código comentado. 

O código comentado aparece em
- src/test/java/ecommerce/service/CompraServiceTest.java
- src/main/java/ecommerce/external/fake/EstoqueSimulado.java
- src/main/java/ecommerce/external/fake/PagamentoSimulado.java

### Tabela de Dispersão para Função de Finalizar Compra

| **Condição**                 | Regra 1 | Regra 2 | Regra 3 | Regra 4 |
|------------------------------|---------|---------|---------|---------|
| Produtos Disponíveis         | F       | V       | V       | V       |
| Pagamento Autorizado         | -       | F       | V       | V       |
| Baixa no estoque com sucesso | -       | -       | F       | V       |
| **AÇÃO**                     | Regra 1 | Regra 2 | Regra 3 | Regra 4 |
| Lança Excessão               | V       | V       | V       | F       |
