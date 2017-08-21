package cz.zcu.kiv;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.security.UserGroupInformation;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
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
 * Const, 2017/06/28 19:15 Dorian Beganovic
 *
 **********************************************************************************************************************/
public class Const {

    private static Log logger = LogFactory.getLog(Const.class);

    // if you get an error with initializing preferences on windows see https://stackoverflow.com/questions/16428098/groovy-shell-warning-could-not-open-create-prefs-root-node
    private static Preferences preferences = Preferences.userRoot().node(Const.class.getName());
    public static final String hadoopSeparator = "/";


    public static final String localhostUri = "http://localhost:8990";
    public static final String remoteHostUri = "http://147.228.63.46:8990";
    public static String serverConnectionUri;

    public static String localSeparator = "";
    public static String uriPrefix;
    public static String remoteUriPrefix;
    static String localUriPrefix;
    public static String homeDirectory;
    private static String useLocalMode;
    public static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    public static double screenSizeWidth = screenSize.getWidth();
    public static double screenSizeHeight = screenSize.getHeight();

    private static FileSystem fileSystem = null;

    /**
     * deploys the application to windows os by properly placing the required winutils.exe file
     */
    private static void configureOS() {
        if (SystemUtils.IS_OS_WINDOWS) {
            localSeparator = "\\";
            String homeDirectory = System.getProperty("user.home");
            // windows deployment !
            System.out.printf(homeDirectory);
            if (!new File(homeDirectory + "/Apache_Hadoop_Client/bin/winutils").exists()) {
                new File(homeDirectory + "/Apache_Hadoop_Client").mkdirs();
                new File(homeDirectory + "/Apache_Hadoop_Client/bin").mkdirs();
                URL inputUrl = Const.class.getResource("/winutils.exe");
                File dest = new File(homeDirectory + "/Apache_Hadoop_Client/bin/winutils.exe");
                try {
                    FileUtils.copyURLToFile(inputUrl, dest);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Done");
            }
            System.setProperty("hadoop.home.dir", homeDirectory + "/Apache_Hadoop_Client");

        } else {
            localSeparator = "/";
            System.setProperty("hadoop.home.dir", "/");
        }
    }

    static {
        configureOS();
    }


    /**
     * initializes the values of setting from the Java preferences API
     */
    static void initializeValues() {
        {
            homeDirectory = preferences.get("homeDirectory", "/user/digitalAssistanceSystem/data/numbers");
            localUriPrefix = preferences.get("localUriPrefix", "hdfs://localhost:8020");
            remoteUriPrefix = preferences.get("remoteUriPrefix", "webhdfs://147.228.63.46:50070");
            useLocalMode = preferences.get("useLocalMode", "false");

            logger.info("Successfully read the preferences");
            if (useLocalMode.equals("true")) {
                serverConnectionUri = localhostUri;
                uriPrefix = localUriPrefix;
            } else {
                serverConnectionUri = remoteHostUri;
                uriPrefix = remoteUriPrefix;
            }
            logPreferences();
        }
    }

    static {
        initializeValues();
    }


    /**
     * @return useLocalMode value (string)
     */
    public static String getUseLocalMode() {
        return useLocalMode;
    }

    /**
     * @param setting string "true" or "false"
     */
    public static void setUseLocalMode(String setting) {
        Const.useLocalMode = setting;
        if (useLocalMode.equals("true")) {
            uriPrefix = localUriPrefix;
        } else {
            uriPrefix = remoteUriPrefix;
        }
        logPreferences();
    }

    /**
     * singleton constructor
     *
     * @return the reference to single instance of FileSystem (Hadoop FileSystem)
     */
    public static FileSystem getHadoopFileSystem() {
        if (fileSystem == null) {
            Configuration conf = new Configuration();
            conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
            conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
            conf.set("fs.swebhdfs.impl", org.apache.hadoop.hdfs.web.WebHdfsFileSystem.class.getName());
            conf.setBoolean("fs.automatic.close", true);

            /*
            conf.set("hadoop.security.authentication", "kerberos");
            conf.set("dfs.namenode.kerberos.principal.pattern", "hdfs/quickstart.cloudera@CLOUDERA");

            UserGroupInformation.setConfiguration(conf);
            try {
                UserGroupInformation.getLoginUser().checkTGTAndReloginFromKeytab();
            } catch (IOException e) {
                e.printStackTrace();
            }
            */

            try {
                fileSystem = FileSystem.get(URI.create(uriPrefix + homeDirectory), conf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileSystem;
    }

    /**
     * used when the user chooses local mode (in the setting panel)
     */
    public static void changeFileSystem() {
        if (fileSystem != null) {
            Configuration conf = new Configuration();
            conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
            conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
            conf.set("fs.swebhdfs.impl", org.apache.hadoop.hdfs.web.WebHdfsFileSystem.class.getName()); // a crazy fix

            conf.setBoolean("fs.automatic.close", true);
            try {
                Const.fileSystem = FileSystem.get(URI.create(uriPrefix + homeDirectory), conf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        logPreferences();
    }

    /**
     * prints the preferences to the log output
     */
    public static void logPreferences() {
        logger.info("homeDirectory =" + homeDirectory);
        logger.info("localUriPrefix =" + localUriPrefix);
        logger.info("remoteUriPrefix =" + remoteUriPrefix);
        logger.info("useLocalMode =" + useLocalMode);
        logger.info("uriPrefix =" + uriPrefix);
    }


}
