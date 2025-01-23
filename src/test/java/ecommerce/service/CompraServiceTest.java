package ecommerce.service;

import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
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

@SpringBootTest
public class CompraServiceTest {

    @Autowired
    private CompraService compraService;

    @ParameterizedTest
    @CsvSource({
            "OURO, 100, 0", // Cliente Ouro, Peso qualquer (REGRA 1)
            "BRONZE, 4, 0", // Cliente qualquer, Peso <5 (REGRA 2)
            "PRATA, 5, 1", // Cliente Prata, Peso <10 (REGRA 3)
            "PRATA, 9, 1", // Cliente Prata, Peso <10 (REGRA 3)
            "PRATA, 10, 2", // Cliente Prata, Peso <50 (REGRA 4)
            "PRATA, 49, 2", // Cliente Prata, Peso <50 (REGRA 4)
            "PRATA, 50, 3.5", // Cliente Prata, Peso >=50 (REGRA 5)
            "PRATA, 51, 3.5", // Cliente Prata, Peso >=50 (REGRA 5)
            "BRONZE, 5, 2", // Cliente Bronze, Peso <10 (REGRA 6)
            "BRONZE, 9, 2", // Cliente Bronze, Peso <10 (REGRA 6)
            "BRONZE, 10, 4", // Cliente Bronze, Peso <50 (REGRA 7)
            "BRONZE, 49, 4", // Cliente Bronze, Peso <50 (REGRA 7)
            "BRONZE, 50, 7", // Cliente Bronze, Peso >=50 (REGRA 8)
            "BRONZE, 51, 7" // Cliente Bronze, Peso >=50 (REGRA 8)
    })
	void testeCalcularFrete(TipoCliente tipo, Integer peso, BigDecimal multiplicador) {
        // Inicializar cliente
        Cliente cliente = new Cliente(1L, "João", "Rua 1", tipo);

        // Inicializar produto
        Produto produto1 = new Produto(2L, "Produto 1", "Descrição 1", new BigDecimal("100.00"), peso, TipoProduto.ROUPA);

        // Inicializar itens
        ItemCompra item1 = new ItemCompra(3L, produto1, 1L);
        List<ItemCompra> itens = new ArrayList<>();
        itens.add(item1);

        // Inicializar carrinho
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras(4L, cliente, itens, null);

		// Calcular frete
		BigDecimal frete = compraService.calcularFrete(carrinho);

        assertTrue(frete.compareTo(multiplicador.multiply(BigDecimal.valueOf(peso))) == 0);
	}

    @ParameterizedTest
    @CsvSource({
            "499.99, 1", // <=500 (REGRA 1)
            "500, 1", // <=500 (REGRA 1)
            "999.99, 0.9", // <=1000 (REGRA 2)
            "1000, 0.9", // <=1000 (REGRA 2)
            "1000.01, 0.8" // >1000 (REGRA 3)
    })
    void testeCalcularValorProdutos(BigDecimal valorBase, BigDecimal multiplicador) {
        // Inicializar cliente
        Cliente cliente = new Cliente(1L, "João", "Rua 1", TipoCliente.PRATA);

        // Inicializar produto
        Produto produto1 = new Produto(2L, "Produto 1", "Descrição 1", valorBase, 10, TipoProduto.ROUPA);

        // Inicializar itens
        ItemCompra item1 = new ItemCompra(3L, produto1, 1L);
        List<ItemCompra> itens = new ArrayList<>();
        itens.add(item1);

        // Inicializar carrinho
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras(4L, cliente, itens, null);

        // Calcular frete
        BigDecimal valorProdutos = compraService.calcularValorProdutos(carrinho);

        assertEquals(valorProdutos, valorBase.multiply(multiplicador));
        assertTrue(valorProdutos.compareTo(multiplicador.multiply(valorBase)) == 0);
    }
}
