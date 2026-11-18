package com.example.registroproducao.model;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.example.registroproducao.R;

/**
 * Nível de prioridade de um registro. A prioridade não depende só da cor:
 * Alta = pílula vermelha preenchida; Média = pílula com contorno preto; Baixa = pílula cinza.
 */
public enum Prioridade {

    BAIXA(R.string.prioridade_baixa, R.drawable.bg_pilula_baixa, R.color.texto_secundario),
    MEDIA(R.string.prioridade_media, R.drawable.bg_pilula_media, R.color.preto),
    ALTA(R.string.prioridade_alta, R.drawable.bg_pilula_alta, R.color.branco);

    @StringRes private final int rotuloRes;
    @DrawableRes private final int fundoPilulaRes;
    @ColorRes private final int corTextoPilulaRes;

    Prioridade(@StringRes int rotuloRes, @DrawableRes int fundoPilulaRes, @ColorRes int corTextoPilulaRes) {
        this.rotuloRes = rotuloRes;
        this.fundoPilulaRes = fundoPilulaRes;
        this.corTextoPilulaRes = corTextoPilulaRes;
    }

    @StringRes public int getRotuloRes() { return rotuloRes; }
    @DrawableRes public int getFundoPilulaRes() { return fundoPilulaRes; }
    @ColorRes public int getCorTextoPilulaRes() { return corTextoPilulaRes; }
}
