package com.jmanuel.mikroficha;

import java.io.Serializable;

public class IPBindingMk implements Serializable {

    private String ip_binding;
    private String servidor;
    private String mac;
    private String type;
    private String estado;
    private String id_binding;
    private String comentario;
    private String to_address;

    public IPBindingMk(String ip_binding, String servidor, String mac, String type, String estado, String id_binding, String comentario, String to_address) {
        this.ip_binding = ip_binding;
        this.servidor = servidor;
        this.mac = mac;
        this.type = type;
        this.estado = estado;
        this.id_binding = id_binding;
        this.comentario = comentario;
        this.to_address = to_address;
    }

    public String getIp_binding() {
        return ip_binding;
    }

    public void setIp_binding(String ip_binding) {
        this.ip_binding = ip_binding;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public String getTo_address() {
        return to_address;
    }

    public void setTo_address(String to_address) {
        this.to_address = to_address;
    }

    public String getServidor() {
        return servidor;
    }

    public void setServidor(String servidor) {
        this.servidor = servidor;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getId_binding() {
        return id_binding;
    }

    public void setId_binding(String id_binding) {
        this.id_binding = id_binding;
    }
}
