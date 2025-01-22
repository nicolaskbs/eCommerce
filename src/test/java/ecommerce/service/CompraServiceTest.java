package ecommerce.service;

import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.Cliente;
import ecommerce.entity.ItemCompra;
import ecommerce.entity.Produto;
import ecommerce.entity.TipoCliente;
import ecommerce.entity.TipoProduto;

public class CompraServiceTest {

    @Autowired
    private CompraService compraService;

    @Test
	void testeCalcularCustoTotal() {
        // Inicializar cliente
        Cliente cliente = new Cliente(1L, "João", "Rua 1", TipoCliente.OURO);

        // Iniciarlizar produtos
        Produto produto1 = new Produto(2L, "Produto 1", "Descrição 1", new BigDecimal("100.00"), 10, TipoProduto.ROUPA);
        Produto produto2 = new Produto(3L, "Produto 2", "Descrição 2", new BigDecimal("50.00"), 5, TipoProduto.ALIMENTO);

        // Inicializar itens
        ItemCompra item1 = new ItemCompra(2L, produto1, 5L);
        ItemCompra item2 = new ItemCompra(3L, produto2, 8L);
        List<ItemCompra> itens = new ArrayList<>();
        itens.add(item1);
        itens.add(item2);

        // Inicializar carrinho
        CarrinhoDeCompras carrinho = new CarrinhoDeCompras(4L, cliente, itens, null);

		// Calcular resultado
		BigDecimal resultado = compraService.calcularCustoTotal(carrinho);

		assertEquals(resultado, new BigDecimal("1500.00"));
	}
    
}
