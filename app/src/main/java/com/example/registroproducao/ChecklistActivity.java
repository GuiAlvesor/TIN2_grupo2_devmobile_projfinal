package com.example.registroproducao;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.registroproducao.data.RegistroRepository;
import com.example.registroproducao.model.Categoria;
import com.example.registroproducao.model.ItemChecklist;
import com.example.registroproducao.model.ItemChecklist.Resposta;
import com.example.registroproducao.model.Registro;
import com.example.registroproducao.util.Constantes;
import com.example.registroproducao.util.TextWatcherSimples;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Execução de uma Lista de controle (checklist). Cada item é respondido com Sim, Não ou N/A;
 * a resposta "Não" exige uma ação corretiva. As respostas ficam numa cópia de trabalho e só
 * vão para o repositório (em memória) ao tocar em "Salvar rascunho" ou "Finalizar".
 */
public class ChecklistActivity extends BaseActivity {

    private static final String ESTADO_RESPOSTAS = "estado_respostas";
    private static final String ESTADO_OBSERVACOES = "estado_observacoes";
    private static final int SEM_RESPOSTA = -1;

    @NonNull
    public static Intent criarIntent(@NonNull Context contexto, int registroId) {
        return new Intent(contexto, ChecklistActivity.class)
                .putExtra(Constantes.EXTRA_REGISTRO_ID, registroId);
    }

    /** Referências às views de um card de pergunta. */
    private static final class VistaPergunta {
        MaterialButton btnSim;
        MaterialButton btnNao;
        MaterialButton btnNa;
        View layoutAcaoCorretiva;
        TextInputLayout tilAcaoCorretiva;
        TextInputEditText etAcaoCorretiva;
    }

    private RegistroRepository repositorio;
    private Registro registro;
    /** Cópia de trabalho dos itens (alterada pelas respostas na tela). */
    private List<ItemChecklist> itens;
    private final List<VistaPergunta> vistas = new ArrayList<>();

    private TextView tvProgresso;
    private TextView tvPercentual;
    private LinearProgressIndicator progresso;

    @Override
    protected boolean usaCabecalhoEscuro() {
        return true; // cabeçalho vermelho: ícones claros na status bar
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist);
        aplicarInsetsDeSistema(findViewById(R.id.rolagem), findViewById(R.id.root),
                findViewById(R.id.faixaStatusBar));

        repositorio = RegistroRepository.getInstance();
        int id = getIntent().getIntExtra(Constantes.EXTRA_REGISTRO_ID, Constantes.ID_INVALIDO);
        registro = repositorio.buscarPorId(id);
        if (registro == null) {
            mostrarMensagem(R.string.msg_registro_nao_encontrado);
            finish();
            return;
        }
        if (registro.getCategoria() != Categoria.LISTA_CONTROLE) {
            mostrarMensagem(R.string.msg_nao_e_checklist);
            finish();
            return;
        }

        itens = registro.getItens();
        if (savedInstanceState != null) {
            restaurarRespostas(savedInstanceState);
        }

        tvProgresso = findViewById(R.id.tvProgresso);
        tvPercentual = findViewById(R.id.tvPercentual);
        progresso = findViewById(R.id.progresso);
        ((TextView) findViewById(R.id.tvTitulo)).setText(registro.getTitulo());
        ((TextView) findViewById(R.id.tvSetor)).setText(registro.getSetor().isEmpty()
                ? getString(R.string.setor_nao_informado) : registro.getSetor());

        findViewById(R.id.btnVoltar).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        findViewById(R.id.btnSalvarRascunho).setOnClickListener(v -> salvarRascunho());
        findViewById(R.id.btnFinalizar).setOnClickListener(v -> tentarFinalizar());

        montarPerguntas();
        atualizarProgresso();

        // Avisa antes de sair se houver respostas não salvas.
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

    // ===================== Estado (rotação de tela) =====================

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (itens == null) return;
        int[] respostas = new int[itens.size()];
        ArrayList<String> observacoes = new ArrayList<>();
        for (int i = 0; i < itens.size(); i++) {
            Resposta r = itens.get(i).getResposta();
            respostas[i] = r == null ? SEM_RESPOSTA : r.ordinal();
            observacoes.add(itens.get(i).getObservacao());
        }
        outState.putIntArray(ESTADO_RESPOSTAS, respostas);
        outState.putStringArrayList(ESTADO_OBSERVACOES, observacoes);
    }

    private void restaurarRespostas(@NonNull Bundle estado) {
        int[] respostas = estado.getIntArray(ESTADO_RESPOSTAS);
        ArrayList<String> observacoes = estado.getStringArrayList(ESTADO_OBSERVACOES);
        if (respostas == null || observacoes == null || respostas.length != itens.size()) return;
        for (int i = 0; i < itens.size(); i++) {
            itens.get(i).setResposta(respostas[i] == SEM_RESPOSTA ? null : Resposta.values()[respostas[i]]);
            itens.get(i).setObservacao(observacoes.get(i));
        }
    }

    // ===================== Montagem da tela =====================

    private void montarPerguntas() {
        LinearLayout container = findViewById(R.id.containerPerguntas);
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < itens.size(); i++) {
            final int indice = i;
            ItemChecklist item = itens.get(i);
            View card = inflater.inflate(R.layout.item_pergunta_checklist, container, false);

            ((TextView) card.findViewById(R.id.tvNumero)).setText(String.valueOf(i + 1));
            ((TextView) card.findViewById(R.id.tvPergunta)).setText(item.getPergunta());

            VistaPergunta vista = new VistaPergunta();
            vista.btnSim = card.findViewById(R.id.btnSim);
            vista.btnNao = card.findViewById(R.id.btnNao);
            vista.btnNa = card.findViewById(R.id.btnNa);
            vista.layoutAcaoCorretiva = card.findViewById(R.id.layoutAcaoCorretiva);
            vista.tilAcaoCorretiva = card.findViewById(R.id.tilAcaoCorretiva);
            vista.etAcaoCorretiva = card.findViewById(R.id.etAcaoCorretiva);

            // Vários cards usam os mesmos ids; o estado é salvo manualmente em onSaveInstanceState.
            vista.etAcaoCorretiva.setSaveEnabled(false);
            vista.etAcaoCorretiva.setText(item.getObservacao());
            vista.etAcaoCorretiva.addTextChangedListener(new TextWatcherSimples() {
                @Override
                public void afterTextChanged(Editable s) {
                    itens.get(indice).setObservacao(s.toString());
                    vista.tilAcaoCorretiva.setError(null);
                }
            });

            vista.btnSim.setOnClickListener(v -> responder(indice, Resposta.SIM));
            vista.btnNao.setOnClickListener(v -> responder(indice, Resposta.NAO));
            vista.btnNa.setOnClickListener(v -> responder(indice, Resposta.NA));

            vistas.add(vista);
            container.addView(card);
            pintarPergunta(indice);
        }
    }

    /** Marca a resposta; tocar de novo na resposta já marcada desfaz a seleção. */
    private void responder(int indice, @NonNull Resposta resposta) {
        ItemChecklist item = itens.get(indice);
        item.setResposta(item.getResposta() == resposta ? null : resposta);
        pintarPergunta(indice);
        atualizarProgresso();
    }

    /** Selecionado: Sim = preto, Não = vermelho, N/A = cinza. Não selecionado: contorno cinza. */
    private void pintarPergunta(int indice) {
        Resposta resposta = itens.get(indice).getResposta();
        VistaPergunta vista = vistas.get(indice);
        pintarBotao(vista.btnSim, resposta == Resposta.SIM, R.color.preto);
        pintarBotao(vista.btnNao, resposta == Resposta.NAO, R.color.vermelho);
        pintarBotao(vista.btnNa, resposta == Resposta.NA, R.color.texto_secundario);
        vista.layoutAcaoCorretiva.setVisibility(resposta == Resposta.NAO ? View.VISIBLE : View.GONE);
    }

    private void pintarBotao(@NonNull MaterialButton botao, boolean selecionado, @ColorRes int corSelecionado) {
        int cor = ContextCompat.getColor(this, corSelecionado);
        int branco = ContextCompat.getColor(this, R.color.branco);
        botao.setBackgroundTintList(ColorStateList.valueOf(selecionado ? cor : branco));
        botao.setStrokeColor(ColorStateList.valueOf(selecionado
                ? cor : ContextCompat.getColor(this, R.color.cinza_borda)));
        botao.setTextColor(selecionado ? branco : ContextCompat.getColor(this, R.color.preto));
        botao.setSelected(selecionado);
    }

    private void atualizarProgresso() {
        int respondidos = 0;
        for (ItemChecklist item : itens) {
            if (item.isRespondido()) respondidos++;
        }
        int percentual = itens.isEmpty() ? 0 : respondidos * 100 / itens.size();
        tvProgresso.setText(getString(R.string.checklist_progresso, respondidos, itens.size()));
        tvPercentual.setText(getString(R.string.checklist_percentual, percentual));
        progresso.setProgressCompat(percentual, true);
    }

    // ===================== Ações =====================

    /** Salva as respostas sem concluir o registro. */
    private void salvarRascunho() {
        if (gravar(registro.isConcluido())) {
            mostrarMensagem(R.string.msg_rascunho_salvo);
            finish();
        }
    }

    /** Exige ação corretiva nos itens "Não" e confirma se ainda houver itens sem resposta. */
    private void tentarFinalizar() {
        if (!validarAcoesCorretivas()) {
            mostrarMensagem(R.string.msg_acao_corretiva_pendente);
            return;
        }
        int semResposta = 0;
        for (ItemChecklist item : itens) {
            if (!item.isRespondido()) semResposta++;
        }
        if (semResposta == 0) {
            finalizar();
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialogo_finalizar_titulo)
                .setMessage(getResources().getQuantityString(
                        R.plurals.dialogo_finalizar_mensagem, semResposta, semResposta))
                .setPositiveButton(R.string.acao_finalizar, (d, w) -> finalizar())
                .setNegativeButton(R.string.acao_continuar_editando, null)
                .show();
    }

    /** Finalizar = salvar as respostas e marcar o registro como concluído. */
    private void finalizar() {
        if (gravar(true)) {
            mostrarMensagem(R.string.msg_checklist_finalizado);
            finish();
        }
    }

    private boolean validarAcoesCorretivas() {
        boolean valido = true;
        for (int i = itens.size() - 1; i >= 0; i--) {
            ItemChecklist item = itens.get(i);
            if (item.getResposta() == Resposta.NAO && item.getObservacao().isEmpty()) {
                VistaPergunta vista = vistas.get(i);
                vista.tilAcaoCorretiva.setError(getString(R.string.erro_acao_corretiva));
                vista.etAcaoCorretiva.requestFocus(); // termina no primeiro item inválido
                valido = false;
            }
        }
        return valido;
    }

    /** UPDATE no repositório em memória com as respostas atuais. */
    private boolean gravar(boolean concluido) {
        registro.setItens(itens);
        registro.setConcluido(concluido);
        if (!repositorio.atualizar(registro)) {
            mostrarMensagem(R.string.msg_registro_nao_encontrado);
            finish();
            return false;
        }
        return true;
    }

    private boolean possuiAlteracoes() {
        return !itens.equals(registro.getItens());
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
}
