/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.FactoryBean;
import org.testcontainers.containers.localstack.LocalStackContainer;

import com.vmware.test.functional.saas.Service;

import lombok.RequiredArgsConstructor;

import static com.vmware.test.functional.saas.local.LocalstackUtil.mapLocalStackService;


@RequiredArgsConstructor
public class LocalstackContainerServiceFactoryBean implements FactoryBean<LocalStackContainer.Service> {

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
