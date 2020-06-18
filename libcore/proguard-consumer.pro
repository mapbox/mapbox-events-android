# Consumer proguard rules for libcore

# --- GMS ---
-keep class com.google.android.gms.location.LocationServices
-keep class com.google.android.gms.common.GoogleApiAvailability
-dontwarn com.google.android.gms.**
