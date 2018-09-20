# Consumer proguard rules for libtelemetry

# OkHttp configuration from https://github.com/square/okhttp/blob/master/okhttp/src/main/resources/META-INF/proguard/okhttp3.pro
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-dontwarn org.codehaus.mojo.animal_sniffer.*
-dontwarn okhttp3.internal.platform.ConscryptPlatform
-dontnote okhttp3.internal.platform.**

# Gson
-dontnote sun.misc.Unsafe

# don't note duplciate class definitions, https://issuetracker.google.com/issues/37070898
-dontnote android.net.http.**
-dontnote org.apache.http.**