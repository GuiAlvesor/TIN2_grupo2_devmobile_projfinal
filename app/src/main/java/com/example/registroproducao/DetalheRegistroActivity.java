package com.example.registroproducao;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.example.registroproducao.data.RegistroRepository;
import com.example.registroproducao.model.Categoria;
import com.example.registroproducao.model.Registro;
import com.example.registroproducao.util.Constantes;
import com.example.registroproducao.util.EstiloVisual;
import com.example.registroproducao.util.FormatadorData;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

/**
 * Exibe todos os dados de um registro (READ) e permite editar (UPDATE),
 * alterar o status rapidamente (UPDATE) ou excluir (DELETE) com confirmação.
 * Para Listas de controle, mostra o progresso e o botão "Abrir checklist".
 */
public class DetalheRegistroActivity extends BaseActivity {

    @NonNull
    public static Intent criarIntent(@NonNull Context contexto, int registroId) {
        return new Intent(contexto, DetalheRegistroActivity.class)
                .putExtra(Constantes.EXTRA_REGISTRO_ID, registroId);
    }

    private RegistroRepository repositorio;
    private int registroId;
    private Registro registro;

    private View raiz;
    private ImageView ivIconeCategoria;
    private TextView tvCategoria;
    private TextView tvTitulo;
    private TextView tvPrioridade;
    private TextView tvStatus;
    private TextView tvSetor;
    private TextView tvDescricao;
    private TextView tvCriadoEm;
    private TextView tvAtualizadoEm;
    private View linhaAtualizadoEm;
    private MaterialButton btnAlternarStatus;
    private View cardChecklist;
    private TextView tvChecklistProgresso;
    private LinearProgressIndicator progressoChecklist;

    /**
     * Abre o formulário de edição. Se o registro for excluído lá, repassa o
     * resultado para a lista (que mostra "Desfazer") e fecha esta tela.
     */
    private final ActivityResultLauncher<Intent> edicaoLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), resultado -> {
                if (resultado.getResultCode() == Constantes.RESULTADO_REGISTRO_EXCLUIDO) {
                    setResult(Constantes.RESULTADO_REGISTRO_EXCLUIDO, resultado.getData());
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhe_registro);

        raiz = findViewById(R.id.root);
        aplicarInsetsDeSistema(findViewById(R.id.appBar), raiz);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        configurarToolbar(toolbar, R.string.titulo_detalhes, true);

        repositorio = RegistroRepository.getInstance();
        registroId = getIntent().getIntExtra(Constantes.EXTRA_REGISTRO_ID, Constantes.ID_INVALIDO);

        ivIconeCategoria = findViewById(R.id.ivIconeCategoria);
        tvCategoria = findViewById(R.id.tvCategoria);
        tvTitulo = findViewById(R.id.tvTitulo);
        tvPrioridade = findViewById(R.id.tvPrioridade);
        tvStatus = findViewById(R.id.tvStatus);
        tvSetor = findViewById(R.id.tvSetor);
        tvDescricao = findViewById(R.id.tvDescricao);
        tvCriadoEm = findViewById(R.id.tvCriadoEm);
        tvAtualizadoEm = findViewById(R.id.tvAtualizadoEm);
        linhaAtualizadoEm = findViewById(R.id.linhaAtualizadoEm);
        btnAlternarStatus = findViewById(R.id.btnAlternarStatus);
        cardChecklist = findViewById(R.id.cardChecklist);
        tvChecklistProgresso = findViewById(R.id.tvChecklistProgresso);
        progressoChecklist = findViewById(R.id.progressoChecklist);

        findViewById(R.id.btnEditar).setOnClickListener(v ->
                edicaoLauncher.launch(FormularioRegistroActivity.criarIntentEdicao(this, registroId)));
        findViewById(R.id.btnExcluir).setOnClickListener(v -> confirmarExclusao());
        btnAlternarStatus.setOnClickListener(v -> alternarStatus());
        findViewById(R.id.btnAbrirChecklist).setOnClickListener(v ->
                startActivity(ChecklistActivity.criarIntent(this, registroId)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarrega os dados toda vez que a tela volta ao primeiro plano (ex.: após editar).
        carregarRegistro();
    }

    private void carregarRegistro() {
        if (isFinishing()) return; // a tela já está sendo fechada (ex.: registro excluído na edição)
        registro = repositorio.buscarPorId(registroId);
        if (registro == null) {
            mostrarMensagem(R.string.msg_registro_nao_encontrado);
            finish();
            return;
        }
        exibir(registro);
    }

    private void exibir(@NonNull Registro r) {
        Categoria categoria = r.getCategoria();
        EstiloVisual.aplicarIconeCategoria(ivIconeCategoria, categoria);
        tvCategoria.setText(categoria.getRotuloRes());
        tvTitulo.setText(r.getTitulo());

        // Prioridade e status como pílulas (forma + texto, não só cor).
        EstiloVisual.aplicarPrioridade(tvPrioridade, r.getPrioridade());
        tvPrioridade.setText(getString(R.string.rotulo_prioridade_valor, getString(r.getPrioridade().getRotuloRes())));
        EstiloVisual.aplicarStatusPilula(tvStatus, r.isConcluido());

        boolean ehChecklist = categoria == Categoria.LISTA_CONTROLE;
        cardChecklist.setVisibility(ehChecklist ? View.VISIBLE : View.GONE);
        if (ehChecklist) {
            tvChecklistProgresso.setText(getString(R.string.checklist_progresso,
                    r.getItensRespondidos(), r.getTotalItens()));
            progressoChecklist.setProgressCompat(r.getPercentualRespondido(), false);
        }

        tvSetor.setText(r.getSetor().isEmpty() ? getString(R.string.setor_nao_informado) : r.getSetor());
        tvDescricao.setText(r.getDescricao().isEmpty() ? getString(R.string.descricao_vazia) : r.getDescricao());

        tvCriadoEm.setText(FormatadorData.formatar(r.getDataCriacao()));
        boolean foiAtualizado = r.getDataAtualizacao() > r.getDataCriacao();
        linhaAtualizadoEm.setVisibility(foiAtualizado ? View.VISIBLE : View.GONE);
        tvAtualizadoEm.setText(FormatadorData.formatar(r.getDataAtualizacao()));

        btnAlternarStatus.setText(r.isConcluido() ? R.string.acao_reabrir : R.string.acao_marcar_concluido);
        btnAlternarStatus.setIconResource(r.isConcluido() ? R.drawable.ic_replay : R.drawable.ic_check_circle);
    }

    private void alternarStatus() {
        registro.setConcluido(!registro.isConcluido());
        if (repositorio.atualizar(registro)) {
            carregarRegistro();
            Snackbar.make(raiz, registro.isConcluido()
                            ? R.string.msg_marcado_concluido : R.string.msg_registro_reaberto,
                    Snackbar.LENGTH_SHORT).setAnchorView(R.id.barraAcoes).show();
        }
    }

    private void confirmarExclusao() {
        new MaterialAlertDialogBuilder(this)
                .setIcon(R.drawable.ic_delete_alerta)
                .setTitle(R.string.dialogo_excluir_titulo)
                .setMessage(getString(R.string.dialogo_excluir_mensagem, registro.getTitulo()))
                .setPositiveButton(R.string.acao_excluir, (d, w) -> excluir())
                .setNegativeButton(R.string.acao_cancelar, null)
                .show();
    }

    private void excluir() {
        Registro removido = repositorio.remover(registroId);
        if (removido == null) {
            mostrarMensagem(R.string.msg_registro_nao_encontrado);
            finish();
            return;
        }
        // Devolve o resultado para a lista, que mostrará a mensagem com opção "Desfazer".
        Intent dados = new Intent().putExtra(Constantes.EXTRA_TITULO_REGISTRO, removido.getTitulo());
        setResult(Constantes.RESULTADO_REGISTRO_EXCLUIDO, dados);
        finish();
    }
}
