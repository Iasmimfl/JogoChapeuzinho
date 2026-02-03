import javax.sound.sampled.*;
import java.io.File;

public class TesteSom {
    public static void main(String[] args) throws Exception {
        File file = new File("sons/tela_inicial_som.wav");
        System.out.println("Arquivo existe? " + file.exists() + " - " + file.getAbsolutePath());
        AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);
        Clip clip = AudioSystem.getClip();
        clip.open(audioIn);
        clip.start();
        System.out.println("Som tocando por 5 segundos...");
        Thread.sleep(5000);
        clip.stop();
        clip.close();
        System.out.println("Fim do teste.");
    }
}
