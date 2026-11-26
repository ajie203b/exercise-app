# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Gson 通过反射解析的传输对象：必须保留类名与字段名。
# 领域模型 Exercise 由 DTO 转换而来，不再参与反射解析，但保留成本极低且能防止
# 后续有人直接在 Gson 上复用它时踩坑。
-keep class com.exercise.app.ExerciseDto { *; }
-keep class com.exercise.app.Exercise { *; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keepattributes Signature
-keepattributes *Annotation*
