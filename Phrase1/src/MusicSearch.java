import java.io.File;
import java.util.ArrayList;
public class MusicSearch {

    public ArrayList<String> wavFiles = new ArrayList<String>();
    public void print(String message){
        System.out.println(message);
    }
    public void SearchMusicFiles(String path){
        File file = new File(path);
        File[] files = file.listFiles();
        for (File f : files){
            if (f.isDirectory()){
                SearchMusicFiles(f.getAbsolutePath());
            }else{
                if (f.getName().endsWith(".wav")){
                    wavFiles.add(f.getAbsolutePath());
                }
            }
        }
    }

    public static void main(String[] args) {

        String FolderPath = "./src";
        MusicSearch run = new MusicSearch();
        run.SearchMusicFiles(FolderPath);


        ArrayList<String> wavFiles1 = run.wavFiles;

        MusicInfo music = new MusicInfo();
        music.getMusicDetail(wavFiles1);


        music.printMusicInfo();
        music.saveMusicInfoToFile("musicInfo.txt");
        run.print("Total number of wav files: " + run.wavFiles.size());

        run.print("Done!");

    }
}