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

        WebResource webResource = client.resource("http://localhost:9090");
        ClientResponse responseMsg = webResource.path("jobs")
                .queryParam("list1", "one")
                .queryParam("list2", "two")
                .queryParam("list3", "three")
                .post(ClientResponse.class);  // you just change this call from post to get
        String output = responseMsg.getEntity(String.class);


        ClientResponse responseMsg2 = webResource.path("jobs")
                .queryParam("list1", "one")
                .queryParam("list2", "two")
                .queryParam("list3", "three")
                .get(ClientResponse.class);     // the change implemented
        String output2 = responseMsg2.getEntity(String.class);


        System.out.println(responseMsg);
        System.out.println(output);
        System.out.println(output2);

    }
}
