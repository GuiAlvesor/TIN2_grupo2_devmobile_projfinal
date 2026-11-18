# Registro de Produção — Projeto Final de Desenvolvimento Mobile

Aplicativo Android (Java + layouts XML) para organizar **registros**, **listas de controle** e
**anotações operacionais** da área de produção. Funciona **100% offline**: não usa internet,
serviços externos nem banco de dados — os dados ficam **em memória**, em uma `ArrayList`.

> **Antes de entregar:** renomeie a pasta `TIN2_grupo2_devmobile_projfinal` (e o valor de
> `rootProject.name` em `settings.gradle.kts`) para o padrão `<turma>_grupo<numero>_devmobile_projfinal`.

---

## Como abrir e executar

1. Abra o **Android Studio** → *File › Open* → selecione a pasta do projeto.
2. Aguarde o *Gradle Sync* (na primeira vez o Android Studio baixa o Gradle 8.9 e as dependências).
3. Escolha um emulador ou dispositivo (Android 5.0 / API 21 ou superior) e clique em **Run ▶**.

Requisitos: Android Studio Ladybug (2024.2) ou mais recente, JDK 17 (já incluso no Android Studio).

| Item | Valor |
|---|---|
| Linguagem | Java 11 |
| Interface | XML + Material Design 3 (Material Components 1.12) |
| minSdk / targetSdk | 21 (Android 5.0) / 35 (Android 15) |
| Persistência | Em memória (`ArrayList` dentro de um Singleton) |
| Permissões | Nenhuma (sem acesso à rede) |

---

## Estrutura do projeto

```
TIN2_grupo2_devmobile_projfinal/
└── app/
    ├── src/main/AndroidManifest.xml
    ├── src/main/java/com/example/registroproducao/
    │   ├── BaseActivity.java                 ← base comum: ciclo de vida, toolbar, insets, mensagens
    │   ├── MainActivity.java                 ← painel inicial (resumo + atalhos)
    │   ├── ListaRegistrosActivity.java       ← listagem, busca, filtro, excluir deslizando
    │   ├── FormularioRegistroActivity.java   ← cadastro e edição
    │   ├── DetalheRegistroActivity.java      ← detalhes, alterar status, excluir
    │   ├── adapter/RegistroAdapter.java      ← RecyclerView (ListAdapter + DiffUtil)
    │   ├── data/RegistroRepository.java      ← CRUD em memória (ArrayList)
    │   ├── model/Registro.java | Categoria.java | Prioridade.java
    │   └── util/Constantes.java | FormatadorData.java | TextWatcherSimples.java
    ├── src/test/java/.../RegistroRepositoryTest.java              ← testes unitários (JVM)
    ├── src/androidTest/java/.../FormularioRegistroActivityTest.java ← testes instrumentados (Espresso)
    └── src/main/res/
        ├── drawable/   ← ícones vetoriais e fundos
        ├── mipmap-*/   ← ícone do aplicativo (adaptativo + PNG para Android antigo)
        ├── layout/
        │   ├── activity_main.xml
        │   ├── activity_lista_registros.xml
        │   ├── activity_formulario_registro.xml
        │   ├── activity_detalhe_registro.xml
        │   ├── item_registro.xml
        │   └── item_categoria_resumo.xml
        └── values/     ← strings, cores, dimensões e tema
```

---

## Fluxo de telas

```
MainActivity ──(Novo registro)──────────────► FormularioRegistroActivity (cadastrar)
     │
     ├──(Ver registros / card de categoria)──► ListaRegistrosActivity
     │                                              │
     │                                   (toque)    ├──► DetalheRegistroActivity
     │                                              │          ├──(Editar)──► FormularioRegistroActivity (editar)
     │                                              │          └──(Excluir)──► volta à lista com "Desfazer"
     │                                              └──(Novo)──► FormularioRegistroActivity (cadastrar)
```

---

## Como cada requisito foi atendido

### 1️⃣ Múltiplas Activities (manifests)
| Exigência | Onde está |
|---|---|
| MainActivity (principal) | `MainActivity.java` + `activity_main.xml` — declarada no Manifest com `MAIN`/`LAUNCHER` |
| Activity de Cadastro | `FormularioRegistroActivity.java` + `activity_formulario_registro.xml` |
| Activity de Listagem | `ListaRegistrosActivity.java` + `activity_lista_registros.xml` |
| Extra | `DetalheRegistroActivity.java` (visualização detalhada) e `BaseActivity.java` (código comum) |

### 2️⃣ Navegação, Manifest e Ciclo de Vida
| Exigência | Onde está |
|---|---|
| Manifest coerente | `AndroidManifest.xml`: 4 Activities declaradas, `label` de cada tela, `parentActivityName` (hierarquia), `exported="false"` nas telas internas, sem permissão de internet |
| Intents explícitas | Métodos de fábrica `criarIntent(...)`, `criarIntentNovo(...)`, `criarIntentEdicao(...)` com *extras* (`EXTRA_REGISTRO_ID`, `EXTRA_CATEGORIA`) definidos em `util/Constantes.java` |
| Retorno entre telas | `registerForActivityResult` (Lista ← Detalhes ← Formulário) devolvendo `RESULTADO_REGISTRO_EXCLUIDO` |
| Ciclo de vida | `onResume()` recarrega os dados ao voltar para Painel, Lista e Detalhes; `savedInstanceState` preserva campos/filtros na rotação; `BaseActivity` registra `onCreate/onStart/onResume/onPause/onStop/onRestart/onDestroy` no Logcat (tag **CicloDeVida**); `OnBackPressedCallback` no formulário |

### 3️⃣ Persistência em memória e CRUD
| Exigência | Onde está |
|---|---|
| Armazenamento em `ArrayList` | `data/RegistroRepository.java` → `private final List<Registro> registros = new ArrayList<>();` |
| **CRUD completo na Activity de cadastro** | `FormularioRegistroActivity`: **C**reate (`salvar()` sem id), **R**ead (carrega o registro pelo id e preenche os campos), **U**pdate (`salvar()` em modo edição), **D**elete (lixeira na Toolbar → `confirmarExclusao()`/`excluir()`) |
| CRUD também nas outras telas | Lista (ler, buscar, filtrar, excluir deslizando + desfazer); Detalhes (ler, alterar status, excluir) |

### 4️⃣ Múltiplas interfaces gráficas (XML)
6 layouts em `res/layout` + menu em `res/menu`. Componentes usados: `TextView`, `TextInputEditText` (EditText), `ImageView`, `MaterialButton`, `ExtendedFloatingActionButton`, `RecyclerView` (lista), `MaterialCardView`, `MaterialToolbar`, `Chip`/`ChipGroup`, `MaterialButtonToggleGroup`, `MaterialSwitch`, `HorizontalScrollView`, `NestedScrollView`, `CoordinatorLayout`, `AppBarLayout`, `LinearLayout`, `FrameLayout`, `<include>` de layout reutilizável.

### 5️⃣ Feedback ao usuário
| Mensagem | Tipo | Quando |
|---|---|---|
| "Registro cadastrado com sucesso!" | Toast | Ao cadastrar |
| "Registro atualizado com sucesso!" | Toast | Ao salvar edição |
| "Registro "X" excluído" + **Desfazer** | Snackbar | Ao excluir (lista, detalhes ou formulário) |
| "Registro restaurado" | Toast | Ao tocar em Desfazer |
| "Registro marcado como concluído" / "reaberto" | Snackbar | Ao alterar status |
| "Verifique os campos destacados" + erro no campo | Toast + erro no `TextInputLayout` | Título vazio ou curto |
| "Alterações descartadas" | Toast | Ao sair sem salvar |
| "Registro não encontrado" | Toast | Id inválido |
| Diálogos de confirmação | MaterialAlertDialog | Excluir / descartar alterações |

### 6️⃣ Organização e boas práticas
Pacotes por responsabilidade (`model`, `data`, `adapter`, `util`); nomes descritivos em português; Javadoc e comentários em todas as classes; textos, cores, dimensões e estilos em `res/values` (nada fixo no código); `BaseActivity` evita repetição; repositório devolve cópias (encapsulamento); testes unitários e instrumentados.

### Requisitos do contexto
- **Offline:** nenhuma permissão de rede nem dependência externa em tempo de execução.
- **Qualquer equipamento Android:** `minSdk 21` (Android 5.0).
- **Ambiente industrial:** botões de 56dp (uso com luvas), alto contraste e textos grandes.

---

## Funcionalidades

- **Painel** com totais (registros, pendentes e alta prioridade) e atalhos por categoria.
- **Cadastrar** registro com categoria, título (obrigatório, mín. 3 caracteres), setor, prioridade, descrição e status.
- **Listar** com busca instantânea (título, setor, descrição) e filtro por categoria.
- **Visualizar** detalhes com datas de criação e última atualização.
- **Editar** registro (com aviso ao sair sem salvar).
- **Excluir** pela tela de detalhes, pela lixeira do formulário de edição (ambos com confirmação) ou **deslizando o item** na lista — sempre com **Desfazer**.
- **Marcar como concluído / reabrir** com um toque.

Na primeira abertura são carregados 5 registros de exemplo para facilitar a demonstração.

> Como os dados ficam em memória (requisito do projeto), eles são perdidos quando o aplicativo é
> encerrado pelo sistema ou pelo usuário.

---

## Testes

- **Unitários** (sem emulador): clique com o botão direito em `app/src/test/...` → *Run Tests*, ou
  `./gradlew test`.
- **Instrumentados** (com emulador/dispositivo): `app/src/androidTest/...` → *Run Tests*, ou
  `./gradlew connectedAndroidTest`.

---

## Evidências (opcional): vídeo de até 30 segundos

No Android Studio, com o emulador aberto: painel **Running Devices** → botão **Record Screen** (ícone de câmera de vídeo) → ao terminar, **Stop** e salve o `.mp4`. Para prints, use o botão **Take Screenshot** do mesmo painel.

Roteiro sugerido (≈ 28 s, passando por todas as telas):

| Tempo | Ação |
|---|---|
| 0–4 s | Painel inicial: mostrar o resumo e as categorias |
| 4–11 s | **Novo registro** → preencher título e setor → escolher prioridade Alta → **Cadastrar** (Toast de sucesso) |
| 11–15 s | **Ver todos os registros** → digitar na busca e tocar em um chip de filtro |
| 15–20 s | Tocar em um item → Detalhes → **Marcar como concluído** (Snackbar) |
| 20–25 s | **Editar** → alterar o título → **Salvar** (Toast de atualização) |
| 25–28 s | Na lista, deslizar um item para excluir → tocar em **Desfazer** |
