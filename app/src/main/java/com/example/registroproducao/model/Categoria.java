package com.example.registroproducao.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.example.registroproducao.R;

/**
 * Tipos de informação operacional tratados pelo aplicativo.
 * Cada categoria carrega seus próprios recursos visuais (texto, descrição e ícone),
 * o que mantém a interface consistente em todas as telas.
 */
public enum Categoria {

    REGISTRO(R.string.categoria_registro,
            R.string.categoria_registro_descricao,
            R.drawable.ic_registro),

    LISTA_CONTROLE(R.string.categoria_lista_controle,
            R.string.categoria_lista_controle_descricao,
            R.drawable.ic_checklist),

    ANOTACAO(R.string.categoria_anotacao,
            R.string.categoria_anotacao_descricao,
            R.drawable.ic_anotacao);

    @StringRes private final int rotuloRes;
    @StringRes private final int descricaoRes;
    @DrawableRes private final int iconeRes;

    Categoria(@StringRes int rotuloRes, @StringRes int descricaoRes, @DrawableRes int iconeRes) {
        this.rotuloRes = rotuloRes;
        this.descricaoRes = descricaoRes;
        this.iconeRes = iconeRes;
    }

    @StringRes public int getRotuloRes() { return rotuloRes; }
    @StringRes public int getDescricaoRes() { return descricaoRes; }
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
