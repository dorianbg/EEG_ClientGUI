package cz.zcu.kiv.DataUploading;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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
 * HdfsClient, 2017/07/20 11:55 dbg
 *
 **********************************************************************************************************************/
public class HdfsClient {
    @Test
    public void testReceiveListOfStrings() throws Exception {
        Client client = Client.create();
        WebResource webResource = client.resource("http://localhost:8080/");

        String basePath = "hdfs/";
        String pathVariable = "/"; //root of the fs
        ClientResponse responseMsg =
                webResource
                        .path(basePath + pathVariable.replace("/",","))
                        .get(ClientResponse.class);  // you just change this call from post to get

        String output = responseMsg.getEntity(String.class);

        ObjectMapper mapper = new ObjectMapper();

        List<HadoopFile> hadoopFileList =
                mapper.readValue(output, mapper.getTypeFactory().constructCollectionType(ArrayList.class, HadoopFile.class));

        System.out.println("First file name");
        for (int i = 0; i < 25; i++){
            System.out.println(hadoopFileList.get(i).getFileName());
            System.out.println(hadoopFileList.get(i).getPath());
            System.out.println(hadoopFileList.get(i).getSize());
            System.out.println(hadoopFileList.get(i).getDateModified());
            System.out.println(hadoopFileList.get(i).getOwner());

        }
    }
}
