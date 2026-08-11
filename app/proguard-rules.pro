# Room (use if minifyEnabled = true)
#-keep class androidx.room.RoomDatabase { *; }
#-keep interface androidx.room.RoomDatabase$Builder { *; }
#-keep class * extends androidx.room.RoomDatabase
#-keep class * implements androidx.room.RoomDatabase
##-keepnames @androidx.room.RoomDatabase
#-keep class * extends androidx.room.RoomDatabase { *; }
#
#
#-keep class androidx.core.app.CoreComponentFactory { *; }
#
#-keep class androidx.lifecycle.LiveData { *; }
#
#-keep class android.webkit.** { *; }
#-keepclassmembers class android.webkit.WebSettings { public *; }
#
#
#-keep class androidx.navigation.** { *; }
#-keep interface androidx.navigation.** { *; }
#-keep class * implements androidx.navigation.NavArgs { *; }
#-keep class * implements androidx.navigation.Navigator { *; }
#-keep class * implements androidx.navigation.NavigatorProvider { *; }
#-keep class * extends androidx.navigation.NavType { *; }
#-keep class * extends androidx.navigation.NavOptions { *; }
#-keep class * extends androidx.navigation.NavDirections { *; }
#-keep class * extends androidx.navigation.NavGraph { *; }
#-keep class * extends androidx.navigation.NavDestination { *; }
#-keep class * extends androidx.navigation.NavController { *; }
#-keep class * extends androidx.navigation.NavInflater { *; }
#-keep class * extends androidx.navigation.NavigatorProvider { *; }
#
#-keep class com.google.android.gms.** { *; }
#-dontwarn com.google.android.gms.**
#
#
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.fragments.** { *; }
#
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.DailyStatus.Retrofitcalls.models.** { *; }
#
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.ui.adapters.LabelsAdapter.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.ui.adapters.SindhiPoetryShowAdapter.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.ui.sindhiPoetryModels.SindhiPoetry.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.ui.sindhiPoetryModels.SindhiPoetryItemClickListener.** { *; }
#
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.dbClasses.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.dbClasses.DataBaseCopyOperationsKt { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.dbClasses.SuggestionItems.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.KeyboardConstantsKt.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.constants.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.keyboard_classes.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.keyboard_classes.CustomRecentEmojiProvider.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.keyboard_classes.CustomTheme.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.keyboard_classes.Key.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.keyboard_classes.LayoutState.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.keyboard_classes.KeyboardVisibilityProviderKt.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.CustomFirebaseEvents.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.ForegroundCheckTask.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.HelperFunctionsKt.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.MyConfigUpdateListener.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.service.CustomImeService.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.service.ComposeKeyboardView.** { *; }
#
## Keep all Ad Classes
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.ads.InterstitialClassAdMob.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.ads.CustomDialog.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.utilityClasses.AdObject.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.ads.ResumeAd.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NativeMaster.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.ads.NewNativeAdClass.** { *; }
#
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.data.** { *; }
#
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.ads.ApplicationClass.** { *; }
#
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.theme.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.theme.AppTheme.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.theme.ThemeKt.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.theme.ColorKt.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.theme.TypeKt.** { *; }
#
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.SindhiStatusFragment.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.SindhiStatusShowFragment.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.SpeechToText.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.SpeechToText.AllCountrylistMe.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.SpeechToText.countryrvadapter.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.SpeechToText.fromcountry_fragment.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.SpeechToText.tocountry_fragment.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.SpeechToText.SpeechFragment.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.SpeechToText.TranslatorCall.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history.roomDb.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history.history_fragment.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history.HistoryAdapter.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history.HistoryConversationAdapter.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history.roomDb.Conversation.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history.roomDb.ConversationExtension.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history.roomDb.ConversationExtensionTemp.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history.roomDb.Conversationto.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history.roomDb.DateConverter.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history.roomDb.HistoryDb.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history.roomDb.historyRepo.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history.roomDb.HistoryViewModel.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history.roomDb.viewmodelfactory.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history.roomDb.wordDao.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.history.roomDb.WordEntity.** { *; }
#
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.keyboardLayout.MyCandidateViewKt.** { *; }
#-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.keyboardLayout.MyKeyboardLayoutKt.** { *; }
#
#-keepclassmembers class com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.keyboardLayout.MyCandidateViewKt { *; }
#-keepclassmembers class com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.keyboardLayout.MyKeyboardLayoutKt { *; }
#
#-keep class com.google.android.gms.ads.** { *; }
#-keep class com.google.android.gms.ads.mediation.** { *; }
#-keep class com.google.android.gms.ads.rewarded.** { *; }
#-keep class com.google.android.gms.ads.interstitial.** { *; }
#-keep class com.google.android.gms.ads.reward.** { *; }
#-keep class com.google.android.gms.ads.doubleclick.** { *; }
#
#-keepclassmembers class com.google.android.gms.ads.** { *; }
#-keepclassmembers class com.google.android.gms.ads.mediation.** { *; }
#-keepclassmembers class com.google.android.gms.ads.rewarded.** { *; }
#-keepclassmembers class com.google.android.gms.ads.interstitial.** { *; }
#-keepclassmembers class com.google.android.gms.ads.reward.** { *; }
#-keepclassmembers class com.google.android.gms.ads.doubleclick.** { *; }
#
## Keep all classes in Firebase packages
#-keep class com.google.firebase.** { *; }
#-keep class com.google.android.gms.** { *; }
#
## Keep all classes in Firebase Analytics
#-keepnames class com.google.android.gms.measurement.** { *; }
#-keep class com.google.android.gms.measurement.internal.** { *; }
#
## Keep all classes in Firebase Auth
#-keepnames class com.google.firebase.auth.** { *; }
#
## Keep all classes in Firebase Firestore
#-keepnames class com.google.firebase.firestore.** { *; }
#
## Keep all classes in Firebase Realtime Database
#-keepnames class com.google.firebase.database.** { *; }
#
## Keep all classes in Firebase Cloud Messaging
#-keepnames class com.google.firebase.messaging.** { *; }
#
## Keep all classes in Firebase Remote Config
#-keepnames class com.google.firebase.remoteconfig.** { *; }
#
## Keep all classes in Firebase Crashlytics
#-keepnames class com.google.firebase.crashlytics.** { *; }
#
## Keep all classes in Firebase Performance Monitoring
#-keepnames class com.google.firebase.perf.** { *; }
#
## Keep all classes in Firebase In-App Messaging
#-keepnames class com.google.firebase.inappmessaging.** { *; }
#
## Keep all classes in Firebase ML Kit
#-keepnames class com.google.firebase.ml.** { *; }


# 1. RETROFIT & GSON / JSON PARSING MODELS
# We must keep these completely untouched so the JSON
# from your APIs maps perfectly to your Kotlin variables.
# ==========================================
-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.stickers.** { *; }
-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.DailyStatus.Retrofitcalls.models.** { *; }
-keep class com.urdu_keyboard.data.FilesModel.** { *; }
-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.dbClasses.** { *; }
-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.data.** { *; }
-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.jetpack_version.domain.keyboard_classes.** { *; }
-repackageclasses ''
# ==========================================
# 2. RETROFIT API INTERFACES
# Retrofit uses reflection to create these interfaces.
# ==========================================
-keep interface com.sindhi.urdu.english.keybad.sindhikeyboard.stickers.** { *; }
-keep interface com.sindhi.urdu.english.keybad.sindhikeyboard.stickers.ApiService.** { *; }

# Keep generic signature info for Retrofit generics to work properly
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# ==========================================
# 3. JAVASCRIPT INTERFACES (If you use WebViews)
# If any of your models are passed into a WebView, keep this.
# ==========================================
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keep class com.urdu_keyboard.urduEditor.model.** { *; }
-keepclassmembers class com.sindhi.urdu.english.keybad.sindhikeyboard.sindiEditor.model.** { *; }

# ==========================================
# 4. SUPPRESS HARMLESS WARNINGS
# Prevents the build from failing due to missing references
# in third-party libraries that you don't actually use.
# ==========================================
-dontwarn android.media.LoudnessCodecController**
-dontwarn org.slf4j.**
-dontwarn kotlin.coroutines.**
-dontwarn retrofit2.**

-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.examples.android.model.** { *; }
-keep class com.google.gson.** { *; }
-keep class com.sindhi.urdu.english.keybad.sindhikeyboard.ui.fragments.ibadat.HadithModel { *; }