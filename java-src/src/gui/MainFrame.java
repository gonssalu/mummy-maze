package gui;

import agent.Heuristic;
import agent.Solution;
import mummymaze.MummyMazeAgent;
import mummymaze.MummyMazeProblem;
import mummymaze.MummyMazeState;
import mummymaze.util.TileType;
import searchmethods.BeamSearch;
import searchmethods.DepthLimitedSearch;
import searchmethods.SearchMethod;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.NoSuchElementException;

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
    private final JButton buttonSolveAllTests = new JButton("Solve all tests");
    private JComboBox comboBoxSearchMethods;
    private JComboBox comboBoxHeuristics;
    private JTextArea textArea;
    private GameArea game;

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

        pack();
    }

    public void buttonSolveAllTests_ActionPerformed() throws IOException {
        JFileChooser fc = new JFileChooser(new java.io.File("../"));

        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        if(fc.getSelectedFile().exists())
            fc.getSelectedFile().delete();
        Files.writeString(fc.getSelectedFile().toPath(),
                "Level;Search Algorithm;Heuristic;Beam/Limit Size;Solution Cost;Num of Expanded Nodes;Max Frontier Size;Num of Generated States");

        try {

            /* FOR EVERY FILE IN ../materials/niveis */
            String levelName = "nivel1.txt";
            game.setState(agent.readInitialStateFromFile(new File("../materials/Niveis/"+levelName)));
            buttonShowSolution.setEnabled(false);
            buttonReset.setEnabled(false);
            buttonInitialState.setEnabled(false);
            buttonSolve.setEnabled(false);
            SwingWorker worker = new SwingWorker<Solution, Void>() {
                @Override
                public Solution doInBackground() {
                    textArea.setText("");
                    buttonStop.setEnabled(true);
                    buttonSolveAllTests.setEnabled(false);
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
                        textArea.setText("Ran " + levelName + "\n");
                        StringBuilder sb = new StringBuilder();
                        sb.append("\n").append(levelName).append(";").append(agent.getCsvSearchReport());
                        String fileContent = sb.toString();
                        try {
                            Files.writeString(fc.getSelectedFile().toPath(), fileContent);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    buttonSolveAllTests.setEnabled(true);
                    buttonStop.setEnabled(false);
                    buttonInitialState.setEnabled(true);
                    buttonSolve.setEnabled(true);
                }
            };

            worker.execute();



            /*ENDS HERE*/
        } catch (IOException e1) {
            e1.printStackTrace(System.err);
        } catch (NoSuchElementException e2) {
            JOptionPane.showMessageDialog(this, "File format not valid", "Error!", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void buttonInitialState_ActionPerformed() {
        JFileChooser fc = new JFileChooser(new java.io.File("../materials/Niveis"));
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
                buttonInitialState.setEnabled(true);
            }
        };

        worker.execute();
    }

    public void buttonStop_ActionPerformed() {
        agent.stop();
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
