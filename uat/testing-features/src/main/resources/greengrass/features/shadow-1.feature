@ShadowManager @Shadow1
Feature: Greengrass V2 ShadowManager

    As a developer, I can access the Greengrass local shadow service from my component.

    Background:
        Given my device is registered as a Thing
        And my device is running Greengrass
        And I start an assertion server
        Given I create a Greengrass deployment with components
            | aws.greengrass.Cli        | LATEST |
            | aws.greengrass.ShadowManager | LATEST |
        And I deploy the Greengrass deployment configuration
        Then the Greengrass deployment is COMPLETED on the device after 2 minutes
        Then I verify the aws.greengrass.ShadowManager component is RUNNING using the greengrass-cli

    Scenario: Shadow-1-T1: As a developer, I can use the Greengrass local shadow service to UPDATE and GET a shadow from my component.
        When I install the component ShadowComponentPing from local store with configuration
            | key                    | value             |
            | assertionServerPort    | ${assertionServerPort} |
            | Operation              | UpdateThingShadow |
            | ThingName              | testThing         |
            | ShadowName             | testShadow        |
            | ShadowDocument         | {\"state\":{\"reported\":{\"color\":{\"r\":255,\"g\":255,\"b\":255},\"SomeKey\":\"SomeValue\"}}}         |
        Then the local Greengrass deployment is SUCCEEDED on the device after 120 seconds
        When I install the component ShadowComponentPong from local store with configuration
            | key                    | value             |
            | assertionServerPort    | ${assertionServerPort} |
            | Operation              | GetThingShadow |
            | ThingName              | testThing         |
            | ShadowName             | testShadow        |
            | ShadowDocument         | {\"state\":{\"reported\":{\"color\":{\"r\":255,\"g\":255,\"b\":255},\"SomeKey\":\"SomeValue\"}}}         |
        Then the local Greengrass deployment is SUCCEEDED on the device after 120 seconds
        Then I get 1 assertions with context "Retrieved matching shadow document"

    Scenario: Shadow-1-T2: As a developer, I can use the Greengrass local shadow service to UPDATE a shadow from my component.
        When I install the component ShadowComponentPing from local store with configuration
            | key                    | value              |
            | assertionServerPort    | ${assertionServerPort} |
            | Operation              | UpdateThingShadow  |
            | ThingName              | testThing          |
            | ShadowName             | testShadow         |
            | ShadowDocument         | {\"state\":{\"reported\":{\"color\":{\"r\":255,\"g\":255,\"b\":255},\"SomeKey\":\"SomeValue\"}}}         |
        Then the local Greengrass deployment is SUCCEEDED on the device after 120 seconds
        When I install the component ShadowComponentPong from local store with configuration
            | key                    | value             |
            | assertionServerPort    | ${assertionServerPort} |
            | Operation              | UpdateThingShadow |
            | ThingName              | testThing         |
            | ShadowName             | testShadow        |
            | ShadowDocument         | {\"state\":{\"reported\":{\"color\":{\"r\":255,\"g\":255,\"b\":255},\"SomeKey\":\"SomeValue\"}}}         |
        Then the local Greengrass deployment is SUCCEEDED on the device after 120 seconds
        And I update the component ShadowComponentPing with configuration
            | key            | value                    |
            | assertionServerPort    | ${assertionServerPort} |
            | Operation      | GetThingShadow           |
            | ThingName      | testThing                |
            | ShadowName     | testShadow               |
            | ShadowDocument | {\"version\":2,\"state\":{\"reported\":{\"color\":{\"r\":255,\"g\":0,\"b\":0},\"SomeKey\":\"SomeOtherValue\"}}}  |
        Then the Greengrass deployment is COMPLETED on the device after 2 minutes
        Then I get 1 assertions with context "Retrieved matching shadow document"

    Scenario: Shadow-1-T3: As a developer, I can use the Greengrass local shadow service to DELETE a shadow from my component.
        When I install the component ShadowComponentPing from local store with configuration
            | key                    | value                                                                                                     |
            | assertionServerPort    | ${assertionServerPort}                                                                                    |
            | Operation              | UpdateThingShadow                                                                                         |
            | ThingName              | testThing                                                                                                 |
            | ShadowName             | testShadow                                                                                                |
            | ShadowDocument         | {\"state\":{\"reported\":{\"color\":{\"r\":255,\"g\":255,\"b\":255},\"SomeKey\":\"SomeValue\"}}}          |
        Then the local Greengrass deployment is SUCCEEDED on the device after 120 seconds
        When I install the component ShadowComponentPong from local store with configuration
            | key            | value             |
            | assertionServerPort    | ${assertionServerPort} |
            | Operation      | DeleteThingShadow |
            | ThingName      | testThing         |
            | ShadowName     | testShadow        |
        Then the local Greengrass deployment is SUCCEEDED on the device after 120 seconds
        And I update the component ShadowComponentPing with configuration
            | key            | value          |
            | assertionServerPort    | ${assertionServerPort} |
            | Operation      | GetThingShadow |
            | ThingName      | testThing      |
            | ShadowName     | testShadow     |
        Then the Greengrass deployment is COMPLETED on the device after 2 minutes

    Scenario: Shadow-1-T4: As a developer, I can use the Greengrass local shadow service to LIST Named Shadows from my component.
        When I install the component ShadowComponentPong from local store with configuration
            | key                    | value                                                                                                          |
            | assertionServerPort    | ${assertionServerPort}                                                                                         |
            | Operation              | SetupListNamedShadowTest                                                                                       |
            | ThingName              | testThing                                                                                                      |
            | ShadowDocument         | {\"state\":{\"reported\":{\"color\":{\"r\":255,\"g\":255,\"b\":255},\"SomeKey\":\"SomeValue\"}}}               |
        Then the local Greengrass deployment is SUCCEEDED on the device after 120 seconds
        When I install the component ShadowComponentPing from local store with configuration
            | key       | value                    |
            | assertionServerPort    | ${assertionServerPort} |
            | Operation | ListNamedShadowsForThing |
            | ThingName | testThing                |
        Then the local Greengrass deployment is SUCCEEDED on the device after 120 seconds

    Scenario: Shadow-1-T5: As a developer, I can use the Greengrass local shadow service to LIST Named Shadows from my component with pageSize and nextToken.
        When I install the component ShadowComponentPong from local store with configuration
            | key                    | value                                                                                                          |
            | assertionServerPort    | ${assertionServerPort}                                                                                         |
            | Operation              | SetupListNamedShadowTest                                                                                       |
            | ThingName              | testThing                                                                                                      |
            | ShadowDocument         | {\"state\":{\"reported\":{\"color\":{\"r\":255,\"g\":255,\"b\":255},\"SomeKey\":\"SomeValue\"}}}               |
        Then the local Greengrass deployment is SUCCEEDED on the device after 120 seconds
        When I install the component ShadowComponentPing from local store with configuration
            | key       | value                                 |
            | assertionServerPort    | ${assertionServerPort}   |
            | Operation | ListNamedShadowsForThing |
            | ThingName | testThing                |
            | PageSize  | 2                        |
        Then the local Greengrass deployment is SUCCEEDED on the device after 120 seconds
        When I update the component ShadowComponentPing with configuration
            | key       | value                    |
            | assertionServerPort    | ${assertionServerPort}   |
            | Operation | ListNamedShadowsForThing |
            | ThingName | testThing                |
            | nextToken | 1uTRLnjIlNrqirv+CtW3bg== |
        Then the local Greengrass deployment is SUCCEEDED on the device after 120 seconds

    Scenario: Shadow-1-T6: As a customer, I can react to named shadow updates based on the delta payload received over PubSub
        When I install the component ShadowReactiveComponentPing from local store with configuration
            | key                     | value                                                                                               |
            | SubscribeTopic          | $aws/things/testThingName/shadow/name/testShadowName/update/delta                                   |
            | UpdateDocumentRequest1  | {\"state\":{\"reported\":{\"color\":{\"r\":255,\"g\":255,\"b\":255},\"SomeKey\":\"SomeValue\"}}}    |
        Then the local Greengrass deployment is SUCCEEDED on the device after 120 seconds
        When I install the component ShadowReactiveComponentPong from local store with configuration
            | key                     | value                                                                                                     |
            | UpdateDocumentRequest1  | {\"version\":1,\"state\":{\"desired\":{\"color\":{\"r\":255,\"g\":0,\"b\":0},\"SomeKey\":\"SomeValue\"}}} |
        Then the local Greengrass deployment is SUCCEEDED on the device after 120 seconds

    Scenario: Shadow-1-T7: As a customer, I can react to classic shadow updates based on the delta payload received over PubSub
        When I install the component ShadowReactiveComponentPing from local store with configuration
            | key                     | value                                                                                               |
            | ThingName               | testThing                                                                                           |
            | ShadowName              | CLASSIC                                                                                             |
            | SubscribeTopic          | $aws/things/testThing/shadow/update/delta                                                           |
            | UpdateDocumentRequest1  | {\"state\":{\"reported\":{\"color\":{\"r\":255,\"g\":255,\"b\":255},\"SomeKey\":\"SomeValue\"}}}    |
        Then the local Greengrass deployment is SUCCEEDED on the device after 120 seconds
        When I install the component ShadowReactiveComponentPong from local store with configuration
            | key                     | value                                                                                                     |
            | ThingName               | testThing                                                                                                 |
            | ShadowName              | CLASSIC                                                                                                   |
            | UpdateDocumentRequest1  | {\"version\":1,\"state\":{\"desired\":{\"color\":{\"r\":255,\"g\":0,\"b\":0},\"SomeKey\":\"SomeValue\"}}} |
        Then the local Greengrass deployment is SUCCEEDED on the device after 120 seconds

    Scenario: Shadow-1-T8: As a customer, I can get the message on the accepted topic after a shadow update
        When I install the component ShadowReactiveComponentPing from local store with configuration
            | key                     | value                                         |
            | ThingName               | testThing                                     |
            | ShadowName              | CLASSIC                                       |
            | SubscribeTopic          | $aws/things/testThing/shadow/update/accepted |
        Then the local Greengrass deployment is SUCCEEDED on the device after 120 seconds
        When I install the component ShadowReactiveComponentPong from local store with configuration
            | key                     | value                                                                                                          |
            | ThingName               | testThing                                                                                                      |
            | ShadowName              | CLASSIC                                                                                                        |
            | UpdateDocumentRequest1  | {\"state\":{\"reported\":{\"color\":{\"r\":255,\"g\":255,\"b\":255},\"SomeKey\":\"SomeValue\"}}}               |
        Then the local Greengrass deployment is SUCCEEDED on the device after 120 seconds

    Scenario: Shadow-1-T9: As a customer, I can get the message on the rejected topic after a shadow update
        When I install the component ShadowReactiveComponentPing from local store with configuration
            | key                     | value                                         |
            | ThingName               | testThing                                     |
            | ShadowName              | CLASSIC                                       |
            | SubscribeTopic          | $aws/things/testThing/shadow/update/rejected  |
        Then the local Greengrass deployment is SUCCEEDED on the device after 120 seconds
        When I install the component ShadowReactiveComponentPong from local store with configuration
            | key                     | value                                                                                                           |
            | ThingName               | testThing                                                                                                       |
            | ShadowName              | CLASSIC                                                                                                         |
            | UpdateDocumentRequest1  | {\"version\":10,\"state\":{\"reported\":{\"color\":{\"r\":255,\"g\":255,\"b\":255},\"SomeKey\":\"SomeValue\"}}} |
        Then the local Greengrass deployment is SUCCEEDED on the device after 120 seconds

    Scenario: Shadow-1-T10: As a customer, I can get the message on the accepted topic after a shadow delete
        When I install the component ShadowReactiveComponentPing from local store with configuration
            | key                     | value                                           |
            | ThingName               | testThing                                       |
            | ShadowName              | CLASSIC                                         |
            | SubscribeTopic          | $aws/things/testThing/shadow/delete/accepted    |
        And I install the component ShadowComponentPing from local store with configuration
            | key                    | value                                                                                                          |
            | Operation              | UpdateThingShadow                                                                                              |
            | ThingName              | testThing                                                                                                      |
            | ShadowName             | CLASSIC                                                                                                        |
            | ShadowDocument         | {\"state\":{\"reported\":{\"color\":{\"r\":255,\"g\":255,\"b\":255},\"SomeKey\":\"SomeValue\"}}}               |
        Then the local Greengrass deployment is SUCCEEDED on the device after 120 seconds
        When I install the component ShadowComponentPong from local store with configuration
            | key            | value             |
            | Operation      | DeleteThingShadow |
            | ThingName      | testThing         |
            | ShadowName     | CLASSIC           |
        Then the local Greengrass deployment is SUCCEEDED on the device after 120 seconds

    Scenario: Shadow-1-T11: As a customer, I can get the message on the rejected topic after a shadow delete
        When I install the component ShadowReactiveComponentPing from local store with configuration
            | key                     | value                                         |
            | ThingName               | testThing                                     |
            | ShadowName              | CLASSIC                                       |
            | SubscribeTopic          | $aws/things/testThing/shadow/delete/rejected |
        When I install the component ShadowComponentPong from local store with configuration
            | key            | value             |
            | Operation      | DeleteThingShadow |
            | ThingName      | testThing         |
            | ShadowName     | CLASSIC           |
        Then the local Greengrass deployment is SUCCEEDED on the device after 120 seconds

    Scenario: Shadow-1-T12: As a customer, I can get the message on the accepted topic after a shadow get
        When I install the component ShadowReactiveComponentPing from local store with configuration
            | key                     | value                                      |
            | ThingName               | testThing                                  |
            | ShadowName              | CLASSIC                                    |
            | SubscribeTopic          | $aws/things/testThing/shadow/get/accepted |
        And I install the component ShadowComponentPing from local store with configuration
            | key                    | value                                                                                                          |
            | Operation              | UpdateThingShadow                                                                                              |
            | ThingName              | testThing                                                                                                      |
            | ShadowName             | CLASSIC                                                                                                        |
            | ShadowDocument         | {\"state\":{\"reported\":{\"color\":{\"r\":255,\"g\":255,\"b\":255},\"SomeKey\":\"SomeValue\"}}}               |
        Then the local Greengrass deployment is SUCCEEDED on the device after 120 seconds
        When I install the component ShadowComponentPong from local store with configuration
            | key            | value          |
            | Operation      | GetThingShadow |
            | ThingName      | testThing      |
            | ShadowName     | CLASSIC        |
        Then the local Greengrass deployment is SUCCEEDED on the device after 120 seconds

    Scenario: Shadow-1-T13: As a customer, I can get the message on the rejected topic after a shadow get
        When I install the component ShadowReactiveComponentPing from local store with configuration
            | key                     | value                                      |
            | ThingName               | testThing                                  |
            | ShadowName              | CLASSIC                                    |
            | SubscribeTopic          | $aws/things/testThing/shadow/get/rejected |
        When I install the component ShadowComponentPong from local store with configuration
            | key            | value          |
            | Operation      | GetThingShadow |
            | ThingName      | testThing      |
            | ShadowName     | CLASSIC        |
        Then the local Greengrass deployment is SUCCEEDED on the device after 120 seconds
