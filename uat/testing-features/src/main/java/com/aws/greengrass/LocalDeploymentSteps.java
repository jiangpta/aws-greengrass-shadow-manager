/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aws.greengrass;

import com.aws.greengrass.testing.api.ComponentPreparationService;
import com.aws.greengrass.testing.api.device.Device;
import com.aws.greengrass.testing.api.device.exception.CommandExecutionException;
import com.aws.greengrass.testing.api.device.model.CommandInput;
import com.aws.greengrass.testing.api.model.ComponentOverrideNameVersion;
import com.aws.greengrass.testing.api.model.ComponentOverrideVersion;
import com.aws.greengrass.testing.api.model.ComponentOverrides;
import com.aws.greengrass.testing.features.WaitSteps;
import com.aws.greengrass.testing.model.ScenarioContext;
import com.aws.greengrass.testing.model.TestContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import io.cucumber.datatable.DataTable;
import io.cucumber.guice.ScenarioScoped;
import io.cucumber.java.en.When;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.services.greengrassv2.model.ComponentDeploymentSpecification;
import software.amazon.awssdk.utils.ImmutableMap;
import software.amazon.awssdk.utils.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import javax.inject.Inject;

import static com.aws.greengrass.testing.component.LocalComponentPreparationService.ARTIFACTS_DIR;
import static com.aws.greengrass.testing.component.LocalComponentPreparationService.LOCAL_STORE;
import static com.aws.greengrass.testing.component.LocalComponentPreparationService.RECIPE_DIR;
import static com.aws.greengrass.testing.features.GreengrassCliSteps.LOCAL_DEPLOYMENT_ID;

@ScenarioScoped
public class LocalDeploymentSteps {
    private static final Logger LOGGER = LogManager.getLogger(LocalDeploymentSteps.class);
    private static final String THING_GROUP_DEPLOYMENT_TARGET_TYPE = "thinggroup";
    private static final String MERGE_CONFIG = "MERGE";
    private static final String RESET_CONFIG = "RESET";
    private static final Path LOCAL_STORE_RECIPES = Paths.get("local:", "local-store", "recipes");
    private static final int MAX_DEPLOYMENT_RETRY_COUNT = 3;
    private final ComponentPreparationService componentPreparation;
    private final ComponentOverrides overrides;
    private final TestContext testContext;
    private final WaitSteps waits;
    private final ObjectMapper mapper;
    private final ScenarioContext scenarioContext;
    private final Path artifactPath;
    private final Path recipePath;
    private final Device device;

    @Inject
    @SuppressWarnings("MissingJavadocMethod")
    public LocalDeploymentSteps(
            final ComponentOverrides overrides,
            final TestContext testContext,
            final ComponentPreparationService componentPreparation,
            final ScenarioContext scenarioContext,
            final WaitSteps waits,
            final ObjectMapper mapper,
            final Device device) {
        this.overrides = overrides;
        this.testContext = testContext;
        this.componentPreparation = componentPreparation;
        this.scenarioContext = scenarioContext;
        this.waits = waits;
        this.mapper = mapper;
        this.artifactPath = testContext.installRoot().resolve(LOCAL_STORE).resolve(ARTIFACTS_DIR);
        this.recipePath = testContext.installRoot().resolve(LOCAL_STORE).resolve(RECIPE_DIR);
        this.device = device;
    }

    /**
     * implemented the step of installing a custom component with configuration.
     *
     * @param componentName      the name of the custom component
     * @param configurationTable the table which describes the configurations
     * @throws InterruptedException InterruptedException could be throw out during the component deployment
     * @throws IOException          IOException could be throw out during preparation of the CLI command
     */
    @When("I install the component {word} from local store with configuration")
    public void installComponentWithConfiguration(final String componentName, final DataTable configurationTable)
            throws InterruptedException, IOException {
        List<Map<String, String>> configuration = configurationTable.asMaps(String.class, String.class);
        List<String> componentSpecs = Arrays.asList(
                componentName, LOCAL_STORE_RECIPES.resolve(String.format("%s.yaml", componentName)).toString()
        );
        installComponent(componentSpecs, configuration);
    }

    /**
     * implemented the step of updating a custom component's configuration.
     *
     * @param componentName      the name of the custom component
     * @param configurationTable the table which describes the configurations
     * @throws InterruptedException InterruptedException could be throw out during the component deployment
     * @throws IOException          IOException could be throw out during preparation of the CLI command
     */
    @When("I update the component {word} with configuration")
    public void updateComponentConfiguration(final String componentName, final DataTable configurationTable)
            throws InterruptedException, IOException {
        List<Map<String, String>> configuration = configurationTable.asMaps(String.class, String.class);
        CommandInput command = getCliDeploymentCommand(componentName, null, configuration);
        createLocalDeploymentWithRetry(command, 0);
    }

    /**
     * test for creating an empty deployment configuration.
     *
     * @param deploymentName name of deployment
     */
    @When("I create an empty deployment configuration for deployment {word}")
    public void createDeploymentConfigForDeployment(final String deploymentName) {
        createBaseDeploymentConfiguration(deploymentName,
                THING_GROUP_DEPLOYMENT_TARGET_TYPE, "gg");
        //TODO: "gg" was temperal replacement, in evergreen it was kernel.getThingGroups().get(0));
    }

    /**
     * update Shadow Component Deployment With Configuration.
     *
     * @throws IOException ioexception might be throw out.
     */
    @When("I update the deployment configuration {word}, setting the shadow component version {string} configuration"
            + " with {int} named shadows with prefix {word} per {int} things with prefix {word}:")
    public void updateShadowComponentDeploymentWithConfiguration() throws IOException {
        //    String deploymentName, String componentVersion,
        //                                                                 int noOfNamedShadows, String shadowPrefix,
        //                                                                 int noOfThings, String thingPrefix,
        //                                                                 String configuration)
        //TODO: temperal copied from Evergreen DeploymentSteps.java
        //        List<ShadowDocument> shadowDocuments = new ArrayList<>();
        //        for (int i = 0; i < noOfThings; i++) {
        //            String thingName = scenarioContextManager.getStringFromContext(thingPrefix);
        //            if (noOfThings > 1) {
        //                thingName = thingName + i;
        //            }
        //            ShadowDocument shadowDocument = new ShadowDocument();
        //            shadowDocument.setNamedShadows(new ArrayList<>());
        //            shadowDocument.setThingName(thingName);
        //            for (int j = 0; j < noOfNamedShadows; j++) {
        //                String shadowName = scenarioContextManager.getStringFromContext(shadowPrefix) + j;
        //                shadowDocument.getNamedShadows().add(shadowName);
        //            }
        //            shadowDocuments.add(shadowDocument);
        //        }
        //        configuration = configuration.replace("processedShadowDocuments",
        //                mapper.writeValueAsString(shadowDocuments));
        //
        //        updateDeploymentComponentWithConfiguration(deploymentName, "aws.greengrass.ShadowManager",
        //        componentVersion, configuration);
    }

    void createBaseDeploymentConfiguration(String deploymentName, String targetType, String targetName) {
        //TDOO: the implements need to be revised.
        //        String actualTargetName;
        //        if (THING_GROUP_DEPLOYMENT_TARGET_TYPE.equals(targetType)) {
        //            IotThingGroup group = awsResources.getResources().getThingGroups().stream()
        //                    .filter(g -> g.getGroupName().startsWith(targetName + "e2e-")
        //                            || g.getGroupName().equals(targetName)).findFirst().get();
        //            actualTargetName = group.getGroupArn();
        //        } else {
        //            actualTargetName = awsResources.getSpecs().getThings().stream()
        //                    .filter(t -> t.getResultingThing().getThingName().startsWith(targetName + "e2e-")
        //                            || t.getResultingThing().getThingName().equals(targetName)).findFirst().get()
        //                    .getResultingThing().getThingArn();
        //        }
        //
        //        this.deploymentConfigurations.putIfAbsent(deploymentName,
        //                new CreateDeploymentRequest()
        //                        .withDeploymentName(deploymentName)
        //                        .withTargetArn(actualTargetName).withDeploymentPolicies(
        //                                new DeploymentPolicies()
        //                                        .withFailureHandlingPolicy(DeploymentFailureHandlingPolicy.DO_NOTHING)
        //                                        .withComponentUpdatePolicy(new DeploymentComponentUpdatePolicy()
        //                                          .withAction(DeploymentComponentUpdatePolicyAction.NOTIFY_COMPONENTS)
        //                                          .withTimeoutInSeconds(120))
        //                                        .withConfigurationValidationPolicy(
        //                                                new DeploymentConfigurationValidationPolicy()
        //                                                        .withTimeoutInSeconds(120)))
        //                        // Adding this since the API does not handle packages being null.
        //                        .withComponents(new HashMap<>()));
        //        this.deploymentConfigurations.get(deploymentName).withClientToken(UUID.randomUUID().toString());
    }

    private CommandInput getCliDeploymentCommand(String componentName, String componentVersion,
                                                 List<Map<String, String>> configuration) throws IOException {
        List<String> commandArgs = new ArrayList<>(Arrays.asList(
                "deployment",
                "create",
                "--artifactDir " + artifactPath.toString(),
                "--recipeDir " + recipePath.toString()));
        if (StringUtils.isNotBlank(componentVersion)) {
            commandArgs.add("--merge " + componentName + "=" + componentVersion);
        }
        String updateConfigArgs = getCliUpdateConfigArgs(componentName, configuration);
        if (!updateConfigArgs.isEmpty()) {
            commandArgs.add("--update-config '" + updateConfigArgs + "'");
        }

        return CommandInput.builder()
                .line(testContext.installRoot().resolve("bin").resolve("greengrass-cli").toString())
                .addAllArgs(commandArgs)
                .build();
    }

    private void createLocalDeploymentWithRetry(CommandInput commandInput, int retryCount) throws InterruptedException {
        try {
            String response = executeCommand(commandInput);
            LOGGER.info("The response from executing gg-cli command is {}", response);

            String[] responseArray = response.split(":");
            String deploymentId = responseArray[responseArray.length - 1];
            LOGGER.info("The local deployment response is " + deploymentId);
            scenarioContext.put(LOCAL_DEPLOYMENT_ID, deploymentId);
        } catch (CommandExecutionException e) {
            if (retryCount > MAX_DEPLOYMENT_RETRY_COUNT) {
                throw e;
            }
            waits.until(5, "SECONDS");
            LOGGER.warn("the deployment request threw an exception, retried {} times...",
                    retryCount, e);
            this.createLocalDeploymentWithRetry(commandInput, retryCount + 1);
        }
    }

    private String executeCommand(CommandInput input) throws CommandExecutionException {
        final StringJoiner joiner = new StringJoiner(" ").add(input.line());
        Optional.ofNullable(input.args()).ifPresent(args -> args.forEach(joiner::add));
        byte[] op = device.execute(CommandInput.builder()
                .workingDirectory(input.workingDirectory())
                .line("sh")
                .addArgs("-c", joiner.toString())
                .input(input.input())
                .timeout(input.timeout())
                .build());
        return new String(op, StandardCharsets.UTF_8);
    }

    private void installComponent(List<String> component, List<Map<String, String>> configuration)
            throws InterruptedException, IOException {
        final Map<String, ComponentDeploymentSpecification> localComponentSpec = prepareLocalComponent(component);
        for (Map.Entry<String, ComponentDeploymentSpecification> localComponent : localComponentSpec.entrySet()) {
            String componentName = localComponent.getKey();
            String componentVersion = localComponent.getValue().componentVersion();
            CommandInput command = getCliDeploymentCommand(componentName, componentVersion, configuration);
            createLocalDeploymentWithRetry(command, 0);
        }
    }

    private String getCliUpdateConfigArgs(String componentName, List<Map<String, String>> configuration)
            throws IOException {
        Map<String, Map<String, Object>> configurationUpdate = new HashMap<>();
        // config update for each component, in the format of <componentName, <MERGE/RESET, map>>
        updateConfigObject(componentName, configuration, configurationUpdate);
        if (configurationUpdate.isEmpty()) {
            return "";
        }
        return mapper.writeValueAsString(configurationUpdate);
    }

    @VisibleForTesting
    Map<String, ComponentDeploymentSpecification> prepareLocalComponent(
            List<String> component) {
        String name = component.get(0);
        String value = component.get(1);
        ComponentOverrideNameVersion.Builder overrideNameVersion = ComponentOverrideNameVersion.builder()
                .name(name);
        String[] parts = value.split(":", 2);
        if (parts.length == 2) {
            overrideNameVersion.version(ComponentOverrideVersion.of(parts[0], parts[1]));
        } else {
            overrideNameVersion.version(ComponentOverrideVersion.of("cloud", parts[0]));
        }
        overrides.component(name).ifPresent(overrideNameVersion::from);
        ComponentDeploymentSpecification.Builder builder = ComponentDeploymentSpecification.builder();
        componentPreparation.prepare(overrideNameVersion.build()).ifPresent(nameVersion -> {
            builder.componentVersion(nameVersion.version().value());
        });
        Map<String, ComponentDeploymentSpecification> components = new HashMap<>();
        components.put(name, builder.build());
        return components;
    }

    /**
     * Transform component config to CLI --update-config acceptable format. Put it in given configurationUpdate map.
     *
     * @param component           component name
     * @param configuration       list of configs given by cucumber step
     * @param configurationUpdate format: (componentName, (MERGE or RESET, (KV map)))
     */
    private void updateConfigObject(String component, List<Map<String, String>> configuration,
                                    Map<String, Map<String, Object>> configurationUpdate) throws IOException {
        if (configuration != null) {
            Map<String, Map<String, Object>> componentToConfig = new HashMap<>();
            for (Map<String, String> configKeyValue : configuration) {
                String value = configKeyValue.get("value");
                value = scenarioContext.get(value);
                if (value == null) {
                    value = scenarioContext.applyInline(configKeyValue.get("value"));
                }
                if (value != null && (value.contains(MERGE_CONFIG) || value.contains(RESET_CONFIG))) {
                    configurationUpdate.put(component, mapper.readValue(value, Map.class));
                } else {
                    String[] parts = configKeyValue.get("key").split(":");
                    String componentName;
                    String path;
                    if (parts.length == 1) {
                        componentName = component;
                        path = parts[0];
                    } else {
                        componentName = parts[0];
                        path = parts[1];
                    }

                    Map<String, Object> config = componentToConfig.get(componentName);
                    if (config == null) {
                        config = new HashMap<>();
                        componentToConfig.put(componentName, config);
                    }
                    Object objVal = value;
                    try {
                        objVal = mapper.readValue(value, Map.class);
                    } catch (IllegalArgumentException | IOException ignored) {
                    }
                    config.put(path, objVal);
                }
            }
            for (Map.Entry<String, Map<String, Object>> entry : componentToConfig.entrySet()) {
                configurationUpdate.put(entry.getKey(), ImmutableMap.of(MERGE_CONFIG, entry.getValue()));
            }
        }
    }
}



