package br.com.nexum.domain.entity;

import br.com.nexum.domain.enums.StatusEtapaProducao;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "etapas_producao")
public class EtapaProducao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Column(nullable = false)
    private Integer ordem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private StatusEtapaProducao status;

    @Column(name = "data_inicio_prevista")
    private LocalDate dataInicioPrevista;

    @Column(name = "data_fim_prevista")
    private LocalDate dataFimPrevista;

    @Column(name = "data_inicio_real")
    private LocalDateTime dataInicioReal;

    @Column(name = "data_fim_real")
    private LocalDateTime dataFimReal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encomenda_id", nullable = false)
    private Encomenda encomenda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id")
    private Usuario responsavel;

    protected EtapaProducao() {
        // Construtor exigido pelo JPA.
    }

    public EtapaProducao(String nome, String descricao, Integer ordem,
                         LocalDate dataInicioPrevista, LocalDate dataFimPrevista) {
        this.nome = textoObrigatorio(nome, "O nome da etapa é obrigatório.");
        this.descricao = normalizar(descricao);
        if (ordem == null || ordem <= 0) {
            throw new IllegalArgumentException("A ordem da etapa deve ser maior que zero.");
        }
        validarPeriodo(dataInicioPrevista, dataFimPrevista);
        this.ordem = ordem;
        this.dataInicioPrevista = dataInicioPrevista;
        this.dataFimPrevista = dataFimPrevista;
        this.status = StatusEtapaProducao.PENDENTE;
    }

    public void atribuirResponsavel(Usuario novoResponsavel) {
        if (novoResponsavel != null && !novoResponsavel.isAtivo()) {
            throw new IllegalStateException("Não é possível atribuir uma etapa a um funcionário inativo.");
        }
        String anterior = responsavel == null ? null : responsavel.getNome();
        Usuario responsavelAnterior = this.responsavel;
        if (responsavelAnterior != null) {
            responsavelAnterior.desvincularEtapa(this);
        }
        this.responsavel = novoResponsavel;
        if (novoResponsavel != null) {
            novoResponsavel.vincularEtapa(this);
        }
        registrarEvento("Responsável da etapa alterado.", "responsavel", anterior,
                novoResponsavel == null ? null : novoResponsavel.getNome(), novoResponsavel);
    }

    public void iniciar() {
        if (status != StatusEtapaProducao.PENDENTE && status != StatusEtapaProducao.PAUSADA) {
            throw new IllegalStateException("Somente etapas pendentes ou pausadas podem ser iniciadas.");
        }
        StatusEtapaProducao anterior = status;
        status = StatusEtapaProducao.EM_ANDAMENTO;
        if (dataInicioReal == null) dataInicioReal = LocalDateTime.now();
        registrarEvento("Etapa de produção iniciada.", "status", anterior.name(), status.name(), responsavel);
    }

    public void pausar() {
        if (status != StatusEtapaProducao.EM_ANDAMENTO) {
            throw new IllegalStateException("Somente uma etapa em andamento pode ser pausada.");
        }
        StatusEtapaProducao anterior = status;
        status = StatusEtapaProducao.PAUSADA;
        registrarEvento("Etapa de produção pausada.", "status", anterior.name(), status.name(), responsavel);
    }

    public void concluir() {
        if (status != StatusEtapaProducao.EM_ANDAMENTO) {
            throw new IllegalStateException("Somente uma etapa em andamento pode ser concluída.");
        }
        StatusEtapaProducao anterior = status;
        status = StatusEtapaProducao.CONCLUIDA;
        dataFimReal = LocalDateTime.now();
        registrarEvento("Etapa de produção concluída.", "status", anterior.name(), status.name(), responsavel);
    }

    public void reprogramar(LocalDate novoInicio, LocalDate novoFim) {
        validarPeriodo(novoInicio, novoFim);
        String anterior = dataInicioPrevista + " / " + dataFimPrevista;
        dataInicioPrevista = novoInicio;
        dataFimPrevista = novoFim;
        registrarEvento("Datas previstas da etapa reprogramadas.", "periodoPrevisto", anterior,
                novoInicio + " / " + novoFim, responsavel);
    }

    void vincularEncomenda(Encomenda encomenda) {
        this.encomenda = Objects.requireNonNull(encomenda, "A encomenda da etapa é obrigatória.");
    }

    private void registrarEvento(String descricaoEvento, String campo, String anterior,
                                 String novo, Usuario usuarioResponsavel) {
        if (encomenda != null) {
            encomenda.registrarEventoEtapa(descricaoEvento, campo, anterior, novo, usuarioResponsavel);
        }
    }

    private static void validarPeriodo(LocalDate inicio, LocalDate fim) {
        if (inicio != null && fim != null && fim.isBefore(inicio)) {
            throw new IllegalArgumentException("A data final prevista não pode anteceder a data inicial.");
        }
    }

    private static String textoObrigatorio(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException(mensagem);
        return valor.trim();
    }

    private static String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public Integer getOrdem() { return ordem; }
    public StatusEtapaProducao getStatus() { return status; }
    public LocalDate getDataInicioPrevista() { return dataInicioPrevista; }
    public LocalDate getDataFimPrevista() { return dataFimPrevista; }
    public LocalDateTime getDataInicioReal() { return dataInicioReal; }
    public LocalDateTime getDataFimReal() { return dataFimReal; }
    public Encomenda getEncomenda() { return encomenda; }
    public Usuario getResponsavel() { return responsavel; }
}
