// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;


/** Sends a number of event messages to an IoT Hub. */
public class SendEventWithProxy
{
    // The maximum amount of time to wait for a message to be sent. Typically, this operation finishes in under a second.
    private static final int D2C_MESSAGE_TIMEOUT_MILLISECONDS = 10000;

    protected static class IotHubConnectionStatusChangeCallbackLogger implements IotHubConnectionStatusChangeCallback
    {
        @Override
        public void onStatusChanged(ConnectionStatusChangeContext connectionStatusChangeContext)
        {
            IotHubConnectionStatus status = connectionStatusChangeContext.getNewStatus();
            IotHubConnectionStatusChangeReason statusChangeReason = connectionStatusChangeContext.getNewStatusReason();
            Throwable throwable = connectionStatusChangeContext.getCause();

            System.out.println();
            System.out.println("CONNECTION STATUS UPDATE: " + status);
            System.out.println("CONNECTION STATUS REASON: " + statusChangeReason);
            System.out.println("CONNECTION STATUS THROWABLE: " + (throwable == null ? "null" : throwable.getMessage()));
            System.out.println();

            if (throwable != null)
            {
                throwable.printStackTrace();
            }

            if (status == IotHubConnectionStatus.DISCONNECTED)
            {
                System.out.println("The connection was lost, and is not being re-established." +
                        " Look at provided exception for how to resolve this issue." +
                        " Cannot send messages until this issue is resolved, and you manually re-open the device client");
            }
            else if (status == IotHubConnectionStatus.DISCONNECTED_RETRYING)
            {
                System.out.println("The connection was lost, but is being re-established." +
                        " Can still send messages, but they won't be sent until the connection is re-established");
            }
            else if (status == IotHubConnectionStatus.CONNECTED)
            {
                System.out.println("The connection was successfully established. Can send messages.");
            }
        }
    }

    /**
     * Sends a number of messages to an IoT or Edge Hub. Default protocol is to
     * use MQTT transport.
     *
     * @param args
     * args[0] = IoT Hub or Edge Hub connection string
     * args[1] = number of messages to send
     * args[2] = protocol (optional, one of 'https', 'mqtt_ws' or 'amqps_ws')
     * args[3] = proxy host name ie: "127.0.0.1", "localhost", etc.
     * args[4] = proxy port number ie "8888", "3128", etc
     * args[5] = (optional) proxy username
     * args[6] = (optional) proxy password
     */
    public static void main(String[] args)
        throws IOException, URISyntaxException, IotHubClientException, InterruptedException
    {
        System.out.println("Starting...");
        System.out.println("Beginning setup.");

        if (args.length != 5 && args.length != 7)
        {
            System.out.format(
                    "Expected 5 or 7 arguments but received: %d.\n"
                            + "The program should be called with the following args: \n"
                            + "1. [Device connection string] - String containing Hostname, Device Id & Device Key in one of the following formats: HostName=<iothub_host_name>;DeviceId=<device_id>;SharedAccessKey=<device_key> or HostName=<iothub_host_name>;DeviceId=<device_id>;SharedAccessKey=<device_key>;GatewayHostName=<gateway> \n"
                            + "2. [number of requests to send]\n"
                            + "3. (https | amqps_ws | mqtt_ws)\n"
                            + "4. proxy hostname (ie: '127.0.0.1', 'localhost', etc.)\n"
                            + "5. proxy port number\n"
                            + "6. (optional) username for the proxy \n"
                            + "7. (optional) password for the proxy \n",
                    args.length);
            return;
        }

        String connString = args[0];
        int numRequests;
        try
        {
            numRequests = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException e)
        {
            System.out.format(
                    "Could not parse the number of requests to send. "
                            + "Expected an int but received:\n%s.\n", args[1]);
            return;
        }
        IotHubClientProtocol protocol;
        String protocolStr = args[2];
        if (protocolStr.equalsIgnoreCase("https"))
        {
            protocol = IotHubClientProtocol.HTTPS;
        }
        else if (protocolStr.equalsIgnoreCase("amqps"))
        {
            throw new UnsupportedOperationException("AMQPS does not have proxy support");
        }
        else if (protocolStr.equalsIgnoreCase("mqtt"))
        {
            throw new UnsupportedOperationException("MQTT does not have proxy support");
        }
        else if (protocolStr.equalsIgnoreCase("amqps_ws"))
        {
            protocol = IotHubClientProtocol.AMQPS_WS;
        }
        else if (protocolStr.equalsIgnoreCase("mqtt_ws"))
        {
            protocol = IotHubClientProtocol.MQTT_WS;
        }
        else
        {
            System.out.format(
                    "Received a protocol string that could not be understood: %s.\n"
                            + "The program should be called with the following args: \n"
                            + "1. [Device connection string] - String containing Hostname, Device Id & Device Key in one of the following formats: HostName=<iothub_host_name>;DeviceId=<device_id>;SharedAccessKey=<device_key> or HostName=<iothub_host_name>;DeviceId=<device_id>;SharedAccessKey=<device_key>;GatewayHostName=<gateway> \n"
                            + "2. [number of requests to send]\n"
                            + "3. (https | amqps_ws | mqtt_ws)\n"
                            + "4. proxy hostname (ie: '127.0.0.1', 'localhost', etc.)\n"
                            + "5. proxy port number\n"
                            + "6. (optional) username for the proxy \n"
                            + "7. (optional) password for the proxy \n",
                    protocolStr);
            return;
        }

        String proxyHostname = args[3];
        int proxyPort;
        try
        {
            proxyPort = Integer.parseInt(args[4]);
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Expected argument 5 (port number) to be an integer");
        }

        String proxyUsername;
        char[] proxyPassword;
        if (args.length == 7)
        {
            proxyUsername = args[5];
            proxyPassword = args[6].toCharArray();
        }
        else
        {
            proxyUsername = null;
            proxyPassword = null;
        }

        System.out.println("Successfully read input parameters.");
        System.out.format("Using communication protocol %s.\n", protocol.name());

        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHostname, proxyPort));
        ProxySettings httpProxySettings = new ProxySettings(proxy, proxyUsername, proxyPassword);
        System.out.println("Using proxy address: " + proxyHostname + ":" + proxyPort);

        ClientOptions clientOptions = ClientOptions.builder().proxySettings(httpProxySettings).build();
        DeviceClient client = new DeviceClient(connString, protocol, clientOptions);

        System.out.println("Successfully created an IoT Hub client.");

        client.setConnectionStatusChangeCallback(new IotHubConnectionStatusChangeCallbackLogger(), new Object());

        client.open(true);

        System.out.println("Opened connection to IoT Hub.");
        System.out.println("Sending the following event messages:");

        try
        {
            client.sendEvent(new Message("This is a message sent over proxy"), D2C_MESSAGE_TIMEOUT_MILLISECONDS);
            System.out.println("Successfully sent the message");
        }
        catch (IotHubClientException e)
        {
            System.out.println("Failed to send the message. Status code: " + e.getStatusCode());
        }

        // close the connection
        System.out.println("Closing the client...");
        client.close();
    }
}
