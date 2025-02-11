Para executar os testes, execute no terminal:
- mvn test

Para gerar o relatório de cobertura de arestas usamos o intelliJ onde executamos os testes normais, no temrinal "mvn test", e depois usamos a opção da própria IDE de gerar relatorio de cobertura.

A partir desse relatório gerado conseguimos verificar que cobrimos todas as arestas.

Para gerar o relatório de mutantes, basta executar o comando "mvn test-compile org.pitest:pitest-maven:mutationCoverage" na raiz do projeto, depois que finalizar a execução sem erros os arquivos estarão na pasta target/pit-reports

A partir desses arquivos gerados é possivel verificar o arquivo "target/pit-reports/ecommerce.service/CompraService.java.html", o mesmo possui a informação de quis mutações foram matadas, nessas informações temos a que para a função de calcularCustoTotal todos os mutantes relevantes foram finalizados.

# Regras de Cálculo de Frete

### - Tipo de Cliente

- OURO

  NÃO paga frete


- PRATA

  Paga METADE do valor base do frete


- BRONZE

  Paga frete INTEGRAL

### Cálculo valor base do frete:

- (Peso Total do Carrinho) < 5kg

  NÃO paga frete


- 5kg ≤ (Peso Total do Carrinho) < 10kg

  (Valor Base do Frete) := (Peso Total do Carrinho) * 2


- 10kg ≤ (Peso Total do Carrinho) < 50kg

  (Valor Base do Frete) := (Peso Total do Carrinho) * 4


- (Peso Total do Carrinho) ≥ 50kg

  (Valor Base do Frete) := (Peso Total do Carrinho) * 7

# Regras de Cálculo da Promoção (não considera frete):

- (Valor total dos produtos) ≤ 500

  SEM promoção


- 500 < (Valor total dos produtos) ≤ 1000

  Promoção de 10%


- (Valor total dos produtos) > 1000

  Promoção de 20%






### Tabela de Dispersão para Custo de Frete

| **Condição**               | Regra 1 | Regra 2 | Regra 3 | Regra 4 | Regra 5 | Regra 6 | Regra 7 | Regra 8 |
|----------------------------|---------|---------|---------|---------|---------|---------|---------|---------|
| Tipo {OURO; PRATA; BRONZE} | OURO    | -       | PRATA   | PRATA   | PRATA   | BRONZE  | BRONZE  | BRONZE  |
| Peso {<5; <10; <50; ≥50}   | -       | <5      | <10     | <50     | ≥50     | <10     | <50     | ≥50     |
| **Multiplicador Frete**    | Regra 1 | Regra 2 | Regra 3 | Regra 4 | Regra 5 | Regra 6 | Regra 7 | Regra 8 |
| {0; 1; 2; 3.5; 4; 7}       | 0       | 0       | 1       | 2       | 3.5     | 2       | 4       | 7       |


### Tabela de Dispersão para Promoção sobre Produtos

| **Condição**                        | Regra 1 | Regra 2 | Regra 3 |
|-------------------------------------|---------|---------|---------|
| Valor Produtos {≤500; ≤1000; >1000} | ≤500    | ≤1000   | >1000   |
| **Multiplicador Promoção**          | Regra 1 | Regra 2 | Regra 3 |
| {0.8; 0.9; 1}                       | 1       | 0.9     | 0.8     |