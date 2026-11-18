package com.example.registroproducao.util;

import android.app.Activity;

/** Chaves e códigos compartilhados entre as Activities (Intents e resultados). */
public final class Constantes {

    public static final String EXTRA_REGISTRO_ID = "com.example.registroproducao.EXTRA_REGISTRO_ID";
    public static final String EXTRA_CATEGORIA = "com.example.registroproducao.EXTRA_CATEGORIA";
    public static final String EXTRA_TITULO_REGISTRO = "com.example.registroproducao.EXTRA_TITULO_REGISTRO";

    /** Valor usado quando nenhum id foi enviado no Intent. */
    public static final int ID_INVALIDO = -1;

    /** Código de resultado devolvido pela tela de detalhes quando o registro é excluído. */
    public static final int RESULTADO_REGISTRO_EXCLUIDO = Activity.RESULT_FIRST_USER + 1;

    private Constantes() {
    }
}
