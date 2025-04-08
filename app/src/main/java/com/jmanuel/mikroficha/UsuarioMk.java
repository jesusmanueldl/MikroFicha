package com.jmanuel.mikroficha;

import java.io.Serializable;

public class UsuarioMk implements Serializable {

    private String nombre;
    private String servidor;
    private String perfil;
    private String uptime;
    private String comentario;
    private String contrasenia;
    private String idus;

    public UsuarioMk(String nombre, String servidor, String perfil, String uptime, String comentario, String contrasenia, String idus) {
        this.nombre = nombre;
        this.servidor = servidor;
        this.perfil = perfil;
        this.uptime = uptime;
        this.comentario = comentario;
        this.contrasenia = contrasenia;
        this.idus = idus;
    }

    public String getContrasenia() {
        return contrasenia;
    }

    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
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

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public String getUptime() {
        return uptime;
    }

    public void setUptime(String uptime) {
        this.uptime = uptime;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
