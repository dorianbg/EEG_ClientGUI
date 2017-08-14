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
 * NeuralNetworkConfigScreen, 2017/08/08 15:24 dbg
 *
 **********************************************************************************************************************/
public class NeuralNetworkConfigScreen extends JPanel {
    private HashMap<String, String> config;
    private String config_seed;
    private String config_num_iterations;
    private String config_learning_rate;
    private String config_momentum;
    private String config_weight_init;
    private String config_updater;
    private String config_pretrain;
    private String config_backprop;
    private String config_optimization_algo;
    private String config_loss_function;

    public NeuralNetworkConfigScreen(HashMap<String, String> config){
        this.config = config;
        this.config_seed = config.get("config_seed");
        this.config_num_iterations = config.get("config_num_iterations");
        this.config_learning_rate = config.get("config_learning_rate");
        this.config_momentum = config.get("config_momentum");
        this.config_weight_init = config.get("config_weight_init");
        this.config_updater = config.get("config_updater");
        this.config_optimization_algo = config.get("config_optimization_algo");
        this.config_loss_function = config.get("config_loss_function");
        this.config_pretrain = config.get("config_pretrain");
        this.config_backprop = config.get("config_backprop");

        initializePanel();
    }

    private void initializePanel(){

        /*
        general constraints
         */
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.WEST;


        /*
        neural network general configuration screen
         */
        JPanel generalConfigPanel = new JPanel();
        generalConfigPanel.setLayout(new GridLayout(5,2,1,1));
        // 1st row
        final JLabel label1 = new JLabel("Seed");
        label1.setPreferredSize(new Dimension((int)(Const.screenSizeWidth*2/6),(int)(Const.screenSizeHeight*1/24)));
        final JTextField seedValueTextField1 = new JTextField(config_seed);
        seedValueTextField1.setPreferredSize(new Dimension((int)(Const.screenSizeWidth*1/12),(int)(Const.screenSizeHeight*1/24)));
        JPanel panel1 = new JPanel(new BorderLayout());
        panel1.add(label1,BorderLayout.WEST);
        panel1.add(seedValueTextField1,BorderLayout.EAST);

        // 2nd row
        final JLabel label2 = new JLabel("Iterations");
        label2.setPreferredSize(new Dimension((int)(Const.screenSizeWidth*2/6),(int)(Const.screenSizeHeight*1/24)));
        final JTextField numIterationsTextField2 = new JTextField(config_num_iterations);
        numIterationsTextField2.setPreferredSize(new Dimension((int)(Const.screenSizeWidth*1/12),(int)(Const.screenSizeHeight*1/24)));
        JPanel panel2 = new JPanel(new BorderLayout());
        panel2.add(label2,BorderLayout.WEST);
        panel2.add(numIterationsTextField2,BorderLayout.EAST);

        // 3rd row
        final JLabel label3 = new JLabel("Learning rate");
        label3.setPreferredSize(new Dimension((int)(Const.screenSizeWidth*2/6),(int)(Const.screenSizeHeight*1/24)));
        final JTextField learningRateTextField3 = new JTextField(config_learning_rate);
        learningRateTextField3.setPreferredSize(new Dimension((int)(Const.screenSizeWidth*1/12),(int)(Const.screenSizeHeight*1/24)));
        JPanel panel3 = new JPanel(new BorderLayout());
        panel3.add(label3,BorderLayout.WEST);
        panel3.add(learningRateTextField3,BorderLayout.EAST);

        // 4th row
        final JLabel label4 = new JLabel("Momentum");
        label4.setPreferredSize(new Dimension((int)(Const.screenSizeWidth*2/6),(int)(Const.screenSizeHeight*1/24)));
        final JTextField momentumValueTextField4 = new JTextField(config_momentum);
        momentumValueTextField4.setPreferredSize(new Dimension((int)(Const.screenSizeWidth*1/12),(int)(Const.screenSizeHeight*1/24)));
        JPanel panel4 = new JPanel(new BorderLayout());
        panel4.add(label4,BorderLayout.WEST);
        panel4.add(momentumValueTextField4,BorderLayout.EAST);

        // 5th row
        JLabel label5 = new JLabel("Weight init");
        label5.setPreferredSize(new Dimension((int)(Const.screenSizeWidth*2/6),(int)(Const.screenSizeHeight*1/24)));
        String[] choices5 = {"xavier", "zero", "sigmoid", "uniform", "relu"};
        final JComboBox weightInitComboBox5 = new JComboBox(choices5);
        weightInitComboBox5.setSelectedItem(config.get("config_weight_init"));
        JPanel panel5 = new JPanel(new BorderLayout());
        panel5.add(label5,BorderLayout.WEST);
        panel5.add(weightInitComboBox5,BorderLayout.EAST);

        // 6th row
        JLabel label6 = new JLabel("Updater");
        label6.setPreferredSize(new Dimension((int)(Const.screenSizeWidth*2/6),(int)(Const.screenSizeHeight*1/24)));
        String[] choices6 = {"sgd", "adam", "nesterovs", "adagrad", "rmsprop"};
        final JComboBox updaterComboBox6 = new JComboBox(choices6);
        updaterComboBox6.setSelectedItem(config.get("config_updater"));
        JPanel panel6 = new JPanel(new BorderLayout());
        panel6.add(label6,BorderLayout.WEST);
        panel6.add(updaterComboBox6,BorderLayout.EAST);

        // 7th row
        JLabel label7 = new JLabel("Optimization algo");
        label7.setPreferredSize(new Dimension((int)(Const.screenSizeWidth*2/6),(int)(Const.screenSizeHeight*1/24)));
        String[] choices7 = {"line_gradient_descent", "lbfgs", "conjugate_gradient", "stochastic_gradient_descent"};
        final JComboBox optimizationAlgoComboBox7 = new JComboBox(choices7);
        optimizationAlgoComboBox7.setSelectedItem(config.get("config_optimization_algo"));
        JPanel panel7 = new JPanel(new BorderLayout());
        panel7.add(label7,BorderLayout.WEST);
        panel7.add(optimizationAlgoComboBox7,BorderLayout.EAST);

        // 8th row
        JLabel label8 = new JLabel("Loss function (output)");
        label8.setPreferredSize(new Dimension((int)(Const.screenSizeWidth*2/6),(int)(Const.screenSizeHeight*1/24)));
        String[] choices8 = {"mse", "xent", "squared_loss" ,"negativeloglikelihood"};
        final JComboBox lossFunctionComboBox8 = new JComboBox(choices8);
        lossFunctionComboBox8.setSelectedItem(config.get("config_loss_function"));
        JPanel panel8 = new JPanel(new BorderLayout());
        panel8.add(label8,BorderLayout.WEST);
        panel8.add(lossFunctionComboBox8,BorderLayout.EAST);

        // 9th row
        JLabel label9 = new JLabel("Pretrain");
        label9.setPreferredSize(new Dimension((int)(Const.screenSizeWidth*2/6),(int)(Const.screenSizeHeight*1/24)));
        final JCheckBox pretrainCheckBox9 = new JCheckBox();
        if (config.get("config_pretrain").equals("true")) {
            pretrainCheckBox9.setSelected(true);
        } else {
            pretrainCheckBox9.setSelected(false);
        }
        JPanel panel9 = new JPanel(new BorderLayout());
        panel9.add(label9, BorderLayout.WEST);
        panel9.add(pretrainCheckBox9, BorderLayout.EAST);

        // 10th row
        JLabel label10 = new JLabel("Backprop");
        label10.setPreferredSize(new Dimension((int)(Const.screenSizeWidth*2/6),(int)(Const.screenSizeHeight*1/24)));
        final JCheckBox backpropCheckBox10 = new JCheckBox();
        if (config.get("config_backprop").equals("true")) {
            backpropCheckBox10.setSelected(true);
        } else {
            backpropCheckBox10.setSelected(false);
        }
        JPanel panel10 = new JPanel(new BorderLayout());
        panel10.add(label10, BorderLayout.WEST);
        panel10.add(backpropCheckBox10, BorderLayout.EAST);

        generalConfigPanel.add(panel1);
        generalConfigPanel.add(panel2);
        generalConfigPanel.add(panel3);
        generalConfigPanel.add(panel4);
        generalConfigPanel.add(panel5);
        generalConfigPanel.add(panel6);
        generalConfigPanel.add(panel7);
        generalConfigPanel.add(panel8);
        generalConfigPanel.add(panel9);
        generalConfigPanel.add(panel10);



        /*
        layers panel to which new layers are added
         */
        final JPanel layersPanel = new JPanel();
        layersPanel.setLayout(new GridBagLayout());
        layersPanel.add(new NeuralNetworkLayerPanel(layersPanel.getComponents().length+1),gbc);
        layersPanel.revalidate();

        /*
        management panel with the main buttons
         */
        JPanel managementPanel = new JPanel();
        JButton addLayerButton = new JButton("Add layer");
        addLayerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JPanel layerConfigPanel = new NeuralNetworkLayerPanel(layersPanel.getComponents().length+1);
                layersPanel.add(layerConfigPanel,gbc);
                layersPanel.revalidate();
                NeuralNetworkConfigScreen.this.revalidate();
                for(Component component : layersPanel.getComponents()){
                    System.out.println(component.toString());
                }
            }
        });
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {


                config_seed = seedValueTextField1.getText();
                config_num_iterations = numIterationsTextField2.getText();
                config_learning_rate = learningRateTextField3.getText();
                config_momentum = momentumValueTextField4.getText();
                config_weight_init = String.valueOf(weightInitComboBox5.getSelectedItem());
                config_updater = String.valueOf(updaterComboBox6.getSelectedItem());
                config_optimization_algo = String.valueOf(optimizationAlgoComboBox7.getSelectedItem());
                config_loss_function = String.valueOf(lossFunctionComboBox8.getSelectedItem());

                if(pretrainCheckBox9.isSelected()){
                    config_pretrain = "true";
                }
                else{
                    config_pretrain = "false";
                }

                if(backpropCheckBox10.isSelected()){
                    config_backprop = "true";
                }
                else{
                    config_backprop = "false";
                }



                boolean flag = true;
                try{
                    int num = Integer.parseInt(config_seed);
                    if(num<0){
                        flag = false;
                        JOptionPane.showMessageDialog(NeuralNetworkConfigScreen.this,"Please insert an positive integer as seed");
                    }
                } catch (NumberFormatException e1) {
                    flag = false;
                    JOptionPane.showMessageDialog(NeuralNetworkConfigScreen.this,"Please insert an positive integer as seed");
                }

                try{
                    int num = Integer.parseInt(config_num_iterations);
                    if(num<0){
                        flag = false;
                        JOptionPane.showMessageDialog(NeuralNetworkConfigScreen.this,"Please insert an positive integer as num iterations");
                    }
                } catch (NumberFormatException e1) {
                    flag = false;
                    JOptionPane.showMessageDialog(NeuralNetworkConfigScreen.this,"Please insert an integer as num iterations");
                }

                try{
                    double num = Double.parseDouble(config_learning_rate);
                    if(num<0){
                        flag = false;
                        JOptionPane.showMessageDialog(NeuralNetworkConfigScreen.this,"Please insert an positive double as learning rate");
                    }
                    if(num > 1){
                        flag = false;
                        JOptionPane.showMessageDialog(NeuralNetworkConfigScreen.this,"Please insert an positive double less than 1 as learning rate");
                    }
                } catch (NumberFormatException e1) {
                    flag = false;
                    JOptionPane.showMessageDialog(NeuralNetworkConfigScreen.this,"Please insert an positive double as learning rate");
                }
                try{
                    double num = Double.parseDouble(config_momentum);
                    if(num<0){
                        flag = false;
                        JOptionPane.showMessageDialog(NeuralNetworkConfigScreen.this,"Please insert an positive double as momentum");
                    }
                    if(num > 1){
                        flag = false;
                        JOptionPane.showMessageDialog(NeuralNetworkConfigScreen.this,"Please insert an positive double less than 1 as momentum");
                    }
                } catch (NumberFormatException e1) {
                    flag = false;
                    JOptionPane.showMessageDialog(NeuralNetworkConfigScreen.this,"Please insert an positive double as momentum");
                }


                if(flag){
                    config.put("config_seed", config_seed);
                    config.put("config_num_iterations", config_num_iterations);
                    config.put("config_learning_rate", config_learning_rate);
                    config.put("config_momentum", config_momentum);
                    config.put("config_weight_init", config_weight_init);
                    config.put("config_updater", config_updater);
                    config.put("config_optimization_algo", config_optimization_algo);
                    config.put("config_loss_function", config_loss_function);
                    config.put("config_pretrain", config_pretrain);
                    config.put("config_backprop", config_backprop);


                    for(Component jPanel : layersPanel.getComponents()){
                        if(jPanel instanceof NeuralNetworkLayerPanel){
                            config.putAll(((NeuralNetworkLayerPanel) jPanel).getConfig());
                        }
                    }
                }
            }
        });
        managementPanel.add(addLayerButton);
        managementPanel.add(saveButton);


        // main panel management
        setLayout(new GridBagLayout());
        add(generalConfigPanel, gbc);
        add(layersPanel, gbc);
        add(managementPanel, gbc);
    }

}
