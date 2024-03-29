/*
 * Copyright 2011-2022 Fraunhofer ISE
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

import static org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig.USER_TOKEN_POLICY_ANONYMOUS;
import static org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig.USER_TOKEN_POLICY_USERNAME;
import static org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig.USER_TOKEN_POLICY_X509;

import java.io.File;
import java.security.KeyPair;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig;
import org.eclipse.milo.opcua.sdk.server.util.HostnameUtil;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.UaRuntimeException;
import org.eclipse.milo.opcua.stack.core.security.DefaultCertificateManager;
import org.eclipse.milo.opcua.stack.core.security.DefaultTrustListManager;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.transport.TransportProfile;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.core.types.structured.BuildInfo;
import org.eclipse.milo.opcua.stack.core.util.CertificateUtil;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateGenerator;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedHttpsCertificateBuilder;
import org.eclipse.milo.opcua.stack.server.EndpointConfiguration;
import org.eclipse.milo.opcua.stack.server.security.DefaultServerCertificateValidator;
import org.openmuc.framework.server.ServerActivator;
import org.openmuc.framework.server.annotation.Configure;
import org.openmuc.framework.server.annotation.Server;
import org.openmuc.framework.server.spi.ServerService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = ServerService.class)
@Server(id = OpcServer.ID)
public class OpcServer extends ServerActivator<OpcChannel> {
    private static final Logger logger = LoggerFactory.getLogger(OpcServer.class);

    public static final String ID = "opcua";

    private static final int TCP_BIND_PORT = 4840;
    private static final int HTTPS_BIND_PORT = 4843;

    private OpcUaServer server;
    private ChannelNamespace namespace;

    static {
        // Required for SecurityPolicy.Aes256_Sha256_RsaPss
        Security.addProvider(new BouncyCastleProvider());
    }

    @Activate
    public void activate() throws Exception {
        logger.info("Activating OPC UA Server");

        File securityTempDir = new File(System.getProperty("java.io.tmpdir"), "security");
        if (!securityTempDir.exists() && !securityTempDir.mkdirs()) {
            throw new Exception("Unable to create security temp dir: " + securityTempDir);
        }
        logger.debug("OPC UA security temp dir: {}", securityTempDir.getAbsolutePath());

        KeyStoreLoader loader = new KeyStoreLoader().load(securityTempDir);

        DefaultCertificateManager certificateManager = new DefaultCertificateManager(loader.getServerKeyPair(),
                loader.getServerCertificateChain());

        File pkiDir = securityTempDir.toPath().resolve("pki").toFile();
        DefaultTrustListManager trustListManager = new DefaultTrustListManager(pkiDir);
        logger.debug("OPC UA pki dir: {}", pkiDir.getAbsolutePath());

        DefaultServerCertificateValidator certificateValidator = 
                new DefaultServerCertificateValidator(trustListManager);

        KeyPair httpsKeyPair = SelfSignedCertificateGenerator.generateRsaKeyPair(2048);

        SelfSignedHttpsCertificateBuilder httpsCertificateBuilder = 
                new SelfSignedHttpsCertificateBuilder(httpsKeyPair);
        httpsCertificateBuilder.setCommonName(HostnameUtil.getHostname());
        HostnameUtil.getHostnames("0.0.0.0").forEach(httpsCertificateBuilder::addDnsName);
        X509Certificate httpsCertificate = httpsCertificateBuilder.build();

//        UsernameIdentityValidator identityValidator = new UsernameIdentityValidator(true, authChallenge -> {
//            String username = authChallenge.getUsername();
//            String password = authChallenge.getPassword();
//
//            boolean userOk = "user".equals(username) && "password1".equals(password);
//            boolean adminOk = "admin".equals(username) && "password2".equals(password);
//
//            return userOk || adminOk;
//        });
//
//        X509IdentityValidator x509IdentityValidator = new X509IdentityValidator(c -> true);

        // If you need to use multiple certificates you'll have to be smarter than this.
        X509Certificate certificate = certificateManager.getCertificates().stream().findFirst()
                .orElseThrow(() -> new UaRuntimeException(StatusCodes.Bad_ConfigurationError,
                        "no certificate found"));

        // The configured application URI must match the one in the certificate(s)
        String applicationUri = CertificateUtil.getSanUri(certificate)
                .orElseThrow(() -> new UaRuntimeException(StatusCodes.Bad_ConfigurationError,
                        "certificate is missing the application URI"));

        Set<EndpointConfiguration> endpointConfigurations = createEndpointConfigurations(certificate);

        OpcUaServerConfig serverConfig = OpcUaServerConfig.builder().setApplicationUri(applicationUri)
                .setApplicationName(LocalizedText.english("OpenMUC OPC UA Server"))
                .setEndpoints(endpointConfigurations)
                .setBuildInfo(new BuildInfo("urn:openmuc:server", "openmuc", "openmuc server",
                        OpcUaServer.SDK_VERSION, "", new DateTime(System.currentTimeMillis())))
                .setCertificateManager(certificateManager).setTrustListManager(trustListManager)
                .setCertificateValidator(certificateValidator).setHttpsKeyPair(httpsKeyPair)
                .setHttpsCertificate(httpsCertificate)
//                .setIdentityValidator(new CompositeValidator(identityValidator, x509IdentityValidator))
                .setProductUri("urn:openmuc:server").build();

        server = new OpcUaServer(serverConfig);
        server.startup();
    }

    @Deactivate
    public void deactivate() {
        logger.info("Deactivating OPC UA Server");
        server.shutdown();
    }

    @Configure
    protected void configure(List<OpcChannel> channels) {
        if (namespace != null) {
            namespace.shutdown();
        }
        namespace = new ChannelNamespace(server);
        
        for (OpcChannel channel : channels) {
            try {
                namespace.addChannelNode(channel);
                
            } catch (UaException e) {
                logger.info("Failed to register UA Node for channel {}: {}", channel.getId(), 
                        e.getMessage());
            }
        }
        namespace.startup();
    }

    private Set<EndpointConfiguration> createEndpointConfigurations(X509Certificate certificate) {
        Set<EndpointConfiguration> endpointConfigurations = new LinkedHashSet<>();

        List<String> bindAddresses = new ArrayList<String>();
        bindAddresses.add("0.0.0.0");

        Set<String> hostnames = new LinkedHashSet<>();
        hostnames.add(HostnameUtil.getHostname());
        hostnames.addAll(HostnameUtil.getHostnames("0.0.0.0"));

        for (String bindAddress : bindAddresses) {
            for (String hostname : hostnames) {
                EndpointConfiguration.Builder builder =
                        EndpointConfiguration.newBuilder().setBindAddress(bindAddress)
                        .setHostname(hostname).setPath("/opc").setCertificate(certificate).addTokenPolicies(
                                USER_TOKEN_POLICY_ANONYMOUS, USER_TOKEN_POLICY_USERNAME,
                                USER_TOKEN_POLICY_X509);

                EndpointConfiguration.Builder noSecurityBuilder = 
                        builder.copy().setSecurityPolicy(SecurityPolicy.None)
                        .setSecurityMode(MessageSecurityMode.None);

                endpointConfigurations.add(buildTcpEndpoint(noSecurityBuilder));
                endpointConfigurations.add(buildHttpsEndpoint(noSecurityBuilder));

                // TCP Basic256Sha256 / SignAndEncrypt
                endpointConfigurations
                        .add(buildTcpEndpoint(builder.copy().setSecurityPolicy(SecurityPolicy.Basic256Sha256)
                                .setSecurityMode(MessageSecurityMode.SignAndEncrypt)));

                // HTTPS Basic256Sha256 / Sign (SignAndEncrypt not allowed for HTTPS)
                endpointConfigurations.add(buildHttpsEndpoint(builder.copy()
                        .setSecurityPolicy(SecurityPolicy.Basic256Sha256).setSecurityMode(MessageSecurityMode.
                                Sign)));

                /*
                 * It's good practice to provide a discovery-specific endpoint with no security.
                 * It's required practice if all regular endpoints have security configured.
                 *
                 * Usage of the "/discovery" suffix is defined by OPC UA Part 6:
                 *
                 * Each OPC UA Server Application implements the Discovery Service Set. If the
                 * OPC UA Server requires a different address for this Endpoint it shall create
                 * the address by appending the path "/discovery" to its base address.
                 */
                EndpointConfiguration.Builder discoveryBuilder = builder.copy().setPath("/opc/discovery")
                        .setSecurityPolicy(SecurityPolicy.None).setSecurityMode(MessageSecurityMode.None);

                endpointConfigurations.add(buildTcpEndpoint(discoveryBuilder));
                endpointConfigurations.add(buildHttpsEndpoint(discoveryBuilder));
            }
        }

        return endpointConfigurations;
    }

    private static EndpointConfiguration buildTcpEndpoint(EndpointConfiguration.Builder base) {
        return base.copy().setTransportProfile(TransportProfile.TCP_UASC_UABINARY)
                .setBindPort(TCP_BIND_PORT).build();
    }

    private static EndpointConfiguration buildHttpsEndpoint(EndpointConfiguration.Builder base) {
        return base.copy().setTransportProfile(TransportProfile.HTTPS_UABINARY)
                .setBindPort(HTTPS_BIND_PORT).build();
    }

    public CompletableFuture<OpcUaServer> startup() {
        return server.startup();
    }

    public CompletableFuture<OpcUaServer> shutdown() {
        return server.shutdown();
    }

}
