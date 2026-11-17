package br.com.nexum.domain.entity;

import br.com.nexum.domain.enums.PerfilAcesso;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "senha_hash", nullable = false, length = 255)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PerfilAcesso perfil;

    @Column(nullable = false)
    private boolean ativo;

    @OneToMany(mappedBy = "responsavel", fetch = FetchType.LAZY)
    private List<HistoricoAlteracao> alteracoesRegistradas = new ArrayList<>();

    @OneToMany(mappedBy = "responsavel", fetch = FetchType.LAZY)
    private List<Pagamento> pagamentosRegistrados = new ArrayList<>();

    @OneToMany(mappedBy = "responsavel", fetch = FetchType.LAZY)
    private List<EtapaProducao> etapasAtribuidas = new ArrayList<>();

    protected Usuario() {
        // Construtor exigido pelo JPA.
    }

    public Usuario(String nome, String email, String senhaHash, PerfilAcesso perfil) {
        this.nome = textoObrigatorio(nome, "O nome do funcionário é obrigatório.");
        this.email = textoObrigatorio(email, "O e-mail do funcionário é obrigatório.").toLowerCase();
        this.senhaHash = textoObrigatorio(senhaHash, "A senha deve ser armazenada como hash.");
        this.perfil = Objects.requireNonNull(perfil, "O perfil de acesso é obrigatório.");
        this.ativo = true;
    }

    public void alterarNome(String novoNome) {
        this.nome = textoObrigatorio(novoNome, "O nome do funcionário é obrigatório.");
    }

    public void alterarEmail(String novoEmail) {
        this.email = textoObrigatorio(novoEmail, "O e-mail do funcionário é obrigatório.").toLowerCase();
    }

    public void alterarSenhaHash(String novoHash) {
        this.senhaHash = textoObrigatorio(novoHash, "A senha deve ser armazenada como hash.");
    }

    public void alterarPerfil(PerfilAcesso novoPerfil) {
        this.perfil = Objects.requireNonNull(novoPerfil, "O perfil de acesso é obrigatório.");
    }

    public void desativar() { this.ativo = false; }
    public void reativar() { this.ativo = true; }

    void vincularHistorico(HistoricoAlteracao historico) {
        if (historico != null && !alteracoesRegistradas.contains(historico)) {
            alteracoesRegistradas.add(historico);
        }
    }

    void vincularPagamento(Pagamento pagamento) {
        if (pagamento != null && !pagamentosRegistrados.contains(pagamento)) {
            pagamentosRegistrados.add(pagamento);
        }
    }

    void vincularEtapa(EtapaProducao etapa) {
        if (etapa != null && !etapasAtribuidas.contains(etapa)) {
            etapasAtribuidas.add(etapa);
        }
    }

    void desvincularEtapa(EtapaProducao etapa) {
        etapasAtribuidas.remove(etapa);
    }

    private static String textoObrigatorio(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensagem);
        }
        return valor.trim();
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getSenhaHash() { return senhaHash; }
    public PerfilAcesso getPerfil() { return perfil; }
    public boolean isAtivo() { return ativo; }
    public List<HistoricoAlteracao> getAlteracoesRegistradas() { return Collections.unmodifiableList(alteracoesRegistradas); }
    public List<Pagamento> getPagamentosRegistrados() { return Collections.unmodifiableList(pagamentosRegistrados); }
    public List<EtapaProducao> getEtapasAtribuidas() { return Collections.unmodifiableList(etapasAtribuidas); }
}
