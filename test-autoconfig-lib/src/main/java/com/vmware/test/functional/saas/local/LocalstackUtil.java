package com.vmware.test.functional.saas.local;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.testcontainers.containers.localstack.LocalStackContainer;

import com.vmware.test.functional.saas.Service;

import static com.vmware.test.functional.saas.ServiceConditionUtil.getRequiredServiceDependencies;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.DYNAMODB;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.KINESIS;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.REDSHIFT;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SES;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SNS;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

/**
 *  Utility class to retrieve AWS services requested to be provisioned by LocalStack.
 */
final class LocalstackUtil {
   private static final String SERVICES_PROVIDED_BY_LOCALSTACK = "services.provided.by.localstack";
   private static final Map<Service, LocalStackContainer.Service> LOCALSTACK_SERVICE_MAPPING = Map.of(
         Service.DYNAMO_DB, DYNAMODB,
         Service.KINESIS, KINESIS,
         Service.REDSHIFT, REDSHIFT,
         Service.S3, S3,
         Service.SES, SES,
         Service.SNS, SNS,
         Service.SQS, SQS);

   /**
    * Utility method to retrieve required Localstack services from the context.
    *
    * @param context {@link ConditionContext}
    * @return List of services that should be started in localstack.
    */
   static List<String> getLocalstackServices(@NotNull final ConditionContext context) {
      final Environment environment = context.getEnvironment();
      return getLocalstackServices(environment);
   }

   /**
    * Utility method to retrieve required Localstack services from the context.
    *
    * @param environment {@link Environment}
    * @return List of services that should be started in localstack.
    */
   static List<String> getLocalstackServices(final Environment environment) {
      List<String> localstackServices = new ArrayList<>();
      final String localstackRequestedServices = environment.getProperty(SERVICES_PROVIDED_BY_LOCALSTACK);
      if (StringUtils.isNotBlank(localstackRequestedServices)) {
         localstackServices = Arrays.asList(localstackRequestedServices.split(","));
      }
      return localstackServices;
   }

   static LocalStackContainer.Service mapLocalStackService(final Service service) {
      return LOCALSTACK_SERVICE_MAPPING.get(service);
   }

   /**
    * Utility method to retrieve required service dependencies and their info from the context.
    *
    * @param listableBeanFactory {@link ConfigurableListableBeanFactory}
    * @return Set of required {@link Service}
    */
   static Set<LocalService.BeanInfo> lookupRequiredServiceDependenciesInfo(final ConfigurableListableBeanFactory listableBeanFactory) {
      return getRequiredServiceDependencies(listableBeanFactory).stream()
            .map(LocalstackUtil::localService)
            .collect(Collectors.toSet());
   }

   private static LocalService.BeanInfo localService(Service service) {
      return LocalService.BeanInfo.builder()
            .beanRef(new RuntimeBeanReference(service.name()))
            .service(service)
            .localstackService(isLocalstackService(service))
            .endpointName(service.getEndpointName())
            .build();
   }

   private static boolean isLocalstackService(Service service) {
      return LOCALSTACK_SERVICE_MAPPING.containsKey(service);
   }

}
