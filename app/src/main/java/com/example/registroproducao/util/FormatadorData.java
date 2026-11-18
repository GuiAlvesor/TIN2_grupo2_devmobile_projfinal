package com.example.registroproducao.util;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Formata datas no padrão brasileiro (dd/MM/aaaa HH:mm). */
public final class FormatadorData {

    private static final Locale PT_BR = new Locale("pt", "BR");

    private FormatadorData() {
    }

    @NonNull
    public static String formatar(long timestamp) {
        // SimpleDateFormat não é thread-safe, por isso uma instância por chamada.
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", PT_BR).format(new Date(timestamp));
    }

    /** Hora no formato HH:mm (usada nas listas, onde os registros costumam ser do mesmo dia). */
    @NonNull
    public static String formatarHora(long timestamp) {
        return new SimpleDateFormat("HH:mm", PT_BR).format(new Date(timestamp));
    }

    /** Indica se o instante pertence ao dia de hoje. */
    public static boolean ehHoje(long timestamp) {
        SimpleDateFormat dia = new SimpleDateFormat("yyyyMMdd", PT_BR);
        return dia.format(new Date(timestamp)).equals(dia.format(new Date()));
    }
}
