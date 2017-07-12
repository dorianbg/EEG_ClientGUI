package cz.zcu.kiv.Deprecated;

import cz.zcu.kiv.Const;
import cz.zcu.kiv.DataUploading.ScreenAllUsers;
import cz.zcu.kiv.JFrameSingleton;
import cz.zcu.kiv.SettingsPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
 * cz.zcu.kiv.Deprecated.IntroJPanel, 2017/06/27 23:46 Dorian Beganovic
 *
 **********************************************************************************************************************/
public class IntroJPanel extends JPanel{

    public IntroJPanel(){

        JButton uploadButton = new JButton("Upload files");
        uploadButton.setPreferredSize(new Dimension(180,60));
        uploadButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                JFrameSingleton.getMainScreen().setContentPane(new ScreenAllUsers());
                JFrameSingleton.getMainScreen().invalidate();
                JFrameSingleton.getMainScreen().validate();
            }
        });

        JButton analyzeButton = new JButton("Analyze data");
        analyzeButton.setPreferredSize(new Dimension(180,60));
        analyzeButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {

            }
        });

        JButton settingsButton = new JButton("Settings");
        settingsButton.setPreferredSize(new Dimension(180,60));
        settingsButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                JFrameSingleton.getMainScreen().setContentPane(new SettingsPanel("",""));
                JFrameSingleton.getMainScreen().invalidate();
                JFrameSingleton.getMainScreen().validate();

            }
        });


        this.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.WEST;

        this.add(uploadButton,gbc);
        this.add(analyzeButton,gbc);
        this.add(settingsButton,gbc);

        // ranom import to initialize the Const class
        String hadoopSeparator = Const.hadoopSeparator;

    }

}
