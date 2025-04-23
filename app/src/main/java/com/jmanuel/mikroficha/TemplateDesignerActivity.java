package com.jmanuel.mikroficha;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItem;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import me.legrange.mikrotik.ApiConnection;
import yuku.ambilwarna.AmbilWarnaDialog;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Map;
import android.Manifest;

import javax.net.SocketFactory;

public class TemplateDesignerActivity extends AppCompatActivity {

    private static final int REQUEST_SELECT_LOGO = 1;
    private boolean ADMOB = true;

    // Componentes de la UI
    private ImageView logoPreview;
    private Button selectLogoButton;
    private RadioGroup radioGroupBgType;
    private Button color1Picker, color2Picker;
    private EditText titleText, usernameHint, passwordHint, footerText;
    private Spinner spinnerOrientation;
    private WebView previewWebView;
    private Button exportButton;

    // Controles para las propiedades del logo
    private EditText logoWidthEdit, logoHeightEdit;
    // Control para el radio de borde del logo
    private SeekBar seekBarLogoBorderRadius;

    // Controles para personalización del formulario
    private EditText inputFontSizeEdit, inputPaddingEdit, inputMarginEdit;
    private Button formBgColorPicker;

    // Controles para el borde del formulario
    private SeekBar seekBarBorderWidth;
    private Button formBorderColorPicker;
    private Spinner spinnerBorderStyle;

    // Control para el desplazamiento vertical del formulario
    private EditText verticalOffsetEdit;

    // Nuevo: RadioGroup para ocultar o mostrar la contraseña
    private RadioGroup radioGroupHidePassword;

    // Modelo que almacena la configuración de la plantilla
    private Template currentTemplate = new Template();

    private String editedHtml = ""; // Variable para guardar el html modificado

    private boolean manualHtmlMode = false;
    private SharedPreferences prefences;
    private String uuid_app;
    private SharedPreferences router_file_pref;
    private DatabaseReference mDatabase;
    private AdView adview;
    private SharedPreferences.Editor editor;
    private static final int REQUEST_SELECT_BACKGROUND_IMAGE = 2;
    private Button selectBackgroundImageButton;

    private static final int STORAGE_PERMISSION_REQUEST_CODE = 100;

    private static final int REQUEST_PERMISSION_SETTINGS = 3;
    private static final long MAX_LOGO_SIZE = 300 * 1024; // 300KB en bytes
    private static final long MAX_BACKGROUND_SIZE = 500 * 1024; // 500KB en bytes


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template_designer);

        prefences = TemplateDesignerActivity.this.getSharedPreferences("clave_uuid_app", Context.MODE_PRIVATE);
        uuid_app = prefences.getString("uuid_app","N/A");
        ADMOB = prefences.getBoolean("ADMOB",true);
        router_file_pref = this.getSharedPreferences("router_file", Context.MODE_PRIVATE);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        adview = findViewById(R.id.adViewdisenio);

        mDatabase.child("UUID_APP").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.child(uuid_app).child("ADMOB").exists()) {
                        prefences = TemplateDesignerActivity.this.getSharedPreferences("clave_uuid_app", Context.MODE_PRIVATE);
                        editor = prefences.edit();
                        editor.putBoolean("ADMOB", (Boolean) snapshot.child(uuid_app).child("ADMOB").getValue());
                        editor.apply();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //ANUNCIO de video reward

        if(ADMOB) {
            adview.setVisibility(View.VISIBLE);
            MobileAds.initialize(this, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {

                }
            });
            AdRequest adRequest = new AdRequest.Builder().build();
            adview.loadAd(adRequest);
        }
        else {
            adview.setVisibility(View.GONE);
        }


        // Vincular componentes del layout
        logoPreview = findViewById(R.id.logoPreview);
        selectLogoButton = findViewById(R.id.selectLogoButton);
        color1Picker = findViewById(R.id.color1Picker);
        color2Picker = findViewById(R.id.color2Picker);
        titleText = findViewById(R.id.titleText);
        usernameHint = findViewById(R.id.usernameHint);
        passwordHint = findViewById(R.id.passwordHint);
        footerText = findViewById(R.id.footerText);
        spinnerOrientation = findViewById(R.id.spinnerOrientation);
        previewWebView = findViewById(R.id.previewWebView);
        exportButton = findViewById(R.id.exportButton);

        // Controles para las propiedades del logo
        logoWidthEdit = findViewById(R.id.logoWidthEdit);
        logoHeightEdit = findViewById(R.id.logoHeightEdit);
        seekBarLogoBorderRadius = findViewById(R.id.seekBarLogoBorderRadius);

        // Controles para personalización del formulario
        inputFontSizeEdit = findViewById(R.id.inputFontSizeEdit);
        inputPaddingEdit = findViewById(R.id.inputPaddingEdit);
        inputMarginEdit = findViewById(R.id.inputMarginEdit);
        formBgColorPicker = findViewById(R.id.formBgColorPicker);

        // Controles para el borde del formulario
        seekBarBorderWidth = findViewById(R.id.seekBarBorderWidth);
        formBorderColorPicker = findViewById(R.id.formBorderColorPicker);
        spinnerBorderStyle = findViewById(R.id.spinnerBorderStyle);

        // Control para el desplazamiento vertical del formulario
        verticalOffsetEdit = findViewById(R.id.verticalOffsetEdit);

        // Vincular el RadioGroup para ocultar la contraseña (defínelo en el XML con id radioGroupHidePassword)
        radioGroupHidePassword = findViewById(R.id.radioGroupHidePassword);
        radioGroupHidePassword.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioHideYes) {
                currentTemplate.setHidePassword(true);
            } else {
                currentTemplate.setHidePassword(false);
            }
            updatePreview();
        });

        // Listeners para las propiedades del logo
        logoWidthEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String width = logoWidthEdit.getText().toString().trim();
                if (!width.isEmpty()) {
                    // Si no se encuentra 'px' ni '%', se asume píxeles.
                    if (!width.matches(".*(px|%)$")) {
                        width = width + "px";
                    }
                    currentTemplate.setLogoWidth(width);
                    updatePreview();
                }
            }
        });
        logoHeightEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String height = logoHeightEdit.getText().toString().trim();
                if (!height.isEmpty()) {
                    // Si no se encuentra 'px' ni '%', se asume píxeles.
                    if (!height.matches(".*(px|%)$")) {
                        height = height + "px";
                    }
                    currentTemplate.setLogoWidth(height);
                    updatePreview();
                }
            }
        });
        seekBarLogoBorderRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentTemplate.setLogoBorderRadius(progress + "px");
                updatePreview();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        // Listeners para la personalización del formulario
        inputFontSizeEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String fontSize = inputFontSizeEdit.getText().toString().trim();
                if (!fontSize.isEmpty()) {
                    // Si no se encuentra 'px' ni '%', se asume píxeles.
                    if (!fontSize.matches(".*(px|%)$")) {
                        fontSize = fontSize + "px";
                    }
                    currentTemplate.setInputFontSize(fontSize);
                    updatePreview();
                }
            }
        });
        inputPaddingEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String padding = inputPaddingEdit.getText().toString().trim();
                if (!padding.isEmpty()) {
                    // Si no se encuentra 'px' ni '%', se asume píxeles.
                    if (!padding.matches(".*(px|%)$")) {
                        padding = padding + "px";
                    }
                    currentTemplate.setInputPadding(padding);
                    updatePreview();
                }
            }
        });
        inputMarginEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String margin = inputMarginEdit.getText().toString().trim();
                if (!margin.isEmpty()) {
                    // Si no se encuentra 'px' ni '%', se asume píxeles.
                    if (!margin.matches(".*(px|%)$")) {
                        margin = margin + "px";
                    }
                    currentTemplate.setInputMargin(margin);
                    updatePreview();
                }
            }
        });
        formBgColorPicker.setOnClickListener(v ->
                showAdvancedColorPicker("Color fondo formulario", currentTemplate.getFormBackgroundColor(), color -> {
                    currentTemplate.setFormBackgroundColor(String.format("#%06X", (0xFFFFFF & color)));
                    updatePreview();
                })
        );

        // Configurar el SeekBar para el borde del formulario
        seekBarBorderWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentTemplate.setFormBorderWidth(progress);
                updatePreview();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        formBorderColorPicker.setOnClickListener(v ->
                showAdvancedColorPicker("Color Borde Formulario", currentTemplate.getFormBorderColor(), color -> {
                    currentTemplate.setFormBorderColor(String.format("#%06X", (0xFFFFFF & color)));
                    updatePreview();
                })
        );
        String[] estilos = {"solid", "dashed", "dotted"};
        ArrayAdapter<String> styleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, estilos);
        styleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBorderStyle.setAdapter(styleAdapter);
        spinnerBorderStyle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentTemplate.setFormBorderStyle(estilos[position]);
                updatePreview();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // Listener para el desplazamiento vertical del formulario
        verticalOffsetEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String offset = verticalOffsetEdit.getText().toString().trim();
                if (!offset.isEmpty()) {
                    // Si no se encuentra 'px' ni '%', se asume píxeles.
                    if (!offset.matches(".*(px|%)$")) {
                        offset = offset + "px";
                    }
                    currentTemplate.setVerticalOffset(offset);
                    updatePreview();
                }
            }
        });

        // Configurar el WebView para que soporte JavaScript y evitar caché
        previewWebView.getSettings().setJavaScriptEnabled(true);
        previewWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        previewWebView.setWebViewClient(new WebViewClient());
        previewWebView.getSettings().setLoadWithOverviewMode(true);
        previewWebView.getSettings().setUseWideViewPort(true);
        previewWebView.setInitialScale(50);

        previewWebView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    // Pídele al padre que no intercepte el evento
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // Cuando sueltas el dedo, el padre puede volver a interceptar
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }
            return false; // Permite que la WebView maneje su propio scroll
        });



        color1Picker.setOnClickListener(v ->
                showAdvancedColorPicker("Color 1", currentTemplate.getBgColor1(), color -> {
                    currentTemplate.setBgColor1(String.format("#%06X", (0xFFFFFF & color)));
                    updatePreview();
                })
        );
        color2Picker.setOnClickListener(v ->
                showAdvancedColorPicker("Color 2", currentTemplate.getBgColor2(), color -> {
                    currentTemplate.setBgColor2(String.format("#%06X", (0xFFFFFF & color)));
                    updatePreview();
                })
        );

        // Configurar el Spinner para la orientación del degradado con opciones en español
        String[] opcionesOrientacion = {"a la derecha", "abajo", "a la izquierda", "arriba"};
        String[] orientacionCSS = {"to right", "to bottom", "to left", "to top"};
        ArrayAdapter<String> orientAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, opcionesOrientacion);
        orientAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrientation.setAdapter(orientAdapter);
        spinnerOrientation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentTemplate.setGradientOrientation(orientacionCSS[position]);
                updatePreview();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // Botón para seleccionar logo mediante la galería
        selectLogoButton.setOnClickListener(v -> selectLogo());

        // Listeners para los EditText de textos
        titleText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                if (manualHtmlMode) {  // Si el usuario está en modo manual, preguntar si se desea sincronizar
                    new AlertDialog.Builder(this)
                            .setTitle("Sincronizar HTML")
                            .setMessage("Se perderán los cambios manuales en el HTML y se regenerará a partir de las opciones. ¿Desea continuar?")
                            .setPositiveButton("Sí", (dialog, which) -> {
                                manualHtmlMode = false;
                                editedHtml = ""; // se descarta la edición manual actual
                                currentTemplate.setTitleText(titleText.getText().toString());
                                updatePreview();
                            })
                            .setNegativeButton("No", (dialog, which) -> {
                                // Opcional: restaurar el valor en el campo si se decide mantener el HTML manual
                                titleText.setText(currentTemplate.getTitleText());
                            })
                            .show();
                } else {
                    currentTemplate.setTitleText(titleText.getText().toString());
                    updatePreview();
                }
            }
        });

        usernameHint.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                currentTemplate.setUsernameHint(usernameHint.getText().toString());
                updatePreview();
            }
        });
        passwordHint.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                currentTemplate.setPasswordHint(passwordHint.getText().toString());
                updatePreview();
            }
        });
        footerText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                currentTemplate.setFooterText(footerText.getText().toString());
                updatePreview();
            }
        });
        Button syncHtmlButton = findViewById(R.id.syncHtmlButton);
        syncHtmlButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Sincronizar HTML")
                    .setMessage("Se perderán los cambios manuales. ¿Desea sincronizar y regenerar el HTML a partir de las opciones actuales?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        manualHtmlMode = false;
                        editedHtml = "";
                        updatePreview();
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        Button editHtmlButton = findViewById(R.id.editHtmlButton);
        editHtmlButton.setOnClickListener(v -> openEditHtmlModal());

        // Vincula el FloatingActionButton (FAB) para ver la vista previa modal
        FloatingActionButton fabPreview = findViewById(R.id.fabPreview);
        fabPreview.setOnClickListener(v -> openPreviewModal());

        // Vincular el SeekBar para ajustar el ancho del formulario
        SeekBar seekBarFormWidth = findViewById(R.id.seekBarFormWidth);
        seekBarFormWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Puedes definir un mínimo si lo deseas, por ejemplo, sumarle 200
                int newWidth = progress; // O: progress + 200;
                currentTemplate.setFormWidth(newWidth);
                updatePreview();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        RadioGroup radioGroupBackgroundType = findViewById(R.id.radioGroupBackgroundType);
        radioGroupBackgroundType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if (checkedId == R.id.radioGradient) {
                    currentTemplate.setBackgroundType("GRADIENT");
                    currentTemplate.setBackgroundImageUri(""); // Limpia la imagen
                    selectBackgroundImageButton.setEnabled(false);
                    // Habilitar controles de colores si es necesario
                    color1Picker.setEnabled(true);
                    color2Picker.setEnabled(true);
                } else if (checkedId == R.id.radioImage) {
                    currentTemplate.setBackgroundType("IMAGE");
                    selectBackgroundImageButton.setEnabled(true);
                    // Deshabilitar controles de gradiente
                    color1Picker.setEnabled(false);
                    color2Picker.setEnabled(false);
                }
                updatePreview();
            }
        });


// Vincular el SeekBar para ajustar el alto del formulario
        SeekBar seekBarFormHeight = findViewById(R.id.seekBarFormHeight);
        seekBarFormHeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Similarmente, puedes definir un mínimo
                int newHeight = progress; // O: progress + 200;
                currentTemplate.setFormHeight(newHeight);
                updatePreview();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });


        SeekBar seekBarButtonBorderRadius = findViewById(R.id.seekBarButtonBorderRadius);
        seekBarButtonBorderRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Actualiza el radio del botón (agregando la unidad "px")
                currentTemplate.setButtonBorderRadius(progress + "px");
                updatePreview();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        Button buttonColorPicker = findViewById(R.id.buttonColorPicker);
        buttonColorPicker.setOnClickListener(v ->
                showAdvancedColorPicker("Color del Botón", currentTemplate.getButtonColor(), color -> {
                    currentTemplate.setButtonColor(String.format("#%06X", (0xFFFFFF & color)));
                    updatePreview();
                })
        );

        Button buttonTextColorPicker = findViewById(R.id.buttonTextColorPicker);
        buttonTextColorPicker.setOnClickListener(v ->
                showAdvancedColorPicker("Color del texto del Botón", currentTemplate.getButtonTextColor(), color -> {
                    currentTemplate.setButtonTextColor(String.format("#%06X", (0xFFFFFF & color)));
                    updatePreview();
                })
        );

        EditText buttonWidthEdit = findViewById(R.id.buttonWidthEdit);
        EditText buttonHeightEdit = findViewById(R.id.buttonHeightEdit);

        buttonWidthEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String width = buttonWidthEdit.getText().toString().trim();
                if (!width.isEmpty()) {
                    // Si no se encuentra 'px' ni '%', se asume píxeles.
                    if (!width.matches(".*(px|%)$")) {
                        width = width + "px";
                    }
                    currentTemplate.setButtonWidth(width);
                    updatePreview();
                }
            }
        });

        buttonHeightEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String height = buttonHeightEdit.getText().toString().trim();
                if (!height.isEmpty()) {
                    if (!height.matches(".*(px|%)$")) {
                        height = height + "px";
                    }
                    currentTemplate.setButtonHeight(height);
                    updatePreview();
                }
            }
        });

        EditText buttonTextSizeEdit = findViewById(R.id.buttonTextSizeEdit);

        buttonTextSizeEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String textSize = buttonTextSizeEdit.getText().toString().trim();
                if (!textSize.isEmpty()) {
                    // Si no se encuentra 'px' ni '%', se asume píxeles.
                    if (!textSize.matches(".*(px|%)$")) {
                        textSize = textSize + "px";
                    }
                    currentTemplate.setButtonTextSize(textSize);
                    updatePreview();
                }
            }
        });

        EditText buttonTextEdit = findViewById(R.id.buttonTextEdit);

        buttonTextEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String text = buttonTextEdit.getText().toString().trim();
                if (!text.isEmpty()) {
                    currentTemplate.setButtonText(text);
                    updatePreview();
                }
            }
        });
        selectBackgroundImageButton = findViewById(R.id.selectBackgroundImageButton);

        selectBackgroundImageButton.setOnClickListener(v -> selectBackgroundImage());

        TextView textView = findViewById(R.id.textViewPdfLink);
        DatabaseReference manualRef = FirebaseDatabase.getInstance().getReference("MANUALES").child("M_TEMPLY");

        manualRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String urlManual = snapshot.child("URL").getValue(String.class);

                if (urlManual != null && !urlManual.isEmpty()) {
                    String html = "<a href='" + urlManual + "'>Descargar manual en PDF</a>";
                    textView.setMovementMethod(LinkMovementMethod.getInstance());
                    textView.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    textView.setText("Manual no disponible.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error al leer el manual", error.toException());
            }
        });





        // Agregar esta línea en onCreate para enlazar el seekBarFormBorderRadius
        SeekBar seekBarFormBorderRadius = findViewById(R.id.seekBarFormBorderRadius);
        seekBarFormBorderRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Se establece el borde con la unidad "px"
                currentTemplate.setFormBorderRadius(progress + "px");
                updatePreview();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        // Botón para exportar la plantilla
        //exportButton.setOnClickListener(v -> {
        //    doExportTemplate();
        //});

        // en onCreate, después de exportButton:
        exportButton.setOnClickListener(v -> {
            if (doExportTemplate()) {         // ← devuelve boolean
                openRouterSelector();       // muestra el modal de routers
            }
        });

        // Inicializar la vista previa con los valores por defecto
        updatePreview();
    }

    // ---------- dentro de TemplateDesignerActivity ----------
    private void openRouterSelector() {

        View view = getLayoutInflater()
                .inflate(R.layout.dialog_select_router, null);

        Spinner spRouters   = view.findViewById(R.id.spinnerNeighbors);
        EditText edtUser    = view.findViewById(R.id.editUser);
        EditText edtPass    = view.findViewById(R.id.editPass);
        EditText edtRemote  = view.findViewById(R.id.editRemoteDir);
        Button   btnUpload  = view.findViewById(R.id.btnUpload);

        ArrayAdapter<String> neighborAdapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item,
                        new ArrayList<>());
        neighborAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        // ← placeholder fijo
        neighborAdapter.add("Selecciona un router…");   // ← placeholder fijo
        spRouters.setAdapter(neighborAdapter);
        spRouters.setSelection(0, false);

        /* 3️⃣  Listener: habilita solo si la posición > 0 */
        spRouters.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                btnUpload.setEnabled(pos > 0);          // true solo si eligió un router real
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { btnUpload.setEnabled(false); }
        });

        AlertDialog dlg = new AlertDialog.Builder(this)
                .setTitle("Enviar plantilla al Router")
                .setView(view)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Nueva búsqueda", null)    // ← NUEVO
                .create();
        dlg.show();

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Buscando routers…");
        pd.setCancelable(false);            // evita cerrar antes de tiempo

        /* función para descubrir */
        Runnable runDiscovery = () -> {
            /* limpia todo menos el placeholder */
            while (neighborAdapter.getCount() > 1) neighborAdapter.remove(neighborAdapter.getItem(1));
            btnUpload.setEnabled(false);
            new TaskDiscoverNeighbors(neighborAdapter, pd).execute();
        };
        runDiscovery.run();                                 // primera vez

        /* ✅ Interceptamos el botón POSITIVE para que NO cierre el diálogo */
        dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> runDiscovery.run());

        /* 2️⃣  Cuando el usuario pulse “Conectar y subir” */
        btnUpload.setOnClickListener(b -> {
            String selected = (String) spRouters.getSelectedItem(); // "IP (MAC)"
            if (selected == null) { Toast.makeText(this,"Sin routers",Toast.LENGTH_SHORT).show(); return; }

            String host = selected.split(" ")[1];                  // solo la IP
            int    port = 21;                                      // FTP; cámbialo si usas SFTP
            String user = edtUser.getText().toString().trim();
            String pass = edtPass.getText().toString().trim();
            String dir  = edtRemote.getText().toString().trim();

            new TaskUploadTemplate(host,port,user,pass,dir).execute();
            dlg.dismiss();
        });
    }

    /* ---------- AsyncTask para descubrir routers ---------- */
    private static class TaskDiscoverNeighbors extends AsyncTask<Void, String, Void> {

        private final ArrayAdapter<String> adapter;
        private static final String TAG = "DISCOVER_MT";
        private final ProgressDialog pd;

        TaskDiscoverNeighbors(ArrayAdapter<String> adapter, ProgressDialog pd) { this.adapter = adapter; this.pd = pd; }

        @Override protected void onPreExecute() {            // ① se muestra
            super.onPreExecute();
            if (pd != null && !pd.isShowing()) pd.show();
        }

        @Override protected Void doInBackground(Void... p){
            try(DatagramSocket sock = new DatagramSocket(5678)){
                sock.setBroadcast(true);
                sock.setSoTimeout(6000);
                byte[] req = {(byte)0xFF,(byte)0xFF};
                DatagramPacket pkt = new DatagramPacket(req,req.length,
                        InetAddress.getByName("192.168.88.255"),5678);

                for(int i=0;i<3 && !isCancelled();i++){
                    sock.send(pkt);                                     // broadcast
                    while(true){
                        try{
                            DatagramPacket resp = new DatagramPacket(new byte[240],240);
                            sock.receive(resp);

                            byte[] buf = resp.getData();
                            String ip  = resp.getAddress().getHostAddress();
                            String mac = String.format("%02X:%02X:%02X:%02X:%02X:%02X",
                                    buf[4],buf[5],buf[6],buf[7],buf[8],buf[9]);

                            /* --------- leer System-Identity --------- */
                            int start = 18;                              // 4 ID +1 ver +6 MAC +4 IP +3 pad
                            int end   = start;
                            while(end < resp.getLength() && buf[end] != 0) end++;
                            String id = new String(buf,start,end-start,"UTF-8").trim();

                            publishProgress(id + " " + ip); // 👈
                        }catch(SocketTimeoutException e){ break; }
                    }
                }
            }catch(IOException e){ Log.e(TAG,"MNDP error",e); }
            return null;
        }

        @Override protected void onProgressUpdate(String... values) {
            adapter.add(values[0]); adapter.notifyDataSetChanged();
        }
        @Override protected void onPostExecute(Void r) {     // ② se oculta
            super.onPostExecute(r);
            if (pd != null && pd.isShowing()) pd.dismiss();
        }


    }

    private void selectBackgroundImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkAndRequestPermission(Manifest.permission.READ_MEDIA_IMAGES, REQUEST_SELECT_BACKGROUND_IMAGE);
        } else {
            checkAndRequestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_SELECT_BACKGROUND_IMAGE);
        }
    }

    private void selectLogo() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkAndRequestPermission(Manifest.permission.READ_MEDIA_IMAGES, REQUEST_SELECT_LOGO);
        } else {
            checkAndRequestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_SELECT_LOGO);
        }
    }
    private void checkAndRequestPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            // Verifica si debemos mostrar explicación
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                new AlertDialog.Builder(this)
                        .setTitle("Permiso necesario")
                        .setMessage("Para seleccionar un logo, necesitamos acceso a tus imágenes.")
                        .setPositiveButton("Conceder", (dialog, which) -> {
                            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            } else {
                // Primera vez o el usuario marcó "No volver a preguntar"
                ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
            }
        } else {
            // Ya tiene permiso
            if (requestCode == REQUEST_SELECT_LOGO) {
                abrirGaleriaImagen(REQUEST_SELECT_LOGO);
            } else if (requestCode == REQUEST_SELECT_BACKGROUND_IMAGE) {
                abrirGaleriaImagen(REQUEST_SELECT_BACKGROUND_IMAGE);
            }
        }
    }

    private void abrirGaleriaImagen(int requestCode) {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo abrir la galería: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("TemplateDesigner", "Error al abrir galería", e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_SELECT_LOGO || requestCode == REQUEST_SELECT_BACKGROUND_IMAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (requestCode == REQUEST_SELECT_LOGO) {
                    abrirGaleriaImagen(REQUEST_SELECT_LOGO);
                } else {
                    abrirGaleriaImagen(REQUEST_SELECT_BACKGROUND_IMAGE);
                }
            } else {
                boolean showRationale = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES);
                } else {
                    showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE);
                }

                if (!showRationale) {
                    // Usuario marcó "No volver a preguntar"
                    new AlertDialog.Builder(this)
                            .setTitle("Permiso denegado")
                            .setMessage("Sin este permiso no podrás seleccionar imágenes. Puedes habilitarlo en la configuración de la aplicación.")
                            .setPositiveButton("Ir a configuración", (dialog, which) -> {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivityForResult(intent, REQUEST_PERMISSION_SETTINGS);
                            })
                            .setNegativeButton("Cancelar", null)
                            .show();
                } else {
                    String tipo = (requestCode == REQUEST_SELECT_LOGO) ? "logo" : "imagen de fondo";
                    Toast.makeText(this, "Permiso denegado. No podrás seleccionar una " + tipo + ".", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private boolean doExportTemplate() {

        final String TAG = "EXPORT";

        try {
            Log.d(TAG, "⇢ Iniciando exportación…");

            /* 1️⃣  Generar archivos base */
            Map<String, String> filesMap =
                    TemplateGenerator.generateAllTemplates(currentTemplate);
            Log.d(TAG, " · Archivos generados: " + filesMap.keySet());

            /* 2️⃣  Extensiones reales */
            String logoExt = FilesManager.guessExtension(this, currentTemplate.getLogoUri());
            String bgExt   = FilesManager.guessExtension(this, currentTemplate.getBackgroundImageUri());
            Log.d(TAG, " · Ext logo: " + logoExt + "  · Ext fondo: " + bgExt);

            /* 3️⃣  Reemplazos en login.html / css */
            String html = filesMap.get("login.html");
            if (html != null) {
                html = html.replace("logo.png", "logo." + logoExt)
                        .replace("background.png", "background." + bgExt);
                filesMap.put("login.html", html);
                Log.d(TAG, " · login.html modificado");
            }

            if ("IMAGE".equals(currentTemplate.getBackgroundType())) {
                String css = filesMap.get("login.css");
                if (css != null) {
                    css = css.replace("background.png", "background." + bgExt);
                    filesMap.put("login.css", css);
                    Log.d(TAG, " · login.css modificado");
                }
            }

            /* 4️⃣  HTML manual */
            if (manualHtmlMode && editedHtml != null && !editedHtml.isEmpty()) {
                String processed = editedHtml.replace("logo.png", "logo." + logoExt)
                        .replace("background.png", "background." + bgExt);
                filesMap.put("login.html", processed);
                Log.d(TAG, " · HTML editado sobrescrito");
            }

            /* 5️⃣  Copiar a Downloads/hotspot/ */
            boolean ok = FilesManager.writeTemplateFiles(
                    this,
                    filesMap,
                    currentTemplate.getLogoUri(),
                    currentTemplate.getBackgroundImageUri()
            );
            Log.d(TAG, " · writeTemplateFiles devuelve: " + ok);

            Toast.makeText(this,
                    ok ? "Plantilla exportada exitosamente."
                            : "Error al exportar la plantilla.",
                    Toast.LENGTH_LONG).show();

            Log.d(TAG, "⇠ Exportación terminada");
            return ok;

        } catch (Exception e) {
            Log.e("EXPORT", "‼️ Error inesperado en exportación", e);
            Toast.makeText(this,
                    "Error al exportar la plantilla: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            return false;
        }
    }




    // Método para abrir la ventana modal de vista previa
    private void openPreviewModal() {
        // Infla el layout del diálogo
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_preview, null);
        WebView modalWebView = dialogView.findViewById(R.id.modalPreviewWebView);
        // Configura el WebView
        modalWebView.getSettings().setJavaScriptEnabled(true);
        modalWebView.getSettings().setLoadWithOverviewMode(true);
        modalWebView.getSettings().setUseWideViewPort(true);

        // Usa el HTML editado si existe; en caso contrario, genera el HTML a partir de currentTemplate
        String htmlContent = !editedHtml.isEmpty()
                ? editedHtml
                : TemplateGenerator.generateLoginHtml(currentTemplate, false);
        modalWebView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);

        // Crea el AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setTitle("Vista previa")
                .setPositiveButton("CERRAR", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        // Desactivar animaciones para evitar efecto de estiramiento
        if (dialog.getWindow() != null) {
            dialog.getWindow().setWindowAnimations(0);
        }

        // Muestra el diálogo
        dialog.show();
    }


    private void openEditHtmlModal() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_html, null);
        EditText editHtmlCode = dialogView.findViewById(R.id.editHtmlCode);

        // Obtén el HTML generado actualmente (o el que ya se editó previamente)
        if (manualHtmlMode && !editedHtml.isEmpty()) {
            editHtmlCode.setText(editedHtml);
        } else {
            editHtmlCode.setText(TemplateGenerator.generateLoginHtml(currentTemplate, false));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar HTML")
                .setView(dialogView)
                .setPositiveButton("Guardar cambios", (dialog, which) -> {
                    // Guarda el HTML editado y activa el modo manual
                    editedHtml = editHtmlCode.getText().toString().trim();
                    manualHtmlMode = true;
                    updatePreview(); // En este caso, updatePreview usará editedHtml sin fusionar
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }


    // Método para actualizar la vista previa usando el HTML editado
    private void updatePreviewWithEditedHtml() {
        if (!editedHtml.isEmpty()){
            previewWebView.clearCache(true);
            previewWebView.loadDataWithBaseURL(null, editedHtml, "text/html", "UTF-8", null);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) return;
        Uri uri = data.getData();
        if (uri == null) return;

        /* ---------- PERMISO PERSISTENTE ---------- */
        final int takeFlags = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
        try {                                             // NEW
            getContentResolver().takePersistableUriPermission(uri, takeFlags); // NEW
        } catch (SecurityException ignore) { }            // NEW
        /* ----------------------------------------- */

        // Decide qué tipo de imagen y tamaño máximo
        long maxSize;
        String loadingMsg;
        if (requestCode == REQUEST_SELECT_LOGO) {
            maxSize = MAX_LOGO_SIZE;                  // 300 KB
            loadingMsg = "Verificando logo...";
        } else if (requestCode == REQUEST_SELECT_BACKGROUND_IMAGE) {
            maxSize = MAX_BACKGROUND_SIZE;            // 500 KB
            loadingMsg = "Verificando imagen de fondo...";
        } else {
            return; // no es nuestro caso
        }

        // Mostrar diálogo de progreso
        ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage(loadingMsg);
        progress.setCancelable(false);
        progress.show();

        new Thread(() -> {
            boolean isValid = false;
            String error = null;
            long size = -1;

            // 1) Intentar obtener el tamaño desde OpenableColumns.SIZE
            try (Cursor cursor = getContentResolver()
                    .query(uri,
                            new String[]{OpenableColumns.SIZE},
                            null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int idx = cursor.getColumnIndex(OpenableColumns.SIZE);
                    if (idx != -1) {
                        size = cursor.getLong(idx);
                    }
                }
            } catch (Exception e) {
                // ignore — lo intentaremos leyendo bytes
            }

            // 2) Si no obtuvimos tamaño válido, leer bytes hasta el límite
            if (size < 0) {
                try (InputStream is = getContentResolver().openInputStream(uri)) {
                    if (is != null) {
                        byte[] buf = new byte[8192];
                        int len;
                        long acc = 0;
                        while ((len = is.read(buf)) != -1) {
                            acc += len;
                            if (acc > maxSize) {
                                size = acc;
                                break;
                            }
                        }
                        if (size < 0) size = acc;
                    }
                } catch (Exception e) {
                    error = "No se pudo verificar el tamaño: " + e.getMessage();
                }
            }

            // 3) Validar contra el máximo permitido
            if (size >= 0) {
                if (size <= maxSize) {
                    isValid = true;
                } else {
                    String humanMax = (maxSize / 1024) + " KB";
                    error = "El archivo pesa " + (size/1024) + " KB y supera el límite de " + humanMax + ".";
                }
            } else if (error == null) {
                error = "No se pudo determinar el tamaño del archivo.";
            }

            boolean finalValid = isValid;
            String finalError = error;

            // 4) Volver al hilo UI
            runOnUiThread(() -> {
                progress.dismiss();
                if (finalValid) {
                    if (requestCode == REQUEST_SELECT_LOGO) {
                        // Guardar logo
                        currentTemplate.setLogoUri(uri.toString());
                        logoPreview.setImageURI(uri);
                        Toast.makeText(this, "Logo seleccionado correctamente", Toast.LENGTH_SHORT).show();
                    } else {
                        // Guardar fondo
                        currentTemplate.setBackgroundImageUri(uri.toString());
                        currentTemplate.setBackgroundType("IMAGE");
                        Toast.makeText(this, "Imagen de fondo seleccionada correctamente", Toast.LENGTH_SHORT).show();
                    }
                    updatePreview();
                } else {
                    new AlertDialog.Builder(this)
                            .setTitle("Error al seleccionar imagen")
                            .setMessage(finalError)
                            .setPositiveButton("Entendido", null)
                            .show();
                }
            });
        }).start();
    }


    // Método para actualizar la vista previa en el WebView
    private void updatePreview() {
        String htmlContent;
        if (manualHtmlMode && !editedHtml.isEmpty()){
            // Fusiona los cambios del template en el HTML editado
            htmlContent = updateEditedHtmlWithTemplateChanges();
            editedHtml = htmlContent;  // Actualiza la variable para que siga representando el HTML fusionado.
        } else {
            htmlContent = TemplateGenerator.generateLoginHtml(currentTemplate, false);
        }
        previewWebView.clearCache(true);
        previewWebView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
    }


    private String updateEditedHtmlWithTemplateChanges() {
        String updated = editedHtml;
        // Actualiza el width y height del botón en el CSS
        // Se asume que el CSS contiene reglas como: "width: [valor];" en el selector del botón.
        updated = updated.replaceAll("(?i)(button\\s*\\{[^}]*width:\\s*)([^;]+)(;)",
                "$1" + currentTemplate.getButtonWidth() + "$3");
        updated = updated.replaceAll("(?i)(button\\s*\\{[^}]*height:\\s*)([^;]+)(;)",
                "$1" + currentTemplate.getButtonHeight() + "$3");
        // Puedes repetir para otros campos como el alto del formulario (#box)
        updated = updated.replaceAll("(?i)(#box\\s*\\{[^}]*height:\\s*)([^;]+)(;)",
                "$1" + currentTemplate.getFormHeight() + "px$3");
        return updated;
    }


    // Método para exportar la plantilla generando los archivos y guardándolos en disco
    private void exportTemplaxte() {
        Map<String, String> filesMap = TemplateGenerator.generateAllTemplates(currentTemplate);
        boolean success = FilesManager.writeTemplateFiles(this, filesMap, currentTemplate.getLogoUri(), currentTemplate.getBackgroundImageUri());
        if (success) {
            Toast.makeText(this, "Plantilla exportada exitosamente en la carpeta de descargas.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Error al exportar la plantilla.", Toast.LENGTH_LONG).show();
        }
    }

    // Implementación del selector de color usando AmbilWarnaDialog
    private interface ColorSelectedListener {
        void onColorSelected(int color);
    }

    private void showAdvancedColorPicker(String title, String currentColorHex, final ColorSelectedListener listener) {
        int initialColor = Color.parseColor(currentColorHex);
        AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this, initialColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                listener.onColorSelected(color);
            }
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                // No hace nada al cancelar
            }
        });
        colorPicker.show();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(TemplateDesignerActivity.this, MainActivity.class));
        finish();
    }

    /* ---------- AsyncTask subida (igual al de antes) ---------- */
    private class TaskUploadTemplate extends AsyncTask<Void,Void,Boolean>{
        String h;int p;String u,pa,d;ProgressDialog pg;
        TaskUploadTemplate(String h,int p,String u,String pa,String d){
            this.h=h;this.p=p;this.u=u;this.pa=pa;this.d=d;
        }
        @Override protected void onPreExecute(){
            /* 1️⃣  Muestra todo lo que va a usarse en la conexión */
            Log.d("UPLOAD", "=== Parámetros FTP ===");
            Log.d("UPLOAD", "Host : " + h);
            Log.d("UPLOAD", "Puerto: " + p);
            Log.d("UPLOAD", "Usuario: " + u);
            Log.d("UPLOAD", "Pass  : " + (pa.isEmpty() ? "<vacío>" : "******"));
            Log.d("UPLOAD", "Direct: " + d);
            Log.d("UPLOAD", "=======================");
            pg = ProgressDialog.show(TemplateDesignerActivity.this,
                    "Temply","Subiendo archivos…",true,false);
        }
        @Override protected Boolean doInBackground(Void... v){
            return FilesManager.uploadTemplateFTP(
                    h,p,u,pa,d,TemplateDesignerActivity.this);
        }
        @Override protected void onPostExecute(Boolean ok){
            pg.dismiss();
            Toast.makeText(TemplateDesignerActivity.this,
                    ok?"Plantilla subida al Router ✅":"Error al subir ❌",Toast.LENGTH_LONG).show();
        }
    }


}
