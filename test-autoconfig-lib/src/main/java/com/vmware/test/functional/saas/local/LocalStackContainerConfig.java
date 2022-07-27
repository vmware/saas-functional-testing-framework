package com.vmware.test.functional.saas.local;

import java.util.List;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.startupcheck.IsRunningStartupCheckStrategy;

import com.vmware.test.functional.saas.ServiceEndpoint;

import static com.vmware.test.functional.saas.local.CustomDockerContainer.DEFAULT_DOCKER_CONTAINER_STARTUP_TIMEOUT;

@Configuration
public class LocalStackContainerConfig {

   @Value("${aws.testDefaultRegion}")
   private String testDefaultRegion;
   public static final String LOCALSTACK_REGION = "DEFAULT_REGION";

   @Bean
   @Conditional(ContainerCondition.LocalStackContainerCondition.class)
   LocalStackFactory localStackContainer(@Autowired(required = false)
   final List<LocalStackContainer.Service> localstackServices, final ConfigurableListableBeanFactory listableBeanFactory,
         final ServiceEndpoint localStackEndpoint) {
      return new LocalStackFactory(
            listableBeanFactory,
            localStackEndpoint,
            localstackServices,
            modifyLocalStackContainer(localStackEndpoint));
   }

   private Consumer<LocalStackContainer> modifyLocalStackContainer(final ServiceEndpoint localStackEndpoint) {
      return container -> {
         container.setNetwork(localStackEndpoint.getContainerConfig().getNetworkInfo().getNetwork());
         container.setEnv(List.of(LOCALSTACK_REGION + "=" + this.testDefaultRegion));
         container.withCreateContainerCmdModifier(cmd -> cmd.withName(localStackEndpoint.getContainerConfig().getName()));
         container.withStartupCheckStrategy(new IsRunningStartupCheckStrategy().withTimeout(DEFAULT_DOCKER_CONTAINER_STARTUP_TIMEOUT));
      };
   }
}
