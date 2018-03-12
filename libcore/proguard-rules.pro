# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
-keep public class com.google.android.gms.* { public *; }
-dontwarn com.google.android.gms.**
-dontwarn java.awt.Color
-dontwarn com.mapzen.android.lost.api**
