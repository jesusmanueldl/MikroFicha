package com.jmanuel.mikroficha;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WifiScanAdapter extends RecyclerView.Adapter<WifiScanAdapter.ViewHolder>{

    private List<WifiData> listaWifi;
    private LayoutInflater inflaterlayout;
    private Context context;
    final WifiScanAdapter.OnItemClickListener listener;

    public interface OnItemClickListener{
        void OnItemClick(WifiData item);
        void OnItemClickMenu(WifiData item, View view);
    }

    public WifiScanAdapter(List<WifiData> listaWifi, Context context, WifiScanAdapter.OnItemClickListener listener) {
        this.listaWifi = listaWifi;
        this.inflaterlayout = LayoutInflater.from(context);
        this.context = context;
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return listaWifi.size();
    }

    @Override
    public WifiScanAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewTipe){
        View view = inflaterlayout.inflate(R.layout.recycler_wifiscan_desing, null);
        return new WifiScanAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final WifiScanAdapter.ViewHolder holder, final int position){
        holder.bindData(listaWifi.get(position));
    }

    public void setItems(List<WifiData> items){
        listaWifi = items;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView ssid;
        private TextView bssid;
        private TextView potencia;
        private TextView frecuencia;
        private TextView ip;
        //private ImageView opciones;


        ViewHolder(View itenView){
            super(itenView);

            ssid = itenView.findViewById(R.id.wifi_ssid);
            bssid = itenView.findViewById(R.id.wifi_bssid);
            potencia = itenView.findViewById(R.id.wifi_potencia);
            frecuencia = itenView.findViewById(R.id.wifi_frecuencia);
            ip = itenView.findViewById(R.id.wifi_ip);
            //opciones = itenView.findViewById(R.id.pop_menu);
        }

        void bindData(final WifiData item){
            ssid.setText("SSID: "+item.getSsid());
            bssid.setText("MAC: "+item.getBssid());
            potencia.setText(item.getPotencia()+"dBm");
            frecuencia.setText(item.getFrecuencia()+"MHz");
            ip.setText("IP: "+item.getIp());
/*
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.OnItemClick(item);
                }
            });*/

           /* opciones.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.OnItemClickMenu(item, view);
                }
            });*/

        }

        
    }

}
