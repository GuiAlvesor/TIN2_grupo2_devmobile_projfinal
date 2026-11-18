# Instalação, compilação e execução

[← Voltar ao README](../README.md) · [Fluxo do app →](FLUXO.md)

Este guia mostra como preparar o ambiente, abrir o projeto, compilar e rodar o aplicativo
*Registro de Produção*, tanto pelo **Android Studio** (recomendado e obrigatório pelo enunciado)
quanto pela **linha de comando**.

---

## 1. Pré-requisitos

| Ferramenta | Versão | Observação |
|---|---|---|
| **Android Studio** | Ladybug (2024.2) ou mais recente | IDE obrigatória do projeto |
| **JDK do Gradle** | 17 a 21 | O Gradle 8.9 do projeto **não roda com JDK 22 ou superior**. Versões recentes do Android Studio trazem o JDK 25 embutido: nesse caso use um JDK 21 (ex.: *jbr-21*), ver seção 8 |
| **Android SDK Platform** | API 35 (Android 15) | Usado para compilar (`compileSdk = 35`) |
| **Android SDK Build-Tools** | 35.x | Instalado automaticamente pelo Gradle Sync, se faltar |
| **Emulador ou dispositivo** | Android 5.0 (API 21) ou superior | `minSdk = 21` |
| **Git** (opcional) | Qualquer versão recente | Apenas para clonar o repositório |

> 🌐 **Internet só é necessária na primeira compilação**, para baixar o Gradle e as bibliotecas.
> O aplicativo em si funciona totalmente offline.

### Instalando os componentes do SDK

No Android Studio: **Tools › SDK Manager**

1. Aba **SDK Platforms** → marque **Android 15.0 ("VanillaIceCream") – API 35**.
2. Aba **SDK Tools** → confirme que **Android SDK Build-Tools**, **Android Emulator** e
   **Android SDK Platform-Tools** estão instalados.
3. Clique em **Apply**.

---

## 2. Dependências do projeto

As versões ficam centralizadas no catálogo `gradle/libs.versions.toml`. Todas as dependências são do
Google/AndroidX, como exige o enunciado — **não há bibliotecas de terceiros, banco de dados nem
serviços externos**.

### Ferramentas de build

| Item | Versão |
|---|---|
| Gradle (via wrapper) | 8.9 |
| Android Gradle Plugin (AGP) | 8.7.3 |
| Linguagem / nível do Java | Java 11 (`sourceCompatibility` / `targetCompatibility`) |
| `compileSdk` / `targetSdk` / `minSdk` | 35 / 35 / 21 |

### Bibliotecas do app

| Biblioteca | Versão | Uso |
|---|---|---|
| `androidx.appcompat:appcompat` | 1.7.0 | `AppCompatActivity`, compatibilidade com Android antigo |
| `com.google.android.material:material` | 1.12.0 | Componentes Material Design 3 (cards, chips, botões, Snackbar, diálogos) |
| `androidx.activity:activity` | 1.9.3 | `registerForActivityResult`, `OnBackPressedCallback` |
| `androidx.constraintlayout:constraintlayout` | 2.2.0 | Layouts |
| `androidx.recyclerview:recyclerview` | 1.3.2 | Lista de registros |
| `androidx.coordinatorlayout:coordinatorlayout` | 1.2.0 | Toolbar + lista + botão flutuante |

### Bibliotecas de teste

| Biblioteca | Versão | Uso |
|---|---|---|
| `junit:junit` | 4.13.2 | Testes unitários (JVM) |
| `androidx.test.ext:junit` | 1.3.0 | Runner dos testes instrumentados |
| `androidx.test.espresso:espresso-core` | 3.7.0 | Testes de interface (3.7.0 ou superior é necessário para Android 15+) |

### Fontes

As fontes **Manrope** (títulos) e **Inter** (corpo) são arquivos `.ttf` estáticos do Google Fonts
(licença SIL Open Font License 1.1) guardados em `app/src/main/res/font/`, com as famílias
`manrope.xml` e `inter.xml`. Elas vão **dentro do APK**: nada é baixado em tempo de execução e o app
continua funcionando sem internet.

Não é preciso instalar nada manualmente: o **Gradle Sync** baixa tudo dos repositórios `google()` e
`mavenCentral()`.

---

## 3. Obtendo o código

**Opção A — clonar do GitHub**

```bash
git clone https://github.com/GuiAlvesor/TIN2_grupo2_devmobile_projfinal.git
```

A pasta clonada já é o projeto Android (contém o `settings.gradle.kts`) e pode ser aberta direto no Android Studio.

**Opção B — a partir do .zip de entrega**

Extraia o arquivo `TIN2_grupo2_projfinal.zip` em uma pasta qualquer.

> 💡 No Windows, prefira um caminho curto e sem acentos (ex.: `C:\projetos\`), para evitar erros de
> caminho longo durante a compilação.

---

## 4. Abrindo, compilando e rodando no Android Studio

1. Abra o Android Studio → **File › Open**.
2. Selecione a **pasta do projeto** (a que contém `settings.gradle.kts`) e clique em **OK**.
3. Se aparecer *"Trust Gradle project?"*, clique em **Trust Project**.
4. Aguarde o **Gradle Sync** terminar (barra de progresso no rodapé). Na primeira vez ele baixa o
   Gradle 8.9 e as dependências, o que pode levar alguns minutos.
5. **Crie um emulador**, caso ainda não tenha: **Tools › Device Manager › + (Create Virtual Device)** →
   escolha um aparelho (ex.: *Pixel 7*) → uma imagem de sistema (ex.: *API 35*) → **Finish**.
6. Na barra superior, confira se a configuração de execução é **app** e selecione o emulador ou o
   dispositivo.
7. Clique em **Run ▶** (ou `Shift + F10`). O Android Studio compila, instala e abre o app.

Para apenas **compilar** sem executar: **Build › Make Project** (`Ctrl + F9`).

### Rodando em um celular físico

1. No celular: **Configurações › Sobre o telefone** → toque 7 vezes em **Número da versão** para ativar
   as *Opções do desenvolvedor*.
2. Em **Opções do desenvolvedor**, ative **Depuração USB**.
3. Conecte o celular ao computador pelo cabo USB e aceite a autorização de depuração no aparelho.
4. O celular aparece na lista de dispositivos do Android Studio → selecione-o e clique em **Run ▶**.

---

## 5. Compilando pela linha de comando (opcional)

Na pasta do projeto, use o Gradle Wrapper (não é preciso instalar o Gradle). É necessário ter um
JDK de 17 a 21 configurado na variável `JAVA_HOME`. Se o JDK embutido no Android Studio
(`<pasta do Android Studio>/jbr`) for o 25, aponte para um JDK 21 — por exemplo, o que o próprio
Android Studio baixa em `C:\Users\<usuario>\.jdks\jbr-21...`:

```powershell
$env:JAVA_HOME = "$env:USERPROFILE\.jdks\jbr-21.0.11"   # ajuste para a versão instalada
.\gradlew.bat assembleDebug
```

| Ação | Windows | Linux / macOS |
|---|---|---|
| Compilar o APK de debug | `gradlew.bat assembleDebug` | `./gradlew assembleDebug` |
| Instalar no emulador/dispositivo conectado | `gradlew.bat installDebug` | `./gradlew installDebug` |
| Rodar testes unitários | `gradlew.bat test` | `./gradlew test` |
| Rodar testes instrumentados | `gradlew.bat connectedAndroidTest` | `./gradlew connectedAndroidTest` |
| Limpar arquivos de build | `gradlew.bat clean` | `./gradlew clean` |

O APK gerado fica em `app/build/outputs/apk/debug/app-debug.apk`.

> O APK serve apenas para testes locais — **não** é aceito na entrega do projeto.

---

## 6. Executando os testes

| Tipo | Arquivo | Precisa de emulador? | Como rodar no Android Studio |
|---|---|---|---|
| Unitário | `app/src/test/.../RegistroRepositoryTest.java` | Não | Botão direito na pasta `test` → **Run 'Tests in ...'** |
| Instrumentado (Espresso) | `app/src/androidTest/.../FormularioRegistroActivityTest.java` | Sim | Com o emulador aberto, botão direito na pasta `androidTest` → **Run 'Tests in ...'** |

Os testes unitários validam o CRUD do `RegistroRepository` (adicionar, buscar, atualizar, remover,
desfazer e filtrar). Os instrumentados percorrem o formulário na interface real.

### Acompanhando o ciclo de vida no Logcat

Com o app rodando, abra a janela **Logcat** e filtre por `tag:CicloDeVida` para ver as chamadas
`onCreate`, `onStart`, `onResume`, `onPause`, `onStop`, `onRestart` e `onDestroy` de cada tela.

---

## 7. Gerando o .zip de entrega

1. Feche o Android Studio.
2. (Opcional, deixa o arquivo menor) apague as pastas geradas automaticamente: `build/`, `app/build/`
   e `.gradle/`. Elas são recriadas na próxima compilação.
3. Confira se a pasta raiz se chama `TIN2_grupo2_devmobile_projfinal`.
4. Compacte a pasta inteira com o nome **`TIN2_grupo2_projfinal.zip`**.
   - Windows: botão direito na pasta → **Enviar para › Pasta compactada**, depois renomeie.
5. Envie o .zip pelo Canvas.

---

## 8. Problemas comuns

| Problema | Solução |
|---|---|
| `SDK location not found` | Abra o projeto pelo Android Studio (ele cria o `local.properties` sozinho) ou crie o arquivo com `sdk.dir=C:\\Users\\<usuario>\\AppData\\Local\\Android\\Sdk` |
| `Android Gradle plugin requires Java 17` | **File › Settings › Build, Execution, Deployment › Build Tools › Gradle** → em *Gradle JDK*, escolha um JDK 17 ou 21 (ex.: *jbr-21*) |
| Build falha logo no início com `What went wrong: 25.0.x` (ou outra versão 22+) | O Gradle está rodando com JDK novo demais. Escolha um JDK 21 em *Gradle JDK* (Android Studio) ou no `JAVA_HOME` (linha de comando) |
| Testes instrumentados falham com `NoSuchMethodException: InputManager.getInstance` | Espresso antigo em emulador Android 15+. O projeto já usa Espresso 3.7.0; confira se o `libs.versions.toml` não foi revertido |
| `Failed to find target with hash string 'android-35'` | Instale a **API 35** no SDK Manager (seção 1) |
| Gradle Sync demorado ou falhando por rede | Verifique a conexão na primeira sincronização e clique em **File › Sync Project with Gradle Files** |
| Emulador não inicia | Ative a virtualização (VT-x / AMD-V) na BIOS e confira em **Device Manager** se a imagem do sistema foi baixada |
| Erro de caminho muito longo no Windows | Mova o projeto para um caminho curto, como `C:\projetos\` |
| Dados sumiram ao reabrir o app | Comportamento esperado: os dados ficam em memória, como exige o enunciado |
