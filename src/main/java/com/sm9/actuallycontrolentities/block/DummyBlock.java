package com.sm9.actuallycontrolentities.block;

import com.sm9.actuallycontrolentities.Constants;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import static com.sm9.actuallycontrolentities.ActuallyControlEntities.dummyBlock;

public class DummyBlock extends Block {
    private DummyBlock() {
        super(Material.GRASS);
    }

    public static void preInit() {
        dummyBlock = new DummyBlock();
        dummyBlock.setRegistryName(Constants.MOD_ID, "dummy_block");

        ForgeRegistries.BLOCKS.register(dummyBlock);
    }
}