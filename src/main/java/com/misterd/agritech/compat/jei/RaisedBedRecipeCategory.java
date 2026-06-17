package com.misterd.agritech.compat.jei;

import com.misterd.agritech.block.ATBlocks;
import com.misterd.agritech.recipe.DropEntry;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class RaisedBedRecipeCategory implements IRecipeCategory<RaisedBedRecipe> {
    public static final Identifier UID = Identifier.fromNamespaceAndPath("agritech", "raised_bed");
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("agritech", "textures/gui/jei/jei_raised_be_gui.png");
    public static final IRecipeType<RaisedBedRecipe> RAISED_BED_RECIPE_TYPE = IRecipeType.create(UID, RaisedBedRecipe.class);
    private final IDrawable background;
    private final IDrawable icon;

    public RaisedBedRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 162, 63);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ATBlocks.OAK_RAISED_BED));
    }

    @Override
    public IRecipeType<RaisedBedRecipe> getRecipeType() {
        return RAISED_BED_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.agritech.raised_bed.tooltip");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return 162;
    }

    @Override
    public int getHeight() {
        return 63;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RaisedBedRecipe recipe, IFocusGroup focuses) {
        var plantSlot = builder.addSlot(RecipeIngredientRole.INPUT, 10, 28);
        recipe.getPlant().items().map(h -> new ItemStack(h.value())).forEach(plantSlot::add);

        var soilSlot = builder.addSlot(RecipeIngredientRole.INPUT, 46, 28);
        for (var soilIngredient : recipe.getSoils()) {
            soilIngredient.items().map(h -> new ItemStack(h.value())).forEach(soilSlot::add);
        }

        List<DropEntry> drops = recipe.getDrops();
        int outputIndex = 0;
        for (ItemStack output : recipe.getOutputs()) {
            if (outputIndex >= 8) break;
            int x = 82 + outputIndex % 4 * 18;
            int y = 19 + outputIndex / 4 * 18;

            final DropEntry entry = outputIndex < drops.size() ? drops.get(outputIndex) : null;

            var slot = builder.addSlot(RecipeIngredientRole.OUTPUT, x, y).add(output);

            if (entry != null) {
                slot.addRichTooltipCallback((slotView, tooltip) -> {
                    String countStr = entry.min() == entry.max()
                            ? String.valueOf(entry.min())
                            : entry.min() + "–" + entry.max();
                    tooltip.add(Component.literal("Count: " + countStr).withStyle(ChatFormatting.GRAY));
                    if (entry.chance() < 1.0F) {
                        tooltip.add(Component.literal(String.format("%.0f%% chance", entry.chance() * 100))
                                .withStyle(ChatFormatting.GOLD));
                    }
                });
            }

            outputIndex++;
        }
    }

    @Override
    public void draw(RaisedBedRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics);

        Font font = Minecraft.getInstance().font;

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(81, 8);
        guiGraphics.pose().scale(0.8f, 0.8f);
        guiGraphics.text(font,
                Component.translatable("jei.agritech.raised_bed.drops_on_ground").withStyle(ChatFormatting.DARK_GRAY),
                0, 0, 0xFF323232, false);
        guiGraphics.pose().popMatrix();
    }
}