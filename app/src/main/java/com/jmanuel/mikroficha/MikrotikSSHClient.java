package com.jmanuel.mikroficha;

import android.util.Log;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MikrotikSSHClient {

    private String host;
    private int port;
    private String username;
    private String password;
    private Session session;

    public MikrotikSSHClient(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public void connect() throws JSchException {
        JSch jsch = new JSch();
        session = jsch.getSession(username, host, port);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
    }

    public void disconnect() {
        if (session != null) {
            session.disconnect();
        }
    }

    public List<String> executeCommand(String command) throws JSchException, IOException {
        List<String> result = new ArrayList<>();
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(channel.getInputStream()));
        channel.connect();
        String line;
        while ((line = reader.readLine()) != null) {
           if (line.startsWith("Flags:")) {
                continue;
            }
            if (line.contains("#")) {
                String[] encabezado = line.split("\\n+");
                Log.d("Wifix","Encabezado: "+encabezado.length);
                continue;
            }

            String[] columns = line.split("\\t+");
            Log.d("Wifix","cont: "+columns[0]);
            Log.d("Wifix","tam: "+columns[0].length());
            for (String column : columns) {
                //Log.d("Wifix","Line: "+column);
                String[] c = line.split("\\s+");
                //Log.d("Wifix","tamaño: "+c.length);
                //Log.d("Wifix","contenido: "+c[1]);
               /* String[] keyValue = column.split("=");
                if (keyValue.length == 2) {
                    row.put(keyValue[0], keyValue[1]);
                }*/
                result.add(column);
            }

        }
        channel.disconnect();
        return result;
    }
    public ConnectionStatus getConnectionStatus() {
        ConnectionStatus status = new ConnectionStatus();
        if (session != null && session.isConnected()) {
            status.success = true;
        } else {
            status.success = false;
            status.errorMessage = "No esposible conectarse a: " + host;
        }
        return status;
    }

    public static class ConnectionStatus {
        public boolean success;
        public String errorMessage;
    }
}
