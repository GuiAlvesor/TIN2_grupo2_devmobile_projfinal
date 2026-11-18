package com.example.registroproducao;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
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
import com.example.registroproducao.model.Registro;
import com.example.registroproducao.util.Constantes;
import com.example.registroproducao.util.TextWatcherSimples;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

/**
 * Tela de listagem (READ) com busca por texto, filtro por categoria,
 * exclusão por gesto (deslizar) e opção de desfazer.
 */
public class ListaRegistrosActivity extends BaseActivity {

    /** Cria o Intent de navegação para esta tela, opcionalmente já filtrada por categoria. */
    @NonNull
    public static Intent criarIntent(@NonNull Context contexto, @Nullable Categoria filtro) {
        Intent intent = new Intent(contexto, ListaRegistrosActivity.class);
        if (filtro != null) {
            intent.putExtra(Constantes.EXTRA_CATEGORIA, filtro.name());
        }
        return intent;
    }

    private RegistroRepository repositorio;
    private RegistroAdapter adapter;

    private View raiz;
    private TextInputEditText etBusca;
    private ChipGroup chipGroupFiltro;
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
        configurarToolbar(toolbar, R.string.titulo_lista, true);

        repositorio = RegistroRepository.getInstance();

        etBusca = findViewById(R.id.etBusca);
        chipGroupFiltro = findViewById(R.id.chipGroupFiltro);
        tvContador = findViewById(R.id.tvContador);
        rvRegistros = findViewById(R.id.rvRegistros);
        layoutVazio = findViewById(R.id.layoutVazio);
        fabNovo = findViewById(R.id.fabNovo);

        configurarLista();

        // Só aplica o filtro vindo do Intent na primeira criação; após rotação de tela
        // o próprio Android restaura o chip selecionado e o texto da busca.
        if (savedInstanceState == null) {
            selecionarChip(Categoria.deNome(getIntent().getStringExtra(Constantes.EXTRA_CATEGORIA)));
        }

        chipGroupFiltro.setOnCheckedStateChangeListener((grupo, ids) -> atualizarLista());
        etBusca.addTextChangedListener(new TextWatcherSimples() {
            @Override
            public void afterTextChanged(Editable s) {
                atualizarLista();
            }
        });

        fabNovo.setOnClickListener(v -> startActivity(
                FormularioRegistroActivity.criarIntentNovo(this, getCategoriaSelecionada())));
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
        List<Registro> lista = repositorio.filtrar(texto, getCategoriaSelecionada());
        adapter.submitList(lista);

        tvContador.setText(getResources().getQuantityString(
                R.plurals.contador_registros, lista.size(), lista.size()));

        boolean vazio = lista.isEmpty();
        layoutVazio.setVisibility(vazio ? View.VISIBLE : View.GONE);
        rvRegistros.setVisibility(vazio ? View.GONE : View.VISIBLE);
    }

    @Nullable
    private Categoria getCategoriaSelecionada() {
        int id = chipGroupFiltro.getCheckedChipId();
        // Os ids de R não são constantes (AGP 8+), por isso if/else em vez de switch.
        if (id == R.id.chipFiltroRegistros) return Categoria.REGISTRO;
        if (id == R.id.chipFiltroListas) return Categoria.LISTA_CONTROLE;
        if (id == R.id.chipFiltroAnotacoes) return Categoria.ANOTACAO;
        return null;
    }

    private void selecionarChip(@Nullable Categoria categoria) {
        if (categoria == Categoria.REGISTRO) chipGroupFiltro.check(R.id.chipFiltroRegistros);
        else if (categoria == Categoria.LISTA_CONTROLE) chipGroupFiltro.check(R.id.chipFiltroListas);
        else if (categoria == Categoria.ANOTACAO) chipGroupFiltro.check(R.id.chipFiltroAnotacoes);
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
