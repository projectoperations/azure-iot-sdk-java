/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions;

public class ProvisioningDeviceConnectionException extends ProvisioningDeviceTransportException
{
    public ProvisioningDeviceConnectionException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ProvisioningDeviceConnectionException(Throwable cause)
    {
        super(cause);
    }

    public ProvisioningDeviceConnectionException(String message)
    {
        super(message);
    }
}
