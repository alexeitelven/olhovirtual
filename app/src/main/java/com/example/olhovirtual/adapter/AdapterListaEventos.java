package com.example.olhovirtual.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.olhovirtual.R;
import com.example.olhovirtual.activity.ListaEventoActivity;
import com.example.olhovirtual.model.Evento;

import java.util.List;

public class AdapterListaEventos extends RecyclerView.Adapter<AdapterListaEventos.MyViewHolder>{

    private List<Evento> listaEvento;
    private Context context;

    public AdapterListaEventos(List<Evento> l, Context c) {
        this.listaEvento = l;
        this.context = c;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_lista_eventos,parent,false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            Evento evento = listaEvento.get(position);
            holder.tituloEvento.setText(evento.getNomeEvento());
            holder.descricao.setText(evento.getDescricao());
            //holder.imageEvento.setImageResource(R.drawable.logo4);


            Uri uri = Uri.parse( evento.getUrlImagem() );
            if (uri != null){
                Glide.with(context).load(uri)
                .into(holder.imageEvento);
            }else{
                holder.imageEvento.setImageResource(R.drawable.logo4);
            }
    }

    @Override
    public int getItemCount() {
        return listaEvento.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView imageEvento;
        ImageView bordaLayout;
        TextView tituloEvento;
        TextView descricao;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageEvento = itemView.findViewById(R.id.imageAdEvento);
            tituloEvento = itemView.findViewById(R.id.textAdTitulo);
            descricao = itemView.findViewById(R.id.textAdDescricao);
            //bordaLayout = itemView.findViewById(R.id.bordaLayout);

        }
    }

}
