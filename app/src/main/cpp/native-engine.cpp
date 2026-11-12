#include <jni.h>
#include <string>
#include <cmath>
#include <algorithm>
#include <android/log.h>

#define TAG "NativeGamingEngine"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

extern "C" {

/**
 * Calculates hit accuracy percentage (0.0f - 100.0f)
 */
JNIEXPORT jfloat JNICALL
Java_com_gaming_companion_nativeengine_NativeEngine_calculateAccuracy(
        JNIEnv *env,
        jobject /* this */,
        jint hits,
        jint totalShots) {
    if (totalShots <= 0) return 0.0f;
    float accuracy = (static_cast<float>(hits) / static_cast<float>(totalShots)) * 100.0f;
    return std::clamp(accuracy, 0.0f, 100.0f);
}

/**
 * Calculates hit zone for aim training:
 * 0 = Miss
 * 1 = Body
 * 2 = Neck
 * 3 = Head
 */
JNIEXPORT jint JNICALL
Java_com_gaming_companion_nativeengine_NativeEngine_calculateHitZone(
        JNIEnv *env,
        jobject /* this */,
        jfloat tapX,
        jfloat tapY,
        jfloat targetX,
        jfloat targetY,
        jfloat targetRadius) {

    float distance = std::hypot(tapX - targetX, tapY - targetY);
    if (distance > targetRadius) {
        return 0; // Miss
    }

    // Relative height on target (-targetRadius at top, +targetRadius at bottom)
    float relativeY = tapY - targetY;

    // Top 30% is Head zone
    if (relativeY <= -targetRadius * 0.40f) {
        return 3; // Head
    }
    // Next 25% is Neck zone
    else if (relativeY <= -targetRadius * 0.15f) {
        return 2; // Neck
    }
    // Remaining lower section is Body zone
    else {
        return 1; // Body
    }
}

/**
 * Calculates drag shot alignment score [0.0 - 100.0]
 * Evaluates vertical upward swipe precision typical of mobile battle royale drag shooting
 */
JNIEXPORT jfloat JNICALL
Java_com_gaming_companion_nativeengine_NativeEngine_calculateDragScore(
        JNIEnv *env,
        jobject /* this */,
        jfloat startX,
        jfloat startY,
        jfloat endX,
        jfloat endY,
        jfloat targetHeadX,
        jfloat targetHeadY) {

    float deltaX = endX - startX;
    float deltaY = endY - startY; // In screen coords, negative deltaY is upward drag

    // Ideal drag should be upward towards head
    float distanceToEnd = std::hypot(endX - targetHeadX, endY - targetHeadY);
    float score = 100.0f - (distanceToEnd * 0.5f);

    // Bonus for vertical discipline (less horizontal wobble)
    float horizontalDev = std::abs(deltaX);
    score -= horizontalDev * 0.15f;

    return std::clamp(score, 0.0f, 100.0f);
}

/**
 * Evaluates overall session performance rating string (S+, S, A, B, C)
 */
JNIEXPORT jstring JNICALL
Java_com_gaming_companion_nativeengine_NativeEngine_evaluatePerformanceRating(
        JNIEnv *env,
        jobject /* this */,
        jint hits,
        jint totalShots,
        jint headshots,
        jint neckshots,
        jlong durationSeconds) {

    if (totalShots == 0) {
        return env->NewStringUTF("N/A");
    }

    float hitRate = (static_cast<float>(hits) / static_cast<float>(totalShots)) * 100.0f;
    float headshotRate = (hits > 0) ? (static_cast<float>(headshots) / static_cast<float>(hits)) * 100.0f : 0.0f;

    // Composite scoring formula
    float compositeScore = (hitRate * 0.55f) + (headshotRate * 0.45f);

    std::string rating;
    if (compositeScore >= 92.0f) {
        rating = "S+ GODLIKE";
    } else if (compositeScore >= 82.0f) {
        rating = "S ELITE";
    } else if (compositeScore >= 70.0f) {
        rating = "A PRO";
    } else if (compositeScore >= 55.0f) {
        rating = "B SOLID";
    } else {
        rating = "C ROOKIE";
    }

    return env->NewStringUTF(rating.c_str());
}

/**
 * Computes Radar X/Y coordinates given distance in meters and angle in degrees
 * Maps real world range (0 - 100m) to pixel radius
 */
JNIEXPORT jfloatArray JNICALL
Java_com_gaming_companion_nativeengine_NativeEngine_calculateRadarPoint(
        JNIEnv *env,
        jobject /* this */,
        jfloat angleDegrees,
        jfloat distanceMeters,
        jfloat maxRangeMeters,
        jfloat radarRadiusPx) {

    float normalizedDist = std::clamp(distanceMeters / maxRangeMeters, 0.0f, 1.0f);
    float radiusPx = normalizedDist * radarRadiusPx;

    // Convert angle degrees to radians (-90 deg offset so 0 deg is North/Top)
    float rad = (angleDegrees - 90.0f) * (M_PI / 180.0f);

    float x = radiusPx * std::cos(rad);
    float y = radiusPx * std::sin(rad);

    jfloatArray result = env->NewFloatArray(2);
    jfloat coords[2] = {x, y};
    env->SetFloatArrayRegion(result, 0, 2, coords);

    return result;
}

} // extern "C"
