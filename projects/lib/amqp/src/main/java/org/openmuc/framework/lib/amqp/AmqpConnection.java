/*
 * Copyright 2011-2020 Fraunhofer ISE
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

package org.openmuc.framework.lib.amqp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.openmuc.framework.lib.ssl.SslManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Recoverable;
import com.rabbitmq.client.RecoveryListener;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * Represents a connection to an AMQP broker
 */
public class AmqpConnection {

    private static final Logger logger = LoggerFactory.getLogger(AmqpConnection.class);
    private static final List<String> DECLARED_QUEUES = new ArrayList<>();

    private String exchange;
    private Connection connection;
    private Channel channel;

    /**
     * A connection to an AMQP broker
     *
     * @param settings
     *            connection details {@link AmqpSettings}
     * @throws IOException
     *             when connection fails
     * @throws TimeoutException
     *             when connection fails due time out
     */
    public AmqpConnection(AmqpSettings settings) throws IOException, TimeoutException {
        ConnectionFactory factory;
        factory = getConnectionFactoryForSsl(settings);
        factory.setHost(settings.getHost());
        factory.setPort(settings.getPort());
        factory.setVirtualHost(settings.getVirtualHost());
        factory.setUsername(settings.getUsername());
        factory.setPassword(settings.getPassword());

        connect(settings, factory);
    }

    private ConnectionFactory getConnectionFactoryForSsl(AmqpSettings settings) {
        ConnectionFactory factory = new ConnectionFactory();
        if (settings.isSsl()) {
            factory.useSslProtocol(SslManager.getInstance().getSslContext());
            factory.enableHostnameVerification();
        }
        return factory;
    }

    private void connect(AmqpSettings settings, ConnectionFactory factory) throws IOException, TimeoutException {

        connection = factory.newConnection();
        ((Recoverable) connection).addRecoveryListener(new RecoveryListener() {
            @Override
            public void handleRecovery(Recoverable recoverable) {
                logger.debug("Connection recovery completed");
            }

            @Override
            public void handleRecoveryStarted(Recoverable recoverable) {
                logger.debug("Connection recovery started");
            }
        });
        channel = connection.createChannel();
        exchange = settings.getExchange();
        channel.exchangeDeclare(exchange, "topic", true);
        if (logger.isTraceEnabled()) {
            logger.trace("Connected to {}:{} on virtualHost {} as user {}", settings.getHost(), settings.getPort(),
                    settings.getVirtualHost(), settings.getPort());
        }
    }

    /**
     * Close the channel and connection
     */
    public void disconnect() {
        try {
            channel.close();
            connection.close();
            if (logger.isTraceEnabled()) {
                logger.trace("Successfully disconnected");
            }
        } catch (IOException | TimeoutException | ShutdownSignalException e) {
            logger.error("failed to close connection: {}", e.getMessage());
        }
    }

    /**
     * Declares the passed queue as a durable queue
     *
     * @param queue
     *            the queue that should be declared
     * @throws IOException
     */
    public void declareQueue(String queue) throws IOException {
        if (!DECLARED_QUEUES.contains(queue)) {
            channel.queueDeclare(queue, true, false, false, null);
            channel.queueBind(queue, exchange, queue);
            DECLARED_QUEUES.add(queue);
            if (logger.isTraceEnabled()) {
                logger.trace("Queue {} declared", queue);
            }
        }
    }

    public String getExchange() {
        return exchange;
    }

    public Channel getRabbitMqChannel() {
        return channel;
    }
}
