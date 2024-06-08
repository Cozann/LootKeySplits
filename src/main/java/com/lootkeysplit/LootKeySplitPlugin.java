package com.lootkeysplit;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import com.lootkeysplit.LootKeySplitPanel;

import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import ch.qos.logback.classic.*;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import net.runelite.client.RuneLite;
import net.runelite.api.events.*;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.io.IOException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import net.runelite.client.util.ImageUtil;

@PluginDescriptor(name = "LootKey Splits",
		description = "lootkey splits enable to use",
		loadWhenOutdated = true)

public class LootKeySplitPlugin extends Plugin
{
	private static final String BASE_DIRECTORY = RuneLite.RUNELITE_DIR + "/chatlogs/";

	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	private LootKeySplitPanel panel;
	private NavigationButton navButton;

	@Inject
	private LootKeySplitConfig config;

	private Logger clanChatLogger;
	private boolean can_load = false;
	public static LootKeySplitConfig CONFIG;
	public static LootKeySplitPlugin PLUGIN;

	public static String logfilepath = RuneLite.RUNELITE_DIR + "/chatlogs/clan/latest.log";

	@Provides
	LootKeySplitConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(LootKeySplitConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		CONFIG = config; // save static instances of config/plugin to easily use in
		PLUGIN = this;   // other contexts without passing them all the way down or injecting

		panel = new LootKeySplitPanel(this,this.config);

		navButton = NavigationButton.builder()
				.tooltip("SPLITS")
				.icon(ImageUtil.loadImageResource(getClass(), "/lootkey.png"))
				.priority(15)
				.panel(panel)
				.build();

		clientToolbar.addNavigation(navButton);

		if(client.getGameState().equals(GameState.LOGGED_IN)){
			triggerInit();
		}
	}

	@Override
	protected void shutDown()
	{
		clientToolbar.removeNavigation(navButton);
		panel = null;
		navButton = null;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			triggerInit();
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick) {
		// If we are logging per player, wait until we have the player login name
		if (can_load && client.getLocalPlayer().getName() != null) {
			initLoggers();
			can_load = false;
		}
	}

	private void triggerInit() {
		can_load = true;
	}

	private void stopTracking(){

	}

	public void resetChatLog() throws IOException {
		new FileWriter(logfilepath, false).close();
	}

	private void initLoggers() {
		clanChatLogger = setupLogger("ClanChatLogger", "clan");
	}

	@Subscribe
	public void onChatMessage(ChatMessage event) {

		switch (event.getType()) {
			case CLAN_GIM_CHAT:

			case CLAN_GIM_MESSAGE:

			case CLAN_GIM_FORM_GROUP:

			case CLAN_GIM_GROUP_WITH:

			case FRIENDSCHAT:

			case GAMEMESSAGE:

			case CLAN_CHAT:

			case CLAN_GUEST_CHAT:

			case CLAN_MESSAGE:
				if (SharedState.isLoggingEnabled) {
						clanChatLogger.info("{}", event.getMessage());
					}

			case PRIVATECHAT:

			case MODPRIVATECHAT:

			case PRIVATECHATOUT:

			case MODCHAT:

			case PUBLICCHAT:
		}
	}


	private Logger setupLogger(String loggerName, String subFolder) {
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

		PatternLayoutEncoder encoder = new PatternLayoutEncoder();
		encoder.setContext(context);
		encoder.setPattern("%msg%n");
		encoder.start();

		String directory = BASE_DIRECTORY;

		directory += subFolder + "/";

		RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
		appender.setFile(directory + "latest.log");
		appender.setAppend(true);
		appender.setEncoder(encoder);
		appender.setContext(context);

		TimeBasedRollingPolicy<ILoggingEvent> logFilePolicy = new TimeBasedRollingPolicy<>();
		logFilePolicy.setContext(context);
		logFilePolicy.setParent(appender);
		logFilePolicy.setFileNamePattern(directory + "chatlog_%d{yyyy-MM-dd}.log");
		logFilePolicy.setMaxHistory(30);
		logFilePolicy.start();

		appender.setRollingPolicy(logFilePolicy);
		appender.start();

		Logger logger = context.getLogger(loggerName);
		logger.detachAndStopAllAppenders();
		logger.setAdditive(false);
		logger.setLevel(Level.INFO);
		logger.addAppender(appender);

		return logger;
	}

}

