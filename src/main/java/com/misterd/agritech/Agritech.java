package com.misterd.agritech;

import com.misterd.agritech.block.ATBlocks;
import com.misterd.agritech.blockentity.ATBlockEntities;
import com.misterd.agritech.config.ATConfig;
import com.misterd.agritech.datamap.ATDataMaps;
import com.misterd.agritech.gui.ATMenuTypes;
import com.misterd.agritech.item.ATCreativeTab;
import com.misterd.agritech.item.ATItems;
import com.misterd.agritech.network.ATNetwork;
import com.misterd.agritech.recipe.ATRecipe;
import com.misterd.agritech.recipe.ATRecipeTypes;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Agritech implements ModInitializer {
	public static final String MODID = "agritech";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
	public static int RECIPE_REVISION = 0;

	@Override
	public void onInitialize() {
		ATConfig.register();
		ATRecipeTypes.register();
		ATRecipe.register();
		ATDataMaps.register();
		ATItems.registerATItems();
		ATBlocks.registerATBlocks();
		ATCreativeTab.registerCreativeModeTabs();
		ATBlockEntities.registerBlockEntities();
		ATMenuTypes.registerATMenuTypes();
		ATNetwork.registerATNetwork();

		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(
				new SimpleSynchronousResourceReloadListener() {
					@Override
					public Identifier getFabricId() {
						return Identifier.fromNamespaceAndPath(Agritech.MODID, "recipe_revision_tracker");
					}

					@Override
					public void onResourceManagerReload(ResourceManager manager) {
						Agritech.RECIPE_REVISION++;
					}
				}
		);
	}
}