/*
 * Copyright 2011-18 Fraunhofer ISE
 *
 * This file is part of OpenMUC.
 * For more information visit http://www.openmuc.org
 *
 * OpenMUC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenMUC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenMUC.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openmuc.framework.server.opcua;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import org.eclipse.milo.opcua.sdk.server.util.HostnameUtil;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateBuilder;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class KeyStoreLoader {
    private static final Logger logger = LoggerFactory.getLogger(KeyStoreLoader.class);

    private static final Pattern IP_ADDR_PATTERN = Pattern.compile(
        "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    private static final String SERVER_ALIAS = "server-ai";
    private static final char[] PASSWORD = "password".toCharArray();

    private X509Certificate[] serverCertificateChain;
    private X509Certificate serverCertificate;
    private KeyPair serverKeyPair;

    KeyStoreLoader load(File baseDir) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");

        File serverKeyStore = baseDir.toPath().resolve("example-server.pfx").toFile();

        logger.debug("Loading KeyStore at {}", serverKeyStore);

        if (!serverKeyStore.exists()) {
            keyStore.load(null, PASSWORD);

            KeyPair keyPair = SelfSignedCertificateGenerator.generateRsaKeyPair(2048);

            String applicationUri = "urn:eclipse:milo:examples:server:" + UUID.randomUUID();

            SelfSignedCertificateBuilder builder = new SelfSignedCertificateBuilder(keyPair)
                .setCommonName("Eclipse Milo Example Server")
                .setOrganization("digitalpetri")
                .setOrganizationalUnit("dev")
                .setLocalityName("Folsom")
                .setStateName("CA")
                .setCountryCode("US")
                .setApplicationUri(applicationUri);

            // Get as many hostnames and IP addresses as we can listed in the certificate.
            Set<String> hostnames = new HashSet<String>();
            hostnames.add(HostnameUtil.getHostname());
            hostnames.addAll(HostnameUtil.getHostnames("0.0.0.0", false));

            for (String hostname : hostnames) {
                if (IP_ADDR_PATTERN.matcher(hostname).matches()) {
                    builder.addIpAddress(hostname);
                } else {
                    builder.addDnsName(hostname);
                }
            }

            X509Certificate certificate = builder.build();

            keyStore.setKeyEntry(SERVER_ALIAS, keyPair.getPrivate(), PASSWORD, new X509Certificate[]{certificate});
            keyStore.store(new FileOutputStream(serverKeyStore), PASSWORD);
        } else {
            keyStore.load(new FileInputStream(serverKeyStore), PASSWORD);
        }

        Key serverPrivateKey = keyStore.getKey(SERVER_ALIAS, PASSWORD);
        if (serverPrivateKey instanceof PrivateKey) {
            serverCertificate = (X509Certificate) keyStore.getCertificate(SERVER_ALIAS);

            serverCertificateChain = Arrays.stream(keyStore.getCertificateChain(SERVER_ALIAS))
                .map(X509Certificate.class::cast)
                .toArray(X509Certificate[]::new);

            PublicKey serverPublicKey = serverCertificate.getPublicKey();
            serverKeyPair = new KeyPair(serverPublicKey, (PrivateKey) serverPrivateKey);
        }

        return this;
    }

    X509Certificate getServerCertificate() {
        return serverCertificate;
    }

    public X509Certificate[] getServerCertificateChain() {
        return serverCertificateChain;
    }

    KeyPair getServerKeyPair() {
        return serverKeyPair;
    }

}
