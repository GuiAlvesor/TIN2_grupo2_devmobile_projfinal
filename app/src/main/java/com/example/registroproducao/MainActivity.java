package com.example.registroproducao;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.ViewCompat;
import androidx.core.widget.ImageViewCompat;

import com.example.registroproducao.data.RegistroRepository;
import com.example.registroproducao.model.Categoria;

/**
 * Tela inicial (painel). Mostra um resumo dos registros, atalhos por categoria
 * e os botões principais de navegação. O resumo é recalculado em {@link #onResume()},
 * ou seja, sempre que o usuário volta para esta tela.
 */
public class MainActivity extends BaseActivity {

    private RegistroRepository repositorio;

    private TextView tvTotal;
    private TextView tvPendentes;
    private TextView tvPrioridadeAlta;

    private View cardRegistros;
    private View cardListasControle;
    private View cardAnotacoes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        aplicarInsetsDeSistema(findViewById(R.id.appBar), findViewById(R.id.root));

        repositorio = RegistroRepository.getInstance();
        repositorio.carregarExemplosSeNecessario();

        tvTotal = findViewById(R.id.tvTotal);
        tvPendentes = findViewById(R.id.tvPendentes);
        tvPrioridadeAlta = findViewById(R.id.tvPrioridadeAlta);

        cardRegistros = findViewById(R.id.cardCategoriaRegistros);
        cardListasControle = findViewById(R.id.cardCategoriaListas);
        cardAnotacoes = findViewById(R.id.cardCategoriaAnotacoes);

        configurarCardCategoria(cardRegistros, Categoria.REGISTRO);
        configurarCardCategoria(cardListasControle, Categoria.LISTA_CONTROLE);
        configurarCardCategoria(cardAnotacoes, Categoria.ANOTACAO);

        // Navegação explícita entre telas via Intent
        findViewById(R.id.btnNovoRegistro).setOnClickListener(v ->
                startActivity(FormularioRegistroActivity.criarIntentNovo(this, null)));

        findViewById(R.id.btnVerRegistros).setOnClickListener(v ->
                startActivity(ListaRegistrosActivity.criarIntent(this, null)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        atualizarResumo();
    }

    /** Preenche ícone, cores e textos do card de categoria e define a navegação filtrada. */
    private void configurarCardCategoria(@NonNull View card, @NonNull Categoria categoria) {
        int cor = ContextCompat.getColor(this, categoria.getCorRes());

        ImageView icone = card.findViewById(R.id.ivIconeCategoria);
        icone.setImageResource(categoria.getIconeRes());
        ImageViewCompat.setImageTintList(icone, ColorStateList.valueOf(cor));
        ViewCompat.setBackgroundTintList(icone,
                ColorStateList.valueOf(ColorUtils.setAlphaComponent(cor, 40)));

        ((TextView) card.findViewById(R.id.tvTituloCategoria)).setText(categoria.getRotuloRes());
        ((TextView) card.findViewById(R.id.tvDescricaoCategoria)).setText(categoria.getDescricaoRes());
        ((TextView) card.findViewById(R.id.tvQuantidadeCategoria)).setTextColor(cor);

        card.setOnClickListener(v ->
                startActivity(ListaRegistrosActivity.criarIntent(this, categoria)));
    }

    private void atualizarResumo() {
        tvTotal.setText(String.valueOf(repositorio.contarTotal()));
        tvPendentes.setText(String.valueOf(repositorio.contarPendentes()));
        tvPrioridadeAlta.setText(String.valueOf(repositorio.contarPrioridadeAltaPendentes()));

        atualizarQuantidade(cardRegistros, Categoria.REGISTRO);
        atualizarQuantidade(cardListasControle, Categoria.LISTA_CONTROLE);
        atualizarQuantidade(cardAnotacoes, Categoria.ANOTACAO);
    }

    private void atualizarQuantidade(@NonNull View card, @NonNull Categoria categoria) {
        int quantidade = repositorio.contarPorCategoria(categoria);
        TextView tv = card.findViewById(R.id.tvQuantidadeCategoria);
        tv.setText(String.valueOf(quantidade));
        card.setContentDescription(getString(categoria.getRotuloRes()) + ": "
                + getResources().getQuantityString(R.plurals.contador_registros, quantidade, quantidade));
    }
}
