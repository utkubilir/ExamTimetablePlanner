package com.examplanner.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;


class ExamSlotTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create exam slot with valid parameters")
        void shouldCreateExamSlotWithValidParameters() {
            LocalDate date = LocalDate.of(2024, 12, 20);
            LocalTime startTime = LocalTime.of(9, 0);
            LocalTime endTime = LocalTime.of(11, 0);

            ExamSlot slot = new ExamSlot(date, startTime, endTime);

            assertEquals(date, slot.getDate());
            assertEquals(startTime, slot.getStartTime());
            assertEquals(endTime, slot.getEndTime());
        }

        @Test
        @DisplayName("Should throw exception for null date")
        void shouldThrowExceptionForNullDate() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new ExamSlot(null, LocalTime.of(9, 0), LocalTime.of(11, 0)));
        }

        @Test
        @DisplayName("Should throw exception for null start time")
        void shouldThrowExceptionForNullStartTime() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new ExamSlot(LocalDate.of(2024, 12, 20), null, LocalTime.of(11, 0)));
        }

        @Test
        @DisplayName("Should throw exception for null end time")
        void shouldThrowExceptionForNullEndTime() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new ExamSlot(LocalDate.of(2024, 12, 20), LocalTime.of(9, 0), null));
        }

        @Test
        @DisplayName("Should throw exception when end time is before start time")
        void shouldThrowExceptionWhenEndTimeBeforeStartTime() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new ExamSlot(
                            LocalDate.of(2024, 12, 20),
                            LocalTime.of(11, 0),
                            LocalTime.of(9, 0)));
        }

        @Test
        @DisplayName("Should throw exception when end time equals start time")
        void shouldThrowExceptionWhenEndTimeEqualsStartTime() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new ExamSlot(
                            LocalDate.of(2024, 12, 20),
                            LocalTime.of(9, 0),
                            LocalTime.of(9, 0)));
        }
    }

    @Nested
    @DisplayName("Duration Calculation Tests")
    class DurationTests {

        @Test
        @DisplayName("Should calculate duration correctly")
        void shouldCalculateDurationCorrectly() {
            ExamSlot slot = new ExamSlot(
                    LocalDate.of(2024, 12, 20),
                    LocalTime.of(9, 0),
                    LocalTime.of(11, 30));

            long durationMinutes = Duration.between(slot.getStartTime(), slot.getEndTime()).toMinutes();
            assertEquals(150, durationMinutes);
        }
    }

    @Nested
    @DisplayName("Overlap Tests")
    class OverlapTests {

        @Test
        @DisplayName("Should detect overlapping slots on same day")
        void shouldDetectOverlappingSlotsOnSameDay() {
            ExamSlot slot1 = new ExamSlot(
                    LocalDate.of(2024, 12, 20),
                    LocalTime.of(9, 0),
                    LocalTime.of(11, 0));
            ExamSlot slot2 = new ExamSlot(
                    LocalDate.of(2024, 12, 20),
                    LocalTime.of(10, 0),
                    LocalTime.of(12, 0));

            assertTrue(slot1.overlaps(slot2));
            assertTrue(slot2.overlaps(slot1));
        }

        @Test
        @DisplayName("Should not detect overlap on different days")
        void shouldNotDetectOverlapOnDifferentDays() {
            ExamSlot slot1 = new ExamSlot(
                    LocalDate.of(2024, 12, 20),
                    LocalTime.of(9, 0),
                    LocalTime.of(11, 0));
            ExamSlot slot2 = new ExamSlot(
                    LocalDate.of(2024, 12, 21),
                    LocalTime.of(9, 0),
                    LocalTime.of(11, 0));

            assertFalse(slot1.overlaps(slot2));
        }

        @Test
        @DisplayName("Should not detect overlap for adjacent slots")
        void shouldNotDetectOverlapForAdjacentSlots() {
            ExamSlot slot1 = new ExamSlot(
                    LocalDate.of(2024, 12, 20),
                    LocalTime.of(9, 0),
                    LocalTime.of(11, 0));
            ExamSlot slot2 = new ExamSlot(
                    LocalDate.of(2024, 12, 20),
                    LocalTime.of(11, 0),
                    LocalTime.of(13, 0));

            assertFalse(slot1.overlaps(slot2));
        }

        @Test
        @DisplayName("Should detect contained slot")
        void shouldDetectContainedSlot() {
            ExamSlot outerSlot = new ExamSlot(
                    LocalDate.of(2024, 12, 20),
                    LocalTime.of(9, 0),
                    LocalTime.of(14, 0));
            ExamSlot innerSlot = new ExamSlot(
                    LocalDate.of(2024, 12, 20),
                    LocalTime.of(10, 0),
                    LocalTime.of(12, 0));

            assertTrue(outerSlot.overlaps(innerSlot));
            assertTrue(innerSlot.overlaps(outerSlot));
        }

        @Test
        @DisplayName("Should not overlap when completely before")
        void shouldNotOverlapWhenCompletelyBefore() {
            ExamSlot slot1 = new ExamSlot(
                    LocalDate.of(2024, 12, 20),
                    LocalTime.of(9, 0),
                    LocalTime.of(10, 0));
            ExamSlot slot2 = new ExamSlot(
                    LocalDate.of(2024, 12, 20),
                    LocalTime.of(11, 0),
                    LocalTime.of(12, 0));

            assertFalse(slot1.overlaps(slot2));
            assertFalse(slot2.overlaps(slot1));
        }
    }

    @Nested
    @DisplayName("Setter Tests")
    class SetterTests {

        @Test
        @DisplayName("Should set date")
        void shouldSetDate() {
            ExamSlot slot = new ExamSlot(
                    LocalDate.of(2024, 12, 20),
                    LocalTime.of(9, 0),
                    LocalTime.of(11, 0));
            LocalDate newDate = LocalDate.of(2024, 12, 25);

            slot.setDate(newDate);

            assertEquals(newDate, slot.getDate());
        }

        @Test
        @DisplayName("Should set start time")
        void shouldSetStartTime() {
            ExamSlot slot = new ExamSlot(
                    LocalDate.of(2024, 12, 20),
                    LocalTime.of(9, 0),
                    LocalTime.of(11, 0));
            LocalTime newStartTime = LocalTime.of(10, 0);

            slot.setStartTime(newStartTime);

            assertEquals(newStartTime, slot.getStartTime());
        }

        @Test
        @DisplayName("Should set end time")
        void shouldSetEndTime() {
            ExamSlot slot = new ExamSlot(
                    LocalDate.of(2024, 12, 20),
                    LocalTime.of(9, 0),
                    LocalTime.of(11, 0));
            LocalTime newEndTime = LocalTime.of(12, 0);

            slot.setEndTime(newEndTime);

            assertEquals(newEndTime, slot.getEndTime());
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should return formatted string")
        void shouldReturnFormattedString() {
            ExamSlot slot = new ExamSlot(
                    LocalDate.of(2024, 12, 20),
                    LocalTime.of(9, 0),
                    LocalTime.of(11, 0));
            String result = slot.toString();

            assertTrue(result.contains("2024-12-20"));
            assertTrue(result.contains("09:00"));
            assertTrue(result.contains("11:00"));
        }
    }
}
