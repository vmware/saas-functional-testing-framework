# Test Functional

This library provides classes used in functional tests.

## Use of LocalAppProcessConfigTemplate

This class can and should be used by each individual `LocalAppProcessConfig` defined by an app or service. The class
has collected some common methods used in <-app>-services. You will need to implement your app/service-specific
environment (provided as a `Map`) and pass it to `defaultTestProcessBuilder`. Another method is provided that can control
any pre-startup behaviors - create `appPreStartCallback()` which executes before the app/service container
is started. That method can be used for such items as data source initialization, or creating service stubs/mocks.

Examine the source and comments within `com.aw.dpa.test.process.LocalAppProcessConfigTemplate` for additional details.

