package com.jmanuel.mikroficha;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UsuariosActivoAdapter extends RecyclerView.Adapter<UsuariosActivoAdapter.ViewHolder>{

    private List<UsuarioActivoMk> listaUsuariosActivos;
    private LayoutInflater inflaterlayout;
    private Context context;
    final UsuariosActivoAdapter.OnItemClickListener listener;

    public interface OnItemClickListener{
        void OnItemClick(UsuarioActivoMk item);
        void OnItemClickMenu(UsuarioActivoMk item, View view);
    }

    public UsuariosActivoAdapter(List<UsuarioActivoMk> listaUsuariosActivos, Context context, UsuariosActivoAdapter.OnItemClickListener listener) {
        this.listaUsuariosActivos = listaUsuariosActivos;
        this.inflaterlayout = LayoutInflater.from(context);
        this.context = context;
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return listaUsuariosActivos.size();
    }

    @Override
    public UsuariosActivoAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewTipe){
        View view = inflaterlayout.inflate(R.layout.recycler_usuario_activo_desing, null);
        return new UsuariosActivoAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final UsuariosActivoAdapter.ViewHolder holder, final int position){
        holder.bindData(listaUsuariosActivos.get(position));
    }

    public void setItems(List<UsuarioActivoMk> items){
        listaUsuariosActivos = items;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView nombre;
        private TextView servidor;
        private TextView ipMac;
        private TextView uptime;
        private ImageView opciones;


        ViewHolder(View itenView){
            super(itenView);

            nombre = itenView.findViewById(R.id.txv_nombre_usuario_activo);
            servidor = itenView.findViewById(R.id.txv_servidor_usuario_activo_ad);
            ipMac = itenView.findViewById(R.id.txv_ip_mac_usuario_activo);
            uptime = itenView.findViewById(R.id.txv_uptime_usuario_activo);
            opciones = itenView.findViewById(R.id.pop_menu_usuario_activo);

        }

        void bindData(final UsuarioActivoMk item){
            nombre.setText(item.getNombre());
            servidor.setText(item.getServidor());
            ipMac.setText(item.getIpMac());
            uptime.setText(item.getUptime());


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
