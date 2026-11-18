package com.example.registroproducao.model;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.example.registroproducao.R;

/**
 * Tipos de informação operacional tratados pelo aplicativo.
 * Cada categoria carrega seus próprios recursos visuais (texto, cor e ícone),
 * o que mantém a interface consistente em todas as telas.
 */
public enum Categoria {

    REGISTRO(R.string.categoria_registro,
            R.string.categoria_registro_descricao,
            R.color.categoria_registro,
            R.drawable.ic_registro),

    LISTA_CONTROLE(R.string.categoria_lista_controle,
            R.string.categoria_lista_controle_descricao,
            R.color.categoria_lista_controle,
            R.drawable.ic_checklist),

    ANOTACAO(R.string.categoria_anotacao,
            R.string.categoria_anotacao_descricao,
            R.color.categoria_anotacao,
            R.drawable.ic_anotacao);

    @StringRes private final int rotuloRes;
    @StringRes private final int descricaoRes;
    @ColorRes private final int corRes;
    @DrawableRes private final int iconeRes;

    Categoria(@StringRes int rotuloRes, @StringRes int descricaoRes,
              @ColorRes int corRes, @DrawableRes int iconeRes) {
        this.rotuloRes = rotuloRes;
        this.descricaoRes = descricaoRes;
        this.corRes = corRes;
        this.iconeRes = iconeRes;
    }

    @StringRes public int getRotuloRes() { return rotuloRes; }
    @StringRes public int getDescricaoRes() { return descricaoRes; }
    @ColorRes public int getCorRes() { return corRes; }
    @DrawableRes public int getIconeRes() { return iconeRes; }

    /** Converte o nome salvo em um Intent de volta para a categoria (ou null se inválido). */
    @Nullable
    public static Categoria deNome(@Nullable String nome) {
        if (nome == null) return null;
        try {
            return Categoria.valueOf(nome);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
