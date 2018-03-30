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
package org.openhab.binding.edimax;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link EdimaxBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author JWD - Initial contribution
 */
@NonNullByDefault
public class EdimaxBindingConstants {

    public static final String PROPERTY_MAC_ADDRESS = "macAddress";
    public static final String PROPERTY_PASSWORD = "password";
    public static final String PROPERTY_IP_ADDRESS = "ipAddress";

    private static final String BINDING_ID = "edimax";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SP1101W = new ThingTypeUID(BINDING_ID, "sp1101w");

    // List of all Channel ids
    public static final String CHANNEL_POWER = "power";

}
