package com.jmanuel.mikroficha;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.util.HashMap;
import java.util.Map;

public class TemplateGenerator {

    // Genera el HTML del login a partir del objeto Template, incrustando el CSS generado inline.
    public static String generateLoginHtml(Template template, boolean isExport) {
        // Genera el CSS actual
        String css = generateLoginCss(template, isExport);

        // Extrae la extensión y usa "logo.<ext>" al exportar
        String logoSource;
        if (template.getLogoUri().isEmpty()) {
            logoSource = "default_logo.png";
        } else {
            String logoExt = getExtensionFromUri(template.getLogoUri());
            logoSource = isExport ? "logo." + logoExt          // ← aquí sí se añade .png, .jpg…
                    : template.getLogoUri();      // ← preview: se usa la URI tal cual
        }

        // Construye el HTML con placeholders de MikroTik + tu plantilla
        // Observa que aquí van las directivas $(if chap-id), etc.
        // y un <link rel="stylesheet" href="login.css"> para usar el CSS generado.
        // También agregamos md5.js (necesario para CHAP).
        // Ajusta el <form> para que use tus estilos (ej. #box) y placeholders de usuario/contraseña.

        String html =
                "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "  <meta charset=\"utf-8\">\n" +
                        "  <meta http-equiv=\"pragma\" content=\"no-cache\" />\n" +
                        "  <meta http-equiv=\"expires\" content=\"-1\" />\n" +
                        "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0; maximum-scale=1.0; user-scalable=0\" />\n" +
                        "  <title>" + template.getTitleText() + "</title>\n" +
                        "  <style>\n" +
                        css + "\n" +  // Incluimos el CSS directamente (o podrías usar <link ...>)
                        "    /* Contenedor flexible extra */\n" +
                        "    .flex-container {\n" +
                        "      display: flex;\n" +
                        "      flex-wrap: wrap;\n" +
                        "      gap: 20px;\n" +
                        "      justify-content: center;\n" +
                        "      margin-top: 20px;\n" +
                        "    }\n" +
                        "  </style>\n" +
                        "</head>\n" +
                        "<body class='login'>\n" +

                        "$(if chap-id)\n" +
                        "  <form name=\"sendin\" action=\"$(link-login-only)\" method=\"post\">\n" +
                        "    <input type=\"hidden\" name=\"username\" />\n" +
                        "    <input type=\"hidden\" name=\"password\" />\n" +
                        "    <input type=\"hidden\" name=\"dst\" value=\"$(link-orig)\" />\n" +
                        "    <input type=\"hidden\" name=\"popup\" value=\"true\" />\n" +
                        "  </form>\n" +
                        "  <script type=\"text/javascript\" src=\"md5.js\"></script>\n" +
                        "  <script type=\"text/javascript\">\n" +
                        "    function doLogin() {\n" +
                        "      document.sendin.username.value = document.login.username.value;\n" +
                        "      document.sendin.password.value = hexMD5('$(chap-id)' + document.login.password.value + '$(chap-challenge)');\n" +
                        "      document.sendin.submit();\n" +
                        "      return false;\n" +
                        "    }\n" +
                        "  </script>\n" +
                        "$(endif)\n" +

                        "<!-- Form principal -->\n" +
                        "<form class=\"vertical-form\" name=\"login\" action=\"$(link-login-only)\" method=\"post\" \n" +
                        "      $(if chap-id) onSubmit=\"return doLogin()\" $(endif)>\n" +
                        "  <input type=\"hidden\" name=\"dst\" value=\"$(link-orig)\" />\n" +
                        "  <input type=\"hidden\" name=\"popup\" value=\"true\" />\n" +
                        "\n" +
                        "  <center>\n" +
                        "    <div id=\"box\" style=\"margin-top: " + template.getVerticalOffset() + ";\">\n" +
                        "      <!-- Logo -->\n" +
                        "      <img src=\"" + logoSource + "\" \n" +
                        "           alt=\"Logo\" \n" +
                        "           style=\"width:" + template.getLogoWidth() + "; height:" + template.getLogoHeight() + "; border-radius:" + template.getLogoBorderRadius() + ";\" />\n" +
                        "\n" +
                        "      <!-- Título -->\n" +
                        "      <h1>" + template.getTitleText() + "</h1>\n" +
                        "\n" +
                        "      <!-- Inputs -->\n" +
                        "      <input id=\"user\" \n" +
                        "             autocomplete=\"on\" \n" +
                        "             name=\"username\" \n" +
                        "             type=\"text\" \n" +
                        "             value=\"$(username)\" \n" +
                        "             placeholder=\"" + template.getUsernameHint() + "\" />\n" +
                        "\n";

// Si la contraseña NO está oculta, agregamos el input password
        if (!template.isHidePassword()) {
            html +=
                    "      <input id=\"pass\" \n" +
                            "             autocomplete=\"off\" \n" +
                            "             name=\"password\" \n" +
                            "             type=\"password\" \n" +
                            "             label=\"false\" \n" +
                            "             placeholder=\"" + template.getPasswordHint() + "\" />\n";
        }

// Botón
        html +=
                "      <button type=\"submit\">" + template.getButtonText() + "</button>\n" +
                        "\n" +
                        "      <!-- Footer / info adicional -->\n" +
                        "      <footer>" + template.getFooterText() + "</footer>\n" +
                        "    </div> <!-- /#box -->\n" +
                        "  </center>\n" +

                        "  <!-- contenedor flexible (si quieres agregar algo) -->\n" +
                        "  <div class=\"flex-container\">\n" +
                        "    <!-- Aquí el usuario puede agregar contenido y se ajustará -->\n" +
                        "  </div>\n" +
                        "\n" +
                        "  <!-- Sección trial, error, etc. -->\n" +
                        "  <div class='footer'>\n" +
                        "    <p>$(if trial == 'yes') Prueba gratis disponible, <a href=\"$(link-login-only)?dst=$(link-orig-esc)&amp;username=T-$(mac-esc)\">Aceptar</a>.$(endif)</p>\n" +
                        "    <p>$(if error)<span style=\"color:#ffffff;font-size:16px\">$(error)</span>$(endif)</p>\n" +
                        "  </div>\n" +
                        "</form>\n" +

                        "<!-- Forzar focus en el campo username -->\n" +
                        "<script type=\"text/javascript\">\n" +
                        "  document.login.username.focus();\n" +
                        "</script>\n" +
                        "\n" +
                        "<!-- Desactivar menú contextual si lo deseas -->\n" +
                        "<script type=\"text/javascript\">\n" +
                        "  document.oncontextmenu = function(){return false;}\n" +
                        "</script>\n" +
                        "\n" +
                        "</body>\n" +
                        "</html>\n";

        return html;
    }

    // Genera todos los archivos de la plantilla en un Map (nombre del archivo -> contenido)
    public static Map<String, String> generateAllTemplates(Template template) {
        Map<String, String> filesMap = new HashMap<>();

        // 1. archivos "dinámicos"
        filesMap.put("login.html", generateLoginHtml(template, true));
        filesMap.put("login.css", generateLoginCss(template, true));

        // 2. archivos estáticos con su contenido literal
        filesMap.put("status.html",
                "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "<title>mikrotik hotspot > status</title>\n" +
                        "$(if refresh-timeout)\n" +
                        "<meta http-equiv=\"refresh\" content=\"$(refresh-timeout-secs)\">\n" +
                        "$(endif)\n" +
                        "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">\n" +
                        "<meta http-equiv=\"pragma\" content=\"no-cache\">\n" +
                        "<meta http-equiv=\"expires\" content=\"-1\">\n" +
                        "<style type=\"text/css\">\n" +
                        "<!--\n" +
                        "textarea,input,select {\n" +
                        "	background-color: #FDFBFB;\n" +
                        "	border: 1px #BBBBBB solid;\n" +
                        "	padding: 2px;\n" +
                        "	margin: 1px;\n" +
                        "	font-size: 14px;\n" +
                        "	color: #808080;\n" +
                        "}\n" +
                        ".tabula{\n" +
                        " border-width: 1px; \n" +
                        " border-collapse: collapse; \n" +
                        " border-color: #c1c1c1; \n" +
                        " background-color: transparent;\n" +
                        " font-family: verdana;\n" +
                        " font-size: 11px;\n" +
                        "}\n" +
                        "body{ color: #737373; font-size: 12px; font-family: verdana; }\n" +
                        "a, a:link, a:visited, a:active { color: #AAAAAA; text-decoration: none; font-size: 12px; }\n" +
                        "a:hover { border-bottom: 1px dotted #c1c1c1; color: #AAAAAA; }\n" +
                        "img {border: none;}\n" +
                        "td { font-size: 12px; padding: 4px;}\n" +
                        "-->\n" +
                        "</style>\n" +
                        "<script language=\"JavaScript\">\n" +
                        "<!--\n" +
                        "$(if advert-pending == 'yes')\n" +
                        "    var popup = '';\n" +
                        "    function focusAdvert() {\n" +
                        "	if (window.focus) popup.focus();\n" +
                        "    }\n" +
                        "    function openAdvert() {\n" +
                        "	popup = open('$(link-advert)', 'hotspot_advert', '');\n" +
                        "	setTimeout(\"focusAdvert()\", 1000);\n" +
                        "    }\n" +
                        "$(endif)\n" +
                        "    function openLogout() {\n" +
                        "	if (window.name != 'hotspot_status') return true;\n" +
                        "        open('$(link-logout)', 'hotspot_logout', 'toolbar=0,location=0,directories=0,status=0,menubars=0,resizable=1,width=280,height=250');\n" +
                        "	window.close();\n" +
                        "	return false;\n" +
                        "    }\n" +
                        "//-->\n" +
                        "</script>\n" +
                        "</head>\n" +
                        "<body bottommargin=\"0\" topmargin=\"0\" leftmargin=\"0\" rightmargin=\"0\"\n" +
                        "$(if advert-pending == 'yes')\n" +
                        "	onLoad=\"openAdvert()\"\n" +
                        "$(endif)\n" +
                        ">\n" +
                        "<table width=\"100%\" height=\"100%\">\n" +
                        "\n" +
                        "<tr>\n" +
                        "<td align=\"center\" valign=\"middle\">\n" +
                        "<form action=\"$(link-logout)\" name=\"logout\" onSubmit=\"return openLogout()\">\n" +
                        "<table border=\"1\" class=\"tabula\">\n" +
                        "$(if login-by == 'trial')\n" +
                        "	<br><div style=\"text-align: center;\">Welcome trial user!</div><br>\n" +
                        "$(elif login-by != 'mac')\n" +
                        "	<br><div style=\"text-align: center;\">Welcome $(username)!</div><br>\n" +
                        "$(endif)\n" +
                        "	<tr><td align=\"right\">IP address:</td><td>$(ip)</td></tr>\n" +
                        "	<tr><td align=\"right\">bytes up/down:</td><td>$(bytes-in-nice) / $(bytes-out-nice)</td></tr>\n" +
                        "$(if session-time-left)\n" +
                        "	<tr><td align=\"right\">connected / left:</td><td>$(uptime) / $(session-time-left)</td></tr>\n" +
                        "$(else)\n" +
                        "	<tr><td align=\"right\">connected:</td><td>$(uptime)</td></tr>\n" +
                        "$(endif)\n" +
                        "$(if blocked == 'yes')\n" +
                        "	<tr><td align=\"right\">status:</td><td><div style=\"color: #FF8080\">\n" +
                        "<a href=\"$(link-advert)\" target=\"hotspot_advert\">advertisement</a> required</div></td>\n" +
                        "$(elif refresh-timeout)\n" +
                        "	<tr><td align=\"right\">status refresh:</td><td>$(refresh-timeout)</td>\n" +
                        "$(endif)\n" +
                        "\n" +
                        "</table>\n" +
                        "$(if login-by-mac != 'yes')\n" +
                        "<br>\n" +
                        "<!-- user manager link. if user manager resides on other router, replace $(hostname) by its address -->\n" +
                        "<!-- <button onclick=\"document.location='http://$(hostname)/user?subs='; return false;\">status</button> -->\n" +
                        "<!-- end of user manager link -->\n" +
                        "<input type=\"submit\" value=\"log off\">\n" +
                        "$(endif)\n" +
                        "</form>\n" +
                        "\n" +
                        "</td>\n" +
                        "</table>\n" +
                        "</body>\n" +
                        "</html>"
        );

        filesMap.put("md5.js",
                "/*\n" +
                        " * A JavaScript implementation of the RSA Data Security, Inc. MD5 Message\n" +
                        " * Digest Algorithm, as defined in RFC 1321.\n" +
                        " * Version 1.1 Copyright (C) Paul Johnston 1999 - 2002.\n" +
                        " * Code also contributed by Greg Holt\n" +
                        " * See http://pajhome.org.uk/site/legal.html for details.\n" +
                        " */\n" +
                        "\n" +
                        "/*\n" +
                        " * Add integers, wrapping at 2^32. This uses 16-bit operations internally\n" +
                        " * to work around bugs in some JS interpreters.\n" +
                        " */\n" +
                        "function safe_add(x, y)\n" +
                        "{\n" +
                        "  var lsw = (x & 0xFFFF) + (y & 0xFFFF)\n" +
                        "  var msw = (x >> 16) + (y >> 16) + (lsw >> 16)\n" +
                        "  return (msw << 16) | (lsw & 0xFFFF)\n" +
                        "}\n" +
                        "\n" +
                        "/*\n" +
                        " * Bitwise rotate a 32-bit number to the left.\n" +
                        " */\n" +
                        "function rol(num, cnt)\n" +
                        "{\n" +
                        "  return (num << cnt) | (num >>> (32 - cnt))\n" +
                        "}\n" +
                        "\n" +
                        "/*\n" +
                        " * These functions implement the four basic operations the algorithm uses.\n" +
                        " */\n" +
                        "function cmn(q, a, b, x, s, t)\n" +
                        "{\n" +
                        "  return safe_add(rol(safe_add(safe_add(a, q), safe_add(x, t)), s), b)\n" +
                        "}\n" +
                        "function ff(a, b, c, d, x, s, t)\n" +
                        "{\n" +
                        "  return cmn((b & c) | ((~b) & d), a, b, x, s, t)\n" +
                        "}\n" +
                        "function gg(a, b, c, d, x, s, t)\n" +
                        "{\n" +
                        "  return cmn((b & d) | (c & (~d)), a, b, x, s, t)\n" +
                        "}\n" +
                        "function hh(a, b, c, d, x, s, t)\n" +
                        "{\n" +
                        "  return cmn(b ^ c ^ d, a, b, x, s, t)\n" +
                        "}\n" +
                        "function ii(a, b, c, d, x, s, t)\n" +
                        "{\n" +
                        "  return cmn(c ^ (b | (~d)), a, b, x, s, t)\n" +
                        "}\n" +
                        "\n" +
                        "/*\n" +
                        " * Calculate the MD5 of an array of little-endian words, producing an array\n" +
                        " * of little-endian words.\n" +
                        " */\n" +
                        "function coreMD5(x)\n" +
                        "{\n" +
                        "  var a =  1732584193\n" +
                        "  var b = -271733879\n" +
                        "  var c = -1732584194\n" +
                        "  var d =  271733878\n" +
                        "\n" +
                        "  for(i = 0; i < x.length; i += 16)\n" +
                        "  {\n" +
                        "    var olda = a\n" +
                        "    var oldb = b\n" +
                        "    var oldc = c\n" +
                        "    var oldd = d\n" +
                        "\n" +
                        "    a = ff(a, b, c, d, x[i+ 0], 7 , -680876936)\n" +
                        "    d = ff(d, a, b, c, x[i+ 1], 12, -389564586)\n" +
                        "    c = ff(c, d, a, b, x[i+ 2], 17,  606105819)\n" +
                        "    b = ff(b, c, d, a, x[i+ 3], 22, -1044525330)\n" +
                        "    a = ff(a, b, c, d, x[i+ 4], 7 , -176418897)\n" +
                        "    d = ff(d, a, b, c, x[i+ 5], 12,  1200080426)\n" +
                        "    c = ff(c, d, a, b, x[i+ 6], 17, -1473231341)\n" +
                        "    b = ff(b, c, d, a, x[i+ 7], 22, -45705983)\n" +
                        "    a = ff(a, b, c, d, x[i+ 8], 7 ,  1770035416)\n" +
                        "    d = ff(d, a, b, c, x[i+ 9], 12, -1958414417)\n" +
                        "    c = ff(c, d, a, b, x[i+10], 17, -42063)\n" +
                        "    b = ff(b, c, d, a, x[i+11], 22, -1990404162)\n" +
                        "    a = ff(a, b, c, d, x[i+12], 7 ,  1804603682)\n" +
                        "    d = ff(d, a, b, c, x[i+13], 12, -40341101)\n" +
                        "    c = ff(c, d, a, b, x[i+14], 17, -1502002290)\n" +
                        "    b = ff(b, c, d, a, x[i+15], 22,  1236535329)\n" +
                        "\n" +
                        "    a = gg(a, b, c, d, x[i+ 1], 5 , -165796510)\n" +
                        "    d = gg(d, a, b, c, x[i+ 6], 9 , -1069501632)\n" +
                        "    c = gg(c, d, a, b, x[i+11], 14,  643717713)\n" +
                        "    b = gg(b, c, d, a, x[i+ 0], 20, -373897302)\n" +
                        "    a = gg(a, b, c, d, x[i+ 5], 5 , -701558691)\n" +
                        "    d = gg(d, a, b, c, x[i+10], 9 ,  38016083)\n" +
                        "    c = gg(c, d, a, b, x[i+15], 14, -660478335)\n" +
                        "    b = gg(b, c, d, a, x[i+ 4], 20, -405537848)\n" +
                        "    a = gg(a, b, c, d, x[i+ 9], 5 ,  568446438)\n" +
                        "    d = gg(d, a, b, c, x[i+14], 9 , -1019803690)\n" +
                        "    c = gg(c, d, a, b, x[i+ 3], 14, -187363961)\n" +
                        "    b = gg(b, c, d, a, x[i+ 8], 20,  1163531501)\n" +
                        "    a = gg(a, b, c, d, x[i+13], 5 , -1444681467)\n" +
                        "    d = gg(d, a, b, c, x[i+ 2], 9 , -51403784)\n" +
                        "    c = gg(c, d, a, b, x[i+ 7], 14,  1735328473)\n" +
                        "    b = gg(b, c, d, a, x[i+12], 20, -1926607734)\n" +
                        "\n" +
                        "    a = hh(a, b, c, d, x[i+ 5], 4 , -378558)\n" +
                        "    d = hh(d, a, b, c, x[i+ 8], 11, -2022574463)\n" +
                        "    c = hh(c, d, a, b, x[i+11], 16,  1839030562)\n" +
                        "    b = hh(b, c, d, a, x[i+14], 23, -35309556)\n" +
                        "    a = hh(a, b, c, d, x[i+ 1], 4 , -1530992060)\n" +
                        "    d = hh(d, a, b, c, x[i+ 4], 11,  1272893353)\n" +
                        "    c = hh(c, d, a, b, x[i+ 7], 16, -155497632)\n" +
                        "    b = hh(b, c, d, a, x[i+10], 23, -1094730640)\n" +
                        "    a = hh(a, b, c, d, x[i+13], 4 ,  681279174)\n" +
                        "    d = hh(d, a, b, c, x[i+ 0], 11, -358537222)\n" +
                        "    c = hh(c, d, a, b, x[i+ 3], 16, -722521979)\n" +
                        "    b = hh(b, c, d, a, x[i+ 6], 23,  76029189)\n" +
                        "    a = hh(a, b, c, d, x[i+ 9], 4 , -640364487)\n" +
                        "    d = hh(d, a, b, c, x[i+12], 11, -421815835)\n" +
                        "    c = hh(c, d, a, b, x[i+15], 16,  530742520)\n" +
                        "    b = hh(b, c, d, a, x[i+ 2], 23, -995338651)\n" +
                        "\n" +
                        "    a = ii(a, b, c, d, x[i+ 0], 6 , -198630844)\n" +
                        "    d = ii(d, a, b, c, x[i+ 7], 10,  1126891415)\n" +
                        "    c = ii(c, d, a, b, x[i+14], 15, -1416354905)\n" +
                        "    b = ii(b, c, d, a, x[i+ 5], 21, -57434055)\n" +
                        "    a = ii(a, b, c, d, x[i+12], 6 ,  1700485571)\n" +
                        "    d = ii(d, a, b, c, x[i+ 3], 10, -1894986606)\n" +
                        "    c = ii(c, d, a, b, x[i+10], 15, -1051523)\n" +
                        "    b = ii(b, c, d, a, x[i+ 1], 21, -2054922799)\n" +
                        "    a = ii(a, b, c, d, x[i+ 8], 6 ,  1873313359)\n" +
                        "    d = ii(d, a, b, c, x[i+15], 10, -30611744)\n" +
                        "    c = ii(c, d, a, b, x[i+ 6], 15, -1560198380)\n" +
                        "    b = ii(b, c, d, a, x[i+13], 21,  1309151649)\n" +
                        "    a = ii(a, b, c, d, x[i+ 4], 6 , -145523070)\n" +
                        "    d = ii(d, a, b, c, x[i+11], 10, -1120210379)\n" +
                        "    c = ii(c, d, a, b, x[i+ 2], 15,  718787259)\n" +
                        "    b = ii(b, c, d, a, x[i+ 9], 21, -343485551)\n" +
                        "\n" +
                        "    a = safe_add(a, olda)\n" +
                        "    b = safe_add(b, oldb)\n" +
                        "    c = safe_add(c, oldc)\n" +
                        "    d = safe_add(d, oldd)\n" +
                        "  }\n" +
                        "  return [a, b, c, d]\n" +
                        "}\n" +
                        "\n" +
                        "/*\n" +
                        " * Convert an array of little-endian words to a hex string.\n" +
                        " */\n" +
                        "function binl2hex(binarray)\n" +
                        "{\n" +
                        "  var hex_tab = \"0123456789abcdef\"\n" +
                        "  var str = \"\"\n" +
                        "  for(var i = 0; i < binarray.length * 4; i++)\n" +
                        "  {\n" +
                        "    str += hex_tab.charAt((binarray[i>>2] >> ((i%4)*8+4)) & 0xF) +\n" +
                        "           hex_tab.charAt((binarray[i>>2] >> ((i%4)*8)) & 0xF)\n" +
                        "  }\n" +
                        "  return str\n" +
                        "}\n" +
                        "\n" +
                        "/*\n" +
                        " * Convert an array of little-endian words to a base64 encoded string.\n" +
                        " */\n" +
                        "function binl2b64(binarray)\n" +
                        "{\n" +
                        "  var tab = \"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/\"\n" +
                        "  var str = \"\"\n" +
                        "  for(var i = 0; i < binarray.length * 32; i += 6)\n" +
                        "  {\n" +
                        "    str += tab.charAt(((binarray[i>>5] << (i%32)) & 0x3F) |\n" +
                        "                      ((binarray[i>>5+1] >> (32-i%32)) & 0x3F))\n" +
                        "  }\n" +
                        "  return str\n" +
                        "}\n" +
                        "\n" +
                        "/*\n" +
                        " * Convert an 8-bit character string to a sequence of 16-word blocks, stored\n" +
                        " * as an array, and append appropriate padding for MD4/5 calculation.\n" +
                        " * If any of the characters are >255, the high byte is silently ignored.\n" +
                        " */\n" +
                        "function str2binl(str)\n" +
                        "{\n" +
                        "  var nblk = ((str.length + 8) >> 6) + 1 // number of 16-word blocks\n" +
                        "  var blks = new Array(nblk * 16)\n" +
                        "  for(var i = 0; i < nblk * 16; i++) blks[i] = 0\n" +
                        "  for(var i = 0; i < str.length; i++)\n" +
                        "    blks[i>>2] |= (str.charCodeAt(i) & 0xFF) << ((i%4) * 8)\n" +
                        "  blks[i>>2] |= 0x80 << ((i%4) * 8)\n" +
                        "  blks[nblk*16-2] = str.length * 8\n" +
                        "  return blks\n" +
                        "}\n" +
                        "\n" +
                        "/*\n" +
                        " * Convert a wide-character string to a sequence of 16-word blocks, stored as\n" +
                        " * an array, and append appropriate padding for MD4/5 calculation.\n" +
                        " */\n" +
                        "function strw2binl(str)\n" +
                        "{\n" +
                        "  var nblk = ((str.length + 4) >> 5) + 1 // number of 16-word blocks\n" +
                        "  var blks = new Array(nblk * 16)\n" +
                        "  for(var i = 0; i < nblk * 16; i++) blks[i] = 0\n" +
                        "  for(var i = 0; i < str.length; i++)\n" +
                        "    blks[i>>1] |= str.charCodeAt(i) << ((i%2) * 16)\n" +
                        "  blks[i>>1] |= 0x80 << ((i%2) * 16)\n" +
                        "  blks[nblk*16-2] = str.length * 16\n" +
                        "  return blks\n" +
                        "}\n" +
                        "\n" +
                        "/*\n" +
                        " * External interface\n" +
                        " */\n" +
                        "function hexMD5 (str) { return binl2hex(coreMD5( str2binl(str))) }\n" +
                        "function hexMD5w(str) { return binl2hex(coreMD5(strw2binl(str))) }\n" +
                        "function b64MD5 (str) { return binl2b64(coreMD5( str2binl(str))) }\n" +
                        "function b64MD5w(str) { return binl2b64(coreMD5(strw2binl(str))) }\n" +
                        "/* Backward compatibility */\n" +
                        "function calcMD5(str) { return binl2hex(coreMD5( str2binl(str))) }\n"
        );



        filesMap.put("alogin.html",
                "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"><html><head><style> .loader {margin: 60px auto;font-size: 10px;position: relative;text-indent: -9999em;border-top: 1.1em solid rgba(145,145,146, 0.2);border-right: 1.1em solid rgba(145,145,146, 0.2);border-bottom: 1.1em solid rgba(145,145,146, 0.2);border-left: 1.1em solid #919192;-webkit-transform: translateZ(0);-ms-transform: translateZ(0);transform: translateZ(0);-webkit-animation: load8 1.1s infinite linear;animation: load8 1.1s infinite linear;}.loader,.loader:after {border-radius: 50%;width: 10em;height: 10em;}@-webkit-keyframes load8 {0% {-webkit-transform: rotate(0deg);transform: rotate(0deg);}100% {-webkit-transform: rotate(360deg);transform: rotate(360deg);}}@keyframes load8 {0% {-webkit-transform: rotate(0deg);transform: rotate(0deg);}100% {-webkit-transform: rotate(360deg);transform: rotate(360deg);}}</style><title>Conectando... &gt; redirect</title><meta http-equiv=\"refresh\" content=\"4; url=$(link-redirect)\"><meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\"><meta http-equiv=\"pragma\" content=\"no-cache\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><meta http-equiv=\"expires\" content=\"-1\"><style type=\"text/css\">textarea,input,select {background-color: #FDFBFB;border: 1px #BBBBBB solid;padding: 2px;margin: 1px;font-size: 14px;color: #808080;}body{ color: #737373; font-size: 12px; font-family: verdana; }a, a:link, a:visited, a:active { color: #AAAAAA; text-decoration: none; font-size: 12px; }a:hover { border-bottom: 1px dotted #c1c1c1; color: #AAAAAA; }img {border: none;}td { font-size: 12px; color: #7A7A7A; }</style><script language=\"JavaScript\">function startClock() {$(if popup == 'true')open('$(link-status)', 'hotspot_status', 'toolbar=0,location=0,directories=0,status=0,menubars=0,resizable=1,width=290');$(endif)location.href = 'https://www.google.com.mx/',500;}</script></head><body style=\"width: 100%;\" onload=\"startClock()\"><table style=\"background-color: transparent; width: 100%; margin-left: 0px; height: 100%;\"><tbody><tr><td style=\"vertical-align: middle; background-color: white; text-align: center; margin-top: 20%; height: 100%;\"><div class=\"loader\">Cargando...</div></td></tr></tbody></table></body></html>"
        );

        filesMap.put("error.html",
                "<html>\n" +
                        "<head>\n" +
                        "<title>mikrotik hotspot > error</title>\n" +
                        "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">\n" +
                        "<meta http-equiv=\"pragma\" content=\"no-cache\">\n" +
                        "<meta http-equiv=\"expires\" content=\"-1\">\n" +
                        "<style type=\"text/css\">\n" +
                        "<!--\n" +
                        "textarea,input,select {\n" +
                        "\tbackground-color: #FDFBFB;\n" +
                        "\tborder: 1px #BBBBBB solid;\n" +
                        "\tpadding: 2px;\n" +
                        "\tmargin: 1px;\n" +
                        "\tfont-size: 14px;\n" +
                        "\tcolor: #808080;\n" +
                        "}\n" +
                        "\n" +
                        "body{ color: #737373; font-size: 12px; font-family: verdana; }\n" +
                        "\n" +
                        "a, a:link, a:visited, a:active { color: #AAAAAA; text-decoration: none; font-size: 12px; }\n" +
                        "a:hover { border-bottom: 1px dotted #c1c1c1; color: #AAAAAA; }\n" +
                        "img {border: none;}\n" +
                        "td { font-size: 12px; color: #7A7A7A; }\n" +
                        "\n" +
                        "-->\n" +
                        "</style>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "<table width=\"100%\" height=\"100%\">\n" +
                        "\n" +
                        "<tr>\n" +
                        "<td align=\"center\" valign=\"middle\">\n" +
                        "Hotspot ERROR: $(error)<br>\n" +
                        "<br>\n" +
                        "Login page: <a href=\"$(link-login)\">$(link-login)</a>\n" +
                        "</td>\n" +
                        "</tr>\n" +
                        "</table>\n" +
                        "</body>\n" +
                        "</html>\n"
        );

        filesMap.put("logout.html",
                "<html>\n" +
                        "<head>\n" +
                        "<title>mikrotik hotspot > logout</title>\n" +
                        "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">\n" +
                        "<meta http-equiv=\"pragma\" content=\"no-cache\">\n" +
                        "<meta http-equiv=\"expires\" content=\"-1\">\n" +
                        "<style type=\"text/css\">\n" +
                        "<!--\n" +
                        "textarea,input,select {\n" +
                        "\tbackground-color: #FDFBFB;\n" +
                        "\tborder: 1px #BBBBBB solid;\n" +
                        "\tpadding: 2px;\n" +
                        "\tmargin: 1px;\n" +
                        "\tfont-size: 14px;\n" +
                        "\tcolor: #808080;\n" +
                        "}\n" +
                        "\n" +
                        ".tabula{\n" +
                        " \n" +
                        "border-width: 1px; \n" +
                        "border-collapse: collapse; \n" +
                        "border-color: #c1c1c1; \n" +
                        "background-color: transparent;\n" +
                        "font-family: verdana;\n" +
                        "font-size: 11px;\n" +
                        "}\n" +
                        "\n" +
                        "body{ color: #737373; font-size: 12px; font-family: verdana; }\n" +
                        "\n" +
                        "a, a:link, a:visited, a:active { color: #AAAAAA; text-decoration: none; font-size: 12px; }\n" +
                        "a:hover { border-bottom: 1px dotted #c1c1c1; color: #AAAAAA; }\n" +
                        "img {border: none;}\n" +
                        "td { font-size: 12px; padding: 4px;}\n" +
                        "\n" +
                        "-->\n" +
                        "</style>\n" +
                        "</head>\n" +
                        "\n" +
                        "<body>\n" +
                        "<script language=\"JavaScript\">\n" +
                        "<!--\n" +
                        "    function openLogin() {\n" +
                        "\tif (window.name != 'hotspot_logout') return true;\n" +
                        "\topen('$(link-login)', '_blank', '');\n" +
                        "\twindow.close();\n" +
                        "\treturn false;\n" +
                        "    }\n" +
                        "//-->\n" +
                        "</script>\n" +
                        "\n" +
                        "<table width=\"100%\" height=\"100%\">\n" +
                        "\n" +
                        "<tr>\n" +
                        "<td align=\"center\" valign=\"middle\">\n" +
                        "<b>you have just logged out</b> <br><br>\n" +
                        "<table class=\"tabula\" border=\"1\">  \n" +
                        "<tr><td align=\"right\">user name</td><td>$(username)</td></tr>\n" +
                        "<tr><td align=\"right\">IP address</td><td>$(ip)</td></tr>\n" +
                        "<tr><td align=\"right\">MAC address</td><td>$(mac)</td></tr>\n" +
                        "<tr><td align=\"right\">session time</td><td>$(uptime)</td></tr>\n" +
                        "$(if session-time-left)\n" +
                        "<tr><td align=\"right\">time left</td><td>$(session-time-left)</td></tr>\n" +
                        "$(endif)\n" +
                        "<tr><td align=\"right\">bytes up/down:</td><td>$(bytes-in-nice) / $(bytes-out-nice)</td></tr>\n" +
                        "</table>\n" +
                        "<br>\n" +
                        "<form action=\"$(link-login)\" name=\"login\" onSubmit=\"return openLogin()\">\n" +
                        "<input type=\"submit\" value=\"log in\">\n" +
                        "</form>\n" +
                        "</td>\n" +
                        "</table>\n" +
                        "</body>\n" +
                        "</html>\n"
        );

        filesMap.put("radvert.html",
                "<html>\n" +
                        "<head>\n" +
                        "<title>mikrotik hotspot > advertisement</title>\n" +
                        "<meta http-equiv=\"refresh\" content=\"2; url=$(link-orig)\">\n" +
                        "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">\n" +
                        "<meta http-equiv=\"pragma\" content=\"no-cache\">\n" +
                        "<meta http-equiv=\"expires\" content=\"-1\">\n" +
                        "<style type=\"text/css\">\n" +
                        "<!--\n" +
                        "textarea,input,select {\n" +
                        "\tbackground-color: #FDFBFB;\n" +
                        "\tborder: 1px #BBBBBB solid;\n" +
                        "\tpadding: 2px;\n" +
                        "\tmargin: 1px;\n" +
                        "\tfont-size: 14px;\n" +
                        "\tcolor: #808080;\n" +
                        "}\n" +
                        "\n" +
                        "body{ color: #737373; font-size: 12px; font-family: verdana; }\n" +
                        "\n" +
                        "a, a:link, a:visited, a:active { color: #AAAAAA; text-decoration: none; font-size: 12px; }\n" +
                        "a:hover { border-bottom: 1px dotted #c1c1c1; color: #AAAAAA; }\n" +
                        "img {border: none;}\n" +
                        "td { font-size: 12px; color: #7A7A7A; }\n" +
                        "\n" +
                        "-->\n" +
                        "</style>\n" +
                        "<script language=\"JavaScript\">\n" +
                        "<!--\n" +
                        "    var popup = '';\n" +
                        "    function openOrig() {\n" +
                        "\tif (window.focus) popup.focus();\n" +
                        "\tlocation.href = '$(link-orig)';\n" +
                        "    }\n" +
                        "    function openAd() {\n" +
                        "\tlocation.href = '$(link-redirect)';\n" +
                        "    }\n" +
                        "    function openAdvert() {\n" +
                        "\tif (window.name != 'hotspot_advert') {\n" +
                        "\t\tpopup = open('$(link-redirect)', 'hotspot_advert', '');\n" +
                        "\t\tsetTimeout(\"openOrig()\", 1000);\n" +
                        "\t\treturn;\n" +
                        "\t}\n" +
                        "\tsetTimeout(\"openAd()\", 1000);\n" +
                        "    }\n" +
                        "//-->\n" +
                        "</script>\n" +
                        "</head>\n" +
                        "<body onLoad=\"openAdvert()\">\n" +
                        "<table width=\"100%\" height=\"100%\">\n" +
                        "<tr>\n" +
                        "\t<td align=\"center\" valign=\"middle\">\n" +
                        "\tAdvertisement.\n" +
                        "\t<br><br>\n" +
                        "\tIf nothing happens, open\n" +
                        "\t<a href=\"$(link-redirect)\" target=\"hotspot_advert\">advertisement</a>\n" +
                        "\tmanually.\n" +
                        "\t</td>\n" +
                        "</tr>\n" +
                        "</table>\n" +
                        "</body>\n" +
                        "</html>\n"
        );

        filesMap.put("redirect.html",
                "$(if http-status == 302)Hotspot redirect$(endif)\n" +
                        "$(if http-header == \"Location\")$(link-redirect)$(endif)\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "<title>...</title>\n" +
                        "<meta http-equiv=\"refresh\" content=\"0; url=$(link-redirect)\">\n" +
                        "<meta http-equiv=\"pragma\" content=\"no-cache\">\n" +
                        "<meta http-equiv=\"expires\" content=\"-1\">\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "</body>\n" +
                        "</html>\n"
        );

        filesMap.put("rlogin.html",
                "$(if http-status == 302)Hotspot login required$(endif)\n" +
                        "$(if http-header == \"Location\")$(link-redirect)$(endif)\n" +
                        "<html>\n" +
                        "<!--\n" +
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "  <WISPAccessGatewayParam\n" +
                        "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                        "  xsi:noNamespaceSchemaLocation=\"http://$(hostname)/xml/WISPAccessGatewayParam.xsd\">\n" +
                        "    <Redirect>\n" +
                        "\t<AccessProcedure>1.0</AccessProcedure>\n" +
                        "\t<AccessLocation>$(location-id)</AccessLocation>\n" +
                        "\t<LocationName>$(location-name)</LocationName>\n" +
                        "\t<LoginURL>$(link-login-only)?target=xml</LoginURL>\n" +
                        "\t<MessageType>100</MessageType>\n" +
                        "\t<ResponseCode>0</ResponseCode>\n" +
                        "    </Redirect>\n" +
                        "  </WISPAccessGatewayParam>\n" +
                        "-->\n" +
                        "<head>\n" +
                        "<title>...</title>\n" +
                        "<meta http-equiv=\"refresh\" content=\"0; url=$(link-redirect)\">\n" +
                        "<meta http-equiv=\"pragma\" content=\"no-cache\">\n" +
                        "<meta http-equiv=\"expires\" content=\"-1\">\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "</body>\n" +
                        "</html>\n"
        );

        return filesMap;
    }


    // Genera el CSS del login basado en los parámetros del Template
    public static String generateLoginCss(Template template, boolean isExport) {
        StringBuilder css = new StringBuilder();

        // Estilos generales para el body, centrando el contenido
        // Fondo del body:
        css.append("body { ");
        if (template.getBackgroundType().equals("IMAGE") && !template.getBackgroundImageUri().isEmpty()) {
            // En exportación se usará "background.png"; en preview la URI real

            // Extrae la extensión y usa "background.<ext>" al exportar
            String bgExt = getExtensionFromUri(template.getBackgroundImageUri());
            String bgUrl = isExport
                    ? "background." + bgExt
                    : template.getBackgroundImageUri();


            css.append("background: url('").append(bgUrl).append("') ")
                    .append(template.getBackgroundImageRepeat()).append(" ")
                    .append(template.getBackgroundImagePosition()).append("; ");
            css.append("background-size: ").append(template.getBackgroundImageSize()).append("; ");
        } else if (template.getBackgroundType().equals("GRADIENT")) {
            css.append("background: linear-gradient(")
                    .append(template.getGradientOrientation()).append(", ")
                    .append(template.getBgColor1()).append(", ")
                    .append(template.getBgColor2()).append("); ");
        }
        css.append("}\n");

        // Contenedor del formulario (#box) usando ancho relativo y max-width
        css.append("#box {\n")
                .append("  background: ").append(template.getFormBackgroundColor()).append(";\n")
                .append("  margin: 20px auto;\n")
                .append("  padding: 20px;\n")
                .append("  width: 80%;\n")
                .append("  max-width: ").append(template.getFormWidth()).append("px;\n")
                .append("  border-radius: ").append(template.getFormBorderRadius()).append(";\n")
                .append("  border: ").append(template.getFormBorderWidth()).append("px ")
                .append(template.getFormBorderStyle()).append(" ")
                .append(template.getFormBorderColor()).append(";\n")
                .append("  text-align: center;\n")
                .append("  box-sizing: border-box;\n")
                .append("}\n");

        // Inputs: ancho relativo y max-width para que se adapten en pantallas pequeñas
        css.append("input[type='text'], input[type='password'] {\n")
                .append("  font-size: ").append(template.getInputFontSize()).append(";\n")
                .append("  padding: ").append(template.getInputPadding()).append(";\n")
                .append("  margin: ").append(template.getInputMargin()).append(" auto;\n")
                .append("  border: 1px solid #ccc;\n")
                .append("  border-radius: 4px;\n")
                .append("  width: 90%;\n")
                .append("  max-width: 300px;\n")
                .append("  box-sizing: border-box;\n")
                .append("}\n");

        // Botón: ancho relativo para que se ajuste en pantallas pequeñas
        css.append("button {\n")
                .append("  background: ").append(template.getButtonColor()).append(";\n")
                .append("  color: ").append(template.getButtonTextColor()).append(";\n")
                .append("  font-size: ").append(template.getButtonTextSize()).append(";\n")
                .append("  padding: 10px 20px;\n")
                // Usa un ancho relativo con máximo; en este ejemplo, si el valor es "auto", se puede ajustar así:
                //.append("  width: 80%;\n")
                //.append("  max-width: 300px;\n")
                //.append("  height: auto;\n")
                .append("  width: ").append(template.getButtonWidth()).append(";\n")
                .append("  height: ").append(template.getButtonHeight()).append(";\n")
                .append("  border: none;\n")
                .append("  border-radius: ").append(template.getButtonBorderRadius()).append(";\n")
                .append("  cursor: pointer;\n")
                .append("  box-sizing: border-box;\n")
                .append("}\n");

        // Media Queries para mejorar la adaptabilidad en pantallas pequeñas
        css.append("@media screen and (max-width: 600px) {\n")
                .append("  #box { padding: 10px; width: 95%; }\n")
                .append("  input[type='text'], input[type='password'] { width: 100%; max-width: none; }\n")
                .append("  button { width: 100%; max-width: none; }\n")
                // Si mantenemos el body como flex, aquí lo forzamos a columna:
                .append("  body {\n")
                .append("    flex-direction: column;\n")
                .append("    align-items: stretch; /* para que ocupe el ancho completo */\n")
                .append("  }\n")

                // Además forzamos la .flex-container a ocupar todo el ancho
                .append("  .flex-container {\n")
                .append("    flex-direction: column;\n")
                .append("    width: 100%;\n")
                .append("  }\n")
                .append("}\n");

        return css.toString();
    }

    // Helper para extraer extensión de un URI (última parte después del '.')
    // Requiere un Context para acceder al ContentResolver
    public static String getExtensionFromUri(String uriString) {
        // Primero, obtener solo el nombre del archivo (eliminar la ruta)
        String fileName = uriString;
        if (uriString.contains("/")) {
            fileName = uriString.substring(uriString.lastIndexOf("/") + 1);
        }

        // Decodificar el nombre del archivo si contiene códigos URL
        if (fileName.contains("%")) {
            try {
                fileName = java.net.URLDecoder.decode(fileName, "UTF-8");
            } catch (Exception e) {
                // Manejar excepción si ocurre
            }
        }

        // Ahora buscar la extensión en el nombre limpio
        int dot = fileName.lastIndexOf('.');
        if (dot != -1 && dot < fileName.length() - 1) {
            return fileName.substring(dot + 1).toLowerCase();
        }

        return "png";  // fallback
    }




}
