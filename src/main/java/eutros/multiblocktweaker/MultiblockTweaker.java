package eutros.multiblocktweaker;

import eutros.multiblocktweaker.client.PreviewHandler;
import gregtech.GTInternalTags;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

@Mod(modid = MultiblockTweaker.MOD_ID,
        name = "Multiblock Tweaker",
        version = MBTTags.VERSION,
        dependencies = GTInternalTags.DEP_VERSION_STRING)
public class MultiblockTweaker {

    public static final String MOD_ID = "multiblocktweaker";

    public MultiblockTweaker() {
    }

    @Mod.EventHandler
    public static void postInit(FMLPostInitializationEvent evt) {
        if (evt.getSide().isClient())
            PreviewHandler.init();
    }

}
