package net.shizotoaster.foglooksmodernnow.compat;

import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraftforge.fml.ModList;

public class IrisShadersCompat {
    public static boolean isUsingShaders() {
        if (ModList.get().isLoaded("iris")) {
            return  IrisApi.getInstance().isShaderPackInUse();
        }
        return false;
    }
}
