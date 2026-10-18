package dev.fixpot47.armorhudextra.compat;

public interface ModCompat {
    float hudOpacity();
    int screenSafeArea();

    class NoOpModCompat implements ModCompat {
        @Override
        public float hudOpacity() {
            return 1.0F;
        }

        @Override
        public int screenSafeArea() {
            return 0;
        }
    }
}
