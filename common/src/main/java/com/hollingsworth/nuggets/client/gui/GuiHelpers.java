package com.hollingsworth.nuggets.client.gui;

import com.mojang.datafixers.util.Either;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GuiHelpers {

    public static boolean isMouseInRelativeRange(int mouseX, int mouseY, AbstractWidget widget) {
        return isMouseInRelativeRange(mouseX, mouseY, widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight());
    }

    public static boolean isMouseInRelativeRange(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    public static List<ClientTooltipComponent> gatherTooltipComponents(ItemStack stack, List<? extends FormattedText> textElements, int mouseX, int screenWidth, int screenHeight, Font fallbackFont) {
        return gatherTooltipComponents(stack, textElements, Optional.empty(), mouseX, screenWidth, screenHeight, fallbackFont);
    }

    public static List<ClientTooltipComponent> gatherTooltipComponents(ItemStack stack, List<? extends FormattedText> textElements, Optional<TooltipComponent> itemComponent, int mouseX, int screenWidth, int screenHeight, Font fallbackFont) {
        Font font = fallbackFont;
        List<Either<FormattedText, TooltipComponent>> elements = (List) textElements.stream().map(Either::left).collect(Collectors.toCollection(ArrayList::new));
        itemComponent.ifPresent((c) -> {
            elements.add(1, Either.right(c));
        });

        int tooltipTextWidth = elements.stream().mapToInt((either) -> {
            Objects.requireNonNull(font);
            return (Integer) either.map(font::width, (component) -> {
                return 0;
            });
        }).max().orElse(0);
        boolean needsWrap = false;
        int tooltipX = mouseX + 12;
        if (tooltipX + tooltipTextWidth + 4 > screenWidth) {
            tooltipX = mouseX - 16 - tooltipTextWidth;
            if (tooltipX < 4) {
                if (mouseX > screenWidth / 2) {
                    tooltipTextWidth = mouseX - 12 - 8;
                } else {
                    tooltipTextWidth = screenWidth - 16 - mouseX;
                }

                needsWrap = true;
            }
        }

        if (screenWidth > 0 && tooltipTextWidth > screenWidth) {
            tooltipTextWidth = screenWidth;
            needsWrap = true;
        }

        int tooltipTextWidthF = tooltipTextWidth;
        return needsWrap ? elements.stream().flatMap((either) -> (Stream) either.map((text) -> splitLine(text, font, tooltipTextWidthF), (component) -> Stream.of(ClientTooltipComponent.create(component)))).toList() : elements.stream().map((either) -> either.map((text) -> ClientTooltipComponent.create(text instanceof Component ? ((Component) text).getVisualOrderText() : Language.getInstance().getVisualOrder(text)), ClientTooltipComponent::create)).toList();

    }

    public static Stream<ClientTooltipComponent> splitLine(FormattedText text, Font font, int maxWidth) {
        if (text instanceof Component component) {
            if (component.getString().isEmpty()) {
                return Stream.of(component.getVisualOrderText()).map(ClientTooltipComponent::create);
            }
        }

        return font.split(text, maxWidth).stream().map(ClientTooltipComponent::create);
    }
}
