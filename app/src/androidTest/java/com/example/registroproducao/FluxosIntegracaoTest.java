package com.example.registroproducao;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.registroproducao.data.RegistroRepository;
import com.example.registroproducao.model.Categoria;
import com.example.registroproducao.model.FiltroLista;
import com.example.registroproducao.model.ItemChecklist;
import com.example.registroproducao.model.Prioridade;
import com.example.registroproducao.model.Registro;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

/**
 * Testes de integração (executam no emulador/dispositivo): telas reais + repositório em memória.
 * Usa replaceText (e não typeText): a digitação simulada tecla a tecla não suporta acentos no
 * teclado virtual e sofre com a maiúscula automática dos campos.
 */
@RunWith(AndroidJUnit4.class)
public class FluxosIntegracaoTest {

    private Context contexto;
    private RegistroRepository repositorio;

    @Before
    public void preparar() {
        contexto = ApplicationProvider.getApplicationContext();
        repositorio = RegistroRepository.getInstance();
        repositorio.limpar();
    }

    /** Lista de controle com um único item, para que os botões Sim/Não/N/A tenham ids únicos. */
    private Registro checklistComUmItem() {
        Registro r = new Registro("Ronda de segurança", "", "Linha 1",
                Categoria.LISTA_CONTROLE, Prioridade.ALTA, false);
        r.setItens(Collections.singletonList(new ItemChecklist("Extintores no lugar?")));
        return repositorio.adicionar(r);
    }

    // ===================== Checklist =====================

    @Test
    public void checklist_responderSimEFinalizar_concluiORegistro() {
        Registro salvo = checklistComUmItem();
        try (ActivityScenario<ChecklistActivity> ignored = ActivityScenario.launch(
                ChecklistActivity.criarIntent(contexto, salvo.getId()))) {

            onView(withId(R.id.btnSim)).perform(click());
            onView(withId(R.id.btnFinalizar)).perform(click());

            Registro lido = repositorio.buscarPorId(salvo.getId());
            assertTrue(lido.isConcluido());
            assertEquals(ItemChecklist.Resposta.SIM, lido.getItens().get(0).getResposta());
        }
    }

    @Test
    public void checklist_naoSemAcaoCorretiva_naoFinaliza() {
        Registro salvo = checklistComUmItem();
        try (ActivityScenario<ChecklistActivity> ignored = ActivityScenario.launch(
                ChecklistActivity.criarIntent(contexto, salvo.getId()))) {

            onView(withId(R.id.btnNao)).perform(click());
            onView(withId(R.id.layoutAcaoCorretiva)).check(matches(isDisplayed()));
            onView(withId(R.id.btnFinalizar)).perform(click());

            Registro lido = repositorio.buscarPorId(salvo.getId());
            assertFalse(lido.isConcluido());
            assertNull(lido.getItens().get(0).getResposta()); // nada foi gravado
        }
    }

    @Test
    public void checklist_naoComAcaoCorretiva_gravaObservacao() {
        Registro salvo = checklistComUmItem();
        try (ActivityScenario<ChecklistActivity> ignored = ActivityScenario.launch(
                ChecklistActivity.criarIntent(contexto, salvo.getId()))) {

            onView(withId(R.id.btnNao)).perform(click());
            onView(withId(R.id.etAcaoCorretiva)).perform(scrollTo(), replaceText("Trocar extintor"), closeSoftKeyboard());
            onView(withId(R.id.btnFinalizar)).perform(click());

            ItemChecklist item = repositorio.buscarPorId(salvo.getId()).getItens().get(0);
            assertEquals(ItemChecklist.Resposta.NAO, item.getResposta());
            assertEquals("Trocar extintor", item.getObservacao());
        }
    }

    @Test
    public void checklist_salvarRascunho_mantemPendente() {
        Registro salvo = checklistComUmItem();
        try (ActivityScenario<ChecklistActivity> ignored = ActivityScenario.launch(
                ChecklistActivity.criarIntent(contexto, salvo.getId()))) {

            onView(withId(R.id.btnNa)).perform(click());
            onView(withId(R.id.btnSalvarRascunho)).perform(click());

            Registro lido = repositorio.buscarPorId(salvo.getId());
            assertFalse(lido.isConcluido());
            assertEquals(ItemChecklist.Resposta.NA, lido.getItens().get(0).getResposta());
        }
    }

    // ===================== Formulário =====================

    @Test
    public void formulario_novaListaDeControleSemItens_usaOs5ItensPadrao() {
        try (ActivityScenario<FormularioRegistroActivity> ignored = ActivityScenario.launch(
                FormularioRegistroActivity.criarIntentNovo(contexto, Categoria.LISTA_CONTROLE))) {

            onView(withId(R.id.etTitulo)).perform(replaceText("Conferência de EPIs"), closeSoftKeyboard());
            onView(withId(R.id.btnSalvar)).perform(click());

            Registro salvo = repositorio.filtrar("Conferência de EPIs", Categoria.LISTA_CONTROLE).get(0);
            assertEquals(5, salvo.getTotalItens());
        }
    }

    @Test
    public void formulario_prioridadeAltaSelecionada_eGravada() {
        try (ActivityScenario<FormularioRegistroActivity> ignored = ActivityScenario.launch(
                FormularioRegistroActivity.criarIntentNovo(contexto, null))) {

            onView(withId(R.id.etTitulo)).perform(replaceText("Vazamento de óleo"), closeSoftKeyboard());
            onView(withId(R.id.btnPrioridadeAlta)).perform(scrollTo(), click());
            onView(withId(R.id.btnSalvar)).perform(click());

            Registro salvo = repositorio.filtrar("Vazamento", null).get(0);
            assertEquals(Prioridade.ALTA, salvo.getPrioridade());
            assertEquals(Categoria.REGISTRO, salvo.getCategoria());
        }
    }

    // ===================== Lista =====================

    @Test
    public void lista_abertaNoFiltroChecklists_mostraSoListasDeControle() {
        repositorio.adicionar(new Registro("Anotação qualquer", "", "", Categoria.ANOTACAO, Prioridade.BAIXA, false));
        checklistComUmItem();

        try (ActivityScenario<ListaRegistrosActivity> cenario = ActivityScenario.launch(
                ListaRegistrosActivity.criarIntent(contexto, FiltroLista.CHECKLISTS))) {
            cenario.onActivity(activity -> {
                RecyclerView rv = activity.findViewById(R.id.rvRegistros);
                assertEquals(1, rv.getAdapter().getItemCount());
            });
        }
    }
}
