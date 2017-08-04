package cz.zcu.kiv.DataUploading;

import cz.zcu.kiv.Const;
import org.apache.hadoop.fs.FileStatus;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

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
 * HadoopFile, 2017/06/30 15:30 Dorian Beganovic
 *
 **********************************************************************************************************************/
@JsonIgnoreProperties(ignoreUnknown = true)
public class HadoopFile implements Serializable {

    private String path;
    private String fileName;
    private String owner;
    private String size;
    private String dateModified;
    private boolean isDirectory;

    public HadoopFile() {

    }

    public HadoopFile(FileStatus file) throws IOException {
        this.fileName = file.getPath().toString().substring(file.getPath().toString().lastIndexOf(Const.hadoopSeparator)).replaceFirst(Const.hadoopSeparator, "");
        this.path = file.getPath().toString();
        this.owner = file.getOwner();
        this.size = Long.toString(file.getLen() / 1024) + " kb";
        this.dateModified = new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new java.util.Date(file.getModificationTime()));
        this.isDirectory = file.isDirectory();
    }

    public String getFileName() {
        return fileName;
    }

    public String getPath() {
        return path;
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

    public boolean isDirectory() {
        return isDirectory;
    }


}
