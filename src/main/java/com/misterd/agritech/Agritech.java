package com.misterd.agritech;

import com.misterd.agritech.block.ATBlocks;
import com.misterd.agritech.blockentity.ATBlockEntities;
import com.misterd.agritech.config.Config;
import com.misterd.agritech.config.PlantablesConfig;
import com.misterd.agritech.gui.ATMenuTypes;
import com.misterd.agritech.item.ATCreativeTab;
import com.misterd.agritech.item.ATItems;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Agritech implements ModInitializer {
	public static final String MODID = "agritech";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	@Override
	public void onInitialize() {
		Config.load();
		PlantablesConfig.loadConfig();

		ATItems.registerATItems();
		ATBlocks.registerATBlocks();
		ATCreativeTab.registerCreativeModeTabs();
		ATBlockEntities.registerBlockEntities();
		ATMenuTypes.registerATMenuTypes();
	}
}