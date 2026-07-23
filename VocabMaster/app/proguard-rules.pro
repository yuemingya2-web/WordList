# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# desugaring options.
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep Moshi metadata
-keepclassmembers class * {
    @com.squareup.moshi.JsonClass <fields>;
}
-keep @com.squareup.moshi.JsonClass class * { *; }
-keep class com.vocabmaster.app.data.remote.dto.** { *; }

# Retrofit
-keepattributes Signature, Exceptions
-keep class retrofit2.** { *; }
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
