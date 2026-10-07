# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /usr/local/Cellar/android-sdk/24.3.3/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# kotlinx.serialization: keep generated serializers for our own @Serializable models
# (recommended rule from https://github.com/Kotlin/kotlinx.serialization#android)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.kronos.multiplatform.weatherapp.**$$serializer { *; }
-keepclassmembers class com.kronos.multiplatform.weatherapp.** {
    *** Companion;
}
-keepclasseswithmembers class com.kronos.multiplatform.weatherapp.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# WorkManager instantiates our Worker subclasses reflectively by class name.
-keep public class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Room instantiates the generated *_Impl database/DAO classes reflectively.
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Optional/transitive dependencies pulled in by Ktor's OkHttp engine that aren't on the runtime
# classpath for this app.
-dontwarn org.slf4j.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Move obfuscated classes into the default (unnamed) package for a smaller DEX — becomes the
# AGP default starting at 9.1, we're still on 9.0.1. Safe for an app (not a library): nothing
# here looks up resources/classes by this app's own package path at runtime.
-repackageclasses
