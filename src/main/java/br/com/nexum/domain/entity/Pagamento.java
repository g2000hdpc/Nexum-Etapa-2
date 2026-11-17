package br.com.nexum.domain.entity;

import br.com.nexum.domain.enums.FormaPagamento;
import br.com.nexum.domain.enums.StatusPagamento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "pagamentos")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false, length = 30)
    private FormaPagamento formaPagamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPagamento status;

    @Column(name = "data_pagamento", nullable = false)
    private LocalDateTime dataPagamento;

    @Column(length = 300)
    private String observacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encomenda_id", nullable = false)
    private Encomenda encomenda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id")
    private Usuario responsavel;

    protected Pagamento() {
        // Construtor exigido pelo JPA.
    }

    public Pagamento(BigDecimal valor, FormaPagamento formaPagamento,
                     LocalDateTime dataPagamento, String observacao, Usuario responsavel) {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("O valor do pagamento deve ser maior que zero.");
        }
        this.valor = valor;
        this.formaPagamento = Objects.requireNonNull(formaPagamento, "A forma de pagamento é obrigatória.");
        this.dataPagamento = dataPagamento == null ? LocalDateTime.now() : dataPagamento;
        this.observacao = observacao == null || observacao.isBlank() ? null : observacao.trim();
        this.responsavel = responsavel;
        if (responsavel != null) {
            responsavel.vincularPagamento(this);
        }
        this.status = StatusPagamento.CONFIRMADO;
    }

    void vincularEncomenda(Encomenda encomenda) {
        this.encomenda = Objects.requireNonNull(encomenda, "A encomenda do pagamento é obrigatória.");
    }

    void estornarInternamente() {
        if (status != StatusPagamento.CONFIRMADO) {
            throw new IllegalStateException("Somente pagamentos confirmados podem ser estornados.");
        }
        status = StatusPagamento.ESTORNADO;
    }

    public Long getId() { return id; }
    public BigDecimal getValor() { return valor; }
    public FormaPagamento getFormaPagamento() { return formaPagamento; }
    public StatusPagamento getStatus() { return status; }
    public LocalDateTime getDataPagamento() { return dataPagamento; }
    public String getObservacao() { return observacao; }
    public Encomenda getEncomenda() { return encomenda; }
    public Usuario getResponsavel() { return responsavel; }
}
