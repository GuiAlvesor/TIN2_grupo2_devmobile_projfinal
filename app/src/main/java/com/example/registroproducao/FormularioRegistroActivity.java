package com.example.registroproducao;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.registroproducao.data.RegistroRepository;
import com.example.registroproducao.model.Categoria;
import com.example.registroproducao.model.ItemChecklist;
import com.example.registroproducao.model.Prioridade;
import com.example.registroproducao.model.Registro;
import com.example.registroproducao.util.Constantes;
import com.example.registroproducao.util.TextWatcherSimples;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity de cadastro de registros, com o CRUD completo:
 * - CREATE: sem id no Intent, cadastra um novo registro;
 * - READ:   com id no Intent, carrega o registro do repositório e preenche os campos;
 * - UPDATE: no modo de edição, salva as alterações;
 * - DELETE: no modo de edição, o ícone de lixeira da Toolbar exclui o registro.
 * Para Listas de controle, também permite digitar os itens do checklist (um por linha).
 */
public class FormularioRegistroActivity extends BaseActivity {

    private static final int TAMANHO_MINIMO_TITULO = 3;
    private static final String ESTADO_CATEGORIA = "estado_categoria";
    private static final String ESTADO_PRIORIDADE = "estado_prioridade";

    @NonNull
    public static Intent criarIntentNovo(@NonNull Context contexto, @Nullable Categoria categoriaInicial) {
        Intent intent = new Intent(contexto, FormularioRegistroActivity.class);
        if (categoriaInicial != null) {
            intent.putExtra(Constantes.EXTRA_CATEGORIA, categoriaInicial.name());
        }
        return intent;
    }

    @NonNull
    public static Intent criarIntentEdicao(@NonNull Context contexto, int registroId) {
        return new Intent(contexto, FormularioRegistroActivity.class)
                .putExtra(Constantes.EXTRA_REGISTRO_ID, registroId);
    }

    private RegistroRepository repositorio;
    /** Registro original em edição; {@code null} quando é um novo cadastro. */
    @Nullable private Registro registroEmEdicao;

    private Categoria categoriaSelecionada = Categoria.REGISTRO;
    private Prioridade prioridadeSelecionada = Prioridade.MEDIA;

    private View blocoRegistro;
    private View blocoLista;
    private View blocoAnotacao;
    private MaterialButton btnPrioridadeBaixa;
    private MaterialButton btnPrioridadeMedia;
    private MaterialButton btnPrioridadeAlta;
    private TextInputLayout tilTitulo;
    private TextInputEditText etTitulo;
    private TextInputEditText etSetor;
    private TextInputEditText etDescricao;
    private View layoutItens;
    private TextInputEditText etItens;
    private MaterialSwitch swConcluido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_registro);
        aplicarInsetsDeSistema(findViewById(R.id.appBar), findViewById(R.id.root));

        repositorio = RegistroRepository.getInstance();

        int id = getIntent().getIntExtra(Constantes.EXTRA_REGISTRO_ID, Constantes.ID_INVALIDO);
        if (id != Constantes.ID_INVALIDO) {
            registroEmEdicao = repositorio.buscarPorId(id);
            if (registroEmEdicao == null) {
                mostrarMensagem(R.string.msg_registro_nao_encontrado);
                finish();
                return;
            }
        }
        boolean modoEdicao = registroEmEdicao != null;

        // Toolbar: X para fechar; no modo de edição, lixeira para excluir (DELETE).
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        configurarToolbar(toolbar, 0, R.drawable.ic_close, R.string.acao_fechar);
        if (modoEdicao) {
            toolbar.inflateMenu(R.menu.menu_formulario);
            toolbar.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menuExcluir) {
                    confirmarExclusao();
                    return true;
                }
                return false;
            });
        }
        ((TextView) findViewById(R.id.tvTituloFormulario))
                .setText(modoEdicao ? R.string.titulo_editar : R.string.form_titulo_novo);

        vincularViews();
        configurarSeletores();

        findViewById(R.id.btnSalvar).setOnClickListener(v -> salvar());
        findViewById(R.id.linhaResolvido).setOnClickListener(v -> swConcluido.toggle());

        if (savedInstanceState == null) {
            preencherCampos();
        } else {
            // Os textos dos campos o Android restaura sozinho; categoria e prioridade são restauradas aqui.
            Categoria categoria = Categoria.deNome(savedInstanceState.getString(ESTADO_CATEGORIA));
            selecionarCategoria(categoria != null ? categoria : Categoria.REGISTRO);
            selecionarPrioridade(prioridadeDeNome(savedInstanceState.getString(ESTADO_PRIORIDADE)));
        }

        etTitulo.addTextChangedListener(new TextWatcherSimples() {
            @Override
            public void afterTextChanged(Editable s) {
                tilTitulo.setError(null);
            }
        });

        // Intercepta o "voltar" (e o X) para avisar sobre alterações não salvas.
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (possuiAlteracoes()) {
                    confirmarDescarte();
                } else {
                    finish();
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ESTADO_CATEGORIA, categoriaSelecionada.name());
        outState.putString(ESTADO_PRIORIDADE, prioridadeSelecionada.name());
    }

    private void vincularViews() {
        blocoRegistro = findViewById(R.id.blocoRegistro);
        blocoLista = findViewById(R.id.blocoLista);
        blocoAnotacao = findViewById(R.id.blocoAnotacao);
        btnPrioridadeBaixa = findViewById(R.id.btnPrioridadeBaixa);
        btnPrioridadeMedia = findViewById(R.id.btnPrioridadeMedia);
        btnPrioridadeAlta = findViewById(R.id.btnPrioridadeAlta);
        tilTitulo = findViewById(R.id.tilTitulo);
        etTitulo = findViewById(R.id.etTitulo);
        etSetor = findViewById(R.id.etSetor);
        etDescricao = findViewById(R.id.etDescricao);
        layoutItens = findViewById(R.id.layoutItens);
        etItens = findViewById(R.id.etItens);
        swConcluido = findViewById(R.id.swConcluido);
    }

    private void configurarSeletores() {
        blocoRegistro.setOnClickListener(v -> selecionarCategoria(Categoria.REGISTRO));
        blocoLista.setOnClickListener(v -> selecionarCategoria(Categoria.LISTA_CONTROLE));
        blocoAnotacao.setOnClickListener(v -> selecionarCategoria(Categoria.ANOTACAO));

        btnPrioridadeBaixa.setOnClickListener(v -> selecionarPrioridade(Prioridade.BAIXA));
        btnPrioridadeMedia.setOnClickListener(v -> selecionarPrioridade(Prioridade.MEDIA));
        btnPrioridadeAlta.setOnClickListener(v -> selecionarPrioridade(Prioridade.ALTA));
    }

    private void preencherCampos() {
        if (registroEmEdicao != null) {
            etTitulo.setText(registroEmEdicao.getTitulo());
            etSetor.setText(registroEmEdicao.getSetor());
            etDescricao.setText(registroEmEdicao.getDescricao());
            etItens.setText(textoDosItens(registroEmEdicao.getItens()));
            selecionarCategoria(registroEmEdicao.getCategoria());
            selecionarPrioridade(registroEmEdicao.getPrioridade());
            swConcluido.setChecked(registroEmEdicao.isConcluido());
        } else {
            Categoria inicial = Categoria.deNome(getIntent().getStringExtra(Constantes.EXTRA_CATEGORIA));
            selecionarCategoria(inicial != null ? inicial : Categoria.REGISTRO);
            selecionarPrioridade(Prioridade.MEDIA);
            swConcluido.setChecked(false);
        }
    }

    // ===================== Seletores (categoria e prioridade) =====================

    private void selecionarCategoria(@NonNull Categoria categoria) {
        categoriaSelecionada = categoria;
        blocoRegistro.setSelected(categoria == Categoria.REGISTRO);
        blocoLista.setSelected(categoria == Categoria.LISTA_CONTROLE);
        blocoAnotacao.setSelected(categoria == Categoria.ANOTACAO);
        layoutItens.setVisibility(categoria == Categoria.LISTA_CONTROLE ? View.VISIBLE : View.GONE);
    }

    private void selecionarPrioridade(@NonNull Prioridade prioridade) {
        prioridadeSelecionada = prioridade;
        btnPrioridadeBaixa.setChecked(prioridade == Prioridade.BAIXA);
        btnPrioridadeMedia.setChecked(prioridade == Prioridade.MEDIA);
        btnPrioridadeAlta.setChecked(prioridade == Prioridade.ALTA);
    }

    @NonNull
    private static Prioridade prioridadeDeNome(@Nullable String nome) {
        for (Prioridade p : Prioridade.values()) {
            if (p.name().equals(nome)) return p;
        }
        return Prioridade.MEDIA;
    }

    // ===================== Leitura e validação =====================

    @NonNull
    private Registro lerFormulario() {
        Registro registro = registroEmEdicao != null ? new Registro(registroEmEdicao) : new Registro();
        registro.setTitulo(textoDe(etTitulo));
        registro.setSetor(textoDe(etSetor));
        registro.setDescricao(textoDe(etDescricao));
        registro.setCategoria(categoriaSelecionada);
        registro.setPrioridade(prioridadeSelecionada);
        registro.setConcluido(swConcluido.isChecked());
        registro.setItens(categoriaSelecionada == Categoria.LISTA_CONTROLE
                ? lerItens(registroEmEdicao != null ? registroEmEdicao.getItens() : new ArrayList<>())
                : new ArrayList<>());
        return registro;
    }

    /**
     * Converte o texto (um item por linha) em itens do checklist. Itens que já existiam
     * mantêm as respostas; com o campo vazio são usados os 5 itens padrão de segurança.
     */
    @NonNull
    private List<ItemChecklist> lerItens(@NonNull List<ItemChecklist> anteriores) {
        List<String> perguntas = new ArrayList<>();
        for (String linha : textoDe(etItens).split("\n")) {
            if (!linha.trim().isEmpty()) perguntas.add(linha.trim());
        }
        if (perguntas.isEmpty()) {
            for (String padrao : getResources().getStringArray(R.array.itens_checklist_padrao)) {
                perguntas.add(padrao);
            }
        }

        List<ItemChecklist> itens = new ArrayList<>();
        for (String pergunta : perguntas) {
            ItemChecklist item = new ItemChecklist(pergunta);
            for (ItemChecklist anterior : anteriores) {
                if (anterior.getPergunta().equals(pergunta)) {
                    item = new ItemChecklist(anterior);
                    break;
                }
            }
            itens.add(item);
        }
        return itens;
    }

    @NonNull
    private static String textoDosItens(@NonNull List<ItemChecklist> itens) {
        List<String> perguntas = new ArrayList<>();
        for (ItemChecklist item : itens) perguntas.add(item.getPergunta());
        return TextUtils.join("\n", perguntas);
    }

    private boolean validar() {
        String titulo = textoDe(etTitulo);
        if (titulo.isEmpty()) {
            tilTitulo.setError(getString(R.string.erro_titulo_obrigatorio));
        } else if (titulo.length() < TAMANHO_MINIMO_TITULO) {
            tilTitulo.setError(getString(R.string.erro_titulo_curto, TAMANHO_MINIMO_TITULO));
        } else {
            tilTitulo.setError(null);
            return true;
        }
        etTitulo.requestFocus();
        mostrarMensagem(R.string.msg_verifique_campos);
        return false;
    }

    // ===================== Ações =====================

    private void salvar() {
        if (!validar()) return;

        Registro registro = lerFormulario();
        if (registroEmEdicao != null) {
            if (repositorio.atualizar(registro)) {
                mostrarMensagem(R.string.msg_registro_atualizado);
            } else {
                mostrarMensagem(R.string.msg_registro_nao_encontrado);
            }
        } else {
            Registro salvo = repositorio.adicionar(registro);
            mostrarMensagem(R.string.msg_registro_cadastrado);
            // Uma nova Lista de controle pendente já abre para ser respondida.
            if (salvo.getCategoria() == Categoria.LISTA_CONTROLE && !salvo.isConcluido()) {
                startActivity(ChecklistActivity.criarIntent(this, salvo.getId()));
            }
        }
        setResult(RESULT_OK);
        finish();
    }

    /** DELETE: confirma e remove o registro em edição do repositório em memória. */
    private void confirmarExclusao() {
        if (registroEmEdicao == null) return;
        new MaterialAlertDialogBuilder(this)
                .setIcon(R.drawable.ic_delete_alerta)
                .setTitle(R.string.dialogo_excluir_titulo)
                .setMessage(getString(R.string.dialogo_excluir_mensagem, registroEmEdicao.getTitulo()))
                .setPositiveButton(R.string.acao_excluir, (d, w) -> excluir())
                .setNegativeButton(R.string.acao_cancelar, null)
                .show();
    }

    private void excluir() {
        Registro removido = repositorio.remover(registroEmEdicao.getId());
        if (removido == null) {
            mostrarMensagem(R.string.msg_registro_nao_encontrado);
            finish();
            return;
        }
        // Devolve o resultado para quem abriu o formulário (detalhes -> lista),
        // que exibirá a mensagem de exclusão com a opção "Desfazer".
        Intent dados = new Intent().putExtra(Constantes.EXTRA_TITULO_REGISTRO, removido.getTitulo());
        setResult(Constantes.RESULTADO_REGISTRO_EXCLUIDO, dados);
        finish();
    }

    private boolean possuiAlteracoes() {
        if (registroEmEdicao != null) {
            return !lerFormulario().temMesmoConteudo(registroEmEdicao);
        }
        return !textoDe(etTitulo).isEmpty()
                || !textoDe(etSetor).isEmpty()
                || !textoDe(etDescricao).isEmpty()
                || !textoDe(etItens).isEmpty();
    }

    private void confirmarDescarte() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialogo_descartar_titulo)
                .setMessage(R.string.dialogo_descartar_mensagem)
                .setPositiveButton(R.string.acao_descartar, (d, w) -> {
                    mostrarMensagem(R.string.msg_alteracoes_descartadas);
                    finish();
                })
                .setNegativeButton(R.string.acao_continuar_editando, null)
                .show();
    }

    @NonNull
    private static String textoDe(@NonNull TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }
}
