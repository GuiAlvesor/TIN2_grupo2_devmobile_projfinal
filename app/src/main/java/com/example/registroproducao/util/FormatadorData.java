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
}
