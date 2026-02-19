# 🏋️ Workout Tracking App

A modern Android app for tracking gym workouts, built with Kotlin and Jetpack Compose.

![Android](https://img.shields.io/badge/Android-3DDC84?style=flat&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat&logo=jetpackcompose&logoColor=white)
![Min SDK](https://img.shields.io/badge/Min%20SDK-26-blue)

## ✨ Features

### Core Workout Tracking
- **Workout Templates** - Create reusable workout routines with exercises, sets, and rest times
- **Quick Workouts** - Start an empty workout without a template
- **Set Logging** - Track weight, reps, and RPE (Rate of Perceived Exertion) for each set
- **Set Types** - Distinguish between Regular, Warmup, and Drop sets
- **Rest Timer** - Configurable countdown timer between sets with audio notification
- **Workout History** - View all completed workouts with full details

### Progress Tracking
- **Calendar View** - Visualize workout frequency on a monthly calendar
- **Weekly Volume Chart** - Track total training volume over time with training phase overlays
- **Effective Sets Analysis** - See your most and least trained muscle groups using fractional set counting
- **1RM Estimation** - Estimated one-rep max for each exercise based on performance
- **Body Weight Tracking** - Log and chart body weight over time

### Program Analysis
- **Volume by Muscle Group** - Analyze your templates to see set distribution across muscle groups
- **Primary vs Auxiliary** - Understand direct and indirect muscle targeting
- **Workout Time Estimation** - Get estimated workout duration based on sets and rest times

### Training Phases
- **Phase Types** - Track Bulk, Cut, Maintenance, Strength, Hypertrophy, Deload, and Custom phases
- **Visual Overlays** - See training phases as colored bands on all charts
- **Phase History** - Review past training phases and their durations

### Data Management
- **CSV Export** - Export all workout data for external analysis
- **Save Workout as Template** - Convert any completed workout into a reusable template
- **Configurable Settings** - Customize effective set counting, time estimates, and more

## 📱 Screenshots

*Coming soon*

## 🛠️ Tech Stack

| Component | Technology |
|-----------|------------|
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose with Material 3 |
| **Database** | Room (SQLite) |
| **Navigation** | Jetpack Navigation Compose |
| **Async** | Kotlin Coroutines + Flow |
| **Min SDK** | 26 (Android 8.0) |
| **Target SDK** | 35 |

## 📂 Project Structure

```
app/src/main/java/com/workout/app/
├── MainActivity.kt              # Entry point
├── WorkoutApp.kt               # Application class with database singleton
├── data/
│   ├── AppDatabase.kt          # Room database definition
│   ├── ExerciseLibrary.kt      # Static library of ~100 exercises
│   ├── entities/               # Room entities (database tables)
│   └── dao/                    # Data Access Objects
├── ui/
│   ├── theme/                  # Material 3 theme
│   ├── navigation/
│   │   └── AppNavigation.kt    # All routes and navigation logic
│   ├── screens/                # Full-screen composables
│   └── components/             # Reusable UI components
└── util/
    ├── AudioPlayer.kt          # Timer completion sound
    ├── TimerManager.kt         # Countdown timer logic
    ├── WorkoutExporter.kt      # CSV export
    ├── ProgramAnalyzer.kt      # Muscle group analysis
    └── SettingsManager.kt      # User preferences
```

## 🏗️ Architecture

The app follows a simple, pragmatic architecture:

- **Single Activity** - One activity hosts all Compose screens
- **Room Database** - All data persisted locally with migrations
- **StateFlow** - Reactive data flow from database to UI
- **Composable Functions** - Declarative UI with Jetpack Compose

### Data Model

```
WorkoutTemplate (1) ──┬── (*) TemplateExercise ──── (*) TemplateSet
                      │
                      └── (*) WorkoutSession ──── (*) ExerciseLog ──── (*) SetLog
```

- **Templates** define the structure (exercises, default sets, rest times)
- **Sessions** capture what actually happened (logged weights, reps, RPE)

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 35

### Build & Run

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/workout-app-vibe.git
   cd workout-app-vibe
   ```

2. Open in Android Studio

3. Sync Gradle and run on device/emulator:
   ```bash
   ./gradlew assembleDebug
   ```

### Running Tests

```bash
./gradlew test                    # Unit tests
./gradlew connectedAndroidTest    # Instrumented tests
```

## 🏋️ Exercise Library

The app includes 100+ exercises across 17 muscle categories:

- Chest, Back, Lower Back, Trapezius
- Shoulders, Biceps, Triceps, Forearms
- Quads, Hamstrings, Glutes, Calves
- Abductors, Adductors, Core
- Full Body, Cardio

Each exercise has:
- **Primary muscle** - Main muscle targeted (counts as 1 effective set)
- **Auxiliary muscles** - Secondary muscles worked (counts as 0.5 effective sets)

Custom exercises can be added with user-defined muscle targeting.

## ⚙️ Settings

| Setting | Description |
|---------|-------------|
| **Warmup Sets as Effective** | Include warmup sets in effective set calculations |
| **Drop Sets as Effective** | Include drop sets in effective set calculations |
| **Time Per Set** | Estimated time per set for workout duration calculation |
| **Previous Lift Source** | Show previous lifts from same template or any workout |

## 📊 Effective Sets Formula

The app uses fractional set counting to analyze training volume:

```
Effective Sets = Primary Sets × 1.0 + Auxiliary Sets × 0.5
```

This accounts for the fact that exercises indirectly train multiple muscle groups.
