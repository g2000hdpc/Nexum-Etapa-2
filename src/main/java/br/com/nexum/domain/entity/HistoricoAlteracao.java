package br.com.nexum.domain.entity;

import br.com.nexum.domain.enums.TipoAlteracao;
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

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "historicos_alteracao")
public class HistoricoAlteracao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoAlteracao tipo;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Column(name = "campo_alterado", length = 80)
    private String campoAlterado;

    @Column(name = "valor_anterior", length = 500)
    private String valorAnterior;

    @Column(name = "valor_novo", length = 500)
    private String valorNovo;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encomenda_id", nullable = false)
    private Encomenda encomenda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsavel_id")
    private Usuario responsavel;

    protected HistoricoAlteracao() {
        // Construtor exigido pelo JPA.
    }

    private HistoricoAlteracao(TipoAlteracao tipo, String descricao, String campoAlterado,
                              String valorAnterior, String valorNovo, Usuario responsavel) {
        this.tipo = Objects.requireNonNull(tipo, "O tipo da alteração é obrigatório.");
        this.descricao = textoObrigatorio(descricao, "A descrição da alteração é obrigatória.");
        this.campoAlterado = normalizar(campoAlterado);
        this.valorAnterior = normalizar(valorAnterior);
        this.valorNovo = normalizar(valorNovo);
        this.dataHora = LocalDateTime.now();
        this.responsavel = responsavel;
        if (responsavel != null) {
            responsavel.vincularHistorico(this);
        }
    }

    static HistoricoAlteracao registrar(TipoAlteracao tipo, String descricao, String campoAlterado,
                                        String valorAnterior, String valorNovo, Usuario responsavel) {
        return new HistoricoAlteracao(tipo, descricao, campoAlterado, valorAnterior, valorNovo, responsavel);
    }

    void vincularEncomenda(Encomenda encomenda) {
        this.encomenda = Objects.requireNonNull(encomenda, "A encomenda do histórico é obrigatória.");
    }

    private static String textoObrigatorio(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException(mensagem);
        return valor.trim();
    }

    private static String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    public Long getId() { return id; }
    public TipoAlteracao getTipo() { return tipo; }
    public String getDescricao() { return descricao; }
    public String getCampoAlterado() { return campoAlterado; }
    public String getValorAnterior() { return valorAnterior; }
    public String getValorNovo() { return valorNovo; }
    public LocalDateTime getDataHora() { return dataHora; }
    public Encomenda getEncomenda() { return encomenda; }
    public Usuario getResponsavel() { return responsavel; }
}
