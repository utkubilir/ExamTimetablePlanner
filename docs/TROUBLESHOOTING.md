# Troubleshooting Guide

Common issues and solutions for ExamTimetablePlanner.

---

## Scheduling Issues

### "No feasible solution" Error

**Symptoms:** Algorithm fails to find any valid schedule.

**Causes:**
- Constraints too tight for available resources
- Too many overlapping student enrollments
- Insufficient classroom capacity

**Solutions:**
1. Increase the exam period (add more available days)
2. Add classrooms with larger capacity
3. Check for students enrolled in an unusually high number of courses
4. Verify no single course exceeds total classroom capacity

### Schedule Takes Very Long

**Symptoms:** Generation time exceeds 10+ seconds.

**Causes:**
- Very large dataset with many constraint conflicts
- Algorithm exploring many dead-end branches

**Solutions:**
1. Use sample datasets first to verify setup
2. Check for data quality issues (duplicate enrollments, etc.)
3. The algorithm is exponential worst-case; some inputs are inherently slow

---

## Import Issues

### "Wrong file format" Warning

**Symptoms:** Import dialog rejects a CSV file.

**Causes:**
- File doesn't match expected pattern
- Wrong delimiter used
- Encoding issues

**Solutions:**
1. Verify file matches pattern in [CSV_SPEC.md](CSV_SPEC.md)
2. Ensure file is UTF-8 encoded
3. Check delimiter: semicolon (`;`) for classrooms, single-column for others

### Students Not Appearing

**Symptoms:** Imported attendance data shows fewer students than expected.

**Causes:**
- Malformed Python list syntax in attendance file
- Students not properly quoted

**Solutions:**
1. Verify attendance format uses exact syntax:
   ```
   CourseCode_01
   ['Std_ID_001', 'Std_ID_002']
   ```
2. Ensure single quotes around each student ID
3. Check for trailing whitespace or invisible characters

---

## Application Issues

### Application Won't Start

**Symptoms:** Nothing happens when running `mvn javafx:run`.

**Causes:**
- JavaFX not properly configured
- Wrong Java version
- Missing dependencies

**Solutions:**
```bash
# 1. Verify Java 21+
java -version

# 2. Clean and rebuild
mvn clean install

# 3. Run with explicit JavaFX
mvn javafx:run
```

### UI Freezes During Generation

**Symptoms:** Interface becomes unresponsive while scheduling.

**Causes:**
- Large dataset processing
- UI thread blocked

**Solutions:**
- This is expected for large datasets (~10k students)
- Wait for completion; generation runs on background thread
- Progress indicator should show activity

### Dark Mode Not Working

**Symptoms:** Theme toggle doesn't change appearance.

**Causes:**
- CSS file not loaded
- Browser cache (for docs site)

**Solutions:**
1. Refresh the page (Ctrl+Shift+R)
2. Check browser console for CSS errors

---

## Database Issues

### "Database locked" Error

**Symptoms:** Error on import or export operations.

**Causes:**
- Multiple application instances running
- Previous operation didn't complete

**Solutions:**
1. Close all application instances
2. Delete `examplanner.db` and re-import data
3. Restart the application

---

## Getting Help

If your issue isn't covered here:

1. Check [GitHub Issues](https://github.com/UtkuBilir/ExamTimetablePlanner/issues)
2. Review the [FAQ section](https://utkubilir.github.io/ExamTimetablePlanner/#faq)
3. Open a new issue with:
   - Steps to reproduce
   - Error message (if any)
   - Dataset size and characteristics
   - Java version and OS
