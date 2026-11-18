package com.example.registroproducao.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

/**
 * Entidade principal do aplicativo: representa uma informação operacional
 * da produção (registro, lista de controle ou anotação).
 */
public class Registro {

    private int id;
    private String titulo = "";
    private String descricao = "";
    private String setor = "";
    private Categoria categoria = Categoria.REGISTRO;
    private Prioridade prioridade = Prioridade.MEDIA;
    private boolean concluido;
    private long dataCriacao;
    private long dataAtualizacao;

    public Registro() {
    }

    /** Construtor de cópia: evita que telas alterem diretamente os objetos guardados no repositório. */
    public Registro(@NonNull Registro outro) {
        this.id = outro.id;
        this.titulo = outro.titulo;
        this.descricao = outro.descricao;
        this.setor = outro.setor;
        this.categoria = outro.categoria;
        this.prioridade = outro.prioridade;
        this.concluido = outro.concluido;
        this.dataCriacao = outro.dataCriacao;
        this.dataAtualizacao = outro.dataAtualizacao;
    }

    public Registro(String titulo, String descricao, String setor,
                    Categoria categoria, Prioridade prioridade, boolean concluido) {
        setTitulo(titulo);
        setDescricao(descricao);
        setSetor(setor);
        setCategoria(categoria);
        setPrioridade(prioridade);
        this.concluido = concluido;
    }

    // ---------- Getters e setters ----------

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @NonNull public String getTitulo() { return titulo; }
    public void setTitulo(@Nullable String titulo) { this.titulo = titulo == null ? "" : titulo.trim(); }

    @NonNull public String getDescricao() { return descricao; }
    public void setDescricao(@Nullable String descricao) { this.descricao = descricao == null ? "" : descricao.trim(); }

    @NonNull public String getSetor() { return setor; }
    public void setSetor(@Nullable String setor) { this.setor = setor == null ? "" : setor.trim(); }

    @NonNull public Categoria getCategoria() { return categoria; }
    public void setCategoria(@Nullable Categoria categoria) {
        this.categoria = categoria == null ? Categoria.REGISTRO : categoria;
    }

    @NonNull public Prioridade getPrioridade() { return prioridade; }
    public void setPrioridade(@Nullable Prioridade prioridade) {
        this.prioridade = prioridade == null ? Prioridade.MEDIA : prioridade;
    }

    public boolean isConcluido() { return concluido; }
    public void setConcluido(boolean concluido) { this.concluido = concluido; }

    public long getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(long dataCriacao) { this.dataCriacao = dataCriacao; }

    public long getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(long dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }

    // ---------- Regras auxiliares ----------

    /** Indica se o texto informado aparece no título, setor ou descrição (sem diferenciar maiúsculas). */
    public boolean contemTexto(@Nullable String termo) {
        if (termo == null || termo.trim().isEmpty()) return true;
        String t = termo.trim().toLowerCase();
        return titulo.toLowerCase().contains(t)
                || setor.toLowerCase().contains(t)
                || descricao.toLowerCase().contains(t);
    }

    /** Compara apenas os campos editáveis pelo usuário (usado para detectar alterações não salvas). */
    public boolean temMesmoConteudo(@Nullable Registro outro) {
        if (outro == null) return false;
        return concluido == outro.concluido
                && titulo.equals(outro.titulo)
                && descricao.equals(outro.descricao)
                && setor.equals(outro.setor)
                && categoria == outro.categoria
                && prioridade == outro.prioridade;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Registro)) return false;
        Registro outro = (Registro) o;
        return id == outro.id
                && dataCriacao == outro.dataCriacao
                && dataAtualizacao == outro.dataAtualizacao
                && temMesmoConteudo(outro);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, titulo, descricao, setor, categoria, prioridade,
                concluido, dataCriacao, dataAtualizacao);
    }

    @NonNull
    @Override
    public String toString() {
        return "Registro{id=" + id + ", titulo='" + titulo + "', categoria=" + categoria + "}";
    }
}
