package com.jmanuel.mikroficha;

import java.io.Serializable;

public class PlanesMk implements Serializable {
    private String nombre;
    private String costo;
    private String numerosDeUsuarios;
    private String velocidad;
    private String duracionFicha;
    private String idMikro;

    public PlanesMk(String nombre, String costo, String numerosDeUsuarios, String velocidad, String duracionFicha, String idMikro) {
        this.nombre = nombre;
        this.costo = costo;
        this.numerosDeUsuarios = numerosDeUsuarios;
        this.velocidad = velocidad;
        this.duracionFicha = duracionFicha;
        this.idMikro = idMikro;
    }

    public String getIdMikro() {
        return idMikro;
    }

    public void setIdMikro(String idMikro) {
        this.idMikro = idMikro;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCosto() {
        return costo;
    }

    public void setCosto(String costo) {
        this.costo = costo;
    }

    public String getNumerosDeUsuarios() {
        return numerosDeUsuarios;
    }

    public void setNumerosDeUsuarios(String numerosDeUsuarios) {
        this.numerosDeUsuarios = numerosDeUsuarios;
    }

    public String getVelocidad() {
        return velocidad;
    }

    public void setVelocidad(String velocidad) {
        this.velocidad = velocidad;
    }

    public String getDuracionFicha() {
        return duracionFicha;
    }

    public void setDuracionFicha(String duracionFicha) {
        this.duracionFicha = duracionFicha;
    }

}
