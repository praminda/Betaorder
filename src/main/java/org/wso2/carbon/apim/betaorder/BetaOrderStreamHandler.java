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

package org.wso2.carbon.apim.betaorder;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apim.betaorder.model.BetaOrderCancelPublisherDTO;
import org.wso2.carbon.apim.betaorder.model.BetaOrderConstants;
import org.wso2.carbon.apim.betaorder.model.BetaOrderPublisherDTO;
import org.wso2.carbon.apim.betaorder.model.Item;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Handler for publishing beta order API usage data to analytics server
 * <p>This handler will be only invoked for POST requests made to
 * /order resource path.</p>
 */
public class BetaOrderStreamHandler extends AbstractHandler implements ManagedLifecycle {
    private static final Log log = LogFactory.getLog(BetaOrderStreamHandler.class);
    BetaOrderRequestDataPublisher betaOrderRequestPublisher;

    /**
     * Initializes the beta order data publisher
     *
     * @param synapseEnvironment
     */
    public void init(SynapseEnvironment synapseEnvironment) {
        this.betaOrderRequestPublisher = new BetaOrderRequestDataPublisher();
        this.betaOrderRequestPublisher.init();
    }

    /**
     * Validate the request for handler's matching criteria.
     * Extracts item list from the request and add it to messageContext
     * if the request is to add order
     *
     * @param messageContext
     * @return
     */
    public boolean handleRequest(MessageContext messageContext) {
        buildMessage(messageContext);
        String method = (String) messageContext.getProperty(BetaOrderConstants.HTTP_METHOD_KEY);
        String resourcePath = (String) messageContext.getProperty(BetaOrderConstants.REQUEST_PATH_KEY);

        // skip if the request is not a 'POST' or 'DELETE' and request to '/order' resource
        if (isValidForHandler(method, resourcePath)) {
            return true;
        }

        if (method.equals(BetaOrderConstants.HTTP_POST)) {
            List<Item> itemList = getItems(messageContext);
            messageContext.setProperty(BetaOrderConstants.ITEMS_PROPERTY, itemList);
        }

        return true;
    }

    /**
     * Publish events to analytics server if request is successful
     *
     * @param messageContext
     * @return
     */
    public boolean handleResponse(MessageContext messageContext) {
        buildMessage(messageContext);
        String method = (String) messageContext.getProperty(BetaOrderConstants.HTTP_METHOD_KEY);
        String resourcePath = (String) messageContext.getProperty(BetaOrderConstants.REQUEST_PATH_KEY);

        // skip if the request is not a 'POST' or 'DELETE' and request to '/order' resource
        if (isValidForHandler(method, resourcePath)) {
            return true;
        }

        SOAPFault fault = messageContext.getEnvelope().getBody().getFault();

        // 'fault' not null means there was an error in the backend and
        // no need to publish the events
        if (fault == null) {

            // publish add order event or cancel order event depending on the HTTP method
            if (method.equals(BetaOrderConstants.HTTP_POST)) {
                String username = ((AuthenticationContext) messageContext
                        .getProperty(BetaOrderConstants.API_AUTH_CONTEXT)).getSubscriber();
                String orderId = getOrderId(messageContext, BetaOrderConstants.ACTION_ADD);
                List<Item> itemList = (List<Item>) messageContext.getProperty(BetaOrderConstants.ITEMS_PROPERTY);
                publishOrderEvents(username, itemList, orderId);
            } else {
                String orderId = getOrderId(messageContext, BetaOrderConstants.ACTION_DELETE);
                BetaOrderCancelPublisherDTO betaOrderCancelPublisherDTO = new BetaOrderCancelPublisherDTO();
                betaOrderCancelPublisherDTO.setOrderId(orderId);
                betaOrderCancelPublisherDTO.setTimestamp(System.currentTimeMillis());

                try {
                    this.betaOrderRequestPublisher.publishEvent(betaOrderCancelPublisherDTO);
                } catch (APIManagementException e) {
                    log.error("Failed to publish the order cancel event", e);
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Build the message for reading
     *
     * @param messageContext
     */
    private void buildMessage(MessageContext messageContext) {
        try {
            RelayUtils.buildMessage(((Axis2MessageContext) messageContext).getAxis2MessageContext());
        } catch (IOException e) {
            log.error("Failed to build the message context", e);
        } catch (XMLStreamException e) {
            log.error("Failed to build the message context", e);
        }

    }

    /**
     * Publish separate event for each order in <code>itemList</code>
     *
     * @param username username of the user who ordered the items
     * @param itemList list of items ordered
     * @param orderId  id of the order
     */
    private void publishOrderEvents(String username, List<Item> itemList, String orderId) {
        for (Item item : itemList) {
            BetaOrderPublisherDTO betaOrderPublisherDTO = new BetaOrderPublisherDTO();
            betaOrderPublisherDTO.setCustomerId(username);
            betaOrderPublisherDTO.setItemId(item.getItemID());
            betaOrderPublisherDTO.setQuantity(item.getQuantity());
            betaOrderPublisherDTO.setOrderID(orderId);
            betaOrderPublisherDTO.setTimestamp(System.currentTimeMillis());

            try {
                this.betaOrderRequestPublisher.publishEvent(betaOrderPublisherDTO);
            } catch (APIManagementException e) {

                // log and continue without throwing to try publishing other events
                // even if this event failed to publish
                log.error("Failed to publish the event", e);
            }
        }

    }

    /**
     * Retrieves list of items ordered from the {@link MessageContext messageContext}
     * <p>A UUID is attached to each item for identifying the corresponding
     * order for the item</p>
     *
     * @param messageContext message context containing the soap envelop of order details
     * @return list of items in the current order request
     */
    private List<Item> getItems(MessageContext messageContext) {
        List<Item> itemList = new ArrayList<Item>();
        SOAPBody body = messageContext.getEnvelope().getBody();
        OMElement order = body.getFirstChildWithName(new QName("http://wso2.org", "submitOrder"));
        Iterator itemLines = order.getChildrenWithName(new QName("http://wso2.org", "itemLines"));

        while (itemLines.hasNext()) {
            OMElement itemLine = (OMElement) itemLines.next();
            Item item = new Item();
            item.setItemID(itemLine.getFirstChildWithName(new QName("http://models.wso2.org/xsd", "itemId")).getText());
            item.setQuantity(Long.parseLong(
                    itemLine.getFirstChildWithName(new QName("http://models.wso2.org/xsd", "quantity")).getText()));
            itemList.add(item);
        }

        return itemList;
    }

    /**
     * Retrieves order Id of a request
     *
     * @param messageContext message context containing the envelop with order id
     * @param action         type of request 'ADD' or 'CANCEL'
     * @return
     */
    private String getOrderId(MessageContext messageContext, String action) {
        String orderId;
        String elementName = "";

        if (action.equalsIgnoreCase(BetaOrderConstants.ACTION_ADD)) {
            elementName = "submitOrderResponse";
        } else if (action.equalsIgnoreCase(BetaOrderConstants.ACTION_DELETE)) {
            elementName = "cancelOrderResponse";
        }

        SOAPBody body = messageContext.getEnvelope().getBody();
        OMElement order = body.getFirstChildWithName(new QName("http://wso2.org", elementName));
        orderId = order.getFirstChildWithName(new QName("http://wso2.org", "return")).getText();

        return orderId;
    }

    /**
     * Check if request is valid for this handler to process
     *
     * @param method       http method
     * @param resourcePath resource path
     * @return
     */
    private boolean isValidForHandler(String method, String resourcePath) {
        return (!method.equals(BetaOrderConstants.HTTP_POST) || !resourcePath
                .startsWith(BetaOrderConstants.ALLOWED_RESOURCE_PATH)) && (
                !method.equals(BetaOrderConstants.HTTP_DELETE) || !resourcePath
                        .startsWith(BetaOrderConstants.ALLOWED_RESOURCE_PATH));
    }

    public void destroy() {
    }
}
