/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */
package com.vmware.test.functional.saas.local.aws.kms;

import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.AliasListEntry;

import java.util.List;
import java.util.Optional;

/**
 * KMS Health Helper.
 */
public final class KmsHealthHelper {

    private KmsHealthHelper() {

    }

    /**
     * KMS Health Helper - verifying the stream status.
     *
     * @param kmsClient {@link KmsClient}.
     * @param aliasName KMS Alias name
     * @return {@code true} if the stream is {@code ACTIVE}, else {@code false}.
     */
    public static boolean checkHealth(final KmsClient kmsClient, final String aliasName) {
        final List<AliasListEntry> aliases = kmsClient.listAliases().aliases();

        final Optional<AliasListEntry> foundAlias = aliases.stream()
                .filter(a -> a.aliasName().equals(aliasName))
                .findAny();
        return foundAlias.isPresent();
    }
}
