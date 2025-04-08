
// Versión actualizada de activity_chat.java con carga del archivo mikrobotconocimeinto.txt desde Firebase Storage

package com.jmanuel.mikroficha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class activity_chat extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private Button sendButton;
    private ChatAdapter adapter;
    private List<ChatMessage> messages;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private String knowledgeBaseText = "";

    private static String API_KEY = "";
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String BOT_NAME = "MikroBot";
    private String systemPrompt;

    FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        messages = new ArrayList<>();
        adapter = new ChatAdapter(messages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(adapter);

        displayWelcomeMessage();

        Map<String, Object> defaults = new HashMap<>();
        defaults.put("chatgpt_api_key", "SIN_CLAVE");
        defaults.put("chatgpt_model", "gpt-3.5-turbo");
        defaults.put("chatgpt_temperature", 0.7);
        defaults.put("chatgpt_max_tokens", 500);
        mFirebaseRemoteConfig.setDefaultsAsync(defaults);

        mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                API_KEY = mFirebaseRemoteConfig.getString("chatgpt_api_key");
                loadKnowledgeBaseFromFirebase(() -> {
                    sendButton.setOnClickListener(v -> sendMessage(API_KEY));
                });
            } else {
                displayBotMessage("Error al obtener clave API.");
            }
        });
    }

    private void loadKnowledgeBaseFromFirebase1(Runnable onFinishCallback) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Cargando conocimiento");
        progressDialog.setMessage("Espere un momento...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("conocimiento/mikrobotconocimeinto.txt");

        storageRef.getBytes(1024 * 1024).addOnSuccessListener(bytes -> {
            knowledgeBaseText = new String(bytes);
            progressDialog.dismiss();
            onFinishCallback.run();
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            displayBotMessage("⚠ No se pudo cargar el conocimiento desde Firebase." + storageRef.toString());
        });
    }

    private void loadKnowledgeBaseFromFirebase(Runnable onFinishCallback) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Cargando conocimiento");
        progressDialog.setMessage("Espere un momento...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("conocimiento/mikrobotconocimeinto.txt");

        storageRef.getBytes(1024 * 1024).addOnSuccessListener(bytes -> {
            knowledgeBaseText = new String(bytes);
            progressDialog.setMessage("Cargando directiva...");

            DatabaseReference promptRef = FirebaseDatabase.getInstance().getReference()
                    .child("PROMPT").child("DIRECTIVA1").child("MIKROBOT_SYSPROMPT");

            promptRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String promptBase = snapshot.getValue(String.class);
                        systemPrompt = promptBase + "\n\nBase de conocimiento:\n" + knowledgeBaseText;
                    } else {
                        systemPrompt = "⚠ No se encontró el prompt en la base de datos.\n\n" + knowledgeBaseText;
                    }
                    progressDialog.dismiss();
                    onFinishCallback.run();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    systemPrompt = "⚠ Error al cargar prompt: " + error.getMessage() + "\n\n" + knowledgeBaseText;
                    progressDialog.dismiss();
                    onFinishCallback.run();
                }
            });

        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            displayBotMessage("⚠ No se pudo cargar el conocimiento desde Firebase.");
        });
    }


    private void sendMessage(String API_KEY) {
        String userMessage = messageInput.getText().toString().trim();
        if (userMessage.isEmpty()) return;

        messages.add(new ChatMessage(userMessage, true));
        adapter.notifyItemInserted(messages.size() - 1);
        messageInput.setText("");

        executorService.execute(() -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(API_URL).openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setDoOutput(true);

                JSONArray messagesArray = new JSONArray();
                /*String systemPrompt = "Eres MikroBot, un asistente especializado exclusivamente en responder dudas sobre la aplicación MikroFicha. Solo debes usar la información proporcionada a continuación como tu única base de conocimiento. \n\n" +
                        "Si el usuario te dice 'hola', 'gracias', 'sí', 'ok', o algo similar, puedes responder de forma amable o continuar la conversación con sugerencias sobre lo que puedes hacer, agrega emojis para hacer la platicas más amena, además quiero que recuerdes los cinco últimos mensajes que has dado, para un mejor contexto con el usuario.\n\n" +
                        "Si la pregunta del usuario no está relacionada con la información proporcionada, responde con: 'Lo siento, esa información no está en mi base de conocimiento, pero puedo ayudarte con todo lo relacionado a MikroFicha. Puedes preguntarme, por ejemplo: ¿cómo generar fichas?, ¿qué planes existen?, ¿cómo se imprime?, ¿cuáles son los métodos de pago?, ¿qué es MikroFicha?,'\n\n" +
                        "Base de conocimiento:\n" + knowledgeBaseText;*/

                messagesArray.put(new JSONObject().put("role", "system").put("content", systemPrompt));
                messagesArray.put(new JSONObject().put("role", "user").put("content", userMessage));

                String model = mFirebaseRemoteConfig.getString("chatgpt_model");
                double temperature = mFirebaseRemoteConfig.getDouble("chatgpt_temperature");
                int maxTokens = (int) mFirebaseRemoteConfig.getLong("chatgpt_max_tokens");

                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put("model", model);
                jsonRequest.put("messages", messagesArray);
                jsonRequest.put("temperature", temperature);
                jsonRequest.put("max_tokens", maxTokens);

                OutputStream os = connection.getOutputStream();
                os.write(jsonRequest.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    Scanner scanner = new Scanner(inputStream);
                    StringBuilder response = new StringBuilder();
                    while (scanner.hasNext()) {
                        response.append(scanner.nextLine());
                    }
                    scanner.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONArray choices = jsonResponse.getJSONArray("choices");
                    String botResponse = choices.getJSONObject(0).getJSONObject("message").getString("content");

                    runOnUiThread(() -> {
                        messages.add(new ChatMessage("\uD83E\uDD16 : " + botResponse, false));
                        adapter.notifyItemInserted(messages.size() - 1);
                        chatRecyclerView.scrollToPosition(messages.size() - 1);
                    });
                } else {
                    displayBotMessage("Error al contactar al asistente.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                displayBotMessage("Ocurrió un error interno.");
            }
        });
    }

    private void displayWelcomeMessage() {
        String welcomeMessage = "🤖 ¡Hola! Soy MikroBot, tu asistente inteligente de la app MikroFicha.\n\n" +
                "Estoy aquí para ayudarte con todo lo que necesites sobre la app.\n\n" +
                "Puedes preguntarme cosas como:\n" +
                "• ¿Cómo generar fichas?\n" +
                "• ¿Qué planes de suscripción existen?\n" +
                "• ¿Cómo conecto MikroFicha a mi router MikroTik?\n" +
                "• ¿Cómo imprimir las fichas?\n" +
                "• ¿Qué métodos de pago hay?\n\n" +
                "✨ ¡Estoy listo para ayudarte!";

        messages.add(new ChatMessage(welcomeMessage, false));  // false = mensaje del bot
        adapter.notifyItemInserted(messages.size() - 1);
        chatRecyclerView.scrollToPosition(messages.size() - 1);
    }


    private void displayBotMessage(String message) {
        runOnUiThread(() -> {
            messages.add(new ChatMessage("\uD83E\uDD16: " + message, false));
            adapter.notifyItemInserted(messages.size() - 1);
            chatRecyclerView.smoothScrollToPosition(messages.size() - 1);
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(activity_chat.this, MainActivity.class));
        finish();
    }
}
