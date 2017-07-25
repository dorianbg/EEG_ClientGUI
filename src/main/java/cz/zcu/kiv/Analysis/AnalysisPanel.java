package cz.zcu.kiv.Analysis;

import cz.zcu.kiv.Const;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static cz.zcu.kiv.Const.screenSizeHeight;
import static cz.zcu.kiv.Const.screenSizeWidth;

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
 * AnalysisPanel, 2017/07/11 19:58 Dorian Beganovic
 *
 **********************************************************************************************************************/
public class AnalysisPanel extends JPanel {
    private String infoFileName;
    private String eegFileName;
    private int guessedNumber;


    public AnalysisPanel(String infoFileName){
        this.infoFileName = infoFileName;
        this.eegFileName = "";
        this.guessedNumber = -100;
        initializePanel();
    }

    public AnalysisPanel(String eegFileName, int guessedNumber){
        this.infoFileName = "";
        this.eegFileName = eegFileName;
        this.guessedNumber = guessedNumber;
        initializePanel();
    }

    public void initializePanel(){
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.WEST;

        // 1st row
        JLabel label1 = new JLabel("File  ");
        String fileName;
        if(infoFileName.length()==0) {
            fileName = eegFileName;
        }
        else{
            infoFileName = infoFileName;
        }
        final JTextField textField1 = new JTextField(infoFileName);
        textField1.setEditable(false);
        textField1.setPreferredSize(new Dimension(400,30));
        final JPanel panel1 = new JPanel(new BorderLayout());
        panel1.add(label1,BorderLayout.WEST);
        panel1.add(textField1,BorderLayout.EAST);

        // 2nd row
        JLabel label2 = new JLabel("Feature extraction method  ");
        String[] choices = { "dwt-8","CHOICE 2"};
        final JComboBox cb = new JComboBox(choices);
        cb.setPreferredSize(new Dimension(400,30));
        JPanel panel2 = new JPanel(new BorderLayout());
        panel2.add(label2,BorderLayout.WEST);
        panel2.add(cb,BorderLayout.EAST);



        // 4th row - adapts to what the user chose in previous step
        final CardLayout cardLayout = new CardLayout();
        final JPanel panel4 = new JPanel(cardLayout);

        String [] queries1 = {"log_reg_demo_Dorian_22.3.2017.", "svm_demo_Dorian_22.3.2017."};
        JList list1 = new JList(queries1);
        list1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list1.setVisibleRowCount(5);
        JScrollPane scrollPane1 = new JScrollPane(list1);
        Dimension d1 = list1.getPreferredSize();
        d1.width = (int)(screenSizeWidth*1/3);
        d1.height = (int)(screenSizeHeight*1/6);
        scrollPane1.setPreferredSize(d1);
        JPanel panel11 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel11.add(scrollPane1);

        JPanel loadPanel = new JPanel();
        loadPanel.setLayout(new GridBagLayout());
        loadPanel.add(panel11,gbc);




        String [] queries2 = {"Support Vector Machine", "Logistic Regression", "Random Forest", "Gradient Boosted Trees", "Neural Networks"};
        JList list2 = new JList(queries2);
        list2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list2.setVisibleRowCount(5);
        JScrollPane scrollPane2 = new JScrollPane(list2);
        Dimension d2 = list2.getPreferredSize();
        d2.width = (int)(screenSizeWidth*1/6);
        d2.height = (int)(screenSizeHeight*1/6);
        scrollPane2.setPreferredSize(d2);
        JPanel panel21 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel21.add(scrollPane2);
        JButton configureButton = new JButton("Configure");
        configureButton.setPreferredSize(new Dimension((int)(screenSizeWidth*1/12),(int)(screenSizeHeight*1/24)));
        panel21.add(configureButton);

        // 5th row
        JLabel label5 = new JLabel("Save classifier ");
        final JCheckBox jCheckBox = new JCheckBox();
        if(Const.getUseLocalMode().equals("true")){
            jCheckBox.setSelected(true);
        }
        else {
            jCheckBox.setSelected(false);
        }
        jCheckBox.setPreferredSize(new Dimension(400,30));
        JPanel panel5 = new JPanel(new BorderLayout());
        panel5.add(label5,BorderLayout.WEST);
        panel5.add(jCheckBox,BorderLayout.EAST);

        // 6th row
        JLabel label6 = new JLabel("Saved classifier name");
        JTextField jTextField6 =  new JTextField(" ");
        jTextField6.setPreferredSize(new Dimension((int)screenSizeWidth/5,(int)screenSizeHeight/20));
        JPanel panel6 = new JPanel(new BorderLayout());
        panel6.add(label6,BorderLayout.WEST);
        panel6.add(jTextField6,BorderLayout.EAST);



        JPanel trainPanel = new JPanel();
        trainPanel.setLayout(new GridBagLayout());
        trainPanel.add(panel21,gbc);
        trainPanel.add(panel5,gbc);
        trainPanel.add(panel6,gbc);



        panel4.add(loadPanel,"LOAD");
        panel4.add(trainPanel,"TRAIN");

        // 3rd row
        ButtonGroup buttonGroup = new ButtonGroup();

        JRadioButton button1 = new JRadioButton("Load a classifier", true);
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CardLayout cardLayout = (CardLayout)(panel4.getLayout());
                cardLayout.show(panel4, "LOAD");
            }
        });

        final JRadioButton button2 = new JRadioButton("Train a classifier", false);
        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CardLayout cardLayout = (CardLayout)(panel4.getLayout());
                cardLayout.show(panel4, "TRAIN");
            }
        });

        buttonGroup.add(button1);
        buttonGroup.add(button2);
        JPanel panel3 = new JPanel(new GridBagLayout());
        panel3.add(button1);
        panel3.add(button2);





        // 7th row
        JButton jButton7 = new JButton("GO");
        jButton7.setPreferredSize(new Dimension((int)screenSizeWidth/10,(int)screenSizeHeight/20));
        jButton7.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                System.out.println("saving");
            }
        });
        JPanel panel7 = new JPanel(new GridBagLayout());
        panel7.add(jButton7);



        // final settings
        setLayout(new GridBagLayout());

        add(panel1,gbc);
        add(panel2,gbc);
        add(panel3,gbc);
        add(panel4,gbc);
        add(panel7,gbc);
        /*
        add(topPanel);
        add(middlePanel);
        add(bottomPanel);
        */
    }
}
