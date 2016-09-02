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
 * Constants for Beta order data publisher implementation
 */
public class BetaOrderConstants {
    public static final String ALLOWED_RESOURCE_PATH = "/order";
    public static final String HTTP_POST = "POST";
    public static final String HTTP_DELETE = "DELETE";
    public static final String ORDER_STREAM_NAME = "org.wso2.apimgt.statistics.beta.order";
    public static final String ORDER_STREAM_VERSION = "1.0.0";
    public static final String CANCEL_STREAM_NAME = "org.wso2.apimgt.statistics.beta.order.cancel";
    public static final String CANCEL_STREAM_VERSION = "1.0.0";
    public static final String HTTP_METHOD_KEY = "api.ut.HTTP_METHOD";
    public static final String REQUEST_PATH_KEY = "REST_SUB_REQUEST_PATH";
    public static final String API_AUTH_CONTEXT = "__API_AUTH_CONTEXT";
    public static final String ITEMS_PROPERTY = "BETA_ORDER_ITEMS";
    public static final String ACTION_ADD = "ADD";
    public static final String ACTION_DELETE = "DELETE";
}
