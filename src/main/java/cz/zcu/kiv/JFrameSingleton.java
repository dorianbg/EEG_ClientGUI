package cz.zcu.kiv;

import cz.zcu.kiv.DataUploading.GenScreen;
import cz.zcu.kiv.DataUploading.ScreenAllUsers;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;

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
 * cz.zcu.kiv.JFrameSingleton, 2017/06/28 16:03 Dorian Beganovic
 *
 **********************************************************************************************************************/
public class JFrameSingleton {

    private static Log logger = LogFactory.getLog(JFrameSingleton.class);

    private static JFrame mainScreen = null;

    private JFrameSingleton(){
        // Exists only to defeat instantiation.
    }

    public static JFrame getMainScreen() {
        if (mainScreen == null) {
            mainScreen = new JFrame();
            mainScreen.addWindowListener(new WindowListener() {
                @Override
                public void windowOpened(WindowEvent e) {
                    //new HadoopController().cacheHadoopFiles(Const.homeDirectory);

                }

                @Override
                // TODO - the cached state of hdfs should be also saved here
                public void windowClosing(WindowEvent e) {
                    try {
                        logger.info("FileSystem Closed");
                        Const.getHadoopFileSystem().close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    };

                }

                @Override
                public void windowClosed(WindowEvent e) {

                }

                @Override
                public void windowIconified(WindowEvent e) {

                }

                @Override
                public void windowDeiconified(WindowEvent e) {

                }

                @Override
                public void windowActivated(WindowEvent e) {

                }

                @Override
                public void windowDeactivated(WindowEvent e) {

                }
            });

            String cleanHomeDirectory;
            if(!Const.homeDirectory.endsWith("/")){
                cleanHomeDirectory = Const.homeDirectory + "/";
            }
            else {
                cleanHomeDirectory = Const.homeDirectory;
            }
            logger.info("Starting the screen");
            mainScreen.add(new GenScreen(getMainScreen(), cleanHomeDirectory));
            mainScreen.setSize(800, 700);
            mainScreen.setResizable(true);
            mainScreen.setLocationByPlatform(true);
            mainScreen.setLocationRelativeTo(null);
            mainScreen.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            mainScreen.setVisible(true);
            return null;
        }
        return mainScreen;
    }


}
