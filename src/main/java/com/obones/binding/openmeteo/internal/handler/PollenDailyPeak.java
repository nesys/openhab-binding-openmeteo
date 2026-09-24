/**
 * Copyright (c) 2026 Andrea Riela
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file,
 * you can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package com.obones.binding.openmeteo.internal.handler;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.function.IntToDoubleFunction;

import org.eclipse.jdt.annotation.NonNullByDefault;

/** Computes the peak from hourly forecast values for a calendar day in the openHAB time zone. */
@NonNullByDefault
final class PollenDailyPeak {
    private PollenDailyPeak() {
    }

    static float maximum(long startEpochSecond, int intervalSeconds, int valueCount, IntToDoubleFunction valueAt,
            int dayOffset, ZoneId zone, Instant now) {
        LocalDate targetDate = now.atZone(zone).toLocalDate().plusDays(dayOffset);
        long firstForecastHour = now.truncatedTo(ChronoUnit.HOURS).getEpochSecond();
        float maximum = Float.NEGATIVE_INFINITY;

        for (int index = 0; index < valueCount; index++) {
            long timestamp = startEpochSecond + (long) index * intervalSeconds;
            if (timestamp >= firstForecastHour
                    && targetDate.equals(Instant.ofEpochSecond(timestamp).atZone(zone).toLocalDate())) {
                float value = (float) valueAt.applyAsDouble(index);
                if (Float.isFinite(value)) {
                    maximum = Math.max(maximum, value);
                }
            }
        }
        return Float.isFinite(maximum) ? maximum : Float.NaN;
    }
}
