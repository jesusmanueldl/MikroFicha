package com.jmanuel.mikroficha;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BtScanAdapter extends RecyclerView.Adapter<BtScanAdapter.ViewHolder>{

    private List<BtData> listaBt;
    private LayoutInflater inflaterlayout;
    private Context context;
    final BtScanAdapter.OnItemClickListener listener;

    public interface OnItemClickListener{
        void OnItemClick(BtData item);
        void OnItemClickMenu(BtData item, View view);
    }

    public BtScanAdapter(List<BtData> listaBt, Context context, BtScanAdapter.OnItemClickListener listener) {
        this.listaBt = listaBt;
        this.inflaterlayout = LayoutInflater.from(context);
        this.context = context;
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return listaBt.size();
    }

    @Override
    public BtScanAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewTipe){
        View view = inflaterlayout.inflate(R.layout.recycler_bt_scan_desing, null);
        return new BtScanAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final BtScanAdapter.ViewHolder holder, final int position){
        holder.bindData(listaBt.get(position));
    }

    public void setItems(List<BtData> items){
        listaBt = items;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView namebt;
        private TextView macbt;
        private ImageView opciones;

        ViewHolder(View itenView){
            super(itenView);

            namebt = itenView.findViewById(R.id.nombre_bt);
            macbt = itenView.findViewById(R.id.mac_bt);
            opciones = itenView.findViewById(R.id.pop_menu);
        }

        void bindData(final BtData item){
            namebt.setText(""+item.getNombre_bt());
            macbt.setText(""+item.getMac_bt());

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
