package com.jmanuel.mikroficha;
/*
String ssid = result.SSID; // Nombre de la red Wi-Fi (SSID)
        String bssid = result.BSSID; // Dirección MAC del punto de acceso
        int signalStrength = result.level; // Nivel de potencia de la señal en dBm
        int frequency = result.frequency; // Frecuencia de la señal en MHz
        String capabilities = result.capabilities; // Características de seguridad de la red Wi-Fi

*/
public class WifiData {

    private String ssid;
    private String bssid;
    private int potencia;
    private int Frecuencia;
    private String ip;

    public WifiData(String ssid, String bssid, int potencia, int frecuencia, String ip) {
        this.ssid = ssid;
        this.bssid = bssid;
        this.potencia = potencia;
        this.Frecuencia = frecuencia;
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public int getPotencia() {
        return potencia;
    }

    public void setPotencia(int potencia) {
        this.potencia = potencia;
    }

    public int getFrecuencia() {
        return Frecuencia;
    }

    public void setFrecuencia(int frecuencia) {
        Frecuencia = frecuencia;
    }
}
