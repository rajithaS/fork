/*
 * Copyright 2015 Shazam Entertainment Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.shazam.fork.runner.listeners;

import com.android.ddmlib.testrunner.TestIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.shazam.fork.utils.Utils.millisSinceNanoTime;
import static java.lang.System.nanoTime;

class SlowWarningTestRunListener extends NoOpITestRunListener {
    private static final Logger logger = LoggerFactory.getLogger(SlowWarningTestRunListener.class);
    private static final long TEST_LENGTH_THRESHOLD_MILLIS = 30 * 1000;
    private long startTime;

    @Override
    public void testStarted(TestIdentifier test) {
        startTime = nanoTime();
    }

    @Override
    public void testEnded(TestIdentifier test, Map<String, String> testMetrics) {
        long testDuration = millisSinceNanoTime(startTime);
        if (testDuration > TEST_LENGTH_THRESHOLD_MILLIS) {
            logger.warn("Slow test ({}ms): {} {}", testDuration, test.getClassName(), test.getTestName());
        }
    }
}
