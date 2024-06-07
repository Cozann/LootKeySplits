package com.lootkeysplit;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

import net.runelite.api.ChatMessageType;
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


@Slf4j
@PluginDescriptor(name = "LootKey Splits")

public class LootKeySplitPlugin extends Plugin
{
	private static final String BASE_DIRECTORY = RuneLite.RUNELITE_DIR + "/chatlogs/";

	@Inject
	private Client client;

	@Inject
	private LootKeySplitConfig config;

	private Logger clanChatLogger;
	private boolean can_load = false;

	@Override
	protected void startUp() throws Exception
	{
		if(client.getGameState().equals(GameState.LOGGED_IN)){
			triggerInit();
		}
		log.info("LookKey Split started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("LookKey Split stopped!");
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
				if (config.StartLootLog()) {
					clanChatLogger.info("{}: {}", event.getName(), event.getMessage());
				}
			case CLAN_GUEST_CHAT:

			case CLAN_MESSAGE:
				if (config.StartLootLog()) {
						clanChatLogger.info("{}", event.getMessage());
					}

			case PRIVATECHAT:

			case MODPRIVATECHAT:

			case PRIVATECHATOUT:

			case MODCHAT:

			case PUBLICCHAT:

		}
	}

	@Provides
	LootKeySplitConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(LootKeySplitConfig.class);
	}

	private Logger setupLogger(String loggerName, String subFolder) {
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

		PatternLayoutEncoder encoder = new PatternLayoutEncoder();
		encoder.setContext(context);
		encoder.setPattern("%d{HH:mm:ss} %msg%n");
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
