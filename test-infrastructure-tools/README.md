# Test Functional Infrastructure Tools

When required for faster test implementation or debugging,  an option to run tests locally is provided where app process is not started before each test but can be started only once and used by all other tests while developing/running.

Module provides some sort of a local dev env or some sort of a local cli.

This is achieved by providing:

- config option ``` default.ports.enabled ``` to bind services to default ports defined in the com.aw.dpa.test.aws.local.service.Service
- config option ``` local.run.skip```  to allow functional tests to be executed locally without starting services, process or wiremock server``

-Dlocal.run.skip=true
-Ddefault.ports.enabled=true
-D{app}Endpoint.port.override=10250

# How to enable local execution and provisioning
Start only services
```mvn surefire:test@services-only -D{app}Endpoint.port.override=10250 -P local-test-env -pl :<app>-automation```

Start apps only and no services
```mvn surefire:test@apps-only-no-services -D{app}Endpoint.port.override=10250 -P local-test-env -pl :<app>-automation```

Start apps only
```mvn surefire:test@apps-only -D{app}Endpoint.port.override=10250 -P local-test-env -pl :<app>-automation```

Execute the tests without starting the <app>-server
```mvn surefire:test@tests-only -D{app}Endpoint.port.override=10250 -P local-test-env -pl :<app>-automation```
