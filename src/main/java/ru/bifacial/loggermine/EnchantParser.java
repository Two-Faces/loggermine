package ru.bifacial.loggermine;

import org.bukkit.enchantments.Enchantment;

import java.util.Map;
import java.util.StringJoiner;

public class EnchantParser {

    public String parse(Map<Enchantment, Integer> list) {
        StringJoiner string = new StringJoiner("&");

        list.forEach((Enchantment enchant, Integer level) -> {
            string.add(enchant.getName() + "|" + level);
        });

        return string.toString();
    }
}
