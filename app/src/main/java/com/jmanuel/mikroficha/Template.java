package com.jmanuel.mikroficha;

/**
 * Clase modelo que representa los parámetros personalizables de la plantilla.
 */
public class Template {

    // ---------------------------------------------------------
    // 1. Configuración de Fondo y Degradado
    // ---------------------------------------------------------
    // backgroundType puede tomar valores como:
    //   "TWO_COLORS"  para un fondo con dos colores
    //   "GRADIENT"    para un degradado lineal entre bgColor1 y bgColor2
    private String backgroundType = "GRADIENT";
    private String bgColor1 = "#1976D2";   // Color principal
    private String bgColor2 = "#ECEFF1";   // Color secundario
    private String gradientOrientation = "to right";  // Ej: "to right", "to bottom", etc.

    // ---------------------------------------------------------
    // 2. Configuración del Logo
    // ---------------------------------------------------------
    private String logoUri = "";           // URI de la imagen del logo
    private String logoWidth = "150px";    // Ancho del logo (por defecto)
    private String logoHeight = "auto";    // Alto del logo (por defecto)
    private String logoBorderRadius = "0px";  // Redondez de las esquinas del logo

    // ---------------------------------------------------------
    // 3. Configuración de Colores Generales y Texto
    // ---------------------------------------------------------
    private String textColor = "#212121";  // Color principal del texto
    private String buttonColor = "#1976D2";    // Color principal de los botones

    // ---------------------------------------------------------
    // 4. Textos para la Interfaz
    // ---------------------------------------------------------
    private String titleText = "Login";
    private String usernameHint = "Usuario";
    private String passwordHint = "Contraseña";
    private String footerText = "Información adicional";

    // ---------------------------------------------------------
    // 5. Configuración del Formulario
    // ---------------------------------------------------------
    private String inputFontSize = "14px";          // Tamaño de fuente de los campos
    private String inputPadding = "10px";           // Padding de los campos
    private String inputMargin = "5px";             // Margen de los campos
    private String formBackgroundColor = "#FFFFFF"; // Color de fondo del contenedor
    private int formBorderWidth = 1;                // Grosor del borde en px
    private String formBorderColor = "#000000";     // Color del borde
    private String formBorderStyle = "solid";       // Estilo del borde (solid, dotted, dashed)
    private String formBorderRadius = "2px";        // Curvatura de las esquinas
    private boolean hidePassword = false;           // Para ocultar campo password y usar PIN
    private String verticalOffset = "0px";          // Desplazamiento vertical del formulario
    private int formWidth = 650;                    // Ancho del formulario en px
    private int formHeight = 450;                   // Alto del formulario en px

    // ---------------------------------------------------------
    // 6. Configuración de Botones
    // ---------------------------------------------------------
    private String buttonBorderRadius = "4px";      // Curvatura de esquinas del botón
    private String buttonIconUri = "";              // URI del icono en el botón (opcional)
    private String buttonTextColor = "#FFFFFF";     // Color del texto del botón
    private String buttonWidth = "auto";            // Ancho del botón (ej. "100px" o "auto")
    private String buttonHeight = "auto";           // Alto del botón (ej. "40px" o "auto")
    private String buttonTextSize = "16px";         // Tamaño del texto del botón
    private String buttonText = "Ingresar";         // Texto que se muestra en el botón

    // ---------------------------------------------------------
    // Getters y Setters
    // ---------------------------------------------------------

    // -- Fondo y Degradado --
    public String getBackgroundType() {
        return backgroundType;
    }
    public void setBackgroundType(String backgroundType) {
        this.backgroundType = backgroundType;
    }

    public String getBgColor1() {
        return bgColor1;
    }
    public void setBgColor1(String bgColor1) {
        this.bgColor1 = bgColor1;
    }

    public String getBgColor2() {
        return bgColor2;
    }
    public void setBgColor2(String bgColor2) {
        this.bgColor2 = bgColor2;
    }

    public String getGradientOrientation() {
        return gradientOrientation;
    }
    public void setGradientOrientation(String gradientOrientation) {
        this.gradientOrientation = gradientOrientation;
    }

    // -- Logo --
    public String getLogoUri() {
        return logoUri;
    }
    public void setLogoUri(String logoUri) {
        this.logoUri = logoUri;
    }

    public String getLogoWidth() {
        return logoWidth;
    }
    public void setLogoWidth(String logoWidth) {
        this.logoWidth = logoWidth;
    }

    public String getLogoHeight() {
        return logoHeight;
    }
    public void setLogoHeight(String logoHeight) {
        this.logoHeight = logoHeight;
    }

    public String getLogoBorderRadius() {
        return logoBorderRadius;
    }
    public void setLogoBorderRadius(String logoBorderRadius) {
        this.logoBorderRadius = logoBorderRadius;
    }

    // -- Colores Generales --
    public String getTextColor() {
        return textColor;
    }
    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public String getButtonColor() {
        return buttonColor;
    }
    public void setButtonColor(String buttonColor) {
        this.buttonColor = buttonColor;
    }

    // -- Textos --
    public String getTitleText() {
        return titleText;
    }
    public void setTitleText(String titleText) {
        this.titleText = titleText;
    }

    public String getUsernameHint() {
        return usernameHint;
    }
    public void setUsernameHint(String usernameHint) {
        this.usernameHint = usernameHint;
    }

    public String getPasswordHint() {
        return passwordHint;
    }
    public void setPasswordHint(String passwordHint) {
        this.passwordHint = passwordHint;
    }

    public String getFooterText() {
        return footerText;
    }
    public void setFooterText(String footerText) {
        this.footerText = footerText;
    }

    // -- Formulario --
    public String getInputFontSize() {
        return inputFontSize;
    }
    public void setInputFontSize(String inputFontSize) {
        this.inputFontSize = inputFontSize;
    }

    public String getInputPadding() {
        return inputPadding;
    }
    public void setInputPadding(String inputPadding) {
        this.inputPadding = inputPadding;
    }

    public String getInputMargin() {
        return inputMargin;
    }
    public void setInputMargin(String inputMargin) {
        this.inputMargin = inputMargin;
    }

    public String getFormBackgroundColor() {
        return formBackgroundColor;
    }
    public void setFormBackgroundColor(String formBackgroundColor) {
        this.formBackgroundColor = formBackgroundColor;
    }

    public int getFormBorderWidth() {
        return formBorderWidth;
    }
    public void setFormBorderWidth(int formBorderWidth) {
        this.formBorderWidth = formBorderWidth;
    }

    public String getFormBorderColor() {
        return formBorderColor;
    }
    public void setFormBorderColor(String formBorderColor) {
        this.formBorderColor = formBorderColor;
    }

    public String getFormBorderStyle() {
        return formBorderStyle;
    }
    public void setFormBorderStyle(String formBorderStyle) {
        this.formBorderStyle = formBorderStyle;
    }

    public String getFormBorderRadius() {
        return formBorderRadius;
    }
    public void setFormBorderRadius(String formBorderRadius) {
        this.formBorderRadius = formBorderRadius;
    }

    public boolean isHidePassword() {
        return hidePassword;
    }
    public void setHidePassword(boolean hidePassword) {
        this.hidePassword = hidePassword;
    }

    public String getVerticalOffset() {
        return verticalOffset;
    }
    public void setVerticalOffset(String verticalOffset) {
        this.verticalOffset = verticalOffset;
    }

    public int getFormWidth() {
        return formWidth;
    }
    public void setFormWidth(int formWidth) {
        this.formWidth = formWidth;
    }

    public int getFormHeight() {
        return formHeight;
    }
    public void setFormHeight(int formHeight) {
        this.formHeight = formHeight;
    }

    // -- Botones --
    public String getButtonBorderRadius() {
        return buttonBorderRadius;
    }
    public void setButtonBorderRadius(String buttonBorderRadius) {
        this.buttonBorderRadius = buttonBorderRadius;
    }

    public String getButtonIconUri() {
        return buttonIconUri;
    }
    public void setButtonIconUri(String buttonIconUri) {
        this.buttonIconUri = buttonIconUri;
    }

    public String getButtonTextColor() {
        return buttonTextColor;
    }
    public void setButtonTextColor(String buttonTextColor) {
        this.buttonTextColor = buttonTextColor;
    }

    public String getButtonWidth() {
        return buttonWidth;
    }
    public void setButtonWidth(String buttonWidth) {
        this.buttonWidth = buttonWidth;
    }

    public String getButtonHeight() {
        return buttonHeight;
    }
    public void setButtonHeight(String buttonHeight) {
        this.buttonHeight = buttonHeight;
    }

    public String getButtonTextSize() {
        return buttonTextSize;
    }
    public void setButtonTextSize(String buttonTextSize) {
        this.buttonTextSize = buttonTextSize;
    }

    public String getButtonText() {
        return buttonText;
    }
    public void setButtonText(String buttonText) {
        this.buttonText = buttonText;
    }
}
