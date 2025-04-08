package com.jmanuel.mikroficha;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UsuariosAdapter extends RecyclerView.Adapter<UsuariosAdapter.ViewHolder>{

    private List<UsuarioMk> listaUsuarios;
    private LayoutInflater inflaterlayout;
    private Context context;
    final UsuariosAdapter.OnItemClickListener listener;

    public interface OnItemClickListener{
        void OnItemClick(UsuarioMk item);
        void OnItemClickMenu(UsuarioMk item, View view);
    }

    public UsuariosAdapter(List<UsuarioMk> listaUsuarios, Context context, UsuariosAdapter.OnItemClickListener listener) {
        this.listaUsuarios = listaUsuarios;
        this.inflaterlayout = LayoutInflater.from(context);
        this.context = context;
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }

    @Override
    public UsuariosAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewTipe){
        View view = inflaterlayout.inflate(R.layout.recycler_usuario_desing, null);
        return new UsuariosAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final UsuariosAdapter.ViewHolder holder, final int position){
        holder.bindData(listaUsuarios.get(position));
    }

    public void setItems(List<UsuarioMk> items){
        listaUsuarios = items;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView nombre;
        private TextView servidor;
        private TextView perfil;
        private TextView uptime;
        private TextView comentario;
        private TextView contrasenia;
        private ImageView opciones;


        ViewHolder(View itenView){
            super(itenView);

            nombre = itenView.findViewById(R.id.txv_nombre_usuario);
            servidor = itenView.findViewById(R.id.txv_servidor_usuario);
            perfil = itenView.findViewById(R.id.txv_perfil_usuario);
            uptime = itenView.findViewById(R.id.txv_uptime_usuario);
            comentario = itenView.findViewById(R.id.txv_comentario_usuario);
            contrasenia = itenView.findViewById(R.id.txv_contrasenia_usuario);
            opciones = itenView.findViewById(R.id.pop_menu_usuario);

        }

        void bindData(final UsuarioMk item){
            nombre.setText(item.getNombre());
            servidor.setText(item.getServidor());
            perfil.setText(item.getPerfil());
            uptime.setText(item.getUptime());
            comentario.setText(item.getComentario());
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
