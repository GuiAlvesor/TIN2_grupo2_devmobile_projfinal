package com.example.registroproducao.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.registroproducao.R;
import com.example.registroproducao.model.Categoria;
import com.example.registroproducao.model.Registro;

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

        private final View viewFaixaCategoria;
        private final ImageView ivIconeCategoria;
        private final TextView tvCategoria;
        private final TextView tvPrioridade;
        private final TextView tvTitulo;
        private final TextView tvDescricao;
        private final TextView tvSetor;
        private final TextView tvStatus;

        RegistroViewHolder(@NonNull View itemView) {
            super(itemView);
            viewFaixaCategoria = itemView.findViewById(R.id.viewFaixaCategoria);
            ivIconeCategoria = itemView.findViewById(R.id.ivIconeCategoria);
            tvCategoria = itemView.findViewById(R.id.tvCategoria);
            tvPrioridade = itemView.findViewById(R.id.tvPrioridade);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvDescricao = itemView.findViewById(R.id.tvDescricao);
            tvSetor = itemView.findViewById(R.id.tvSetor);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        void vincular(@NonNull Registro registro, @NonNull OnRegistroClickListener listener) {
            Context ctx = itemView.getContext();
            Categoria categoria = registro.getCategoria();
            int corCategoria = ContextCompat.getColor(ctx, categoria.getCorRes());

            ViewCompat.setBackgroundTintList(viewFaixaCategoria, ColorStateList.valueOf(corCategoria));
            ivIconeCategoria.setImageResource(categoria.getIconeRes());
            ImageViewCompat.setImageTintList(ivIconeCategoria, ColorStateList.valueOf(corCategoria));
            tvCategoria.setText(categoria.getRotuloRes());
            tvCategoria.setTextColor(corCategoria);

            tvPrioridade.setText(registro.getPrioridade().getRotuloRes());
            ViewCompat.setBackgroundTintList(tvPrioridade, ColorStateList.valueOf(
                    ContextCompat.getColor(ctx, registro.getPrioridade().getCorRes())));

            tvTitulo.setText(registro.getTitulo());

            if (registro.getDescricao().isEmpty()) {
                tvDescricao.setVisibility(View.GONE);
            } else {
                tvDescricao.setVisibility(View.VISIBLE);
                tvDescricao.setText(registro.getDescricao());
            }

            tvSetor.setText(registro.getSetor().isEmpty()
                    ? ctx.getString(R.string.setor_nao_informado) : registro.getSetor());

            int corStatus = ContextCompat.getColor(ctx,
                    registro.isConcluido() ? R.color.status_concluido : R.color.status_pendente);
            tvStatus.setText(registro.isConcluido() ? R.string.status_concluido : R.string.status_pendente);
            tvStatus.setTextColor(corStatus);
            tvStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    registro.isConcluido() ? R.drawable.ic_check_circle_pequeno : R.drawable.ic_schedule_pequeno,
                    0, 0, 0);
            TextViewCompat.setCompoundDrawableTintList(tvStatus, ColorStateList.valueOf(corStatus));

            itemView.setOnClickListener(v -> listener.onRegistroClick(registro));
        }
    }
}
