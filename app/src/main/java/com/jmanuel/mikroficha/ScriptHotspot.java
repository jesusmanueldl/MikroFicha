package com.jmanuel.mikroficha;

import java.io.Serializable;
import java.util.List;

public class ScriptHotspot implements Serializable {

    private String info;
    private String script_cmd;
    private Boolean subs;
    private String titulo;

    public ScriptHotspot(String info, String script_cmd, Boolean subs, String titulo) {
        this.info = info;
        this.script_cmd = script_cmd;
        this.subs = subs;
        this.titulo = titulo;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getScript_cmd() {
        return script_cmd;
    }

    public void setScript_cmd(String script_cmd) {
        this.script_cmd = script_cmd;
    }

    public Boolean getSubs() {
        return subs;
    }

    public void setSubs(Boolean subs) {
        this.subs = subs;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
}
