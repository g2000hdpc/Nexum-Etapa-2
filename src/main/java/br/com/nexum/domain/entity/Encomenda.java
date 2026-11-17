package br.com.nexum.domain.entity;

import br.com.nexum.domain.enums.OrigemEncomenda;
import br.com.nexum.domain.enums.StatusEncomenda;
import br.com.nexum.domain.enums.TipoAlteracao;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "encomendas")
public class Encomenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_entrega_prevista")
    private LocalDate dataEntregaPrevista;

    @Column(name = "data_entrega_real")
    private LocalDate dataEntregaReal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrigemEncomenda origem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusEncomenda status;

    @Column(length = 1000)
    private String observacoes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @OneToMany(mappedBy = "encomenda", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<ItemEncomenda> itens = new ArrayList<>();

    @OneToMany(mappedBy = "encomenda", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<EtapaProducao> etapasProducao = new ArrayList<>();

    @OneToMany(mappedBy = "encomenda", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Pagamento> pagamentos = new ArrayList<>();

    @OneToMany(mappedBy = "encomenda", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<HistoricoAlteracao> historico = new ArrayList<>();

    protected Encomenda() {
        // Construtor exigido pelo JPA.
    }

    public Encomenda(Cliente cliente, OrigemEncomenda origem, LocalDate dataEntregaPrevista, String observacoes) {
        this.cliente = Objects.requireNonNull(cliente, "O cliente da encomenda é obrigatório.");
        if (!cliente.isAtivo()) {
            throw new IllegalStateException("Não é possível abrir uma encomenda para um cliente inativo.");
        }
        this.origem = Objects.requireNonNull(origem, "A origem da encomenda é obrigatória.");
        this.dataEntregaPrevista = dataEntregaPrevista;
        this.observacoes = normalizar(observacoes);
        this.dataCriacao = LocalDateTime.now();
        this.status = StatusEncomenda.RECEBIDA;
        cliente.vincularEncomenda(this);
    }

    public void adicionarItem(ItemEncomenda item, Usuario responsavel) {
        Objects.requireNonNull(item, "O item da encomenda é obrigatório.");
        if (item.getEncomenda() != null && item.getEncomenda() != this) {
            throw new IllegalArgumentException("O item já está associado a outra encomenda.");
        }
        item.vincularEncomenda(this);
        itens.add(item);
        registrarHistorico(TipoAlteracao.ITEM, "Item adicionado à encomenda.", "itens", null,
                item.getDescricao(), responsavel);
    }

    public void adicionarEtapaProducao(EtapaProducao etapa, Usuario responsavel) {
        Objects.requireNonNull(etapa, "A etapa de produção é obrigatória.");
        if (etapa.getEncomenda() != null && etapa.getEncomenda() != this) {
            throw new IllegalArgumentException("A etapa já está associada a outra encomenda.");
        }
        etapa.vincularEncomenda(this);
        etapasProducao.add(etapa);
        registrarHistorico(TipoAlteracao.ETAPA_PRODUCAO, "Etapa de produção adicionada.", "etapasProducao",
                null, etapa.getNome(), responsavel);
    }

    public void registrarPagamento(Pagamento pagamento) {
        Objects.requireNonNull(pagamento, "O pagamento é obrigatório.");
        if (pagamento.getEncomenda() != null && pagamento.getEncomenda() != this) {
            throw new IllegalArgumentException("O pagamento já está associado a outra encomenda.");
        }
        if (pagamento.getStatus() != br.com.nexum.domain.enums.StatusPagamento.CONFIRMADO) {
            throw new IllegalStateException("Somente pagamentos confirmados podem ser registrados.");
        }
        BigDecimal novoTotalPago = getTotalPago().add(pagamento.getValor());
        if (novoTotalPago.compareTo(getValorTotal()) > 0) {
            throw new IllegalArgumentException("O pagamento ultrapassa o saldo pendente da encomenda.");
        }
        pagamento.vincularEncomenda(this);
        pagamentos.add(pagamento);
        registrarHistorico(TipoAlteracao.PAGAMENTO, "Pagamento registrado.", "pagamentos", null,
                pagamento.getValor().toPlainString(), pagamento.getResponsavel());
    }

    public void estornarPagamento(Pagamento pagamento, Usuario responsavel) {
        Objects.requireNonNull(pagamento, "O pagamento é obrigatório.");
        if (!pagamentos.contains(pagamento)) {
            throw new IllegalArgumentException("O pagamento não pertence a esta encomenda.");
        }
        if (pagamento.getStatus() == br.com.nexum.domain.enums.StatusPagamento.ESTORNADO) {
            throw new IllegalStateException("Este pagamento já foi estornado.");
        }
        pagamento.estornarInternamente();
        registrarHistorico(TipoAlteracao.PAGAMENTO, "Pagamento estornado.", "statusPagamento",
                "CONFIRMADO", "ESTORNADO", responsavel);
    }

    public void alterarStatus(StatusEncomenda novoStatus, Usuario responsavel) {
        Objects.requireNonNull(novoStatus, "O novo status é obrigatório.");
        if (novoStatus == status) return;
        if (!transicaoPermitida(status, novoStatus)) {
            throw new IllegalStateException("Transição de status não permitida: " + status + " -> " + novoStatus);
        }

        StatusEncomenda statusAnterior = this.status;
        this.status = novoStatus;
        if (novoStatus == StatusEncomenda.ENTREGUE) {
            this.dataEntregaReal = LocalDate.now();
        }
        registrarHistorico(TipoAlteracao.STATUS, "Status da encomenda alterado.", "status",
                statusAnterior.name(), novoStatus.name(), responsavel);
    }

    public void atualizarPrazoEntrega(LocalDate novoPrazo, Usuario responsavel) {
        LocalDate prazoAnterior = this.dataEntregaPrevista;
        this.dataEntregaPrevista = novoPrazo;
        registrarHistorico(TipoAlteracao.PRAZO, "Prazo de entrega atualizado.", "dataEntregaPrevista",
                String.valueOf(prazoAnterior), String.valueOf(novoPrazo), responsavel);
    }

    public void atualizarObservacoes(String novasObservacoes, Usuario responsavel) {
        String observacoesAnteriores = this.observacoes;
        this.observacoes = normalizar(novasObservacoes);
        registrarHistorico(TipoAlteracao.DADOS_ENCOMENDA, "Observações da encomenda atualizadas.",
                "observacoes", observacoesAnteriores, this.observacoes, responsavel);
    }

    public void registrarSolicitacaoDeAlteracao(String descricao, Usuario responsavel) {
        registrarHistorico(TipoAlteracao.SOLICITACAO_CLIENTE, descricao, null, null, null, responsavel);
    }

    void registrarEventoItem(String descricao, String campo, String valorAnterior, String valorNovo) {
        registrarHistorico(TipoAlteracao.ITEM, descricao, campo, valorAnterior, valorNovo, null);
    }

    void registrarEventoPersonalizacao(String descricao, String valorAnterior, String valorNovo) {
        registrarHistorico(TipoAlteracao.PERSONALIZACAO, descricao, "personalizacao", valorAnterior, valorNovo, null);
    }

    void registrarEventoEtapa(String descricao, String campo, String valorAnterior, String valorNovo, Usuario responsavel) {
        registrarHistorico(TipoAlteracao.ETAPA_PRODUCAO, descricao, campo, valorAnterior, valorNovo, responsavel);
    }

    private void registrarHistorico(TipoAlteracao tipo, String descricao, String campo,
                                    String valorAnterior, String valorNovo, Usuario responsavel) {
        HistoricoAlteracao registro = HistoricoAlteracao.registrar(
                tipo, descricao, campo, valorAnterior, valorNovo, responsavel);
        registro.vincularEncomenda(this);
        historico.add(registro);
    }

    private boolean transicaoPermitida(StatusEncomenda atual, StatusEncomenda proximo) {
        return switch (atual) {
            case RECEBIDA -> proximo == StatusEncomenda.EM_PRODUCAO || proximo == StatusEncomenda.CANCELADA;
            case EM_PRODUCAO -> proximo == StatusEncomenda.PRONTA || proximo == StatusEncomenda.CANCELADA;
            case PRONTA -> proximo == StatusEncomenda.EM_ENTREGA
                    || proximo == StatusEncomenda.ENTREGUE || proximo == StatusEncomenda.CANCELADA;
            case EM_ENTREGA -> proximo == StatusEncomenda.ENTREGUE;
            case ENTREGUE, CANCELADA -> false;
        };
    }

    private static String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    public BigDecimal getValorTotal() {
        return itens.stream()
                .map(ItemEncomenda::calcularSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalPago() {
        return pagamentos.stream()
                .filter(pagamento -> pagamento.getStatus() == br.com.nexum.domain.enums.StatusPagamento.CONFIRMADO)
                .map(Pagamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getSaldoPendente() {
        BigDecimal saldo = getValorTotal().subtract(getTotalPago());
        return saldo.signum() < 0 ? BigDecimal.ZERO : saldo;
    }

    public Long getId() { return id; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public LocalDate getDataEntregaPrevista() { return dataEntregaPrevista; }
    public LocalDate getDataEntregaReal() { return dataEntregaReal; }
    public OrigemEncomenda getOrigem() { return origem; }
    public StatusEncomenda getStatus() { return status; }
    public String getObservacoes() { return observacoes; }
    public Cliente getCliente() { return cliente; }
    public List<ItemEncomenda> getItens() { return Collections.unmodifiableList(itens); }
    public List<EtapaProducao> getEtapasProducao() { return Collections.unmodifiableList(etapasProducao); }
    public List<Pagamento> getPagamentos() { return Collections.unmodifiableList(pagamentos); }
    public List<HistoricoAlteracao> getHistorico() { return Collections.unmodifiableList(historico); }
}
