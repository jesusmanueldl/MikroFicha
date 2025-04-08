package com.jmanuel.mikroficha;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FichaAdapter extends RecyclerView.Adapter<FichaAdapter.ViewHolder>{

    private List<Ficha> listaFicha;
    private LayoutInflater inflaterlayout;
    private Context context;
    final FichaAdapter.OnItemClickListener listener;

    public interface OnItemClickListener{
        void OnItemClick(Ficha item);
        void OnItemClickMenu(Ficha item, View view);
    }

    public FichaAdapter(List<Ficha> listaFicha, Context context, FichaAdapter.OnItemClickListener listener) {
        this.listaFicha = listaFicha;
        this.inflaterlayout = LayoutInflater.from(context);
        this.context = context;
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return listaFicha.size();
    }

    @Override
    public FichaAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewTipe){
        View view = inflaterlayout.inflate(R.layout.recycler_ficha_desing, null);
        return new FichaAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final FichaAdapter.ViewHolder holder, final int position){
        holder.bindData(listaFicha.get(position));
    }

    public void setItems(List<Ficha> items){
        listaFicha = items;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView nombre;
        private TextView url;
        private ImageView opciones;


        ViewHolder(View itenView){
            super(itenView);

            nombre = itenView.findViewById(R.id.nombre_ficha);
            url = itenView.findViewById(R.id.url_ficha);
            opciones = itenView.findViewById(R.id.pop_menu_ficha);

        }

        void bindData(final Ficha item){
            nombre.setText(item.getNombre());
            url.setText(item.getUrl());


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.OnItemClick(item);
                }
            });

            opciones.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.OnItemClickMenu(item, view);
                }
            });

        }

        
    }

}
