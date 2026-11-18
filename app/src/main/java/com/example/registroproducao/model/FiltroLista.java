package com.example.registroproducao.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/** Filtros rápidos da tela de listagem (pílulas "Todos", "Pendentes", "Alta" e "Checklists"). */
public enum FiltroLista {

    TODOS,
    PENDENTES,
    ALTA,
    CHECKLISTS;

    /** Indica se o registro deve aparecer quando este filtro está selecionado. */
    public boolean aceita(@NonNull Registro registro) {
        switch (this) {
            case PENDENTES:
                return !registro.isConcluido();
            case ALTA:
                return registro.getPrioridade() == Prioridade.ALTA;
            case CHECKLISTS:
                return registro.getCategoria() == Categoria.LISTA_CONTROLE;
            default:
                return true;
        }
    }

    /** Converte o nome salvo em um Intent de volta para o filtro (TODOS se inválido). */
    @NonNull
    public static FiltroLista deNome(@Nullable String nome) {
        if (nome == null) return TODOS;
        try {
            return FiltroLista.valueOf(nome);
        } catch (IllegalArgumentException e) {
            return TODOS;
        }
    }
}
