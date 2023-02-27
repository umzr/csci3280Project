import javax.swing.*;

public class Main {
    public static void main(String[] args) {


        MainFrame run = new MainFrame();

        run.setContentPane(run.MainGui);
        run.setTitle("CSCI3280 Project Phrase 1 Music Player");
        run.setSize(800, 600);
        run.setVisible(true);
        run.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


    }
}