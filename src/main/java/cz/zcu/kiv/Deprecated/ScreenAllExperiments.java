package cz.zcu.kiv.Deprecated;

import cz.zcu.kiv.Const;
import cz.zcu.kiv.DataUploading.HadoopController;
import cz.zcu.kiv.DataUploading.HadoopScreen;
import cz.zcu.kiv.JFrameSingleton;
import cz.zcu.kiv.SettingsPanel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

import static cz.zcu.kiv.Const.homeDirectory;
import static cz.zcu.kiv.DataUploading.HadoopController.deleteFile;
import static java.lang.System.out;

/***********************************************************************************************************************
 *
 * This file is part of the EEG_WorkflowGUI project

 * ==========================================
 *
 * Copyright () 2017 by University of West Bohemia (http://www.zcu.cz/en/)
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
 * ScreenAllExperiments, 2017/06/28 07:16 Dorian Beganovic
 *
 **********************************************************************************************************************/
public class ScreenAllExperiments extends JPanel implements ListSelectionListener, HadoopScreen {

    private static Log logger = LogFactory.getLog(ScreenAllExperiments.class);
    // initialize contents of the table as empty
    private String[][] data = new String[0][5];
    // these are the predefined columns
    private final String[] columns = HadoopController.columns;
    // table model ie. the data stored in the table
    private DefaultTableModel tableModel;
    private  JTable table = new JTable();
    // path which we want to analyze, in this case this is pretty fixed and mainly dependant on Constants
    private String path = "";
    private String fileTypeOption = "FILES";


    public ScreenAllExperiments(final String path){
        //use the borderlayout
        super(new BorderLayout());

        // add the path
        this.path = path;

        // start the thread to get hadoop data
        HadoopController.getCachedHadoopData(this,fileTypeOption);
        HadoopController.getHadoopData(this,fileTypeOption);
        initializePanel();

    }

    public void initializePanel() {

        /*
        create the graphics
         */

        final JTable table = new JTable();

        //Create the table and put it in a scroll pane.
        tableModel = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                //all cells false
                return false;
            }
        };
        table.setModel(tableModel);
        table.setRowHeight(30);


        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(10000, 50));

        //table.setShowHorizontalLines(true);
        //table.setShowVerticalLines(true);
        table.setGridColor(Color.BLACK);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);


        JScrollPane listScrollPane = new JScrollPane(table);

        // back button
        JButton backButton = new JButton("BACK");
        backButton.setPreferredSize(new Dimension(120,40));
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFrameSingleton.getMainScreen().setContentPane(new ScreenAllUsers());
                JFrameSingleton.getMainScreen().invalidate();
                JFrameSingleton.getMainScreen().validate();
            }
        });


        // next button
        JButton deleteButton = new JButton("DELETE FILE");
        deleteButton.setPreferredSize(new Dimension(120,40));
        deleteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteFile(
                        homeDirectory + Const.hadoopSeparator + path + Const.hadoopSeparator +  data[table.getSelectedRow()][0],
                        Const.getHadoopFileSystem() );
                HadoopController.getHadoopData(ScreenAllExperiments.this,fileTypeOption);
                JFrameSingleton.getMainScreen().invalidate();
                JFrameSingleton.getMainScreen().validate();
            }
        });


        // next button
        JButton nextButton = new JButton("NEXT");
        nextButton.setPreferredSize(new Dimension(120,40));
        nextButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // IMPORTANT: THE NEXT PATH IS THE PATH YOU RECEIVED FROM PREVIOUS SCREEN
                // PLUS THE PATH WHICH THE USER CHOOSE
                JFrameSingleton.getMainScreen().setContentPane(new ScreenSingleExperiment(
                        path + Const.hadoopSeparator + data[table.getSelectedRow()][0])); // we use index 0 bcs filename is in col0
                JFrameSingleton.getMainScreen().invalidate();
                JFrameSingleton.getMainScreen().validate();
            }
        });


        // ADD EXPERIMENT button
        JButton addButton = new JButton("UPLOAD A DIRECTORY");
        addButton.setPreferredSize(new Dimension(120,40));
        addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Configuration conf = new Configuration();

                try {

                    JFileChooser jFileChooser = new JFileChooser();
                    // user can only choose a directory !
                    jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    jFileChooser.showSaveDialog(null);

                    logger.info("Uploading a directory");
                    //String filepath = jFileChooser.getSelectedFile().getAbsoluteFile().getAbsolutePath();
                    // this is the filepath user chooses
                    String filepath = jFileChooser.getCurrentDirectory().toString();
                    // name of the file the user chose
                    String filename = filepath.substring(filepath.lastIndexOf(Const.localSeparator));

                    logger.info("File path of the input folder/file " + filepath);
                    logger.info("File name of the folder/file " + filename);

                    FileSystem fs = Const.getHadoopFileSystem();


                    // @src -> @destination
                    //fs.copyFromLocalFile(false, true, new Path(filePath), new Path(homeDirectory + filename));

                    JDialog jDialog = new JDialog();
                    DefaultListModel model = new DefaultListModel();
                    JList list = new JList(model);
                    JScrollPane sp = new JScrollPane(list);
                    jDialog.add(sp, BorderLayout.CENTER);
                    jDialog.pack();
                    jDialog.setSize(600,350);
                    jDialog.setLocationRelativeTo(null);
                    jDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                    jDialog.setVisible(true);

                    File dir = new File(filepath);

                    //copyFilesToDir(dir.listFiles(), fs, homeDirectory + Const.hadoopSeparator + path  + filename, list);
                    HadoopController.copyFilesToDir(dir.listFiles(),fs, homeDirectory + Const.hadoopSeparator + path  + filename, list,ScreenAllExperiments.this);
                    HadoopController.getHadoopData(ScreenAllExperiments.this,fileTypeOption);
                }
                catch (Exception ex) {
                    out.println(ex.getMessage());
                }
            }
        });

        //Create a panel that uses BoxLayout.
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new GridBagLayout());
        buttonPane.add(backButton);
        buttonPane.add(Box.createHorizontalStrut(5));
        buttonPane.add(new JSeparator(SwingConstants.VERTICAL));
        buttonPane.add(Box.createHorizontalStrut(5));
        buttonPane.add(addButton);
        buttonPane.add(Box.createHorizontalStrut(5));
        buttonPane.add(new JSeparator(SwingConstants.VERTICAL));
        buttonPane.add(Box.createHorizontalStrut(5));
        buttonPane.add(nextButton);
        buttonPane.add(Box.createHorizontalStrut(5));
        buttonPane.add(new JSeparator(SwingConstants.VERTICAL));
        buttonPane.add(Box.createHorizontalStrut(100));
        buttonPane.add(deleteButton);

        buttonPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        JMenuBar jMenuBar = new JMenuBar();
        JMenu jMenu = new JMenu("Settings");
        jMenu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
                    JFrameSingleton.getMainScreen().setContentPane(new SettingsPanel("AllExperiments",path));
                    JFrameSingleton.getMainScreen().invalidate();
                    JFrameSingleton.getMainScreen().validate();
                }
            }
        });
        JMenu jMenu2 = new JMenu("Current path: " + homeDirectory + Const.hadoopSeparator + path);
        jMenuBar.add(jMenu);
        jMenuBar.add(jMenu2);

        add(jMenuBar,BorderLayout.PAGE_START);

        add(listScrollPane, BorderLayout.CENTER);
        add(buttonPane, BorderLayout.PAGE_END);

    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
    }

    @Override
    public String[][] getData() {
        if(data == null){
            return new String[0][5];
        }
        return data;
    }
    @Override
    public String getPath() {
        return path;
    }

    @Override
    public JTable getJTable() {
        return this.table;
    }

    @Override
    public void setData(String[][] data) {
        this.data = data;
    }

    @Override
    public void setJTable(JTable newTable) {
        this.table = newTable;
    }

    @Override
    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    @Override
    public void setTableModel(DefaultTableModel tableModel) {
        this.tableModel = tableModel;
    }


}

