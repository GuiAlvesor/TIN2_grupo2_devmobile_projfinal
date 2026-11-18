package com.example.registroproducao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.registroproducao.data.RegistroRepository;
import com.example.registroproducao.model.Categoria;
import com.example.registroproducao.model.Prioridade;
import com.example.registroproducao.model.Registro;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Testes unitários (executam na JVM, sem emulador) do CRUD em memória.
 */
public class RegistroRepositoryTest {

    private RegistroRepository repositorio;

    @Before
    public void prepararRepositorio() {
        repositorio = RegistroRepository.getInstance();
        repositorio.limpar();
    }

    private Registro novo(String titulo, Categoria categoria) {
        return new Registro(titulo, "descrição de " + titulo, "Linha 1",
                categoria, Prioridade.MEDIA, false);
    }

    @Test
    public void adicionar_geraIdEArmazenaRegistro() {
        Registro salvo = repositorio.adicionar(novo("Inspeção visual", Categoria.REGISTRO));

        assertEquals(1, salvo.getId());
        assertEquals(1, repositorio.contarTotal());
        assertTrue(salvo.getDataCriacao() > 0);
        assertEquals("Inspeção visual", repositorio.buscarPorId(salvo.getId()).getTitulo());
    }

    @Test
    public void adicionar_novosRegistrosFicamNoTopo() {
        repositorio.adicionar(novo("Primeiro", Categoria.REGISTRO));
        repositorio.adicionar(novo("Segundo", Categoria.REGISTRO));

        assertEquals("Segundo", repositorio.listarTodos().get(0).getTitulo());
    }

    @Test
    public void atualizar_alteraCamposEMantemDataDeCriacao() {
        Registro salvo = repositorio.adicionar(novo("Original", Categoria.ANOTACAO));
        salvo.setTitulo("Alterado");
        salvo.setConcluido(true);

        assertTrue(repositorio.atualizar(salvo));

        Registro lido = repositorio.buscarPorId(salvo.getId());
        assertNotNull(lido);
        assertEquals("Alterado", lido.getTitulo());
        assertTrue(lido.isConcluido());
        assertEquals(salvo.getDataCriacao(), lido.getDataCriacao());
        assertTrue(lido.getDataAtualizacao() > lido.getDataCriacao());
    }

    @Test
    public void atualizar_registroInexistenteRetornaFalse() {
        Registro fantasma = novo("Não existe", Categoria.REGISTRO);
        fantasma.setId(999);
        assertFalse(repositorio.atualizar(fantasma));
    }

    @Test
    public void buscarPorId_retornaCopiaProtegendoALista() {
        Registro salvo = repositorio.adicionar(novo("Protegido", Categoria.REGISTRO));
        Registro copia = repositorio.buscarPorId(salvo.getId());
        copia.setTitulo("Mudado fora do repositório");

        assertEquals("Protegido", repositorio.buscarPorId(salvo.getId()).getTitulo());
    }

    @Test
    public void remover_eDesfazerRestauraNaMesmaPosicao() {
        repositorio.adicionar(novo("A", Categoria.REGISTRO));
        Registro b = repositorio.adicionar(novo("B", Categoria.REGISTRO));
        repositorio.adicionar(novo("C", Categoria.REGISTRO));
        // Ordem atual: C, B, A

        Registro removido = repositorio.remover(b.getId());
        assertNotNull(removido);
        assertEquals(2, repositorio.contarTotal());
        assertNull(repositorio.buscarPorId(b.getId()));

        assertTrue(repositorio.desfazerUltimaRemocao());
        assertEquals("B", repositorio.listarTodos().get(1).getTitulo());
        assertFalse(repositorio.desfazerUltimaRemocao());
    }

    @Test
    public void remover_idInexistenteRetornaNull() {
        assertNull(repositorio.remover(42));
    }

    @Test
    public void filtrar_porTextoECategoria() {
        repositorio.adicionar(novo("Checklist de EPI", Categoria.LISTA_CONTROLE));
        repositorio.adicionar(novo("Parada da prensa", Categoria.REGISTRO));
        repositorio.adicionar(novo("Checklist de limpeza", Categoria.LISTA_CONTROLE));

        assertEquals(2, repositorio.filtrar("checklist", null).size());
        assertEquals(1, repositorio.filtrar("prensa", Categoria.REGISTRO).size());
        assertEquals(0, repositorio.filtrar("prensa", Categoria.LISTA_CONTROLE).size());

        List<Registro> todos = repositorio.filtrar("", null);
        assertEquals(3, todos.size());
    }

    @Test
    public void contadores_refletemOsDados() {
        Registro alta = novo("Vazamento", Categoria.ANOTACAO);
        alta.setPrioridade(Prioridade.ALTA);
        repositorio.adicionar(alta);

        Registro concluido = novo("Feito", Categoria.REGISTRO);
        concluido.setConcluido(true);
        repositorio.adicionar(concluido);

        assertEquals(2, repositorio.contarTotal());
        assertEquals(1, repositorio.contarPendentes());
        assertEquals(1, repositorio.contarPrioridadeAltaPendentes());
        assertEquals(1, repositorio.contarPorCategoria(Categoria.ANOTACAO));
    }

    @Test
    public void carregarExemplos_executaApenasUmaVez() {
        repositorio.carregarExemplosSeNecessario();
        int quantidade = repositorio.contarTotal();
        assertTrue(quantidade > 0);

        repositorio.carregarExemplosSeNecessario();
        assertEquals(quantidade, repositorio.contarTotal());
    }
}
