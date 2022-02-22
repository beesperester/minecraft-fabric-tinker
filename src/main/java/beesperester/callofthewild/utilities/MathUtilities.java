package beesperester.callofthewild.utilities;

public class MathUtilities {
    public static float fitRange(float value, float srcMin, float srcMax, float destMin, float destMax) {
        return ((value - srcMin) / (srcMax - srcMin)) * (destMax - destMin) + destMin;
    }

    public static float lerp(float bias, float a, float b) {
        return (bias * (b - a)) + a;
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(Math.min(value, max), min);
    }
}
