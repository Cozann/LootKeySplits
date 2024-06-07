package com.lootkeysplit;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("LootKeySplit")
public interface LootKeySplitConfig extends Config
{

	@ConfigItem(
			keyName = "clan",
			name = "Clan Chat",
			description = "Enables logging of the clan chat"
	)

	default boolean StartLootLog() {
		return true;
	}
}
