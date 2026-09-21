# kotlinx.serialization.json
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class kotlinx.serialization.** { *; }

# jsoup
-keep class org.jsoup.** { *; }
-dontwarn org.jsoup.**

# gson
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# Glide
-keep class com.bumptech.glide.** { *; }
-keep class com.bumptech.glide.load.model.stream.** { *; }
-keep class com.bumptech.glide.load.resource.bitmap.** { *; }
-keep class com.bumptech.glide.load.resource.drawable.** { *; }
-keep class com.bumptech.glide.load.resource.gif.** { *; }
-keep class com.bumptech.glide.manager.** { *; }
-keep class com.bumptech.glide.request.** { *; }
-keep class com.bumptech.glide.util.** { *; }

# Glide Annotation Processor
-keep class com.bumptech.glide.compiler.** { *; }

# Glide AppGlideModule (SVG support) and generated GlideApp
-keep class * extends com.bumptech.glide.module.AppGlideModule { *; }
-keep class com.breadfinancial.breadpartners.sdk.utilities.imageloader.** { *; }

# AndroidSVG: Keep classes used for SVG decoding
-keep class com.caverock.androidsvg.** { *; }
-dontwarn com.caverock.androidsvg.**

# reCAPTCHA
-keep class com.google.android.gms.recaptcha.** { *; }

# CoordinatorLayout
-keep class android.support.design.widget.CoordinatorLayout$** { *; }
-keep class androidx.coordinatorlayout.widget.CoordinatorLayout$** { *; }

# ConstraintLayout
-keep class androidx.constraintlayout.widget.** { *; }

# This will keep any required resources used by Glide or any other image loading functionality
-keep class android.graphics.drawable.** { *; }
-dontwarn android.graphics.drawable.**

# Or keep all classes in a package public
-keep public class com.breadfinancial.breadpartners.sdk.core.** { *; }

-keep class androidx.multidex.** { *; }

-dontwarn javax.annotation.**