package cz.zcu.kiv.DataUploading;

import cz.zcu.kiv.Const;
import cz.zcu.kiv.JFrameSingleton;
import cz.zcu.kiv.SettingsPanel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;

import static cz.zcu.kiv.Const.homeDirectory;
import static cz.zcu.kiv.Const.uriPrefix;
import static cz.zcu.kiv.DataUploading.HadoopController.deleteFile;
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
 * GenScreen, 2017/07/13 23:31 Dorian Beganovic
 *
 **********************************************************************************************************************/
public class GenScreen extends JPanel implements ListSelectionListener, HadoopScreen {

    private static Log logger = LogFactory.getLog(GenScreen.class);

    private JFrame jFrameParent;
    // initialize contents of the table as empty
    private String[][] data = new String[0][5];
    // these are the predefined columns
    private final String[] columns = HadoopController.columns;
    // table model ie. the data stored in the table
    private DefaultTableModel tableModel;
    private  JTable table = new JTable();
    // path which we want to analyze, in this case this is pretty fixed and mainly dependant on Constants
    private String path;

    public GenScreen(JFrame jFrameParent, final String path){
        //use the borderlayout
        super(new BorderLayout());

        this.jFrameParent = jFrameParent;
        // add the path
        logger.info("initialized with path " + path);
        this.path = path;

        // start the thread to get hadoop data
        //HadoopController.getCachedHadoopData(this,"FILES");
        //HadoopController.getHadoopData(this,"FILES");

        initializePanel();

        SwingWorker<Void,Void> swingWorker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                logger.info("Getting hadoop data in background...");
                HadoopController.getCachedHadoopData(GenScreen.this,"FILES");
                HadoopController.getHadoopData(GenScreen.this,"FILES");
                return null;
            }
        };
        swingWorker.execute();

    }




    @Override
    public void initializePanel() {
        /*
        create the graphics
         */
        logger.info("Initializing the panel");

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


        logger.debug("1");
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(10000, 50));

        //table.setShowHorizontalLines(true);
        //table.setShowVerticalLines(true);
        table.setGridColor(Color.BLACK);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);


        JScrollPane listScrollPane = new JScrollPane(table);
        // double click functionality
        listScrollPane.getViewport().getView().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
                    logger.info("Double clicked");
                    /*
                    JFrameSingleton.getMainScreen().setContentPane(new ScreenAllExperiments(data[table.getSelectedRow()][0]));
                    JFrameSingleton.getMainScreen().invalidate();
                    JFrameSingleton.getMainScreen().validate();
                    logger.info("Going into Hadoop folder: " +  uriPrefix+ homeDirectory +Const.hadoopSeparator +data[table.getSelectedRow()][0]);
                    */
                    /*
                    JDialog jDialog = new JDialog();
                    jDialog.add(new ScreenAllExperiments(data[table.getSelectedRow()][0]));
                    jDialog.setSize(JFrameSingleton.getMainScreen().getSize());
                    jDialog.setLocationRelativeTo(null);
                    jDialog.setVisible(true);
                    */
                    JFrame frame = new JFrame();
                    String cleanPath = path.replaceAll("/$","");
                    String cleanFileName = "";
                    if (data[table.getSelectedRow()][0].startsWith("/")){
                        cleanFileName = data[table.getSelectedRow()][0].replaceFirst("/","");
                    }
                    else{
                        cleanFileName = data[table.getSelectedRow()][0];
                    }
                    frame.add(new GenScreen(frame, cleanPath + Const.hadoopSeparator + cleanFileName));
                    frame.setSize(800, 700);
                    frame.setResizable(true);
                    frame.setLocationByPlatform(true);
                    //frame.setLocation(JFrameSingleton.getMainScreen().getLocation().x-10,JFrameSingleton.getMainScreen().getLocation().y-10);
                    frame.setLocationRelativeTo(null);
                    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                    frame.setVisible(true);

                }
            }
        });

        logger.debug("2");



        // back button
        JButton backButton = new JButton("BACK");
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logger.info("Path is " + path);
                String backPath = path.substring(0,path.lastIndexOf(Const.hadoopSeparator));
                logger.info("Back path is " + backPath);
                jFrameParent.setContentPane(new GenScreen(jFrameParent,backPath));
                jFrameParent.invalidate();
                jFrameParent.validate();
            }
        });

        logger.debug("3");


        // delete button
        JButton deleteButton = new JButton("DELETE");
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedOption = JOptionPane.showConfirmDialog(null,
                        "Do you wanna delete this folder/file and all of it's contents?",
                        "Choose",
                        JOptionPane.YES_NO_OPTION);
                if (selectedOption == JOptionPane.YES_OPTION) {
                    deleteFile(
                            path + Const.hadoopSeparator +  data[table.getSelectedRow()][0],
                            Const.getHadoopFileSystem() );
                }

                HadoopController.getHadoopData(GenScreen.this,"FILES");
            }
        });


        logger.debug("4");


        // next button
        JButton nextButton = new JButton("NEXT");
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // IMPORTANT: THE NEXT PATH IS THE PATH YOU RECEIVED FROM PREVIOUS SCREEN
                // PLUS THE PATH WHICH THE USER CHOOSE
                String cleanPath = path.replaceAll("/$","");
                if(path.endsWith("/")){
                    cleanPath = path.substring(0,path.length()-1);
                }
                else{
                    cleanPath = path;
                }
                String cleanFileName = "";
                if (data[table.getSelectedRow()][0].startsWith("/")){
                    cleanFileName = data[table.getSelectedRow()][0].replaceFirst("/","");
                }
                else{
                    cleanFileName = data[table.getSelectedRow()][0];
                }
                String cleanNewPath = cleanPath + Const.hadoopSeparator + cleanFileName;
                logger.info("Clean path " + path);
                logger.info("Clean file name " + cleanFileName);
                logger.info("Clean new path " + cleanPath + Const.hadoopSeparator + cleanFileName);
                jFrameParent.setContentPane(new GenScreen(jFrameParent,cleanNewPath));
                jFrameParent.invalidate();
                jFrameParent.validate();
            }
        });

        logger.debug("5");



        // ADD EXPERIMENT button
        JButton createFolder = new JButton("ADD FOLDER");
        createFolder.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String filename = JOptionPane.showInputDialog(null,
                            "How should the folder be called?", "Enter the folder name",  JOptionPane.QUESTION_MESSAGE);
                    FileSystem fs = Const.getHadoopFileSystem();
                    logger.info("User wants to create a path " +  path + Const.hadoopSeparator +filename );
                    fs.mkdirs(new Path(  path + Const.hadoopSeparator + filename));
                    logger.info("Created a path " +  path + Const.hadoopSeparator + filename );
                    HadoopController.getHadoopData(GenScreen.this,"FILES");
                }
                catch (Exception ex) {
                    out.println(ex.getMessage());
                }
            }
        });

        // Upload directory
        JButton uploadDirectory = new JButton("UPLOAD FOLDER");
        uploadDirectory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {

                    JFileChooser jFileChooser = new JFileChooser();
                    // user can only choose a directory !
                    //jFileChooser.setCurrentDirectory(new java.io.File("."));
                    jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    jFileChooser.showSaveDialog(null);
                    jFileChooser.setDialogTitle("Choose a directory to upload");


                    logger.info("Uploading a directory");
                    String filepath = jFileChooser.getSelectedFile().getAbsolutePath();
                    // this is the filepath user chooses
                    //String filepath = jFileChooser.getCurrentDirectory().toString();
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
                    HadoopController.copyFilesToDir(dir.listFiles(),fs, path  + filename, list,GenScreen.this);
                    HadoopController.getHadoopData(GenScreen.this,"FILES");
                }
                catch (Exception ex) {
                    out.println(ex.getMessage());
                }
            }
        });


        logger.debug("6");

        // Upload files
        JButton uploadFiles = new JButton("UPLOAD FILES");
        uploadFiles.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Configuration conf = new Configuration();

                try {

                    JFileChooser jFileChooser = new JFileChooser();
                    // user can only choose files !
                    jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    jFileChooser.setMultiSelectionEnabled(true);
                    jFileChooser.showOpenDialog(null);

                    File[] files = jFileChooser.getSelectedFiles();

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

                    HadoopController.copyFilesToDir(files, fs,  path, list,GenScreen.this);

                    HadoopController.getHadoopData(GenScreen.this,"FILES");
                }
                catch (Exception ex) {
                    out.println(ex.getMessage());
                }
            }
        });

        logger.debug("7");

        // filter text field
        final JTextField jTextField = new JTextField();
        jTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                update();
            }
            public void removeUpdate(DocumentEvent e) {
                update();
            }
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            public void update() {
                String query = jTextField.getText();

                logger.info("Set the hadoop data (String[][]) matrix onto JTable ");

                ArrayList<String[]> tempData = new ArrayList<String[]>();
                for(String[] file : data){
                    String filename = file[0];
                    if (filename.toLowerCase().contains(query.toLowerCase())) {
                        tempData.add(file);
                    }
                }
                String[][] tempArray = new String[tempData.size()][5];
                for(int i = 0; i < tempData.size(); i++){
                    tempArray[i] = tempData.get(i);
                }
                getTableModel().setDataVector(tempArray,columns);
                getTableModel().fireTableDataChanged();
            }
        });

        logger.debug("8");

        JButton analyzeButton = new JButton("ANALYZE");
        analyzeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("hi");
            }
        });


        logger.info("Organizing panel");

        // row1

        analyzeButton.setPreferredSize(new Dimension(140,35));
        analyzeButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        nextButton.setPreferredSize(new Dimension(140,35));
        nextButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        backButton.setPreferredSize(new Dimension(120,35));
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        jTextField.setPreferredSize(new Dimension(220,35));
        jTextField.setMinimumSize(jTextField.getPreferredSize());
        jTextField.setAlignmentX(Component.CENTER_ALIGNMENT);

        // row2

        createFolder.setPreferredSize(new Dimension(160,35));
        createFolder.setAlignmentX(Component.CENTER_ALIGNMENT);

        deleteButton.setPreferredSize(new Dimension(120,35));
        deleteButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        uploadFiles.setPreferredSize(new Dimension(160,35));
        uploadFiles.setAlignmentX(Component.CENTER_ALIGNMENT);

        uploadDirectory.setPreferredSize(new Dimension(180,35));
        uploadDirectory.setAlignmentX(Component.CENTER_ALIGNMENT);


        // create the bottom panel
        JPanel twoFloorPane = new JPanel();
        JPanel panelUp = new JPanel();
        JPanel panelDown = new JPanel();

        twoFloorPane.setLayout(new GridLayout(2,1));
        panelUp.setLayout(new FlowLayout());
        panelDown.setLayout(new FlowLayout());

        panelUp.add(backButton);
        panelUp.add(Box.createHorizontalStrut(5));
        panelUp.add(new JSeparator(SwingConstants.VERTICAL));
        panelUp.add(Box.createHorizontalStrut(5));
        panelUp.add(jTextField);
        panelUp.add(Box.createHorizontalStrut(5));
        panelUp.add(new JSeparator(SwingConstants.VERTICAL));
        panelUp.add(Box.createHorizontalStrut(5));
        panelUp.add(analyzeButton);
        panelUp.add(Box.createHorizontalStrut(5));
        panelUp.add(new JSeparator(SwingConstants.VERTICAL));
        panelUp.add(Box.createHorizontalStrut(5));
        panelUp.add(nextButton);


        panelDown.add(createFolder);
        panelDown.add(Box.createHorizontalStrut(5));
        panelDown.add(new JSeparator(SwingConstants.VERTICAL));
        panelDown.add(Box.createHorizontalStrut(5));
        panelDown.add(uploadFiles);
        panelDown.add(Box.createHorizontalStrut(5));
        panelDown.add(new JSeparator(SwingConstants.VERTICAL));
        panelDown.add(Box.createHorizontalStrut(5));
        panelDown.add(uploadDirectory);
        panelDown.add(Box.createHorizontalStrut(5));
        panelDown.add(new JSeparator(SwingConstants.VERTICAL));
        panelDown.add(Box.createHorizontalStrut(5));
        panelDown.add(deleteButton);


        twoFloorPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        twoFloorPane.add(panelUp);
        twoFloorPane.add(panelDown);


        // menu bar
        JMenuBar jMenuBar = new JMenuBar();

        JMenu jMenu1 = new JMenu("Settings");
        jMenu1.setBorderPainted(true);
        jMenu1.setBorder(new LineBorder(Color.BLACK));
        jMenu1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
                    JFrameSingleton.getMainScreen().setContentPane(new SettingsPanel(jFrameParent,"GeneralScreen",path));
                    JFrameSingleton.getMainScreen().invalidate();
                    JFrameSingleton.getMainScreen().validate();
                }
            }
        });
        JMenu jMenu2 = new JMenu("   Current path: " + path);
        jMenu1.setBorderPainted(true);
        jMenuBar.add(jMenu1);
        jMenuBar.add(jMenu2);


        // add all together
        add(jMenuBar,BorderLayout.PAGE_START);
        add(listScrollPane, BorderLayout.CENTER);
        add(twoFloorPane, BorderLayout.PAGE_END);

        logger.info("Done initializing JPanel");
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
