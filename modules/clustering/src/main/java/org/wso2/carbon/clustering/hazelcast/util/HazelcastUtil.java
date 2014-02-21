/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.clustering.hazelcast.util;

import com.hazelcast.core.Member;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.clustering.ClusterConfiguration;
import org.wso2.carbon.clustering.ClusterMessage;
import org.wso2.carbon.clustering.ClusterMember;
import org.wso2.carbon.clustering.CarbonCluster;
import org.wso2.carbon.clustering.exception.MessageFailedException;
import org.wso2.carbon.clustering.internal.DataHolder;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Utility class for hazezcast based clustering implementation
 */
public class HazelcastUtil {
    public static ClusterMember toClusterMember(Member hazelcastMember) {
        InetSocketAddress inetSocketAddress = hazelcastMember.getInetSocketAddress();
        ClusterMember clusterMember = new ClusterMember(inetSocketAddress.getHostName(),
                                                        inetSocketAddress.getPort());
        clusterMember.setId(hazelcastMember.getUuid());
        return clusterMember;
    }

    /**
     * Replay messages to a newly joining member
     */
    public static void sendMessagesToMember(List<ClusterMessage> messageBuffer,
                                            Member member) throws MessageFailedException {
        CarbonCluster carbonCluster = DataHolder.getInstance().getCarbonCluster();
        for (ClusterMessage clusterMessage : messageBuffer) {
            ArrayList<ClusterMember> members = new ArrayList<>();
            members.add(HazelcastUtil.toClusterMember(member));
            carbonCluster.sendMessage(clusterMessage, members);
        }
    }

    public static void loadPropertiesFromConfig(ClusterConfiguration clusterConfiguration,
                                                Properties hazelcastProperties) {

        List<Object> propsParam = clusterConfiguration.getElement("properties");
        if (propsParam != null) {
            for (Object property : propsParam) {
                if (property instanceof Node) {
                    NodeList nodeList = ((Node) property).getChildNodes();
                    for (int count = 0; count < nodeList.getLength(); count++) {
                        Node node = nodeList.item(count);
                        if (node.getNodeType() == Node.ELEMENT_NODE &&
                            "property".equals(node.getNodeName())) {
                            String attributeName = ((Element) node).getAttribute("name");
                            String attributeValue = node.getTextContent();
                            hazelcastProperties.setProperty(attributeName, attributeValue);
                        }
                    }
                }
            }
        }
    }

    public static String lookupHazelcastProperty(ClusterConfiguration clusterConfiguration,
                                                 String propertyName) {
        String propertyValue = null;
        List<Object> propsParam = clusterConfiguration.getElement("LocalMember.Properties");
        if (propsParam != null) {
            for (Object property : propsParam) {
                if (property instanceof Node) {
                    NodeList nodeList = ((Node) property).getChildNodes();
                    for (int count = 0; count < nodeList.getLength(); count++) {
                        Node node = nodeList.item(count);
                        if (node.getNodeType() == Node.ELEMENT_NODE &&
                            "Property".equals(node.getNodeName())) {
                            String attributeName = ((Element) node).getAttribute("name");
                            if (attributeName != null && propertyName.equals(attributeName)) {
                                propertyValue = node.getTextContent();
                                break;
                            }
                        }
                    }
                }
            }
        }

        return propertyValue;
    }
}
