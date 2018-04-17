# Consumer proguard rules for libcore

# --- GMS ---
-keep public class com.google.android.gms.* { public *; }
-dontwarn com.google.android.gms.**