package ecommerce.external.fake;

import org.springframework.stereotype.Service;

import ecommerce.dto.PagamentoDTO;
import ecommerce.external.IPagamentoExternal;

@Service
public class PagamentoSimulado implements IPagamentoExternal{

    @Override
	public PagamentoDTO autorizarPagamento(Long id, Double valor) {
		// Implement the method logic here
        throw new UnsupportedOperationException("Unimplemented method 'autorizarPagamento'");
	}

	@Override
	public void cancelarPagamento(Long id, Long valor) {
		// Implement the method logic here
        throw new UnsupportedOperationException("Unimplemented method 'cancelarPagamento'");
	}
}
