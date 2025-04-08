package com.jmanuel.mikroficha;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class IPBindingAdapter extends RecyclerView.Adapter<IPBindingAdapter.ViewHolder>{

    private List<IPBindingMk> listaBinding;
    private LayoutInflater inflaterlayout;
    private Context context;
    final IPBindingAdapter.OnItemClickListener listener;

    public interface OnItemClickListener{
        void OnItemClick(IPBindingMk item);
        void OnItemClickMenu(IPBindingMk item, View view);
    }

    public IPBindingAdapter(List<IPBindingMk> listaBinding, Context context, IPBindingAdapter.OnItemClickListener listener) {
        this.listaBinding = listaBinding;
        this.inflaterlayout = LayoutInflater.from(context);
        this.context = context;
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return listaBinding.size();
    }

    @Override
    public IPBindingAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewTipe){
        View view = inflaterlayout.inflate(R.layout.recycler_ipbinding_desing, null);
        return new IPBindingAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final IPBindingAdapter.ViewHolder holder, final int position){
        holder.bindData(listaBinding.get(position));
    }

    public void setItems(List<IPBindingMk> items){
        listaBinding = items;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView mac;
        private TextView ip_binding;
        private TextView server;
        private TextView to_address;
        private TextView type;
        private TextView estado;
        private TextView comentario;
        private TextView id_biding;
        private ImageView opciones;
        private ImageView semaforo;


        ViewHolder(View itenView){
            super(itenView);

            mac = itenView.findViewById(R.id.txv_mac_binding);
            ip_binding = itenView.findViewById(R.id.txv_ip_binding);
            server = itenView.findViewById(R.id.txv_server_binding);
            to_address = itenView.findViewById(R.id.txv_to_binding);
            type = itenView.findViewById(R.id.txv_tipo_binding);
            estado = itenView.findViewById(R.id.txv_estado_binding);
            comentario = itenView.findViewById(R.id.txv_comentario_binding);
            id_biding = itenView.findViewById(R.id.txv_id_binding);
            opciones = itenView.findViewById(R.id.pop_menu_binding);
            semaforo = itenView.findViewById(R.id.imv_semaforo_binding);

        }

        void bindData(final IPBindingMk item){
            mac.setText(item.getMac());
            ip_binding.setText(item.getIp_binding());
            server.setText(item.getServidor());
            to_address.setText(item.getTo_address());
            type.setText(item.getType());
            estado.setText(item.getEstado());
            comentario.setText(item.getComentario());
            id_biding.setText(item.getId_binding());

            if(item.getEstado().equals("Disabled-false")){
                semaforo.setColorFilter(Color.parseColor("#388E3C"));
            }else{
                semaforo.setColorFilter(Color.parseColor("#900C3F"));
            }


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
