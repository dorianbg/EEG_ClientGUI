package cz.zcu.kiv.Analysis;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import cz.zcu.kiv.Analysis.ConfigPanels.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import static cz.zcu.kiv.Const.*;

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
 * AnalysisPanel, 2017/07/11 19:58 Dorian Beganovic
 *
 **********************************************************************************************************************/
public class AnalysisPanel extends JPanel {
    private String infoFileName;
    private String eegFileName;
    private int guessedNumber;
    private HashMap<String, String> config = new HashMap<String, String>();
    private static Log logger = LogFactory.getLog(AnalysisPanel.class);

    /**
     * when initializing with a info.txt file
     *
     * @param infoFileName location of the file
     */
    public AnalysisPanel(String infoFileName) {
        this.infoFileName = infoFileName;
        logger.info("Starting analysis panel with infoFileName= " + infoFileName);
        this.eegFileName = "";
        this.guessedNumber = -100;
        initializePanel();
    }

    /**
     * when initializing with a file and a guessed number
     *
     * @param eegFileName   filename
     * @param guessedNumber guessed number
     */
    public AnalysisPanel(String eegFileName, int guessedNumber) {
        logger.info("initializing a new Analysis Panel");
        this.infoFileName = "";
        this.eegFileName = eegFileName;
        logger.info("Starting analysis panel with eeg file name= " + eegFileName);
        this.guessedNumber = guessedNumber;
        logger.info("Starting analysis panel with guessed number= " + guessedNumber);

        initializePanel();
    }

    public void initializePanel() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.WEST;

        /*
          1st row -> display file name
         */
        JLabel label1 = new JLabel("File  ");
        String fileName;
        if (infoFileName.length() == 0) {
            fileName = eegFileName;
        } else {
            fileName = infoFileName;
        }
        final JTextField textField1 = new JTextField(fileName);
        textField1.setEditable(false);
        textField1.setPreferredSize(new Dimension(400, 30));
        final JPanel filenameRow = new JPanel(new BorderLayout());
        filenameRow.add(label1, BorderLayout.WEST);
        filenameRow.add(textField1, BorderLayout.EAST);

        /*
         * 2nd row -> choose feature extraction method
         */
        JLabel label2 = new JLabel("Feature extraction method  ");
        String[] choices = {"dwt-8"};
        final JComboBox featureExtractionComboBox = new JComboBox(choices);
        featureExtractionComboBox.setPreferredSize(new Dimension(400, 30));
        JPanel featureExtractionRow = new JPanel(new BorderLayout());
        featureExtractionRow.add(label2, BorderLayout.WEST);
        featureExtractionRow.add(featureExtractionComboBox, BorderLayout.EAST);


        /*
         *  Mid panel -> Save classifier vs Load classifier options
         */

        /*
         * Load classifier panel
         */

        final Client loadClient = Client.create();
        WebResource webResourceClient = loadClient.resource(serverConnectionUri).path("/classifiers/list");
        ClientResponse responseMsg = webResourceClient.get(ClientResponse.class);  // you just change this call from post to get
        String[] queries1 = responseMsg.getEntity(String.class).replace('[',' ').replace(']',' ').split(",");

        final JList loadClfList = new JList(queries1);
        loadClfList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        loadClfList.setVisibleRowCount(5);
        JScrollPane scrollPane1 = new JScrollPane(loadClfList);
        Dimension d1 = loadClfList.getPreferredSize();
        d1.width = (int) (screenSizeWidth * 1 / 3);
        d1.height = (int) (screenSizeHeight * 1 / 6);
        scrollPane1.setPreferredSize(d1);
        JPanel panel11 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel11.add(scrollPane1);

        JPanel loadPanel = new JPanel();
        loadPanel.setLayout(new GridBagLayout());
        loadPanel.add(panel11, gbc);


        /*
         * save classifier panel
         */

        // list and selection of classifier
        String[] queries2 = {"Support Vector Machine", "Logistic Regression", "Random Forest", "Decision Tree", "Neural Network"};
        final JList trainClfList = new JList(queries2);
        // initialize for the first classifier - Support Vector Machine
        trainClfList.setSelectedIndex(0);
        config.put("config_step_size", "1.0");
        config.put("config_num_iterations", "100");
        config.put("config_reg_param", "0.01");
        config.put("config_mini_batch_fraction", "1.0");

        trainClfList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        trainClfList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                // there are 2 events => release and press, this makes sure to only register one of them
                if (!e.getValueIsAdjusting() && e.getFirstIndex() != e.getLastIndex()) {
                    String value = trainClfList.getSelectedValue().toString();
                    System.out.println(value);
                    if (value.equals("Support Vector Machine")) {
                        config.clear();
                        config.put("config_step_size", "1.0");
                        config.put("config_num_iterations", "100");
                        config.put("config_reg_param", "0.01");
                        config.put("config_mini_batch_fraction", "1.0");
                    } else if (value.equals("Logistic Regression")) {
                        config.clear();
                        config.put("config_step_size", "1.0");
                        config.put("config_num_iterations", "100");
                        config.put("config_mini_batch_fraction", "1.0");
                    } else if (value.equals("Decision Tree")) {
                        config.clear();
                        config.put("config_max_depth", "5");
                        config.put("config_max_bins", "32");
                        config.put("config_min_instances_per_node", "1");
                        config.put("config_impurity", "gini");
                    } else if (value.equals("Random Forest")) {
                        config.clear();
                        config.put("config_max_depth", "5");
                        config.put("config_max_bins", "32");
                        config.put("config_min_instances_per_node", "1");
                        config.put("config_impurity", "gini");
                        config.put("config_feature_subset", "auto");
                        config.put("config_num_trees", "100");
                    } else if(value.equals("Neural Network")) {
                        config.clear();
                        config.put("config_seed", "12345");
                        config.put("config_num_iterations", "1000");
                        config.put("config_learning_rate", "0.1");
                        config.put("config_momentum", "0.5");
                        config.put("config_weight_init", "xavier");
                        config.put("config_updater", "nesterovs");
                        config.put("config_optimization_algo", "conjugate_gradient");
                        config.put("config_loss_function", "xent");
                        config.put("config_pretrain", "false");
                        config.put("config_backprop", "true");
                    }
                    else{

                    }
                }
            }
        });
        trainClfList.setVisibleRowCount(5);
        JScrollPane scrollPane2 = new JScrollPane(trainClfList);
        Dimension d2 = trainClfList.getPreferredSize();
        d2.width = (int) (screenSizeWidth * 1 / 6);
        d2.height = (int) (screenSizeHeight * 1 / 6);
        scrollPane2.setPreferredSize(d2);
        JPanel panel21 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel21.add(scrollPane2);

        //
        final JButton configureButton = new JButton("Configure");
        configureButton.setPreferredSize(new Dimension((int) (screenSizeWidth * 1 / 12), (int) (screenSizeHeight * 1 / 24)));
        configureButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JFrame frame = new JFrame();
                frame.setSize((int) screenSizeWidth / 3, (int) (screenSizeHeight * 1 / 2));
                frame.setResizable(true);
                frame.setLocationByPlatform(true);
                frame.setLocationRelativeTo(null);
                frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

                if (trainClfList.getSelectedValue().toString().equals("Logistic Regression")) {
                    frame.add(new LogRegConfigScreen(config));
                } else if (trainClfList.getSelectedValue().toString().equals("Support Vector Machine")) {
                    frame.add(new SVMConfigScreen(config));
                } else if (trainClfList.getSelectedValue().toString().equals("Decision Tree")) {
                    frame.add(new DecisionTreeConfigScreen(config));
                } else if (trainClfList.getSelectedValue().toString().equals("Random Forest")) {
                    frame.add(new RandomForestConfigScreen(config));
                } else if (trainClfList.getSelectedValue().toString().equals("Neural Network")) {
                    frame.setSize((int) screenSizeWidth * 1/2, (int) (screenSizeHeight * 2 / 3));

                    JPanel creatingPanel = new NeuralNetworkConfigScreen(config);
                    frame.add(creatingPanel);
                } else {

                }
                frame.setVisible(true);
                // set the escape button
                KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
                Action escapeAction = new AbstractAction() {
                    // close the frame when the user presses escape
                    public void actionPerformed(ActionEvent e) {
                        frame.dispose();
                    }
                };
                frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
                frame.getRootPane().getActionMap().put("ESCAPE", escapeAction);
            }
        });
        panel21.add(configureButton);


        /*
         * save classifier button
         */
        JLabel label5 = new JLabel("Save classifier ");
        final JCheckBox saveClassifierCheckBox = new JCheckBox();
        saveClassifierCheckBox.setPreferredSize(new Dimension(400, 30));
        JPanel saveClassifierOptionRow = new JPanel(new BorderLayout());
        saveClassifierOptionRow.add(label5, BorderLayout.WEST);
        saveClassifierOptionRow.add(saveClassifierCheckBox, BorderLayout.EAST);

        /*
         * input save classifier name
         */
        JLabel label6 = new JLabel("Saved classifier name");
        final JTextField classifierNameJTextField = new JTextField("");
        classifierNameJTextField.setPreferredSize(new Dimension((int) screenSizeWidth / 5, (int) screenSizeHeight / 20));
        JPanel savedClassifierNameRow = new JPanel(new BorderLayout());
        savedClassifierNameRow.add(label6, BorderLayout.WEST);
        savedClassifierNameRow.add(classifierNameJTextField, BorderLayout.EAST);


        // construct the whole train panel
        JPanel trainPanel = new JPanel();
        trainPanel.setLayout(new GridBagLayout());
        trainPanel.add(panel21, gbc);
        trainPanel.add(saveClassifierOptionRow, gbc);
        trainPanel.add(savedClassifierNameRow, gbc);


        // add all together using CardLayout for switching
        final CardLayout cardLayout = new CardLayout();
        final JPanel panel4 = new JPanel(cardLayout);
        panel4.add(loadPanel, "LOAD");
        panel4.add(trainPanel, "TRAIN");


        /*
         * Choose whether to load or save a classifier
         */
        ButtonGroup buttonGroup = new ButtonGroup();

        final JRadioButton loadButton = new JRadioButton("Load a classifier", true);
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CardLayout cardLayout = (CardLayout) (panel4.getLayout());
                cardLayout.show(panel4, "LOAD");
                WebResource webResourceClient = loadClient.resource(serverConnectionUri).path("/classifiers/list");
                ClientResponse responseMsg = webResourceClient.get(ClientResponse.class);  // you just change this call from post to get
                String[] queries1 = responseMsg.getEntity(String.class).replace('[',' ').replace(']',' ').split(",");
                loadClfList.setListData(queries1);

            }
        });

        final JRadioButton trainButton = new JRadioButton("Train a classifier", false);
        trainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CardLayout cardLayout = (CardLayout) (panel4.getLayout());
                cardLayout.show(panel4, "TRAIN");
            }
        });

        buttonGroup.add(loadButton);
        buttonGroup.add(trainButton);
        JPanel panel3 = new JPanel(new GridBagLayout());
        panel3.add(loadButton);
        panel3.add(trainButton);


        // last
        JButton goButton = new JButton("GO");
        goButton.setPreferredSize(new Dimension((int) screenSizeWidth / 10, (int) screenSizeHeight / 20));

        goButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent me) {
                Random rand = new Random();

                final int jobId = rand.nextInt(Integer.MAX_VALUE-2) + 1;

                HashMap<String, String> queryParams = new LinkedHashMap<String, String>(25);

                // add the source
                if (infoFileName.length() == 0) {
                    queryParams.put("eeg_file", eegFileName);
                    queryParams.put("guessed_num", String.valueOf(guessedNumber));
                } else {
                    queryParams.put("info_file", infoFileName);
                }

                // add the feature extraction
                queryParams.put("fe", String.valueOf(featureExtractionComboBox.getSelectedItem()));

                // add the classifier
                if (loadButton.isSelected()) {
                    String value = loadClfList.getSelectedValue().toString();
                    queryParams.put("load_clf", value.substring(1, value.indexOf("_")));
                    queryParams.put("load_name", value.substring(1,value.length()).replaceAll(" ", "").replaceAll("\n","").replaceAll("\t",""));
                } else if (trainButton.isSelected()) {
                    String value = trainClfList.getSelectedValue().toString();
                    String clfAbbreviation = "";
                    if (value.equals("Support Vector Machine")) {
                        clfAbbreviation = "svm";
                    } else if (value.equals("Logistic Regression")) {
                        clfAbbreviation = "logreg";
                    } else if (value.equals("Random Forest")) {
                        clfAbbreviation = "rf";
                    } else if (value.equals("Decision Tree")) {
                        clfAbbreviation = "dt";
                    } else if (value.equals("Neural Network")) {
                        clfAbbreviation = "nn";
                    } else {
                        clfAbbreviation = "";
                    }

                    queryParams.put("train_clf", clfAbbreviation);
                    if (saveClassifierCheckBox.isSelected()) {
                        queryParams.put("save_clf", "true");
                        queryParams.put("save_name", clfAbbreviation + "_"+ classifierNameJTextField.getText().replaceAll("[^a-zA-Z0-9]", "_"));
                    }
                } else {
                    throw new IllegalArgumentException("Please select train or load a classifier button");
                }

                // add config params
                queryParams.putAll(config);

                //create the client connection
                final Client client = Client.create();
                WebResource webResource = client.resource(serverConnectionUri).path("/jobs/submit/" + jobId);
                for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                    webResource = webResource.queryParam(entry.getKey(), entry.getValue());
                }
                ClientResponse responseMsg = webResource.get(ClientResponse.class);  // you just change this call from post to get

                logger.info(webResource);
                logger.info(responseMsg);

                logger.info("Launching GUI");
                final JFrame frame = new JFrame();

                final JobTrackingPanel panel = new JobTrackingPanel(client,jobId,queryParams);
                frame.add(panel);
                frame.setSize((int) screenSizeWidth / 2, (int) (screenSizeHeight * 3 / 4));
                frame.setResizable(true);
                frame.setLocationByPlatform(true);
                frame.setLocationRelativeTo(null);
                frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                frame.setVisible(true);
                frame.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                        // pop-up window with a question
                        if (JOptionPane.showConfirmDialog(frame, "Are you you want to close this window? If a job is running it will be cancelled", "Closing", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
                                == JOptionPane.YES_OPTION)
                        {
                            logger.info("Closing the window");
                            WebResource webResource = client.resource(serverConnectionUri).path("/jobs/cancel/" + jobId);
                            ClientResponse responseMsg = webResource.get(ClientResponse.class);  // you just change this call from post to get
                            logger.info("Cancelled the job");
                        }
                    }
                });
            }
        });

        JPanel panel7 = new JPanel(new GridBagLayout());
        panel7.add(goButton);


        // final settings
        setLayout(new GridBagLayout());

        add(filenameRow, gbc);
        add(featureExtractionRow, gbc);
        add(panel3, gbc);
        add(panel4, gbc);
        add(panel7, gbc);

    }

}
