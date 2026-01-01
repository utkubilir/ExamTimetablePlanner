# CSV File Format Specification

This document defines the canonical format for all CSV files used by ExamTimetablePlanner.

## General Rules

- **Encoding:** UTF-8. BOM characters are automatically stripped.
- **Delimiter:** Semicolon (`;`) for classrooms, single-column for others.
- **File detection:** The importer uses heuristic detection (`detectCsvKind`) based on headers.

---

## 1. Courses

| Column | Type | Required | Description |
|--------|------|----------|-------------|
| CourseCode | string | Yes | Unique course identifier |

**File pattern:** `*Courses*.csv`  
**Header:** First line is ignored (treated as header)

```csv
ALL OF THE COURSES IN THE SYSTEM
CourseCode_01
CourseCode_02
CourseCode_03
```

---

## 2. Classrooms

| Column | Type | Required | Description |
|--------|------|----------|-------------|
| ClassroomID | string | Yes | Unique classroom identifier |
| Capacity | integer | Yes | Maximum student capacity (must be > 0) |

**File pattern:** `*Classrooms*.csv`  
**Delimiter:** Semicolon (`;`)  
**Header:** First line is ignored

```csv
ALL OF THE CLASSROOMS; AND THEIR CAPACITIES IN THE SYSTEM
Classroom_01;25
Classroom_02;30
Classroom_03;40
```

---

## 3. Students

| Column | Type | Required | Description |
|--------|------|----------|-------------|
| StudentID | string | Yes | Unique student identifier |

**File pattern:** `*Students*.csv`  
**Header:** First line is ignored

```csv
ALL OF THE STUDENTS IN THE SYSTEM
Std_ID_001
Std_ID_002
Std_ID_003
```

---

## 4. Attendance (Enrollment)

**Format:** Non-standard. Alternating lines:
1. Course code (plain text)
2. Python-list-style array of student IDs

**File pattern:** `*Attendance*.csv`  
**No header row.**

```
CourseCode_01
['Std_ID_001', 'Std_ID_002', 'Std_ID_003']

CourseCode_02
['Std_ID_004', 'Std_ID_005']
```

> **Note:** Students referenced in attendance but not in students.csv are auto-created.

---

## Sample Data

Sample datasets are available in `sample_data/`:

| Dataset | Courses | Students | Classrooms | Use Case |
|---------|---------|----------|------------|----------|
| small | 12 | 100 | 6 | Quick demos |
| medium | ~50 | ~1,000 | ~20 | Standard testing |
| large | ~200 | ~5,000 | ~50 | Load testing |
| extralarge | 500+ | 10,000+ | 100+ | Stress testing |

---

## Validation Rules

The importer validates:
- Course codes are unique
- Classroom capacities are positive integers
- Student IDs are unique
- Exam durations (if specified) are positive

Duplicate entries trigger warnings but don't halt import.
