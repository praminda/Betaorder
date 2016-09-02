/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apim.betaorder.model;

/**
 * Beta Order data bridge publisher data transfer object for cancel order
 */
public class DataBridgeBetaOrderCancelPublisherDTO extends BetaOrderCancelPublisherDTO {
    public DataBridgeBetaOrderCancelPublisherDTO(BetaOrderCancelPublisherDTO betaOrderCancelPublisherDTO) {
        setOrderId(betaOrderCancelPublisherDTO.getOrderId());
        setTimestamp(betaOrderCancelPublisherDTO.getTimestamp());
    }

    /**
     * @return json representation of the stream definition
     */
    public static String getStreamDefinition() {
        return "{"
                + " 'name': " + BetaOrderConstants.CANCEL_STREAM_NAME + ","
                + " 'version': " + BetaOrderConstants.CANCEL_STREAM_VERSION + ","
                + " 'nickName': Beta Order Cancel Request,"
                + " 'description': Beta order cancel request stream,"
                + " 'metaData': ["
                + "     {'name': 'clientType','type':'STRING'}"
                + " ],"
                + " 'payloadData': ["
                + "     {'name': 'orderId', 'type': 'STRING'},"
                + "     {'name': 'timestamp', 'type': 'LONG'}"
                + " ]"
                + "}";
    }

    /**
     * Create the payload matching the stream definition
     *
     * @return payload as an {@link Object} array
     */
    public Object createPayload() {
        return new Object[] { getOrderId(), getTimestamp() };
    }
}
