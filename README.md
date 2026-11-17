# NEXUM - Etapa 2: Modelagem do domínio e arquitetura

Sistema web de gerenciamento de encomendas personalizadas. Este repositório contém a modelagem do domínio da segunda etapa do projeto, com entidades Java, mapeamentos JPA, métodos de alteração de estado, diagrama UML e justificativa da arquitetura.

> **Escopo desta entrega:** modelagem do domínio e definição da arquitetura. Controllers, Services, repositórios e endpoints REST não foram implementados, conforme a orientação da atividade.

## 1. Escopo considerado

A modelagem foi construída a partir da apresentação da Etapa 1. O MVP do NEXUM prevê:

- Cadastro e consulta rápida de clientes.
- Registro de encomendas, itens e personalizações.
- Histórico das alterações solicitadas.
- Acompanhamento das etapas de produção e dos prazos.
- Registro de pagamentos, total já pago e saldo pendente.
- Identificação dos funcionários e de seus perfis de acesso.
- Registro da origem da encomenda: WhatsApp, Instagram ou atendimento presencial.

Integração automática com redes sociais, controle avançado de estoque, emissão fiscal, relatórios contábeis complexos e aplicativo mobile nativo permanecem fora do MVP, conforme o recorte apresentado na primeira etapa.

## 2. Entidades implementadas

| Entidade | Responsabilidade | Principais atributos |
|---|---|---|
| `Cliente` | Armazenar os dados básicos do cliente. | nome, telefone, e-mail, endereço e situação ativa. |
| `Usuario` | Representar os funcionários que utilizam o sistema. | nome, e-mail, hash da senha, perfil de acesso e situação ativa. |
| `Encomenda` | Centralizar o pedido, sua origem, situação e prazos. | data de criação, datas prevista/real de entrega, origem, status e observações. |
| `ItemEncomenda` | Representar cada item incluído em uma encomenda. | descrição, quantidade, valor unitário e observações. |
| `Personalizacao` | Registrar as características personalizadas de um item. | tipo, descrição e data da solicitação. |
| `EtapaProducao` | Acompanhar as etapas, datas previstas/reais e responsável. | nome, descrição, ordem, status e datas. |
| `Pagamento` | Registrar valores efetivamente recebidos e eventuais estornos. | valor, forma, status, data e observação. |
| `HistoricoAlteracao` | Guardar alterações e solicitações relacionadas à encomenda. | tipo, descrição, campo alterado, valores anterior/novo e data/hora. |

Os tipos enumerados estão em `domain/enums`: `PerfilAcesso`, `OrigemEncomenda`, `StatusEncomenda`, `StatusEtapaProducao`, `FormaPagamento`, `StatusPagamento` e `TipoAlteracao`.

## 3. Relacionamentos JPA e multiplicidades

- `Cliente 1 — 0..* Encomenda`: um cliente pode possuir várias encomendas; cada encomenda pertence a um cliente.
- `Encomenda 1 — 0..* ItemEncomenda`: uma encomenda reúne seus itens; cada item pertence a uma encomenda.
- `ItemEncomenda 1 — 0..* Personalizacao`: um item pode possuir várias personalizações; cada personalização pertence a um item.
- `Encomenda 1 — 0..* EtapaProducao`: uma encomenda possui etapas de produção.
- `Encomenda 1 — 0..* Pagamento`: uma encomenda pode ter nenhum ou vários pagamentos parciais.
- `Encomenda 1 — 0..* HistoricoAlteracao`: uma encomenda mantém seu histórico.
- `Usuario 0..1 — 0..* HistoricoAlteracao`: cada registro de histórico pode ter um funcionário responsável; um funcionário pode registrar vários eventos.
- `Usuario 0..1 — 0..* Pagamento`: um pagamento pode ser associado ao funcionário que o registrou.
- `Usuario 0..1 — 0..* EtapaProducao`: uma etapa pode ter um funcionário responsável.

Os relacionamentos são mapeados com `@OneToMany` e `@ManyToOne`. Não foi utilizado `@OneToOne` ou `@ManyToMany`, pois não há, no escopo apresentado, uma relação de negócio que justifique essas cardinalidades.

## 4. Regras e métodos de domínio

As entidades não utilizam setters genéricos para alterações de negócio. Foram definidos métodos com intenção explícita, por exemplo:

- `Cliente.atualizarContato()`, `desativar()` e `reativar()`.
- `Usuario.alterarPerfil()`, `alterarSenhaHash()` e `desativar()`.
- `Encomenda.alterarStatus()`, `atualizarPrazoEntrega()`, `registrarSolicitacaoDeAlteracao()` e `estornarPagamento()`.
- `ItemEncomenda.atualizarQuantidade()`, `atualizarValorUnitario()` e `adicionarPersonalizacao()`.
- `EtapaProducao.iniciar()`, `pausar()`, `concluir()` e `reprogramar()`.
- `Pagamento` é criado como confirmado e seu estorno é realizado pela encomenda, preservando o registro do pagamento.

O valor total da encomenda é calculado pela soma dos subtotais dos itens. O total pago considera apenas pagamentos confirmados, e o saldo pendente é calculado pela diferença entre esses valores. Os valores monetários utilizam `BigDecimal`.

As alterações de status da encomenda seguem um fluxo básico: recebida → em produção → pronta → em entrega → entregue, com cancelamento permitido antes da conclusão, respeitando as transições implementadas na entidade. As etapas específicas de produção têm nomes configuráveis, pois a primeira etapa ainda não definiu quais etapas e prazos serão utilizados pela empresa.

## 5. Arquitetura escolhida: arquitetura em camadas

Foi escolhida a **arquitetura em camadas**, em um monólito organizado por responsabilidades. Para o NEXUM, o sistema atende inicialmente uma operação delimitada de gerenciamento de encomendas e possui um MVP com funcionalidades bem definidas. Separar apresentação, aplicação, domínio e persistência permite organizar o código sem introduzir a complexidade operacional de microsserviços.

A escolha também favorece uma equipe acadêmica: cada integrante pode trabalhar em uma camada ou funcionalidade com responsabilidades claras, e as partes podem ser integradas gradualmente. O domínio permanece separado de Controllers e detalhes de acesso a dados, facilitando testes e futuras alterações.

### Organização prevista do código

```text
src/main/java/br/com/nexum/
├── NexumApplication.java
├── presentation/             # Controllers e objetos de entrada/saída (etapas futuras)
├── application/              # Services e casos de uso (etapas futuras)
├── domain/
│   ├── entity/               # Entidades JPA implementadas nesta etapa
│   └── enums/                # Estados, perfis, origens e classificações
└── infrastructure/
    └── repository/           # Repositórios Spring Data JPA (etapas futuras)
```

Nesta entrega, estão implementados `domain/entity` e `domain/enums`, além da classe principal do Spring Boot. As pastas das demais camadas serão criadas quando Controllers, Services e repositórios forem solicitados.

## 6. Tecnologias e execução

- Java 21.
- Spring Boot 4.1.2.
- Spring Data JPA / Hibernate para persistência e mapeamento objeto-relacional.
- H2 em memória como banco padrão para facilitar a execução da modelagem.
- Driver PostgreSQL já declarado para uma futura configuração de banco persistente.
- Maven.

### Pré-requisitos

- JDK 21 instalado e configurado no `JAVA_HOME`.
- Maven 3.6.3 ou superior.

### Executar

Na pasta raiz do projeto:

```bash
mvn spring-boot:run
```

Para gerar o pacote:

```bash
mvn clean package
```

A configuração padrão usa H2 em memória e `spring.jpa.hibernate.ddl-auto=create-drop`, fazendo o Hibernate gerar as tabelas ao iniciar a aplicação. Como esta é uma etapa de modelagem, a aplicação não expõe endpoints HTTP e não possui telas.

Para uma futura configuração PostgreSQL, será necessário trocar as propriedades do datasource em `src/main/resources/application.properties` e ajustar o modo de geração do schema conforme o ambiente.

## 7. Diagrama UML

O diagrama de classes com atributos, métodos modificadores, relacionamentos e multiplicidades está disponível em:

- `diagrama/diagrama-classes.png` - imagem para visualização e apresentação.
- `diagrama/diagrama-classes.dot` - arquivo-fonte editável do diagrama.

## 8. Observações e decisões que ainda podem ser refinadas

A apresentação da Etapa 1 registra pontos que devem ser confirmados com o proprietário, como dados obrigatórios no pedido, permissões específicas, etapas exatas da produção e cálculo dos prazos. Por isso, os campos de contato são uma proposta inicial, os perfis de acesso representam os setores identificados e as etapas de produção são configuráveis por encomenda.

A entidade `Usuario` armazena o campo `senhaHash`, nunca uma senha em texto puro; implementação de autenticação e autorização ainda não faz parte desta entrega. Não foram criadas entidades de estoque, produto de catálogo, nota fiscal ou contabilidade porque esses módulos ficaram expressamente fora da primeira versão.

---

**NEXUM - Segunda etapa do projeto**

## 9. Publicar no GitHub e compartilhar com a equipe

Depois de criar um repositório vazio no GitHub, abra o terminal na pasta do projeto e execute:

```bash
git init
git add .
git commit -m "Entrega da etapa 2 - modelagem do dominio"
git branch -M main
git remote add origin URL_DO_REPOSITORIO
git push -u origin main
```

Substitua `URL_DO_REPOSITORIO` pela URL HTTPS ou SSH do repositório criado. Para liberar o acesso dos demais integrantes, o proprietário deve abrir **Settings > Collaborators** no GitHub e convidar cada participante pelo nome de usuário ou e-mail associado à conta.
