package ru.ptrff.ktx_3d_wallpaper.wallpaper


class PIDController(
    private val kp: Float,
    private val ki: Float,
    private val kd: Float
) {
    private var previousErrorX: Float = 0f
    private var previousErrorY: Float = 0f
    private var previousErrorZ: Float = 0f
    private var integralX: Float = 0f
    private var integralY: Float = 0f
    private var integralZ: Float = 0f

    fun compute(
        targetX: Float,
        currentX: Float,
        targetY: Float,
        currentY: Float,
        targetZ: Float,
        currentZ: Float,
        deltaTime: Float
    ): Triple<Float, Float, Float> {
        val errorX = targetX - currentX
        val errorY = targetY - currentY
        val errorZ = targetZ - currentZ

        integralX += errorX * deltaTime
        integralY += errorY * deltaTime
        integralZ += errorZ * deltaTime

        val derivativeX = (errorX - previousErrorX) / deltaTime
        val derivativeY = (errorY - previousErrorY) / deltaTime
        val derivativeZ = (errorZ - previousErrorZ) / deltaTime

        previousErrorX = errorX
        previousErrorY = errorY
        previousErrorZ = errorZ

        val outputX = kp * errorX + ki * integralX + kd * derivativeX
        val outputY = kp * errorY + ki * integralY + kd * derivativeY
        val outputZ = kp * errorZ + ki * integralZ + kd * derivativeZ

        return Triple(currentX + outputX, currentY + outputY, currentZ + outputZ)
    }
}

