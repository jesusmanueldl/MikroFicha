package com.jmanuel.mikroficha;

public class BtData {

    private String nombre_bt;
    private String mac_bt;

    public BtData(String nombre_bt, String mac_bt) {
        this.nombre_bt = nombre_bt;
        this.mac_bt = mac_bt;
    }

    public String getNombre_bt() {
        return nombre_bt;
    }

    public void setNombre_bt(String nombre_bt) {
        this.nombre_bt = nombre_bt;
    }

    public String getMac_bt() {
        return mac_bt;
    }

    public void setMac_bt(String mac_bt) {
        this.mac_bt = mac_bt;
    }
}
