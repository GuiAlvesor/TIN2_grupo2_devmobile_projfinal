package com.example.registroproducao.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.registroproducao.R;
import com.example.registroproducao.model.Registro;
import com.example.registroproducao.util.EstiloVisual;
import com.example.registroproducao.util.FormatadorData;

/**
 * Adapter do RecyclerView da listagem. Usa {@link ListAdapter} + {@link DiffUtil}
 * para atualizar somente os itens que mudaram, com animações automáticas.
 */
public class RegistroAdapter extends ListAdapter<Registro, RegistroAdapter.RegistroViewHolder> {

    public interface OnRegistroClickListener {
        void onRegistroClick(@NonNull Registro registro);
    }

    private static final DiffUtil.ItemCallback<Registro> COMPARADOR = new DiffUtil.ItemCallback<Registro>() {
        @Override
        public boolean areItemsTheSame(@NonNull Registro antigo, @NonNull Registro novo) {
            return antigo.getId() == novo.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Registro antigo, @NonNull Registro novo) {
            return antigo.equals(novo);
        }
    };

    private final OnRegistroClickListener listener;

    public RegistroAdapter(@NonNull OnRegistroClickListener listener) {
        super(COMPARADOR);
        this.listener = listener;
    }

    @NonNull
    public Registro getRegistro(int posicao) {
        return getItem(posicao);
    }

    @NonNull
    @Override
    public RegistroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_registro, parent, false);
        return new RegistroViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RegistroViewHolder holder, int position) {
        holder.vincular(getItem(position), listener);
    }

    static class RegistroViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivIconeCategoria;
        private final TextView tvCategoria;
        private final TextView tvTitulo;
        private final TextView tvPrioridade;
        private final TextView tvSetor;
        private final TextView tvStatus;

        RegistroViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIconeCategoria = itemView.findViewById(R.id.ivIconeCategoria);
            tvCategoria = itemView.findViewById(R.id.tvCategoria);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvPrioridade = itemView.findViewById(R.id.tvPrioridade);
            tvSetor = itemView.findViewById(R.id.tvSetor);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        void vincular(@NonNull Registro registro, @NonNull OnRegistroClickListener listener) {
            Context ctx = itemView.getContext();

            EstiloVisual.aplicarIconeCategoria(ivIconeCategoria, registro.getCategoria());
            tvCategoria.setText(registro.getCategoria().getRotuloRes());
            tvTitulo.setText(registro.getTitulo());
            EstiloVisual.aplicarPrioridade(tvPrioridade, registro.getPrioridade());

            // Rodapé: "Setor · hora" e status à direita.
            String hora = FormatadorData.formatarHora(registro.getDataCriacao());
            tvSetor.setText(registro.getSetor().isEmpty()
                    ? hora : ctx.getString(R.string.formato_meta_2, registro.getSetor(), hora));
            EstiloVisual.aplicarStatus(tvStatus, registro.isConcluido());

            itemView.setOnClickListener(v -> listener.onRegistroClick(registro));
        }
    }
}
