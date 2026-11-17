package br.com.nexum.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "itens_encomenda")
public class ItemEncomenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String descricao;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "valor_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorUnitario;

    @Column(length = 500)
    private String observacoes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encomenda_id", nullable = false)
    private Encomenda encomenda;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Personalizacao> personalizacoes = new ArrayList<>();

    protected ItemEncomenda() {
        // Construtor exigido pelo JPA.
    }

    public ItemEncomenda(String descricao, Integer quantidade, BigDecimal valorUnitario, String observacoes) {
        this.descricao = textoObrigatorio(descricao, "A descrição do item é obrigatória.");
        alterarQuantidade(quantidade);
        alterarValorUnitario(valorUnitario);
        this.observacoes = normalizar(observacoes);
    }

    public void alterarDescricao(String novaDescricao) {
        String anterior = this.descricao;
        this.descricao = textoObrigatorio(novaDescricao, "A descrição do item é obrigatória.");
        registrarEvento("Descrição do item alterada.", "descricao", anterior, this.descricao);
    }

    private void alterarQuantidade(Integer novaQuantidade) {
        if (novaQuantidade == null || novaQuantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        this.quantidade = novaQuantidade;
    }

    public void atualizarQuantidade(Integer novaQuantidade) {
        Integer anterior = this.quantidade;
        alterarQuantidade(novaQuantidade);
        registrarEvento("Quantidade do item alterada.", "quantidade", String.valueOf(anterior), String.valueOf(novaQuantidade));
    }

    private void alterarValorUnitario(BigDecimal novoValor) {
        if (novoValor == null || novoValor.signum() < 0) {
            throw new IllegalArgumentException("O valor unitário não pode ser negativo.");
        }
        this.valorUnitario = novoValor;
    }

    public void atualizarValorUnitario(BigDecimal novoValor) {
        BigDecimal anterior = this.valorUnitario;
        alterarValorUnitario(novoValor);
        registrarEvento("Valor unitário do item alterado.", "valorUnitario", String.valueOf(anterior), String.valueOf(novoValor));
    }

    public void atualizarObservacoes(String novasObservacoes) {
        String anterior = this.observacoes;
        this.observacoes = normalizar(novasObservacoes);
        registrarEvento("Observações do item atualizadas.", "observacoes", anterior, this.observacoes);
    }

    public void adicionarPersonalizacao(Personalizacao personalizacao) {
        Objects.requireNonNull(personalizacao, "A personalização é obrigatória.");
        if (personalizacao.getItem() != null && personalizacao.getItem() != this) {
            throw new IllegalArgumentException("A personalização já pertence a outro item.");
        }
        personalizacao.vincularItem(this);
        personalizacoes.add(personalizacao);
        registrarEvento("Personalização adicionada ao item.", "personalizacoes", null, personalizacao.getDescricao());
    }

    void vincularEncomenda(Encomenda encomenda) {
        this.encomenda = Objects.requireNonNull(encomenda, "A encomenda é obrigatória.");
    }

    Encomenda getEncomendaInterna() { return encomenda; }

    private void registrarEvento(String descricaoEvento, String campo, String anterior, String novo) {
        if (encomenda != null) {
            encomenda.registrarEventoItem(descricaoEvento, campo, anterior, novo);
        }
    }

    public BigDecimal calcularSubtotal() {
        return valorUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    private static String textoObrigatorio(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException(mensagem);
        return valor.trim();
    }

    private static String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    public Long getId() { return id; }
    public String getDescricao() { return descricao; }
    public Integer getQuantidade() { return quantidade; }
    public BigDecimal getValorUnitario() { return valorUnitario; }
    public String getObservacoes() { return observacoes; }
    public Encomenda getEncomenda() { return encomenda; }
    public List<Personalizacao> getPersonalizacoes() { return Collections.unmodifiableList(personalizacoes); }
}
