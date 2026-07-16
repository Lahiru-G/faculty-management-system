package com.faculty.view.dashboard;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.table.DefaultTableModel;

import com.faculty.model.User;
import com.faculty.model.Course;
import com.faculty.model.Student;
import com.faculty.controller.CourseController;
import model.dao.StudentDAO;
import com.faculty.view.components.StyledTable;
import com.faculty.view.components.RoundedButton;
import com.faculty.view.components.RoundedComboBox;

public class StudentCoursesPanel extends JPanel {
    
    private StyledTable table;
    private DefaultTableModel tableModel;
    private CourseController controller;
    private User currentUser;
    private int studentId = -1;
    private int studentDegreeId = 0;
    private RoundedComboBox<String> cmbYear;
    private RoundedComboBox<String> cmbSem;

    public StudentCoursesPanel(User currentUser) {
        this.currentUser = currentUser;
        this.controller = new CourseController();
        
        StudentDAO sdao = new StudentDAO();
        Student student = sdao.getStudentByUserId(currentUser.getId());
        if (student != null) {
            this.studentId = student.getId();
            this.studentDegreeId = student.getDegreeId();
        }

        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(245, 247, 250)); 
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Top Banner
        com.faculty.view.components.GradientPanel banner = new com.faculty.view.components.GradientPanel(new Color(20, 61, 89), new Color(20, 61, 89), false, 20);
        banner.setLayout(new BorderLayout());
        banner.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        JLabel title = new JLabel("Enrolled Courses");
        title.setFont(new Font("Montserrat", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        
        JLabel subtitle = new JLabel("View your registered courses and current academic progress.");
        subtitle.setFont(new Font("Montserrat", Font.PLAIN, 14));
        subtitle.setForeground(new Color(220, 235, 255));
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.add(title);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        titlePanel.add(subtitle);
        
        banner.add(titlePanel, BorderLayout.WEST);
        
        JPanel bannerWrapper = new JPanel(new BorderLayout());
        bannerWrapper.setOpaque(false);
        bannerWrapper.add(banner, BorderLayout.CENTER);
        
        // Registration Controls
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        controlPanel.setOpaque(false);
        
        cmbYear = new RoundedComboBox<>(20);
        cmbYear.addItem("Year 1"); cmbYear.addItem("Year 2"); cmbYear.addItem("Year 3"); cmbYear.addItem("Year 4");
        cmbYear.setPreferredSize(new Dimension(100, 35));

        cmbSem = new RoundedComboBox<>(20);
        cmbSem.addItem("Semester 1"); cmbSem.addItem("Semester 2");
        cmbSem.setPreferredSize(new Dimension(120, 35));

        RoundedButton btnRegister = new RoundedButton("Register For Courses", 20, new Color(20, 61, 89), Color.WHITE);
        btnRegister.setPreferredSize(new Dimension(180, 40));
        btnRegister.setFont(new Font("Montserrat", Font.BOLD, 13));

        controlPanel.add(cmbYear);
        controlPanel.add(cmbSem);
        controlPanel.add(btnRegister);

        bannerWrapper.add(controlPanel, BorderLayout.SOUTH);
        
        add(bannerWrapper, BorderLayout.NORTH);

        String[] columns = {"Course code", "Course name", "Credits", "Grade"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new StyledTable();
        table.setModel(tableModel);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        com.faculty.view.components.CardPanel tableContainer = new com.faculty.view.components.CardPanel(20, Color.WHITE);
        tableContainer.setLayout(new BorderLayout());
        tableContainer.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        tableContainer.add(scrollPane, BorderLayout.CENTER);
        
        JPanel mainContent = new JPanel(new BorderLayout(0, 10));
        mainContent.setOpaque(false);
        mainContent.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        mainContent.add(tableContainer, BorderLayout.CENTER);
        
        add(mainContent, BorderLayout.CENTER);

        cmbYear.addActionListener(e -> loadData());
        cmbSem.addActionListener(e -> loadData());

        loadData();

        btnRegister.addActionListener(e -> {
            if (studentId == -1) {
                JOptionPane.showMessageDialog(this, "Complete your profile before registering for courses.", "Profile Incomplete", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int year = cmbYear.getSelectedIndex() + 1;
            int sem = cmbSem.getSelectedIndex() + 1;
            showRegistrationDialog(year, sem);
        });
    }

    private void loadData() {
        tableModel.setRowCount(0);
        if (cmbYear == null || cmbSem == null) return;
        
        int selectedYear = cmbYear.getSelectedIndex() + 1;
        int selectedSem = cmbSem.getSelectedIndex() + 1;
        
        List<Course> courses = controller.getCoursesByStudentUserId(currentUser.getId());
        for (Course c : courses) {
            if (c.getAcademicYear() == selectedYear && c.getSemester() == selectedSem) {
                tableModel.addRow(new Object[]{
                    c.getCourseCode(),
                    c.getCourseName(),
                    c.getCredits(),
                    c.getGrade() != null ? c.getGrade() : "Pending"
                });
            }
        }
    }

    private void showRegistrationDialog(int year, int sem) {
        List<Course> available;
        if (studentDegreeId > 0) {
            available = controller.getCoursesByDegreeYearAndSemester(studentDegreeId, year, sem);
        } else {
            available = controller.getCoursesByYearAndSemester(year, sem);
        }
        List<Course> enrolled = controller.getCoursesByStudentUserId(currentUser.getId());
        List<String> enrolledCodes = enrolled.stream().map(Course::getCourseCode).collect(Collectors.toList());

        available.removeIf(c -> enrolledCodes.contains(c.getCourseCode()));

        if (available.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No new courses available to register for Year " + year + " Semester " + sem + ".", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] courseNames = new String[available.size()];
        for (int i = 0; i < available.size(); i++) {
            courseNames[i] = available.get(i).getCourseCode() + " - " + available.get(i).getCourseName();
        }

        RoundedComboBox<String> cmbAvailable = new RoundedComboBox<>(20);
        cmbAvailable.setPreferredSize(new Dimension(350, 40));
        for (String c : courseNames) cmbAvailable.addItem(c);

        JComponent[] fields = {cmbAvailable};
        String[] labels = {"Select Course:"};

        com.faculty.view.components.ModernDialog dialog = new com.faculty.view.components.ModernDialog(
            SwingUtilities.getWindowAncestor(this), 
            "Register for Course", 
            labels, fields
        );
        dialog.setVisible(true);

        if (dialog.isOk()) {
            int selectedIndex = cmbAvailable.getSelectedIndex();
            if (selectedIndex >= 0) {
                try {
                    controller.enrollStudent(studentId, available.get(selectedIndex).getId());
                    JOptionPane.showMessageDialog(this, "Successfully enrolled in " + available.get(selectedIndex).getCourseCode() + "!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error enrolling in course.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}