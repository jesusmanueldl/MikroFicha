package com.jmanuel.mikroficha;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlanesAdapter extends RecyclerView.Adapter<PlanesAdapter.ViewHolder>{

    private List<PlanesMk> listaPlanes;
    private LayoutInflater inflaterlayout;
    private Context context;
    final PlanesAdapter.OnItemClickListener listener;

    public interface OnItemClickListener{
        void OnItemClick(PlanesMk item);
        void OnItemClickMenu(PlanesMk item, View view);
    }

    public PlanesAdapter(List<PlanesMk> listaRouters, Context context, PlanesAdapter.OnItemClickListener listener) {
        this.listaPlanes = listaRouters;
        this.inflaterlayout = LayoutInflater.from(context);
        this.context = context;
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return listaPlanes.size();
    }

    @Override
    public PlanesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewTipe){
        View view = inflaterlayout.inflate(R.layout.recycler_planes_desing, null);
        return new PlanesAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PlanesAdapter.ViewHolder holder, final int position){
        holder.bindData(listaPlanes.get(position));
    }

    public void setItems(List<PlanesMk> items){
        listaPlanes = items;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView nombre;
        private TextView costo;
        private TextView numerosDeUsuarios;
        private TextView velocidad;
        private TextView duracionFicha;
        private TextView idMikro;
        private ImageView opciones;


        ViewHolder(View itenView){
            super(itenView);

            nombre = itenView.findViewById(R.id.txv_nombre_plan);
            costo = itenView.findViewById(R.id.txv_costo_plan);
            numerosDeUsuarios = itenView.findViewById(R.id.txv_numero_user_plan);
            velocidad = itenView.findViewById(R.id.txv_velocidad_plan);
            duracionFicha = itenView.findViewById(R.id.txv_tiempo_plan);
            idMikro = itenView.findViewById(R.id.txv_mk_id);;
            opciones = itenView.findViewById(R.id.pop_menu_plan);

        }

        void bindData(final PlanesMk item){
            nombre.setText(item.getNombre());
            costo.setText(item.getCosto());
            numerosDeUsuarios.setText(item.getNumerosDeUsuarios());
            velocidad.setText(item.getVelocidad());
            duracionFicha.setText(item.getDuracionFicha());
            idMikro.setText(item.getIdMikro());

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
