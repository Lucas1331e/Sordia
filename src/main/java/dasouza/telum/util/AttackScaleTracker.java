package dasouza.telum.util;

public final class AttackScaleTracker {

    private static final ThreadLocal<Float> LAST_ATTACK_SCALE = ThreadLocal.withInitial(() -> 1.0f);

    private AttackScaleTracker() {}

    public static void setLastAttackScale(float scale) {
        LAST_ATTACK_SCALE.set(scale);
    }

    public static float getLastAttackScale() {
        return LAST_ATTACK_SCALE.get();
    }
}
