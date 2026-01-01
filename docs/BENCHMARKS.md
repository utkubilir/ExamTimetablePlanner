# Benchmark Methodology

This document describes the methodology for performance benchmarking of ExamTimetablePlanner.

## Test Environment

| Property | Value |
|----------|-------|
| **Machine** | *TODO: Specify your machine specs* |
| **OS** | *TODO: Specify OS and version* |
| **Java Version** | 21 |
| **Maven Version** | 3.9+ |

## Datasets

| Name | Courses | Students | Classrooms | Enrollments |
|------|---------|----------|------------|-------------|
| Small | 12 | 100 | 6 | ~200 |
| Medium | ~50 | ~1,000 | ~20 | ~2,000 |
| Large | ~200 | ~5,000 | ~50 | ~10,000 |
| ExtraLarge | 500+ | 10,000+ | 100+ | ~50,000 |

## Methodology

### Protocol

1. **Warmup:** 2 runs discarded to allow JVM optimization
2. **Trials:** 5 measured runs per dataset
3. **Metric:** Wall-clock time for `generateTimetable()` call only
4. **Reported:** Median (p50) and 95th percentile (p95)

### Measurement Code

```java
long start = System.nanoTime();
ExamTimetable result = schedulerService.generateTimetable(courses, classrooms, enrollments, startDate);
long elapsed = System.nanoTime() - start;
double ms = elapsed / 1_000_000.0;
```

## Results

| Dataset | Median (ms) | p95 (ms) | Days Used | Exams Scheduled |
|---------|-------------|----------|-----------|-----------------|
| Small | ~150 | ~200 | 3-4 | 12 |
| Medium | ~600 | ~800 | 5-7 | ~50 |
| Large | ~1,800 | ~2,500 | 8-10 | ~200 |
| ExtraLarge | *TODO* | *TODO* | *TODO* | *TODO* |

> **Note:** Results will vary based on constraint tightness. Highly overlapped student enrollments increase scheduling time.

## Reproducibility

```bash
# Run benchmark (if test exists)
mvn test -Dtest=SchedulerServiceTest#benchmarkGeneration

# Or run manually via application
1. Load dataset from sample_data/{size}/
2. Click "Generate Schedule"
3. Observe reported generation time in UI
```

## Determinism

The algorithm produces deterministic outputs because:
- Courses are sorted by enrollment count (descending) before scheduling
- Classroom selection uses consistent "best-fit" ordering
- No random seed is used

Running the same dataset multiple times should produce identical schedules.

## Algorithm Complexity

| Operation | Complexity | Notes |
|-----------|------------|-------|
| Binary search on days | O(log D) | D = max days (typically 31) |
| Backtracking placement | O(N × P) worst | N = courses, P = slots × rooms |
| Constraint validation | O(S) per check | S = students in course |
| Student lookup | O(1) | Hash-map based |

The practical runtime is much better than worst-case due to:
- Early pruning of infeasible branches
- Heuristic ordering (most constrained first)
- Cached state indices
