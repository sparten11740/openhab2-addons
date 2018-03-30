/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.edimax.discovery;

import static org.openhab.binding.edimax.EdimaxBindingConstants.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.edimax.internal.EdimaxDevice;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Auto-discover Edimax plugs by broadcasting using all available
 * network adapters
 *
 * @author JWD
 *
 */
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.edimax")
public class UDPDiscoveryService extends AbstractDiscoveryService {

    private static final int DISCOVER_TIMEOUT_SECONDS = 30;
    private final Logger logger = LoggerFactory.getLogger(UDPDiscoveryService.class);

    public UDPDiscoveryService() {
        super(Collections.singleton(THING_TYPE_SP1101W), DISCOVER_TIMEOUT_SECONDS, false);
    }

    /**
     * Discovery Package.
     */
    byte[] DISCOVERY_BYTES = { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x45, 0x44,
            0x49, 0x4d, 0x41, 0x58, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xa1, (byte) 0xff, 0x5e };

    @Override
    protected void startScan() {
        try {
            discover();
            super.stopScan();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void discover() throws SocketException, UnknownHostException, IOException {
        DatagramSocket serverSocket = null;
        try {
            serverSocket = new DatagramSocket();
            serverSocket.setBroadcast(true);

            for (InetAddress addr : listAllBroadcastAddresses()) {
                sendUDPPacket(serverSocket, InetAddress.getByName(addr.getHostAddress()));
                try {
                    while (true) {
                        receiveUDPPacket(serverSocket);
                    }
                } catch (SocketTimeoutException e) {
                    // intended to happen
                }
            }

        } finally {
            if (serverSocket != null) {
                serverSocket.close();
            }
        }
    }

    /**
     * Broadcast discovery bytes payload from ip address
     *
     * @param serverSocket
     * @param ipAddress
     * @throws IOException
     */

    private void sendUDPPacket(DatagramSocket serverSocket, InetAddress ipAddress) throws IOException {
        byte[] sendData = DISCOVERY_BYTES;
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, 20560);
        serverSocket.send(sendPacket);
        serverSocket.setSoTimeout(1000 * 5);
    }

    /**
     * Handle incoming UDP packets
     *
     * @param serverSocket
     * @throws IOException
     */

    private void receiveUDPPacket(DatagramSocket serverSocket) throws IOException {
        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

        serverSocket.receive(receivePacket);
        String sentence = new String(receivePacket.getData());

        if (!StringUtils.isEmpty(sentence) && sentence.contains("EDIMAX")) {
            byte[] mac = new byte[6];
            System.arraycopy(receivePacket.getData(), 0, mac, 0, 6);

            System.err.println(Hex.encodeHexString(receivePacket.getData()));

            String encodedMAC = Hex.encodeHexString(mac).toUpperCase();
            InetAddress discoveredIp = receivePacket.getAddress();

            EdimaxDevice dev = new EdimaxDevice();
            dev.setIp(discoveredIp.getHostAddress());
            dev.setMac(encodedMAC);

            notifyDiscovered(dev);
        }
    }

    private static List<InetAddress> listAllBroadcastAddresses() throws SocketException {
        List<InetAddress> broadcastList = new ArrayList<>();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();

            if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                continue;
            }

            networkInterface.getInterfaceAddresses().stream().map(a -> a.getBroadcast()).filter(Objects::nonNull)
                    .forEach(broadcastList::add);
        }
        return broadcastList;
    }

    public void notifyDiscovered(EdimaxDevice dev) {
        logger.debug(String.format("SP-1101W [%s] discovered", dev.getIp()));
        ThingUID sp1101wThing = new ThingUID(THING_TYPE_SP1101W, dev.getMac().replace(":", "-"));

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(sp1101wThing)
                .withLabel(String.format("SP-1101W [%s]", dev.getIp())).withRepresentationProperty(dev.getMac())
                .withProperty(PROPERTY_MAC_ADDRESS, dev.getMac()).withProperty(PROPERTY_IP_ADDRESS, dev.getIp())
                .build();

        thingDiscovered(discoveryResult);
    }
}
