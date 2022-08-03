/*
 * Copyright 2022 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 */

package com.vmware.test.functional.saas.local.aws.lambda.sam.data;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Preconditions;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;

import lombok.extern.slf4j.Slf4j;

/**
 * Class used for generating AWS SAM templates used for starting lambdas locally.
 */
@Slf4j
public final class SamTemplateGenerator {

    private static final Version FREEMARKER_VERSION = new Version(2, 3, 20);
    /*
     * The number format used by FTL's c built-in (like in someNumber?c).
     * Converts a number to string for a "computer language" as opposed to for human audience.
     */
    private static final String FTL_NUMBER_FORMAT = "computer";
    private static final String FTL_TEMPLATES_LOCATION = "/lambda/templates";
    private static final String FTL_TEMPLATE_FILE_NAME = "template.ftl";
    private static final String FUNCTIONS = "functions";

    private static final Configuration FTL_CONFIG = new Configuration(FREEMARKER_VERSION);

    static {
        FTL_CONFIG.setClassForTemplateLoading(SamTemplateGenerator.class, FTL_TEMPLATES_LOCATION);
        // Some recommended settings
        FTL_CONFIG.setDefaultEncoding(StandardCharsets.UTF_8.name());
        FTL_CONFIG.setLocale(Locale.US);
        FTL_CONFIG.setNumberFormat(FTL_NUMBER_FORMAT);
        FTL_CONFIG.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    private SamTemplateGenerator() {

    }

    /**
     * Helper method to generate AWS SAM template.yaml file.
     *
     * @param samTemplateData Sam template data.
     * @param templateFile    Template file.
     */
    public static void generate(final List<SamFunctionTemplateData> samTemplateData, final String templateFile) {

        final Map<String, Object> templateInput = new HashMap<>();
        templateInput.put(FUNCTIONS, samTemplateData);

        try (Writer fileWriter = new FileWriter(templateFile, StandardCharsets.UTF_8)) {
            final Template template = FTL_CONFIG.getTemplate(FTL_TEMPLATE_FILE_NAME);
            template.process(templateInput, fileWriter);

            Preconditions.checkArgument(FileUtils.getFile(templateFile).exists(),
                    "SAM template file was not successfully generated.");
            log.info("SAM template file [{}] was generated.", FileUtils.getFile(templateFile).getAbsolutePath());
        } catch (final TemplateException | IOException e) {
            throw new RuntimeException("SAM template file was not generated.", e);
        }
    }
}
