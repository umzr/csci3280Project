import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

public class MainFrame extends JFrame{
    JPanel MainGui;
    private JPanel Control;
    private JPanel Setting;
    private JPanel displayLryics;
    private JProgressBar palyProgressBar;
    private JSplitPane Body;
    private JButton pauseButton;
    private JButton Backward;
    private JLabel Start;
    private JLabel End;
    private JButton StopNStart;
    private JSlider Vol;
    private JButton stopButton;
    private JPanel ProgressBar;

    public JList musicList;

    private JList musicTable;
    private JScrollBar scrollBar1;
    private JTable musicLists;
    private JTextField searchField;
    private JButton searchButton;
    private DefaultTableModel tableModel;

    public String musicName;
    public String musicPath = "/Users/icerxm/IdeaProjects/csci3280project/src/album/HongKongCooking.wav";
    MusicControl player = new MusicControl(new File(musicPath));

    public MainFrame() {


        tableModel = new DefaultTableModel();
        tableModel.addColumn("Title");
        tableModel.addColumn("Duration");
        tableModel.addColumn("Author");
        tableModel.addColumn("Channels");
        tableModel.addColumn("Sample Rate");
        tableModel.addColumn("Sample Size");
        tableModel.addColumn("Album");
        tableModel.addColumn("Genre");
        tableModel.addColumn("Year");
        tableModel.addColumn("Comment");
        musicLists.setModel(tableModel);

        palyProgressBar = new JProgressBar();
        palyProgressBar.setMinimum(0);
        palyProgressBar.setMaximum(100);
        palyProgressBar.setStringPainted(true);

        musicLists.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent event) {
                System.out.println(musicLists.getValueAt(musicLists.getSelectedRow(), 0).toString());
                musicName = musicLists.getValueAt(musicLists.getSelectedRow(), 0).toString();
                String currentPath = System.getProperty("user.dir");
                System.out.println("Current path: " + currentPath);
                System.out.println("/album/"+musicName);
                player = new MusicControl(new File(currentPath + "/src/album/"+musicName));
            }
        });

        StopNStart.addActionListener(e -> {
            player.execute();
        });
        pauseButton.addActionListener(e -> {
            player.pausePlayback();
        });
        stopButton.addActionListener(e -> {
            player.stopPlayback();
        });

        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String searchText = searchField.getText();
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        if (tableModel.getValueAt(i, j).toString().contains(searchText)) {
                            // highlight or select row
                            musicLists.setRowSelectionInterval(i, i);
                            return;
                        }
                    }
                }
            }
        });

    }

    public void setMusicInfo(ArrayList<MusicInfo.MusicProperty> musicInfo) {
        tableModel.setRowCount(0);

        for (MusicInfo.MusicProperty info : musicInfo) {
            System.out.println(info.title);

            tableModel.addRow(new Object[]{
                    info.title,
                    info.duration,
                    info.author,
                    info.channels,
                    info.rate,
                    info.bits,
                    info.album,
                    info.genre,
                    info.year,
                    info.comment
            });
        }
    }


    public static void main(String[] args){

        MusicInfo musicInfo = new MusicInfo();
        musicInfo.readMusicInfoFromFile("musicInfo.txt");
        MainFrame run = new MainFrame();
        run.setMusicInfo(musicInfo.getMusicList());
        run.setTitle("CSCI3280 Project Phrase 1 Music Player");
        run.setSize(800, 600);
        run.setVisible(true);
        run.setContentPane(run.MainGui);
        run.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }


    private void createUIComponents() {
        // TODO: place custom component creation code here


    }
}
