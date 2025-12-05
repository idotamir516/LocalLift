# Quickstart: Workout Tracking App

**Feature**: 001-workout-tracking  
**Date**: 2025-12-03

## Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17+
- Android SDK with API 26+ installed
- Physical device or emulator running Android 8.0+

## Project Setup

### 1. Create New Project

In Android Studio:
1. File → New → New Project
2. Select "Empty Activity" (Compose)
3. Configure:
   - Name: `WorkoutTracker`
   - Package: `com.workout.app`
   - Language: Kotlin
   - Minimum SDK: API 26 (Android 8.0)
   - Build configuration: Kotlin DSL

### 2. Update Dependencies

Add to `app/build.gradle.kts`:

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") version "1.9.21-1.0.15"
}

android {
    namespace = "com.workout.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.workout.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.6"
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.01.00")
    implementation(composeBom)
    
    // Compose UI
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.8.2")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")
    
    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    
    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
}
```

### 3. Sync and Build

1. Click "Sync Now" when prompted
2. Build → Make Project
3. Verify no errors

## Initial Files to Create

After project setup, create the following structure:

```
app/src/main/java/com/workout/app/
├── MainActivity.kt          (exists, update)
├── WorkoutApp.kt            (create)
├── data/
│   ├── AppDatabase.kt
│   ├── entities/
│   │   ├── WorkoutTemplate.kt
│   │   ├── TemplateExercise.kt
│   │   ├── WorkoutSession.kt
│   │   ├── ExerciseLog.kt
│   │   └── SetLog.kt
│   ├── dao/
│   │   ├── TemplateDao.kt
│   │   └── SessionDao.kt
│   └── ExerciseLibrary.kt
├── ui/
│   ├── theme/
│   │   ├── Theme.kt
│   │   ├── Color.kt
│   │   └── Type.kt
│   ├── navigation/
│   │   └── AppNavigation.kt
│   └── screens/
│       └── (screens created per tasks)
└── util/
    └── (utilities created per tasks)
```

## Running the App

1. Connect device or start emulator
2. Click Run (▶) or Shift+F10
3. App installs and launches

## Development Workflow

Per constitution principles:

1. **Iterate fast**: Build and run frequently
2. **Manual testing**: Test on device as you build each screen
3. **Simple commits**: Commit working features incrementally
4. **No over-engineering**: Start simple, add complexity only when needed

## Key Technical Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| UI Framework | Jetpack Compose | Modern, declarative, less code |
| Database | Room | Standard Android, compile-time safety |
| Architecture | Single activity, no ViewModel layer | Simplicity per constitution |
| Navigation | Compose Navigation | Built-in, type-safe |
| Theming | Material Design 3 | Modern look out of box |

## First Milestone

Get basic navigation working:
1. Bottom nav with 3 tabs
2. Placeholder screens for each tab
3. Basic Material 3 theming

After this, implement features per task list (see `tasks.md` when generated).
