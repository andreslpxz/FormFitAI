package com.formfit.ai.core.util

object Constants {
    const val SUPABASE_URL = "https://tnjahnkoeziadabetlvx.supabase.co"
    const val APP_SCHEME = "formfitai"

    const val TABLE_PROFILES = "profiles"
    const val TABLE_WORKOUT_SESSIONS = "workout_sessions"
    const val TABLE_ROUTINES = "routines"
    const val TABLE_SUBSCRIPTIONS = "subscriptions"

    const val STRIPE_MONTHLY_PRICE_ID = "price_monthly_placeholder"
    const val STRIPE_YEARLY_PRICE_ID = "price_yearly_placeholder"
    const val STRIPE_MONTHLY_PRICE = "$9.99"
    const val STRIPE_YEARLY_PRICE = "$59.99"

    const val FREE_WEEKLY_WORKOUT_LIMIT = 3

    const val MEDIAPIPE_MODEL_NAME = "pose_landmarker_full.task"
    const val POSE_DETECTION_CONFIDENCE = 0.5f
    const val POSE_TRACKING_CONFIDENCE = 0.5f
    const val POSE_PRESENCE_CONFIDENCE = 0.5f

    const val TARGET_FPS = 60
    const val SKELETON_ANIMATION_LERP = 0.3f

    const val SQUAT_THRESHOLD_ANGLE = 90f
    const val PUSHUP_THRESHOLD_ANGLE = 90f
    const val LUNGE_THRESHOLD_ANGLE = 90f
    const val BICEP_CURL_THRESHOLD_ANGLE = 45f

    const val REP_STATE_DEBOUNCE_FRAMES = 3
}
