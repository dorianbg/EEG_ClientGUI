package cz.zcu.kiv;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.yarn.webapp.hamlet.HamletSpec;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.net.URI;

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
 * Const, 2017/06/28 19:15 Dorian Beganovic
 *
 **********************************************************************************************************************/
public class Const {

    private static Log logger = LogFactory.getLog(Const.class);


    // 0. Separators
    public static String localSeparator = "";
    static {
        if (SystemUtils.IS_OS_WINDOWS) {
            localSeparator = "\\";
        }
        else {
            localSeparator = "/";
        }
    }

    public static String hadoopSeparator = "/";

    // 1.
    public static String uriPrefix;
    public static String remoteUriPrefix;
    public static String localUriPrefix;
    private static String useLocalMode;

    // 2.
    public static  String homeDirectory;

    // 3. initialize all settings from the json files
    public static void initializeValues(){
        {
            System.setProperty("hadoop.home.dir", "/");
            try{
                JSONParser parser = new JSONParser();
                Object obj = parser.parse(new FileReader("src/main/resources/settings.json".replace("/", Const.localSeparator)));
                JSONObject jsonObject = (JSONObject) obj;

                homeDirectory = (String) jsonObject.get("homeDirectory");
                localUriPrefix = (String) jsonObject.get("localUriPrefix");
                remoteUriPrefix = (String) jsonObject.get("remoteUriPrefix");
                useLocalMode = (String) jsonObject.get("useLocalMode");

                logger.info("Successfully read the input file");
                if(useLocalMode.equals("true")){
                    uriPrefix= localUriPrefix;
                }
                else {
                    uriPrefix= remoteUriPrefix;
                }
            }
            catch (Exception e) {
                logger.info("Error when reading the input file, reverting to default settings");

                remoteUriPrefix = "webhdfs://147.228.63.46:50070";
                localUriPrefix = "hdfs://localhost:8020";
                homeDirectory = "/user/digitalAssistanceSystem/data/numbers";
                uriPrefix = remoteUriPrefix;
                useLocalMode = "false";

            }
            logger.info("homeDirectory =" + homeDirectory );
            logger.info("localUriPrefix =" + localUriPrefix );
            logger.info("remoteUriPrefix =" + remoteUriPrefix );
            logger.info("useLocalMode =" + useLocalMode );
            logger.info("uriPrefix =" + uriPrefix);
        }
    }
    static {
        initializeValues();
    }



    public static String getUseLocalMode(){
        return useLocalMode;
    }
    public static void setUseLocalMode(String setting){
        Const.useLocalMode = setting;
        if(useLocalMode.equals("true")){
            uriPrefix= localUriPrefix;
        }
        else {
            uriPrefix= remoteUriPrefix;
        }
        logger.info("Changed useLocalMode property; new settings are:");
        logger.info("homeDirectory =" + homeDirectory );
        logger.info("localUriPrefix =" + localUriPrefix );
        logger.info("remoteUriPrefix =" + remoteUriPrefix );
        logger.info("useLocalMode =" + useLocalMode );
        logger.info("uriPrefix =" + uriPrefix);
    }


    // 3.
    private static FileSystem fileSystem = null;

    public Const() throws IOException, ParseException {
    }

    public static FileSystem getHadoopFileSystem() {
        if (fileSystem == null) {
            Configuration conf = new Configuration();
            conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
            conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
            try {
                fileSystem = FileSystem.get(URI.create(uriPrefix+ homeDirectory), conf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileSystem;
    }

    public static void changeFileSystem(){
        if (fileSystem != null) {
            Configuration conf = new Configuration();
            conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
            conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
            try {
                Const.fileSystem = FileSystem.get(URI.create(uriPrefix+ homeDirectory), conf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        logger.info("homeDirectory =" + homeDirectory );
        logger.info("localUriPrefix =" + localUriPrefix );
        logger.info("remoteUriPrefix =" + remoteUriPrefix );
        logger.info("useLocalMode =" + useLocalMode );
        logger.info("uriPrefix =" + uriPrefix);
    }


}
