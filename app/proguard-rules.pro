# ProGuard rules for Market

# ─── Firebase ────────────────────────────────────────────────
-keep class com.google.firebase.auth.** { *; }
-keep class com.google.firebase.firestore.** { *; }
-keep class com.google.firebase.firestore.**$* { *; }

# Firebase Data Collection (auto-init)
-keep class com.google.firebase.components.** { *; }

# ─── Hilt / Dagger ──────────────────────────────────────────
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }

# ─── Kotlin Serialization (if used) ─────────────────────────
-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations

# ─── Data classes (Firestore mapping) ───────────────────────
-keep class com.market.data.remote.** { *; }
-keep class com.market.domain.model.** { *; }

# ─── Google Sign-In ─────────────────────────────────────────
-keep class com.google.android.gms.auth.** { *; }
-keep class com.google.android.gms.common.** { *; }

# ─── General Android ────────────────────────────────────────
-keepattributes Signature
-keepattributes Exceptions
-dontwarn javax.annotation.**
-dontwarn sun.misc.Unsafe
