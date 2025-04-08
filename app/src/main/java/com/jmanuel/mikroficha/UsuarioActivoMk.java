package com.jmanuel.mikroficha;

import java.io.Serializable;

public class UsuarioActivoMk implements Serializable {

    private String nombre;
    private String servidor;
    private String ipMac;
    private String uptime;
    private String idus;

    public UsuarioActivoMk(String nombre, String servidor, String ipMac, String uptime, String idus) {
        this.nombre = nombre;
        this.servidor = servidor;
        this.ipMac = ipMac;
        this.uptime = uptime;
        this.idus = idus;
    }

    public String getIdus() {
        return idus;
    }

    public void setIdus(String idus) {
        this.idus = idus;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getServidor() {
        return servidor;
    }

    public void setServidor(String servidor) {
        this.servidor = servidor;
    }

    public String getIpMac() {
        return ipMac;
    }

    public void setIpMac(String ipMac) {
        this.ipMac = ipMac;
    }

    public String getUptime() {
        return uptime;
    }

    public void setUptime(String uptime) {
        this.uptime = uptime;
    }
}
