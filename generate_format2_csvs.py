import random
import csv

# Configuration
NUM_COURSES = 30
NUM_STUDENTS = 400
NUM_CLASSROOMS = 15
MIN_ENROLLMENTS_PER_COURSE = 30
MAX_ENROLLMENTS_PER_COURSE = 120

random.seed(42)

# 1. Generate Courses
courses = []
for i in range(1, NUM_COURSES + 1):
    code = f"CourseCode_{i:03d}"
    name = f"Course {i:03d}"
    # Vary duration: 60, 90, 120, 150 minutes
    duration = random.choice([60, 90, 120, 150])
    courses.append((code, name, duration))

with open('format2_AllCourses.csv', 'w', newline='', encoding='utf-8') as f:
    writer = csv.writer(f)
    writer.writerow(['CourseCode', 'CourseName', 'DurationMinutes'])
    writer.writerows(courses)
print(f"Created format2_AllCourses.csv with {len(courses)} courses")

# 2. Generate Students
students = []
for i in range(1, NUM_STUDENTS + 1):
    sid = f"Std_ID_{i:04d}"
    name = f"Student {i}"
    students.append((sid, name))

with open('format2_AllStudents.csv', 'w', newline='', encoding='utf-8') as f:
    writer = csv.writer(f)
    writer.writerow(['StudentID', 'StudentName'])
    writer.writerows(students)
print(f"Created format2_AllStudents.csv with {len(students)} students")

# 3. Generate Classrooms with varied capacities
classrooms = []
capacities = [40, 50, 60, 80, 100, 120, 150, 200]
for i in range(1, NUM_CLASSROOMS + 1):
    rid = f"Room_{i:03d}"
    name = f"Classroom {i}"
    # Mix of capacities
    capacity = capacities[i % len(capacities)]
    classrooms.append((rid, name, capacity))

with open('format2_AllClassrooms.csv', 'w', newline='', encoding='utf-8') as f:
    writer = csv.writer(f)
    writer.writerow(['RoomID', 'RoomName', 'Capacity'])
    writer.writerows(classrooms)
print(f"Created format2_AllClassrooms.csv with {len(classrooms)} classrooms")

# 4. Generate Attendance with varied enrollment counts
# Ensure each course has different number of students (30-120 range)
enrollments = []
course_student_counts = {}

for course_code, _, _ in courses:
    # Random number of students per course
    num_students = random.randint(MIN_ENROLLMENTS_PER_COURSE, MAX_ENROLLMENTS_PER_COURSE)
    # Randomly select students for this course
    selected_students = random.sample([s[0] for s in students], min(num_students, len(students)))
    course_student_counts[course_code] = len(selected_students)
    
    for student_id in selected_students:
        enrollments.append((student_id, course_code))

# Shuffle to avoid patterns
random.shuffle(enrollments)

with open('format2_AllAttendanceLists.csv', 'w', newline='', encoding='utf-8') as f:
    writer = csv.writer(f)
    writer.writerow(['StudentID', 'CourseCode'])
    writer.writerows(enrollments)

print(f"Created format2_AllAttendanceLists.csv with {len(enrollments)} enrollments")
print(f"\nStudent counts per course (sample):")
for code, count in list(course_student_counts.items())[:10]:
    print(f"  {code}: {count} students")
print(f"  ...")
print(f"\nMin students: {min(course_student_counts.values())}")
print(f"Max students: {max(course_student_counts.values())}")
print(f"Avg students: {sum(course_student_counts.values()) / len(course_student_counts):.1f}")
