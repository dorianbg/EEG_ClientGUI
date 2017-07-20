package cz.zcu.kiv;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import cz.zcu.kiv.DataUploading.HadoopFile;
import cz.zcu.kiv.DataUploading.HadoopScreen;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.*;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import javax.swing.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static cz.zcu.kiv.Const.uriPrefix;
import static java.lang.System.out;

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
 * HadoopController, 2017/06/28 13:37 Dorian Beganovic
 *
 **********************************************************************************************************************/
public class HadoopController {

    private static Log logger = LogFactory.getLog(HadoopController.class);
    private static Preferences preferences = Preferences.userRoot().node(Const.class.getName());
    private static ArrayList<HadoopFile> cachedHadoopFilesList = new ArrayList<HadoopFile>();
    public static final String[] columns = {"File name", "Owner", "Size","Date modified"};


    // initialize the cachedHadoopFilesList
    /*
    static {
        int count = 0;
        try {
            for (String key : preferences.keys()){
                if (key.startsWith("hadoopCache")){
                    count++;
                }
            }
            logger.info("hadoopCache keys count is: " + count);
            if(count <= 10){
                initializeHadoopCacheFromSerialized();
            }
            else {
                initializeHadoopCacheFromPreferences();
            }

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
    */

    public static void initializeHadoopCacheFromSerialized() throws IOException, ClassNotFoundException {
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
            for(int idx = 0 ; idx < size ; cnt++) {
                if ((size - idx) > Preferences.MAX_VALUE_LENGTH) {
                    preferences.put(key + "." + cnt, jsonInString.substring(idx,idx+Preferences.MAX_VALUE_LENGTH));
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


    public static void initializeHadoopCacheFromPreferences() throws IOException, BackingStoreException {
        int count=0;
        for (String key : preferences.keys()){
            if (key.startsWith("hadoopCache")){
                count++;
            }
        }
        logger.info("loading hadoopCache from preferences");
        StringBuilder jsonValue= new StringBuilder();
        for (int i = 0; i <= count; i++){
            jsonValue.append(preferences.get("hadoopCache." + i, ""));
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        cachedHadoopFilesList = mapper.readValue(jsonValue.toString(), mapper.getTypeFactory().constructCollectionType(ArrayList.class, HadoopFile.class));
    }

    private static ArrayList<HadoopFile> recursivelyListFilesInDirectory(String baseDirectoryPath){
        ArrayList<HadoopFile> paths = new ArrayList<HadoopFile>(100);
        FileSystem fs = Const.getHadoopFileSystem();
        FileStatus[] files;
        try {
            files = fs.listStatus(new Path(baseDirectoryPath));
            for (FileStatus file : files){
                paths.add(new HadoopFile(file));
                if(file.isDirectory()){
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

    public static List<HadoopFile> getHadoopFilesListFromJava(String baseDirectoryPath){
        return recursivelyListFilesInDirectory(baseDirectoryPath);
    }

    /**
     *
     * @param baseDirectoryPath
     * @return
     */
    public static List<HadoopFile> getHadoopFilesListFromREST(String baseDirectoryPath){
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
                        .path(basePath + pathVariable.replace("/",","))
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
     * @param baseDirectoryPath
     */
    public static void cacheHadoopFilesIntoPreferences(String baseDirectoryPath){
        List<HadoopFile> paths = getHadoopFilesListFromJava(baseDirectoryPath);
        ObjectMapper mapper = new ObjectMapper();
        String key = "hadoopCache";
        int cnt;
        try {
            // removes all previous hadoopCache keys
            for (String keyTemp : preferences.keys()){
                if (keyTemp.startsWith("hadoopCache")){
                    preferences.remove(keyTemp);
                }
            }
            logger.info("Cleared previous preferences cache");
            // now write the json value of the arraylist as preferences
            String jsonInString = mapper.writeValueAsString(paths);
            int size = jsonInString.length();
            if (size > Preferences.MAX_VALUE_LENGTH) {
                cnt = 1;
                for(int idx = 0 ; idx < size ; cnt++) {
                    if ((size - idx) > Preferences.MAX_VALUE_LENGTH) {
                        preferences.put(key + "." + cnt, jsonInString.substring(idx,idx+Preferences.MAX_VALUE_LENGTH));
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
     * @param baseDirectoryPath
     */
    public static void cacheHadoopFilesIntoSerialized(String baseDirectoryPath){
        ArrayList<HadoopFile> paths = recursivelyListFilesInDirectory(baseDirectoryPath);
        logger.info("Read all the files");
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream("src/main/resources/hadoopFilesList.obj".replace("/",Const.localSeparator));
            ObjectOutputStream objectOutputStream= new ObjectOutputStream(fileOutputStream);
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
     * Used to update the JTable model with live Hadoop data
     * @param screen
     */
    public static void getHadoopData(final HadoopScreen screen){
        SwingWorker<String[][],Void> swingWorker = new SwingWorker<String[][], Void>() {
            @Override
            protected String[][] doInBackground() throws Exception {

                String path = screen.getPath();
                logger.info("Getting hadoop data in background...");
                String[][] data = null;

                String uri = uriPrefix + path;
                FileSystem fs = Const.getHadoopFileSystem();
                try {

                    FileStatus[] files = fs.listStatus(new Path(uri));

                    data = new String[files.length][columns.length];

                    for (int i = 0; i < files.length; i++){
                        // 1. filename
                        data[i][0]= files[i].getPath().toString().substring(files[i].getPath().toString().lastIndexOf(Const.hadoopSeparator)).replaceFirst(Const.hadoopSeparator,"");
                        // 2. fileowner
                        data[i][1]= files[i].getOwner();
                        // 3. folder/file size
                        data[i][2] = Long.toString(files[i].getLen()/ 1024 ) + " kb";
                        //data[i][2]= Long.toString(fs.getContentSummary(files[i].getPath()).getSpaceConsumed() / (1024 * 1024)) + " mb";
                        // data modified
                        data[i][3]= new SimpleDateFormat("dd.MM.yyyy").format(new java.util.Date(files[i].getModificationTime()));
                        // date accessed
                        //data[i][4]= new SimpleDateFormat("dd.MM.yyyy").format(new java.util.Date(files[i].getAccessTime()));
                    }

                }
                catch (Exception e){
                    out.println(e.getMessage());
                }
                return data;
            }

            @Override
            protected void done() {
                logger.info("Done with getting hadoop data in background...");
                logger.info("Created the data (String[][] matrix) for hadoop data");

                String[][] fullData =null;
                try {
                    fullData = get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }


                final int[] sel = screen.getJTable().getSelectedRows();

                screen.setData(fullData);
                logger.info("Set the hadoop data (String[][]) matrix onto JTable ");

                screen.getTableModel().setDataVector(fullData,columns);
                screen.getTableModel().fireTableDataChanged();

                // re-apply selected rows in jtable
                for (int i=0; i<sel.length; i++){
                    screen.getJTable().getSelectionModel().addSelectionInterval(sel[i], sel[i]);
                }
            }
        };
        swingWorker.execute();
    }

    /**
     * Used to filter list (cache) of current Hadoop files
     * @param screen
     */
    public static void getCachedHadoopData(final HadoopScreen screen){
        SwingWorker<String[][],Void> swingWorker = new SwingWorker<String[][], Void>() {
            @Override
            protected String[][] doInBackground() throws Exception {
                String desiredPath = screen.getPath();

                if(!desiredPath.endsWith("/")){
                    desiredPath+="/";
                }
                logger.info("Desired path is " + desiredPath);
                String[][] data = null;

                List<HadoopFile> hadoopFiles = new ArrayList<HadoopFile>(50);

                for (HadoopFile file : cachedHadoopFilesList) {
                    try {
                        if (!file.getPath().split(desiredPath)[1].contains("/")) {
                            hadoopFiles.add(file);
                            logger.debug("Found folder " + file.getPath());
                        }
                    }
                    catch (ArrayIndexOutOfBoundsException e){
                        logger.debug("Failed to split the file: " + file.getPath() + " based on " + desiredPath);
                    }
                }
                // populate the data matrix
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

                String[][] fullData =null;
                try {
                    // this gets the result of doInBackground() method above
                    fullData = get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                // this is the base case when the table is empty
                if(screen.getData().length == 0){
                    logger.info("Set the cached data (String[][]) matrix onto JTable ");
                    screen.setData(fullData);
                    screen.getTableModel().setDataVector(fullData,columns);
                    screen.getTableModel().fireTableDataChanged();

                }
            }
        };
        swingWorker.execute();
    }


    /**
     * recursively copies from path A to path B preserving the internal structure
     * @param files
     * @param fs
     * @param destDirPath
     * @param list
     * @param screen
     * @throws IOException
     */
    public static void copyFilesToDir(final File[] files, final FileSystem fs, final String destDirPath, final JList list, final HadoopScreen screen)
            throws IOException {
        SwingWorker<Void,String> swingWorker = new SwingWorker<Void, String>() {

            @Override
            protected Void doInBackground() throws Exception {
                logger.info("Copying all the files from directory ");
                for (File file :  files) {
                    if(!file.getName().startsWith(".")){
                        logger.info("SRC: " + file.getPath());
                        logger.info("DEST: " + destDirPath + Const.hadoopSeparator + file.getName());
                        fs.copyFromLocalFile(new Path(file.getPath()),new Path(destDirPath, file.getName()));
                        String text = "SRC: " + file.getPath() + " \n" + "DEST: " + destDirPath + Const.hadoopSeparator + file.getName();
                        publish(text);
                    }
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                String lastLine= chunks.get(chunks.size()-1);
                String source = lastLine.split("\n")[0];
                String dest = lastLine.split("\n")[1];
                logger.info("Chunk" + lastLine);
                DefaultListModel listModel = (DefaultListModel) list.getModel();
                listModel.addElement(source);
                listModel.addElement(dest);
                listModel.addElement("-> Copied");
            }

            @Override
            protected void done() {
                list.revalidate();
                DefaultListModel listModel = (DefaultListModel) list.getModel();
                listModel.addElement("------> Done with copying");
                logger.info("Done with copying data to hadoop...");
                HadoopController.getHadoopData(screen);
            }
        };
        swingWorker.execute();
    }

    /**
     * recursively deletes a path
     * @param filePath
     * @param fs
     */
    public static void deleteFile(String filePath, FileSystem fs)  {
        logger.info("Deleting file " + filePath);
        try {
            // the false flags means you can only delete files and not folders
            fs.delete(new Path(filePath),true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void cacheFilesTest(){
        cacheHadoopFilesIntoPreferences("/user/digitalAssistanceSystem/data/numbers");
        //cacheHadoopFilesIntoSerialized(Const.homeDirectory);
    }

    @Test
    public void cacheFilesTest2(){
        cacheHadoopFilesIntoSerialized("/user/digitalAssistanceSystem/data/numbers");

    }
}
