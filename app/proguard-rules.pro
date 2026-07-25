# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# --- Gson ---
# Estas clases se serializan/deserializan por nombre de campo via reflexion
# (SharedPreferences y respuesta de Retrofit), sin @SerializedName, asi que
# los nombres de campo deben sobrevivir la ofuscacion tal cual.
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.jmanuel.mikroficha.RoutersMk { <fields>; }
-keep class com.jmanuel.mikroficha.BtData { <fields>; }
-keep class com.jmanuel.mikroficha.SubscriptionDetails { <fields>; }
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken