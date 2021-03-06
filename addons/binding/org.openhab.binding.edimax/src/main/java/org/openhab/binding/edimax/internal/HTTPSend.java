/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.edimax.internal;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Authentication;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.DigestAuthentication;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.edimax.internal.commands.GetCurrent;
import org.openhab.binding.edimax.internal.commands.GetMAC;
import org.openhab.binding.edimax.internal.commands.GetPower;
import org.openhab.binding.edimax.internal.commands.GetState;
import org.openhab.binding.edimax.internal.commands.SetState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sends commands and returns responses for the edimax device, using it's http
 * interface.
 *
 * @author Heinz
 * @author JWD - replaced apache http client with jetty
 *
 */
public class HTTPSend {

    private final static Logger logger = LoggerFactory.getLogger(HTTPSend.class);

    private static final int TIMEOUT = 5000;
    public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF8\"?>\r\n";

    private static final String defaultUser = "admin";
    private static final String defaultPassword = "1234";

    protected static final int PORT = 10000;

    private static String completeURL(String anIp) {
        return "http://" + anIp;
    }

    private String password;

    public HTTPSend() {
        this(defaultPassword);
    }

    public HTTPSend(String aPw) {
        password = aPw;
    }

    /**
     * Switch to.
     *
     * @param anIp
     * @param newState
     * @return
     * @throws IOException
     */
    public Boolean switchState(String anIp, Boolean newState) throws Exception {
        String completeUrl = completeURL(anIp);
        ConnectionInformation ci = new ConnectionInformation(defaultUser, password, completeUrl, PORT);

        SetState setS = new SetState(newState);
        return setS.executeCommand(ci);
    }

    /**
     * Returns state for device with given IP.
     *
     * @param anIp
     * @return
     * @throws IOException
     */
    public Boolean getState(String anIp) throws Exception {
        String completeUrl = completeURL(anIp);
        ConnectionInformation ci = new ConnectionInformation(defaultUser, password, completeUrl, PORT);

        GetState getS = new GetState();
        return getS.executeCommand(ci);
    }

    /**
     * Receive the MAC address.
     *
     * @param anIp
     * @return
     * @throws IOException
     */
    public String getMAC(String anIp) throws Exception {
        String completeUrl = completeURL(anIp);
        ConnectionInformation ci = new ConnectionInformation(defaultUser, password, completeUrl, PORT);

        GetMAC getC = new GetMAC();
        return getC.executeCommand(ci);
    }

    /**
     * Returns the current.
     *
     * @param anIp
     * @return
     * @throws IOException
     */
    public BigDecimal getCurrent(String anIp) throws Exception {
        String completeUrl = completeURL(anIp);
        ConnectionInformation ci = new ConnectionInformation(defaultUser, password, completeUrl, PORT);

        GetCurrent getC = new GetCurrent();
        return getC.executeCommand(ci);
    }

    /**
     * Gets the actual power.
     *
     * @param anIp
     * @return
     * @throws Exception
     * @throws IOExceptionif
     *             (mac != null) { // found a device! Device d = new Device();
     *             d.ip = portScanUsage.getIp(); d.mac = mac; discovered.add(d);
     *             }
     */
    public BigDecimal getPower(String anIp) throws Exception {
        String completeUrl = completeURL(anIp);
        ConnectionInformation ci = new ConnectionInformation(defaultUser, password, completeUrl, PORT);

        GetPower getC = new GetPower();
        return getC.executeCommand(ci);
    }

    public static String post(String targetURL, int targetPort, String targetURlPost, String data, String username,
            String password) throws Exception {

        String complete = targetURL + ":" + targetPort + "/" + targetURlPost;

        HttpClient client = new HttpClient();
        client.start();

        AuthenticationStore a = client.getAuthenticationStore();
        a.addAuthentication(new DigestAuthentication(new URI(complete), Authentication.ANY_REALM, username, password));

        // @formatter:off
        ContentResponse response = client.newRequest(complete)
                .method(HttpMethod.POST)
                .content(new StringContentProvider(data))
                .timeout(TIMEOUT, TimeUnit.MILLISECONDS)
                .send();


        int statusCode = response.getStatus();

        if (statusCode == HttpStatus.UNAUTHORIZED_401) {
            String statusLine = response.getStatus() + " " + response.getReason();
            logger.error("Received '{}' from server", statusLine);
        }

        if (statusCode != HttpStatus.OK_200) {
            String statusLine = response.getStatus() + " " + response.getReason();
            logger.error("HTTP POST method failed: {}", statusLine);
        }

        return response.getContentAsString();
    }
}
