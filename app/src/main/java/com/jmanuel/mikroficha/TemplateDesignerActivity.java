package com.jmanuel.mikroficha;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import yuku.ambilwarna.AmbilWarnaDialog;

import java.util.Map;

public class TemplateDesignerActivity extends AppCompatActivity {

    private static final int REQUEST_SELECT_LOGO = 1;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template_designer);

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
                    currentTemplate.setLogoWidth(width);
                    updatePreview();
                }
            }
        });
        logoHeightEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String height = logoHeightEdit.getText().toString().trim();
                if (!height.isEmpty()) {
                    currentTemplate.setLogoHeight(height);
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
                    currentTemplate.setInputFontSize(fontSize);
                    updatePreview();
                }
            }
        });
        inputPaddingEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String padding = inputPaddingEdit.getText().toString().trim();
                if (!padding.isEmpty()) {
                    currentTemplate.setInputPadding(padding);
                    updatePreview();
                }
            }
        });
        inputMarginEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String margin = inputMarginEdit.getText().toString().trim();
                if (!margin.isEmpty()) {
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
        selectLogoButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_SELECT_LOGO);
        });

        // Listeners para los EditText de textos
        titleText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                currentTemplate.setTitleText(titleText.getText().toString());
                updatePreview();
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
        exportButton.setOnClickListener(v -> {
            Map<String, String> filesMap = TemplateGenerator.generateAllTemplates(currentTemplate);
            // Llama al método pasando currentTemplate.getLogoUri() como tercer parámetro.
            boolean success = FilesManager.writeTemplateFiles(this, filesMap, currentTemplate.getLogoUri());
            if (success) {
                Toast.makeText(this, "Plantilla exportada exitosamente.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Error al exportar la plantilla.", Toast.LENGTH_LONG).show();
            }
        });

        // Inicializar la vista previa con los valores por defecto
        updatePreview();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_LOGO && resultCode == RESULT_OK && data != null) {
            Uri logoUri = data.getData();
            // Verificar el tamaño de la imagen: máximo 300KB
            Cursor cursor = getContentResolver().query(logoUri, null, null, null, null);
            if (cursor != null) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                cursor.moveToFirst();
                long size = cursor.getLong(sizeIndex);
                cursor.close();
                if (size > 300 * 1024) { // 300 KB
                    Toast.makeText(this, "La imagen supera el límite de 300KB", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            currentTemplate.setLogoUri(logoUri.toString());
            logoPreview.setImageURI(logoUri);
            updatePreview();
        }
    }

    // Método para actualizar la vista previa en el WebView
    private void updatePreview() {
        // Usamos false para que el HTML de la vista previa use la URI real del logo.
        String htmlContent = TemplateGenerator.generateLoginHtml(currentTemplate, false);
        previewWebView.clearCache(true);
        previewWebView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
    }

    // Método para exportar la plantilla generando los archivos y guardándolos en disco
    private void exportTemplate() {
        Map<String, String> filesMap = TemplateGenerator.generateAllTemplates(currentTemplate);
        boolean success = FilesManager.writeTemplateFiles(this, filesMap, currentTemplate.getLogoUri());
        if (success) {
            Toast.makeText(this, "Plantilla exportada exitosamente.", Toast.LENGTH_LONG).show();
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
}
