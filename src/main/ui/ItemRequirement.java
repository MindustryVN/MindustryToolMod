package main.ui;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import mindustry.type.ItemSeq;

public class ItemRequirement {

    public String name;
    public String color;
    public int amount;

    public void setName(String name) {
        this.name = name;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public ItemRequirement(String name, String color, int amount) {
        this.name = name;
        this.color = color;
        this.amount = amount;
    }

    public ItemRequirement() {

    }

    public static List<ItemRequirement> toItemRequirement(ItemSeq itemSeq) {
        return Arrays.asList(itemSeq//
                .toArray())
                .stream()
                .map((r) -> new ItemRequirement(r.item.name, r.item.color.toString(), r.amount))//
                .collect(Collectors.toList());
    }
}
