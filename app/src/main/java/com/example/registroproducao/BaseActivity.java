package com.example.registroproducao;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

/**
 * Activity base do projeto. Centraliza comportamentos comuns a todas as telas:
 * - registro (Log) dos métodos do ciclo de vida;
 * - configuração da barra superior (Toolbar) com botão "voltar";
 * - ajuste do layout às barras do sistema (edge-to-edge);
 * - exibição de mensagens de feedback (Toast).
 */
public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG_CICLO_VIDA = "CicloDeVida";

    // ===================== Ciclo de vida =====================

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ícones claros na status bar sobre cabeçalhos vermelhos; escuros sobre cabeçalhos brancos.
        // No Android 5.x (sem ícones escuros) a barra recebe um véu escuro para manter a leitura.
        int veuEscuro = ContextCompat.getColor(this, R.color.preto_25);
        EdgeToEdge.enable(this,
                usaCabecalhoEscuro()
                        ? SystemBarStyle.dark(Color.TRANSPARENT)
                        : SystemBarStyle.light(Color.TRANSPARENT, veuEscuro),
                SystemBarStyle.light(Color.TRANSPARENT, veuEscuro));
        registrarCiclo("onCreate" + (savedInstanceState != null ? " (estado restaurado)" : ""));
    }

    @Override
    protected void onStart() {
        super.onStart();
        registrarCiclo("onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        registrarCiclo("onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        registrarCiclo("onResume");
    }

    @Override
    protected void onPause() {
        registrarCiclo("onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        registrarCiclo("onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        registrarCiclo("onDestroy");
        super.onDestroy();
    }

    private void registrarCiclo(String metodo) {
        Log.d(TAG_CICLO_VIDA, getClass().getSimpleName() + " -> " + metodo);
    }

    // ===================== Utilitários de interface =====================

    /**
     * Indica se a tela tem cabeçalho escuro/vermelho no topo (ícones claros na status bar).
     * Telas com cabeçalho branco mantêm o padrão {@code false}.
     */
    protected boolean usaCabecalhoEscuro() {
        return false;
    }

    /** Configura a Toolbar: título e botão de voltar que respeita o OnBackPressedDispatcher. */
    protected void configurarToolbar(@NonNull MaterialToolbar toolbar, @StringRes int titulo,
                                     boolean exibirVoltar) {
        configurarToolbar(toolbar, titulo, exibirVoltar ? R.drawable.ic_arrow_back : 0, R.string.acao_voltar);
    }

    /**
     * Configura a Toolbar com um ícone de navegação específico (ex.: X para fechar o formulário).
     * Use {@code 0} em {@code titulo} para não exibir título e em {@code icone} para não exibir ícone.
     */
    protected void configurarToolbar(@NonNull MaterialToolbar toolbar, @StringRes int titulo,
                                     @DrawableRes int icone, @StringRes int descricaoIcone) {
        if (titulo != 0) {
            toolbar.setTitle(titulo);
        } else {
            toolbar.setTitle(null);
        }
        if (icone != 0) {
            toolbar.setNavigationIcon(icone);
            toolbar.setNavigationContentDescription(descricaoIcone);
            toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        }
    }

    /**
     * Aplica os espaçamentos das barras do sistema (status bar, barra de navegação e teclado):
     * a área superior recebe o espaço da status bar e a raiz recebe o espaço inferior e laterais.
     */
    protected void aplicarInsetsDeSistema(@NonNull View areaSuperior, @NonNull View raiz) {
        aplicarInsetsDeSistema(areaSuperior, raiz, null);
    }

    /**
     * Igual ao método acima e, além disso, ajusta a altura de {@code faixaStatusBar} (uma view
     * fixa no topo, da cor do cabeçalho) para cobrir a status bar. Assim, em telas com cabeçalho
     * vermelho que rola junto com o conteúdo, os ícones claros da status bar continuam legíveis.
     */
    protected void aplicarInsetsDeSistema(@NonNull View areaSuperior, @NonNull View raiz,
                                          @Nullable View faixaStatusBar) {
        final int topoOriginal = areaSuperior.getPaddingTop();
        final int esquerdaOriginal = raiz.getPaddingLeft();
        final int direitaOriginal = raiz.getPaddingRight();
        final int baseOriginal = raiz.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(raiz, (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            Insets teclado = insets.getInsets(WindowInsetsCompat.Type.ime());

            v.setPadding(esquerdaOriginal + barras.left,
                    v.getPaddingTop(),
                    direitaOriginal + barras.right,
                    baseOriginal + Math.max(barras.bottom, teclado.bottom));

            areaSuperior.setPadding(areaSuperior.getPaddingLeft(),
                    topoOriginal + barras.top,
                    areaSuperior.getPaddingRight(),
                    areaSuperior.getPaddingBottom());

            if (faixaStatusBar != null && faixaStatusBar.getLayoutParams().height != barras.top) {
                faixaStatusBar.getLayoutParams().height = barras.top;
                faixaStatusBar.requestLayout();
            }
            return WindowInsetsCompat.CONSUMED;
        });
    }

    protected void mostrarMensagem(@StringRes int mensagem, Object... argumentos) {
        Toast.makeText(this, getString(mensagem, argumentos), Toast.LENGTH_SHORT).show();
    }
}
