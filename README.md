# Registro de Produção — Projeto Final (AF) de Desenvolvimento Mobile

Aplicativo Android nativo (Java + layouts XML) para organizar **registros**, **listas de controle**
e **anotações operacionais** da área de produção de uma empresa industrial. Funciona **100% offline**
e guarda os dados **em memória** (`ArrayList`), conforme o enunciado do projeto.

| 📄 Documento | Conteúdo |
|---|---|
| **README.md** (este arquivo) | Identificação do grupo, objetivo, requisitos e como cada um foi atendido |
| [docs/INSTALACAO.md](docs/INSTALACAO.md) | Pré-requisitos, dependências, como compilar, executar e testar |
| [docs/FLUXO.md](docs/FLUXO.md) | Fluxo de telas, navegação, CRUD e mensagens exibidas ao usuário |

---

## 1. Identificação

| Item | Informação |
|---|---|
| Instituição | Facens |
| Disciplina | Desenvolvimento Mobile |
| Professor | Rafael Soleiman Franco |
| Avaliação | Projeto Final — AF |
| Turma | **TIN2** |
| Grupo | **2** |

### Integrantes

| Nome | RA |
|---|---|
| _Nome do integrante 1_ | _000000_ |
| _Nome do integrante 2_ | _000000_ |
| _Nome do integrante 3_ | _000000_ |
| _Nome do integrante 4_ | _000000_ |

---

## 2. Contexto

Uma empresa do segmento industrial precisa organizar as informações usadas no dia a dia da
produção — registros, listas de controle e anotações operacionais — que hoje ficam dispersas em
papel, planilhas e arquivos isolados.

O ambiente produtivo **não tem acesso à rede local nem à internet** e usa equipamentos robustos e
intrinsecamente seguros. Por isso, o aplicativo precisa:

- funcionar exclusivamente de forma local, sem internet, serviços externos ou banco de dados remoto;
- ser simples e intuitivo de usar;
- rodar em qualquer equipamento Android;
- ser desenvolvido no **Android Studio**, com lógica em **Java** e interface em **XML**;
- armazenar os dados **em memória**, com estruturas simples (arrays ou listas).

## 3. Objetivo

Desenvolver um aplicativo Android em Java que:

- utilize corretamente a arquitetura Android;
- possua múltiplas Activities;
- faça a navegação entre telas com Intents e métodos de ciclo de vida;
- implemente um CRUD completo em memória;
- utilize componentes visuais e elementos modernos de UI;
- apresente código organizado, legível e de fácil manutenção.

**Solução proposta:** o app *Registro de Produção* permite **cadastrar, visualizar, editar, excluir,
buscar e filtrar** informações operacionais, classificadas em três categorias (Registro, Lista de
controle e Anotação), com prioridade (Baixa, Média ou Alta) e status (Pendente ou Concluído).

---

## 4. Requisitos do enunciado e como foram atendidos

Os requisitos abaixo vêm do documento **AF – Projeto Final – Desenvolvimento Mobile** e guiaram o
desenvolvimento.

### 1️⃣ Múltiplas Activities (manifests)

| Requisito | Onde está |
|---|---|
| MainActivity (Activity principal) | `MainActivity.java` + `activity_main.xml` — declarada no Manifest com `MAIN`/`LAUNCHER` |
| Pelo menos uma Activity de cadastro | `FormularioRegistroActivity.java` + `activity_formulario_registro.xml` |
| Pelo menos uma Activity de listagem | `ListaRegistrosActivity.java` + `activity_lista_registros.xml` |
| Extra | `DetalheRegistroActivity.java` (visualização detalhada) e `BaseActivity.java` (código comum) |

### 2️⃣ Navegação, Manifest e ciclo de vida

| Requisito | Onde está |
|---|---|
| Manifest coerente | `AndroidManifest.xml`: 4 Activities, `label` em cada tela, `parentActivityName` (hierarquia), `exported="false"` nas telas internas e nenhuma permissão de internet |
| Intents explícitas | Métodos de fábrica `criarIntent(...)`, `criarIntentNovo(...)` e `criarIntentEdicao(...)`, com *extras* (`EXTRA_REGISTRO_ID`, `EXTRA_CATEGORIA`) definidos em `util/Constantes.java` |
| Retorno entre telas | `registerForActivityResult` (Lista ← Detalhes ← Formulário), devolvendo `RESULTADO_REGISTRO_EXCLUIDO` |
| Ciclo de vida | `onResume()` recarrega os dados ao voltar para Painel, Lista e Detalhes; `savedInstanceState` preserva campos e filtros na rotação; `BaseActivity` registra `onCreate`, `onStart`, `onResume`, `onPause`, `onStop`, `onRestart` e `onDestroy` no Logcat (tag **CicloDeVida**); `OnBackPressedCallback` no formulário |

### 3️⃣ Persistência de dados em memória (CRUD)

| Requisito | Onde está |
|---|---|
| Armazenamento em `ArrayList` | `data/RegistroRepository.java` → `private final List<Registro> registros = new ArrayList<>();` |
| CRUD completo na Activity de cadastro | `FormularioRegistroActivity`: **C**reate (`salvar()` sem id), **R**ead (carrega o registro pelo id e preenche os campos), **U**pdate (`salvar()` em modo de edição), **D**elete (lixeira na Toolbar → `confirmarExclusao()` / `excluir()`) |
| CRUD também nas outras telas | Lista (ler, buscar, filtrar, excluir deslizando + desfazer); Detalhes (ler, alterar status, excluir) |

### 4️⃣ Múltiplas interfaces gráficas (XML)

São 6 layouts em `res/layout`, mais um menu em `res/menu`. Componentes usados: `TextView`,
`TextInputEditText` (EditText), `ImageView`, `MaterialButton`, `ExtendedFloatingActionButton`,
`RecyclerView` (lista), `MaterialCardView`, `MaterialToolbar`, `Chip`/`ChipGroup`,
`MaterialButtonToggleGroup`, `MaterialSwitch`, `HorizontalScrollView`, `NestedScrollView`,
`CoordinatorLayout`, `AppBarLayout`, `LinearLayout`, `FrameLayout` e `<include>` de layout reutilizável.

### 5️⃣ Feedback ao usuário (Toast / Snackbar)

O app exibe Toasts, Snackbars (com ação **Desfazer**), erros nos campos e diálogos de confirmação
após cada ação principal. A tabela completa de mensagens está em
[docs/FLUXO.md → Mensagens ao usuário](docs/FLUXO.md#6-mensagens-ao-usuário).

### 6️⃣ Organização do código e boas práticas

- Pacotes separados por responsabilidade: `model`, `data`, `adapter`, `util`.
- Nomes de classes e métodos descritivos, em português.
- Javadoc e comentários em todas as classes.
- Textos, cores, dimensões e estilos em `res/values` (nada fixo no código).
- `BaseActivity` concentra o código repetido entre as telas.
- O repositório devolve cópias dos objetos (encapsulamento).
- Testes unitários (`src/test`) e instrumentados (`src/androidTest`).
- Evidências (opcional): roteiro de vídeo de até 30 s em [docs/FLUXO.md](docs/FLUXO.md#8-roteiro-para-o-vídeo-de-evidência-até-30-s).

### Requisitos de contexto

| Requisito | Como foi atendido |
|---|---|
| Funcionar sem rede | Nenhuma permissão de rede e nenhum serviço externo |
| Rodar em qualquer Android | `minSdk 21` (Android 5.0) |
| Fácil utilização em ambiente industrial | Botões de 56 dp (uso com luvas), alto contraste e textos grandes |
| Apenas dependências Google/Android | Somente AndroidX e Material Components (ver [docs/INSTALACAO.md](docs/INSTALACAO.md#2-dependências-do-projeto)) |

---

## 5. Estrutura do projeto

```
TIN2_grupo2_devmobile_projfinal/
├── README.md
├── docs/
│   ├── INSTALACAO.md
│   └── FLUXO.md
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/example/registroproducao/
│       │   │   ├── MainActivity.java               ← painel inicial (resumo + atalhos)
│       │   │   ├── ListaRegistrosActivity.java     ← listagem, busca, filtro, excluir deslizando
│       │   │   ├── FormularioRegistroActivity.java ← cadastro e edição (CRUD completo)
│       │   │   ├── DetalheRegistroActivity.java    ← detalhes, alterar status, excluir
│       │   │   ├── BaseActivity.java               ← código comum: ciclo de vida, toolbar, mensagens
│       │   │   ├── adapter/RegistroAdapter.java    ← RecyclerView (ListAdapter + DiffUtil)
│       │   │   ├── data/RegistroRepository.java    ← CRUD em memória (ArrayList)
│       │   │   ├── model/                          ← Registro, Categoria, Prioridade
│       │   │   └── util/                           ← Constantes, FormatadorData, TextWatcherSimples
│       │   └── res/
│       │       ├── drawable/   ← ícones vetoriais e fundos
│       │       ├── mipmap-*/   ← ícone do aplicativo
│       │       ├── layout/     ← 4 telas + 2 itens de lista
│       │       ├── menu/       ← menu da tela de edição
│       │       └── values/     ← strings, cores, dimensões e tema
│       ├── test/          ← testes unitários (RegistroRepositoryTest)
│       └── androidTest/   ← testes instrumentados com Espresso (FormularioRegistroActivityTest)
├── gradle/                ← wrapper e catálogo de versões (libs.versions.toml)
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## 6. Entrega

Conforme o enunciado, a entrega é feita **via Canvas**, em um arquivo **.zip** com toda a estrutura
do projeto (**não** é aceito `.apk`), com o nome no padrão:

```
<turma>_grupo<numero_do_grupo>_projfinal.zip   →   TIN2_grupo2_projfinal.zip
```

A pasta raiz do projeto deve seguir o padrão `<turma>_grupo<numero>_devmobile_projfinal`
(`TIN2_grupo2_devmobile_projfinal`), igual ao `rootProject.name` em `settings.gradle.kts`.
O passo a passo para gerar o .zip está em [docs/INSTALACAO.md](docs/INSTALACAO.md#7-gerando-o-zip-de-entrega).

> ⚠️ Como os dados ficam em memória (requisito do projeto), eles são perdidos quando o aplicativo é
> encerrado. Na primeira abertura, o app carrega 5 registros de exemplo para facilitar a demonstração.
