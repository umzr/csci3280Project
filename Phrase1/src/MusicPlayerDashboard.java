import music.MusicManager;
import music.MusicPlayer;
import music.MusicProperty;
import newnetwork.P2PMusicStreaming;

import javax.sound.sampled.*;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;
import java.util.function.Supplier;

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
    private JFormattedTextField serverIP;
    private JButton severOKBtn;
    private JButton P2PconnectButton;
    private JLabel serverLabel;
    private JLabel clientLabel;
    private JFormattedTextField clientIP;
    private JButton clientOKBtn;
    private JButton P2PSync;
    private JFormattedTextField LibraryLocationPath;
    private JButton NOUSEButton;
    private JLabel CSVPath;
    private JFormattedTextField ClientName;

    private DefaultTableModel LibraryTableModel;

    private Clip clip;
    private MusicPlayer player;

    public String csvPath = "./music_properties.csv";
    private MusicManager musicManager;
    public String AlbumPath = "./src/Album";
    public String userName = "Client1";
    public String serverIPaddress = "tcp://localhost:4444"; // default server address
    public String clientIPaddress = "tcp://localhost:5555"; // default client address

    public MusicManager getMusicManager() {
        if (musicManager == null) {
            musicManager = new MusicManager();
        }
        return musicManager;
    }

    public void setMusicManager(MusicManager musicManager) {
        this.musicManager = musicManager;
    }
    public void syncMusicInfo() { // get the music management
        musicManager.reload(AlbumPath);
        System.out.println(AlbumPath);
        musicManager.saveMusicPropertyToFile(csvPath);
        System.out.println(csvPath);
    }

    class TargetMusic {
        public MusicProperty property;
        public String title;
        public String Path;
        public String author;
        public float duration;

        public String tcpClient;
    }

    public TargetMusic targetMusic = new TargetMusic();

    public void setTargetMusic(TargetMusic targetMusic) {
        targetMusic.Path = targetMusic.Path.substring(targetMusic.Path.indexOf("src"));
        this.targetMusic = targetMusic;
    }

    public void loadLibraryTableFromSearch(ArrayList<MusicProperty> musicProperties) {
        LibraryTableModel.setRowCount(0);
        musicManager.setMusicInfo(musicProperties);
        for (MusicProperty musicProperty : musicProperties) {
            LibraryTableModel.addRow(new Object[] {
                    musicProperty.title,
                    musicProperty.duration,
                    musicProperty.artist,
                    musicProperty.channels,
                    musicProperty.rate,
                    musicProperty.bits,
                    musicProperty.album,
                    musicProperty.genre,
                    musicProperty.year,
                    musicProperty.comment,
                    musicProperty.ftpPath
            });
        }

    }
    public void loadLibraryTable() {
        LibraryTableModel.setRowCount(0);
        for (MusicProperty musicProperty : musicManager.getMusicInfo()) {
            LibraryTableModel.addRow(new Object[] {
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

    public P2PMusicStreaming app = null;

    public void setApp(P2PMusicStreaming app) {
        this.app = app;
    }
     public P2PMusicStreaming getApp() {
            return app;
        }

    public LrcFileReader.LrcLine getLrcLine(int ms) throws IOException { // get the lrc line
        String lrcPath = targetMusic.Path.replace(".wav", ".lrc");
        if (targetMusic.property == null || !targetMusic.property.hasLrc)
            return null;

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

    public void getLrcLines() throws IOException { // get the lrc lines
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

    public String getFormattedTimeMs(int ms) {
        int min = ms / 1000 / 60;
        int sec = ms / 1000 % 60;
        return String.format("%d:%02d", min, sec);
    }

    public void setVolume() {
        player.setVolume((float) volumeSlider.getValue() / volumeSlider.getMaximum());
//        if (clip != null) {
//            FloatControl ctrl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
//            if (volumeSlider.getValue() == 0) {
//                ctrl.setValue(ctrl.getMinimum());
//            } else {
//                float db = 20 * (float) Math.log10((float) volumeSlider.getValue() / volumeSlider.getMaximum());
//                db = Math.max(db, ctrl.getMinimum());
//                ctrl.setValue(db);
//            }
//        }
    }

    public boolean isMusicLocal(TargetMusic music){
        return music.tcpClient == null || music.tcpClient.isEmpty() || clientIPaddress.equals(music.tcpClient);
    }

    public MusicPlayerDashboard() {

    }

    private void init(){
        JFrame frame = new JFrame("MusicPlayerDashboard");
        frame.setContentPane(this.MusicPlayer);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        startButton.addActionListener(this);
        stopButton.addActionListener(this);

        volumeSlider.addChangeListener(e -> {
            setVolume();
        });

        player = new MusicPlayer(this::getApp);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == startButton) {
            try {
//                if (clip != null && clip.isRunning()) {
//                    clip.stop();
//                    clip.setMicrosecondPosition(0);
//                }
                if(player.isPlaying()){
                    player.stop();
                }

                player.startPlayMusic(targetMusic.property, isMusicLocal(targetMusic));
//                AudioInputStream audioInputStream;
//                if(isMusicLocal(targetMusic)){
//                    audioInputStream = AudioSystem.getAudioInputStream(new File(targetMusic.Path));
//                }
//                else{
//
//                }
//                clip = AudioSystem.getClip();
//                clip.open(audioInputStream);
//                setVolume();

                // Set the maximum value of the progress bar to the length of the audio file
                musicProgressBar.setMaximum((int) targetMusic.duration);
//                musicProgressBar.setMaximum((int) clip.getMicrosecondLength() / 1000);

                // Create a SwingWorker to play the audio and update the progress bar in the
                // background
                SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {

                    @Override
                    protected Void doInBackground() throws Exception {
//                        clip.start();
                        System.out.println("latestPosition");
                        boolean oncePlayed = false;
                        while(player.isOpen()){
                            if (oncePlayed && !player.isPlaying())
                                break;
                            if (!oncePlayed && player.isPlaying())
                                oncePlayed = true;
                            int position = (int) player.getCurrentTime();
                            publish(position); // publish the position to update the progress bar on the EDT
                            Thread.sleep(1000);
                        }
//                        while (clip != null && clip.isOpen()) {
//                            // it is not necessary that the clip is running right after clip.start() is
//                            // called,
//                            // therefore we have a flag such that the loop gets break only if the clip was
//                            // running
//                            // and stopped
//                            if (oncePlayed && !clip.isRunning())
//                                break;
//                            if (!oncePlayed && clip.isRunning())
//                                oncePlayed = true;
//                            int position = (int) clip.getMicrosecondPosition() / 1000;
//                            publish(position); // publish the position to update the progress bar on the EDT
//                            Thread.sleep(1000);
//                        }
                        return null;
                    }

                    @Override
                    protected void process(List<Integer> chunks) {
                        // Update the progress bar on the EDT;
                        int latestPosition = chunks.get(chunks.size() - 1);
                        musicProgressBar.setValue(latestPosition);
                        String endTime = getFormattedTimeMs((int) targetMusic.duration * 1000);
                        String startTime = getFormattedTimeMs(latestPosition * 1000);
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
            player.stop();
            player.close();
//            clip.stop();
//            clip.close();
            musicProgressBar.setValue(0);
            musicTime.setText("0:00 / 0:00");
        }
    }

    public static void main(String[] args) {
        MusicPlayerDashboard player = new MusicPlayerDashboard();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if(player.getApp() != null){
                CompletableFuture.runAsync(() -> player.getApp().unregisterWithTracker());
            }
        }));
        player.init();
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
        LibraryTableModel.addColumn("TCP");
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
                    Object selectedData = libraryTable.getValueAt(libraryTable.getSelectedRow(), 0); // assuming the
                                                                                                     // data is in the
                                                                                                     // first column

                    TargetMusic targetMusic = new TargetMusic();
                    MusicProperty selectedProperty = getMusicManager().getMusicInfo()
                            .get(libraryTable.getSelectedRow());
                    targetMusic.property = selectedProperty;
                    targetMusic.title = selectedProperty.title;
                    targetMusic.duration = selectedProperty.duration;
                    targetMusic.author = selectedProperty.artist;
                    targetMusic.Path = selectedProperty.path;
                    targetMusic.tcpClient = selectedProperty.ftpPath;
                    System.out.println("tcp is " + targetMusic.tcpClient);
                    System.out.println("clientIPaddress is " + clientIPaddress);
                    if(!MusicPlayerDashboard.this.isMusicLocal(targetMusic)) {
                        System.out.println("tcp is null");

                        setTargetMusic(targetMusic);
                        // set the selected data to the JEditorPane
                        selectedMusicBox.setText(targetMusic.title + " (" + targetMusic.Path + ") "
                                + (targetMusic.author.isBlank() ? "(No Authors)" : targetMusic.author) + " (non local music)");
//                        selectedMusicBox.setText(targetMusic.title +"NONE! this is not your local music (information Display)");



                    }else {
                        setTargetMusic(targetMusic);
                        // set the selected data to the JEditorPane
                        selectedMusicBox.setText(targetMusic.title + " (" + targetMusic.Path + ") "
                                + (targetMusic.author.isBlank() ? "(No Authors)" : targetMusic.author));

                        try {
                            System.out.println("get lrc lines");
                            getLrcLines();
                        } catch (IOException e) {
                            lyricsDisplay.setText("No Lyrics");
                        }
                    }
                }
            }
        });

        LibraryLocationPath = new JFormattedTextField();
        /*LibraryLocationPath.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("LibraryLocationPath");
                String path = LibraryLocationPath.getText();
                AlbumPath = path;
                System.out.println(AlbumPath);
            }
        });*/

        ClientName = new JFormattedTextField();
        ClientName.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("ClientName");
                String name = ClientName.getText();
                userName = name;
                System.out.println(userName);
                csvPath = AlbumPath + "/" + userName + ".csv";
                System.out.println("csvPath: " + csvPath);
            }
        });

        LibraryScroll = new JScrollPane(libraryTable);
        // create the table model end
        SYNCButton = new JButton("SYNC");
        SYNCButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("SYNC...");
                String path = LibraryLocationPath.getText();
                AlbumPath = path;
                System.out.println("LibraryLocationPath: "+AlbumPath);
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

        serverIP = new JFormattedTextField();
        /*serverIP.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("serverIP");
                String ip = serverIP.getText();
                serverIPaddress = ip;
                System.out.println("serverIPaddress: "+ serverIPaddress);
            }
        });*/

        clientIP = new JFormattedTextField();
        /*clientIP.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("clientIP");
                String ip = clientIP.getText();
                clientIPaddress = ip;
                System.out.println("clientIPaddress: "+ clientIPaddress);
            }
        });*/

        P2PconnectButton = new JButton("P2Pconnect");
        P2PconnectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String serverip = serverIP.getText();
                serverIPaddress = serverip;
                System.out.println("serverIP: "+serverIPaddress );
                String clientip = clientIP.getText();
                clientIPaddress = clientip;
                System.out.println("clinetIP: "+clientIPaddress);
                System.out.println("P2Pconnect");

                if(!serverIPaddress.isEmpty() && !clientIPaddress.isEmpty()){
                    if(MusicPlayerDashboard.this.app != null){
                        MusicPlayerDashboard.this.app.close();
                    }
                    P2PMusicStreaming app = P2PMusicStreaming.run(serverIPaddress, clientIPaddress);
                    setApp(app);
                    syncMusicInfo();
                    app.setMusicManager(getMusicManager());
                }else{
                    System.out.println("IP is null");
                }
            }
        });

        P2PSync = new JButton("P2PSync");
        P2PSync.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("P2PSync");

                P2PMusicStreaming app = getApp();
                List<String> onlinePeers = app.getOnlinePeers(serverIPaddress);
                System.out.println("onlinePeers: " + onlinePeers);



                HashSet<MusicProperty> peerMusicSet = new HashSet<MusicProperty>();
                for (String peer : onlinePeers) {
                    System.out.println("peer: " + peer);
                    ArrayList<MusicProperty> recvMusicList = app.sendSearchRequest("Na", peer);
                    System.out.println("recvMusicList: " + recvMusicList);
                    // Use addAll method to add received music properties to the HashSet
                    peerMusicSet.addAll(recvMusicList);
                }
                ArrayList<MusicProperty> peerMusicLists = new ArrayList<MusicProperty>(peerMusicSet);



                /*
                // Initialize peerMusicLists outside the loop
                ArrayList<MusicProperty> peerMusicLists = new ArrayList<MusicProperty>();

                for (String peer : onlinePeers) {
                    System.out.println("peer: " + peer);
                    ArrayList<MusicProperty> recvMusicList = app.sendSearchRequest("Na", peer);
                    System.out.println("recvMusicList: " + recvMusicList);
                    peerMusicLists.addAll(recvMusicList);
                }
                */

                System.out.println("peerMusicLists: output");
                for(MusicProperty music: peerMusicLists){
                    System.out.println(music.title);
                }
                musicManager.setMusicInfo(peerMusicLists);
                loadLibraryTable();
//                loadLibraryTableFromSearch(peerMusicLists);

            }
        });
    }
}
