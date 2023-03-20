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
import java.io.File;
import java.io.IOException;
import java.util.List;

public class MusicPlayerDashboard implements ActionListener {
    private JTabbedPane MusicPlayerFeature;
    private JPanel HomeTab;
    private JPanel LibraryTab;
    private JPanel SearchTab;
    private JPanel InfoTab;
    private JButton backButton;
    private JButton startButton;
    private JButton lyricsButton;
    private JButton stopButton;
    private JSlider volumeSlider;
    private JProgressBar musicProgressBar;
    private JTextArea MusicInfo;
    private JTextPane textPane1;
    private JTable libraryTable;
    private JPanel MusicPlayer;
    private JPanel Music;
    private JFormattedTextField HomePageText;
    private JButton SYNCButton;
    private JLabel SyncMessage;
    private JScrollPane LibraryScroll;
    private JEditorPane selectedMusicBox;
    private JPanel SelectedPanel;
    private JTextArea lyricsDisplay;
    private JEditorPane musicTime;
    private JButton SearchButton;
    private JTextField SearchText;
    private JFormattedTextField lyricsRealtimeText;
    private JLabel lyricsRealtimeLabel;
    private DefaultTableModel LibraryTableModel;


    private Clip clip;

    public String csvPath = "./music_properties.csv";
    private MusicManager musicManager;

    public MusicManager getMusicManager(){
        if (musicManager == null){
            musicManager = new MusicManager();
        }
        return musicManager;
    }

    public void syncMusicInfo() {  // get the music management
        musicManager.reload();
        musicManager.saveMusicPropertyToFile(csvPath);
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

    public void loadLibraryTable() {
        LibraryTableModel.setRowCount(0);
        for (MusicManager.MusicProperty musicProperty : musicManager.getMusicInfo()) {
            LibraryTableModel.addRow(new Object[]{
                    musicProperty.title,
                    musicProperty.duration,
                    musicProperty.artist,
                    musicProperty.channels,
                    musicProperty.rate,
                    musicProperty.bits,
                    musicProperty.album,
                    musicProperty.genre,
                    musicProperty.year,
                    musicProperty.comment
            });
        }
    }

    public LrcFileReader.LrcLine getLrcLine(int ms) throws IOException {  // get the lrc line
        String lrcPath = targetMusic.Path.replace(".wav", ".lrc");

        LrcFileReader reader = new LrcFileReader(lrcPath);

        LrcFileReader.LrcLine prevLine = null;

        for (LrcFileReader.LrcLine line : reader.getLrcLines()) {
            if (line.getTimestamp() > ms) {
                return prevLine;
            }
            prevLine = line;
        }
        return null;
    }

    public void getLrcLines() throws IOException {  // get the lrc lines
        lyricsDisplay.setText("");

        String lrcPath = targetMusic.Path.replace(".wav", ".lrc");

        LrcFileReader reader = new LrcFileReader(lrcPath);

        String lrc = "";
        for (LrcFileReader.LrcLine line : reader.getLrcLines()) {
            System.out.println(line.getTimestamp() + " " + line.getLyrics());
            lrc += " " + line.getLyrics() + "\n";
        }
        lyricsDisplay.setText(lrc);
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

        startButton.addActionListener(this);
        stopButton.addActionListener(this);

    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == startButton) {
            try {
                if (clip != null && clip.isRunning()) {
                    clip.stop();
                    clip.setMicrosecondPosition(0);
                }
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(targetMusic.Path));
                clip = AudioSystem.getClip();
                clip.open(audioInputStream);

                // Set the maximum value of the progress bar to the length of the audio file
                musicProgressBar.setMaximum((int) clip.getMicrosecondLength() / 1000);

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
                        // Update the progress bar on the EDT;
                        int latestPosition = chunks.get(chunks.size() - 1);
                        musicProgressBar.setValue(latestPosition);
                        String endTime = getMMSSTime((int) clip.getMicrosecondLength() / 1000);
                        String startTime = getMMSSTime(latestPosition);
                        musicTime.setText(startTime + " / " + endTime);
                        System.out.println(latestPosition);

                        try {
                            LrcFileReader.LrcLine lrcLine = getLrcLine(latestPosition);
                            if (lrcLine != null) {
                                lyricsRealtimeText.setText(lrcLine.getLyrics());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            lyricsRealtimeText.setText("NONE! no lrc file");
                        }

                    }
                };

                worker.execute(); // start the SwingWorker

            } catch (Exception e) {
                e.printStackTrace();
                selectedMusicBox.setText("NONE! no music in the list (information Display)");
            }
        } else if (event.getSource() == stopButton) {
            clip.stop();
            clip.close();
            musicProgressBar.setValue(0);
            musicTime.setText("0:0 / 0:0");
        }
    }


    public static void main(String[] args) throws IOException {
        MusicPlayerDashboard player = new MusicPlayerDashboard();

    }

    private void createUIComponents() {
        // TODO: place custom component creation code here

        // create the lyricsButton
        lyricsButton = new JButton("Lyrics");
        lyricsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MusicPlayerFeature.setSelectedIndex(1); // switch to the Music tab
            }
        });
        // create lyricsButton end

        // create the table model
        libraryTable = new JTable();
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
        getMusicManager().loadFromCsv(csvPath);
        loadLibraryTable();
        libraryTable.setModel(LibraryTableModel);


        // add a selection listener to the table
        selectedMusicBox = new JEditorPane(); // assuming you have a JEditorPane instance
        libraryTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                // check if the selection is valid and not adjusting
                if (!event.getValueIsAdjusting() && libraryTable.getSelectedRow() != -1) {
                    // get the selected row data
                    Object selectedData = libraryTable.getValueAt(libraryTable.getSelectedRow(), 0); // assuming the data is in the first column

                    TargetMusic targetMusic = new TargetMusic();
                    MusicManager.MusicProperty selectedProperty = getMusicManager().getMusicInfo().get(libraryTable.getSelectedRow());
                    targetMusic.title = selectedProperty.title;
                    targetMusic.duration = String.valueOf(selectedProperty.duration);
                    targetMusic.author = selectedProperty.artist;
                    targetMusic.Path = selectedProperty.path;


                    setTargetMusic(targetMusic);
                    // set the selected data to the JEditorPane
                    selectedMusicBox.setText(targetMusic.title + " (" + targetMusic.Path + ") " + (targetMusic.author.isBlank() ? "(No Authors)" : targetMusic.author));

                    try {
                        System.out.println("get lrc lines");
                        getLrcLines();
                    } catch (IOException e) {
                        lyricsDisplay.setText("No Lyrics");
                    }
                }
            }
        });

        LibraryScroll = new JScrollPane(libraryTable);
        // create the table model end
        SYNCButton = new JButton("SYNC");
        SYNCButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("SYNC...");

                syncMusicInfo();
                loadLibraryTable();

            }
        });

        lyricsDisplay = new JTextArea();
        lyricsDisplay.setText("lyricsDisplay");
        lyricsDisplay.setEditable(false);

        SearchButton = new JButton("Search");
        SearchText = new JTextField("Search Here");

        SearchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String searchText = SearchText.getText();
                TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(libraryTable.getModel());
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
                libraryTable.setRowSorter(sorter);
            }
        });

    }
}
