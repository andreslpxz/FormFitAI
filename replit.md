# FormFit AI — Android App

A real-time AI fitness coaching Android app with pose detection, rep counting, and instant form correction.

## Project Overview

FormFit AI is a native Android application built with Kotlin and Jetpack Compose that uses MediaPipe Pose Landmarker to detect 33 body keypoints in real-time and provide personalized coaching feedback.

## Architecture

### Multi-Module Structure

```
FormFitAI/
├── app/          — Main application (entry point, navigation, DI root)
├── core/         — Shared domain: models, Room DB, Supabase client, DataStore
├── vision/       — Computer vision: CameraX + MediaPipe + PoseAnalysisManager
└── ui/           — Design system + all Compose screens and components
```

### Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 1.9.22 |
| UI | Jetpack Compose + Material 3 |
| DI | Hilt 2.50 |
| Navigation | Navigation Compose 2.7 |
| Camera | CameraX 1.3.1 |
| Pose AI | MediaPipe Tasks Vision 0.10.9 |
| Local DB | Room 2.6.1 |
| Preferences | DataStore 1.0.0 |
| Auth/Backend | Supabase 2.2.3 (GoTrue, PostgREST, Storage, Realtime) |
| Payments | Stripe Android 20.35 |
| Charts | Vico 1.13 |
| Animations | Lottie Compose 6.3 |
| Image Loading | Coil 2.5.0 |
| Build | Gradle 8.4 + Version Catalogs |

### Key Features

- **60 FPS Pose Detection** — MediaPipe LIVE_STREAM mode with GPU Delegate/NNAPI
- **33-Keypoint Skeleton Overlay** — Compose Canvas with color-coded limb feedback
- **Rep Counting State Machine** — Per-exercise: IDLE → STANDING → DESCENDING → THRESHOLD_REACHED → ASCENDING → CONFIRMED
- **Form Analysis** — Angle-based (atan2/law of cosines) per-joint quality assessment
- **Auto-calibration** — Detects user distance and centering before workout starts
- **Supabase Auth** — Google OAuth, Email/Password, Magic Link, password reset
- **Custom Routines** — Build and save custom workout routines
- **Progress Tracking** — Workout history, personal bests, form scores in Room DB + Supabase
- **Stripe Plans** — Free tier (3 workouts/week) + Pro ($9.99/mo or $59.99/yr)
- **Onboarding Survey** — 6-step personalization flow (name, gender, goal, frequency, equipment, referral)

### Supabase Configuration

- **Project URL:** https://tnjahnkoeziadabetlvx.supabase.co
- **Project Name:** FormFitAI

#### Required Supabase Tables

```sql
-- profiles
CREATE TABLE profiles (
  id UUID REFERENCES auth.users PRIMARY KEY,
  display_name TEXT,
  avatar_url TEXT,
  gender TEXT,
  goal TEXT,
  workout_frequency TEXT,
  equipment TEXT,
  referral_source TEXT,
  subscription_plan TEXT DEFAULT 'free',
  subscription_expiry TIMESTAMPTZ,
  onboarding_complete BOOLEAN DEFAULT false,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- workout_sessions (cloud sync of local Room records)
CREATE TABLE workout_sessions (
  id BIGSERIAL PRIMARY KEY,
  user_id UUID REFERENCES profiles(id),
  exercise_id TEXT,
  exercise_name TEXT,
  rep_count INT,
  duration_seconds INT,
  avg_form_score FLOAT,
  calories_burned FLOAT,
  sets_completed INT DEFAULT 1,
  form_issues JSONB,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- routines (user-created routines)
CREATE TABLE routines (
  id TEXT PRIMARY KEY,
  user_id UUID REFERENCES profiles(id),
  name TEXT,
  description TEXT,
  exercises JSONB,
  estimated_minutes INT,
  difficulty TEXT,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- subscriptions (managed by Stripe webhook)
CREATE TABLE subscriptions (
  id TEXT PRIMARY KEY,
  user_id UUID REFERENCES profiles(id),
  stripe_customer_id TEXT,
  stripe_subscription_id TEXT,
  plan TEXT,
  status TEXT,
  current_period_end TIMESTAMPTZ,
  created_at TIMESTAMPTZ DEFAULT NOW()
);
```

### Stripe Configuration

Add to environment (configured later):
- `STRIPE_SECRET_KEY` — Your Stripe secret key
- `STRIPE_WEBHOOK_SECRET` — Webhook signing secret
- `STRIPE_MONTHLY_PRICE_ID` — Price ID for $9.99/mo plan
- `STRIPE_YEARLY_PRICE_ID` — Price ID for $59.99/yr plan

### Building the Android App

Prerequisites:
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17+
- Android SDK 34

Steps:
1. Open the project root in Android Studio
2. Set `SUPABASE_ANON_KEY` in `local.properties` or `app/build.gradle.kts`
3. Add `google-services.json` (for Google OAuth)
4. Run `./gradlew assembleDebug`
5. Install on device: `./gradlew installDebug`

### MediaPipe Model

The model file (`pose_landmarker_full.task`, ~9MB) is already downloaded and placed at:
```
vision/src/main/assets/pose_landmarker_full.task
```

If re-downloading: https://storage.googleapis.com/mediapipe-models/pose_landmarker/pose_landmarker_full/float16/1/pose_landmarker_full.task

### Project Preview Server

A Node.js server runs on port 5000 showing the project documentation and architecture overview.
- `server.js` — Express server
- `public/index.html` — Project overview UI

## Task Progress

| Task | Status |
|------|--------|
| #1 Foundation & Architecture | ✅ Complete |
| #2 Onboarding & Auth (Supabase) | ✅ Complete |
| #3 Vision Engine (MediaPipe) | ✅ Complete |
| #4 Workout Engine & Active Workout UI | Pending |
| #5 Progress Dashboard & Stripe | Pending |

### Task #3 Vision Engine — Delivered Components

- `vision/PoseLandmarkerHelper.kt` — MediaPipe LIVE_STREAM, GPU→CPU fallback, RGBA_8888 stride-aware bitmap conversion
- `vision/CameraManager.kt` — CameraX front camera, ImageAnalysis, 60 FPS target, lifecycle-bound
- `vision/PoseLandmarkSmoother.kt` — EMA lerp smoothing (alpha=0.35) for flicker-free skeleton
- `vision/PoseAnalysisManager.kt` — Knee/hip/elbow/shoulder angle calculation, squat/pushup/lunge form analysis
- `vision/RepCounterEngine.kt` — State machine rep counting per exercise
- `vision/model/PoseLandmarks.kt` — 33 landmark indices, SKELETON_CONNECTIONS graph
- `vision/model/FormQuality.kt` — FormQualityLevel/SegmentQuality/CalibrationState enums and data classes
- `vision/assets/pose_landmarker_full.task` — MediaPipe full pose model (9MB)
- `ui/screens/workout/SkeletonOverlay.kt` — Compose Canvas skeleton with color-coded bones
- `ui/screens/workout/CalibrationOverlay.kt` — Auto-calibration UI (step back/closer/center)
- `ui/screens/workout/PoseCameraScreen.kt` — Full camera screen with permission handling
- `ui/screens/workout/CameraPermissionScreen.kt` — Camera permission rationale UI
- `ui/screens/workout/PoseDetectionViewModel.kt` — Connects camera → MediaPipe → form analysis → smooth pose
