/*
 * Copyright 2022 VMware, Inc.
 * All rights reserved.
 */

package com.vmware.test.functional.saas.local;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.FactoryBean;
import org.testcontainers.containers.localstack.LocalStackContainer;

import com.vmware.test.functional.saas.Service;

import lombok.RequiredArgsConstructor;

import static com.vmware.test.functional.saas.local.ServiceConditionUtil.mapLocalStackService;


@RequiredArgsConstructor
public class LocalStackContainerServiceFactoryBean implements FactoryBean<LocalStackContainer.Service> {

   @NotNull
   protected final Service service;

   @Override
   public LocalStackContainer.Service getObject() throws Exception {
      return mapLocalStackService(service);
   }

   @Override
   public Class<?> getObjectType() {
      return LocalStackContainer.Service.class;
   }
}
