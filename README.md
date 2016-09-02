# Betaorder

Sample custom data publisher written for WSO2 API Manager 2.0
A handler is implemented to capture API invocations of /order path. The BetaOrder publisher publishes add and delete order
events to org.wso2.apimgt.statistics.beta.order:1.0.0 and org.wso2.apimgt.statistics.beta.order.cancel:1.0.0 streams in 
WSO2 API Manager Analytics Server.

## Configurations
1. Copy betaorder-data-publisher-1.0.jar to repository/components/lib
* Add handler `<handler class="org.wso2.carbon.apim.betaorder.BetaOrderStreamHandler"/>` to the API
* Change publisher class in repository/conf/api-manager.xml to `org.wso2.carbon.apim.betaorder.BetaOrderRequestDataPublisher`.
* Create following two streams in Analytics server.
    1. org.wso2.apimgt.statistics.beta.order:1.0.0
        1. meta_clientType:string
        * customerId:string
        * itemId:string
        * quantity:long
        * orderId:string
        * timestamp:long
    * org.wso2.apimgt.statistics.beta.order.cancel:1.0.0
        1. meta_clientType:string,
        * orderId:string,
        * timestamp:long
* Create Recievers for above streams
