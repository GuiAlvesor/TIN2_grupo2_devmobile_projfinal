package com.example.registroproducao.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

/**
 * Uma pergunta de uma Lista de controle (checklist), com a resposta dada
 * e a observação (ação corretiva) registrada quando a resposta é "Não".
 */
public class ItemChecklist {

    /** Respostas possíveis. {@code null} significa "ainda não respondido". */
    public enum Resposta { SIM, NAO, NA }

    private String pergunta = "";
    @Nullable private Resposta resposta;
    private String observacao = "";

    public ItemChecklist(@Nullable String pergunta) {
        setPergunta(pergunta);
    }

    /** Construtor de cópia: evita que as telas alterem diretamente os dados do repositório. */
    public ItemChecklist(@NonNull ItemChecklist outro) {
        this.pergunta = outro.pergunta;
        this.resposta = outro.resposta;
        this.observacao = outro.observacao;
    }

    @NonNull public String getPergunta() { return pergunta; }
    public void setPergunta(@Nullable String pergunta) { this.pergunta = pergunta == null ? "" : pergunta.trim(); }

    @Nullable public Resposta getResposta() { return resposta; }
    public void setResposta(@Nullable Resposta resposta) { this.resposta = resposta; }

    @NonNull public String getObservacao() { return observacao; }
    public void setObservacao(@Nullable String observacao) { this.observacao = observacao == null ? "" : observacao.trim(); }

    public boolean isRespondido() { return resposta != null; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemChecklist)) return false;
        ItemChecklist outro = (ItemChecklist) o;
        return pergunta.equals(outro.pergunta)
                && resposta == outro.resposta
                && observacao.equals(outro.observacao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pergunta, resposta, observacao);
    }
}
