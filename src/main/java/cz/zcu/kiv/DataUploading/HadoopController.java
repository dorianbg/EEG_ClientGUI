package cz.zcu.kiv.DataUploading;

import cz.zcu.kiv.Const;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.junit.Test;

import javax.swing.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static cz.zcu.kiv.Const.homeDirectory;
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

    private static ArrayList<HadoopFile> arraylist= new ArrayList<HadoopFile>();

    static {
        try{
            FileInputStream fileInputStream = new FileInputStream("src/main/resources/hadoopFilesList.obj".replace("/",Const.localSeparator));
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            arraylist = (ArrayList) objectInputStream.readObject();
            logger.info("Loaded the serialized file");
            objectInputStream.close();
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static final String[] columns = {"File name", "Owner", "Size","Data modified", "Data Created"};

    public static String[] getColumns(){
        return columns;
    }

    public static String[][] listFoldersInDirectory(String path){

        String[][] data = null;

        String uri = uriPrefix + path;
        Configuration conf = new Configuration();

        try {

            FileSystem fs = Const.getHadoopFileSystem();

            FileStatus[] filesInDir = fs.listStatus(new Path(uri));
            List<FileStatus> directories = new ArrayList<FileStatus>();

            for (FileStatus file : filesInDir){
                if(file.isDirectory()){
                    directories.add(file);
                }
            }

            data = new String[directories.size()][columns.length];

            for (int i = 0; i < directories.size(); i++){
                FileStatus files = directories.get(i);
                data[i][0]= files.getPath().toString().substring(files.getPath().toString().lastIndexOf(Const.hadoopSeparator)).replaceFirst(Const.hadoopSeparator,"");
                data[i][1]= files.getOwner();
                data[i][2]= Long.toString(fs.getContentSummary(files.getPath()).getSpaceConsumed() / (1024 * 1024)) + " mb";
                data[i][3]= new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new java.util.Date(files.getModificationTime()));
                data[i][4]= new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new java.util.Date(files.getAccessTime()));
            }

        }
        catch (Exception e){
            out.println(e.getMessage());
        }
        return data;
    }


    public static String[][] listFilesInDirectory(String path){
        //String uri = "webhdfs://147.228.63.46:50070/user/digitalAssistanceSystem/data/";
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
                data[i][2]= Long.toString(fs.getContentSummary(files[i].getPath()).getSpaceConsumed() / (1024 * 1024)) + " mb";
                // data modified
                data[i][3]= new SimpleDateFormat("dd.MM.yyyy").format(new java.util.Date(files[i].getModificationTime()));
                // date accessed
                data[i][4]= new SimpleDateFormat("dd.MM.yyyy").format(new java.util.Date(files[i].getAccessTime()));
            }

        }
        catch (Exception e){
            out.println(e.getMessage());
        }
        return data;
    }


    public static List<HadoopFile> recursivelyListFilesInDirectory(String baseDirectoryPath){
        List<HadoopFile> paths = new ArrayList<HadoopFile>(100);
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

    public static void cacheHadoopFiles(String baseDirectoryPath){
        List<HadoopFile> paths = recursivelyListFilesInDirectory(baseDirectoryPath);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream("src/main/resources/hadoopFilesList.obj".replace("/",Const.localSeparator));
            ObjectOutputStream objectOutputStream= new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(paths);
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void deleteFile(String filePath, FileSystem fs)  {
        logger.info("Deleting file " + filePath);
        try {
            // the false flags means you can only delete files and not folders
            fs.delete(new Path(filePath),true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String[][] getCachedHadoopData(String desiredPath, String fileTypeOption){
        if(!desiredPath.endsWith("/")){
            desiredPath+="/";
        }
        logger.info("Desired path is " + desiredPath);
        String[][] data = null;

        List<HadoopFile> hadoopFiles = new ArrayList<HadoopFile>(50);

        for (HadoopFile file : arraylist) {
            try {
                if (fileTypeOption.equals("FOLDERS")) {
                    if (file.isDirectory()) {
                        if (!file.getPath().split(desiredPath)[1].contains("/")) {
                            hadoopFiles.add(file);
                            logger.debug("Found folder " + file.getPath());
                        }
                    }
                }
                else if (fileTypeOption.equals("FILES")) {
                    if(!file.getPath().split(desiredPath)[1].contains("/")){
                        hadoopFiles.add(file);
                        logger.debug("Found file " + file.getPath());
                    }
                }
                else {
                    throw new IllegalArgumentException("Choose either FOLDERS or FILES");
                }
            }
            catch (ArrayIndexOutOfBoundsException e){
                logger.debug("Failed to split the file: " + file.getPath() + " based on " + desiredPath);
            }
        }

        data = new String[hadoopFiles.size()][5];
        for (int i = 0; i < hadoopFiles.size(); i++) {
            HadoopFile file = hadoopFiles.get(i);
            data[i][0] = file.getFileName();
            // 2. fileowner
            data[i][1] = file.getOwner();
            // 3. folder/file size
            data[i][2] = file.getSize();
            // data modified
            data[i][3] = file.getDateModified();
            // date accessed
            data[i][4] = file.getDateCreated();
        }
        return data;
    }





    public static void getHadoopData(final HadoopScreen screen, final String option){
        SwingWorker<String[][],Void> swingWorker = new SwingWorker<String[][], Void>() {
            @Override
            protected String[][] doInBackground() throws Exception {
                logger.info("Getting hadoop data in background...");
                if (option.equals("FOLDERS")){
                    return listFoldersInDirectory(screen.getPath());
                }
                else if (option.equals("FILES")){
                    return listFilesInDirectory(screen.getPath());
                }
                else {
                    throw new IllegalArgumentException("Please use either FOLDERS or FILES option");
                }
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
                for (int i=0; i<sel.length; i++)
                    screen.getJTable().getSelectionModel().addSelectionInterval(sel[i], sel[i]);
            }
        };
        swingWorker.execute();
    }

    public static void getCachedHadoopData(final HadoopScreen screen, final String option){
        SwingWorker<String[][],Void> swingWorker = new SwingWorker<String[][], Void>() {
            @Override
            protected String[][] doInBackground() throws Exception {
                return HadoopController.getCachedHadoopData(screen.getPath(), option);
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
                HadoopController.getHadoopData(screen, "FILES");
            }
        };
        swingWorker.execute();
    }








    /*
    tests
     */
    @Test
    public void testWritingCachedHadoopFile(){
        List<HadoopFile> paths = recursivelyListFilesInDirectory(Const.homeDirectory);
        for (HadoopFile filePath : paths){
            logger.info(filePath.getPath());
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream("src/main/resources/hadoopFilesList.obj".replace("/",Const.localSeparator));
            logger.info("Saved the serialized object");
            ObjectOutputStream objectOutputStream= new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(paths);
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testCachedHadoopFiles(){
        String desiredPath = Const.uriPrefix + "/user/digitalAssistanceSystem/data/numbers/";

        ArrayList<HadoopFile> arraylist= new ArrayList<HadoopFile>();
        try {
            FileInputStream fileInputStream = new FileInputStream("src/main/resources/hadoopFilesList.obj".replace("/",Const.localSeparator));
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            arraylist = (ArrayList) objectInputStream.readObject();
            logger.info("Loaded the serialized file");
            objectInputStream.close();
            fileInputStream.close();
        }catch(IOException ioe){
            ioe.printStackTrace();
            return;
        }catch(ClassNotFoundException c){
            System.out.println("Class not found");
            c.printStackTrace();
            return;
        }
        for(HadoopFile file: arraylist){
            if(file.isDirectory()){
                logger.info(file.getPath());
            }
        }

    }

    @Test
    public void cacheFilesTest(){
        cacheHadoopFiles("/");
        //cacheHadoopFiles(Const.homeDirectory);
    }


    public int numCachedFiles(){
        return arraylist.size();
    }




}
