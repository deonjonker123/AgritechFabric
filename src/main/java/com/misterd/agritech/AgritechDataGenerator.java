package com.misterd.agritech;

import com.misterd.agritech.datagen.custom.ATBlockTagProvider;
import com.misterd.agritech.datagen.custom.ATLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class AgritechDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		pack.addProvider(ATLootTableProvider::new);
		pack.addProvider(ATBlockTagProvider::new);
	}
}
