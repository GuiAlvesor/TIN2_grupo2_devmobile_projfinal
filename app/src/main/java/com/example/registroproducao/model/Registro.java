package com.example.registroproducao.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entidade principal do aplicativo: representa uma informação operacional
 * da produção (registro, lista de controle ou anotação).
 * Registros da categoria {@link Categoria#LISTA_CONTROLE} também guardam os itens do checklist.
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
    private final List<ItemChecklist> itens = new ArrayList<>();

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
        setItens(outro.itens);
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

    // ---------- Itens do checklist ----------

    /** Devolve cópias dos itens (alterações só valem depois de {@link #setItens(List)}). */
    @NonNull
    public List<ItemChecklist> getItens() {
        List<ItemChecklist> copia = new ArrayList<>();
        for (ItemChecklist item : itens) copia.add(new ItemChecklist(item));
        return copia;
    }

    public void setItens(@Nullable List<ItemChecklist> novosItens) {
        itens.clear();
        if (novosItens == null) return;
        for (ItemChecklist item : novosItens) itens.add(new ItemChecklist(item));
    }

    public int getTotalItens() { return itens.size(); }

    public int getItensRespondidos() {
        int total = 0;
        for (ItemChecklist item : itens) {
            if (item.isRespondido()) total++;
        }
        return total;
    }

    /** Percentual (0 a 100) de itens respondidos. */
    public int getPercentualRespondido() {
        return itens.isEmpty() ? 0 : getItensRespondidos() * 100 / itens.size();
    }

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
                && prioridade == outro.prioridade
                && itens.equals(outro.itens);
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
                concluido, dataCriacao, dataAtualizacao, itens);
    }

    @NonNull
    @Override
    public String toString() {
        return "Registro{id=" + id + ", titulo='" + titulo + "', categoria=" + categoria + "}";
    }
}
