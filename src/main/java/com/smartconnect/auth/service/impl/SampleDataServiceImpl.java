package com.smartconnect.auth.service.impl;

import com.smartconnect.auth.model.entity.Admin;
import com.smartconnect.auth.model.entity.Student;
import com.smartconnect.auth.model.entity.Teacher;
import com.smartconnect.auth.model.entity.User;
import com.smartconnect.auth.model.enums.StudentStatus;
import com.smartconnect.auth.model.enums.UserRole;
import com.smartconnect.auth.repository.AdminRepository;
import com.smartconnect.auth.repository.StudentRepository;
import com.smartconnect.auth.repository.TeacherRepository;
import com.smartconnect.auth.repository.UserRepository;
import com.smartconnect.auth.service.SampleDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SampleDataServiceImpl implements SampleDataService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    private final Faker faker = new Faker(Locale.forLanguageTag("vi-VN"));

    private static final String DEFAULT_SEEDED_PASSWORD = "Admin@123";

    private static final String[] ADMIN_DEPARTMENTS = {
            "IT Department", "Academic Affairs", "Student Affairs",
            "Finance", "Human Resources", "Administration"
    };

    private static final String[] ADMIN_POSITIONS = {
            "System Administrator", "Academic Admin", "Student Services Manager",
            "Finance Manager", "HR Manager", "Operations Manager"
    };

    private static final String[] TEACHER_SPECIALIZATIONS = {
            "Artificial Intelligence", "Machine Learning", "Data Science",
            "Software Engineering", "Computer Networks", "Database Systems",
            "Applied Mathematics", "Statistics", "Physics", "Chemistry"
    };

    private static final String[] TEACHER_DEGREES = {
            "PhD", "Master", "Bachelor", "Tiến sĩ", "Thạc sĩ", "Cử nhân"
    };

    private static final String[] TEACHER_TITLES = {
            "Professor", "Associate Professor", "Assistant Professor",
            "Lecturer", "Giáo sư", "Phó Giáo sư", "Giảng viên"
    };

    @Override
    @Transactional
    public SampleDataSummary generateSampleData(int adminCount, int teacherCount, int studentCount) {
        log.info("Starting sample data generation: {} admins, {} teachers, {} students",
                adminCount, teacherCount, studentCount);

        List<User> generatedUsers = new ArrayList<>();
        List<Admin> generatedAdmins = new ArrayList<>();
        List<Teacher> generatedTeachers = new ArrayList<>();
        List<Student> generatedStudents = new ArrayList<>();

        for (int i = 0; i < adminCount; i++) {
            try {
                User user = createAdminUser(i + 1);
                generatedUsers.add(user);

                Admin admin = createAdmin(user, i + 1);
                generatedAdmins.add(admin);
            } catch (Exception e) {
                log.error("Error creating admin {}: {}", i + 1, e.getMessage());
            }
        }

        for (int i = 0; i < teacherCount; i++) {
            try {
                User user = createTeacherUser(i + 1);
                generatedUsers.add(user);

                Teacher teacher = createTeacher(user, i + 1);
                generatedTeachers.add(teacher);
            } catch (Exception e) {
                log.error("Error creating teacher {}: {}", i + 1, e.getMessage());
            }
        }

        for (int i = 0; i < studentCount; i++) {
            try {
                User user = createStudentUser(i + 1);
                generatedUsers.add(user);

                Student student = createStudent(user, i + 1);
                generatedStudents.add(student);
            } catch (Exception e) {
                log.error("Error creating student {}: {}", i + 1, e.getMessage());
            }
        }

        log.info("Sample data generation completed: {} users, {} admins, {} teachers, {} students",
                generatedUsers.size(), generatedAdmins.size(), generatedTeachers.size(), generatedStudents.size());

        return SampleDataSummary.builder()
                .usersGenerated(generatedUsers.size())
                .adminsGenerated(generatedAdmins.size())
                .teachersGenerated(generatedTeachers.size())
                .studentsGenerated(generatedStudents.size())
                .build();
    }

    private User createAdminUser(int index) {
        String username = generateUniqueUsername("admin", index);
        String email = generateUniqueEmail(username);

        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(DEFAULT_SEEDED_PASSWORD))
                .fullName(faker.name().fullName())
                .phone(faker.phoneNumber().cellPhone())
                .role(faker.random().nextInt(0, 1) == 0 ? UserRole.ADMIN : UserRole.SUPER_ADMIN)
                .isActive(true)
                .isEmailVerified(true)
                .failedLoginAttempts(0)
                .build();

        return userRepository.save(user);
    }

    private Admin createAdmin(User user, int index) {
        String adminCode = String.format("AD%04d", index);

        int counter = 0;
        while (adminRepository.existsByAdminCode(adminCode) && counter < 100) {
            adminCode = String.format("AD%04d", index + counter);
            counter++;
        }

        Admin admin = Admin.builder()
                .user(user)
                .adminCode(adminCode)
                .department(faker.options().option(ADMIN_DEPARTMENTS))
                .position(faker.options().option(ADMIN_POSITIONS))
                .accessLevel(user.getRole() == UserRole.SUPER_ADMIN ? 3 : faker.random().nextInt(1, 2))
                .canManageUsers(faker.bool().bool())
                .canManageCourses(faker.bool().bool())
                .canManageGrades(faker.bool().bool())
                .canViewReports(true)
                .canManageSystem(user.getRole() == UserRole.SUPER_ADMIN)
                .isActive(true)
                .hireDate(faker.date().past(365 * 5, TimeUnit.DAYS).toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate())
                .build();

        return adminRepository.save(admin);
    }

    private User createTeacherUser(int index) {
        String username = generateUniqueUsername("teacher", index);
        String email = generateUniqueEmail(username);

        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(DEFAULT_SEEDED_PASSWORD))
                .fullName(faker.name().fullName())
                .phone(faker.phoneNumber().cellPhone())
                .role(UserRole.TEACHER)
                .isActive(true)
                .isEmailVerified(true)
                .failedLoginAttempts(0)
                .build();

        return userRepository.save(user);
    }

    private Teacher createTeacher(User user, int index) {
        String teacherCode = String.format("TC%04d", index);

        int counter = 0;
        while (teacherRepository.existsByTeacherCode(teacherCode) && counter < 100) {
            teacherCode = String.format("TC%04d", index + counter);
            counter++;
        }

        LocalDate hireDate = faker.date().past(365 * 15, TimeUnit.DAYS).toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();

        Teacher teacher = Teacher.builder()
                .user(user)
                .teacherCode(teacherCode)
                .specialization(faker.options().option(TEACHER_SPECIALIZATIONS))
                .degree(faker.options().option(TEACHER_DEGREES))
                .title(faker.options().option(TEACHER_TITLES))
                .office(String.format("Building %s, Room %d",
                        faker.letterify("?", true), faker.random().nextInt(100, 500)))
                .officeHours(String.format("%s %02d:00-%02d:00",
                        faker.options().option("Mon-Fri", "Tue-Thu", "Mon-Wed-Fri"),
                        faker.random().nextInt(8, 12),
                        faker.random().nextInt(14, 18)))
                .bio(faker.lorem().paragraph())
                .researchInterests(faker.lorem().sentence())
                .publicationsCount(faker.random().nextInt(0, 50))
                .hireDate(hireDate)
                .isActive(true)
                .build();

        return teacherRepository.save(teacher);
    }

    private User createStudentUser(int index) {
        String username = generateUniqueUsername("student", index);
        String email = generateUniqueEmail(username);

        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(DEFAULT_SEEDED_PASSWORD))
                .fullName(faker.name().fullName())
                .phone(faker.phoneNumber().cellPhone())
                .role(UserRole.STUDENT)
                .isActive(true)
                .isEmailVerified(true)
                .failedLoginAttempts(0)
                .build();

        return userRepository.save(user);
    }

    private Student createStudent(User user, int index) {
        String studentCode = String.format("ST%04d", index);

        int counter = 0;
        while (studentRepository.existsByStudentCode(studentCode) && counter < 100) {
            studentCode = String.format("ST%04d", index + counter);
            counter++;
        }

        int currentYear = LocalDate.now().getYear();
        int admissionYear = faker.random().nextInt(currentYear - 4, currentYear);

        double gpa = faker.random().nextDouble(2.0, 4.0);
        BigDecimal gpaDecimal = BigDecimal.valueOf(gpa).setScale(2, RoundingMode.HALF_UP);

        StudentStatus[] statuses = StudentStatus.values();
        StudentStatus status = faker.options().option(statuses);

        LocalDate dateOfBirth = faker.date().birthday(18, 25).toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();

        Student student = Student.builder()
                .user(user)
                .studentCode(studentCode)
                .admissionYear(admissionYear)
                .gpa(gpaDecimal)
                .status(status)
                .dateOfBirth(dateOfBirth)
                .address(faker.address().fullAddress())
                .parentPhone(faker.phoneNumber().cellPhone())
                .emergencyContact(faker.name().fullName())
                .build();

        return studentRepository.save(student);
    }

    private String generateUniqueUsername(String prefix, int index) {
        String username = String.format("%s%d", prefix, index);
        int counter = 0;
        while (userRepository.existsByUsername(username) && counter < 100) {
            username = String.format("%s%d_%d", prefix, index, counter);
            counter++;
        }
        return username;
    }

    private String generateUniqueEmail(String username) {
        String email = String.format("%s@smartconnect.edu.vn", username);
        int counter = 0;
        while (userRepository.existsByEmail(email) && counter < 100) {
            email = String.format("%s%d@smartconnect.edu.vn", username, counter);
            counter++;
        }
        return email;
    }
}

