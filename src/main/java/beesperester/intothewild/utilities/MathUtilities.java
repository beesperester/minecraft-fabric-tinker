package beesperester.intothewild.utilities;

public class MathUtilities {
    public static float fitRange(float value, float srcMin, float srcMax, float destMin, float destMax) {
        return fitRange(value, srcMin, srcMax, destMin, destMax, false);
    }

    public static float fitRange(float value, float srcMin, float srcMax, float destMin, float destMax, boolean clamp) {
        float result = ((value - srcMin) / (srcMax - srcMin)) * (destMax - destMin) + destMin;

        return clamp ? clamp(result, destMin, destMax) : result;
    }

    public static float lerp(float bias, float a, float b) {
        return lerp(bias, a, b, false);
    }

    public static float lerp(float bias, float a, float b, boolean clamp) {
        float result = (bias * (b - a)) + a;

        return clamp ? clamp(result, a, b) : result;
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(Math.min(value, max), min);
    }

    public static float distance(float a, float b) {
        return Math.abs(a - b);
    }

    public static float easeInCubic(float x) {
        return x * x * x;
    }

    public static float easeOutCubic(float x) {
        return (float) (1.0 - Math.pow(1.0 - x, 3));
    }

    public static float easeInOutCubic(float x) {
        return (float) ((x < 0.5) ? (4.0 * x * x * x) : (1.0 - Math.pow((-2.0 * x) + 2.0, 3.0) / 2.0));
    }

    public static float easeInCirc(float x) {
        return (float) (-Math.sin(Math.acos(x)) + 1.0);
    }

    public static float easeOutCirc(float x) {
        return (float) (Math.sin(Math.acos(x - 1.0)));
    }
}
