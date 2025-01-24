package ecommerce.external.fake;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ecommerce.entity.Produto;
import ecommerce.entity.TipoProduto;
import org.springframework.stereotype.Service;

import ecommerce.dto.DisponibilidadeDTO;
import ecommerce.dto.EstoqueBaixaDTO;
import ecommerce.external.IEstoqueExternal;

@Service
public class EstoqueSimulado implements IEstoqueExternal {

    // Criando Estoque para simulação
    // Chave: id do cliente, valor: quantidade de produtos
    Map<Long, Long> estoqueSimulado = new HashMap<>();
    public EstoqueSimulado() {
        // Inicializando o estoque simulado com alguns produtos
        Produto produto1 = new Produto(1L, "produto1", "descrição 1", new BigDecimal("700.00"), 25, TipoProduto.ELETRONICO);
        Produto produto2 = new Produto(2L, "produto2", "descrição 2", new BigDecimal("125.25"), 2, TipoProduto.LIVRO);
        Produto produto3 = new Produto(3L, "produto3", "descrição 3", new BigDecimal("3.50"), 1, TipoProduto.ALIMENTO);

        estoqueSimulado.put(1L, 10L);
        estoqueSimulado.put(2L, 570L);
        estoqueSimulado.put(3L, 44L);
    }

    @Override
    public EstoqueBaixaDTO darBaixa(List<Long> produtosIds, List<Long> produtosQuantidades) {

        boolean sucesso = true;

        for(int i = 0; i < produtosIds.size(); i++){
            Long produtoId = produtosIds.get(i);
            Long quantidade = produtosQuantidades.get(i);
            if(estoqueSimulado.containsKey(produtoId)){
                if(quantidade <= estoqueSimulado.get(produtoId)){
                    estoqueSimulado.put(produtoId, estoqueSimulado.get(produtoId) - quantidade);
                } else {
                    sucesso = false;
                }
            } else {
                sucesso = false;
            }
        }
        return new EstoqueBaixaDTO(sucesso);
    }

    @Override
    public DisponibilidadeDTO verificarDisponibilidade(List<Long> produtosIds, List<Long> produtosQuantidades) {

        // Inicialmente todos os produtos estão disponíveis
        boolean disponivel = true;
        List<Long> idsProdutosIndisponiveis = new ArrayList<>();

        // Guardando produtos indisponíveis
        for(int i = 0; i < produtosIds.size(); i++){
            if(estoqueSimulado.containsKey(produtosIds.get(i))){
                if(produtosQuantidades.get(i) > estoqueSimulado.get(i)){
                    disponivel = false;
                    idsProdutosIndisponiveis.add(produtosIds.get(i));
                }
            } else {
                disponivel = false;
                idsProdutosIndisponiveis.add(produtosIds.get(i));
            }
        }

        return new DisponibilidadeDTO(disponivel, idsProdutosIndisponiveis);
    }
}
