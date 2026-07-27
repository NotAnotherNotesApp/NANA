# Keep Room entities
-keep class com.allubie.nana.data.model.** { *; }

# Keep enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
