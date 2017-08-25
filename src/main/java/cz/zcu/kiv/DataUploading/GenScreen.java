package cz.zcu.kiv.DataUploading;

import cz.zcu.kiv.Analysis.AnalysisPanel;
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
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static cz.zcu.kiv.Const.screenSizeHeight;
import static cz.zcu.kiv.Const.screenSizeWidth;
import static cz.zcu.kiv.DataUploading.HadoopHdfsController.deleteFile;
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

    // parent screen (if double clicked)
    private JFrame jFrameParent;
    // initialize contents of the table as empty
    private String[][] data;
    private String[][] tempArray;
    // these are the predefined columns
    private final String[] columns = HadoopHdfsController.columns;
    // table model ie. the data stored in the table
    private JTable table = new JTable();
    private DefaultTableModel tableModel;
    // path which we want to analyze, in this case this is pretty fixed and mainly dependant on Constants
    private String path;

    public GenScreen(JFrame jFrameParent, final String path) {
        //use the borderlayout
        super(new BorderLayout());

        this.jFrameParent = jFrameParent;
        // add the path
        logger.info("initialized with path " + path);
        this.path = path;

        // initialize the GUI
        initializePanel();
    }


    @Override
    public void initializePanel() {

        // get the hadoop data for current path in a separate thread
        SwingWorker<Void, Void> swingWorker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                logger.info("Getting hadoop data in background...");
                HadoopHdfsController.getHadoopData(GenScreen.this);
                return null;
            }
        };
        swingWorker.execute();


        /*
        1. JTable
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
        /*
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        table.getColumnModel().getColumn(0).setMinWidth(500);
        table.getColumnModel().getColumn(0).setPreferredWidth(500);
        table.getColumnModel().getColumn(0).setMaxWidth(500);
        table.getColumnModel().getColumn(1).setMinWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setMaxWidth(100);
        table.getColumnModel().getColumn(2).setMinWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setMaxWidth(100);
        table.getColumnModel().getColumn(3).setMinWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setMaxWidth(100);
        */


        /*
        2. Scroll panel listener for double clicks
         */

        JScrollPane listScrollPane = new JScrollPane(table);
        // double click functionality
        listScrollPane.getViewport().getView().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
                    logger.info("Double clicked");
                    final JFrame frame = new JFrame();
                    String cleanPath = path.replaceAll("/$", ""); // replace the ending "/" sign
                    String cleanFileName;
                    if (data[table.getSelectedRow()][0].startsWith("/")) {
                        cleanFileName = data[table.getSelectedRow()][0].replaceFirst("/", "");
                    } else {
                        cleanFileName = data[table.getSelectedRow()][0];
                    }
                    frame.add(new GenScreen(frame, cleanPath + Const.hadoopSeparator + cleanFileName));
                    frame.setSize((int) screenSizeWidth / 2, (int) (screenSizeHeight * 3 / 4));
                    frame.setResizable(true);
                    frame.setLocationByPlatform(true);
                    frame.setLocationRelativeTo(null);
                    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                    frame.setVisible(true);
                    // set the escape button
                    KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
                    Action escapeAction = new AbstractAction() {
                        // close the frame when the user presses escape
                        public void actionPerformed(ActionEvent e) {
                            frame.dispose();
                        }
                    };
                    frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
                    frame.getRootPane().getActionMap().put("ESCAPE", escapeAction);
                }
            }
        });

        /*
        Buttons
         */

        // 1. back button
        JButton backButton = new JButton("BACK");
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logger.info("Path is " + path);
                String backPath = path.substring(0, path.lastIndexOf(Const.hadoopSeparator));
                logger.info("Back path is " + backPath);
                jFrameParent.setContentPane(new GenScreen(jFrameParent, backPath));
                jFrameParent.invalidate();
                jFrameParent.validate();
            }
        });


        /*
         2. filter text field
          */
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
                for (String[] file : data) {
                    String filename = file[0];
                    if (filename.toLowerCase().contains(query.toLowerCase())) {
                        tempData.add(file);
                    }
                }
                tempArray = new String[tempData.size()][5];
                for (int i = 0; i < tempData.size(); i++) {
                    tempArray[i] = tempData.get(i);
                }
                getTableModel().setDataVector(tempArray, columns);
                getTableModel().fireTableDataChanged();
            }
        });

        /*
         3. analyze button
          */
        JButton analyzeButton = new JButton("ANALYZE");
        analyzeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // IMPORTANT: THE NEXT PATH IS THE PATH YOU RECEIVED FROM PREVIOUS SCREEN PLUS THE PATH WHICH THE USER CHOOSE
                // replace the "/" sign at the end of the string
                String cleanPath;
                if (path.endsWith("/")) {
                    cleanPath = path.substring(0, path.length() - 1);
                } else {
                    cleanPath = path;
                }
                // isolate the filename from the path
                String cleanFileName = "";

                DefaultTableModel dtm = (DefaultTableModel) table.getModel();
                int nRow = dtm.getRowCount(), nCol = dtm.getColumnCount();
                String[][] tableData = new String[nRow][nCol];
                for (int i = 0 ; i < nRow ; i++)
                    for (int j = 0 ; j < nCol ; j++)
                        tableData[i][j] = String.valueOf(dtm.getValueAt(i,j));
                if (tableData[table.getSelectedRow()][0].startsWith("/")) {
                    cleanFileName = tableData[table.getSelectedRow()][0].replaceFirst("/", "");
                } else {
                    cleanFileName = tableData[table.getSelectedRow()][0];
                }
                // create the "clean" path value
                String cleanNewPath = cleanPath + Const.hadoopSeparator + cleanFileName;

                //
                if (cleanFileName.endsWith(".txt")) {
                    final JFrame frame = new JFrame();
                    frame.add(new AnalysisPanel(cleanNewPath));
                    frame.setSize((int) screenSizeWidth / 2, (int) (screenSizeHeight * 3 / 4));
                    frame.setResizable(true);
                    frame.setLocationByPlatform(true);
                    frame.setLocationRelativeTo(null);
                    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                    frame.setVisible(true);
                    // set the escape button
                    KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
                    Action escapeAction = new AbstractAction() {
                        // close the frame when the user presses escape
                        public void actionPerformed(ActionEvent e) {
                            frame.dispose();
                        }
                    };
                    frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
                    frame.getRootPane().getActionMap().put("ESCAPE", escapeAction);
                } else if (cleanFileName.endsWith(".eeg")) {
                    String input = JOptionPane.showInputDialog("Guessed number");
                    int guessedNumber = Integer.parseInt(input);
                    logger.info(input);
                    final JFrame frame = new JFrame();
                    frame.add(new AnalysisPanel(cleanNewPath, guessedNumber));
                    frame.setSize((int) screenSizeWidth / 2, (int) (screenSizeHeight * 3 / 4));
                    frame.setResizable(true);
                    frame.setLocationByPlatform(true);
                    frame.setLocationRelativeTo(null);
                    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                    frame.setVisible(true);
                    // set the escape button
                    KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
                    Action escapeAction = new AbstractAction() {
                        // close the frame when the user presses escape
                        public void actionPerformed(ActionEvent e) {
                            frame.dispose();
                        }
                    };
                    frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
                    frame.getRootPane().getActionMap().put("ESCAPE", escapeAction);

                } else {
                    JOptionPane.showMessageDialog(null, "Please choose a .eeg or info.txt file");
                    logger.info("Please choose a .eeg or info.txt file");
                }

                logger.info("Clean path " + path);
                logger.info("Clean file name " + cleanFileName);
                logger.info("Clean new path " + cleanPath + Const.hadoopSeparator + cleanFileName);

                //jFrameParent.setContentPane(new AnalysisPanel(jFrameParent,cleanNewPath));
                jFrameParent.invalidate();
                jFrameParent.validate();
            }
        });

        // 4. next button
        JButton nextButton = new JButton("NEXT");
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // IMPORTANT: THE NEXT PATH IS THE PATH YOU RECEIVED FROM PREVIOUS SCREEN PLUS THE PATH WHICH THE USER CHOOSE
                // replace the "/" sign at the end of the string
                String cleanPath;
                if (path.endsWith("/")) {
                    cleanPath = path.substring(0, path.length() - 1);
                } else {
                    cleanPath = path;
                }
                // isolate the filename from the path
                String cleanFileName = "";
                if (data[table.getSelectedRow()][0].startsWith("/")) {
                    cleanFileName = data[table.getSelectedRow()][0].replaceFirst("/", "");
                } else {
                    cleanFileName = data[table.getSelectedRow()][0];
                }
                // create the "clean" path value
                String cleanNewPath = cleanPath + Const.hadoopSeparator + cleanFileName;
                logger.info("Clean path " + path);
                logger.info("Clean file name " + cleanFileName);
                logger.info("Clean new path " + cleanPath + Const.hadoopSeparator + cleanFileName);
                // initialize the new screen with the new path
                jFrameParent.setContentPane(new GenScreen(jFrameParent, cleanNewPath));
                jFrameParent.invalidate();
                jFrameParent.validate();
            }
        });


        // 5. delete button
        JButton deleteButton = new JButton("DELETE");
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedOption = JOptionPane.showConfirmDialog(null,
                        "Do you wanna delete this folder/file and all of it's contents?",
                        "Choose",
                        JOptionPane.YES_NO_OPTION);
                if (selectedOption == JOptionPane.YES_OPTION) {
                    deleteFile(
                            path + Const.hadoopSeparator + data[table.getSelectedRow()][0],
                            Const.getHadoopFileSystem());
                }

                HadoopHdfsController.getHadoopData(GenScreen.this);
            }
        });


        // 6. upload folder
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

                    logger.info("File path of the input folder/file " + filepath);

                    String filename = filepath.substring(filepath.lastIndexOf("/"), filepath.length());
                    FileSystem fs = Const.getHadoopFileSystem();


                    // @src -> @destination
                    //fs.copyFromLocalFile(false, true, new Path(filePath), new Path(homeDirectory + filename));

                    JDialog jDialog = new JDialog();
                    DefaultListModel model = new DefaultListModel();
                    JList list = new JList(model);
                    JScrollPane sp = new JScrollPane(list);
                    jDialog.add(sp, BorderLayout.CENTER);
                    jDialog.pack();
                    jDialog.setSize((int) (screenSizeWidth / 2.25), (int) (screenSizeHeight / 3));
                    jDialog.setLocationRelativeTo(null);
                    jDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                    jDialog.setVisible(true);

                    File dir = new File(filepath);

                    //copyFilesToDir(dir.listFiles(), fs, homeDirectory + Const.hadoopSeparator + path  + filename, list);
                    HadoopHdfsController.copyFilesToDir(dir.listFiles(), fs, path + filename, list, GenScreen.this);
                    HadoopHdfsController.getHadoopData(GenScreen.this);
                } catch (Exception ex) {
                    out.println(ex.getMessage());
                }
            }
        });


        // 7. create folder button
        JButton createFolder = new JButton("CREATE FOLDER");
        createFolder.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String filename = JOptionPane.showInputDialog(null,
                            "How should the folder be called?", "Enter the folder name", JOptionPane.QUESTION_MESSAGE);
                    FileSystem fs = Const.getHadoopFileSystem();
                    logger.info("User wants to create a path " + path + Const.hadoopSeparator + filename);
                    fs.mkdirs(new Path(path + Const.hadoopSeparator + filename));
                    logger.info("Created a path " + path + Const.hadoopSeparator + filename);
                    HadoopHdfsController.getHadoopData(GenScreen.this);
                } catch (Exception ex) {
                    out.println(ex.getMessage());
                }
            }
        });

        // 8. upload files
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
                    jDialog.setSize((int) (screenSizeWidth / 2.25), (int) (screenSizeHeight / 3));
                    jDialog.setLocationRelativeTo(null);
                    jDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                    jDialog.setVisible(true);

                    HadoopHdfsController.copyFilesToDir(files, fs, path, list, GenScreen.this);

                    HadoopHdfsController.getHadoopData(GenScreen.this);
                } catch (Exception ex) {
                    out.println(ex.getMessage());
                }
            }
        });


        /*
        set the preferred sizes of buttons
         */
        int panelWidth = (int) screenSizeWidth / 2;
        int panelHeight = (int) (screenSizeHeight * 3 / 4);
        int unifiedHeight = (int) panelHeight / 20;

        // panel 1 - preferred sizes
        backButton.setPreferredSize(new Dimension((int) panelWidth / 6, unifiedHeight));
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        jTextField.setPreferredSize(new Dimension((int) panelWidth / 4, unifiedHeight));
        jTextField.setMinimumSize(jTextField.getPreferredSize());
        jTextField.setAlignmentX(Component.CENTER_ALIGNMENT);

        analyzeButton.setPreferredSize(new Dimension((int) panelWidth / 5, unifiedHeight));
        analyzeButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        nextButton.setPreferredSize(new Dimension((int) panelWidth / 5, unifiedHeight));
        nextButton.setAlignmentX(Component.CENTER_ALIGNMENT);


        // panel 2 - preferred sizes
        deleteButton.setPreferredSize(new Dimension((int) panelWidth / 6, unifiedHeight));
        deleteButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        uploadDirectory.setPreferredSize(new Dimension((int) panelWidth / 4, unifiedHeight));
        uploadDirectory.setAlignmentX(Component.CENTER_ALIGNMENT);

        createFolder.setPreferredSize(new Dimension((int) panelWidth / 5, unifiedHeight));
        createFolder.setAlignmentX(Component.CENTER_ALIGNMENT);

        uploadFiles.setPreferredSize(new Dimension((int) panelWidth / 5, unifiedHeight));
        uploadFiles.setAlignmentX(Component.CENTER_ALIGNMENT);



        /*
        create the panels
         */
        JPanel twoFloorPane = new JPanel();
        JPanel panelUp = new JPanel();
        JPanel panelDown = new JPanel();

        // place the items to panels and add layouts
        twoFloorPane.setLayout(new GridLayout(2, 1));
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


        panelDown.add(deleteButton);
        panelDown.add(Box.createHorizontalStrut(5));
        panelDown.add(new JSeparator(SwingConstants.VERTICAL));
        panelDown.add(Box.createHorizontalStrut(5));
        panelDown.add(uploadDirectory);
        panelDown.add(Box.createHorizontalStrut(5));
        panelDown.add(new JSeparator(SwingConstants.VERTICAL));
        panelDown.add(Box.createHorizontalStrut(5));
        panelDown.add(createFolder);
        panelDown.add(Box.createHorizontalStrut(5));
        panelDown.add(new JSeparator(SwingConstants.VERTICAL));
        panelDown.add(Box.createHorizontalStrut(5));
        panelDown.add(uploadFiles);


        twoFloorPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        twoFloorPane.add(panelUp);
        twoFloorPane.add(panelDown);


        /*
         add the menu bar
          */
        JMenuBar jMenuBar = new JMenuBar();

        JMenu jMenu1 = new JMenu("Settings");
        jMenu1.setBorderPainted(true);
        jMenu1.setBorder(new LineBorder(Color.BLACK));
        jMenu1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
                    JFrameSingleton.getMainScreen().setContentPane(new SettingsPanel(jFrameParent, path));
                    JFrameSingleton.getMainScreen().invalidate();
                    JFrameSingleton.getMainScreen().validate();
                }
            }
        });
        JMenu jMenu2 = new JMenu("   Current path: " + path);
        jMenu1.setBorderPainted(true);
        jMenuBar.add(jMenu1);
        jMenuBar.add(jMenu2);



        /*
         add everything together to the JFrame
          */
        add(jMenuBar, BorderLayout.PAGE_START);
        add(listScrollPane, BorderLayout.CENTER);
        add(twoFloorPane, BorderLayout.PAGE_END);

        logger.info("Done initializing JPanel");
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
    }

    @Override
    public String[][] getData() {
        if (data == null) {
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
