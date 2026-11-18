package com.example.registroproducao;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.registroproducao.data.RegistroRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Testes instrumentados (executam no emulador/dispositivo) da tela de cadastro.
 */
@RunWith(AndroidJUnit4.class)
public class FormularioRegistroActivityTest {

    private Context contexto;

    @Before
    public void preparar() {
        contexto = ApplicationProvider.getApplicationContext();
        RegistroRepository.getInstance().limpar();
    }

    @Test
    public void pacoteDoAplicativoEstaCorreto() {
        assertEquals("com.example.registroproducao", contexto.getPackageName());
    }

    @Test
    public void cadastrarRegistro_adicionaNaLista() {
        try (ActivityScenario<FormularioRegistroActivity> ignored = ActivityScenario.launch(
                FormularioRegistroActivity.criarIntentNovo(contexto, null))) {

            onView(withId(R.id.etTitulo)).perform(typeText("Teste instrumentado"), closeSoftKeyboard());
            onView(withId(R.id.btnSalvar)).perform(click());

            assertEquals(1, RegistroRepository.getInstance().filtrar("Teste instrumentado", null).size());
        }
    }

    @Test
    public void tituloVazio_naoCadastra() {
        try (ActivityScenario<FormularioRegistroActivity> ignored = ActivityScenario.launch(
                FormularioRegistroActivity.criarIntentNovo(contexto, null))) {

            onView(withId(R.id.btnSalvar)).perform(click());

            assertEquals(0, RegistroRepository.getInstance().contarTotal());
        }
    }
}
