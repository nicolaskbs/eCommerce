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
		List<Long> produtosQtds = carrinho.getItens().stream().map(i -> i.getQuantidade()).collect(Collectors.toList());

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

		CompraDTO compraDTO = new CompraDTO(true, pagamento.transacaoId(), "Compra finalizada com sucesso.");

		return compraDTO;
	}

	public BigDecimal calcularCustoTotal(CarrinhoDeCompras carrinho) {
		// Definir valor total
		BigDecimal valorTotal = BigDecimal.ZERO;
		Integer pesoTotal = 0;

		// Percorrer itens do carrinho
		for (ItemCompra item : carrinho.getItens()) {
			// Calcular valores
			BigDecimal valor = BigDecimal.valueOf(item.getQuantidade()).multiply(item.getProduto().getPreco());
			Integer peso = (int) (item.getQuantidade() * item.getProduto().getPeso());
			
			// Adicionar valores ao total
			valorTotal = valorTotal.add(valor);
			pesoTotal += peso;
		}

		// Calcular frete com base no peso
		Integer valorFrete = 0;

		if(pesoTotal < 5) {
			valorFrete = 0;
		} else if (pesoTotal < 10) {
			valorFrete = pesoTotal * 2;
		} else if (pesoTotal < 50) {
			valorFrete = pesoTotal * 4;
		} else {
			valorFrete = pesoTotal * 7;
		}

		// Calcular frete para tipo de clientes
		TipoCliente tipoCliente = carrinho.getCliente().getTipo();

		if (tipoCliente == TipoCliente.OURO) {
			valorFrete = 0;
		} else if (tipoCliente == TipoCliente.PRATA) {
			valorFrete = (int) (valorFrete * 0.5);
		}

		// Calcular desconto com base no valor total
		if (valorTotal.compareTo(BigDecimal.valueOf(1000)) > 0) {
			valorTotal = valorTotal.multiply(BigDecimal.valueOf(0.8));
		} else if (valorTotal.compareTo(BigDecimal.valueOf(500)) > 0) {
			valorTotal = valorTotal.multiply(BigDecimal.valueOf(0.9));
		}

		// Retornar o valor total
		return valorTotal;
	}
}
