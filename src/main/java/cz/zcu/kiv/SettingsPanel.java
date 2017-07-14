package cz.zcu.kiv;

import cz.zcu.kiv.DataUploading.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Time;
import java.util.Set;
import java.util.concurrent.ExecutionException;

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
 * SettingsPanel, 2017/07/02 12:30 Dorian Beganovic
 *
 **********************************************************************************************************************/
public class SettingsPanel extends JPanel {

    private static Log logger = LogFactory.getLog(SettingsPanel.class);


    public SettingsPanel(String a, String b){

    }
    public SettingsPanel(final JFrame jFrameParent, final String source, final String path){


        JLabel label1 = new JLabel("Home directory  ");
        final JTextField textField1 = new JTextField(Const.homeDirectory);
        textField1.setPreferredSize(new Dimension(400,30));

        final JLabel label2 = new JLabel("Remote URI        ");
        final JTextField textField2 = new JTextField(Const.remoteUriPrefix);
        textField2.setPreferredSize(new Dimension(400,30));

        JLabel label3 = new JLabel("Local URI           ");
        final JTextField textField3 = new JTextField(Const.localUriPrefix);
        textField3.setPreferredSize(new Dimension(400,30));

        JLabel label4 = new JLabel("Use local mode ");
        final JCheckBox jCheckBox = new JCheckBox();
        if(Const.getUseLocalMode().equals("true")){
            jCheckBox.setSelected(true);
        }
        else {
            jCheckBox.setSelected(false);
        }
        jCheckBox.setPreferredSize(new Dimension(400,30));

        JButton updateCache = new JButton("UPDATE CACHE");
        updateCache.setPreferredSize(new Dimension(400,60));
        updateCache.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {

                SwingWorker<Void,Void> swingWorker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        logger.info("Updating hadoop data ...");
                        HadoopController.cacheHadoopFiles("/");
                        return null;
                    }

                    @Override
                    protected void done() {
                        jFrameParent.dispose();
                        JFrameSingleton.getMainScreen().dispose();
                        System.exit(0);
                    }
                };

                swingWorker.execute();


                final JProgressBar progressBar;
                progressBar = new JProgressBar(JProgressBar.HORIZONTAL, 0, 5000);
                progressBar.setStringPainted(true);
                progressBar.setValue(0);
                final Timer timer = new Timer(1000, null);
                timer.addActionListener(new ActionListener() {
                    int counter = 0;

                    public void actionPerformed(ActionEvent evt) {
                        counter++;
                        progressBar.setValue(counter);
                        if (counter > 5000) {
                            JOptionPane.showMessageDialog(null, "Done!");
                            timer.stop();
                        }

                    }
                });
                timer.start();
                int res = JOptionPane.showOptionDialog(null, progressBar, "Caching hadoop files", JOptionPane.DEFAULT_OPTION,
                        JOptionPane.INFORMATION_MESSAGE, null, null, null);

                System.out.println(res);
                if (res == 0){
                   // swingWorker.cancel(true);
                }
            };
        });



        JButton jButton = new JButton("SAVE");
        jButton.setPreferredSize(new Dimension(400,60));
        jButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                Const.homeDirectory = textField1.getText();
                Const.remoteUriPrefix = textField2.getText();
                Const.localUriPrefix = textField3.getText();
                if(jCheckBox.isSelected() && Const.getUseLocalMode().equals("true")){
                    // no change
                    logger.info("jCheckBox is selected");
                    logger.info("using local mode already");
                    logger.info("no changes will be made");
                }
                else if(!jCheckBox.isSelected() && Const.getUseLocalMode().equals("false")) {
                    // no change
                    logger.info("jCheckBox is not selected");
                    logger.info("not using local mode");
                    logger.info("no changes will be made");
                }
                else {
                    logger.info("changes will be made");
                    if(jCheckBox.isSelected()){
                        Const.setUseLocalMode("true");
                    }
                    else{
                        Const.setUseLocalMode("false");
                    }
                }

                JSONObject obj = new JSONObject();
                obj.put("homeDirectory", Const.homeDirectory);
                obj.put("localUriPrefix", Const.localUriPrefix);
                obj.put("remoteUriPrefix", Const.remoteUriPrefix);
                obj.put("useLocalMode",Const.getUseLocalMode());

                FileWriter file = null;
                try {
                    file = new FileWriter("src/main/resources/settings.json".replace("/",Const.localSeparator));
                    file.write(obj.toJSONString());
                    file.flush();
                    file.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                logger.info("Settings saved to src/main/resources/settings.json");

                Const.initializeValues();
                Const.changeFileSystem();


            }
        });


        JButton jButton2 = new JButton("BACK");
        jButton2.setPreferredSize(new Dimension(400,60));
        jButton2.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                if( source.equals("GeneralScreen")) {
                    jFrameParent.setContentPane(new GenScreen(jFrameParent,path));
                }
                else {
                    throw new IllegalArgumentException("please pass in the correct reference to previous screen");
                }
                JFrameSingleton.getMainScreen().invalidate();
                JFrameSingleton.getMainScreen().validate();
            }
        });


        JPanel panel1 = new JPanel(new BorderLayout());
        panel1.add(label1,BorderLayout.WEST);
        panel1.add(textField1,BorderLayout.EAST);

        JPanel panel2 = new JPanel(new BorderLayout());
        panel2.add(label2,BorderLayout.WEST);
        panel2.add(textField2,BorderLayout.EAST);

        JPanel panel3 = new JPanel(new BorderLayout());
        panel3.add(label3,BorderLayout.WEST);
        panel3.add(textField3,BorderLayout.EAST);

        JPanel panel4 = new JPanel(new BorderLayout());
        panel4.add(label4,BorderLayout.WEST);
        panel4.add(jCheckBox,BorderLayout.EAST);



        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.WEST;

        setLayout(new GridBagLayout());

        add(panel1,gbc);
        add(panel2,gbc);
        add(panel3,gbc);
        add(panel4,gbc);
        add(jButton,gbc);
        add(jButton2,gbc);
        add(updateCache,gbc);



    }


}
