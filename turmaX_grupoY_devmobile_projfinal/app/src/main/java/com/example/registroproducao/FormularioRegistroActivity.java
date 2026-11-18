package com.example.registroproducao;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.registroproducao.data.RegistroRepository;
import com.example.registroproducao.model.Categoria;
import com.example.registroproducao.model.Prioridade;
import com.example.registroproducao.model.Registro;
import com.example.registroproducao.util.Constantes;
import com.example.registroproducao.util.TextWatcherSimples;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Activity de cadastro de registros, com o CRUD completo:
 * - CREATE: sem id no Intent, cadastra um novo registro;
 * - READ:   com id no Intent, carrega o registro do repositório e preenche os campos;
 * - UPDATE: no modo de edição, salva as alterações;
 * - DELETE: no modo de edição, o ícone de lixeira da Toolbar exclui o registro.
 */
public class FormularioRegistroActivity extends BaseActivity {

    private static final int TAMANHO_MINIMO_TITULO = 3;

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

    private ChipGroup chipGroupCategoria;
    private TextInputLayout tilTitulo;
    private TextInputEditText etTitulo;
    private TextInputEditText etSetor;
    private TextInputEditText etDescricao;
    private MaterialButtonToggleGroup togglePrioridade;
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

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        configurarToolbar(toolbar, modoEdicao ? R.string.titulo_editar : R.string.titulo_novo, true);
        if (modoEdicao) {
            // No modo de edição a Toolbar oferece a ação de excluir (DELETE).
            toolbar.inflateMenu(R.menu.menu_formulario);
            toolbar.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menuExcluir) {
                    confirmarExclusao();
                    return true;
                }
                return false;
            });
        }

        chipGroupCategoria = findViewById(R.id.chipGroupCategoria);
        tilTitulo = findViewById(R.id.tilTitulo);
        etTitulo = findViewById(R.id.etTitulo);
        etSetor = findViewById(R.id.etSetor);
        etDescricao = findViewById(R.id.etDescricao);
        togglePrioridade = findViewById(R.id.togglePrioridade);
        swConcluido = findViewById(R.id.swConcluido);

        MaterialButton btnSalvar = findViewById(R.id.btnSalvar);
        btnSalvar.setText(modoEdicao ? R.string.acao_salvar_alteracoes : R.string.acao_cadastrar);
        btnSalvar.setOnClickListener(v -> salvar());
        findViewById(R.id.btnCancelar).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // Ao recriar a tela (ex.: rotação), os campos são restaurados automaticamente pelo Android.
        if (savedInstanceState == null) {
            preencherCampos();
        }

        etTitulo.addTextChangedListener(new TextWatcherSimples() {
            @Override
            public void afterTextChanged(Editable s) {
                tilTitulo.setError(null);
            }
        });

        // Intercepta o "voltar" para avisar sobre alterações não salvas.
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

    private void preencherCampos() {
        if (registroEmEdicao != null) {
            etTitulo.setText(registroEmEdicao.getTitulo());
            etSetor.setText(registroEmEdicao.getSetor());
            etDescricao.setText(registroEmEdicao.getDescricao());
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

    // ===================== Leitura e validação =====================

    @NonNull
    private Registro lerFormulario() {
        Registro registro = registroEmEdicao != null ? new Registro(registroEmEdicao) : new Registro();
        registro.setTitulo(textoDe(etTitulo));
        registro.setSetor(textoDe(etSetor));
        registro.setDescricao(textoDe(etDescricao));
        registro.setCategoria(getCategoriaSelecionada());
        registro.setPrioridade(getPrioridadeSelecionada());
        registro.setConcluido(swConcluido.isChecked());
        return registro;
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
            repositorio.adicionar(registro);
            mostrarMensagem(R.string.msg_registro_cadastrado);
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
        Registro atual = lerFormulario();
        if (registroEmEdicao != null) {
            return !atual.temMesmoConteudo(registroEmEdicao);
        }
        return !atual.getTitulo().isEmpty()
                || !atual.getSetor().isEmpty()
                || !atual.getDescricao().isEmpty();
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

    // ===================== Conversões tela <-> modelo =====================

    @NonNull
    private Categoria getCategoriaSelecionada() {
        int id = chipGroupCategoria.getCheckedChipId();
        if (id == R.id.chipCategoriaListas) return Categoria.LISTA_CONTROLE;
        if (id == R.id.chipCategoriaAnotacoes) return Categoria.ANOTACAO;
        return Categoria.REGISTRO;
    }

    private void selecionarCategoria(@NonNull Categoria categoria) {
        if (categoria == Categoria.LISTA_CONTROLE) chipGroupCategoria.check(R.id.chipCategoriaListas);
        else if (categoria == Categoria.ANOTACAO) chipGroupCategoria.check(R.id.chipCategoriaAnotacoes);
        else chipGroupCategoria.check(R.id.chipCategoriaRegistros);
    }

    @NonNull
    private Prioridade getPrioridadeSelecionada() {
        int id = togglePrioridade.getCheckedButtonId();
        if (id == R.id.btnPrioridadeBaixa) return Prioridade.BAIXA;
        if (id == R.id.btnPrioridadeAlta) return Prioridade.ALTA;
        return Prioridade.MEDIA;
    }

    private void selecionarPrioridade(@NonNull Prioridade prioridade) {
        if (prioridade == Prioridade.BAIXA) togglePrioridade.check(R.id.btnPrioridadeBaixa);
        else if (prioridade == Prioridade.ALTA) togglePrioridade.check(R.id.btnPrioridadeAlta);
        else togglePrioridade.check(R.id.btnPrioridadeMedia);
    }

    @NonNull
    private static String textoDe(@NonNull TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }
}
