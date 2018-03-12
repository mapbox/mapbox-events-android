# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
-keep public class com.google.android.gms.* { public *; }
-dontwarn com.google.android.gms.**
-dontwarn com.mapbox.android.core.location.MockLocationEngine
-dontwarn com.mapbox.android.core.location.MockLocationEngine$LocationUpdateRunnable
-dontwarn java.awt.Color
-dontwarn com.mapzen.android.lost.api**
