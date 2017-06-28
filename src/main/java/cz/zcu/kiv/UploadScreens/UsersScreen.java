package cz.zcu.kiv.UploadScreens;

import cz.zcu.kiv.IntroJPanel;
import cz.zcu.kiv.Util;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/***********************************************************************************************************************
 *
 * This file is part of the EEG_WorkflowGUI project

 * ==========================================
 *
 * Copyright (C) 2017 by University of West Bohemia (http://www.zcu.cz/en/)
 *
 ***********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 ***********************************************************************************************************************
 *
 * UsersScreen, 2017/06/28 07:15 Dorian Beganovic
 *
 **********************************************************************************************************************/
public class UsersScreen extends JPanel implements ListSelectionListener {

    private JList list;
    private DefaultListModel listModel;

    private JButton fireButton;
    private JTextField employeeName;

    public UsersScreen(){
        super(new BorderLayout());

        Object[][] data = new String[10][10];

        Object[] columnNames = new String[]{"Filename","2","3","4","5"};

        JTable table = new JTable();
        //Create the list and put it in a scroll pane.
        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                //all cells false
                return false;
            }
        };

        table.setModel(tableModel);
        table.setRowHeight(30);

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(10000, 50));

        //table.setShowHorizontalLines(true);
        //table.setShowVerticalLines(true);
        table.setGridColor(Color.BLACK);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);


        JScrollPane listScrollPane = new JScrollPane(table);

        // back button
        JButton backButton = new JButton("BACK");
        backButton.setPreferredSize(new Dimension(120,40));
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Util.getMainScreen().setContentPane(new IntroJPanel());
                Util.getMainScreen().invalidate();
                Util.getMainScreen().validate();

            }
        });

        /*
        HireListener hireListener = new HireListener(backButton);
        backButton.setActionCommand(hireString);
        backButton.addActionListener(hireListener);
        */

        // next button
        JButton nextButton = new JButton("NEXT");
        nextButton.setPreferredSize(new Dimension(120,40));
        nextButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Util.getMainScreen().setContentPane(new ExperimentsScreen());
                Util.getMainScreen().invalidate();
                Util.getMainScreen().validate();

            }
        });


        /*
        HireListener hireListener = new HireListener(nextButton);
        nextButton.setActionCommand(hireString);
        nextButton.addActionListener(hireListener);
        */

        // ADD EXPERIMENT button
        JButton addButton = new JButton("ADD USER");
        addButton.setPreferredSize(new Dimension(160,40));
        addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        /*
        HireListener hireListener = new HireListener(addButton);
        addButton.setActionCommand(hireString);
        addButton.addActionListener(hireListener);
         */

        //Create a panel that uses BoxLayout.
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new GridBagLayout());
        buttonPane.add(backButton);
        buttonPane.add(Box.createHorizontalStrut(5));
        buttonPane.add(new JSeparator(SwingConstants.VERTICAL));
        buttonPane.add(Box.createHorizontalStrut(5));
        buttonPane.add(addButton);
        buttonPane.add(Box.createHorizontalStrut(5));
        buttonPane.add(new JSeparator(SwingConstants.VERTICAL));
        buttonPane.add(Box.createHorizontalStrut(5));
        buttonPane.add(nextButton);
        buttonPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        add(listScrollPane, BorderLayout.CENTER);
        add(buttonPane, BorderLayout.PAGE_END);

    }

    public static void main(String[] args) {

    }

    public void valueChanged(ListSelectionEvent e) {

    }
}
