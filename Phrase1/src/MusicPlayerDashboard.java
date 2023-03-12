import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class MusicPlayerDashboard  implements ActionListener  {
    private JTabbedPane MusicPlayerFeature;
    private JPanel HomeTab;
    private JPanel LibraryTab;
    private JPanel SearchTab;
    private JPanel InfoTab;
    private JButton BackButton;
    private JButton StartButton;
    private JButton LryicsButton;
    private JButton StopButton;
    private JSlider Volumn;
    private JProgressBar MusicProgressBar;
    private JTextArea MusicInfo;
    private JTextPane textPane1;
    private JTable LibraryTable;
    private JPanel MusicPlayer;
    private JPanel Music;
    private JFormattedTextField HomePageText;
    private JButton SYNCButton;
    private JTextField SyncMessage;
    private JScrollPane LibraryScroll;
    private JEditorPane SelectedBox;
    private JPanel SelectedPanel;
    private JTextArea LryicsDisplay;
    private JEditorPane MusicTime;
    private JButton SearchButton;
    private JTextField SearchText;
    private DefaultTableModel LibraryTableModel;


    private Clip clip;

    public String csvPath = "./music_properties.csv";
    public void getMusicManagement() {  // get the music management
        MusicManagement musicManagement = new MusicManagement();
        musicManagement.run();
    }

    class TargetMusic{
        public String title;
        public String Path;
        public String author;
        public String duration;
    }

    public TargetMusic targetMusic = new TargetMusic();

    public void setTargetMusic(TargetMusic targetMusic) {
        targetMusic.Path = targetMusic.Path.substring(targetMusic.Path.indexOf("src"));
        this.targetMusic = targetMusic;
    }

    public void getCsvToTable() throws IOException {  // get the csv file to table
        LibraryTableModel.setRowCount(0);

        BufferedReader br = new BufferedReader(new FileReader(csvPath));
        String line;
        line = br.readLine(); // skip first line
        while ((line = br.readLine()) != null) {
            String[] values = line.split(",");
            LibraryTableModel.addRow(values);
        }
        br.close();
    }

    public void getlrcLines() throws IOException {  // get the lrc lines
        LryicsDisplay.setText("");

        String lrcPath = targetMusic.Path.replace(".wav", ".lrc");

        LrcFileReader reader = new LrcFileReader(lrcPath);

        String lrc = "";
        for (LrcFileReader.LrcLine line : reader.getLrcLines()) {
            System.out.println(line.getTimestamp() + " " + line.getLyrics());
            lrc += " " + line.getLyrics() + "\n";
        }
        LryicsDisplay.setText(lrc);
    }

    public String getMMSSTime(int ms) {
        int min = ms / 1000 / 60;
        int sec = ms / 1000 % 60;
        return min + ":" + sec;
    }

    public MusicPlayerDashboard() throws IOException {
        JFrame frame  = new JFrame("MusicPlayerDashboard");
        frame.setContentPane(this.MusicPlayer);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        StartButton.addActionListener(this);
        StopButton.addActionListener(this);

    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == StartButton) {
            try {
                if (clip != null && clip.isRunning()) {
                    clip.stop();
                    clip.setMicrosecondPosition(0);
                }
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(targetMusic.Path));
                clip = AudioSystem.getClip();
                clip.open(audioInputStream);

                // Set the maximum value of the progress bar to the length of the audio file
                MusicProgressBar.setMaximum((int) clip.getMicrosecondLength() / 1000);

                // Create a SwingWorker to play the audio and update the progress bar in the background
                SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {

                    @Override
                    protected Void doInBackground() throws Exception {
                        clip.start();
                        System.out.println("latestPosition");
                        while (clip != null && clip.isRunning()) {
                            int position = (int) clip.getMicrosecondPosition() / 1000;
                            publish(position); // publish the position to update the progress bar on the EDT
                            Thread.sleep(1000);
                        }
                        return null;
                    }

                    @Override
                    protected void process(List<Integer> chunks) {
                        // Update the progress bar on the EDT
                        int latestPosition = chunks.get(chunks.size() - 1);
                        MusicProgressBar.setValue(latestPosition);
                        String endTime= getMMSSTime((int) clip.getMicrosecondLength() / 1000);
                        String startTime = getMMSSTime(latestPosition);
                        MusicTime.setText(startTime + " / " + endTime);
                        System.out.println(latestPosition);

                    }
                };

                worker.execute(); // start the SwingWorker

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (event.getSource() == StopButton) {
            clip.stop();
            clip.close();
            MusicProgressBar.setValue(0);
            MusicTime.setText("0:0 / 0:0");
        }
    }


    public static void main(String[] args) throws IOException {
        MusicPlayerDashboard player = new MusicPlayerDashboard();

    }

    private void createUIComponents() throws IOException {
        // TODO: place custom component creation code here

        // create the LryicsButton
        LryicsButton = new JButton("Lyrics");
        LryicsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MusicPlayerFeature.setSelectedIndex(1); // switch to the Music tab
            }
        });
        // create LryicsButton end

        // create the table model
        LibraryTable = new JTable();
        LibraryTableModel = new DefaultTableModel();
        LibraryTableModel.addColumn("Title");
        LibraryTableModel.addColumn("Duration");
        LibraryTableModel.addColumn("Author");
        LibraryTableModel.addColumn("Channels");
        LibraryTableModel.addColumn("Rate");
        LibraryTableModel.addColumn("Bits");
        LibraryTableModel.addColumn("Album");
        LibraryTableModel.addColumn("Genre");
        LibraryTableModel.addColumn("Year");
        LibraryTableModel.addColumn("Comment");
        getCsvToTable();
        LibraryTable.setModel(LibraryTableModel);


        // add a selection listener to the table
        SelectedBox = new JEditorPane(); // assuming you have a JEditorPane instance
        LibraryTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                // check if the selection is valid and not adjusting
                if (!event.getValueIsAdjusting() && LibraryTable.getSelectedRow() != -1) {
                    // get the selected row data
                    Object selectedData = LibraryTable.getValueAt(LibraryTable.getSelectedRow(), 0); // assuming the data is in the first column

                    TargetMusic targetMusic = new TargetMusic();
                    targetMusic.title = LibraryTable.getValueAt(LibraryTable.getSelectedRow(), 0).toString();
                    targetMusic.duration = LibraryTable.getValueAt(LibraryTable.getSelectedRow(), 1).toString();
                    targetMusic.author = LibraryTable.getValueAt(LibraryTable.getSelectedRow(), 2).toString();
                    targetMusic.Path = LibraryTable.getValueAt(LibraryTable.getSelectedRow(), 6).toString();


                    setTargetMusic(targetMusic);
                    // set the selected data to the JEditorPane
                    SelectedBox.setText(targetMusic.title + " " + targetMusic.Path + " " + targetMusic.author);

                    try {
                        System.out.println("get lrc lines");
                        getlrcLines();
                    } catch (IOException e) {
                        LryicsDisplay.setText("No Lyrics");
                    }
                }
            }
        });

        LibraryScroll = new JScrollPane(LibraryTable);
        // create the table model end
        SYNCButton = new JButton("SYNC");
        SYNCButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               System.out.println("SYNC...");

                getMusicManagement();
                try {
                    getCsvToTable();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

            }
        });

        LryicsDisplay = new JTextArea();
        LryicsDisplay.setText("LryicsDisplay");
        LryicsDisplay.setEditable(false);

          SearchButton = new JButton("Search");
        SearchText = new JTextField("Search Here");

        SearchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String searchText = SearchText.getText();
                TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(LibraryTable.getModel());
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
                LibraryTable.setRowSorter(sorter);
            }
        });

//        SearchButton.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                String searchText = SearchText.getText();
//                for (int i = 0; i < LibraryTable.getRowCount(); i++) {
//                    for (int j = 0; j < LibraryTable.getColumnCount(); j++) {
//                        if (LibraryTable.getValueAt(i, j).toString().contains(searchText)) {
//                            // highlight or select row
//                            LibraryTable.setRowSelectionInterval(i, i);
//                            return;
//                        }
//                    }
//                }
//            }
//        });




    }
}
