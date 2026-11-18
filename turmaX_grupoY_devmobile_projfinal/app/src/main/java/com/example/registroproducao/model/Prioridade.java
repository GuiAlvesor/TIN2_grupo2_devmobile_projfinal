package com.example.registroproducao.model;

import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;

import com.example.registroproducao.R;

/** Nível de prioridade de um registro, com texto e cor associados. */
public enum Prioridade {

    BAIXA(R.string.prioridade_baixa, R.color.prioridade_baixa),
    MEDIA(R.string.prioridade_media, R.color.prioridade_media),
    ALTA(R.string.prioridade_alta, R.color.prioridade_alta);

    @StringRes private final int rotuloRes;
    @ColorRes private final int corRes;

    Prioridade(@StringRes int rotuloRes, @ColorRes int corRes) {
        this.rotuloRes = rotuloRes;
        this.corRes = corRes;
    }

    @StringRes public int getRotuloRes() { return rotuloRes; }
    @ColorRes public int getCorRes() { return corRes; }
}
