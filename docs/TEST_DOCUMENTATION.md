# Test Yapısı Dokümantasyonu

Bu belge, Exam Planner projesinin test altyapısını detaylı olarak açıklamaktadır.

---

## 📁 Dizin Yapısı

```
src/test/java/com/examplanner/
├── domain/                          # Domain sınıflarının testleri
│   ├── CourseTest.java             # Course sınıfı testleri (12 test)
│   ├── StudentTest.java            # Student sınıfı testleri (6 test)
│   ├── ClassroomTest.java          # Classroom sınıfı testleri (6 test)
│   ├── ExamTest.java               # Exam sınıfı testleri (9 test)
│   ├── ExamSlotTest.java           # ExamSlot sınıfı testleri (14 test)
│   └── ExamTimetableTest.java      # ExamTimetable sınıfı testleri (9 test)
└── services/                        # Service sınıflarının testleri
    ├── ConstraintCheckerTest.java  # Kısıt kontrolü testleri (13 test)
    ├── SchedulerServiceTest.java   # Zamanlayıcı testleri (11 test)
    └── DataImportServiceTest.java  # CSV import testleri (22 test)
```

**Toplam: 102 test**

---

## 🔧 Kullanılan Teknolojiler

### Maven Bağımlılıkları (pom.xml)

```xml
<!-- JUnit 5 -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.0</version>
    <scope>test</scope>
</dependency>

<!-- Mockito -->
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>5.7.0</version>
    <scope>test</scope>
</dependency>

<!-- Mockito JUnit 5 Extension -->
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <version>5.7.0</version>
    <scope>test</scope>
</dependency>
```

### Maven Surefire Plugin

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.2.2</version>
    <configuration>
        <includes>
            <include>**/*Test.java</include>
            <include>**/*Tests.java</include>
        </includes>
    </configuration>
</plugin>
```

---

## 🧪 JUnit 5 Annotasyonları

| Annotasyon | Açıklama | Örnek |
|------------|----------|-------|
| `@Test` | Metodun test olduğunu belirtir | `@Test void testMethod() {}` |
| `@DisplayName` | Test için okunabilir isim | `@DisplayName("Should create course")` |
| `@BeforeEach` | Her testten önce çalışır | Setup işlemleri için |
| `@Nested` | İç içe test sınıfları | Testleri gruplamak için |
| `@Timeout` | Timeout limiti belirler | `@Timeout(value=10, unit=SECONDS)` |
| `@TempDir` | Geçici dizin oluşturur | Dosya testleri için |

---

## 📊 Test Sınıfları Detayı

### 1. Domain Testleri

#### CourseTest.java
```java
@Nested
@DisplayName("Constructor Tests")
class ConstructorTests {
    
    @Test
    @DisplayName("Should create course with valid parameters")
    void shouldCreateCourseWithValidParameters() {
        Course course = new Course("CS101", "Programming", 120);
        assertEquals("CS101", course.getCode());
        assertEquals("Programming", course.getName());
        assertEquals(120, course.getExamDurationMinutes());
    }
    
    @Test
    @DisplayName("Should throw exception for null code")
    void shouldThrowExceptionForNullCode() {
        assertThrows(IllegalArgumentException.class,
            () -> new Course(null, "Test", 60));
    }
}
```

**Test edilen durumlar:**
- ✅ Geçerli parametrelerle oluşturma
- ✅ Null code kontrolü
- ✅ Boş code kontrolü
- ✅ Null name kontrolü
- ✅ Boş name kontrolü
- ✅ Sıfır süre kontrolü
- ✅ Negatif süre kontrolü
- ✅ Whitespace trim işlemi
- ✅ toString() metodu

---

#### ExamSlotTest.java
```java
@Nested
@DisplayName("Overlap Tests")
class OverlapTests {
    
    @Test
    @DisplayName("Should detect overlapping slots on same day")
    void shouldDetectOverlappingSlotsOnSameDay() {
        ExamSlot slot1 = new ExamSlot(date, LocalTime.of(9, 0), LocalTime.of(11, 0));
        ExamSlot slot2 = new ExamSlot(date, LocalTime.of(10, 0), LocalTime.of(12, 0));
        
        assertTrue(slot1.overlaps(slot2));
        assertTrue(slot2.overlaps(slot1));
    }
    
    @Test
    @DisplayName("Should not detect overlap for adjacent slots")
    void shouldNotDetectOverlapForAdjacentSlots() {
        ExamSlot slot1 = new ExamSlot(date, LocalTime.of(9, 0), LocalTime.of(11, 0));
        ExamSlot slot2 = new ExamSlot(date, LocalTime.of(11, 0), LocalTime.of(13, 0));
        
        assertFalse(slot1.overlaps(slot2)); // Bitişik slotlar çakışmaz
    }
}
```

**Test edilen durumlar:**
- ✅ Çakışan slotların tespiti
- ✅ Bitişik slotların çakışmadığı
- ✅ Farklı günlerde çakışma olmadığı
- ✅ İç içe slotların tespiti
- ✅ Null parametrelerin reddi
- ✅ Bitiş < Başlangıç durumunun reddi

---

### 2. Service Testleri

#### ConstraintCheckerTest.java

```java
@Nested
@DisplayName("Time Window Tests")
class TimeWindowTests {
    
    @Test
    @DisplayName("Should accept exam within working hours")
    void shouldAcceptExamWithinWorkingHours() {
        ExamSlot slot = new ExamSlot(date, LocalTime.of(9, 0), LocalTime.of(11, 0));
        assertTrue(constraintChecker.isWithinTimeWindow(slot));
    }
    
    @Test
    @DisplayName("Should reject exam starting before 9:00")
    void shouldRejectExamStartingBefore9() {
        ExamSlot slot = new ExamSlot(date, LocalTime.of(8, 0), LocalTime.of(10, 0));
        assertFalse(constraintChecker.isWithinTimeWindow(slot));
    }
}

@Nested
@DisplayName("Capacity Tests")
class CapacityTests {
    
    @Test
    @DisplayName("Should accept when classroom has enough capacity")
    void shouldAcceptWhenCapacityIsSufficient() {
        // 50 öğrenci, 100 kapasiteli sınıf
        assertTrue(constraintChecker.fitsCapacity(classroom, course, courseStudentsMap));
    }
}
```

**Test edilen kısıtlar:**
| Kısıt | Açıklama |
|-------|----------|
| Time Window | 09:00 - 18:30 arası çalışma saatleri |
| Capacity | Derslik kapasitesinin öğrenci sayısına yetmesi |
| Availability | Dersliğin aynı anda başka sınava ayrılmamış olması |
| Gap | Öğrenciler arası minimum 3 saat boşluk |
| Daily Limit | Öğrenci başına günde max 2 sınav |

---

#### SchedulerServiceTest.java

```java
@Nested
@DisplayName("Input Validation Tests")
class InputValidationTests {
    
    @Test
    @DisplayName("Should throw exception for null courses")
    void shouldThrowExceptionForNullCourses() {
        assertThrows(IllegalArgumentException.class,
            () -> schedulerService.generateTimetable(null, classrooms, enrollments, startDate));
    }
}

@Nested
@DisplayName("Simple Scheduling Tests")
class SimpleSchedulingTests {
    
    @Test
    @DisplayName("Should schedule single course successfully")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void shouldScheduleSingleCourse() {
        ExamTimetable result = schedulerService.generateTimetable(
            courses, classrooms, enrollments, startDate);
        
        assertNotNull(result);
        assertEquals(1, result.getExams().size());
    }
}
```

**Test edilen senaryolar:**
- ✅ Null/empty input kontrolü
- ✅ Tek ders zamanlaması
- ✅ Çoklu ders zamanlaması
- ✅ Çalışma saatlerine uygunluk
- ✅ Kapasite kısıtına uygunluk

---

#### DataImportServiceTest.java

```java
@TempDir
Path tempDir;  // JUnit otomatik geçici dizin oluşturur

@Test
@DisplayName("Should load courses from CSV with header")
void shouldLoadCoursesFromCsvWithHeader() throws IOException {
    File coursesFile = createTempFile("courses.csv",
        "CourseCode,CourseName,DurationMinutes\n" +
        "CS101,Introduction to Programming,120\n" +
        "CS102,Data Structures,90\n"
    );
    
    List<Course> courses = dataImportService.loadCourses(coursesFile);
    
    assertEquals(2, courses.size());
    assertEquals("CS101", courses.get(0).getCode());
}

@Test
@DisplayName("Should reject wrong file type for courses")
void shouldRejectWrongFileTypeForCourses() throws IOException {
    File classroomsFile = createTempFile("classrooms.csv",
        "RoomID,RoomName,Capacity\n" +
        "A101,Hall A,150\n"
    );
    
    assertThrows(IllegalArgumentException.class,
        () -> dataImportService.loadCourses(classroomsFile));
}
```

**Test edilen formatlar:**
| Format | Örnek |
|--------|-------|
| Header CSV | `CourseCode,CourseName,DurationMinutes` |
| Semicolon | `RoomA;100` |
| Bracket List | `['S001', 'S002', 'S003']` |
| Single Column | Sadece code listesi |

---

## 🔍 Assertion Metodları

```java
// Eşitlik kontrolü
assertEquals(expected, actual);
assertEquals(expected, actual, "Hata mesajı");

// Boolean kontroller
assertTrue(condition);
assertFalse(condition);

// Null kontroller
assertNotNull(object);
assertNull(object);

// Exception kontrolü
assertThrows(IllegalArgumentException.class, () -> {
    new Course(null, "Test", 60);
});

// Exception fırlatmamasını kontrol
assertDoesNotThrow(() -> {
    constraintChecker.setMinGapMinutes(120);
});

// Collection kontrolleri
assertTrue(list.isEmpty());
assertEquals(5, list.size());
```

---

## ▶️ Test Çalıştırma Komutları

```bash
# Tüm testleri çalıştır
mvn test

# Belirli test sınıfını çalıştır
mvn test -Dtest=CourseTest

# Nested sınıfı çalıştır
mvn test -Dtest="ConstraintCheckerTest$TimeWindowTests"

# Belirli metodu çalıştır
mvn test -Dtest=CourseTest#shouldCreateCourseWithValidParameters

# Pattern ile çalıştır
mvn test -Dtest="*ServiceTest"

# Verbose output
mvn test -Dsurefire.useFile=false

# Test raporunu görüntüle
cat target/surefire-reports/*.txt
```

---

## 📈 Test Sonuç Raporu

```
[INFO] Running com.examplanner.domain.CourseTest
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0

[INFO] Running com.examplanner.services.SchedulerServiceTest
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0

...

[INFO] Results:
[INFO] 
[INFO] Tests run: 102, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] BUILD SUCCESS
```

---

## 📋 Test Özet Tablosu

| Sınıf | Test Sayısı | Kategori |
|-------|-------------|----------|
| CourseTest | 12 | Domain |
| StudentTest | 6 | Domain |
| ClassroomTest | 6 | Domain |
| ExamTest | 9 | Domain |
| ExamSlotTest | 14 | Domain |
| ExamTimetableTest | 9 | Domain |
| ConstraintCheckerTest | 13 | Service |
| SchedulerServiceTest | 11 | Service |
| DataImportServiceTest | 22 | Service |
| **TOPLAM** | **102** | |

---

## 🎯 En İyi Pratikler

1. **Anlamlı İsimler:** `shouldThrowExceptionForNullCode()` gibi kendini açıklayan isimler
2. **Nested Sınıflar:** İlgili testleri gruplamak için `@Nested` kullanımı
3. **DisplayName:** Raporda okunabilir test isimleri için `@DisplayName`
4. **Setup:** Tekrarlanan kod için `@BeforeEach` kullanımı
5. **Timeout:** Uzun sürebilecek testler için `@Timeout`
6. **TempDir:** Dosya testleri için `@TempDir` annotation'ı

---

*Son güncelleme: 16 Aralık 2024*
