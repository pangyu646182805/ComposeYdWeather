# 保留行号便于 crash 定位
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Kotlin Coroutines - R8 可能裁剪掉调度器工厂，需显式保留
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Kotlinx Serialization - $$serializer 在运行时通过反射访问
-keepattributes *Annotation*, InnerClasses
-keep,includedescriptorclasses class com.yd.weather.**$$serializer { *; }
-keepclassmembers @kotlinx.serialization.Serializable class com.yd.weather.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}

# 应用数据类（网络响应模型 / Room 实体 / Type-Safe 路由）
-keep class com.yd.weather.model.** { *; }
-keep class com.yd.weather.db.model.** { *; }
-keep class com.yd.weather.routes.** { *; }
