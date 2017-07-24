package cz.zcu.kiv;

import cz.zcu.kiv.DataUploading.GenScreen;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;

import static cz.zcu.kiv.Const.screenSizeHeight;
import static cz.zcu.kiv.Const.screenSizeWidth;

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

    /**
     * creates the single main JFrame window, which when closed also closes the application
     * @return  singleton JFrame
     */
    public static JFrame getMainScreen() {
        if (mainScreen == null) {
            mainScreen = new JFrame();
            mainScreen.addWindowListener(new WindowListener() {
                @Override
                public void windowOpened(WindowEvent e) {
                    //new HadoopHdfsController().cacheHadoopFilesIntoSerialized(Const.homeDirectory);

                }

                @Override
                public void windowClosing(WindowEvent e) {
                    try {
                        Const.getHadoopFileSystem().close();
                        logger.info("FileSystem Closed");
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
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

            /*
            this sets the UI LookAndFeel manager to Nimbus manager, a much better version compared to the standard
             */
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception e) {
                // If Nimbus is not available, fall back to cross-platform
                try {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                } catch (Exception ex) {
                    // not worth my time
                }
            }

            /*
            screen size taken by the app will be:
            1/2 of screen width
            3/4 of screen screenSizeHeight
             */
            logger.info("Starting the screen");
            mainScreen.add(new GenScreen(getMainScreen(), Const.homeDirectory));
            mainScreen.setSize((int) screenSizeWidth /2, (int)(screenSizeHeight *3/4));
            mainScreen.setResizable(true);
            mainScreen.setLocationByPlatform(true);
            mainScreen.setLocationRelativeTo(null); // positions the window to middle of screen
            mainScreen.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            mainScreen.setVisible(true);
            /*
            here we set the ESCAPE button to close the JFrame
             */
            KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
            Action escapeAction = new AbstractAction() {
                // close the frame when the user presses escape
                public void actionPerformed(ActionEvent e) {
                    mainScreen.dispose();
                }
            };
            mainScreen.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
            mainScreen.getRootPane().getActionMap().put("ESCAPE", escapeAction);

            //
            return null;
        }
        // the singleton returns the screen we just defined
        return mainScreen;
    }

}
