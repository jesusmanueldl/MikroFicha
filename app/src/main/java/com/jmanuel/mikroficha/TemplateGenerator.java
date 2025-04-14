package com.jmanuel.mikroficha;

import java.util.HashMap;
import java.util.Map;

public class TemplateGenerator {

    // Genera el HTML del login a partir del objeto Template, incrustando el CSS generado inline.
    public static String generateLoginHtml(Template template, boolean isExport) {
        String css = generateLoginCss(template);

        // Determinar el HTML para el campo password (ya sea incluirlo o no, según hidePassword)
        String passwordHtml = "";
        if (!template.isHidePassword()) {
            passwordHtml = "<input type=\"text\" name=\"password\" placeholder=\""
                    + template.getPasswordHint() + "\"><br/>";
        }

        // Si se exporta, usamos "logo.png", de lo contrario usamos la URI real
        String logoSource;
        if (template.getLogoUri().isEmpty()) {
            logoSource = "default_logo.png";
        } else {
            logoSource = isExport ? "logo.png" : template.getLogoUri();
        }

        // Preparar el contenido del botón, incluyendo icono si se especifica
        String buttonContent = "Ingresar";
        if (!template.getButtonIconUri().isEmpty()) {
            buttonContent = "<img src=\"" + template.getButtonIconUri() +
                    "\" style=\"vertical-align: middle; margin-right: 5px; height: 16px;\"/>" + buttonContent;
        }

        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "  <meta charset=\"utf-8\">\n" +
                "  <title>" + template.getTitleText() + "</title>\n" +
                "  <style>" + css + "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div id=\"box\">\n" +
                "    <img src=\"" + logoSource +
                "\" alt=\"Logo\" style=\"width:" + template.getLogoWidth() +
                "; height:" + template.getLogoHeight() +
                "; border-radius:" + template.getLogoBorderRadius() + ";\"/>\n" +
                "    <h1>" + template.getTitleText() + "</h1>\n" +
                "    <form>\n" +
                "      <input type=\"text\" name=\"username\" placeholder=\"" + template.getUsernameHint() + "\"><br/>\n" +
                passwordHtml +
                "      <br><button type=\"submit\">" + buttonContent + "</button>\n" +
                "    </form>\n" +
                "    <footer>" + template.getFooterText() + "</footer>\n" +
                "  </div>\n" +
                "</body>\n" +
                "</html>";
        return html;
    }





    // Genera el CSS del login basado en los parámetros del Template
    public static String generateLoginCss(Template template) {
        StringBuilder css = new StringBuilder();

        // Configurar el body para centrar el contenido (formulario) vertical y horizontalmente usando Flexbox
        css.append("body {\n")
                .append("  display: flex;\n")
                .append("  align-items: center;\n")
                .append("  justify-content: center;\n")
                .append("  min-height: 100vh;\n")
                .append("  margin: 0;\n")
                .append("  padding: 0;\n")
                .append("  transform: translateY(-130px);\n")
                .append("  background: linear-gradient(")
                .append(template.getGradientOrientation()).append(", ")
                .append(template.getBgColor1()).append(", ")
                .append(template.getBgColor2()).append(");\n")
                .append("  color: ").append(template.getTextColor()).append(";\n")
                .append("}\n");

        // Regla para el contenedor del formulario (#box), con desplazamiento vertical
        css.append("#box {\n")
                .append("  background: ").append(template.getFormBackgroundColor()).append(";\n")
                .append("  margin: 20px auto;\n")
                .append("  padding: 20px;\n")
                .append("  width: ").append(template.getFormWidth()).append("px;\n")
                .append("  height: ").append(template.getFormHeight()).append("px;\n")
                .append("  border-radius: ").append(template.getFormBorderRadius()).append(";\n")
                .append("  border: ").append(template.getFormBorderWidth()).append("px ")
                .append(template.getFormBorderStyle()).append(" ")
                .append(template.getFormBorderColor()).append(";\n")
                .append("  text-align: center;\n")
                .append("  transform: translateY(").append(template.getVerticalOffset()).append(");\n")
                .append("}\n");

        /// Estilos para el botón (única definición)
        css.append("button {\n")
                .append("  background: ").append(template.getButtonColor()).append(";\n")
                .append("  color: ").append(template.getButtonTextColor()).append(";\n")
                .append("  padding: 10px 20px;\n")
                .append("  border: none;\n")
                .append("  border-radius: ").append(template.getButtonBorderRadius()).append(";\n")
                .append("  cursor: pointer;\n")
                .append("}\n");

        // Estilos para los inputs usando los nuevos parámetros y centrados (ancho fijo, margin auto)
        css.append("input[type='text'], input[type='password'] {\n")
                .append("  font-size: ").append(template.getInputFontSize()).append(";\n")
                .append("  padding: ").append(template.getInputPadding()).append(";\n")
                .append("  margin: ").append(template.getInputMargin()).append(" auto;\n")
                .append("  border: 1px solid #ccc;\n")
                .append("  border-radius: 4px;\n")
                .append("  width: 300px;\n")
                .append("  box-sizing: border-box;\n")
                .append("}\n");



        return css.toString();
    }

    // Genera todos los archivos de la plantilla en un Map (nombre del archivo -> contenido)
    public static Map<String, String> generateAllTemplates(Template template) {
        Map<String, String> filesMap = new HashMap<>();
        // Usamos el parámetro true para exportar
        filesMap.put("login.html", generateLoginHtml(template, true));
        filesMap.put("login.css", generateLoginCss(template));
        // Archivos adicionales
        filesMap.put("error.html", "<html><head><title>Error</title></head><body><p>Error: {error}</p></body></html>");
        filesMap.put("logout.html", "<html><head><title>Logout</title></head><body><p>Sesión cerrada.</p></body></html>");
        filesMap.put("alogin.html", "<html><head><title>Alternate Login</title></head><body><p>Página de login alternativo.</p></body></html>");
        filesMap.put("radvert.html", "<html><head><title>Advertisement</title></head><body><p>Publicidad.</p></body></html>");
        filesMap.put("redirect.html", "<html><head><meta http-equiv='refresh' content='0; url={redirectUrl}' /></head><body></body></html>");
        filesMap.put("rlogin.html", "<html><head><title>Redirect Login</title></head><body><p>Página de redirección.</p></body></html>");
        filesMap.put("status.html", "<html><head><title>Status</title></head><body><p>Estado de la conexión.</p></body></html>");
        return filesMap;
    }
}
