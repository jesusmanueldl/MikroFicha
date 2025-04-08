package com.jmanuel.mikroficha;

import static androidx.core.content.FileProvider.getUriForFile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class FichasPDF extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener{

    private FichaAdapter fichaAdapter;
    private List<Ficha> fichas;
    private Ficha fichaActual;
    String version="", ip="", admin="", pass="", puerto ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fichas_pdf);

        Bundle item = getIntent().getExtras();
        ip = item.getString("ip");
        puerto = item.getString("puerto");
        admin = item.getString("admin");
        pass = item.getString("pass");
        version = item.getString("version");

        fichas = new ArrayList<>();
        fichaAdapter = new FichaAdapter(fichas, this, new FichaAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(Ficha item) {
                moveToDescription(item);
            }
            @Override
            public void OnItemClickMenu(Ficha item, View view) {
                menuOptionItem(item, view);
            }
        });
        RecyclerView mrecyclerview = findViewById(R.id.recycler_ficha);

        mrecyclerview.setHasFixedSize(true);
        mrecyclerview.setLayoutManager(new LinearLayoutManager(this));
        mrecyclerview.setAdapter(fichaAdapter);

        String files[] = FichasPDF.this.fileList();
        for(int i = 0; i < files.length; i++){
            if(files[i].contains("MikroFicha"))
                fichas.add(new Ficha(files[i],FichasPDF.this.getFilesDir()+""));
        }
        fichaAdapter.notifyDataSetChanged();

    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(FichasPDF.this, Admin.class);
        i.putExtra("ip", ip);
        i.putExtra("puerto", puerto);
        i.putExtra("admin", admin);
        i.putExtra("pass", pass);
        i.putExtra("version", version);
        startActivity(i);
        finish();
    }

    private void moveToDescription(Ficha item) {

        File filepath = new File(FichasPDF.this.getFilesDir(),item.getNombre());
        Uri urlfile = FileProvider.getUriForFile(FichasPDF.this,getApplicationContext().getPackageName()+".provider",filepath);
        Intent target = new Intent(Intent.ACTION_VIEW);
        target.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION );
        target.setDataAndType(urlfile,"application/pdf");

        Intent c = Intent.createChooser(target, "Seleccione la aplicación");
        try {
           startActivity(c);

        } catch (ActivityNotFoundException e) {
            Toast.makeText(FichasPDF.this, "No tiene aplicación para leer PDF´s", Toast.LENGTH_LONG).show();
        }
    }

    private void menuOptionItem(Ficha item, View view) {
        showPopMenu(item, view);
    }

    public void showPopMenu(Ficha item,View view){
        PopupMenu popupmenu = new PopupMenu(this, view);
        popupmenu.setOnMenuItemClickListener(this);
        popupmenu.inflate(R.menu.menu_recycler_ficha);
        popupmenu.show();
        fichaActual = item;
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.imprimir_ficha:
                File filepathprint = new File(FichasPDF.this.getFilesDir(),fichaActual.getNombre());
                Uri urlfile = FileProvider.getUriForFile(FichasPDF.this,getApplicationContext().getPackageName()+".provider",filepathprint);
                Intent target = new Intent(Intent.ACTION_VIEW);
                target.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION );
                target.setDataAndType(urlfile,"application/pdf");

                Intent c = Intent.createChooser(target, "Seleccione la aplicación");
                try {
                    startActivity(c);

                } catch (ActivityNotFoundException e) {
                    Toast.makeText(FichasPDF.this, "No tiene aplicación para leer PDF´s", Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.eliminar_ficha:
                File filepathdelete = new File(FichasPDF.this.getFilesDir(),fichaActual.getNombre());
                if(filepathdelete.delete()){
                    Toast.makeText(FichasPDF.this, "Archivo eliminado", Toast.LENGTH_LONG).show();
                    fichas.remove(fichaActual);
                    fichaAdapter.notifyDataSetChanged();

                }

                return true;
            default:
                return false;
        }

    }
}