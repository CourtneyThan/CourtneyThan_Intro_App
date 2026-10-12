# kotlinx.serialization: keep generated serializers for the content schema.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.courtneythan.aboutme.core.model.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.courtneythan.aboutme.core.model.**$$serializer { *; }
