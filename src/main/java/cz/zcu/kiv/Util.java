package cz.zcu.kiv;

import javax.swing.*;

/***********************************************************************************************************************
 *
 * This file is part of the Spark_EEG_Analysis project

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
 * cz.zcu.kiv.Util, 2017/06/28 16:03 Dorian Beganovic
 *
 **********************************************************************************************************************/
public class Util {

    private static JFrame mainScreen = null;

    private Util(){
        // Exists only to defeat instantiation.
    }

    public static JFrame getMainScreen() {
        if (mainScreen == null) {
            mainScreen = new JFrame();
            mainScreen.add(new IntroJPanel());
            mainScreen.setSize(600,700);
            mainScreen.setResizable(true);
            mainScreen.setLocationByPlatform(true);
            mainScreen.setLocationRelativeTo(null);
            mainScreen.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            mainScreen.setVisible(true);
        }
        return mainScreen;
    }


}
