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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

class PollenDailyPeakTest {
    private static final ZoneId ROME = ZoneId.of("Europe/Rome");

    @Test
    void findsPeakInEachLocalCalendarDay() {
        Instant now = Instant.parse("2026-09-24T08:00:00Z");
        float[] values = { 2, 8, 3, 4, 9, 5, 1, 7 };
        long start = Instant.parse("2026-09-24T08:00:00Z").getEpochSecond();

        assertEquals(8, peak(start, 12 * 3600, values, 0, ROME, now));
        assertEquals(4, peak(start, 12 * 3600, values, 1, ROME, now));
        assertEquals(9, peak(start, 12 * 3600, values, 2, ROME, now));
        assertEquals(7, peak(start, 12 * 3600, values, 3, ROME, now));
    }

    @Test
    void attributesUtcMidnightToTheCorrectLocalDay() {
        Instant now = Instant.parse("2026-09-24T20:00:00Z");
        long start = Instant.parse("2026-09-24T21:00:00Z").getEpochSecond();

        assertEquals(2, peak(start, 3600, new float[] { 2, 7 }, 0, ROME, now));
        assertEquals(7, peak(start, 3600, new float[] { 2, 7 }, 1, ROME, now));
    }

    @Test
    void ignoresPastHoursButIncludesCurrentForecastHour() {
        Instant now = Instant.parse("2026-09-24T10:45:00Z");
        long start = Instant.parse("2026-09-24T09:00:00Z").getEpochSecond();

        assertEquals(4, peak(start, 3600, new float[] { 99, 4, 2 }, 0, ROME, now));
    }

    @Test
    void ignoresInvalidValuesAndReturnsNaNWhenDayHasNoValidForecast() {
        Instant now = Instant.parse("2026-09-24T10:00:00Z");
        long start = now.getEpochSecond();

        assertEquals(3, peak(start, 3600, new float[] { Float.NaN, 3, Float.POSITIVE_INFINITY }, 0, ROME, now));
        assertTrue(Float.isNaN(peak(start, 3600, new float[] { Float.NaN, Float.NEGATIVE_INFINITY }, 0, ROME,
                now)));
        assertTrue(Float.isNaN(peak(start, 3600, new float[] { 2, 3 }, 1, ROME, now)));
    }

    @Test
    void handlesRepeatedHourWhenDaylightSavingTimeEnds() {
        Instant now = Instant.parse("2026-10-25T00:00:00Z");
        long start = Instant.parse("2026-10-25T00:00:00Z").getEpochSecond();

        assertEquals(8, peak(start, 3600, new float[] { 3, 8, 2 }, 0, ROME, now));
    }

    private float peak(long start, int interval, float[] values, int dayOffset, ZoneId zone, Instant now) {
        return PollenDailyPeak.maximum(start, interval, values.length, index -> values[index], dayOffset, zone, now);
    }
}
