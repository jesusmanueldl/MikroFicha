package com.jmanuel.mikroficha;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RoutersAdapter extends RecyclerView.Adapter<RoutersAdapter.ViewHolder>{

    private List<RoutersMk> listaRouters;
    private LayoutInflater inflaterlayout;
    private Context context;
    final RoutersAdapter.OnItemClickListener listener;

    public interface OnItemClickListener{
        void OnItemClick(RoutersMk item);
        void OnItemClickMenu(RoutersMk item, View view);
    }

    public RoutersAdapter(List<RoutersMk> listaRouters, Context context, RoutersAdapter.OnItemClickListener listener) {
        this.listaRouters = listaRouters;
        this.inflaterlayout = LayoutInflater.from(context);
        this.context = context;
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return listaRouters.size();
    }

    @Override
    public RoutersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewTipe){
        View view = inflaterlayout.inflate(R.layout.recycler_router_desing, null);
        return new RoutersAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RoutersAdapter.ViewHolder holder, final int position){
        holder.bindData(listaRouters.get(position));
    }

    public void setItems(List<RoutersMk> items){
        listaRouters = items;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView nombre;
        private TextView mac;
        private TextView ip;
        private TextView admin;
        private TextView version;
        private TextView contrasenia;
        private ImageView opciones;


        ViewHolder(View itenView){
            super(itenView);

            nombre = itenView.findViewById(R.id.txv_titulo_mk);
            mac = itenView.findViewById(R.id.txv_serie_mk);
            ip = itenView.findViewById(R.id.txv_ip_mk);
            admin = itenView.findViewById(R.id.tv_admin_mk);
            version = itenView.findViewById(R.id.txv_version_mk);
            contrasenia = itenView.findViewById(R.id.txv_contrasenia_mk);
            opciones = itenView.findViewById(R.id.pop_menu);
        }

        void bindData(final RoutersMk item){
            nombre.setText(item.getNombre());
            mac.setText(item.getMac());
            ip.setText(item.getIp());
            admin.setText(item.getAdmin());
            version.setText(item.getVersion());
            contrasenia.setText(item.getContrasenia());

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
