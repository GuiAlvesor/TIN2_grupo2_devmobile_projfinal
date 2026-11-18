package com.example.registroproducao.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.core.widget.TextViewCompat;

import com.example.registroproducao.R;
import com.example.registroproducao.model.Categoria;
import com.example.registroproducao.model.Prioridade;

/**
 * Aplica a identidade visual compartilhada entre as telas (pílulas de prioridade e status,
 * ícone de categoria), evitando repetir a mesma lógica em cada Activity/Adapter.
 */
public final class EstiloVisual {

    private EstiloVisual() {
    }

    /** Pílula de prioridade: Alta = vermelha preenchida; Média = contorno preto; Baixa = cinza. */
    public static void aplicarPrioridade(@NonNull TextView pilula, @NonNull Prioridade prioridade) {
        pilula.setText(prioridade.getRotuloRes());
        pilula.setBackgroundResource(prioridade.getFundoPilulaRes());
        pilula.setTextColor(ContextCompat.getColor(pilula.getContext(), prioridade.getCorTextoPilulaRes()));
    }

    /** Status em texto com ícone: Pendente em vermelho escuro (relógio); Concluído em preto (check). */
    public static void aplicarStatus(@NonNull TextView status, boolean concluido) {
        int cor = ContextCompat.getColor(status.getContext(),
                concluido ? R.color.preto : R.color.vermelho_escuro);
        pintarStatus(status, concluido, cor);
    }

    /** Status como pílula: Pendente = fundo vermelho suave; Concluído = fundo preto com texto branco. */
    public static void aplicarStatusPilula(@NonNull TextView pilula, boolean concluido) {
        Context ctx = pilula.getContext();
        pilula.setBackgroundResource(concluido ? R.drawable.bg_pilula_concluido : R.drawable.bg_pilula_pendente);
        int cor = ContextCompat.getColor(ctx, concluido ? R.color.branco : R.color.vermelho_escuro);
        pintarStatus(pilula, concluido, cor);
    }

    private static void pintarStatus(@NonNull TextView tv, boolean concluido, int cor) {
        tv.setText(concluido ? R.string.status_concluido : R.string.status_pendente);
        tv.setTextColor(cor);
        tv.setCompoundDrawablesRelativeWithIntrinsicBounds(
                concluido ? R.drawable.ic_check_circle_pequeno : R.drawable.ic_schedule_pequeno, 0, 0, 0);
        TextViewCompat.setCompoundDrawableTintList(tv, ColorStateList.valueOf(cor));
    }

    /** Ícone da categoria em preto (o quadrado cinza de fundo vem do layout). */
    public static void aplicarIconeCategoria(@NonNull ImageView icone, @NonNull Categoria categoria) {
        icone.setImageResource(categoria.getIconeRes());
        ImageViewCompat.setImageTintList(icone,
                ColorStateList.valueOf(ContextCompat.getColor(icone.getContext(), R.color.preto)));
    }
}
