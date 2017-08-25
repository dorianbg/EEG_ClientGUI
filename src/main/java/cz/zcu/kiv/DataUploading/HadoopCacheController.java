package cz.zcu.kiv.DataUploading;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import cz.zcu.kiv.Const;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

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
 * HadoopCacheController, 2017/07/22 13:53 dbg
 *
 **********************************************************************************************************************/
@Deprecated
public class HadoopCacheController {

    private static Log logger = LogFactory.getLog(HadoopCacheController.class);
    private static Preferences preferences = Preferences.userRoot().node(Const.class.getName());
    private static ArrayList<HadoopFile> cachedHadoopFilesList = new ArrayList<HadoopFile>();
    private static final String[] columns = {"File name", "Owner", "Size", "Date modified"};


    /**
     * if the preferences are not filled with cached hadoop files, then load them from the serialized file
     */
    private static void initializeCache() {
        int count = 0;
        try {
            for (String key : preferences.keys()) {
                if (key.startsWith("hadoopCache")) {
                    count++;
                }
            }
            logger.info("hadoopCache keys count is: " + count);
            if (count <= 10) {
                initializePreferencesFromSerialized();
            }
            initializeHadoopCacheFromPreferences();

        } catch (BackingStoreException e) {
            e.printStackTrace();
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * reads the ArrayList of HadoopFiles from the serialized store and loads it into preferences
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static void initializePreferencesFromSerialized() throws IOException, ClassNotFoundException {
        logger.info("loading hadoopCache from serialized file");
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream("hadoopFilesList.obj");
        ObjectInputStream objectInputStream = new ObjectInputStream(is);
        ArrayList<HadoopFile> paths = (ArrayList<HadoopFile>) objectInputStream.readObject();

        ObjectMapper mapper = new ObjectMapper();

        String key = "hadoopCache";
        int cnt;
        String jsonInString = mapper.writeValueAsString(paths);
        int size = jsonInString.length();
        if (size > Preferences.MAX_VALUE_LENGTH) {
            cnt = 1;
            for (int idx = 0; idx < size; cnt++) {
                if ((size - idx) > Preferences.MAX_VALUE_LENGTH) {
                    preferences.put(key + "." + cnt, jsonInString.substring(idx, idx + Preferences.MAX_VALUE_LENGTH));
                    idx += Preferences.MAX_VALUE_LENGTH;
                } else {
                    preferences.put(key + "." + cnt, jsonInString.substring(idx));
                    idx = size;
                }
            }
        } else {
            preferences.put(key, jsonInString);
        }
    }


    /**
     * loads the ArrayList of HadoopFiles from the Java Preferences
     *
     * @throws IOException
     * @throws BackingStoreException
     */
    private static void initializeHadoopCacheFromPreferences() throws IOException, BackingStoreException {
        int count = 0;
        for (String key : preferences.keys()) {
            if (key.startsWith("hadoopCache")) {
                count++;
            }
        }
        logger.info("loading hadoopCache from preferences");
        StringBuilder jsonValue = new StringBuilder();
        for (int i = 0; i <= count; i++) {
            jsonValue.append(preferences.get("hadoopCache." + i, ""));
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        cachedHadoopFilesList = mapper.readValue(jsonValue.toString(), mapper.getTypeFactory().constructCollectionType(ArrayList.class, HadoopFile.class));
    }

    /**
     * recursively visits all folders starting from a path
     *
     * @param baseDirectoryPath the starting path
     * @return return the list of files found in the path
     */
    private static ArrayList<HadoopFile> recursivelyListFilesInDirectory(String baseDirectoryPath) {
        ArrayList<HadoopFile> paths = new ArrayList<HadoopFile>(100);
        FileSystem fs = Const.getHadoopFileSystem();
        FileStatus[] files;
        try {
            files = fs.listStatus(new Path(baseDirectoryPath));
            for (FileStatus file : files) {
                paths.add(new HadoopFile(file));
                if (file.isDirectory()) {
                    // use recursion to add all the files to the main list of files
                    logger.info("Exploring folder " + file.getPath());
                    paths.addAll(recursivelyListFilesInDirectory(file.getPath().toString()));
                }
                logger.info("Exploring file " + file.getPath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return paths;
    }

    /**
     * uses the Hadoop Java API to list all the files
     *
     * @param baseDirectoryPath look for files in this folder
     * @return list of HadoopFile's
     */
    public static List<HadoopFile> getHadoopFilesListFromJava(String baseDirectoryPath) {
        return recursivelyListFilesInDirectory(baseDirectoryPath);
    }

    /**
     * connects to the REST server to get the files
     *
     * @param baseDirectoryPath
     * @return
     */
    public static List<HadoopFile> getHadoopFilesListFromREST(String baseDirectoryPath) {
        Client client = Client.create();
        // change the resource address to our server
        String serverURI = "http://localhost:8080/";
        WebResource webResource = client.resource(serverURI);

        logger.info("Creating the client from server " + serverURI);


        String basePath = "hdfs/"; // this is required for the API

        // changeable
        String pathVariable = baseDirectoryPath; //root of the fs

        ClientResponse responseMsg =
                webResource
                        .path(basePath + pathVariable.replace("/", ","))
                        .get(ClientResponse.class);  // you just change this call from post to get

        String output = responseMsg.getEntity(String.class);

        logger.info("Received the JSON representation of files");

        ObjectMapper mapper = new ObjectMapper();

        List<HadoopFile> hadoopFileList =
                null;
        try {
            hadoopFileList = mapper.readValue(output, mapper.getTypeFactory().constructCollectionType(ArrayList.class, HadoopFile.class));
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.info("Successfully loaded the hadoop files");

        return hadoopFileList;
    }


    /**
     * save the list of cached HadoopFiles into the java preferences
     *
     * @param baseDirectoryPath
     */
    public static void cacheHadoopFilesIntoPreferences(String baseDirectoryPath) {
        List<HadoopFile> paths = getHadoopFilesListFromJava(baseDirectoryPath);
        ObjectMapper mapper = new ObjectMapper();
        String key = "hadoopCache";
        int cnt;
        try {
            // removes all previous hadoopCache keys
            for (String keyTemp : preferences.keys()) {
                if (keyTemp.startsWith("hadoopCache")) {
                    preferences.remove(keyTemp);
                }
            }
            logger.info("Cleared previous preferences cache");
            // now write the json value of the arraylist as preferences
            String jsonInString = mapper.writeValueAsString(paths);
            int size = jsonInString.length();
            if (size > Preferences.MAX_VALUE_LENGTH) {
                cnt = 1;
                for (int idx = 0; idx < size; cnt++) {
                    if ((size - idx) > Preferences.MAX_VALUE_LENGTH) {
                        preferences.put(key + "." + cnt, jsonInString.substring(idx, idx + Preferences.MAX_VALUE_LENGTH));
                        idx += Preferences.MAX_VALUE_LENGTH;
                    } else {
                        preferences.put(key + "." + cnt, jsonInString.substring(idx));
                        idx = size;
                    }
                }
            } else {
                preferences.put(key, jsonInString);
            }
            logger.info("Filled the new preferences");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    /**
     * saves the list of HadoopFiles into a serialized file (object)
     *
     * @param baseDirectoryPath caches the files in sub-folders of this path
     */
    public static void cacheHadoopFilesIntoSerialized(String baseDirectoryPath) {
        ArrayList<HadoopFile> paths = recursivelyListFilesInDirectory(baseDirectoryPath);
        logger.info("Read all the files");
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream("src/main/resources/hadoopFilesList.obj".replace("/", Const.localSeparator));
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(paths);
            objectOutputStream.close();
            fileOutputStream.close();
            logger.info("Saved the files into serialized object");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Filter the list (cache) of current Hadoop files
     *
     * @param screen updates the data shown on screen
     */
    public static void getCachedHadoopData(final HadoopScreen screen) {
        SwingWorker<String[][], Void> swingWorker = new SwingWorker<String[][], Void>() {
            @Override
            protected String[][] doInBackground() throws Exception {
                // 1. get the current path and clean it
                String desiredPath = screen.getPath();

                if (!desiredPath.endsWith("/")) {
                    desiredPath += "/";
                }
                logger.info("Desired path is " + desiredPath);

                // 2. add cached paths that match the current path
                String[][] data = null;

                List<HadoopFile> hadoopFiles = new ArrayList<HadoopFile>(50);

                for (HadoopFile file : cachedHadoopFilesList) {
                    try {
                        if (!file.getPath().split(desiredPath)[1].contains("/")) {
                            hadoopFiles.add(file);
                            logger.debug("Found folder " + file.getPath());
                        }
                    } catch (ArrayIndexOutOfBoundsException e) {
                        logger.debug("Failed to split the file: " + file.getPath() + " based on " + desiredPath);
                    }
                }

                // 3. populate the data matrix
                data = new String[hadoopFiles.size()][4];
                for (int i = 0; i < hadoopFiles.size(); i++) {
                    HadoopFile file = hadoopFiles.get(i);
                    data[i][0] = file.getFileName();
                    // 2. fileowner
                    data[i][1] = file.getOwner();
                    // 3. folder/file size
                    data[i][2] = file.getSize();
                    // data modified
                    data[i][3] = file.getDateModified();
                }
                return data;
            }

            @Override
            protected void done() {
                logger.info("Done with getting cached hadoop data in background...");
                logger.info("Created the data (String[][] matrix) for cached data");

                String[][] fullData = null;
                try {
                    // this gets the result of doInBackground() method above
                    fullData = get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                // this is the base case when the table is empty
                if (screen.getData().length == 0) {
                    logger.info("Set the cached data (String[][]) matrix onto JTable ");
                    screen.setData(fullData);
                    screen.getTableModel().setDataVector(fullData, columns);
                    screen.getTableModel().fireTableDataChanged();
                }
            }
        };
        swingWorker.execute();
    }


    @Test
    public void cacheFilesTest() {
        cacheHadoopFilesIntoPreferences("/user/digitalAssistanceSystem/data/numbers");
        //cacheHadoopFilesIntoSerialized(Const.homeDirectory);
    }

    @Test
    public void cacheFilesTest2() {
        cacheHadoopFilesIntoSerialized("/user/digitalAssistanceSystem/data/numbers");

    }

    @Test
    public void testReceiveListOfStrings() throws Exception {
        Client client = Client.create();
        WebResource webResource = client.resource("http://localhost:8080/");

        String basePath = "hdfs/";
        String pathVariable = "/"; //root of the fs
        ClientResponse responseMsg =
                webResource
                        .path(basePath + pathVariable.replace("/", ","))
                        .get(ClientResponse.class);  // you just change this call from post to get

        String output = responseMsg.getEntity(String.class);

        ObjectMapper mapper = new ObjectMapper();

        List<HadoopFile> hadoopFileList =
                mapper.readValue(output, mapper.getTypeFactory().constructCollectionType(ArrayList.class, HadoopFile.class));

        logger.info("First file name");
        for (int i = 0; i < 25; i++) {
            logger.info(hadoopFileList.get(i).getFileName());
            logger.info(hadoopFileList.get(i).getPath());
            logger.info(hadoopFileList.get(i).getSize());
            logger.info(hadoopFileList.get(i).getDateModified());
            logger.info(hadoopFileList.get(i).getOwner());
        }
    }

}
