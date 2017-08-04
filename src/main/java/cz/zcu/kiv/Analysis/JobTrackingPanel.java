package cz.zcu.kiv.Analysis;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import cz.zcu.kiv.DataUploading.GenScreen;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

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
        classifierInfo.setText(hashMapToText(queryParams));
        classifierInfoPanel.add(classifierInfo,BorderLayout.CENTER);
        jobTrackingPanel.add(classifierInfoPanel);


        this.add(jobStatusPanel,gbc);
        this.add(jobTrackingPanel,gbc);

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
