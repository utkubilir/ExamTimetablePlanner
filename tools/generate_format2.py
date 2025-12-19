import csv
import random
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
STUDENTS_FILE = ROOT / 'format2_AllStudents_1000.csv'
OUT_STUDENTS = ROOT / 'format2_AllStudents_1000.csv'
OUT_COURSES = ROOT / 'format2_AllCourses.csv'
OUT_CLASSROOMS = ROOT / 'format2_AllClassrooms.csv'
OUT_ATTENDANCE = ROOT / 'format2_AllAttendanceLists (1).csv'

# Read existing students to preserve IDs/names
students = []
with STUDENTS_FILE.open(newline='') as f:
    reader = csv.reader(f)
    header = next(reader)
    for r in reader:
        if not r: continue
        students.append(r)

num_students = len(students)

# Define 40 courses with varied participant counts (range 30-200)
course_codes = [f'CourseCode_{i:03d}' for i in range(1, 41)]
# distribution: 5x200, 10x150, 15x80, 10x30 -> total participants = 4000
counts = [200]*5 + [150]*10 + [80]*15 + [30]*10
assert len(counts) == len(course_codes)

# Create courses with durations (choose among 90,120,150)
durations = [90, 120, 150]
courses = [(code, f'Course {code.split("_")[-1]}', random.choice(durations)) for code in course_codes]

# Create classrooms with varied capacities (ensure some large rooms up to 250)
classrooms = []
for i, cap in enumerate([50, 80, 100, 120, 150, 200, 220, 250, 60, 140, 180, 90, 160, 70, 110, 130, 170, 210, 55, 95, 125, 175, 195, 85, 45]):
    classrooms.append((f'R{i+1:03d}', f'Room {i+1:03d}', cap))

# Generate attendance lists by assigning students to courses to meet counts
attendance = []
# We'll assign by cycling through students with a shuffled start to spread load
student_ids = [s[0] for s in students]
pointer = 0
random.seed(42)
# To avoid bias, create a shuffled order and rotate per course
base_order = student_ids.copy()
random.shuffle(base_order)

for idx, (course, target) in enumerate(zip(course_codes, counts)):
    # rotate base_order by idx to vary assigned students
    order = base_order[idx:] + base_order[:idx]
    # take first 'target' students from order
    selected = order[:target]
    for sid in selected:
        attendance.append((sid, course))

# Sanity: total assignments
# Write out CSVs
with OUT_COURSES.open('w', newline='') as f:
    writer = csv.writer(f)
    writer.writerow(['CourseCode','CourseName','DurationMinutes'])
    for c in courses:
        writer.writerow(c)

with OUT_CLASSROOMS.open('w', newline='') as f:
    writer = csv.writer(f)
    writer.writerow(['RoomID','RoomName','Capacity'])
    for r in classrooms:
        writer.writerow(r)

with OUT_ATTENDANCE.open('w', newline='') as f:
    writer = csv.writer(f)
    writer.writerow(['StudentID','CourseCode'])
    for a in attendance:
        writer.writerow(a)

# Students file: preserve as-is but rewrite to ensure consistent formatting
with OUT_STUDENTS.open('w', newline='') as f:
    writer = csv.writer(f)
    writer.writerow(['StudentID','StudentName'])
    for s in students:
        writer.writerow(s)

print('Generated files:', OUT_STUDENTS.name, OUT_COURSES.name, OUT_CLASSROOMS.name, OUT_ATTENDANCE.name)
