import javax.swing.JFrame;

public class JogoChapeuzinho {

    public static void main(String[] args) {

        JFrame janela = new JFrame("Chapeuzinho Vermelho");
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Define tamanho fixo da janela
        janela.setSize(1000, 750);
        janela.setResizable(false);
        janela.setLocationRelativeTo(null);
        PainelJogo jogo = new PainelJogo();
        janela.add(jogo);

        janela.setVisible(true);

    }
}
