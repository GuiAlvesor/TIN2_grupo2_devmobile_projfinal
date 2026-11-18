package com.example.registroproducao.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.registroproducao.model.Categoria;
import com.example.registroproducao.model.Prioridade;
import com.example.registroproducao.model.Registro;

import java.util.ArrayList;
import java.util.List;

/**
 * Repositório responsável pelo CRUD dos registros, armazenados em memória
 * em uma {@link ArrayList}. Implementado como Singleton para que todas as
 * Activities compartilhem a mesma lista enquanto o aplicativo estiver aberto.
 *
 * Os métodos sempre devolvem CÓPIAS dos objetos, garantindo que alterações
 * só cheguem à lista pelos métodos {@link #adicionar} e {@link #atualizar}.
 */
public final class RegistroRepository {

    private static RegistroRepository instancia;

    private final List<Registro> registros = new ArrayList<>();
    private int proximoId = 1;
    private boolean exemplosCarregados = false;

    // Guarda o último item excluído para permitir a ação "Desfazer".
    private Registro ultimoRemovido;
    private int posicaoUltimoRemovido = -1;

    private RegistroRepository() {
    }

    public static synchronized RegistroRepository getInstance() {
        if (instancia == null) {
            instancia = new RegistroRepository();
        }
        return instancia;
    }

    // ============================ CREATE ============================

    /** Cadastra um novo registro, gerando id e datas. Novos itens ficam no topo da lista. */
    @NonNull
    public synchronized Registro adicionar(@NonNull Registro registro) {
        Registro novo = new Registro(registro);
        long agora = System.currentTimeMillis();
        novo.setId(proximoId++);
        novo.setDataCriacao(agora);
        novo.setDataAtualizacao(agora);
        registros.add(0, novo);
        return new Registro(novo);
    }

    // ============================= READ =============================

    @NonNull
    public synchronized List<Registro> listarTodos() {
        return copiar(registros);
    }

    @Nullable
    public synchronized Registro buscarPorId(int id) {
        int indice = indiceDe(id);
        return indice >= 0 ? new Registro(registros.get(indice)) : null;
    }

    /**
     * Filtra por texto (título, setor ou descrição) e por categoria.
     * @param categoria use {@code null} para considerar todas as categorias.
     */
    @NonNull
    public synchronized List<Registro> filtrar(@Nullable String texto, @Nullable Categoria categoria) {
        List<Registro> resultado = new ArrayList<>();
        for (Registro r : registros) {
            boolean categoriaOk = categoria == null || r.getCategoria() == categoria;
            if (categoriaOk && r.contemTexto(texto)) {
                resultado.add(new Registro(r));
            }
        }
        return resultado;
    }

    public synchronized int contarTotal() {
        return registros.size();
    }

    public synchronized int contarPorCategoria(@NonNull Categoria categoria) {
        int total = 0;
        for (Registro r : registros) {
            if (r.getCategoria() == categoria) total++;
        }
        return total;
    }

    public synchronized int contarPendentes() {
        int total = 0;
        for (Registro r : registros) {
            if (!r.isConcluido()) total++;
        }
        return total;
    }

    public synchronized int contarPrioridadeAltaPendentes() {
        int total = 0;
        for (Registro r : registros) {
            if (!r.isConcluido() && r.getPrioridade() == Prioridade.ALTA) total++;
        }
        return total;
    }

    // ============================ UPDATE ============================

    /** Atualiza um registro existente. Retorna {@code false} se o id não existir. */
    public synchronized boolean atualizar(@NonNull Registro registro) {
        int indice = indiceDe(registro.getId());
        if (indice < 0) return false;

        Registro atualizado = new Registro(registro);
        atualizado.setDataCriacao(registros.get(indice).getDataCriacao());
        atualizado.setDataAtualizacao(Math.max(System.currentTimeMillis(),
                registros.get(indice).getDataAtualizacao() + 1));
        registros.set(indice, atualizado);
        return true;
    }

    // ============================ DELETE ============================

    /** Remove o registro e devolve uma cópia dele (ou {@code null} se não existir). */
    @Nullable
    public synchronized Registro remover(int id) {
        int indice = indiceDe(id);
        if (indice < 0) return null;

        ultimoRemovido = registros.remove(indice);
        posicaoUltimoRemovido = indice;
        return new Registro(ultimoRemovido);
    }

    /** Restaura o último registro excluído na mesma posição em que estava. */
    public synchronized boolean desfazerUltimaRemocao() {
        if (ultimoRemovido == null || indiceDe(ultimoRemovido.getId()) >= 0) return false;

        int posicao = Math.min(Math.max(posicaoUltimoRemovido, 0), registros.size());
        registros.add(posicao, ultimoRemovido);
        ultimoRemovido = null;
        posicaoUltimoRemovido = -1;
        return true;
    }

    // ========================== UTILITÁRIOS ==========================

    /** Esvazia o repositório (usado principalmente nos testes automatizados). */
    public synchronized void limpar() {
        registros.clear();
        proximoId = 1;
        ultimoRemovido = null;
        posicaoUltimoRemovido = -1;
        exemplosCarregados = false;
    }

    /**
     * Carrega alguns registros de exemplo na primeira abertura do app,
     * para facilitar a demonstração. Executa apenas uma vez por sessão.
     */
    public synchronized void carregarExemplosSeNecessario() {
        if (exemplosCarregados) return;
        exemplosCarregados = true;
        if (!registros.isEmpty()) return;

        // Inseridos do mais antigo para o mais recente (cada novo vai para o topo).
        adicionar(new Registro("Troca de turno - Linha 2",
                "Máquina 04 operando com ruído anormal no rolamento. Equipe de manutenção avisada.",
                "Linha 2 - Envase", Categoria.ANOTACAO, Prioridade.ALTA, false));
        adicionar(new Registro("Checklist de segurança diário",
                "EPIs conferidos, extintores no lugar, saídas de emergência desobstruídas, "
                        + "botões de parada testados.",
                "Linha 1 - Prensas", Categoria.LISTA_CONTROLE, Prioridade.ALTA, true));
        adicionar(new Registro("Parada para setup - Injetora 3",
                "Setup do molde 115 iniciado às 07h40 e finalizado às 08h25.",
                "Injeção", Categoria.REGISTRO, Prioridade.MEDIA, true));
        adicionar(new Registro("Conferência de matéria-prima",
                "Verificar lote 2026-118 de resina antes de liberar para a produção.",
                "Almoxarifado", Categoria.LISTA_CONTROLE, Prioridade.MEDIA, false));
        adicionar(new Registro("Ajuste de temperatura da estufa",
                "Temperatura ajustada de 180 °C para 175 °C conforme instrução de trabalho IT-042.",
                "Pintura", Categoria.REGISTRO, Prioridade.BAIXA, false));
    }

    private int indiceDe(int id) {
        for (int i = 0; i < registros.size(); i++) {
            if (registros.get(i).getId() == id) return i;
        }
        return -1;
    }

    private static List<Registro> copiar(List<Registro> origem) {
        List<Registro> copia = new ArrayList<>(origem.size());
        for (Registro r : origem) copia.add(new Registro(r));
        return copia;
    }
}
