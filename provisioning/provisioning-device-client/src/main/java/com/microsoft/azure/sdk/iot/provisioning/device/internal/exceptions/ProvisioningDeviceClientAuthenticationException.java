/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions;

public class ProvisioningDeviceClientAuthenticationException extends ProvisioningDeviceClientException
{
    public ProvisioningDeviceClientAuthenticationException(String message)
    {
        super(message);
    }

    public ProvisioningDeviceClientAuthenticationException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ProvisioningDeviceClientAuthenticationException(Throwable cause)
    {
        super(cause);
    }
}
