package cz.zcu.kiv.DataUploading;

import cz.zcu.kiv.Const;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.*;

import javax.swing.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
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
 * HadoopHdfsController, 2017/06/28 13:37 Dorian Beganovic
 *
 **********************************************************************************************************************/
public class HadoopHdfsController {

    private static Log logger = LogFactory.getLog(HadoopHdfsController.class);
    private static Preferences preferences = Preferences.userRoot().node(Const.class.getName());
    private static ArrayList<HadoopFile> cachedHadoopFilesList = new ArrayList<HadoopFile>();
    public static final String[] columns = {"File name", "Owner", "Size", "Date modified"};


    /**
     * Used to update the JTable model with live Hadoop data
     *
     * @param screen
     */
    public static void getHadoopData(final HadoopScreen screen) {
        SwingWorker<String[][], Void> swingWorker = new SwingWorker<String[][], Void>() {
            @Override
            protected String[][] doInBackground() throws Exception {

                String path = screen.getPath();
                logger.info("Getting hadoop data in background...");
                String[][] data = null;

                String uri = uriPrefix + path;
                FileSystem fs = Const.getHadoopFileSystem();
                try {

                    // list files in a folder
                    FileStatus[] files = fs.listStatus(new Path(uri));

                    // create the data array for JTable
                    data = new String[files.length][columns.length];

                    for (int i = 0; i < files.length; i++) {
                        // 1. filename
                        data[i][0] = files[i].getPath().toString().substring(files[i].getPath().toString().lastIndexOf(Const.hadoopSeparator)).replaceFirst(Const.hadoopSeparator, "");
                        // 2. fileowner
                        data[i][1] = files[i].getOwner();
                        // 3. folder/file size
                        data[i][2] = Long.toString(files[i].getLen() / 1024) + " kb";
                        //data[i][2]= Long.toString(fs.getContentSummary(files[i].getPath()).getSpaceConsumed() / (1024 * 1024)) + " mb";
                        // data modified
                        data[i][3] = new SimpleDateFormat("dd.MM.yyyy").format(new java.util.Date(files[i].getModificationTime()));
                    }

                } catch (Exception e) {
                    logger.info(e.getMessage());
                }
                return data;
            }

            // when the above process is done, update the JTable data
            @Override
            protected void done() {
                logger.info("Done with getting hadoop data in background...");
                logger.info("Created the data (String[][] matrix) for hadoop data");

                String[][] fullData = null;
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

                screen.getTableModel().setDataVector(fullData, columns);
                screen.getTableModel().fireTableDataChanged();

                // re-apply selected rows in jtable
                for (int i = 0; i < sel.length; i++) {
                    screen.getJTable().getSelectionModel().addSelectionInterval(sel[i], sel[i]);
                }
            }
        };
        swingWorker.execute();
    }


    /**
     * recursively copies from path A to path B preserving the internal structure
     *
     * @param files       list of local files to be copied
     * @param fs          filesystem to which to copy to
     * @param destDirPath desired directory path
     * @param list        list which is updated during the file copy process
     * @param screen      screen which gets data updated
     * @throws IOException
     */
    public static void copyFilesToDir(final File[] files, final FileSystem fs, final String destDirPath, final JList list, final HadoopScreen screen)
            throws IOException {
        SwingWorker<Void, String> swingWorker = new SwingWorker<Void, String>() {

            // copy the files
            @Override
            protected Void doInBackground() throws Exception {
                logger.info("Copying all the files from directory ");
                for (File file : files) {
                    if (!file.getName().startsWith(".")) {
                        logger.info("SRC: " + file.getPath());
                        logger.info("DEST: " + destDirPath + Const.hadoopSeparator + file.getName());
                        fs.copyFromLocalFile(new Path(file.getPath()), new Path(destDirPath, file.getName()));
                        String text = "SRC: " + file.getPath() + " \n" + "DEST: " + destDirPath + Const.hadoopSeparator + file.getName();
                        publish(text);
                    }
                }
                return null;
            }

            // updates the list every time publish() is executed within doInBackground()
            @Override
            protected void process(List<String> chunks) {
                String lastLine = chunks.get(chunks.size() - 1);
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
                HadoopHdfsController.getHadoopData(screen);
            }
        };
        swingWorker.execute();
    }

    /**
     * recursively deletes a path
     *
     * @param filePath path on which to delete the files
     * @param fs       filesystem on which to delete the files
     */
    public static void deleteFile(String filePath, FileSystem fs) {
        logger.info("Deleting file " + filePath);
        try {
            // the false flags means you can only delete files and not folders
            fs.delete(new Path(filePath), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
