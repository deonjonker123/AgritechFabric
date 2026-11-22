package com.misterd.agritech;

import com.misterd.agritech.block.ATBlocks;
import com.misterd.agritech.blockentity.ATBlockEntities;
import com.misterd.agritech.config.ATConfig;
import com.misterd.agritech.datamap.ATDataMaps;
import com.misterd.agritech.gui.ATMenuTypes;
import com.misterd.agritech.item.ATCreativeTab;
import com.misterd.agritech.item.ATItems;
import com.misterd.agritech.network.ATNetwork;
import com.misterd.agritech.recipe.ATRecipeTypes;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Agritech implements ModInitializer {
	public static final String MODID = "agritech";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	@Override
	public void onInitialize() {
		ATConfig.register();
		ATRecipeTypes.register();
		ATDataMaps.register();
		ATItems.registerATItems();
		ATBlocks.registerATBlocks();
		ATCreativeTab.registerCreativeModeTabs();
		ATBlockEntities.registerBlockEntities();
		ATMenuTypes.registerATMenuTypes();
		ATNetwork.registerATNetwork();
	}
}