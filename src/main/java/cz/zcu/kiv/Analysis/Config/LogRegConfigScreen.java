package cz.zcu.kiv.Analysis.Config;

import cz.zcu.kiv.Const;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

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
 * LogRegConfigScreen, 2017/07/26 16:03 dbg
 *
 **********************************************************************************************************************/
public class LogRegConfigScreen extends JPanel {
    private HashMap<String, String> config;
    private String config_step_size;
    private String config_num_iterations;
    private String config_mini_batch_fraction;

    public LogRegConfigScreen(HashMap<String, String> config){
        this.config = config;
        this.config_step_size = config.get("config_step_size");
        this.config_num_iterations = config.get("config_num_iterations");
        this.config_mini_batch_fraction = config.get("config_mini_batch_fraction");
        initializePanel();
    }

    private void initializePanel(){
        setLayout(new GridLayout(4,1,1,1));

        // 1st row
        JLabel label1 = new JLabel("Step size");
        label1.setPreferredSize(new Dimension((int)(Const.screenSizeWidth*2/6),(int)(Const.screenSizeHeight*1/24)));
        final JTextField textField1 = new JTextField(config_step_size);
        textField1.setPreferredSize(new Dimension((int)(Const.screenSizeWidth*1/6),(int)(Const.screenSizeHeight*1/24)));
        JPanel panel1 = new JPanel(new BorderLayout());
        panel1.add(label1,BorderLayout.WEST);
        panel1.add(textField1,BorderLayout.EAST);
        // 2nd row
        final JLabel label2 = new JLabel("Number of iterations");
        label2.setPreferredSize(new Dimension((int)(Const.screenSizeWidth*2/6),(int)(Const.screenSizeHeight*1/24)));
        final JTextField textField2 = new JTextField(config_num_iterations);
        textField2.setPreferredSize(new Dimension((int)(Const.screenSizeWidth*1/6),(int)(Const.screenSizeHeight*1/24)));
        JPanel panel2 = new JPanel(new BorderLayout());
        panel2.add(label2,BorderLayout.WEST);
        panel2.add(textField2,BorderLayout.EAST);
        // 4th row
        JLabel label4 = new JLabel("Mini batch fraction");
        label4.setPreferredSize(new Dimension((int)(Const.screenSizeWidth*2/6),(int)(Const.screenSizeHeight*1/24)));
        final JTextField textField4 = new JTextField(config_mini_batch_fraction);
        textField4.setPreferredSize(new Dimension((int)(Const.screenSizeWidth*1/6),(int)(Const.screenSizeHeight*1/24)));
        JPanel panel4 = new JPanel(new BorderLayout());
        panel4.add(label4,BorderLayout.WEST);
        panel4.add(textField4,BorderLayout.EAST);


        JButton button = new JButton("SAVE");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                config_step_size = textField1.getText();
                config_num_iterations = textField2.getText();
                config_mini_batch_fraction = textField4.getText();


                boolean flag = true;
                try{
                    double num = Double.parseDouble(config_step_size);
                    if(num<0){
                        flag = false;
                        JOptionPane.showMessageDialog(LogRegConfigScreen.this,"Please insert an positive double as step size");
                    }
                    if(num>1){
                        flag = false;
                        JOptionPane.showMessageDialog(LogRegConfigScreen.this,"Please insert a double less than 1 as step size");
                    }
                } catch (NumberFormatException e1) {
                    flag = false;
                    JOptionPane.showMessageDialog(LogRegConfigScreen.this,"Please insert an positive integer as step size");
                }
                try{
                    int num = Integer.parseInt(config_num_iterations);
                    if(num<0){
                        flag = false;
                        JOptionPane.showMessageDialog(LogRegConfigScreen.this,"Please insert an positive integer as num iterations");
                    }
                } catch (NumberFormatException e1) {
                    flag = false;
                    JOptionPane.showMessageDialog(LogRegConfigScreen.this,"Please insert an integer as num iterations");
                }
                try{
                    double num = Double.parseDouble(config_mini_batch_fraction);
                    if(num<0){
                        flag = false;
                        JOptionPane.showMessageDialog(LogRegConfigScreen.this,"Please insert an positive double as mini batch fraction");
                    }
                    if(num > 1){
                        flag = false;
                        JOptionPane.showMessageDialog(LogRegConfigScreen.this,"Please insert an positive double less than 1 as mini batch fraction");
                    }
                } catch (NumberFormatException e1) {
                    flag = false;
                    JOptionPane.showMessageDialog(LogRegConfigScreen.this,"Please insert an positive double as mini batch fraction");
                }

                if(flag) {
                    config.put("config_step_size", config_step_size);
                    config.put("config_num_iterations", config_num_iterations);
                    config.put("config_mini_batch_fraction", config_mini_batch_fraction);
                }
            }
        });

        add(panel1);
        add(panel2);
        add(panel4);
        add(button);
    }

}
