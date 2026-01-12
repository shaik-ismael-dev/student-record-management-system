package com.studentManagement;


import java.io.*;
import java.util.*;

public class StudentRecord {

    // ================= STUDENT MODEL =================
    static class Student implements Serializable {
        private static final long serialVersionUID = 1L;

        private int id;
        private String name;
        private int age;
        private String course;

        Student(int id, String name, int age, String course) {
            this.id = id;
            this.name = name;
            this.age = age;
            this.course = course;
        }

        int getId() { return id; }
        String getName() { return name; }
        int getAge() { return age; }
        String getCourse() { return course; }

        void setName(String name) { this.name = name; }
        void setAge(int age) { this.age = age; }
        void setCourse(String course) { this.course = course; }

        void display() {
            System.out.println(id + "\t" + name + "\t" + age + "\t" + course);
        }
    }

    // ================= GLOBAL DATA =================
    static ArrayList<Student> students = new ArrayList<>();
    static Stack<ArrayList<Student>> undoStack = new Stack<>();
    static Stack<ArrayList<Student>> redoStack = new Stack<>();

    static Scanner sc = new Scanner(System.in);
    static int idCounter = 1000;
    static final String FILE_NAME = "students.dat";

    // ================= MAIN =================
    public static void main(String[] args) {
        loadFromFile();
        login();
    }

    // ================= LOGIN =================
    static void login() {
        System.out.println("===== LOGIN =====");
        System.out.print("Username: ");
        String user = sc.nextLine();
        System.out.print("Password: ");
        String pass = sc.nextLine();

        if (user.equals("admin") && pass.equals("admin123")) {
            adminMenu();
        } else {
            userMenu();
        }
    }

    // ================= ADMIN MENU =================
    static void adminMenu() {
        int choice;
        do {
            System.out.println("\n===== ADMIN MENU =====");
            System.out.println("1. Add Student");
            System.out.println("2. View Students");
            System.out.println("3. Search by ID");
            System.out.println("4. Update Student");
            System.out.println("5. Delete Student");
            System.out.println("6. Undo");
            System.out.println("7. Redo");
            System.out.println("8. Save & Exit");
            System.out.print("Choice: ");

            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1 -> addStudent();
                case 2 -> viewStudents();
                case 3 -> searchStudent();
                case 4 -> updateStudent();
                case 5 -> deleteStudent();
                case 6 -> undo();
                case 7 -> redo();
                case 8 -> System.out.println("Exiting...");
                default -> System.out.println("Invalid choice");
            }
        } while (choice != 8);
    }

    // ================= USER MENU =================
    static void userMenu() {
        int choice;
        do {
            System.out.println("\n===== USER MENU =====");
            System.out.println("1. View Students");
            System.out.println("2. Search by ID");
            System.out.println("3. Exit");
            System.out.print("Choice: ");

            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1 -> viewStudents();
                case 2 -> searchStudent();
                case 3 -> System.out.println("Goodbye!");
                default -> System.out.println("Invalid choice");
            }
        } while (choice != 3);
    }

    // ================= CORE OPERATIONS =================
    static void addStudent() {
        saveState();

        int id = ++idCounter;
        System.out.print("Name: ");
        String name = sc.nextLine();
        System.out.print("Age: ");
        int age = sc.nextInt();
        sc.nextLine();
        System.out.print("Course: ");
        String course = sc.nextLine();

        students.add(new Student(id, name, age, course));
        saveToFile();
        System.out.println("Student added with ID: " + id);
    }

    static void viewStudents() {
        if (students.isEmpty()) {
            System.out.println("No records found.");
            return;
        }
        System.out.println("\nID\tName\tAge\tCourse");
        students.forEach(Student::display);
    }

    static void searchStudent() {
        System.out.print("Enter ID: ");
        int id = sc.nextInt();

        for (Student s : students) {
            if (s.getId() == id) {
                System.out.println("\nID\tName\tAge\tCourse");
                s.display();
                return;
            }
        }
        System.out.println("Student not found.");
    }

    static void updateStudent() {
        System.out.print("Enter ID: ");
        int id = sc.nextInt();
        sc.nextLine();

        for (Student s : students) {
            if (s.getId() == id) {
                saveState();

                System.out.print("New Name: ");
                s.setName(sc.nextLine());
                System.out.print("New Age: ");
                s.setAge(sc.nextInt());
                sc.nextLine();
                System.out.print("New Course: ");
                s.setCourse(sc.nextLine());

                saveToFile();
                System.out.println("Updated successfully.");
                return;
            }
        }
        System.out.println("Student not found.");
    }

    static void deleteStudent() {
        System.out.print("Enter ID: ");
        int id = sc.nextInt();

        for (Student s : students) {
            if (s.getId() == id) {
                saveState();
                students.remove(s);
                saveToFile();
                System.out.println("Student deleted successfully.");
                return;
            }
        }
        System.out.println("Student not found.");
    }

    // ================= UNDO / REDO =================
    static void saveState() {
        undoStack.push(deepCopy(students));
        redoStack.clear();
    }

    static void undo() {
        if (undoStack.isEmpty()) {
            System.out.println("Nothing to undo.");
            return;
        }
        redoStack.push(deepCopy(students));
        students = undoStack.pop();
        saveToFile();
        System.out.println("Undo successful.");
    }

    static void redo() {
        if (redoStack.isEmpty()) {
            System.out.println("Nothing to redo.");
            return;
        }
        undoStack.push(deepCopy(students));
        students = redoStack.pop();
        saveToFile();
        System.out.println("Redo successful.");
    }

    // ================= FILE HANDLING =================
    static void saveToFile() {
        try (ObjectOutputStream out =
                     new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            out.writeObject(students);
        } catch (Exception e) {
            System.out.println("Error saving file.");
        }
    }

    @SuppressWarnings("unchecked")
	static void loadFromFile() {
        try (ObjectInputStream in =
                     new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            students = (ArrayList<Student>) in.readObject();
            idCounter = students.stream()
                                .mapToInt(Student::getId)
                                .max()
                                .orElse(1000);
        } catch (Exception e) {
            students = new ArrayList<>();
        }
    }

    // ================= DEEP COPY =================
    static ArrayList<Student> deepCopy(ArrayList<Student> list) {
        ArrayList<Student> copy = new ArrayList<>();
        for (Student s : list) {
            copy.add(new Student(
                    s.getId(),
                    s.getName(),
                    s.getAge(),
                    s.getCourse()
            ));
        }
        return copy;
    }
}
