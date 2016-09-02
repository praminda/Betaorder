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
 * Beta Order data bridge publisher data transfer object
 */
public class DataBridgeBetaOrderPublisherDTO extends BetaOrderPublisherDTO {

    public DataBridgeBetaOrderPublisherDTO(BetaOrderPublisherDTO betaOrderPublisherDTO) {
        setCustomerId(betaOrderPublisherDTO.getCustomerId());
        setItemId(betaOrderPublisherDTO.getItemId());
        setQuantity(betaOrderPublisherDTO.getQuantity());
        setOrderID(betaOrderPublisherDTO.getOrderID());
        setTimestamp(betaOrderPublisherDTO.getTimestamp());
    }

    /**
     *
     * @return json representation of the stream definition
     */
    public static String getStreamDefinition() {
        return "{"
                + " 'name': " + BetaOrderConstants.ORDER_STREAM_NAME + ","
                + " 'version': " + BetaOrderConstants.ORDER_STREAM_VERSION + ","
                + " 'nickName': Beta Order Request,"
                + " 'description': Beta order request stream,"
                + " 'metaData': ["
                + "     {'name': 'clientType','type':'STRING'}"
                + " ],"
                + " 'payloadData': ["
                + "     {'name': 'customerId','type':'STRING'},"
                + "     {'name': 'itemId', 'type': 'STRING'},"
                + "     {'name': 'quantity', 'type': 'LONG'},"
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
        return new Object[] { getCustomerId(), getItemId(), getQuantity(), getOrderID(), getTimestamp() };
    }
}
