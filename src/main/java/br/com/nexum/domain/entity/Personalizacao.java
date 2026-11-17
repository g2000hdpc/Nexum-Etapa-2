package br.com.nexum.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "personalizacoes")
public class Personalizacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String tipo;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Column(name = "data_solicitacao", nullable = false, updatable = false)
    private LocalDateTime dataSolicitacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private ItemEncomenda item;

    protected Personalizacao() {
        // Construtor exigido pelo JPA.
    }

    public Personalizacao(String tipo, String descricao) {
        this.tipo = textoObrigatorio(tipo, "O tipo da personalização é obrigatório.");
        this.descricao = textoObrigatorio(descricao, "A descrição da personalização é obrigatória.");
        this.dataSolicitacao = LocalDateTime.now();
    }

    public void alterarDescricao(String novaDescricao) {
        String anterior = this.descricao;
        this.descricao = textoObrigatorio(novaDescricao, "A descrição da personalização é obrigatória.");
        if (item != null && item.getEncomenda() != null) {
            item.getEncomenda().registrarEventoPersonalizacao(
                    "Descrição de personalização alterada.", anterior, this.descricao);
        }
    }

    public void alterarTipo(String novoTipo) {
        String anterior = this.tipo;
        this.tipo = textoObrigatorio(novoTipo, "O tipo da personalização é obrigatório.");
        if (item != null && item.getEncomenda() != null) {
            item.getEncomenda().registrarEventoPersonalizacao(
                    "Tipo de personalização alterado.", anterior, this.tipo);
        }
    }

    void vincularItem(ItemEncomenda item) {
        this.item = Objects.requireNonNull(item, "O item da personalização é obrigatório.");
    }

    private static String textoObrigatorio(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException(mensagem);
        return valor.trim();
    }

    public Long getId() { return id; }
    public String getTipo() { return tipo; }
    public String getDescricao() { return descricao; }
    public LocalDateTime getDataSolicitacao() { return dataSolicitacao; }
    public ItemEncomenda getItem() { return item; }
}
