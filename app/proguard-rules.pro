-keep class com.formfit.ai.** { *; }
-keep class com.google.mediapipe.** { *; }
-keep class io.github.jan.supabase.** { *; }

-keepattributes Signature
-keepattributes *Annotation*

-dontwarn org.slf4j.**
-dontwarn javax.annotation.**

-keep class kotlinx.serialization.** { *; }
-keepclassmembers class ** {
    @kotlinx.serialization.Serializable *;
}
