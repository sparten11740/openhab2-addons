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
package org.openhab.binding.edimax.config;

/**
 * The {@link EdimaxConfiguration} class contains fields mapping thing configuration paramters.
 *
 * @author JWD - Initial contribution
 */
public class EdimaxConfiguration {

    public String ipAddress;
    public String macAddress;
    public String password;

    public String getIpAddress() {
        return ipAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getPassword() {
        return password;
    }

}
