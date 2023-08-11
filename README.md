# saas-functional-testing-framework

## Overview

One of the major challenges of the SaaS delivery train is the difficulty the engineering teams face when trying to replicate the production environments in their CI/CD pipelines.

Saas Functional Testing Framework provides the engineers with the possibility to write tests and run them seamlessly with different execution contexts (local dev environment as well as CI or Production)

As a consequence of the above, Saas Functional Testing Framework allow us to use the same programming model to get the feedback build-time (fail-fast) as well as on each stage of the CI/CD pipeline.

Saas Functional Testing Framework provides the option to plug any 3rd party service like Athena, Presto and many others.


## Try it out

### Prerequisites

* Latest java
* Latest maven
* Docker - the minimum is [Docker CLI]( https://github.com/docker/cli), but docker desktop will also work
* [Sam CLI](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-cli-install.html)

### Build & Run

Build the project
```shell
mvn clean install
```

## Documentation

Understand about the various features and how to use them in your project check out [The Getting Started Guide]
(https://github.com/vmware/saas-functional-testing-framework/wiki)  

## Contributing

The saas-functional-testing-framework project team welcomes contributions from the community. Before you start working with saas-functional-testing-framework, please
read our [Developer Certificate of Origin](https://cla.vmware.com/dco). All contributions to this repository must be
signed as described on that page. Your signature certifies that you wrote the patch or have the right to pass it on
as an open-source patch. For more detailed information, refer to [CONTRIBUTING.md](CONTRIBUTING.md).

## License

