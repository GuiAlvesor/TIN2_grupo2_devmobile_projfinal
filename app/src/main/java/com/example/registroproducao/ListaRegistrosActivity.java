package com.example.registroproducao;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.registroproducao.adapter.RegistroAdapter;
import com.example.registroproducao.data.RegistroRepository;
import com.example.registroproducao.model.Categoria;
import com.example.registroproducao.model.FiltroLista;
import com.example.registroproducao.model.Registro;
import com.example.registroproducao.util.Constantes;
import com.example.registroproducao.util.FormatadorData;
import com.example.registroproducao.util.TextWatcherSimples;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

/**
 * Tela de listagem (READ) com busca por texto, filtros rápidos (Todos, Pendentes, Alta,
 * Checklists) com contador, exclusão por gesto (deslizar) e opção de desfazer.
 */
public class ListaRegistrosActivity extends BaseActivity {

    /** Cria o Intent de navegação para esta tela, já com um dos filtros rápidos selecionado. */
    @NonNull
    public static Intent criarIntent(@NonNull Context contexto, @NonNull FiltroLista filtro) {
        return new Intent(contexto, ListaRegistrosActivity.class)
                .putExtra(Constantes.EXTRA_FILTRO, filtro.name());
    }

    private RegistroRepository repositorio;
    private RegistroAdapter adapter;

    private View raiz;
    private EditText etBusca;
    private ChipGroup chipGroupFiltro;
    private Chip chipTodos;
    private Chip chipPendentes;
    private Chip chipAlta;
    private TextView tvRotuloSecao;
    private TextView tvContador;
    private RecyclerView rvRegistros;
    private View layoutVazio;
    private ExtendedFloatingActionButton fabNovo;

    /** Abre a tela de detalhes e recebe o resultado (ex.: registro excluído). */
    private final ActivityResultLauncher<Intent> detalheLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), resultado -> {
                if (resultado.getResultCode() == Constantes.RESULTADO_REGISTRO_EXCLUIDO
                        && resultado.getData() != null) {
                    String titulo = resultado.getData().getStringExtra(Constantes.EXTRA_TITULO_REGISTRO);
                    mostrarSnackbarExclusao(titulo);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_registros);

        raiz = findViewById(R.id.root);
        aplicarInsetsDeSistema(findViewById(R.id.appBar), raiz);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        // O título grande fica abaixo da Toolbar, que mostra apenas o botão de voltar.
        configurarToolbar(toolbar, 0, R.drawable.ic_arrow_back, R.string.acao_voltar);

        repositorio = RegistroRepository.getInstance();

        etBusca = findViewById(R.id.etBusca);
        chipGroupFiltro = findViewById(R.id.chipGroupFiltro);
        chipTodos = findViewById(R.id.chipFiltroTodos);
        chipPendentes = findViewById(R.id.chipFiltroPendentes);
        chipAlta = findViewById(R.id.chipFiltroAlta);
        tvRotuloSecao = findViewById(R.id.tvRotuloSecao);
        tvContador = findViewById(R.id.tvContador);
        rvRegistros = findViewById(R.id.rvRegistros);
        layoutVazio = findViewById(R.id.layoutVazio);
        fabNovo = findViewById(R.id.fabNovo);

        configurarLista();

        // Só aplica o filtro vindo do Intent na primeira criação; após rotação de tela
        // o próprio Android restaura o chip selecionado e o texto da busca.
        if (savedInstanceState == null) {
            selecionarChip(FiltroLista.deNome(getIntent().getStringExtra(Constantes.EXTRA_FILTRO)));
        }

        chipGroupFiltro.setOnCheckedStateChangeListener((grupo, ids) -> atualizarLista());
        etBusca.addTextChangedListener(new TextWatcherSimples() {
            @Override
            public void afterTextChanged(Editable s) {
                atualizarLista();
            }
        });

        // Em "Checklists", o novo registro já começa como Lista de controle.
        fabNovo.setOnClickListener(v -> startActivity(FormularioRegistroActivity.criarIntentNovo(this,
                getFiltroSelecionado() == FiltroLista.CHECKLISTS ? Categoria.LISTA_CONTROLE : null)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarrega sempre que a tela volta ao primeiro plano (após cadastrar/editar/excluir).
        atualizarLista();
    }

    private void configurarLista() {
        adapter = new RegistroAdapter(registro ->
                detalheLauncher.launch(DetalheRegistroActivity.criarIntent(this, registro.getId())));
        rvRegistros.setLayoutManager(new LinearLayoutManager(this));
        rvRegistros.setAdapter(adapter);

        // Recolhe o botão flutuante ao rolar para baixo e expande ao rolar para cima.
        rvRegistros.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 4 && fabNovo.isExtended()) fabNovo.shrink();
                else if (dy < -4 && !fabNovo.isExtended()) fabNovo.extend();
            }
        });

        new ItemTouchHelper(new ExclusaoPorGestoCallback()).attachToRecyclerView(rvRegistros);
    }

    private void atualizarLista() {
        String texto = etBusca.getText() != null ? etBusca.getText().toString() : "";
        List<Registro> lista = repositorio.filtrarPor(texto, getFiltroSelecionado());
        adapter.submitList(lista);

        atualizarContadoresDosFiltros();
        tvContador.setText(getResources().getQuantityString(
                R.plurals.contador_registros, lista.size(), lista.size()));
        tvRotuloSecao.setText(todosDeHoje(lista) ? R.string.secao_hoje : R.string.secao_mais_recentes);

        boolean vazio = lista.isEmpty();
        layoutVazio.setVisibility(vazio ? View.VISIBLE : View.GONE);
        rvRegistros.setVisibility(vazio ? View.GONE : View.VISIBLE);
    }

    /** "Todos · N", "Pendentes · N" e "Alta · N" (os totais não dependem do texto da busca). */
    private void atualizarContadoresDosFiltros() {
        chipTodos.setText(getString(R.string.filtro_com_contador,
                getString(R.string.filtro_todos), repositorio.contar(FiltroLista.TODOS)));
        chipPendentes.setText(getString(R.string.filtro_com_contador,
                getString(R.string.filtro_pendentes), repositorio.contar(FiltroLista.PENDENTES)));
        chipAlta.setText(getString(R.string.filtro_com_contador,
                getString(R.string.filtro_alta), repositorio.contar(FiltroLista.ALTA)));
    }

    private static boolean todosDeHoje(@NonNull List<Registro> lista) {
        for (Registro r : lista) {
            if (!FormatadorData.ehHoje(r.getDataCriacao())) return false;
        }
        return true;
    }

    @NonNull
    private FiltroLista getFiltroSelecionado() {
        int id = chipGroupFiltro.getCheckedChipId();
        // Os ids de R não são constantes (AGP 8+), por isso if/else em vez de switch.
        if (id == R.id.chipFiltroPendentes) return FiltroLista.PENDENTES;
        if (id == R.id.chipFiltroAlta) return FiltroLista.ALTA;
        if (id == R.id.chipFiltroChecklists) return FiltroLista.CHECKLISTS;
        return FiltroLista.TODOS;
    }

    private void selecionarChip(@NonNull FiltroLista filtro) {
        if (filtro == FiltroLista.PENDENTES) chipGroupFiltro.check(R.id.chipFiltroPendentes);
        else if (filtro == FiltroLista.ALTA) chipGroupFiltro.check(R.id.chipFiltroAlta);
        else if (filtro == FiltroLista.CHECKLISTS) chipGroupFiltro.check(R.id.chipFiltroChecklists);
        else chipGroupFiltro.check(R.id.chipFiltroTodos);
    }

    private void mostrarSnackbarExclusao(@Nullable String titulo) {
        Snackbar.make(raiz, getString(R.string.msg_registro_excluido, titulo == null ? "" : titulo),
                        Snackbar.LENGTH_LONG)
                .setAction(R.string.acao_desfazer, v -> {
                    if (repositorio.desfazerUltimaRemocao()) {
                        atualizarLista();
                        mostrarMensagem(R.string.msg_registro_restaurado);
                    }
                })
                .setAnchorView(fabNovo)
                .show();
    }

    /** Exclui um registro ao deslizá-lo para a esquerda ou direita, desenhando um fundo vermelho. */
    private class ExclusaoPorGestoCallback extends ItemTouchHelper.SimpleCallback {

        private final Paint pintura = new Paint();
        private final Drawable iconeExcluir;
        private final int margemIcone;

        ExclusaoPorGestoCallback() {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            pintura.setColor(ContextCompat.getColor(ListaRegistrosActivity.this, R.color.perigo));
            iconeExcluir = ContextCompat.getDrawable(ListaRegistrosActivity.this, R.drawable.ic_delete);
            margemIcone = getResources().getDimensionPixelSize(R.dimen.espaco_grande);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder origem,
                              @NonNull RecyclerView.ViewHolder destino) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder holder, int direcao) {
            int posicao = holder.getBindingAdapterPosition();
            if (posicao == RecyclerView.NO_POSITION) return;

            Registro removido = repositorio.remover(adapter.getRegistro(posicao).getId());
            atualizarLista();
            if (removido != null) {
                mostrarSnackbarExclusao(removido.getTitulo());
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView rv,
                                @NonNull RecyclerView.ViewHolder holder, float dX, float dY,
                                int estado, boolean ativo) {
            View item = holder.itemView;
            if (dX != 0 && iconeExcluir != null) {
                int alturaIcone = iconeExcluir.getIntrinsicHeight();
                int topoIcone = item.getTop() + (item.getHeight() - alturaIcone) / 2;
                if (dX > 0) {
                    c.drawRect(item.getLeft(), item.getTop(), item.getLeft() + dX, item.getBottom(), pintura);
                    iconeExcluir.setBounds(item.getLeft() + margemIcone, topoIcone,
                            item.getLeft() + margemIcone + iconeExcluir.getIntrinsicWidth(),
                            topoIcone + alturaIcone);
                } else {
                    c.drawRect(item.getRight() + dX, item.getTop(), item.getRight(), item.getBottom(), pintura);
                    iconeExcluir.setBounds(item.getRight() - margemIcone - iconeExcluir.getIntrinsicWidth(),
                            topoIcone, item.getRight() - margemIcone, topoIcone + alturaIcone);
                }
                iconeExcluir.draw(c);
            }
            super.onChildDraw(c, rv, holder, dX, dY, estado, ativo);
        }
    }
}
