package de.ellpeck.wopper.proxy;

import de.ellpeck.wopper.Wopper;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;

public class ClientProxy implements IProxy{

    @Override
    public void preInit(){
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(Wopper.blockWopper), 0, new ModelResourceLocation(Wopper.blockWopper.getRegistryName(), "inventory"));
    }
}
