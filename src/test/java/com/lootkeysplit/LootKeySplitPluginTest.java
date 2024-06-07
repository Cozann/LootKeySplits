package com.lootkeysplit;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class LootKeySplitPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(LootKeySplitPlugin.class);
		RuneLite.main(args);
	}
}