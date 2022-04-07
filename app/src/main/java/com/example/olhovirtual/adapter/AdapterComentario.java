package com.example.olhovirtual.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.olhovirtual.R;
import com.example.olhovirtual.model.Comentario;

import java.util.List;

public class AdapterComentario extends RecyclerView.Adapter<AdapterComentario.MyViewHolder> {

    private List<Comentario> listaComentarios;
    private Context context;

    public AdapterComentario(List<Comentario> listaComentarios, Context context) {
        this.listaComentarios = listaComentarios;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_comentario,parent,false);
        return new AdapterComentario.MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Comentario comentario = listaComentarios.get(position);

        holder.nomeUsuario.setText(comentario.getNomeUsuario());
        holder.comentario.setText(comentario.getComentario());

    }

    @Override
    public int getItemCount() {

        return listaComentarios.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView nomeUsuario, comentario;

        public MyViewHolder(View itemView){
            super(itemView);

            nomeUsuario = itemView.findViewById(R.id.textAdComNomeUsuario);
            comentario = itemView.findViewById(R.id.textAdComentario);
        }

    }

}
