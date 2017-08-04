package cz.zcu.kiv.Analysis;

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
 * JerseyRESTClient, 2017/07/13 14:09 Dorian Beganovic
 *
 **********************************************************************************************************************/
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.junit.Test;

import java.util.ArrayList;


public class JerseyRESTClient {

    @Test
    public void testReceiveListOfStrings() throws Exception {
        Client client = Client.create();
        int jobId = 10001;

        WebResource webResource = client.resource("http://localhost:8080");
        ClientResponse responseMsg = webResource.path("/jobs/submit/" + jobId)
                .queryParam("config_reg_param", "0.01")
                .queryParam("config_mini_batch_fraction", "1.0")
                .queryParam("config_num_iterations", "10")
                .queryParam("train_clf", "svm")
                .queryParam("result_path", System.getProperty("user.home") + "/spark_server/results/" + jobId + ".txt")
                .queryParam("info_file", "/user/digitalAssistanceSystem/data/numbers/infoTrain.txt")
                .queryParam("config_step_size", "1.0")
                .queryParam("fe", "dwt-8")
                .get(ClientResponse.class);  // you just change this call from post to get
//        String output = responseMsg.getEntity(String.class);
        System.out.println(responseMsg);
//        System.out.println(output);

        String output2 = "";

        while (!output2.equals("FINISHED")) {
            output2 = webResource.path("/jobs/check/" + jobId).get(ClientResponse.class).getEntity(String.class);
            System.out.println(output2);
            Thread.sleep(100);
        }

        String output3 = webResource.path("/jobs/result/" + jobId).get(ClientResponse.class).getEntity(String.class);
        System.out.println(output3);
    }
}
