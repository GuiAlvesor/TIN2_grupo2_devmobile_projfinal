# Registro de Produção — Projeto Final (AF) de Desenvolvimento Mobile

Aplicativo Android nativo (Java + layouts XML) para organizar **registros**, **listas de controle**
e **anotações operacionais** da área de produção de uma empresa industrial. Funciona **100% offline**
e guarda os dados **em memória** (`ArrayList`), conforme o enunciado do projeto.

| 📄 Documento | Conteúdo |
|---|---|
| **README.md** (este arquivo) | Identificação do grupo, objetivo, requisitos e como cada um foi atendido |
| [docs/INSTALACAO.md](docs/INSTALACAO.md) | Pré-requisitos, dependências, como compilar, executar e testar |
| [docs/FLUXO.md](docs/FLUXO.md) | Fluxo de telas, navegação, CRUD e mensagens exibidas ao usuário |
| [demo/](demo/) | `RegistroProducao.apk` (instalação direta para testes) e vídeo demonstrativo de 24 s — **não** fazem parte do .zip de entrega |

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
| Marcelo Panini da Silva | 235051 |
| Luis Gustavo Ferraz Sales | 248236 |
| Lucas Gomes de Araujo | 237007 |
| Vinicius Maximus Boy de Araújo | 257958 |
| Guilherme Alves dos Santos | 247843 |

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
As **Listas de controle** podem ser respondidas item a item (Sim / Não / N/A), com registro da
ação corretiva quando a resposta é "Não".

### Telas

| # | Tela | Activity | O que faz |
|---|---|---|---|
| 1 | Painel | `MainActivity` | Pendentes do turno, ações rápidas, checklist em andamento, dica de EPIs, registros recentes e navegação inferior |
| 2 | Registros | `ListaRegistrosActivity` | Busca, filtros com contador (Todos, Pendentes, Alta, Checklists), cards e exclusão deslizando com "Desfazer" |
| 3 | Checklist | `ChecklistActivity` | Responde uma Lista de controle (Sim / Não / N/A), com ação corretiva, progresso, rascunho e finalização |
| 4 | Cadastro / Edição | `FormularioRegistroActivity` | CRUD completo: cria, carrega, atualiza e exclui (lixeira na Toolbar); itens do checklist um por linha |
| 5 | Detalhes | `DetalheRegistroActivity` | Dados completos, alterar status, editar, excluir e "Abrir checklist" |

### Identidade visual

- **Paleta vermelho · branco · preto:** vermelho `#D7192D` (cor principal e prioridade Alta),
  vermelho escuro `#B3121F` (textos e links), vermelho suave `#FDECEE` (destaques), preto `#121212`
  (texto principal, card de contraste, "Finalizar"), cinzas `#5C5C5C`, `#D6D6D6`, `#EDEDED`,
  `#F6F6F6` e `#F2F2F2`.
- **Tipografia:** Manrope (títulos e números, pesos 700/800) e Inter (corpo, 400–700). Os arquivos
  `.ttf` ficam em `res/font/` **dentro do APK** — o app continua 100% offline. Fonte: Google Fonts,
  licença SIL Open Font License 1.1.
- **Prioridade sem depender só de cor:** Alta = pílula vermelha preenchida; Média = pílula com
  contorno preto; Baixa = pílula cinza. Status: Pendente em vermelho escuro com relógio; Concluído
  em preto com ícone de check.
- **Formas:** cantos de 14–20 dp em cards e campos, pílulas em etiquetas e filtros, botões principais
  de 60 dp e áreas de toque de pelo menos 48 dp.

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
| Extras | `ChecklistActivity.java` + `activity_checklist.xml` (respostas da Lista de controle), `DetalheRegistroActivity.java` (visualização detalhada) e `BaseActivity.java` (código comum) |

### 2️⃣ Navegação, Manifest e ciclo de vida

| Requisito | Onde está |
|---|---|
| Manifest coerente | `AndroidManifest.xml`: 5 Activities, `label` em cada tela, `parentActivityName` (hierarquia), `exported="false"` nas telas internas e nenhuma permissão de internet |
| Intents explícitas | Métodos de fábrica `criarIntent(...)`, `criarIntentNovo(...)` e `criarIntentEdicao(...)`, com *extras* (`EXTRA_REGISTRO_ID`, `EXTRA_CATEGORIA`, `EXTRA_FILTRO`) definidos em `util/Constantes.java` |
| Retorno entre telas | `registerForActivityResult` (Painel/Lista ← Detalhes ← Formulário), devolvendo `RESULTADO_REGISTRO_EXCLUIDO` |
| Ciclo de vida | `onResume()` recarrega os dados ao voltar para Painel, Lista e Detalhes; `onSaveInstanceState` preserva categoria/prioridade do formulário e as respostas do checklist na rotação; `BaseActivity` registra `onCreate`, `onStart`, `onResume`, `onPause`, `onStop`, `onRestart` e `onDestroy` no Logcat (tag **CicloDeVida**); `OnBackPressedCallback` no formulário e no checklist |

### 3️⃣ Persistência de dados em memória (CRUD)

| Requisito | Onde está |
|---|---|
| Armazenamento em `ArrayList` | `data/RegistroRepository.java` → `private final List<Registro> registros = new ArrayList<>();` |
| CRUD completo na Activity de cadastro | `FormularioRegistroActivity`: **C**reate (`salvar()` sem id), **R**ead (carrega o registro pelo id e preenche os campos), **U**pdate (`salvar()` em modo de edição), **D**elete (lixeira na Toolbar → `confirmarExclusao()` / `excluir()`) |
| CRUD também nas outras telas | Lista (ler, buscar, filtrar, excluir deslizando + desfazer); Detalhes (ler, alterar status, excluir); Checklist (ler e atualizar as respostas, finalizar) |
| Itens do checklist | `model/ItemChecklist.java` (pergunta + resposta SIM / NAO / NA + observação), guardados numa `List` dentro de cada `Registro` da categoria Lista de controle |

### 4️⃣ Múltiplas interfaces gráficas (XML)

São 8 layouts em `res/layout` (5 telas + 3 itens), 2 menus em `res/menu`, fontes em `res/font` e
seletores de cor em `res/color`. Componentes usados: `TextView`, `EditText` / `TextInputEditText`,
`ImageView`, `MaterialButton`, `ExtendedFloatingActionButton`, `RecyclerView` (lista),
`MaterialCardView`, `MaterialToolbar`, `Chip`/`ChipGroup`, `BottomNavigationView`,
`LinearProgressIndicator`, `MaterialSwitch`, `MaterialAlertDialog`, `HorizontalScrollView`,
`NestedScrollView`, `CoordinatorLayout`, `AppBarLayout`, `LinearLayout` e `FrameLayout`.

### 5️⃣ Feedback ao usuário (Toast / Snackbar)

O app exibe Toasts, Snackbars (com ação **Desfazer**), erros nos campos e diálogos de confirmação
após cada ação principal. A tabela completa de mensagens está em
[docs/FLUXO.md → Mensagens ao usuário](docs/FLUXO.md#6-mensagens-ao-usuário).

### 6️⃣ Organização do código e boas práticas

- Pacotes separados por responsabilidade: `model`, `data`, `adapter`, `util`.
- Nomes de classes e métodos descritivos, em português.
- Javadoc e comentários em todas as classes.
- Textos, cores, dimensões e estilos em `res/values` (nada fixo no código).
- `BaseActivity` concentra o código repetido entre as telas; `util/EstiloVisual` concentra as
  pílulas de prioridade/status e o ícone de categoria usados em várias telas.
- O repositório devolve cópias dos objetos (encapsulamento).
- Testes unitários (`src/test`) e instrumentados (`src/androidTest`).
- Evidências (opcional): roteiro de vídeo de até 30 s em [docs/FLUXO.md](docs/FLUXO.md#8-roteiro-para-o-vídeo-de-evidência-até-30-s).

### Requisitos de contexto

| Requisito | Como foi atendido |
|---|---|
| Funcionar sem rede | Nenhuma permissão de rede e nenhum serviço externo |
| Rodar em qualquer Android | `minSdk 21` (Android 5.0) |
| Fácil utilização em ambiente industrial | Botões principais de 60 dp e respostas de 52 dp (uso com luvas), áreas de toque ≥ 48 dp, alto contraste e textos grandes |
| Apenas dependências Google/Android | Somente AndroidX e Material Components; as fontes são arquivos do Google Fonts empacotados no APK (ver [docs/INSTALACAO.md](docs/INSTALACAO.md#2-dependências-do-projeto)) |

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
│       │   │   ├── MainActivity.java               ← painel inicial
│       │   │   ├── ListaRegistrosActivity.java     ← listagem, busca, filtros, excluir deslizando
│       │   │   ├── ChecklistActivity.java          ← respostas da Lista de controle
│       │   │   ├── FormularioRegistroActivity.java ← cadastro e edição (CRUD completo)
│       │   │   ├── DetalheRegistroActivity.java    ← detalhes, alterar status, excluir
│       │   │   ├── BaseActivity.java               ← código comum: ciclo de vida, toolbar, insets, mensagens
│       │   │   ├── adapter/RegistroAdapter.java    ← RecyclerView (ListAdapter + DiffUtil)
│       │   │   ├── data/RegistroRepository.java    ← CRUD em memória (ArrayList)
│       │   │   ├── model/                          ← Registro, ItemChecklist, Categoria, Prioridade, FiltroLista
│       │   │   └── util/                           ← Constantes, EstiloVisual, FormatadorData, TextWatcherSimples
│       │   └── res/
│       │       ├── color/      ← seletores de cor (filtros, segmentos, campos, navegação)
│       │       ├── drawable/   ← ícones vetoriais, formas e ilustração de EPI
│       │       ├── font/       ← Manrope e Inter (.ttf) + famílias manrope.xml / inter.xml
│       │       ├── mipmap-*/   ← ícone do aplicativo
│       │       ├── layout/     ← 5 telas + 3 itens (registro, recente, pergunta do checklist)
│       │       ├── menu/       ← navegação inferior e menu da tela de edição
│       │       └── values/     ← strings, cores, dimensões, tema e estilos
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

O .zip **não** deve conter a pasta `demo/` (APK e vídeo), a pasta `.git` nem arquivos de cache e
de build (`.gradle/`, `.idea/`, `build/`, `app/build/`, `local.properties`). O vídeo demonstrativo
(opcional) é enviado separadamente.

> ⚠️ Como os dados ficam em memória (requisito do projeto), eles são perdidos quando o aplicativo é
> encerrado. Na primeira abertura, o app carrega 5 registros de exemplo para facilitar a demonstração.
