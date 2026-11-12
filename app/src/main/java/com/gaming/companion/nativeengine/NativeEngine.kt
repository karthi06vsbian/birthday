package com.gaming.companion.nativeengine

object NativeEngine {

    private var isNativeLoaded = false

    init {
        try {
            System.loadLibrary("native-engine")
            isNativeLoaded = true
        } catch (e: UnsatisfiedLinkError) {
            // Fallback gracefully if running in unit tests or desktop preview
            isNativeLoaded = false
        }
    }

    external fun calculateAccuracy(hits: Int, totalShots: Int): Float

    external fun calculateHitZone(
        tapX: Float,
        tapY: Float,
        targetX: Float,
        targetY: Float,
        targetRadius: Float
    ): Int

    external fun calculateDragScore(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        targetHeadX: Float,
        targetHeadY: Float
    ): Float

    external fun evaluatePerformanceRating(
        hits: Int,
        totalShots: Int,
        headshots: Int,
        neckshots: Int,
        durationSeconds: Long
    ): String

    external fun calculateRadarPoint(
        angleDegrees: Float,
        distanceMeters: Float,
        maxRangeMeters: Float,
        radarRadiusPx: Float
    ): FloatArray

    // Pure Kotlin fallbacks if native library cannot be loaded (e.g., in JVM tests)
    fun safeCalculateAccuracy(hits: Int, totalShots: Int): Float {
        return if (isNativeLoaded) {
            calculateAccuracy(hits, totalShots)
        } else {
            if (totalShots <= 0) 0f else (hits.toFloat() / totalShots.toFloat()) * 100f
        }
    }

    fun safeCalculateHitZone(
        tapX: Float,
        tapY: Float,
        targetX: Float,
        targetY: Float,
        targetRadius: Float
    ): Int {
        return if (isNativeLoaded) {
            calculateHitZone(tapX, tapY, targetX, targetY, targetRadius)
        } else {
            val dist = Math.hypot((tapX - targetX).toDouble(), (tapY - targetY).toDouble()).toFloat()
            if (dist > targetRadius) return 0
            val relY = tapY - targetY
            when {
                relY <= -targetRadius * 0.40f -> 3
                relY <= -targetRadius * 0.15f -> 2
                else -> 1
            }
        }
    }

    fun safeEvaluateRating(
        hits: Int,
        totalShots: Int,
        headshots: Int,
        neckshots: Int,
        durationSeconds: Long
    ): String {
        return if (isNativeLoaded) {
            evaluatePerformanceRating(hits, totalShots, headshots, neckshots, durationSeconds)
        } else {
            if (totalShots == 0) return "N/A"
            val hitRate = (hits.toFloat() / totalShots.toFloat()) * 100f
            val hsRate = if (hits > 0) (headshots.toFloat() / hits.toFloat()) * 100f else 0f
            val composite = (hitRate * 0.55f) + (hsRate * 0.45f)
            when {
                composite >= 92f -> "S+ GODLIKE"
                composite >= 82f -> "S ELITE"
                composite >= 70f -> "A PRO"
                composite >= 55f -> "B SOLID"
                else -> "C ROOKIE"
            }
        }
    }

    fun safeCalculateRadarPoint(
        angleDegrees: Float,
        distanceMeters: Float,
        maxRangeMeters: Float,
        radarRadiusPx: Float
    ): Pair<Float, Float> {
        return if (isNativeLoaded) {
            val arr = calculateRadarPoint(angleDegrees, distanceMeters, maxRangeMeters, radarRadiusPx)
            Pair(arr[0], arr[1])
        } else {
            val norm = (distanceMeters / maxRangeMeters).coerceIn(0f, 1f)
            val rad = Math.toRadians((angleDegrees - 90.0).toDouble())
            val r = norm * radarRadiusPx
            Pair((r * Math.cos(rad)).toFloat(), (r * Math.sin(rad)).toFloat())
        }
    }
}
