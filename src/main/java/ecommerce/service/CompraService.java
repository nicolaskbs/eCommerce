package ecommerce.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ecommerce.dto.CompraDTO;
import ecommerce.dto.DisponibilidadeDTO;
import ecommerce.dto.EstoqueBaixaDTO;
import ecommerce.dto.PagamentoDTO;
import ecommerce.entity.CarrinhoDeCompras;
import ecommerce.entity.Cliente;
import ecommerce.entity.ItemCompra;
import ecommerce.entity.TipoCliente;
import ecommerce.external.IEstoqueExternal;
import ecommerce.external.IPagamentoExternal;
import jakarta.transaction.Transactional;

@Service
public class CompraService {

	private final CarrinhoDeComprasService carrinhoService;
	private final ClienteService clienteService;

	private final IEstoqueExternal estoqueExternal;
	private final IPagamentoExternal pagamentoExternal;

	@Autowired
	public CompraService(CarrinhoDeComprasService carrinhoService, ClienteService clienteService,
			IEstoqueExternal estoqueExternal, IPagamentoExternal pagamentoExternal) {
		this.carrinhoService = carrinhoService;
		this.clienteService = clienteService;

		this.estoqueExternal = estoqueExternal;
		this.pagamentoExternal = pagamentoExternal;
	}

	@Transactional
	public CompraDTO finalizarCompra(Long carrinhoId, Long clienteId) {
		Cliente cliente = clienteService.buscarPorId(clienteId);
		CarrinhoDeCompras carrinho = carrinhoService.buscarPorCarrinhoIdEClienteId(carrinhoId, cliente);

		List<Long> produtosIds = carrinho.getItens().stream().map(i -> i.getProduto().getId())
				.collect(Collectors.toList());
		List<Long> produtosQtds = carrinho.getItens().stream().map(i -> i.getQuantidade())
				.collect(Collectors.toList());

		DisponibilidadeDTO disponibilidade = estoqueExternal.verificarDisponibilidade(produtosIds, produtosQtds);

		if (!disponibilidade.disponivel()) {
			throw new IllegalStateException("Itens fora de estoque.");
		}

		BigDecimal custoTotal = calcularCustoTotal(carrinho);

		PagamentoDTO pagamento = pagamentoExternal.autorizarPagamento(cliente.getId(), custoTotal.doubleValue());

		if (!pagamento.autorizado()) {
			throw new IllegalStateException("Pagamento não autorizado.");
		}

		EstoqueBaixaDTO baixaDTO = estoqueExternal.darBaixa(produtosIds, produtosQtds);

		if (!baixaDTO.sucesso()) {
			pagamentoExternal.cancelarPagamento(cliente.getId(), pagamento.transacaoId());
			throw new IllegalStateException("Erro ao dar baixa no estoque.");
		}

		return new CompraDTO(true, pagamento.transacaoId(), "Compra finalizada com sucesso.");
	}

	public BigDecimal calcularCustoTotal(CarrinhoDeCompras carrinho) {

		// Retornar o custo final com cálculo de frete e promoção
		return calcularFrete(carrinho).add(calcularValorProdutos(carrinho));
	}

	BigDecimal calcularFrete(CarrinhoDeCompras carrinho){

		// Cliente Ouro
		TipoCliente tipoCliente = carrinho.getCliente().getTipo();
		if (tipoCliente == TipoCliente.OURO) {
			return BigDecimal.ZERO;
		}

		// Calcular peso do carrinho
		Integer pesoTotal = 0;
		for (ItemCompra item : carrinho.getItens()) {
			Integer peso = (int) (item.getQuantidade() * item.getProduto().getPeso());

			// Adicionar valores ao total
			pesoTotal += peso;
		}

		// Calcular frete com base no peso
		BigDecimal valorFrete = BigDecimal.ZERO;

		if(pesoTotal < 5) {
			return BigDecimal.ZERO;
		} else if (pesoTotal < 10) {
			valorFrete = BigDecimal.valueOf(pesoTotal * 2);
		} else if (pesoTotal < 50) {
			valorFrete = BigDecimal.valueOf(pesoTotal * 4);
		} else {
			valorFrete = BigDecimal.valueOf(pesoTotal * 7);
		}

		// Cliente Prata
		if (tipoCliente == TipoCliente.PRATA) {
			return valorFrete.multiply(BigDecimal.valueOf(0.5));
		}

		// Cliente Bronze
		return valorFrete;
	}

	BigDecimal calcularValorProdutos(CarrinhoDeCompras carrinho){
		// Definir valor total
		BigDecimal valorTotal = BigDecimal.ZERO;

		// Percorrer itens do carrinho e guardar preços
		for (ItemCompra item : carrinho.getItens()) {
			BigDecimal valor = BigDecimal.valueOf(item.getQuantidade()).multiply(item.getProduto().getPreco());

			// Adicionar valores ao total
			valorTotal = valorTotal.add(valor);
		}

		// Calcular desconto com base no valor dos produtos
		if (valorTotal.compareTo(BigDecimal.valueOf(1000)) > 0) {
			// Promoção de 20%
			return valorTotal.multiply(BigDecimal.valueOf(0.8));
		} else if (valorTotal.compareTo(BigDecimal.valueOf(500)) > 0) {
			// Promoção de 10%
			return valorTotal.multiply(BigDecimal.valueOf(0.9));
		}

		// Sem promoção
		return valorTotal;
	}
}
