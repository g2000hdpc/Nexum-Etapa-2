package br.com.nexum.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 25)
    private String telefone;

    @Column(length = 120)
    private String email;

    @Column(length = 255)
    private String endereco;

    @Column(nullable = false)
    private boolean ativo;

    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
    private List<Encomenda> encomendas = new ArrayList<>();

    protected Cliente() {
        // Construtor exigido pelo JPA.
    }

    public Cliente(String nome, String telefone, String email, String endereco) {
        this.nome = textoObrigatorio(nome, "O nome do cliente é obrigatório.");
        this.telefone = textoObrigatorio(telefone, "O telefone do cliente é obrigatório.");
        this.email = normalizarOpcional(email);
        this.endereco = normalizarOpcional(endereco);
        this.ativo = true;
    }

    public void alterarNome(String novoNome) {
        this.nome = textoObrigatorio(novoNome, "O nome do cliente é obrigatório.");
    }

    public void atualizarContato(String novoTelefone, String novoEmail) {
        this.telefone = textoObrigatorio(novoTelefone, "O telefone do cliente é obrigatório.");
        this.email = normalizarOpcional(novoEmail);
    }

    public void atualizarEndereco(String novoEndereco) {
        this.endereco = normalizarOpcional(novoEndereco);
    }

    public void desativar() {
        this.ativo = false;
    }

    public void reativar() {
        this.ativo = true;
    }

    void vincularEncomenda(Encomenda encomenda) {
        Objects.requireNonNull(encomenda, "A encomenda é obrigatória.");
        if (!encomendas.contains(encomenda)) {
            encomendas.add(encomenda);
        }
    }

    private static String textoObrigatorio(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensagem);
        }
        return valor.trim();
    }

    private static String normalizarOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getTelefone() { return telefone; }
    public String getEmail() { return email; }
    public String getEndereco() { return endereco; }
    public boolean isAtivo() { return ativo; }
    public List<Encomenda> getEncomendas() { return Collections.unmodifiableList(encomendas); }
}
