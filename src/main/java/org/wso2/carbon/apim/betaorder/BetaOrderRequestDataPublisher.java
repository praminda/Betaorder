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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apim.betaorder.model.*;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageDataBridgeDataPublisher;
import org.wso2.carbon.apimgt.usage.publisher.DataPublisherUtil;
import org.wso2.carbon.apimgt.usage.publisher.internal.DataPublisherAlreadyExistsException;
import org.wso2.carbon.apimgt.usage.publisher.internal.UsageComponent;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.exception.TransportException;

/**
 * Data publisher for publishing BetaOrder events
 * <p>This class extends {@link APIMgtUsageDataBridgeDataPublisher}
 * and override the  {@link #init()} method to initialize the {@link DataPublisher}
 * required for publishing the events. This is because parent class doesn't provide
 * external access to an initialized Object of itself</p>
 */
public class BetaOrderRequestDataPublisher extends APIMgtUsageDataBridgeDataPublisher {
    private static final Log log = LogFactory.getLog(BetaOrderRequestDataPublisher.class);
    DataPublisher dataPublisher;

    @Override
    public void init() {
        super.init();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing BetaOrderRequestDataPublisher");
            }

            this.dataPublisher = getDataPublisher();

        } catch (Exception e) {
            log.error("Error initializing BetaOrderRequestDataPublisher", e);
        }
    }

    /**
     * Publisher order detail event to analytics server
     *
     * @param betaOrderPublisherDTO dto with event details and payload
     * @throws APIManagementException
     */
    public void publishEvent(BetaOrderPublisherDTO betaOrderPublisherDTO) throws APIManagementException {
        DataBridgeBetaOrderPublisherDTO dataBridgeRequestPublisherDTO = new DataBridgeBetaOrderPublisherDTO(
                betaOrderPublisherDTO);
        try {

            String streamID = BetaOrderConstants.ORDER_STREAM_NAME + ":" + BetaOrderConstants.ORDER_STREAM_VERSION;

            //Publish Request Data
            Object[] metaData = new Object[] { "external" };
            Object[] payload = (Object[]) dataBridgeRequestPublisherDTO.createPayload();
            dataPublisher.tryPublish(streamID, System.currentTimeMillis(), metaData, null, payload);
        } catch (Exception e) {
            log.error("Error while publishing Request event", e);
        }
    }

    /**
     * Publisher order cancel event to analytics server
     *
     * @param betaOrderCancelPublisherDTO dto with canceled order event details and payload
     * @throws APIManagementException
     */
    public void publishEvent(BetaOrderCancelPublisherDTO betaOrderCancelPublisherDTO) throws APIManagementException {
        DataBridgeBetaOrderCancelPublisherDTO cancelOrderDTO = new DataBridgeBetaOrderCancelPublisherDTO(
                betaOrderCancelPublisherDTO);
        try {

            String streamID = BetaOrderConstants.CANCEL_STREAM_NAME + ":" + BetaOrderConstants.CANCEL_STREAM_VERSION;

            //Publish Request Data
            Object[] metaData = new Object[] { "external" };
            Object[] payload = (Object[]) cancelOrderDTO.createPayload();
            dataPublisher.tryPublish(streamID, System.currentTimeMillis(), metaData, null, payload);
        } catch (Exception e) {
            log.error("Error while publishing Request event", e);
        }
    }

    /**
     * Initialize a {@link DataPublisher} from the apim Analytics configurations
     *
     * @return initialized DataPublisher instance
     */
    private static DataPublisher getDataPublisher() {
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        //Get DataPublisher which has been registered for the tenant.
        DataPublisher dataPublisher = UsageComponent.getDataPublisher(tenantDomain);

        //If a DataPublisher had not been registered for the tenant.
        if (dataPublisher == null
                && DataPublisherUtil.getApiManagerAnalyticsConfiguration().getDasReceiverUrlGroups() != null) {

            String serverUser = DataPublisherUtil.getApiManagerAnalyticsConfiguration().getDasReceiverServerUser();
            char[] serverPassword = DataPublisherUtil.getApiManagerAnalyticsConfiguration()
                    .getDasReceiverServerPassword().toCharArray();
            String serverURL = DataPublisherUtil.getApiManagerAnalyticsConfiguration().getDasReceiverUrlGroups();
            String serverAuthURL = DataPublisherUtil.getApiManagerAnalyticsConfiguration()
                    .getDasReceiverAuthUrlGroups();

            try {
                //Create new DataPublisher for the tenant.
                dataPublisher = new DataPublisher(null, serverURL, serverAuthURL, serverUser,
                        new String(serverPassword));

                //Add created DataPublisher.
                UsageComponent.addDataPublisher(tenantDomain, dataPublisher);
            } catch (DataPublisherAlreadyExistsException e) {
                log.warn("Attempting to register a data publisher for the tenant " + tenantDomain +
                        " when one already exists. Returning existing data publisher");
                return UsageComponent.getDataPublisher(tenantDomain);
            } catch (DataEndpointConfigurationException e) {
                log.error("Error while creating data publisher", e);
            } catch (DataEndpointException e) {
                log.error("Error while creating data publisher", e);
            } catch (DataEndpointAgentConfigurationException e) {
                log.error("Error while creating data publisher", e);
            } catch (TransportException e) {
                log.error("Error while creating data publisher", e);
            } catch (DataEndpointAuthenticationException e) {
                log.error("Error while creating data publisher", e);
            }
        }

        return dataPublisher;
    }

}
