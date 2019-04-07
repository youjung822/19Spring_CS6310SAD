import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class Main {
    private static ImageIcon puppyIcon = loadIcon("images/puppy.png");
    private static ImageIcon craterIcon = loadIcon("images/crater.png");

    private static ImageIcon loadIcon(String path) {
        try {
            return new ImageIcon(Main.class.getResource(path));
        } catch (NullPointerException ex) {
            return new ImageIcon(path);
        }
    }

    public static void main(String[] args) {
        SimulationMonitor monitor = new SimulationMonitor();

        JFrame frame = new JFrame("OsMowSis");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1500, 900);

        frame.setLayout(new BorderLayout());

        // Summary Panel
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new GridLayout(1, 8));

        JLabel turnLabel = new JLabel();
        turnLabel.setText("Turns Taken:");
        summaryPanel.add(turnLabel);
        JTextField turnField = new JTextField();
        turnField.setText("0");
        turnField.setEditable(false);
        summaryPanel.add(turnField);

        JLabel grassRemLabel = new JLabel();
        grassRemLabel.setText("Grass Squares Remaining:");
        summaryPanel.add(grassRemLabel);
        JTextField grassRemField = new JTextField();
        grassRemField.setText("0");
        grassRemField.setEditable(false);
        summaryPanel.add(grassRemField);

        JLabel grassCutLabel = new JLabel();
        grassCutLabel.setText("Grass Squares Cut:");
        summaryPanel.add(grassCutLabel);
        JTextField grassCutField = new JTextField();
        grassCutField.setText("0");
        grassCutField.setEditable(false);
        summaryPanel.add(grassCutField);

        JLabel nextTurnLabel = new JLabel();
        nextTurnLabel.setText("Next Turn:");
        summaryPanel.add(nextTurnLabel);
        JTextField nextTurnField = new JTextField();
        nextTurnField.setText("");
        nextTurnField.setEditable(false);
        summaryPanel.add(nextTurnField);

        frame.add(summaryPanel, BorderLayout.PAGE_START);

        // Lawn Panel
        JPanel lawnPanel = new JPanel();
        frame.add(lawnPanel, BorderLayout.CENTER);

        // Status Panel

        JPanel statusPanel = new JPanel();
        frame.add(statusPanel, BorderLayout.LINE_END);

        // Button Panel

        JPanel buttonPanel = new JPanel();
        JButton nextButton = new JButton("Next");
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (monitor.isStopped()) {
                    JOptionPane.showMessageDialog(frame, "Simulation has ended");
                }
                try {
                    monitor.next();
                    render(turnField, monitor, grassCutField, grassRemField, nextTurnField, lawnPanel, statusPanel, frame);
                } catch (Exception ex) {
                    //do nothing
                }
            }
        });

        buttonPanel.add(nextButton);

        JButton stopButton = new JButton("Stop");
        buttonPanel.add(stopButton);
        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    System.out.println(monitor.report());
                } catch (Exception ex) {
                    //do nothing
                }
            }
        });

        JButton ffButton = new JButton("Fast-Forward");
        ffButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (monitor.isStopped()) {
                    JOptionPane.showMessageDialog(frame, "Simulation has ended");
                }
                try {
                    monitor.fastForward();
                    render(turnField, monitor, grassCutField, grassRemField, nextTurnField, lawnPanel, statusPanel, frame);
                } catch (Exception ex) {
                    //do nothing
                }
            }
        });
        buttonPanel.add(ffButton);

        frame.add(buttonPanel, BorderLayout.PAGE_END);

        //Menu
        JMenuBar mb = new JMenuBar();
        JMenu menu = new JMenu("File");
        JMenuItem newMI = new JMenuItem("Open");
        newMI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Input File", "csv");
                JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                jfc.setFileFilter(filter);
                int returnValue = jfc.showOpenDialog(null);

                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = jfc.getSelectedFile();
                    frame.setTitle("OsMowSis: " + selectedFile.getName());
                    monitor.setupUsingFile(selectedFile.getAbsolutePath());
                    render(turnField, monitor, grassCutField, grassRemField, nextTurnField, lawnPanel, statusPanel, frame);
                }
            }
        });
        menu.add(newMI);

        menu.addSeparator();

        JMenuItem quitMI = new JMenuItem("Quit");
        quitMI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        menu.add(quitMI);

        mb.add(menu);
        frame.setJMenuBar(mb);

        frame.setVisible(true);
    }

    private static void render(JTextField turnField, SimulationMonitor monitor, JTextField grassCutField, JTextField grassRemField, JTextField nextTurnField, JPanel lawnPanel, JPanel statusPanel, JFrame frame) {
        turnField.setText(monitor.getTurnCount() + "");
        grassCutField.setText(monitor.getGrassCut() + "");
        grassRemField.setText(monitor.getGrassRemaining() + "");
        Object no = monitor.getNextObject();
        if (no instanceof Mower) {
            nextTurnField.setText("Mower: " + ((Mower) no).getId());
        } else {
            nextTurnField.setText("Puppy: " + ((Puppy) no).getId());
        }

        //lawn panel
        lawnPanel.removeAll();

        lawnPanel.setLayout(new GridLayout(monitor.getLawnHeight() + 1, monitor.getLawnWidth() + 1));

        for (int y = monitor.getLawnHeight() - 1; y >= 0; y--) {
            lawnPanel.add(getSquare(y + "", Color.lightGray, 1));
            for (int x = 0; x < monitor.getLawnWidth(); x++) {

                Location l = new Location(x, y);
                Square sq = monitor.getSquare(l);

                int border = 1;
                if (no instanceof Mower) {
                    if (l.equals(((Mower) no).getLocation())) {
                        border = 6;
                    }
                } else {
                    if (l.equals(((Puppy) no).getLocation())) {
                        border = 6;
                    }
                }

                JLabel label = null;

                if (sq instanceof GrassSquare && ((GrassSquare) sq).isEmpty()) {
                    label = getSquare("", Color.white, border);
                } else if (sq instanceof GrassSquare && !((GrassSquare) sq).isEmpty()) {
                    label = getSquare("", Color.GREEN, border);
                } else if (sq instanceof CraterSquare) {
                    label = getSquare("", Color.darkGray, 1);
                    label.setIcon(craterIcon);
                }

                for (Puppy puppy : monitor.getPuppies()) {
                    if (l.equals(puppy.getLocation())) {
                        label.setIcon(puppyIcon);
                    }
                }

                for (Mower mower : monitor.getMowers()) {
                    if (l.equals(mower.getLocation())) {
                        Direction d = mower.getDirection();
                        int id = mower.getId();
                        label.setText("M" + id + " " + d.getShortName());
                        label.setFont(label.getFont().deriveFont(Font.BOLD));

                    }
                }

                lawnPanel.add(label);
            }
        }

        lawnPanel.add(new JLabel());

        for (int x = 0; x < monitor.getLawnWidth(); x++) {
            lawnPanel.add(getSquare(x + "", Color.lightGray, 1));
        }

        //Status panel
        statusPanel.removeAll();

        String[] columnNames = {"Mower", "Status", "Remaining Turns Stalled"};

        Object[][] data = new Object[monitor.getMowers().size()][3];

        for (int i = 0; i < monitor.getMowers().size(); i++) {
            Mower m = monitor.getMowers().get(i);
            data[i] = new Object[]{m.getId(), m.getStatus(), m.getRemainingTurnsStalled() > 0 ? m.getRemainingTurnsStalled() + "" : ""};
        }

        JTable table = new JTable(data, columnNames) {
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component returnComp = super.prepareRenderer(renderer, row, column);
                Color alternateColor = Color.lightGray;
                Color whiteColor = Color.WHITE;
                if (!returnComp.getBackground().equals(getSelectionBackground())) {
                    Color bg = (row % 2 == 0 ? alternateColor : whiteColor);
                    returnComp.setBackground(bg);
                    bg = null;
                }
                return returnComp;
            }
        };
        statusPanel.setLayout(new BorderLayout());
        statusPanel.add(table.getTableHeader(), BorderLayout.PAGE_START);
        statusPanel.add(table, BorderLayout.CENTER);

        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(0).setMaxWidth(100);
        table.getColumnModel().getColumn(0).setMaxWidth(200);

        frame.repaint();
        frame.setVisible(true);
    }


    private static JLabel getSquare(String text, Color c, int border) {
        JLabel label = new JLabel();
        label.setText(text);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setOpaque(true);
        label.setBackground(c);
        if (border == 1) {
            label.setBorder(BorderFactory.createLineBorder(Color.BLACK, border));
        } else if (border == 6) {
            label.setBorder(BorderFactory.createLineBorder(Color.RED, border));
        }
        return label;
    }
}
