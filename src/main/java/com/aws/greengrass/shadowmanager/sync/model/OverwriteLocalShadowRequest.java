/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aws.greengrass.shadowmanager.sync.model;

import com.aws.greengrass.logging.api.Logger;
import com.aws.greengrass.logging.impl.LogManager;
import com.aws.greengrass.shadowmanager.exception.RetryableException;
import com.aws.greengrass.shadowmanager.exception.SkipSyncRequestException;
import com.aws.greengrass.shadowmanager.model.ShadowDocument;
import com.aws.greengrass.shadowmanager.model.dao.SyncInformation;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Optional;

import static com.aws.greengrass.shadowmanager.model.Constants.LOG_CLOUD_VERSION_KEY;
import static com.aws.greengrass.shadowmanager.model.Constants.LOG_SHADOW_NAME_KEY;
import static com.aws.greengrass.shadowmanager.model.Constants.LOG_THING_NAME_KEY;

public class OverwriteLocalShadowRequest extends BaseSyncRequest {
    private static final Logger logger = LogManager.getLogger(OverwriteLocalShadowRequest.class);

    /**
     * Ctr for BaseSyncRequest.
     *
     * @param thingName  The thing name associated with the sync shadow update
     * @param shadowName The shadow name associated with the sync shadow update
     */
    public OverwriteLocalShadowRequest(String thingName, String shadowName) {
        super(thingName, shadowName);
    }

    /**
     * Executes sync request.
     *
     * @param context context object containing useful objects for requests to use when executing.
     * @throws RetryableException       When error occurs in sync operation indicating a request needs to be retried
     * @throws SkipSyncRequestException When error occurs in sync operation indicating a request needs to be skipped.
     * @throws InterruptedException     if the thread is interrupted while syncing shadow with cloud.
     */
    @Override
    public void execute(SyncContext context) throws RetryableException, SkipSyncRequestException, InterruptedException {
        super.setContext(context);
        SyncInformation syncInformation = getSyncInformation();

        Optional<ShadowDocument> cloudShadowDocument = getCloudShadowDocument();

        if (cloudShadowDocument.isPresent()) {
            if (syncInformation.getCloudVersion() == cloudShadowDocument.get().getVersion()) {
                logger.atDebug()
                        .kv(LOG_THING_NAME_KEY, getThingName())
                        .kv(LOG_SHADOW_NAME_KEY, getShadowName())
                        .kv(LOG_CLOUD_VERSION_KEY, syncInformation.getCloudVersion())
                        .log("Not updating local shadow since the cloud shadow has not changed since the last sync");
            } else {
                // If the cloud shadow version is not the same as the last synced cloud shadow version, go ahead and
                // update the local shadow with the entire cloud shadow,
                long cloudUpdateTime = getCloudUpdateTime(cloudShadowDocument.get());
                ObjectNode updateDocument = (ObjectNode) cloudShadowDocument.get().toJson(false);
                long localDocumentVersion = updateLocalDocumentAndGetUpdatedVersion(updateDocument, Optional.empty());
                updateSyncInformation(updateDocument, localDocumentVersion, cloudShadowDocument.get().getVersion(),
                        cloudUpdateTime);
            }
        } else {
            // If the cloud shadow is not present, then go ahead and delete the local shadow.
            handleLocalDelete(syncInformation);
        }
    }

    /**
     * Check if an update is necessary or not.
     *
     * @param context context object containing useful objects for requests to use when executing.
     * @return true.
     */
    @Override
    public boolean isUpdateNecessary(SyncContext context) {
        return true;
    }
}
