package com.example.registroproducao;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import com.example.registroproducao.data.RegistroRepository;
import com.example.registroproducao.model.Categoria;
import com.example.registroproducao.model.FiltroLista;
import com.example.registroproducao.model.Registro;
import com.example.registroproducao.util.Constantes;
import com.example.registroproducao.util.EstiloVisual;
import com.example.registroproducao.util.FormatadorData;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

/**
 * Tela inicial (painel). Mostra os pendentes do turno, ações rápidas, o checklist em
 * andamento, uma dica de segurança e os registros mais recentes. Todos os dados são
 * recalculados em {@link #onResume()}, ou seja, sempre que o usuário volta para esta tela.
 */
public class MainActivity extends BaseActivity {

    private static final int QUANTIDADE_RECENTES = 2;

    private RegistroRepository repositorio;

    private View raiz;
    private TextView tvQtdPendentes;
    private TextView tvResumoPendentes;
    private View cardEmAndamento;
    private TextView tvEmAndamentoTitulo;
    private TextView tvEmAndamentoSetor;
    private TextView tvEmAndamentoItens;
    private LinearProgressIndicator progressoEmAndamento;
    private LinearLayout containerRecentes;
    private TextView tvRecentesVazio;
    private BottomNavigationView navegacaoInferior;

    /** Checklist exibido no card "Em andamento" (null quando não há nenhum). */
    @Nullable private Registro checklistEmAndamento;

    /** Abre os detalhes de um registro recente; se ele for excluído lá, oferece "Desfazer". */
    private final ActivityResultLauncher<Intent> detalheLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), resultado -> {
                if (resultado.getResultCode() == Constantes.RESULTADO_REGISTRO_EXCLUIDO
                        && resultado.getData() != null) {
                    mostrarSnackbarExclusao(resultado.getData().getStringExtra(Constantes.EXTRA_TITULO_REGISTRO));
                }
            });

    @Override
    protected boolean usaCabecalhoEscuro() {
        return true; // cabeçalho vermelho: ícones claros na status bar
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        raiz = findViewById(R.id.root);
        aplicarInsetsDeSistema(findViewById(R.id.rolagem), raiz, findViewById(R.id.faixaStatusBar));

        repositorio = RegistroRepository.getInstance();
        repositorio.carregarExemplosSeNecessario();

        tvQtdPendentes = findViewById(R.id.tvQtdPendentes);
        tvResumoPendentes = findViewById(R.id.tvResumoPendentes);
        cardEmAndamento = findViewById(R.id.cardEmAndamento);
        tvEmAndamentoTitulo = findViewById(R.id.tvEmAndamentoTitulo);
        tvEmAndamentoSetor = findViewById(R.id.tvEmAndamentoSetor);
        tvEmAndamentoItens = findViewById(R.id.tvEmAndamentoItens);
        progressoEmAndamento = findViewById(R.id.progressoEmAndamento);
        containerRecentes = findViewById(R.id.containerRecentes);
        tvRecentesVazio = findViewById(R.id.tvRecentesVazio);
        navegacaoInferior = findViewById(R.id.navegacaoInferior);

        configurarNavegacao();
    }

    @Override
    protected void onResume() {
        super.onResume();
        navegacaoInferior.getMenu().findItem(R.id.navInicio).setChecked(true);
        atualizarPainel();
    }

    // ===================== Navegação (Intents explícitas) =====================

    private void configurarNavegacao() {
        findViewById(R.id.btnBuscar).setOnClickListener(v -> abrirLista(FiltroLista.TODOS));
        findViewById(R.id.btnAjuda).setOnClickListener(v -> mostrarAjuda());
        findViewById(R.id.areaPendentes).setOnClickListener(v -> abrirLista(FiltroLista.PENDENTES));

        findViewById(R.id.acaoNovoRegistro).setOnClickListener(v -> abrirFormulario(null));
        findViewById(R.id.acaoChecklist).setOnClickListener(v -> abrirFormulario(Categoria.LISTA_CONTROLE));
        findViewById(R.id.acaoAnotacao).setOnClickListener(v -> abrirFormulario(Categoria.ANOTACAO));
        findViewById(R.id.acaoRegistros).setOnClickListener(v -> abrirLista(FiltroLista.TODOS));

        cardEmAndamento.setOnClickListener(v -> {
            if (checklistEmAndamento != null) abrirChecklist(checklistEmAndamento.getId());
        });
        // Dica de EPIs: continua o checklist pendente ou começa uma nova lista de controle.
        findViewById(R.id.tvAbrirChecklistEpi).setOnClickListener(v -> {
            if (checklistEmAndamento != null) {
                abrirChecklist(checklistEmAndamento.getId());
            } else {
                abrirFormulario(Categoria.LISTA_CONTROLE);
            }
        });
        findViewById(R.id.tvVerTodos).setOnClickListener(v -> abrirLista(FiltroLista.TODOS));

        // Os ids de R não são constantes (AGP 8+), por isso if/else em vez de switch.
        navegacaoInferior.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navInicio) {
                ((NestedScrollView) findViewById(R.id.rolagem)).smoothScrollTo(0, 0);
                return true;
            }
            if (id == R.id.navRegistros) {
                abrirLista(FiltroLista.TODOS);
            } else if (id == R.id.navChecklists) {
                abrirLista(FiltroLista.CHECKLISTS);
            } else if (id == R.id.navAjustes) {
                mostrarMensagem(R.string.msg_em_breve);
            }
            return false; // mantém "Início" como item ativo nesta tela
        });
    }

    private void abrirLista(@NonNull FiltroLista filtro) {
        startActivity(ListaRegistrosActivity.criarIntent(this, filtro));
    }

    private void abrirFormulario(@Nullable Categoria categoria) {
        startActivity(FormularioRegistroActivity.criarIntentNovo(this, categoria));
    }

    private void abrirChecklist(int registroId) {
        startActivity(ChecklistActivity.criarIntent(this, registroId));
    }

    private void mostrarAjuda() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialogo_ajuda_titulo)
                .setMessage(R.string.dialogo_ajuda_mensagem)
                .setPositiveButton(R.string.acao_entendi, null)
                .show();
    }

    // ===================== Conteúdo =====================

    private void atualizarPainel() {
        atualizarPendentes();
        atualizarEmAndamento();
        atualizarRecentes();
    }

    /** "N registros" + "X de alta prioridade · Y no total" (X em vermelho escuro). */
    private void atualizarPendentes() {
        int pendentes = repositorio.contarPendentes();
        tvQtdPendentes.setText(getResources().getQuantityString(
                R.plurals.contador_registros, pendentes, pendentes));

        String alta = getString(R.string.main_alta_prioridade, repositorio.contarPrioridadeAltaPendentes());
        SpannableStringBuilder resumo = new SpannableStringBuilder(alta);
        resumo.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.vermelho_escuro)),
                0, alta.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        resumo.append(getString(R.string.main_no_total, repositorio.contarTotal()));
        tvResumoPendentes.setText(resumo);
    }

    /** Card preto com a lista de controle pendente mais recente; escondido se não houver. */
    private void atualizarEmAndamento() {
        checklistEmAndamento = repositorio.buscarChecklistEmAndamento();
        if (checklistEmAndamento == null) {
            cardEmAndamento.setVisibility(View.GONE);
            return;
        }
        Registro c = checklistEmAndamento;
        cardEmAndamento.setVisibility(View.VISIBLE);
        tvEmAndamentoTitulo.setText(c.getTitulo());
        tvEmAndamentoSetor.setText(c.getSetor().isEmpty() ? getString(R.string.setor_nao_informado) : c.getSetor());
        tvEmAndamentoItens.setText(getString(R.string.main_progresso_itens,
                c.getItensRespondidos(), c.getTotalItens()));
        progressoEmAndamento.setProgressCompat(c.getPercentualRespondido(), false);
    }

    private void atualizarRecentes() {
        containerRecentes.removeAllViews();
        List<Registro> recentes = repositorio.listarRecentes(QUANTIDADE_RECENTES);
        tvRecentesVazio.setVisibility(recentes.isEmpty() ? View.VISIBLE : View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < recentes.size(); i++) {
            if (i > 0) adicionarDivisoria();
            Registro r = recentes.get(i);
            View linha = inflater.inflate(R.layout.item_recente, containerRecentes, false);

            EstiloVisual.aplicarIconeCategoria(linha.findViewById(R.id.ivIconeCategoria), r.getCategoria());
            ((TextView) linha.findViewById(R.id.tvTitulo)).setText(r.getTitulo());
            ((TextView) linha.findViewById(R.id.tvMeta)).setText(montarMeta(r));
            EstiloVisual.aplicarPrioridade(linha.findViewById(R.id.tvPrioridade), r.getPrioridade());

            linha.setOnClickListener(v ->
                    detalheLauncher.launch(DetalheRegistroActivity.criarIntent(this, r.getId())));
            containerRecentes.addView(linha);
        }
    }

    /** "Categoria · Setor · hora" (o setor é omitido quando não foi informado). */
    @NonNull
    private String montarMeta(@NonNull Registro r) {
        String categoria = getString(r.getCategoria().getRotuloRes());
        String hora = FormatadorData.formatarHora(r.getDataCriacao());
        return r.getSetor().isEmpty()
                ? getString(R.string.formato_meta_2, categoria, hora)
                : getString(R.string.formato_meta_3, categoria, r.getSetor(), hora);
    }

    private void adicionarDivisoria() {
        View divisoria = new View(this);
        divisoria.setBackgroundColor(ContextCompat.getColor(this, R.color.divisoria));
        containerRecentes.addView(divisoria, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.espessura_divisoria)));
    }

    private void mostrarSnackbarExclusao(@Nullable String titulo) {
        Snackbar.make(raiz, getString(R.string.msg_registro_excluido, titulo == null ? "" : titulo),
                        Snackbar.LENGTH_LONG)
                .setAction(R.string.acao_desfazer, v -> {
                    if (repositorio.desfazerUltimaRemocao()) {
                        atualizarPainel();
                        mostrarMensagem(R.string.msg_registro_restaurado);
                    }
                })
                .setAnchorView(navegacaoInferior)
                .show();
    }
}
