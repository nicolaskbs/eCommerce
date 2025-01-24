package ecommerce.service;

import ecommerce.external.fake.EstoqueSimulado;
import ecommerce.external.fake.PagamentoSimulado;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.Cliente;
import ecommerce.entity.ItemCompra;
import ecommerce.entity.Produto;
import ecommerce.entity.TipoCliente;
import ecommerce.entity.TipoProduto;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CompraServiceTest {

    @Autowired
    private CompraService compraService;

    @ParameterizedTest
    @CsvSource({
            // peso := (item1) + (item2 == 2)
            "OURO, 98, 0", // Cliente Ouro, Peso qualquer (REGRA 1)
            "BRONZE, 2, 0", // Cliente qualquer, Peso <5 (REGRA 2)
            "PRATA, 3, 1", // Cliente Prata, Peso <10 (REGRA 3)
            "PRATA, 7, 1", // Cliente Prata, Peso <10 (REGRA 3)
            "PRATA, 8, 2", // Cliente Prata, Peso <50 (REGRA 4)
            "PRATA, 47, 2", // Cliente Prata, Peso <50 (REGRA 4)
            "PRATA, 48, 3.5", // Cliente Prata, Peso >=50 (REGRA 5)
            "PRATA, 49, 3.5", // Cliente Prata, Peso >=50 (REGRA 5)
            "BRONZE, 3, 2", // Cliente Bronze, Peso <10 (REGRA 6)
            "BRONZE, 7, 2", // Cliente Bronze, Peso <10 (REGRA 6)
            "BRONZE, 8, 4", // Cliente Bronze, Peso <50 (REGRA 7)
            "BRONZE, 47, 4", // Cliente Bronze, Peso <50 (REGRA 7)
            "BRONZE, 48, 7", // Cliente Bronze, Peso >=50 (REGRA 8)
            "BRONZE, 49, 7" // Cliente Bronze, Peso >=50 (REGRA 8)
    })
	void testeCalcularFrete(TipoCliente tipo, Integer peso, BigDecimal multiplicador) {
        // Inicializar cliente
        Cliente cliente = new Cliente(1L, "João", "Rua 1", tipo);

        // Inicializar produtos
        Produto produto1 = new Produto(2L, "Produto 1", "Descrição 1", new BigDecimal("100.00"), peso, TipoProduto.ROUPA);
        Produto produto2 = new Produto(5L, "Produto 2", "Descrição 2", new BigDecimal("100.00"), 1, TipoProduto.ROUPA);

        // Inicializar itens
        // Segundo item é necessário para cobrir arestas do loop
        ItemCompra item1 = new ItemCompra(3L, produto1, 1L);
        ItemCompra item2 = new ItemCompra(6L, produto2, 2L);
        List<ItemCompra> itens = new ArrayList<>();
        itens.add(item1);
        itens.add(item2);

        // Inicializar carrinho
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras(4L, cliente, itens, null);

		// Calcular frete
		BigDecimal frete = compraService.calcularFrete(carrinho);

        peso += 2; // Ajuste para o segundo item.
        assertTrue(frete.compareTo(multiplicador.multiply(BigDecimal.valueOf(peso))) == 0);
	}

    @ParameterizedTest
    @CsvSource({
            // valor := (item1) + (item2 == 2)
            "497.99, 1", // <=500 (REGRA 1)
            "498, 1", // <=500 (REGRA 1)
            "997.99, 0.9", // <=1000 (REGRA 2)
            "998, 0.9", // <=1000 (REGRA 2)
            "998.01, 0.8" // >1000 (REGRA 3)
    })
    void testeCalcularValorProdutos(BigDecimal valorBase, BigDecimal multiplicador) {
        // Inicializar cliente
        Cliente cliente = new Cliente(1L, "João", "Rua 1", TipoCliente.PRATA);

        // Inicializar produto
        Produto produto1 = new Produto(2L, "Produto 1", "Descrição 1", valorBase, 10, TipoProduto.ROUPA);
        Produto produto2 = new Produto(5L, "Produto 2", "Descrição 2", BigDecimal.valueOf(1), 10, TipoProduto.ROUPA);

        // Inicializar itens
        // Segundo item é necessário para cobrir arestas do loop
        ItemCompra item1 = new ItemCompra(3L, produto1, 1L);
        ItemCompra item2 = new ItemCompra(6L, produto2, 2L);
        List<ItemCompra> itens = new ArrayList<>();
        itens.add(item1);
        itens.add(item2);

        // Inicializar carrinho
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras(4L, cliente, itens, null);

        // Calcular frete
        BigDecimal valorProdutos = compraService.calcularValorProdutos(carrinho);

        valorBase = valorBase.add(BigDecimal.valueOf(2)); // Ajuste para o segundo item.
        assertTrue(valorProdutos.compareTo(multiplicador.multiply(valorBase)) == 0);
    }

    @Test
    void testeCalcularCustoTotal() {
        // Inicializar cliente
        Cliente cliente = new Cliente(1L, "João", "Rua 1", TipoCliente.OURO);

        // Inicializar produto
        Produto produto1 = new Produto(2L, "Produto 1", "Descrição 1", new BigDecimal("100.00"), 10, TipoProduto.ROUPA);

        // Inicializar item
        ItemCompra item1 = new ItemCompra(3L, produto1, 1L);
        List<ItemCompra> itens = new ArrayList<>();
        itens.add(item1);

        // Inicializar carrinho
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras(4L, cliente, itens, null);

        // Calcular resultado
        BigDecimal resultado  = compraService.calcularCustoTotal(carrinho);

        assertTrue(resultado.compareTo(new BigDecimal("100.00")) == 0);
    }

    @Test
    void testeFinalizarCompra_LancaExcecao_ItemForaDeEstoque() {

        // Inicializar cliente
        Cliente cliente = new Cliente(6L, "Bob", "rua 3", TipoCliente.BRONZE);

        // Inicializar produto
        Produto produto = new Produto(333L, "produto1", "descrição 1", new BigDecimal("700.00"), 25, TipoProduto.ELETRONICO);

        // Inicializar item
        ItemCompra item = new ItemCompra(452L, produto, 1L);
        List<ItemCompra> itens = new ArrayList<>();
        itens.add(item);

        // Inicializar carrinho
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras(200L, cliente, itens, null);

        assertThrows(IllegalStateException.class, () -> {
            new EstoqueSimulado();
            compraService.finalizarCompra(200L, 6L);
        });
    }

    @Test
    void testeFinalizarCompra_NAOLancaExcecao() {

        // Inicializar cliente
        Cliente cliente = new Cliente(6L, "Bob", "rua 3", TipoCliente.BRONZE);

        // Inicializar produto
        Produto produto = new Produto(1L, "produto1", "descrição 1", new BigDecimal("700.00"), 25, TipoProduto.ELETRONICO);

        // Inicializar item
        ItemCompra item = new ItemCompra(451L, produto, 1L);
        List<ItemCompra> itens = new ArrayList<>();
        itens.add(item);

        // Inicializar carrinho
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras(200L, cliente, itens, null);

        // Assert com banco simulado
        assertDoesNotThrow( () -> {
            new EstoqueSimulado();
            new PagamentoSimulado();
            compraService.finalizarCompra(200L, 6L);
        });
    }
}
