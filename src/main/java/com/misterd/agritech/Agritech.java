package com.misterd.agritech;

import com.misterd.agritech.blocks.ATBlocks;
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
		ATItems.registerATItems();
		ATBlocks.registerATBlocks();
		ATCreativeTab.registerCreativeModeTabs();
	}
}