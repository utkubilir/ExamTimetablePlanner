import java.time.LocalDate;
import java.util.*;

public class ScheduleState {

    private final List<Exam> examsList;
    private final Map<String, Map<LocalDate, Integer>> studentDailyCounts;
    private final Map<String, Map<LocalDate, List<Exam>>> studentDailyExams;
    private final Map<String, List<Student>> courseStudentsMap;

    public ScheduleState(Map<String, List<Student>> courseStudentsMap) {
        this.examsList = new ArrayList<>();
        this.studentDailyCounts = new HashMap<>();
        this.studentDailyExams = new HashMap<>();
        this.courseStudentsMap = courseStudentsMap;
    }
    
    public List<Exam> getExams() { return examsList; }

    public int getExamsCountForStudentDate(String studentId, LocalDate date) {
        if (!studentDailyCounts.containsKey(studentId)) return 0;
        return studentDailyCounts.get(studentId).getOrDefault(date, 0);
    }

    public List<Exam> getExamsForStudentDate(String studentId, LocalDate date) {
        if (!studentDailyExams.containsKey(studentId)) return Collections.emptyList();
        return studentDailyExams.get(studentId).getOrDefault(date, Collections.emptyList());
    }

    public List<Student> getStudentsForCourse(String courseCode) {
        return courseStudentsMap.getOrDefault(courseCode, Collections.emptyList());
    }
}