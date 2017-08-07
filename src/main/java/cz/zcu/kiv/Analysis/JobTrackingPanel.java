package cz.zcu.kiv.Analysis;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import cz.zcu.kiv.DataUploading.GenScreen;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Map;

import static cz.zcu.kiv.Const.screenSizeHeight;
import static cz.zcu.kiv.Const.screenSizeWidth;

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
 * JobTrackingPanel, 2017/07/28 08:17 dbg
 *
 **********************************************************************************************************************/
public class JobTrackingPanel extends JPanel {
    private static Log logger = LogFactory.getLog(JobTrackingPanel.class);
    private Client client;
    private Map<String,String> queryParams;
    private int jobId;
    private JTextArea jobStatusLabel;
    private JTextArea jobResult;

    public JobTrackingPanel(Client client, int jobId, Map<String,String> queryParams){
        this.client = client;
        this.jobId = jobId;
        this.queryParams = queryParams;
        initializePanel();
    }

    public void initializePanel(){
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.WEST;
        setLayout(new GridBagLayout());


        JPanel jobStatusPanel = new JPanel();
        jobStatusPanel.setLayout(new BorderLayout());

        jobStatusPanel.add(new JLabel("Job status:"),BorderLayout.WEST);

        jobStatusLabel = new JTextArea("RUNNING");
        jobStatusLabel.setEditable(false);
        jobStatusPanel.add(jobStatusLabel,BorderLayout.EAST);


        /*
        job tracking
        1. job info
        2. result info
         */
        JPanel jobTrackingPanel = new JPanel();
        jobTrackingPanel.setLayout(new GridLayout(2,1));

        /*
        job info
         */
        JPanel jobResultPanel = new JPanel();
        jobResultPanel.setLayout(new BorderLayout());
        jobResultPanel.add(new JLabel("Result of the job"),BorderLayout.NORTH);
        jobResult = new JTextArea(150,20);
        jobResult.setEditable(false);
        jobResultPanel.add(jobResult);
        jobTrackingPanel.add(jobResultPanel,BorderLayout.CENTER);
        /*
        result info
         */
        JPanel classifierInfoPanel = new JPanel();
        classifierInfoPanel.setLayout(new BorderLayout());
        classifierInfoPanel.add(new JLabel("Job information"),BorderLayout.NORTH);
        JTextArea classifierInfo = new JTextArea(150,20);
        classifierInfo.setEditable(false);
        if(queryParams.containsKey("load_clf")){
            logger.info("Getting the job configuration");
            WebResource webResource = client.resource("http://localhost:8080").path("/jobs/configuration/" + queryParams.get("load_name"));
            String uri = webResource.getURI().toString();
            logger.info("load_name is " + queryParams.get("load_name"));
            logger.info("Request URI is " + uri);
            ClientResponse responseMsg = webResource.get(ClientResponse.class);  // you just change this call from post to get
            String text = responseMsg.getEntity(String.class);
            logger.info("Job configuration is " + text);
            text = text.replace("/////","\n");
            classifierInfo.setText(text);
            logger.info("Set the text of the text area");
        }
        else{
            classifierInfo.setText(hashMapToText(queryParams));
        }
        classifierInfoPanel.add(classifierInfo,BorderLayout.CENTER);
        jobTrackingPanel.add(classifierInfoPanel);

        /*
        log button
         */
        JButton logButton = new JButton("Log");
        logButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JFrame frame = new JFrame();
                final Client loadClient = Client.create();
                WebResource webResourceClient = loadClient.resource("http://localhost:8080").path("/jobs/log/" + jobId);
                ClientResponse responseMsg = webResourceClient.get(ClientResponse.class);  // you just change this call from post to get
                String log = responseMsg.getEntity(String.class);
                //
                JTextArea textArea = new JTextArea(log);
                textArea.setEditable(false); // set textArea non-editable

                JScrollPane jScrollPane = new JScrollPane(textArea);
                // sets the jscrollbar to the bottom
                JScrollBar vertical = jScrollPane.getVerticalScrollBar();
                vertical.setValue( vertical.getMaximum() );

                JPanel panel = new JPanel();
                panel.setLayout(new GridLayout());
                panel.add(jScrollPane);
                frame.add(panel);
                frame.setSize((int) screenSizeWidth * 2 / 3, (int) (screenSizeHeight * 3 / 4));
                frame.setResizable(true);
                frame.setLocationByPlatform(true);
                frame.setLocationRelativeTo(null);
                frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
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



        this.add(jobStatusPanel,gbc);
        this.add(jobTrackingPanel,gbc);
        this.add(logButton,gbc);

        SwingWorker<Void, Void> swingWorker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                String output2 = "";
                WebResource webResource2 = client.resource("http://localhost:8080").path("/jobs/check/" + jobId);
                logger.info(webResource2);

                while (!output2.equals("FINISHED")) {
                    output2 = webResource2.get(ClientResponse.class).getEntity(String.class);
                    logger.info(output2);
                    jobStatusLabel.setText(output2);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                WebResource webResource3 = client.resource("http://localhost:8080").path("/jobs/result/" + jobId);
                logger.info(webResource3);
                String output3 = webResource3.get(ClientResponse.class).getEntity(String.class);
                logger.info(output3);
                jobResult.setText(output3);
                JOptionPane.showMessageDialog(JobTrackingPanel.this,"Job " + jobId + " is done");
            }
        };
        swingWorker.execute();
    }



    private String hashMapToText(Map<String,String> queryParams){
        StringBuilder params = new StringBuilder(500);
        for(String key : queryParams.keySet()){
            params.append(key);
            params.append(": ");
            params.append(queryParams.get(key));
            params.append("\n");
        }
        return params.toString();
    }
}
