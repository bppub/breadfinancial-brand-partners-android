# kotlinx.serialization.json: Keep serialization classes and members
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class kotlinx.serialization.** { *; }

# Jsoup: Keep all classes and suppress warnings
-keep class org.jsoup.** { *; }
-dontwarn org.jsoup.**

# Gson: Keep all classes and suppress warnings
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# Glide: Ensure Glide components and generated code are not obfuscated
-keep class com.bumptech.glide.** { *; }
-keep class com.bumptech.glide.load.model.stream.** { *; }
-keep class com.bumptech.glide.load.resource.bitmap.** { *; }
-keep class com.bumptech.glide.load.resource.drawable.** { *; }
-keep class com.bumptech.glide.load.resource.gif.** { *; }
-keep class com.bumptech.glide.manager.** { *; }
-keep class com.bumptech.glide.request.** { *; }
-keep class com.bumptech.glide.util.** { *; }

# Glide Generated Code and Annotations
-keep class com.bumptech.glide.annotation.GlideModule { *; }
-dontwarn com.bumptech.glide.**

# reCAPTCHA: Keep Google reCAPTCHA classes
-keep class com.google.android.gms.recaptcha.** { *; }

# CoordinatorLayout: Keep widget classes for runtime layout processing
-keep class androidx.coordinatorlayout.widget.CoordinatorLayout$** { *; }

# ConstraintLayout: Keep ConstraintLayout classes
-keep class androidx.constraintlayout.widget.** { *; }

# Multidex: Retain multidex classes (if required)
-keep class androidx.multidex.** { *; }

# Or keep all classes in a package public
-keep public class com.breadfinancial.breadpartners.sdk.** { *; }

# Suppress general warnings for smooth builds
-dontwarn javax.annotation.**

# Optional: Avoid warnings for missing standard resources
-dontwarn android.graphics.drawable.**
