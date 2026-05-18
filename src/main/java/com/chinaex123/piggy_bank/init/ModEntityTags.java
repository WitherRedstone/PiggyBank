package com.chinaex123.piggy_bank.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public interface ModEntityTags {

    TagKey<EntityType<?>> PIG = mcTag("pig");

    private static TagKey<EntityType<?>> mcTag(String name) {
        return TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("minecraft", name));
    }
}
