package cz.zcu.kiv.Analysis.ConfigPanels;

import cz.zcu.kiv.Const;

import javax.swing.*;
import java.awt.*;
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
 * NeuralNetworkLayerPanel, 2017/08/09 08:38 dbg
 *
 **********************************************************************************************************************/
public class NeuralNetworkLayerPanel extends JPanel {
    private HashMap<String, String> config = new HashMap<String, String>(10);
    private String config_layer_type;
    private String config_drop_out;
    private String config_activation_function;
    private String config_n_out;
    private String config_layer_id;

    // instance fields
    private JComboBox layerTypeComboBox1;
    private JTextField dropOutTextField2;
    private JComboBox activationFunctionComboBox3;
    private JTextField nOutTextField;

    public NeuralNetworkLayerPanel(int id){
        this.config_layer_id = String.valueOf(id);
        System.out.println("new layer with id = " + id);
        initializeLayerPanel();
    }

    private void initializeLayerPanel(){
        setLayout(new GridLayout(1,4,3,1));

        // 1st row
        JLabel label1 = new JLabel("Layer type");
        label1.setPreferredSize(new Dimension((int)(Const.screenSizeWidth*2/6),(int)(Const.screenSizeHeight*1/24)));
        String[] choices1 = {"output", "dense", "auto_encoder", "rbm", "graves_lstm"};
        layerTypeComboBox1 = new JComboBox(choices1);
        JPanel panel1 = new JPanel(new BorderLayout());
        panel1.add(label1,BorderLayout.WEST);
        panel1.add(layerTypeComboBox1,BorderLayout.EAST);

        JLabel label2 = new JLabel("Drop Out");
        label2.setPreferredSize(new Dimension((int)(Const.screenSizeWidth*2/6),(int)(Const.screenSizeHeight*1/24)));
        dropOutTextField2 = new JTextField("0.0");
        dropOutTextField2.setPreferredSize(new Dimension((int)(Const.screenSizeWidth*1/36),(int)(Const.screenSizeHeight*1/24)));
        JPanel panel2 = new JPanel(new BorderLayout());
        panel2.add(label2,BorderLayout.WEST);
        panel2.add(dropOutTextField2,BorderLayout.EAST);


        // 2nd row
        JLabel label3 = new JLabel("Activation func");
        label3.setPreferredSize(new Dimension((int)(Const.screenSizeWidth*2/6),(int)(Const.screenSizeHeight*1/24)));
        String[] choices3 = {"sigmoid", "softmax", "relu", "tanh", "identity", "softplus", "elu"};
        activationFunctionComboBox3 = new JComboBox(choices3);
        JPanel panel3 = new JPanel(new BorderLayout());
        panel3.add(label3,BorderLayout.WEST);
        panel3.add(activationFunctionComboBox3,BorderLayout.EAST);

        final JLabel label4 = new JLabel("nOut");
        label4.setPreferredSize(new Dimension((int)(Const.screenSizeWidth*2/6),(int)(Const.screenSizeHeight*1/24)));
        nOutTextField = new JTextField("2");
        nOutTextField.setPreferredSize(new Dimension((int)(Const.screenSizeWidth*1/36),(int)(Const.screenSizeHeight*1/24)));
        JPanel panel4 = new JPanel(new BorderLayout());
        panel4.add(label4,BorderLayout.WEST);
        panel4.add(nOutTextField,BorderLayout.EAST);

        setBorder(BorderFactory.createLineBorder(Color.black));
        add(panel1);
        add(panel3);
        add(panel2);
        add(panel4);
    }

    public HashMap<String,String> getConfig(){
        config_layer_type = String.valueOf(layerTypeComboBox1.getSelectedItem());
        config_drop_out = dropOutTextField2.getText();
        config_activation_function = String.valueOf(activationFunctionComboBox3.getSelectedItem());
        config_n_out = nOutTextField.getText();

        boolean flag = true;
        try{
            int num = Integer.parseInt(config_n_out);
            if(num<0){
                flag = false;
                JOptionPane.showMessageDialog(NeuralNetworkLayerPanel.this,"Please insert an positive integer as number of units out");
            }
        } catch (NumberFormatException e1) {
            flag = false;
            JOptionPane.showMessageDialog(NeuralNetworkLayerPanel.this,"Please insert an positive integer number of units out");
        }
        try{
            double num = Double.parseDouble(config_drop_out);
            if(num<0){
                flag = false;
                JOptionPane.showMessageDialog(NeuralNetworkLayerPanel.this,"Please insert a positive double as drop out rate");
            }
            if(num > 1){
                flag = false;
                JOptionPane.showMessageDialog(NeuralNetworkLayerPanel.this,"Please insert an positive double less than 1 as drop out rate");
            }
        } catch (NumberFormatException e1) {
            flag = false;
            JOptionPane.showMessageDialog(NeuralNetworkLayerPanel.this,"Please insert an positive double as drop out rate");
        }

        if(flag){
            config.put("config_layer"+ config_layer_id + "_layer_type",config_layer_type);
            config.put("config_layer"+ config_layer_id + "_drop_out",config_drop_out);
            config.put("config_layer"+ config_layer_id + "_activation_function",config_activation_function);
            config.put("config_layer"+ config_layer_id + "_n_out",config_n_out);
        }

        return config;
    }


}
