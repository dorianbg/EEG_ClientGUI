package cz.zcu.kiv.DataUploading;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

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
 * HadoopScreen, 2017/07/01 15:38 Dorian Beganovic
 *
 **********************************************************************************************************************/
/*
interface for generalizing screens used by HadoopController classes
 */
public interface HadoopScreen {

    public void initializePanel();

    public String[][] getData();

    public void setData(String[][] data);

    public String getPath();

    public JTable getJTable();

    public void setJTable(JTable newTable);

    public DefaultTableModel getTableModel();

    public void setTableModel(DefaultTableModel tableModel);

}
