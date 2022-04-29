AWSTemplateFormatVersion: '2010-09-09'
Transform: AWS::Serverless-2016-10-31
Description: >
    Sample SAM Template for running Lambda functions using AWS SAM

Resources:
<#list functions as function>
    ${function.name}:
        Type: AWS::Serverless::Function
        Properties:
            Timeout: ${function.timeout}
            CodeUri: ${function.codeUri}
            Handler: ${function.handlerClass}::handleRequest
            Runtime: ${function.runtime}
            MemorySize: ${function.memorySize}
<#if function.environment??>
            Environment:
                Variables:
        <#list function.environment as var, value>
                    ${(var)!}: ${(value)!}
        </#list>
</#if>
</#list>
