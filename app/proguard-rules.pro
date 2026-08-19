# Keep Room entities
-keep class com.allubie.nana.data.model.** { *; }

# Keep Backup data models for JSON serialization / deserialization
-keep class com.allubie.nana.data.BackupData { *; }
-keep class com.allubie.nana.data.BackupPreferences { *; }

# Keep enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
