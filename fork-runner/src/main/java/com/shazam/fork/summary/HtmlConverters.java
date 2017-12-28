/*
 * Copyright 2014 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.shazam.fork.summary;

import com.android.ddmlib.logcat.LogCatMessage;

import java.util.function.Function;

import com.shazam.fork.model.Device;
import com.shazam.fork.model.Diagnostics;

import javax.annotation.Nullable;

import static com.shazam.fork.model.Diagnostics.SCREENSHOTS;
import static com.shazam.fork.model.Diagnostics.VIDEO;
import static com.shazam.fork.summary.OutcomeAggregator.toPoolOutcome;
import static com.shazam.fork.utils.ReadableNames.*;
import static java.util.stream.Collectors.*;

class HtmlConverters {

    public static HtmlSummary toHtmlSummary(Summary summary) {
        HtmlSummary htmlSummary = new HtmlSummary();
        htmlSummary.title = summary.getTitle();
        htmlSummary.subtitle = summary.getSubtitle();
        htmlSummary.pools = summary.getPoolSummaries()
                .stream()
                .map(toHtmlPoolSummary())
                .collect(toList());
        htmlSummary.ignoredTests = summary.getIgnoredTests();
        htmlSummary.failedTests = summary.getFailedTests();
        htmlSummary.overallStatus = new OutcomeAggregator().aggregate(summary) ? "pass" : "fail";
        return htmlSummary;
    }

    private static Function<PoolSummary, HtmlPoolSummary> toHtmlPoolSummary() {
        return new Function<PoolSummary, HtmlPoolSummary>() {
            @Override
            public HtmlPoolSummary apply(@Nullable PoolSummary poolSummary) {
                HtmlPoolSummary htmlPoolSummary = new HtmlPoolSummary();
                htmlPoolSummary.poolStatus = getPoolStatus(poolSummary);
                String poolName = poolSummary.getPoolName();
                htmlPoolSummary.prettyPoolName = readablePoolName(poolName);
                htmlPoolSummary.plainPoolName = poolName;
                htmlPoolSummary.testCount = poolSummary.getTestResults().size();
                htmlPoolSummary.testResults = poolSummary
                        .getTestResults()
                        .stream()
                        .map(a -> toHtmlTestResult(poolName).apply(a))
                        .collect(toList());
                return htmlPoolSummary;
            }

            private String getPoolStatus(PoolSummary poolSummary) {
                Boolean success = toPoolOutcome().apply(poolSummary);
                return (success != null && success ? "pass" : "fail");
            }
        };
    }

    private static Function<TestResult, HtmlTestResult> toHtmlTestResult(final String poolName) {
        return input -> {
            HtmlTestResult htmlTestResult = new HtmlTestResult();
            htmlTestResult.status = computeStatus(input);
            htmlTestResult.prettyClassName = readableClassName(input.getTestClass());
            htmlTestResult.prettyMethodName = readableTestMethodName(input.getTestMethod());
            htmlTestResult.timeTaken = String.format("%.2f", input.getTimeTaken());
            htmlTestResult.plainMethodName = input.getTestMethod();
            htmlTestResult.plainClassName = input.getTestClass();
            htmlTestResult.poolName = poolName;
            htmlTestResult.trace = input.getTrace().split("\n");
            // Keeping logcats in memory is hugely wasteful. Now they're read at page-creation.
            // htmlTestResult.logcatMessages = transform(input.getLogCatMessages(), toHtmlLogCatMessages());
            Device device = input.getDevice();
            htmlTestResult.deviceSerial = device.getSerial();
            htmlTestResult.deviceSafeSerial = device.getSafeSerial();
            htmlTestResult.deviceModelDespaced = device.getModelName().replace(" ", "_");
            Diagnostics supportedDiagnostics = device.getSupportedDiagnostics();
            htmlTestResult.diagnosticVideo = VIDEO.equals(supportedDiagnostics);
            htmlTestResult.diagnosticScreenshots = SCREENSHOTS.equals(supportedDiagnostics);
            return htmlTestResult;
        };
    }

    private static String computeStatus(@Nullable TestResult input) {
        String result = input.getResultStatus().name().toLowerCase();
        if (input.getResultStatus() == ResultStatus.PASS
                && input.getTotalFailureCount() > 0) {
            result = "warn";
        }
        return result;
    }

    public static Function<LogCatMessage, HtmlLogCatMessage> toHtmlLogCatMessages() {
        return logCatMessage -> {
            HtmlLogCatMessage htmlLogCatMessage = new HtmlLogCatMessage();
            htmlLogCatMessage.appName = logCatMessage.getAppName();
            htmlLogCatMessage.logLevel = logCatMessage.getLogLevel().getStringValue();
            htmlLogCatMessage.message = logCatMessage.getMessage();
            htmlLogCatMessage.pid = logCatMessage.getPid();
            htmlLogCatMessage.tag = logCatMessage.getTag();
            htmlLogCatMessage.tid = logCatMessage.getTid();
            htmlLogCatMessage.time = logCatMessage.getTimestamp().toString();
            return htmlLogCatMessage;
        };
    }
}
