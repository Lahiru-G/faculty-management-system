package com.faculty.view.dashboard;

import javax.swing.*;
import java.awt.*;
import com.faculty.controller.StudentController;
import com.faculty.model.Student;
import com.faculty.model.User;
import com.faculty.view.components.RoundedButton;
import com.faculty.view.components.RoundedTextField;

public class StudentProfilePanel extends JPanel {
    private RoundedTextField txtFullName;
    private RoundedTextField txtStudentId;
    private com.faculty.view.components.RoundedComboBox<String> cmbDegree;
    private RoundedTextField txtEmail;
    private RoundedTextField txtMobile;
    private Student currentStudent;
    private boolean isEditing = false;
    
    public StudentProfilePanel(User currentUser) {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(245, 247, 250)); // Very light blue/grey
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        StudentController controller = new StudentController();
        currentStudent = controller.getStudentByUserId(currentUser.getId());

        // Top Banner
        com.faculty.view.components.GradientPanel banner = new com.faculty.view.components.GradientPanel(new Color(20, 61, 89), new Color(20, 61, 89), false, 20);
        banner.setLayout(new BorderLayout());
        banner.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        JLabel title = new JLabel("My Profile");
        title.setFont(new Font("Montserrat", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        
        JLabel subtitle = new JLabel("View and update your personal details and contact information.");
        subtitle.setFont(new Font("Montserrat", Font.PLAIN, 14));
        subtitle.setForeground(new Color(220, 235, 255));
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.add(title);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        titlePanel.add(subtitle);
        
        banner.add(titlePanel, BorderLayout.WEST);
        
        // Wrap banner in a panel to respect its preferred height
        JPanel bannerWrapper = new JPanel(new BorderLayout());
        bannerWrapper.setOpaque(false);
        bannerWrapper.add(banner, BorderLayout.CENTER);
        
        add(bannerWrapper, BorderLayout.NORTH);

        // Form panel inside a card
        com.faculty.view.components.CardPanel formContainer = new com.faculty.view.components.CardPanel(20, Color.WHITE);
        formContainer.setLayout(new BorderLayout());
        formContainer.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);
        
        txtFullName = addField(formPanel, gbc, "Full Name", 0, currentStudent != null ? currentStudent.getFullName() : "");
        txtStudentId = addField(formPanel, gbc, "Student ID", 1, currentStudent != null ? currentStudent.getStudentIdStr() : currentUser.getUsername());
        cmbDegree = addComboBoxField(formPanel, gbc, "Degree", 2, currentStudent != null ? (currentStudent.getDegreeName() != null ? currentStudent.getDegreeName() : "") : "");
        txtEmail = addField(formPanel, gbc, "Email", 3, currentStudent != null ? currentStudent.getEmail() : "");
        txtMobile = addField(formPanel, gbc, "Mobile Number", 4, currentStudent != null ? currentStudent.getMobileNumber() : "");
        
        setFieldsEditable(false);

        formContainer.add(formPanel, BorderLayout.CENTER);
        
        JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 30));
        centerWrapper.setOpaque(false);
        centerWrapper.add(formContainer);
        
        JScrollPane scrollPane = new JScrollPane(centerWrapper);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        bottomPanel.setOpaque(false);
        
        RoundedButton btnEdit = new RoundedButton("Edit", 20, new Color(230, 240, 250), new Color(0, 100, 200));
        btnEdit.setIcon(new com.faculty.view.components.ModernIcon(com.faculty.view.components.ModernIcon.IconType.EDIT, 16, new Color(0, 100, 200)));
        btnEdit.setIconTextGap(10);
        btnEdit.setPreferredSize(new Dimension(120, 45));

        RoundedButton btnSave = new RoundedButton("Save changes", 20, new Color(20, 61, 89), Color.WHITE);
        btnSave.setIcon(new com.faculty.view.components.ModernIcon(com.faculty.view.components.ModernIcon.IconType.SAVE, 18, Color.WHITE));
        btnSave.setIconTextGap(10);
        btnSave.setPreferredSize(new Dimension(200, 45));
        
        btnEdit.addActionListener(e -> {
            isEditing = !isEditing;
            setFieldsEditable(isEditing);
            btnEdit.setText(isEditing ? "Cancel" : "Edit");
            if (!isEditing && currentStudent != null) {
                // Revert changes on cancel
                txtFullName.setText(currentStudent.getFullName());
                txtEmail.setText(currentStudent.getEmail());
                txtMobile.setText(currentStudent.getMobileNumber());
                cmbDegree.setSelectedItem(currentStudent.getDegreeName() != null ? currentStudent.getDegreeName() : "");
            }
        });

        btnSave.addActionListener(e -> {
            try {
                String degreeInput = (String) cmbDegree.getSelectedItem();
                
                validateStudentData(txtStudentId.getText().trim(), txtMobile.getText().trim(), degreeInput, txtEmail.getText().trim());
                
                int degreeId = 0;
                if (degreeInput != null && !degreeInput.isEmpty()) {
                    com.faculty.controller.DegreeController dc = new com.faculty.controller.DegreeController();
                    for (com.faculty.model.Degree d : dc.getAllDegrees()) {
                        if (d.getName().equalsIgnoreCase(degreeInput)) {
                            degreeId = d.getId();
                            break;
                        }
                    }
                }

                if (currentStudent == null) {
                    currentStudent = new Student(0, currentUser.getId(), txtStudentId.getText(), txtFullName.getText(), degreeId, txtEmail.getText(), txtMobile.getText());
                    controller.addStudentProfile(currentStudent);
                } else {
                    currentStudent.setStudentIdStr(txtStudentId.getText().trim());
                    currentStudent.setFullName(txtFullName.getText());
                    currentStudent.setDegreeId(degreeId);
                    currentStudent.setEmail(txtEmail.getText());
                    currentStudent.setMobileNumber(txtMobile.getText());
                    controller.updateStudentProfile(currentStudent);
                }
                JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                isEditing = false;
                setFieldsEditable(false);
                btnEdit.setText("Edit");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error updating profile: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        bottomPanel.add(btnEdit);
        bottomPanel.add(btnSave);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void setFieldsEditable(boolean editable) {
        txtFullName.setEditable(editable);
        cmbDegree.setEnabled(editable);
        txtEmail.setEditable(editable);
        txtMobile.setEditable(editable);
        txtStudentId.setEditable(editable);
    }
    
    private RoundedTextField addField(JPanel formPanel, GridBagConstraints gbc, String label, int row, String value) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Montserrat", Font.BOLD, 15));
        lbl.setForeground(new Color(80, 80, 80));
        
        RoundedTextField txt = new RoundedTextField(20, 15);
        txt.setText(value);
        txt.setPreferredSize(new Dimension(300, 40));
        
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        formPanel.add(lbl, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 0.7;
        formPanel.add(txt, gbc);
        return txt;
    }
    
    private void validateStudentData(String studentId, String mobile, String degreeName, String email) throws Exception {
        if (!mobile.matches("^07\\d{8}$")) {
            throw new Exception("Mobile number must start with 07 and be exactly 10 digits.");
        }
        if (!email.contains("@") || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new Exception("Invalid email format! Email must contain an '@' symbol.");
        }
        String prefix = "";
        if (degreeName.equals("Information Technology")) prefix = "CT";
        else if (degreeName.equals("Engineering Technology")) prefix = "ET";
        else if (degreeName.equals("Computer Science")) prefix = "CS";
        else if (degreeName.equals("Bio Systems Technology")) prefix = "BT";
        else throw new Exception("Unknown degree selected.");

        if (!studentId.matches("^" + prefix + "/20\\d{2}/\\d{3}$")) {
            throw new Exception("Student ID must match format " + prefix + "/20xx/xxx for " + degreeName);
        }
    }

    private com.faculty.view.components.RoundedComboBox<String> addComboBoxField(JPanel formPanel, GridBagConstraints gbc, String label, int row, String selectedItem) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Montserrat", Font.BOLD, 15));
        lbl.setForeground(new Color(80, 80, 80));
        
        com.faculty.view.components.RoundedComboBox<String> cmb = new com.faculty.view.components.RoundedComboBox<>(20);
        cmb.setPreferredSize(new Dimension(300, 40));
        
        com.faculty.controller.DegreeController dc = new com.faculty.controller.DegreeController();
        for (com.faculty.model.Degree d : dc.getAllDegrees()) {
            cmb.addItem(d.getName());
        }
        if (selectedItem != null && !selectedItem.isEmpty()) {
            cmb.setSelectedItem(selectedItem);
        }
        
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        formPanel.add(lbl, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 0.7;
        formPanel.add(cmb, gbc);
        return cmb;
    }
}





