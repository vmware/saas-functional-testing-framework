/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.testcontainers.containers.Network;

import lombok.Setter;

import com.vmware.test.functional.saas.ServiceEndpoint;

/**
 * Manages the container networks.
 */
@Component
public class ContainerNetworkManager implements InitializingBean {

   private static final Map<String, Network> networkMap = new ConcurrentHashMap<>();


   @Setter(onMethod_ = @Autowired(required = false))
   private List<ServiceEndpoint> serviceEndpoints;

   public Network getNetwork(String networkName) {
      return networkMap.computeIfAbsent(networkName,this::buildNetwork);
   }

   private Network buildNetwork(String name) {
      final Network.NetworkImpl network = Network.builder()
            .createNetworkCmdModifier(cmd -> cmd.withName(name))
            .build();
      // Calling getId() makes testContainers to create the network.
      network.getId();
      return network;
   }

   private void forceNetworkInit(ServiceEndpoint serviceEndpoint) {
      getNetwork(serviceEndpoint.getContainerConfig().getNetworkInfo().getName());
   }

   @Override
   public void afterPropertiesSet() {
      if (serviceEndpoints != null) {
         // Force docker networks initialization
         serviceEndpoints.forEach(this::forceNetworkInit);
      }
   }
}
