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
import java.net.HttpURLConnection;
import java.util.Collections;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.openhab.binding.edimax.internal.commands.GetCurrent;
import org.openhab.binding.edimax.internal.commands.GetMAC;
import org.openhab.binding.edimax.internal.commands.GetPower;
import org.openhab.binding.edimax.internal.commands.GetState;
import org.openhab.binding.edimax.internal.commands.SetState;

/**
 * Sends commands and returns responses for the edimax device, using it's http
 * interface.
 *
 * @author Heinz
 *
 */
public class HTTPSend {

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
    public Boolean switchState(String anIp, Boolean newState) throws IOException {
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
    public Boolean getState(String anIp) throws IOException {
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
    public String getMAC(String anIp) throws IOException {
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
    public BigDecimal getCurrent(String anIp) throws IOException {
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
     * @throws IOExceptionif
     *             (mac != null) { // found a device! Device d = new Device();
     *             d.ip = portScanUsage.getIp(); d.mac = mac; discovered.add(d);
     *             }
     */
    public BigDecimal getPower(String anIp) throws IOException {
        String completeUrl = completeURL(anIp);
        ConnectionInformation ci = new ConnectionInformation(defaultUser, password, completeUrl, PORT);

        GetPower getC = new GetPower();
        return getC.executeCommand(ci);
    }

    public static String executePost(String targetURL, int targetPort, String targetURlPost, String data,
            String username, String password) throws IOException {
        String complete = targetURL + ":" + targetPort + "/" + targetURlPost;

        HttpURLConnection connection = null;
        try {

            HttpClient client = new HttpClient();
            Credentials creds = new UsernamePasswordCredentials(username, password);
            client.getState().setCredentials(AuthScope.ANY, creds);
            client.getParams().setAuthenticationPreemptive(true);
            PostMethod post = new PostMethod(complete);
            post.setDoAuthentication(true);
            HttpMethodParams params = post.getParams();
            params.setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, Collections.singleton(AuthPolicy.DIGEST));
            post.setRequestHeader("Connection", "Keep-Alive");
            post.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
            post.setRequestHeader("Content-Length", Integer.toString(data.getBytes().length));
            ByteArrayRequestEntity requestEntity = new ByteArrayRequestEntity(data.getBytes());

            post.setRequestEntity(requestEntity);
            client.executeMethod(post);

            return post.getResponseBodyAsString();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

}
