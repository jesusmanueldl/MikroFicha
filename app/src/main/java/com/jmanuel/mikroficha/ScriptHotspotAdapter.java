package com.jmanuel.mikroficha;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScriptHotspotAdapter extends RecyclerView.Adapter<ScriptHotspotAdapter.ViewHolder>{

    private List<ScriptHotspot> listaScript;
    private LayoutInflater inflaterlayout;
    private Context context;
    final ScriptHotspotAdapter.OnItemClickListener listener;

    public interface OnItemClickListener{
        void OnItemClick(ScriptHotspot item);
        void OnItemClickMenu(ScriptHotspot item, View view);
    }

    public ScriptHotspotAdapter(List<ScriptHotspot> listaScript, Context context, ScriptHotspotAdapter.OnItemClickListener listener) {
        this.listaScript = listaScript;
        this.inflaterlayout = LayoutInflater.from(context);
        this.context = context;
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return listaScript.size();
    }

    @Override
    public ScriptHotspotAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewTipe){
        View view = inflaterlayout.inflate(R.layout.recycler_script_hotspot_desing, null);
        return new ScriptHotspotAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ScriptHotspotAdapter.ViewHolder holder, final int position){
        holder.bindData(listaScript.get(position));
    }

    public void setItems(List<ScriptHotspot> items){
        listaScript = items;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView titulo;
        private TextView info;
        private TextView cmd;
        private TextView subs;
        private ImageView opciones;


        ViewHolder(View itenView){
            super(itenView);

            titulo = itenView.findViewById(R.id.titulo_script_router);
            info = itenView.findViewById(R.id.info_script_router);
            cmd = itenView.findViewById(R.id.cmd_script_router);
            subs = itenView.findViewById(R.id.suscripcion_script_router);
            opciones = itenView.findViewById(R.id.pop_menu_script_router);

        }

        void bindData(final ScriptHotspot item){
            titulo.setText(item.getTitulo());
            info.setText(item.getInfo());
            cmd.setText(item.getScript_cmd());
            if(item.getSubs().toString() == "true")
                subs.setText("Requiere Suscripción: SI");
            else
                subs.setText("Requiere Suscripción: NO");


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
