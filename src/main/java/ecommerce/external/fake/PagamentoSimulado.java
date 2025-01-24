package ecommerce.external.fake;

import ecommerce.entity.Cliente;
import ecommerce.entity.Produto;
import ecommerce.entity.TipoCliente;
import ecommerce.entity.TipoProduto;
import ecommerce.service.ClienteService;
import org.springframework.stereotype.Service;

import ecommerce.dto.PagamentoDTO;
import ecommerce.external.IPagamentoExternal;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class PagamentoSimulado implements IPagamentoExternal{

	// Chave: id do cliente, valor: saldo do cliente para compra
	Map<Long, Double> pagamentoSimulado = new HashMap<>();
	public PagamentoSimulado() {
		// Inicializando o estoque simulado com alguns produtos
		Cliente cliente1 = new Cliente(1L, "Cleiton", "rua 1", TipoCliente.OURO);
		Cliente cliente2 = new Cliente(2L, "Gustavo", "rua 2", TipoCliente.PRATA);
		Cliente cliente3 = new Cliente(3L, "Bob", "rua 3", TipoCliente.BRONZE);
		pagamentoSimulado.put(1L, 1000.50);
		pagamentoSimulado.put(2L, 52.01);
		pagamentoSimulado.put(3L, 150000.99);
	}

	@Override
	public PagamentoDTO autorizarPagamento(Long id, Double valor) {

		// Lógica para gerar id da Transação
		Long idTransacao = 1234567 * id + valor.intValue();
		boolean autorizado = true;

		//
		if(valor > pagamentoSimulado.get(id)){
			autorizado = false;
		} else {
			pagamentoSimulado.put(id, pagamentoSimulado.get(id) - valor);
		}

		return new PagamentoDTO(autorizado, idTransacao);
	}

	@Override
	public void cancelarPagamento(Long id, Long valor) {
		pagamentoSimulado.put(id, pagamentoSimulado.get(id) + valor);
	}
}
