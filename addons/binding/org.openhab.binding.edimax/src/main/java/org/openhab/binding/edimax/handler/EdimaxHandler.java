/**
 * Copyright (c) 2014,2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.edimax.handler;

import static org.openhab.binding.edimax.EdimaxBindingConstants.CHANNEL_POWER;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.edimax.config.EdimaxConfiguration;
import org.openhab.binding.edimax.internal.HTTPSend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EdimaxHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author JWD - Initial contribution
 */
@NonNullByDefault
public class EdimaxHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EdimaxHandler.class);

    @Nullable
    private EdimaxConfiguration config;

    @Nullable
    private HTTPSend httpSend;

    public EdimaxHandler(Thing thing) {
        super(thing);
    }

    @SuppressWarnings("null")
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Channel {} triggered with command {}", channelUID.getId(), command);
        System.err.println(command.toFullString());
        if (channelUID.getId().equals(CHANNEL_POWER)) {
            try {
                if (command.toFullString().equals("ON")) {
                    logger.debug("Sending ON command to {}", config.getIpAddress());
                    httpSend.switchState(config.getIpAddress(), true);
                } else if (command.toFullString().equals("OFF")) {
                    logger.debug("Sending OFF command to {}", config.getIpAddress());
                    httpSend.switchState(config.getIpAddress(), false);
                }

                if (command.toFullString().equals("REFRESH")) {
                    refreshItemState(CHANNEL_POWER);
                }
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        String.format("Could not control device at IP address %s", config.getIpAddress()));
            }
        }
    }

    /**
     * Sync state of thing with visual representation of item in UI
     *
     * @param channel
     * @throws IOException
     */

    public void refreshItemState(String channel) throws Exception {
        boolean isSwitchedOn = httpSend.getState(config.getIpAddress());
        System.err.println(channel + ": Updating state to " + (isSwitchedOn ? "ON" : "OFF"));
        updateState(channel, isSwitchedOn ? OnOffType.ON : OnOffType.OFF);
    }

    /**
     * Initialise the thing and set the visual representation dependent on
     * wether the thing is switched on/off
     */

    @SuppressWarnings("null")
    @Override
    public void initialize() {
        config = getConfigAs(EdimaxConfiguration.class);
        httpSend = new HTTPSend(config.getPassword());

        try {
            refreshItemState(CHANNEL_POWER);
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }
}
