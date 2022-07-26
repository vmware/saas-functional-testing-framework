/*
 * Copyright 2022 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.local;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.FactoryBean;
import org.testcontainers.containers.localstack.LocalStackContainer;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LocalStackContainerServiceFactoryBean implements FactoryBean<LocalStackContainer.Service> {

   @NotNull
   protected final LocalService localService;

   @Override
   public LocalStackContainer.Service getObject() throws Exception {
      return localService.getService();
   }

   @Override
   public Class<?> getObjectType() {
      return LocalStackContainer.Service.class;
   }
}
