package dev.fixpot47.armorhudextra.fabric;

import dev.fixpot47.armorhudextra.ArmorHudExtraMod;
import net.fabricmc.api.ClientModInitializer;

public class ArmorHudExtraFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ArmorHudExtraMod.onInitialize();
    }
}
