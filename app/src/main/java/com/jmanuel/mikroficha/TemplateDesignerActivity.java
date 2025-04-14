package com.jmanuel.mikroficha;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
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
import android.widget.Toast;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

    private String editedHtml = ""; // Variable para guardar el html modificado

    private boolean manualHtmlMode = false;

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
        selectLogoButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_SELECT_LOGO);
        });

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
                    currentTemplate.setButtonWidth(text);
                    updatePreview();
                }
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
        exportButton.setOnClickListener(v -> {
            Map<String, String> filesMap = TemplateGenerator.generateAllTemplates(currentTemplate);
            // Si el usuario ha editado el HTML manualmente, procesamos el contenido para corregir la URI del logo
            if (manualHtmlMode && editedHtml != null && !editedHtml.isEmpty()) {
                // Reemplaza en el HTML cualquier src que comience con "content://" por "logo.png"
                String processedHtml = editedHtml.replaceAll("(<img\\s+[^>]*src=\")content://[^\"]+(\"[^>]*>)", "$1logo.png$2");
                filesMap.put("login.html", processedHtml);
            }

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
        super.onBackPressed();
        startActivity(new Intent(TemplateDesignerActivity.this, MainActivity.class));
        finish();
    }
}
