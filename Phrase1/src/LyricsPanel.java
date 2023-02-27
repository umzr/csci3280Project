
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JTextArea;

public class LyricsPanel extends JTextArea {
    private String lyrics;

    public LyricsPanel() {
        setEditable(false);
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
        setText(lyrics);
    }

    public void loadLyricsFromFile(String fileName) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line = reader.readLine();
        while (line != null) {
            sb.append(line);
            sb.append("\n");
            line = reader.readLine();
        }
        reader.close();
        setLyrics(sb.toString());
    }


}
