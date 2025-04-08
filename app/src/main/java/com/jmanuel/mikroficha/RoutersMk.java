package com.jmanuel.mikroficha;

import java.io.Serializable;

public class RoutersMk implements Serializable {
    private String nombre;
    private String mac;
    private String ip;
    private String puerto;
    private String admin;
    private String version;
    private String contrasenia;

    public RoutersMk(String nombre, String mac, String ip,String puerto, String admin, String version, String contrasenia) {
        this.nombre = nombre;
        this.mac = mac;
        this.ip = ip;
        this.puerto = puerto;
        this.admin = admin;
        this.version = version;
        this.contrasenia = contrasenia;
    }

    public String getPuerto() {
        return puerto;
    }

    public void setPuerto(String puerto) {
        this.puerto = puerto;
    }

    public String getContrasenia() {
        return contrasenia;
    }

    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
