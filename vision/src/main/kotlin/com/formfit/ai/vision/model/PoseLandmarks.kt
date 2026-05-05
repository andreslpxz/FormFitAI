package com.formfit.ai.vision.model

import com.google.mediapipe.tasks.components.containers.Landmark
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark

data class PoseLandmarkResult(
    val landmarks: List<NormalizedLandmark>,
    val worldLandmarks: List<Landmark>,
    val imageWidth: Int,
    val imageHeight: Int,
    val timestamp: Long = System.currentTimeMillis()
)

object PoseLandmarkIndex {
    const val NOSE = 0
    const val LEFT_EYE_INNER = 1
    const val LEFT_EYE = 2
    const val LEFT_EYE_OUTER = 3
    const val RIGHT_EYE_INNER = 4
    const val RIGHT_EYE = 5
    const val RIGHT_EYE_OUTER = 6
    const val LEFT_EAR = 7
    const val RIGHT_EAR = 8
    const val MOUTH_LEFT = 9
    const val MOUTH_RIGHT = 10
    const val LEFT_SHOULDER = 11
    const val RIGHT_SHOULDER = 12
    const val LEFT_ELBOW = 13
    const val RIGHT_ELBOW = 14
    const val LEFT_WRIST = 15
    const val RIGHT_WRIST = 16
    const val LEFT_PINKY = 17
    const val RIGHT_PINKY = 18
    const val LEFT_INDEX = 19
    const val RIGHT_INDEX = 20
    const val LEFT_THUMB = 21
    const val RIGHT_THUMB = 22
    const val LEFT_HIP = 23
    const val RIGHT_HIP = 24
    const val LEFT_KNEE = 25
    const val RIGHT_KNEE = 26
    const val LEFT_ANKLE = 27
    const val RIGHT_ANKLE = 28
    const val LEFT_HEEL = 29
    const val RIGHT_HEEL = 30
    const val LEFT_FOOT_INDEX = 31
    const val RIGHT_FOOT_INDEX = 32
}

val SKELETON_CONNECTIONS = listOf(
    Pair(PoseLandmarkIndex.LEFT_SHOULDER, PoseLandmarkIndex.RIGHT_SHOULDER),
    Pair(PoseLandmarkIndex.LEFT_SHOULDER, PoseLandmarkIndex.LEFT_ELBOW),
    Pair(PoseLandmarkIndex.LEFT_ELBOW, PoseLandmarkIndex.LEFT_WRIST),
    Pair(PoseLandmarkIndex.RIGHT_SHOULDER, PoseLandmarkIndex.RIGHT_ELBOW),
    Pair(PoseLandmarkIndex.RIGHT_ELBOW, PoseLandmarkIndex.RIGHT_WRIST),
    Pair(PoseLandmarkIndex.LEFT_SHOULDER, PoseLandmarkIndex.LEFT_HIP),
    Pair(PoseLandmarkIndex.RIGHT_SHOULDER, PoseLandmarkIndex.RIGHT_HIP),
    Pair(PoseLandmarkIndex.LEFT_HIP, PoseLandmarkIndex.RIGHT_HIP),
    Pair(PoseLandmarkIndex.LEFT_HIP, PoseLandmarkIndex.LEFT_KNEE),
    Pair(PoseLandmarkIndex.LEFT_KNEE, PoseLandmarkIndex.LEFT_ANKLE),
    Pair(PoseLandmarkIndex.RIGHT_HIP, PoseLandmarkIndex.RIGHT_KNEE),
    Pair(PoseLandmarkIndex.RIGHT_KNEE, PoseLandmarkIndex.RIGHT_ANKLE),
    Pair(PoseLandmarkIndex.LEFT_ANKLE, PoseLandmarkIndex.LEFT_HEEL),
    Pair(PoseLandmarkIndex.LEFT_HEEL, PoseLandmarkIndex.LEFT_FOOT_INDEX),
    Pair(PoseLandmarkIndex.RIGHT_ANKLE, PoseLandmarkIndex.RIGHT_HEEL),
    Pair(PoseLandmarkIndex.RIGHT_HEEL, PoseLandmarkIndex.RIGHT_FOOT_INDEX)
)

val LEFT_SIDE_LANDMARKS = setOf(
    PoseLandmarkIndex.LEFT_SHOULDER,
    PoseLandmarkIndex.LEFT_ELBOW,
    PoseLandmarkIndex.LEFT_WRIST,
    PoseLandmarkIndex.LEFT_HIP,
    PoseLandmarkIndex.LEFT_KNEE,
    PoseLandmarkIndex.LEFT_ANKLE,
    PoseLandmarkIndex.LEFT_HEEL,
    PoseLandmarkIndex.LEFT_FOOT_INDEX
)

val RIGHT_SIDE_LANDMARKS = setOf(
    PoseLandmarkIndex.RIGHT_SHOULDER,
    PoseLandmarkIndex.RIGHT_ELBOW,
    PoseLandmarkIndex.RIGHT_WRIST,
    PoseLandmarkIndex.RIGHT_HIP,
    PoseLandmarkIndex.RIGHT_KNEE,
    PoseLandmarkIndex.RIGHT_ANKLE,
    PoseLandmarkIndex.RIGHT_HEEL,
    PoseLandmarkIndex.RIGHT_FOOT_INDEX
)
