package cz.zcu.kiv;

import cz.zcu.kiv.UploadScreens.UsersScreen;

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
 * cz.zcu.kiv.IntroJPanel, 2017/06/27 23:46 Dorian Beganovic
 *
 **********************************************************************************************************************/
public class IntroJPanel extends JPanel{

    public IntroJPanel(){
        JButton uploadButton = new JButton("Upload files");

        uploadButton.setPreferredSize(new Dimension(180,60));
        uploadButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                Util.getMainScreen().setContentPane(new UsersScreen());
                Util.getMainScreen().invalidate();
                Util.getMainScreen().validate();
            }
        });

        JButton analyzeButton = new JButton("Analyze data");
        analyzeButton.setPreferredSize(new Dimension(180,60));
        analyzeButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                System.out.println("2");
            }
        });


        setLayout(new GridBagLayout());

        this.add(uploadButton);
        this.add(analyzeButton);


    }

}
