package gui;

import agent.Heuristic;
import agent.Solution;
import mummymaze.MummyMazeAgent;
import mummymaze.MummyMazeProblem;
import mummymaze.MummyMazeState;
import mummymaze.util.TileType;
import searchmethods.AStarSearch;
import searchmethods.BeamSearch;
import searchmethods.DepthLimitedSearch;
import searchmethods.SearchMethod;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainFrame extends JFrame {

    //private int[][] initialMatrix = {{1, 0, 2}, {3, 4, 5}, {6, 7, 8}};
    private final TileType[][] initialMatrix = new TileType[13][13];
    private final MummyMazeAgent agent = new MummyMazeAgent(new MummyMazeState(initialMatrix));
    private final JLabel labelSearchParameter = new JLabel("limit/beam size:");
    private final JTextField textFieldSearchParameter = new JTextField("0", 5);
    private final JButton buttonInitialState = new JButton("Read initial state");
    private final JButton buttonSolve = new JButton("Solve");
    private final JButton buttonStop = new JButton("Stop");
    private final JButton buttonShowSolution = new JButton("Show solution");
    private final JButton buttonReset = new JButton("Reset to initial state");
    private final JButton buttonSolveAllTests = new JButton("Test all levels");
    private final JButton buttonTestALevel = new JButton("Test a level");
    private JComboBox comboBoxSearchMethods;
    private JComboBox comboBoxHeuristics;
    private JTextArea textArea;
    private GameArea game;

    private SwingWorker runningWorker;
    private LinkedList<StringBuilder> stringBuilders;
    private LinkedList<File> outputFiles;

    private final String FILE_HEADER = "Search Algorithm;Heuristic;Limit Size;Solution Found;Solution Cost;Num of Expanded Nodes;Max Frontier Size;Num of Generated States\n";

    public MainFrame() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private void jbInit() {

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setTitle("Mummy Maze");

        JPanel contentPane = (JPanel) this.getContentPane();
        contentPane.setLayout(new BorderLayout());
        JPanel panelButtons = new JPanel(new FlowLayout());
        panelButtons.add(buttonInitialState);
        buttonInitialState.addActionListener(new ButtonInitialState_ActionAdapter(this));
        panelButtons.add(buttonSolve);
        buttonSolve.addActionListener(new ButtonSolve_ActionAdapter(this));
        panelButtons.add(buttonTestALevel);
        buttonTestALevel.setEnabled(true);
        buttonTestALevel.addActionListener(new ButtonTestALevel_ActionAdapter(this));
        panelButtons.add(buttonSolveAllTests);
        buttonSolveAllTests.setEnabled(true);
        buttonSolveAllTests.addActionListener(new ButtonSolveAllTests_ActionAdapter(this));
        panelButtons.add(buttonStop);
        buttonStop.setEnabled(false);
        buttonStop.addActionListener(new ButtonStop_ActionAdapter(this));
        panelButtons.add(buttonShowSolution);
        buttonShowSolution.setEnabled(false);
        buttonShowSolution.addActionListener(new ButtonShowSolution_ActionAdapter(this));
        panelButtons.add(buttonReset);
        buttonReset.setEnabled(false);
        buttonReset.addActionListener(new ButtonReset_ActionAdapter(this));

        JPanel panelSearchMethods = new JPanel(new FlowLayout());
        comboBoxSearchMethods = new JComboBox(agent.getSearchMethodsArray());
        panelSearchMethods.add(comboBoxSearchMethods);
        comboBoxSearchMethods.addActionListener(new ComboBoxSearchMethods_ActionAdapter(this));
        panelSearchMethods.add(labelSearchParameter);
        labelSearchParameter.setEnabled(false);
        panelSearchMethods.add(textFieldSearchParameter);
        textFieldSearchParameter.setEnabled(false);
        textFieldSearchParameter.setHorizontalAlignment(JTextField.RIGHT);
        textFieldSearchParameter.addKeyListener(new TextFieldSearchParameter_KeyAdapter(this));
        comboBoxHeuristics = new JComboBox(agent.getHeuristicsArray());
        panelSearchMethods.add(comboBoxHeuristics);
        comboBoxHeuristics.setEnabled(false);
        comboBoxHeuristics.addActionListener(new ComboBoxHeuristics_ActionAdapter(this));

        JPanel puzzlePanel = new JPanel(new FlowLayout());
        game = new GameArea();
        puzzlePanel.add(game);
        textArea = new JTextArea(15, 31);
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setEditable(false);
        puzzlePanel.add(scrollPane);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(panelButtons, BorderLayout.NORTH);
        mainPanel.add(panelSearchMethods, BorderLayout.CENTER);
        mainPanel.add(puzzlePanel, BorderLayout.SOUTH);
        contentPane.add(mainPanel);

        try {
            if(!Files.exists(Path.of("./Testes")))
                Files.createDirectory(Path.of("./Testes")); // Be sure this directory exists.
        } catch (IOException e) {
            e.printStackTrace();
        }

        pack();
    }

    public void buttonSolveAllTests_ActionPerformed() throws IOException {
        JFileChooser fc = new JFileChooser(new java.io.File("./Testes"));

        fc.resetChoosableFileFilters();
        fc.setDialogTitle("Select a folder to save the tests too");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        SimpleDateFormat formatter = new SimpleDateFormat("dd_MM_yyyy-HH_mm_ss");
        Date date = new Date();
        String dateStr = formatter.format(date);
        String rn = String.valueOf((new Random()).nextInt(99999 - 10000) + 10000);
        String folderName = fc.getSelectedFile().toPath() + "/" + dateStr + "-" + rn + "/";
        Files.createDirectories(Path.of(folderName));
        try {
            prepareForTests("Solving all levels...\n");

            SwingWorker worker = new SwingWorker<Void, Void>() {
                @Override
                public Void doInBackground() {
                    try {
                        for (File file : Objects.requireNonNull(new File("./Niveis").listFiles())) {
                            prepareFile(folderName + file.getName() + ".csv","Level;");
                            testOnFile(file, true);
                        }
                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                    }
                    return null;
                }

                @Override
                public void done() {
                    finishTests(isCancelled());
                }
            };
            runningWorker = worker;
            worker.execute();


        } catch (NoSuchElementException e2) {
            JOptionPane.showMessageDialog(this, "File format not valid", "Error!", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void buttonTestALevel_ActionPerformed() throws IOException {
        JFileChooser fc = new JFileChooser(new java.io.File("./Niveis"));
        fc.setDialogTitle("Chose a level to test");
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        SimpleDateFormat formatter = new SimpleDateFormat("dd_MM_yyyy-HH_mm_ss");
        Date date = new Date();
        String dateStr = formatter.format(date);
        String rn = String.valueOf((new Random()).nextInt(99999 - 10000) + 10000);
        String fullPath = "./Testes/" + fc.getSelectedFile().getName() + "-" + dateStr + "-" + rn + ".csv";
        try {
            prepareForTests("Solving level " + fc.getSelectedFile().getName() + "...\n");

            SwingWorker worker = new SwingWorker<Void, Void>() {
                @Override
                public Void doInBackground() {
                    try {
                        prepareFile(fullPath, "");
                        testOnFile(fc.getSelectedFile(), false);
                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                    }
                    return null;
                }

                @Override
                public void done() {
                    finishTests(isCancelled());
                }
            };
            runningWorker = worker;
            worker.execute();


        } catch (NoSuchElementException e2) {
            JOptionPane.showMessageDialog(this, "File format not valid", "Error!", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void prepareForTests(String msg) {
        buttonShowSolution.setEnabled(false);
        buttonReset.setEnabled(false);
        buttonInitialState.setEnabled(false);
        buttonSolve.setEnabled(false);
        buttonStop.setEnabled(true);
        buttonSolveAllTests.setEnabled(false);
        comboBoxHeuristics.setEnabled(false);
        comboBoxSearchMethods.setEnabled(false);
        buttonTestALevel.setEnabled(false);

        outputFiles = new LinkedList<>();
        stringBuilders = new LinkedList<>();

        textArea.setText(msg);
    }

    //It has to be synchronized because it is called from a different thread
    private void finishTests(boolean wasCancelled) {
        for(int f = 0; f<outputFiles.size(); f++){
            File currentPath = outputFiles.get(f);
            if(stringBuilders.size()<=f) continue; //In case it was stopped in an unfortunate timing
            StringBuilder sb = stringBuilders.get(f);
            if(wasCancelled && f==outputFiles.size()-1){ //If its on the last file and was cancelled the last test was interrupted
                sb.append(agent.getCsvSearchReport());
                sb.append("\nSTOPPED BY USER");
            }
            String fileContent = sb.toString();
            if(fileContent.length() > 0)
                try {
                    Files.writeString(currentPath.toPath(), fileContent, StandardOpenOption.APPEND);
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

        if (!agent.hasBeenStopped() && !wasCancelled) {
            textArea.append("\n\nDone! All tests were saved in the chosen folder.");
        }else{
            textArea.append("\n\nAction stopped! All finished tests were saved in the chosen folder.");
        }

        buttonSolveAllTests.setEnabled(true);
        buttonTestALevel.setEnabled(true);
        buttonStop.setEnabled(false);
        buttonInitialState.setEnabled(true);
        buttonSolve.setEnabled(true);
        comboBoxSearchMethods.setEnabled(true);
        comboBoxHeuristics.setEnabled(true);
        agent.setSearchMethod((SearchMethod) comboBoxSearchMethods.getItemAt(comboBoxSearchMethods.getSelectedIndex()));
        agent.setHeuristic((Heuristic) comboBoxHeuristics.getItemAt(comboBoxHeuristics.getSelectedIndex()));
    }

    //It has to be synchronized because it is called from a different thread
    private synchronized void prepareFile(String path, String extraHeader) throws IOException {
        outputFiles.add(new File(path));
        Files.writeString(outputFiles.getLast().toPath(),
                extraHeader + FILE_HEADER, StandardOpenOption.CREATE);
        stringBuilders.add(new StringBuilder());
    }

    //It has to be synchronized because it is called from a different thread
    private synchronized void testOnFile(File file, boolean withLvlInfo) throws IOException {
        StringBuilder sb = stringBuilders.getLast();
        agent.readInitialStateFromFile(file);
        textArea.append("\nRunning tests on " + file.getName() + "...");
        for (int i = 0; i<agent.getSearchMethodsArray().length; i++) {

            if(i==3 || i==7) continue; //Don't run tests on the search methods Limited Depth First Search and Beam Search, like the teacher said.

            SearchMethod searchMethod = agent.getSearchMethodsArray()[i];
            Heuristic[] heuristics = agent.getHeuristicsArray();

            //If (index>4) the search method is informed therefore it should run a test for each heuristic available
            //If the search method is not informed it should only run this one time (h==0)
            for(int h = 0; (h<heuristics.length && ( h==0 || i>4 )); h++ ){

                //If the search method is not informed this will stay null
                Heuristic heuristic = null;

                textArea.append("\n\t" + searchMethod);

                //Only choose an heuristic if the search method is informed
                if(i>4){
                    heuristic = agent.getHeuristicsArray()[h];
                    textArea.append(" (" + heuristic + " )");
                }

                agent.setHeuristic(heuristic);

                agent.resetEnvironment(); //Reset the level for each test
                agent.setSearchMethod(searchMethod);

                textArea.append("...");
                MummyMazeProblem problem = new MummyMazeProblem(agent.getEnvironment().clone());
                sb.append("\n");
                if(withLvlInfo)
                    sb.append(file.getName()).append(";");
                try{
                    agent.solveProblem(problem);
                    if(agent.hasBeenStopped())
                        textArea.append(" Stopped.");
                    else
                        textArea.append(" Ok.");
                    sb.append(agent.getCsvSearchReport());
                }catch(Exception e){
                    textArea.append(" Not ok.");
                    sb.append(agent.getCsvSearchReport().replaceFirst("ERROR", ""));
                }
            }
        }
        stringBuilders.set(stringBuilders.size()-1, sb);
        textArea.append("\nFinished tests on " + file.getName() + ".\n");
    }

    public void buttonInitialState_ActionPerformed() {
        JFileChooser fc = new JFileChooser(new java.io.File("./Niveis"));
        try {
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                game.setState(agent.readInitialStateFromFile(fc.getSelectedFile()));
                buttonSolve.setEnabled(true);
                buttonShowSolution.setEnabled(false);
                buttonReset.setEnabled(false);
            }
        } catch (IOException e1) {
            e1.printStackTrace(System.err);
        } catch (NoSuchElementException e2) {
            JOptionPane.showMessageDialog(this, "File format not valid", "Error!", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void comboBoxSearchMethods_ActionPerformed() {
        int index = comboBoxSearchMethods.getSelectedIndex();
        agent.setSearchMethod((SearchMethod) comboBoxSearchMethods.getItemAt(index));
        game.setState(agent.resetEnvironment());
        buttonSolve.setEnabled(true);
        buttonShowSolution.setEnabled(false);
        buttonReset.setEnabled(false);
        textArea.setText("");
        comboBoxHeuristics.setEnabled(index > 4); //Informed search methods
        textFieldSearchParameter.setEnabled(index == 3 || index == 7); // limited depth or beam search
        labelSearchParameter.setEnabled(index == 3 || index == 7); // limited depth or beam search
    }

    public void comboBoxHeuristics_ActionPerformed() {
        int index = comboBoxHeuristics.getSelectedIndex();
        agent.setHeuristic((Heuristic) comboBoxHeuristics.getItemAt(index));
        game.setState(agent.resetEnvironment());
        buttonSolve.setEnabled(true);
        buttonShowSolution.setEnabled(false);
        buttonReset.setEnabled(false);
        textArea.setText("");
    }

    public void buttonSolve_ActionPerformed() {

        SwingWorker worker = new SwingWorker<Solution, Void>() {
            @Override
            public Solution doInBackground() {
                textArea.setText("");
                buttonStop.setEnabled(true);
                buttonSolve.setEnabled(false);
                buttonSolveAllTests.setEnabled(false);
                buttonInitialState.setEnabled(false);
                try {
                    prepareSearchAlgorithm();
                    MummyMazeProblem problem = new MummyMazeProblem(agent.getEnvironment().clone());
                    agent.solveProblem(problem);
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
                return null;
            }

            @Override
            public void done() {
                if (!agent.hasBeenStopped()) {
                    textArea.setText(agent.getSearchReport());
                    if (agent.hasSolution()) {
                        buttonShowSolution.setEnabled(true);
                    }
                }
                buttonSolve.setEnabled(true);
                buttonStop.setEnabled(false);
                buttonSolveAllTests.setEnabled(true);
                buttonInitialState.setEnabled(true);
            }
        };

        worker.execute();
    }

    public void buttonStop_ActionPerformed() {
        agent.stop();
        if(runningWorker != null)
            runningWorker.cancel(true);
        buttonShowSolution.setEnabled(false);
        buttonStop.setEnabled(false);
        buttonSolve.setEnabled(true);
    }

    public void buttonShowSolution_ActionPerformed() {
        buttonShowSolution.setEnabled(false);
        buttonStop.setEnabled(false);
        buttonSolve.setEnabled(false);
        buttonInitialState.setEnabled(false);
        buttonSolveAllTests.setEnabled(false);
        SwingWorker worker = new SwingWorker<Void, Void>() {
            @Override
            public Void doInBackground() {
                agent.executeSolution();
                buttonReset.setEnabled(true);
                return null;
            }

            @Override
            public void done() {
                buttonSolve.setEnabled(true);
                buttonInitialState.setEnabled(true);
                buttonSolveAllTests.setEnabled(true);
            }
        };
        worker.execute();
    }

    public void buttonReset_ActionPerformed() {
        game.setState(agent.resetEnvironment());
        buttonShowSolution.setEnabled(true);
        buttonReset.setEnabled(false);
    }

    private void prepareSearchAlgorithm() {
        if (agent.getSearchMethod() instanceof DepthLimitedSearch) {
            DepthLimitedSearch searchMethod = (DepthLimitedSearch) agent.getSearchMethod();
            searchMethod.setLimit(Integer.parseInt(textFieldSearchParameter.getText()));
        } else if (agent.getSearchMethod() instanceof BeamSearch) {
            BeamSearch searchMethod = (BeamSearch) agent.getSearchMethod();
            searchMethod.setBeamSize(Integer.parseInt(textFieldSearchParameter.getText()));
        }
    }
}

class ComboBoxSearchMethods_ActionAdapter implements ActionListener {

    private final MainFrame adaptee;

    ComboBoxSearchMethods_ActionAdapter(MainFrame adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        adaptee.comboBoxSearchMethods_ActionPerformed();
    }
}

class ComboBoxHeuristics_ActionAdapter implements ActionListener {

    private final MainFrame adaptee;

    ComboBoxHeuristics_ActionAdapter(MainFrame adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        adaptee.comboBoxHeuristics_ActionPerformed();
    }
}

class ButtonInitialState_ActionAdapter implements ActionListener {

    private final MainFrame adaptee;

    ButtonInitialState_ActionAdapter(MainFrame adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        adaptee.buttonInitialState_ActionPerformed();
    }
}

class ButtonSolve_ActionAdapter implements ActionListener {

    private final MainFrame adaptee;

    ButtonSolve_ActionAdapter(MainFrame adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        adaptee.buttonSolve_ActionPerformed();
    }
}

class ButtonStop_ActionAdapter implements ActionListener {

    private final MainFrame adaptee;

    ButtonStop_ActionAdapter(MainFrame adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        adaptee.buttonStop_ActionPerformed();
    }
}

class ButtonShowSolution_ActionAdapter implements ActionListener {

    private final MainFrame adaptee;

    ButtonShowSolution_ActionAdapter(MainFrame adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        adaptee.buttonShowSolution_ActionPerformed();
    }
}

class ButtonReset_ActionAdapter implements ActionListener {

    private final MainFrame adaptee;

    ButtonReset_ActionAdapter(MainFrame adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        adaptee.buttonReset_ActionPerformed();
    }
}

class ButtonSolveAllTests_ActionAdapter implements ActionListener {

    private final MainFrame adaptee;

    ButtonSolveAllTests_ActionAdapter(MainFrame adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            adaptee.buttonSolveAllTests_ActionPerformed();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

class ButtonTestALevel_ActionAdapter implements ActionListener {

    private final MainFrame adaptee;

    ButtonTestALevel_ActionAdapter(MainFrame adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            adaptee.buttonTestALevel_ActionPerformed();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

class TextFieldSearchParameter_KeyAdapter implements KeyListener {

    private final MainFrame adaptee;

    TextFieldSearchParameter_KeyAdapter(MainFrame adaptee) {
        this.adaptee = adaptee;
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        if (!Character.isDigit(c) || c == KeyEvent.VK_BACK_SPACE || c == KeyEvent.VK_DELETE) {
            e.consume();
        }
    }
}
