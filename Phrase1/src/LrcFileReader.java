
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;



public class LrcFileReader {
    public class LrcLine {
        private final int timestamp;
        private final String lyrics;

        public LrcLine(int timestamp, String lyrics) {
            this.timestamp = timestamp;
            this.lyrics = lyrics;
        }

        public int getTimestamp() {
            return timestamp;
        }

        public String getLyrics() {
            return lyrics;
        }
    }


    private final List<LrcLine> lrcLines;



    public LrcFileReader(String filePath) throws IOException {
        lrcLines = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
        for (String line : lines) {
            lrcLines.add(parseLrcLine(line));
        }
    }

    private LrcLine parseLrcLine(String line) {
        // Implement parsing logic for a single LRC line here
        // For example, you could use regular expressions to extract the timestamp and lyrics from the line
        // and then create a new LrcLine object with the parsed values
        // Here's an example implementation assuming the LRC format is "[mm:ss.xx]lyrics"
        int timestampStart = line.indexOf("[");
        int timestampEnd = line.indexOf("]");
        String timestampString = line.substring(timestampStart + 1, timestampEnd);
        String lyrics = line.substring(timestampEnd + 1);
        return new LrcLine(parseTimestamp(timestampString), lyrics);
    }

    private int parseTimestamp(String timestampString) {
        // Implement parsing logic for a timestamp string in the format "mm:ss.xx"
        // For example, you could split the string on ":" and ".", convert the parts to integers,
        // and then calculate the total timestamp in milliseconds
        String[] parts = timestampString.split(":|\\.");
        int minutes = Integer.parseInt(parts[0]);
        int seconds = Integer.parseInt(parts[1]);
        int milliseconds = Integer.parseInt(parts[2]);
        return minutes * 60 * 1000 + seconds * 1000 + milliseconds;
    }

    public List<LrcLine> getLrcLines() {
        return lrcLines;
    }

    public static void main(String[] args) throws IOException {
        LrcFileReader reader = new LrcFileReader("src/Album/香港料理.lrc");
        for (LrcLine line : reader.getLrcLines()) {
            System.out.println(line.getTimestamp() + " " + line.getLyrics());
        }
    }
}

