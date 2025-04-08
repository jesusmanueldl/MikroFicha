package com.jmanuel.mikroficha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TerConPoli extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private TextView tercon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ter_con_poli);

        tercon = findViewById(R.id.tercon);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.child("TERCON").child("POLITICA").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                    tercon.setText(Html.fromHtml(snapshot.getValue().toString(), Html.FROM_HTML_MODE_LEGACY));
                }
                else{
                    tercon.setText(Html.fromHtml(snapshot.getValue().toString()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(TerConPoli.this, MainActivity.class));
        finish();
    }
}