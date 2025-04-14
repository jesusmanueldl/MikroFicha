package com.jmanuel.mikroficha;

/**
 * Clase modelo que representa los parámetros personalizables de la plantilla.
 */
public class Template {

    // Configuración del fondo
    // backgroundType puede tomar los valores:
    // "TWO_COLORS" para fondo con dos colores (por ejemplo, uno para el body y otro para el contenedor)
    // "GRADIENT" para un degradado lineal que combine bgColor1 y bgColor2.
    private String backgroundType = "GRADIENT";
    private String bgColor1 = "#1976D2"; // Color principal (por defecto)
    private String bgColor2 = "#ECEFF1"; // Color secundario (por defecto)

    // Nueva propiedad para la orientación del degradado.
    // Valores de ejemplo: "to right", "to bottom", "to left", "to top"
    private String gradientOrientation = "to right";

    // Configuración del logo
    private String logoUri = ""; // Almacena la URI de la imagen seleccionada

    // Nuevas propiedades para el logo
    private String logoWidth = "150px";
    private String logoHeight = "auto";

    // Configuración de colores generales
    private String buttonColor = "#1976D2";
    private String textColor = "#212121";

    // Configuración de textos
    private String titleText = "Login";
    private String usernameHint = "Usuario";
    private String passwordHint = "Contraseña";
    private String footerText = "Pie de página";

    // --- Campos para personalizar el formulario ---
    private String inputFontSize = "14px";      // Tamaño de fuente de los input
    private String inputPadding = "10px";         // Padding de los input
    private String inputMargin = "5px";           // Margen entre input
    private String formBackgroundColor = "#FFFFFF"; // Color de fondo del contenedor del formulario

    // --- Borde del formulario ---
    private int formBorderWidth = 1;                // Grosor en px
    private String formBorderColor = "#000000";     // Color
    private String formBorderStyle = "solid";       // Estilo (solid, dotted, dashed)

    // Nueva propiedad para ajustar la altura (desplazamiento vertical) del formulario
    private String verticalOffset = "0px"; // Por defecto: sin desplazamiento

    // Nueva propiedad para ajustar el borderauios del logo
    private String logoBorderRadius = "0px";

    public String getLogoBorderRadius() {
        return logoBorderRadius;
    }
    public void setLogoBorderRadius(String logoBorderRadius) {
        this.logoBorderRadius = logoBorderRadius;
    }

    private String formBorderRadius = "2px";

    public String getFormBorderRadius() {
        return formBorderRadius;
    }
    public void setFormBorderRadius(String formBorderRadius) {
        this.formBorderRadius = formBorderRadius;
    }

    // Nueva propiedad para ocultar el password
    private boolean hidePassword = false;

    public boolean isHidePassword() {
        return hidePassword;
    }
    public void setHidePassword(boolean hidePassword) {
        this.hidePassword = hidePassword;
    }

    // Nueva propiedad para redondear el botón
    private String buttonBorderRadius = "4px"; // valor por defecto
    // Nueva propiedad para icono en el botón (URI de la imagen)
    private String buttonIconUri = "";

    // Getters y setters para el botón
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

    // Nueva propiedad para el color del texto del botón
    private String buttonTextColor = "#FFFFFF";  // Valor por defecto, por ejemplo, blanco

    public String getButtonTextColor() {
        return buttonTextColor;
    }

    public void setButtonTextColor(String buttonTextColor) {
        this.buttonTextColor = buttonTextColor;
    }

    // Nueva propiedad para el ancho del formulario (valor en px)
    private int formWidth = 650;  // valor por defecto

    // Nueva propiedad para el alto del formulario (valor en px)
    private int formHeight = 400; // valor por defecto

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

    // Getters y setters

    // Fondo
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

    // Nueva propiedad para degradado
    public String getGradientOrientation() {
        return gradientOrientation;
    }
    public void setGradientOrientation(String gradientOrientation) {
        this.gradientOrientation = gradientOrientation;
    }

    // Logo
    public String getLogoUri() {
        return logoUri;
    }
    public void setLogoUri(String logoUri) {
        this.logoUri = logoUri;
    }
    // Nuevas propiedades para el logo
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

    // Colores generales
    public String getButtonColor() {
        return buttonColor;
    }
    public void setButtonColor(String buttonColor) {
        this.buttonColor = buttonColor;
    }
    public String getTextColor() {
        return textColor;
    }
    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    // Textos
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

    // Formulario
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

    // Borde del formulario
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

    // Desplazamiento vertical
    public String getVerticalOffset() {
        return verticalOffset;
    }
    public void setVerticalOffset(String verticalOffset) {
        this.verticalOffset = verticalOffset;
    }
}
