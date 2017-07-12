package cz.zcu.kiv.DataUploading;

import cz.zcu.kiv.Const;
import org.apache.hadoop.fs.FileStatus;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;

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
 * HadopFile, 2017/06/30 15:30 Dorian Beganovic
 *
 **********************************************************************************************************************/
public class HadoopFile implements Serializable {

    private String path;
    private String fileName;
    private String owner;
    private String size;
    private String dateModified;
    private String dateCreated;
    private boolean isDirectory;
    public HadoopFile(FileStatus file) throws IOException {
        this.fileName= file.getPath().toString().substring(file.getPath().toString().lastIndexOf(Const.hadoopSeparator)).replaceFirst(Const.hadoopSeparator,"");
        this.path = file.getPath().toString();
        this.owner= file.getOwner();
        this.size= Long.toString(Const.getHadoopFileSystem().getContentSummary(file.getPath()).getSpaceConsumed() / (1024 * 1024)) + " mb";
        this.dateModified= new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new java.util.Date(file.getModificationTime()));
        this.dateCreated= new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new java.util.Date(file.getAccessTime()));
        this.isDirectory = file.isDirectory();
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public String getFileName() {
        return fileName;
    }

    public String getOwner() {
        return owner;
    }

    public String getSize() {
        return size;
    }

    public String getDateModified() {
        return dateModified;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public String getPath() {
        return path;
    }
}
