package com.jmanuel.mikroficha;

import java.io.Serializable;

public class Ficha implements Serializable {

    private String nombre;
    private String url;

    public Ficha(String nombre, String url) {
        this.nombre = nombre;
        this.url = url;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
