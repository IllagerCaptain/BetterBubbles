package com.betterbubbles;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterBubbles implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("Better Bubbles");

	@Override
	public void onInitialize() {
		LOGGER.info("Better Bubbles has been initialized!");
	}
}