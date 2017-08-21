package cz.zcu.kiv.Analysis.ConfigPanels;

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
 * DecisionTreeConfigScreen, 2017/08/05 11:56 dbg
 *
 **********************************************************************************************************************/
public class DecisionTreeConfigScreen extends JPanel {
    private HashMap<String, String> config;
    private String maxDepth;
    private String maxBins;
    private String minInstancesPerNode;
    private String impurity;

    public DecisionTreeConfigScreen(HashMap<String, String> config){
        this.config = config;
        this.maxDepth = config.get("config_max_depth");
        this.maxBins = config.get("config_max_bins");
        this.minInstancesPerNode = config.get("config_min_instances_per_node");
        this.impurity = config.get("config_impurity");
        initializePanel();
    }

    private void initializePanel(){
        setLayout(new GridLayout(5,1,1,1));

        // 1st row
        JLabel label1 = new JLabel("Max depth");
        label1.setPreferredSize(new Dimension((int)(Const.screenSizeWidth*2/6),(int)(Const.screenSizeHeight*1/24)));
        final JTextField textField1 = new JTextField(maxDepth);
        textField1.setPreferredSize(new Dimension((int)(Const.screenSizeWidth*1/6),(int)(Const.screenSizeHeight*1/24)));
        JPanel panel1 = new JPanel(new BorderLayout());
        panel1.add(label1,BorderLayout.WEST);
        panel1.add(textField1,BorderLayout.EAST);
        // 2nd row
        final JLabel label2 = new JLabel("Max bins");
        label2.setPreferredSize(new Dimension((int)(Const.screenSizeWidth*2/6),(int)(Const.screenSizeHeight*1/24)));
        final JTextField textField2 = new JTextField(maxBins);
        textField2.setPreferredSize(new Dimension((int)(Const.screenSizeWidth*1/6),(int)(Const.screenSizeHeight*1/24)));
        JPanel panel2 = new JPanel(new BorderLayout());
        panel2.add(label2,BorderLayout.WEST);
        panel2.add(textField2,BorderLayout.EAST);
        // 3rd row
        final JLabel label3 = new JLabel("Min instances per node");
        label3.setPreferredSize(new Dimension((int)(Const.screenSizeWidth*2/6),(int)(Const.screenSizeHeight*1/24)));
        final JTextField textField3 = new JTextField(minInstancesPerNode);
        textField3.setPreferredSize(new Dimension((int)(Const.screenSizeWidth*1/6),(int)(Const.screenSizeHeight*1/24)));
        JPanel panel3 = new JPanel(new BorderLayout());
        panel3.add(label3,BorderLayout.WEST);
        panel3.add(textField3,BorderLayout.EAST);
        // 4th row
        JLabel label4 = new JLabel("Impurity");
        label4.setPreferredSize(new Dimension((int)(Const.screenSizeWidth*2/6),(int)(Const.screenSizeHeight*1/24)));
        String[] choices = {"gini", "entropy"};
        final JComboBox impurityComboBox = new JComboBox(choices);
        impurityComboBox.setSelectedItem(config.get("config_impurity"));
        JPanel panel4 = new JPanel(new BorderLayout());
        panel4.add(label4,BorderLayout.WEST);
        panel4.add(impurityComboBox,BorderLayout.EAST);

        //5th row
        JButton button = new JButton("SAVE");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                maxDepth = textField1.getText();
                maxBins = textField2.getText();
                minInstancesPerNode = textField3.getText();
                impurity = String.valueOf(impurityComboBox.getSelectedItem());

                boolean flag = true;
                try{
                    int num = Integer.parseInt(maxDepth);
                    if(num<0){
                        flag = false;
                        JOptionPane.showMessageDialog(DecisionTreeConfigScreen.this,"Please insert an positive integer as max depth parameter");
                    }
                } catch (NumberFormatException e1) {
                    flag = false;
                    JOptionPane.showMessageDialog(DecisionTreeConfigScreen.this,"Please insert an positive integer as max depth parameter");
                }
                try{
                    int num = Integer.parseInt(maxBins);
                    if(num<0){
                        flag = false;
                        JOptionPane.showMessageDialog(DecisionTreeConfigScreen.this,"Please insert an positive integer as max bins parameter");
                    }
                } catch (NumberFormatException e1) {
                    flag = false;
                    JOptionPane.showMessageDialog(DecisionTreeConfigScreen.this,"Please insert an integer as max bins parameter");
                }
                try{
                    int num = Integer.parseInt(minInstancesPerNode);
                    if(num<0){
                        flag = false;
                        JOptionPane.showMessageDialog(DecisionTreeConfigScreen.this,"Please insert an positive integer as min instances per node");
                    }
                } catch (NumberFormatException e1) {
                    flag = false;
                    JOptionPane.showMessageDialog(DecisionTreeConfigScreen.this,"Please insert an positive integer as min instances per node");
                }


                if(flag){
                    config.put("config_max_depth", maxDepth);
                    config.put("config_max_bins", maxBins);
                    config.put("config_min_instances_per_node", minInstancesPerNode);
                    config.put("config_impurity", impurity);
                }

            }
        });
        add(panel1);
        add(panel2);
        add(panel3);
        add(panel4);
        add(button);
    }

}
