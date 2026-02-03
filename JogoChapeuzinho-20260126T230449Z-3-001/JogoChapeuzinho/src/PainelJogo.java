// Motivo do Game Over: null = padrão, "loboEspecial" = pego pelo lobo marrom/preto
import javax.swing.JPanel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.FontMetrics;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.util.Random;
import java.util.Stack;
import java.util.ArrayList;
import java.util.List;
import java.awt.Rectangle;
import javax.sound.sampled.*;
import java.io.IOException;

public class PainelJogo extends JPanel implements KeyListener {
    // Controle para evitar sobreposição de som dos lobos comuns
    private long ultimoSomLobo = 0;
    private static final int INTERVALO_SOM_LOBO_MS = 400; // mínimo 0.4s entre sons

    private void tocarSomLoboComum() {
        if (!somLigado) return;
        long agora = System.currentTimeMillis();
        if (agora - ultimoSomLobo < INTERVALO_SOM_LOBO_MS) return;
        ultimoSomLobo = agora;
        try {
            java.net.URL somUrl = getClass().getResource("/sons/wrong-100536.wav");
            if (somUrl != null) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(somUrl);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
            }
        } catch (Exception e) {
            System.out.println("[ERRO] Falha ao tocar som de lobo comum: " + e);
        }
    }
    private void tocarSomBonus() {
        if (!somLigado) return;
        try {
            java.net.URL somUrl = getClass().getResource("/sons/bonus_one.wav");
            if (somUrl != null) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(somUrl);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
            }
        } catch (Exception e) {
            System.out.println("[ERRO] Falha ao tocar som de bônus: " + e);
        }
    }
    private Image telaGameOverImg;
    private Image telaVitoriaImg;

    // Motivo do Game Over: null = padrão, "loboEspecial" = pego pelo lobo marrom/preto
    private String motivoGameOver = null;

    private Image chapeuzinho;
    private Image arvore;
    private Image pedra;
    private Image casaChapeuzinho;
    private Image casaVovo;
    private Image caminho;
    private Image florAmarela;
    private Image morango;
    private Image coracao;
    private int pontuacao = 0; //Placar geral
    private int vidas = 3;
    private boolean temFlorAmarela = true; //Controla se a flor aparece ou não
    private boolean temMorango = true;
    private Image lobo;
    private List<Lobo> listaLobos = new ArrayList<>();
    private Image loboMarrom;
    private Image loboPreto;
    private List<LoboEspecial> lobosEspeciais = new ArrayList<>();
    private Image florAzul;
    private Image maca;
    private Image florRosa;
    private Image pirulito;
    private Image chocolateImg;
    private Image cestaCompleta;
    private Image vovoSprite;
    private boolean temFlorAzul = true;
    private Image gameOverImage;
    private Image homeBackground;
    // ícones para botões superiores
    private javax.swing.ImageIcon icSomOn;
    private javax.swing.ImageIcon icSomOff;
    private javax.swing.ImageIcon icInfo;
    private Image telaInicialImg;

    // ===== CASA DA CHAPEUZINHO =====
    private final int CASA_CHAP_X = 15;
    private final int CASA_CHAP_Y = 15;
    private final int CASA_LARGURA = 100;
    private final int CASA_ALTURA = 100;
    private final int VELOCIDADE = 4;

    // posição inicial da Chapeuzinho (será ajustada por placePlayerOnPath)
    private int x = CASA_CHAP_X + (CASA_LARGURA / 2) - 32;
    private int y = CASA_CHAP_Y + CASA_ALTURA + 5;

    // Casa da vovó (posicionada dinamicamente no desenho)

    // ===== CAMINHO =====
    private final int CAMINHO_X = 80;
    private final int CAMINHO_Y = 40;
    private final int CAMINHO_LARGURA = 500;
    private final int CAMINHO_ALTURA = 60;

    private List<Rectangle> obstaculos = new ArrayList<>();
    // ===== LABIRINTO =====
    private int tileSize = 28; // ajustado para tela 1000x750
    private double pathReduction = 0.93; // porcentagem de caminhos a reduzir (0.0 - 1.0) — menos caminhos
    private boolean[][] maze; // true = caminho
    private int mazeCols = 0;
    private int mazeRows = 0;
    private boolean mazeGenerated = false;
    private Random rand = new Random();
    private class Item { int x, y, tipo; /* 0=florAmarela,1=morango,2=florAzul */ }
    private List<Item> itens = new ArrayList<>();
    private class Popup { int x,y; String text; int ttl; }
    private List<Popup> popups = new ArrayList<>();
    // Flags para movimento contínuo pelo loop principal
    private boolean upPressed = false;
    private boolean downPressed = false;
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private EstadoJogo estado = EstadoJogo.TELA_INICIAL;
    private boolean somLigado = true;

    private JButton btnInstrucoes;
    private JButton btnInstrucoesMenu;
    private JButton btnSom;
    private JButton btnStart;
    private JButton btnSair;
    private JButton btnContinue;
    private JButton btnGameOverContinue;
    private JButton btnGameOverHome;
    private JButton btnGerarLabirinto;
    private JButton btnVitoriaMenu;

    private Clip clipTelaInicial;
    private Clip clipGameOver;
    private Clip clipVictory;

    public PainelJogo() {
            var resTelaGameOver = getClass().getResource("/imagens/tela_game_over.png");
            if (resTelaGameOver != null) telaGameOverImg = new ImageIcon(resTelaGameOver).getImage();
        var resTelaVitoria = getClass().getResource("/imagens/tela_vitoria.png");
        if (resTelaVitoria != null) telaVitoriaImg = new ImageIcon(resTelaVitoria).getImage();
        setLayout(null);
        setFocusable(true);
        addKeyListener(this);

        // Botão Retornar ao menu (tela instruções)
        btnInstrucoesMenu = new StyledButton("Retornar ao menu", new Color(180, 210, 255), new Color(120, 170, 255), new Color(0, 70, 200));
        btnInstrucoesMenu.setBounds(0, 0, 200, 36); // posição ajustada dinamicamente
        btnInstrucoesMenu.setFocusable(false);
        btnInstrucoesMenu.setVisible(false);
        btnInstrucoesMenu.addActionListener(e -> setEstado(EstadoJogo.TELA_INICIAL));
        add(btnInstrucoesMenu);

        // ===== IMAGENS =====
        chapeuzinho = new ImageIcon(getClass().getResource("/imagens/chapeuzinho_vermelho_final.png")).getImage();
        arvore = new ImageIcon(getClass().getResource("/imagens/arvore.png")).getImage();
        pedra = new ImageIcon(getClass().getResource("/imagens/pedra.png")).getImage();
        casaChapeuzinho = new ImageIcon(getClass().getResource("/imagens/casa_chapeuzinho.png")).getImage();
        casaVovo = new ImageIcon(getClass().getResource("/imagens/casa_vovo.png")).getImage();
        caminho = new ImageIcon(getClass().getResource("/imagens/caminho.png")).getImage();
        florAmarela = new ImageIcon(getClass().getResource("/imagens/flor_amarela.png")).getImage();
        lobo = new ImageIcon(getClass().getResource("/imagens/lobo.png")).getImage();
        // Lobos especiais com fallback
        var resMarrom = getClass().getResource("/imagens/lobo_marrom.png");
        loboMarrom = (resMarrom != null) ? new ImageIcon(resMarrom).getImage() : lobo;
        var resPreto = getClass().getResource("/imagens/lobo_preto.png");
        loboPreto = (resPreto != null) ? new ImageIcon(resPreto).getImage() : lobo;
        morango = new ImageIcon(getClass().getResource("/imagens/morango.png")).getImage();
        florAzul = new ImageIcon(getClass().getResource("/imagens/flor_azul.png")).getImage();
        var resMaca = getClass().getResource("/imagens/maca.png");
        if (resMaca != null) maca = new ImageIcon(resMaca).getImage();
        var resFlorRosa = getClass().getResource("/imagens/flor_rosa.png");
        if (resFlorRosa != null) florRosa = new ImageIcon(resFlorRosa).getImage();
        var resPirulito = getClass().getResource("/imagens/pirulito.png");
        if (resPirulito != null) pirulito = new ImageIcon(resPirulito).getImage();
        var resChocolate = getClass().getResource("/imagens/chocolate.png");
        if (resChocolate != null) chocolateImg = new ImageIcon(resChocolate).getImage();
        var resCesta = getClass().getResource("/imagens/cesta.png");
        if (resCesta != null) cestaCompleta = new ImageIcon(resCesta).getImage();
        var resVovo = getClass().getResource("/imagens/vovo.png");
        if (resVovo != null) vovoSprite = new ImageIcon(resVovo).getImage();
        // Proteção para não travar se não tiver o coração ainda
        var resCor = getClass().getResource("/imagens/coracao.png");
        if (resCor != null) coracao = new ImageIcon(resCor).getImage();
        var resGameOver = getClass().getResource("/imagens/game_over.png");
        if (resGameOver != null) gameOverImage = new ImageIcon(resGameOver).getImage();
        var resHome = getClass().getResource("/imagens/home_background.png");
        if (resHome != null) homeBackground = new ImageIcon(resHome).getImage();

        var resTelaInicial = getClass().getResource("/imagens/tela_inicial.png");
        if (resTelaInicial != null) telaInicialImg = new ImageIcon(resTelaInicial).getImage();

            // ===== SOM TELA INICIAL =====
            try {
                java.net.URL somUrl = getClass().getResource("/sons/tela_inicial_som.wav");
                System.out.println("[DEBUG] Procurando som: /sons/tela_inicial_som.wav => " + somUrl);
                if (somUrl != null) {
                    AudioInputStream audioIn = AudioSystem.getAudioInputStream(somUrl);
                    clipTelaInicial = AudioSystem.getClip();
                    clipTelaInicial.open(audioIn);
                    clipTelaInicial.loop(Clip.LOOP_CONTINUOUSLY);
                    System.out.println("[DEBUG] Som da tela inicial carregado e tocando.");
                } else {
                    System.out.println("[ERRO] Não foi possível encontrar o som da tela inicial!");
                }
            } catch (Exception e) {
                System.out.println("[ERRO] Falha ao carregar/tocar o som da tela inicial: " + e);
            }

        // ===== SOM GAME OVER =====
        try {
            java.net.URL somGameOverUrl = getClass().getResource("/sons/game_over_som.wav");
            System.out.println("[DEBUG] Procurando som: /sons/game_over_som.wav => " + somGameOverUrl);
            if (somGameOverUrl != null) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(somGameOverUrl);
                clipGameOver = AudioSystem.getClip();
                clipGameOver.open(audioIn);
                System.out.println("[DEBUG] Som de game over carregado.");
            } else {
                System.out.println("[ERRO] Não foi possível encontrar o som de game over!");
            }
        } catch (Exception e) {
            System.out.println("[ERRO] Falha ao carregar/tocar o som de game over: " + e);
        }

        // Criar ícones programáticos para os botões superiores
        try {
            java.awt.image.BufferedImage bsOn = new java.awt.image.BufferedImage(18,18, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D gbi = bsOn.createGraphics();
            gbi.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gbi.setColor(new Color(10,160,30)); gbi.fillOval(1,1,16,16);
            gbi.setColor(Color.WHITE); gbi.fillRect(5,4,6,10); gbi.fillPolygon(new int[]{11,14,11}, new int[]{6,9,12}, 3);
            gbi.dispose(); icSomOn = new javax.swing.ImageIcon(bsOn);

            java.awt.image.BufferedImage bsOff = new java.awt.image.BufferedImage(18,18, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D gbo = bsOff.createGraphics();
            gbo.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gbo.setColor(new Color(200,40,40)); gbo.fillOval(1,1,16,16);
            gbo.setColor(Color.WHITE); gbo.fillRect(5,4,6,10); gbo.fillPolygon(new int[]{11,14,11}, new int[]{6,9,12}, 3);
            gbo.dispose(); icSomOff = new javax.swing.ImageIcon(bsOff);

            java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(18,18, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D gib = bi.createGraphics();
            gib.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gib.setColor(new Color(40,100,200)); gib.fillOval(1,1,16,16);
            gib.setColor(Color.WHITE); gib.setFont(new Font("Arial", Font.BOLD, 12)); gib.drawString("i",7,14);
            gib.dispose(); icInfo = new javax.swing.ImageIcon(bi);
        } catch (Exception ignored) {}

        // ===== CONFIGURAÇÃO DA ALCATEIA (LOBOS) =====
        // Inicialmente vazio — lobos serão posicionados quando o labirinto for gerado

        // ===== MOTOR DOS LOBOS (MOVIMENTO INDEPENDENTE) =====
        // Timer principal do jogo (atualiza lógica e redesenha)
        javax.swing.Timer gameTimer = new javax.swing.Timer(33, e -> {
            if (estado == EstadoJogo.JOGANDO) {
                updateGame();
            }
            repaint();
        });
        gameTimer.start();

        // Árvores (altura 64)
        obstaculos.add(new Rectangle(200, 150, 64, 64));
        obstaculos.add(new Rectangle(500, 100, 64, 64));

        // OBS: Removidas pedras do mapa conforme solicitado

        // ===== BOTÕES =====
        btnInstrucoes = new StyledButton("Como Jogar", new Color(200,230,255), new Color(160,210,255), new Color(30,110,180));
        btnInstrucoes.setBounds(620, 40, 150, 30);
        btnInstrucoes.setFocusable(false);
        btnInstrucoes.setIcon(icInfo);
        btnInstrucoes.addActionListener(e -> setEstado(EstadoJogo.TELA_INSTRUCOES));
        add(btnInstrucoes);

        btnSom = new StyledButton("Som: ON", new Color(220,240,255), new Color(180,220,240), new Color(40,100,160));
        btnSom.setBounds(460, 40, 140, 30);
        btnSom.setFocusable(false);
        btnSom.setIcon(icSomOn);
        btnSom.addActionListener(e -> alternarSom());
        add(btnSom);

        // Botão START
        // Start: vamos usar botão estilizado
        btnStart = new StyledButton("Start", new Color(255, 215, 80), new Color(250, 195, 40), new Color(210,150,10));
        btnStart.setFocusable(false);
        btnStart.addActionListener(e -> {
            setEstado(EstadoJogo.JOGANDO);
            // garantir geração imediata e posicionamento
            generateMazeIfNeeded();
            placePlayerOnPath();
            repaint();
        });
        // criar ícone simples para o botão (círculo verde)
        BufferedImage biStart = new BufferedImage(20,20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gbs = biStart.createGraphics();
        gbs.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gbs.setColor(new Color(30,160,40)); gbs.fillOval(2,2,16,16); gbs.dispose();
        btnStart.setIcon(new ImageIcon(biStart));
        add(btnStart);

        // Botão SAIR
        btnSair = new StyledButton("Sair", new Color(250, 140, 140), new Color(235, 105, 105), new Color(170,40,40));
        btnSair.setFocusable(false);
        btnSair.addActionListener(e -> System.exit(0));
        BufferedImage biExit = new BufferedImage(20,20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gbe = biExit.createGraphics();
        gbe.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gbe.setColor(new Color(200,40,40)); gbe.fillRect(3,3,14,14); gbe.dispose();
        btnSair.setIcon(new ImageIcon(biExit));
        add(btnSair);

        // Botão CONTINUE (aparece na tela de vitória)
        btnContinue = new JButton("Continue");
        btnContinue.setFocusable(false);
        btnContinue.setBackground(new Color(100, 200, 240));
        btnContinue.setOpaque(true);
        btnContinue.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(30,100,140)));
        btnContinue.addActionListener(e -> {
            // continuar: gerar novo labirinto e reiniciar jogador sem resetar pontuação
            mazeGenerated = false;
            generateMazeIfNeeded();
            placePlayerOnPath();
            listaLobos.clear();
            itens.clear();
            // força redistribuição
            mazeGenerated = false;
            generateMazeIfNeeded();
            setEstado(EstadoJogo.JOGANDO);
        });
        // ícone simples
        BufferedImage biCont = new BufferedImage(20,20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gbc = biCont.createGraphics();
        gbc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gbc.setColor(new Color(80,160,220)); gbc.fillOval(2,2,16,16); gbc.dispose();
        btnContinue.setIcon(new ImageIcon(biCont));
        add(btnContinue);

        // Botão CONTINUAR (Game Over)
        btnGameOverContinue = new JButton("Continuar");
        btnGameOverContinue.setFocusable(false);
        btnGameOverContinue.setBackground(new Color(100, 200, 240));
        btnGameOverContinue.setOpaque(true);
        btnGameOverContinue.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(30,100,140)));
        btnGameOverContinue.addActionListener(e -> {
            // continuar: resetar vidas e retornar ao jogo
            vidas = 3;
            mazeGenerated = false;
            generateMazeIfNeeded();
            placePlayerOnPath();
            listaLobos.clear();
            lobosEspeciais.clear();
            itens.clear();
            mazeGenerated = false;
            generateMazeIfNeeded();
            setEstado(EstadoJogo.JOGANDO);
        });
        add(btnGameOverContinue);

        // Botão RETORNAR AO HOME (Game Over)
        btnGameOverHome = new JButton("Retornar ao Home");
        btnGameOverHome.setFocusable(false);
        btnGameOverHome.setBackground(new Color(240, 200, 100));
        btnGameOverHome.setOpaque(true);
        btnGameOverHome.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(120,80,20)));
        btnGameOverHome.addActionListener(e -> {
            // resetar tudo e voltar à tela inicial
            vidas = 3;
            pontuacao = 0;
            temFlorAmarela = true;
            temMorango = true;
            temFlorAzul = true;
            listaLobos.clear();
            lobosEspeciais.clear();
            itens.clear();
            mazeGenerated = false;
            motivoGameOver = null;
            setEstado(EstadoJogo.TELA_INICIAL);
        });
        add(btnGameOverHome);

        // Botão GERAR LABIRINTO (aparece durante o jogo)
        btnGerarLabirinto = new StyledButton("Gerar Labirinto", new Color(140, 200, 255), new Color(100, 170, 240), new Color(30, 90, 160));
        btnGerarLabirinto.setFocusable(false);
        btnGerarLabirinto.addActionListener(e -> {
            // Salvar itens e lobos atuais
            List<Item> itensBackup = new ArrayList<>(itens);
            List<Lobo> lobosBackup = new ArrayList<>(listaLobos);
            List<LoboEspecial> lobosEspeciaisBackup = new ArrayList<>(lobosEspeciais);
            
            // Gerar novo labirinto
            mazeGenerated = false;
            generateMazeIfNeeded();
            
            // Restaurar itens e lobos (filtrando itens que não estão mais sobre caminhos)
            itens.clear();
            for (Item itb : itensBackup) {
                int tx = (itb.x) / tileSize;
                int ty = (itb.y) / tileSize;
                if (tx >= 0 && ty >= 0 && tx < mazeCols && ty < mazeRows && maze[tx][ty]) {
                    itens.add(itb);
                } else {
                    // tentar ajustar para o tile de caminho mais próximo
                    boolean found = false;
                    for (int r = 1; r <= 5 && !found; r++) {
                        for (int dx = -r; dx <= r && !found; dx++) for (int dy = -r; dy <= r && !found; dy++) {
                            int nx = tx + dx, ny = ty + dy;
                            if (nx < 0 || ny < 0 || nx >= mazeCols || ny >= mazeRows) continue;
                            if (!maze[nx][ny]) continue;
                            Item ni = new Item(); ni.tipo = itb.tipo;
                            ni.x = nx*tileSize + (tileSize-24)/2; ni.y = ny*tileSize + (tileSize-24)/2;
                            itens.add(ni); found = true;
                        }
                    }
                }
            }
            listaLobos.clear();
            listaLobos.addAll(lobosBackup);
            lobosEspeciais.clear();
            lobosEspeciais.addAll(lobosEspeciaisBackup);
            
            // Reposicionar jogador em caminho válido
            placePlayerOnPath();
            repaint();
        });
        add(btnGerarLabirinto);

        // Botão RETORNAR AO MENU (aparece na tela de vitória)
        btnVitoriaMenu = new JButton("Retornar ao Menu");
        btnVitoriaMenu.setFocusable(false);
        btnVitoriaMenu.setBackground(new Color(60, 120, 220));
        btnVitoriaMenu.setForeground(Color.WHITE);
        btnVitoriaMenu.setFont(new Font("Arial", Font.BOLD, 18));
        btnVitoriaMenu.setOpaque(true);
        btnVitoriaMenu.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(30, 60, 140), 3));
        btnVitoriaMenu.addActionListener(e -> {
            vidas = 3;
            pontuacao = 0;
            temFlorAmarela = true;
            temMorango = true;
            temFlorAzul = true;
            listaLobos.clear();
            lobosEspeciais.clear();
            itens.clear();
            mazeGenerated = false;
            motivoGameOver = null;
            setEstado(EstadoJogo.TELA_INICIAL);
        });
        add(btnVitoriaMenu);

        // Estilizar botões para tela inicial (cor e fonte)
        try {
            btnSom.setBackground(new Color(200, 220, 240));
            btnSom.setForeground(Color.BLACK);
            btnSom.setOpaque(true);
            btnInstrucoes.setBackground(new Color(200, 220, 240));
            btnInstrucoes.setForeground(Color.BLACK);
            btnInstrucoes.setOpaque(true);
        } catch (Exception ignored) {}

        atualizarBotoes();
        requestFocusInWindow();

        // Configurar Key Bindings para setas e WASD (pressed/released) usando KeyEvent codes
        var inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        var actionMap = getActionMap();

        // setas
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false), "pressed_UP");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, true), "released_UP");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false), "pressed_DOWN");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, true), "released_DOWN");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false), "pressed_LEFT");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, true), "released_LEFT");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, false), "pressed_RIGHT");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, true), "released_RIGHT");

        // WASD
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0, false), "pressed_W");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0, true), "released_W");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, false), "pressed_A");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, true), "released_A");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, false), "pressed_S");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, true), "released_S");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, false), "pressed_D");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, true), "released_D");

        actionMap.put("pressed_UP", new AbstractAction(){@Override public void actionPerformed(ActionEvent e){ setKeyState("UP", true); }});
        actionMap.put("released_UP", new AbstractAction(){@Override public void actionPerformed(ActionEvent e){ setKeyState("UP", false); }});
        actionMap.put("pressed_DOWN", new AbstractAction(){@Override public void actionPerformed(ActionEvent e){ setKeyState("DOWN", true); }});
        actionMap.put("released_DOWN", new AbstractAction(){@Override public void actionPerformed(ActionEvent e){ setKeyState("DOWN", false); }});
        actionMap.put("pressed_LEFT", new AbstractAction(){@Override public void actionPerformed(ActionEvent e){ setKeyState("LEFT", true); }});
        actionMap.put("released_LEFT", new AbstractAction(){@Override public void actionPerformed(ActionEvent e){ setKeyState("LEFT", false); }});
        actionMap.put("pressed_RIGHT", new AbstractAction(){@Override public void actionPerformed(ActionEvent e){ setKeyState("RIGHT", true); }});
        actionMap.put("released_RIGHT", new AbstractAction(){@Override public void actionPerformed(ActionEvent e){ setKeyState("RIGHT", false); }});

        actionMap.put("pressed_W", new AbstractAction(){@Override public void actionPerformed(ActionEvent e){ setKeyState("W", true); }});
        actionMap.put("released_W", new AbstractAction(){@Override public void actionPerformed(ActionEvent e){ setKeyState("W", false); }});
        actionMap.put("pressed_A", new AbstractAction(){@Override public void actionPerformed(ActionEvent e){ setKeyState("A", true); }});
        actionMap.put("released_A", new AbstractAction(){@Override public void actionPerformed(ActionEvent e){ setKeyState("A", false); }});
        actionMap.put("pressed_S", new AbstractAction(){@Override public void actionPerformed(ActionEvent e){ setKeyState("S", true); }});
        actionMap.put("released_S", new AbstractAction(){@Override public void actionPerformed(ActionEvent e){ setKeyState("S", false); }});
        actionMap.put("pressed_D", new AbstractAction(){@Override public void actionPerformed(ActionEvent e){ setKeyState("D", true); }});
        actionMap.put("released_D", new AbstractAction(){@Override public void actionPerformed(ActionEvent e){ setKeyState("D", false); }});
    }

    // Ajusta `x` e `y` do sprite para que a hitbox pequena (20x20) fique centralizada no tile (gx,gy).
    private void snapPlayerToTile(int gx, int gy) {
        int tileCenterX = gx * tileSize + tileSize/2;
        int tileCenterY = gy * tileSize + tileSize/2;
        // nossa hitbox usada para colisões é 20x20 com offset +22 em relação ao sprite
        int hitboxHalf = 10;
        int hitboxX = tileCenterX - hitboxHalf;
        int hitboxY = tileCenterY - hitboxHalf;
        // alinhar o sprite de 64x64 de modo que sua hitbox (x+22,y+22) == hitboxX/hitboxY
        x = hitboxX - 22;
        y = hitboxY - 22;
    }

    private void setKeyState(String key, boolean pressed) {
        switch (key) {
            case "UP": case "W": upPressed = pressed; break;
            case "DOWN": case "S": downPressed = pressed; break;
            case "LEFT": case "A": leftPressed = pressed; break;
            case "RIGHT": case "D": rightPressed = pressed; break;
        }
    }

    private void resetarPosicao() {
        // Garantir que o jogador seja reposicionado em um tile de caminho próximo à casa
        generateMazeIfNeeded();
        placePlayerOnPath();
        // zerar flags de movimento para evitar que volte a andar imediatamente
        upPressed = downPressed = leftPressed = rightPressed = false;
    }

    // Atualiza lógica do jogo: movimento de lobos, colisões e coleta de itens
    private void updateGame() {
        // garantir que o labirinto existe
        generateMazeIfNeeded();

        // mover jogador conforme flags (movimentação contínua)
        int novoX = x;
        int novoY = y;
        if (upPressed) novoY -= VELOCIDADE;
        if (downPressed) novoY += VELOCIDADE;
        if (leftPressed) novoX -= VELOCIDADE;
        if (rightPressed) novoX += VELOCIDADE;

        novoX = Math.max(0, Math.min(novoX, getWidth() - 64));
        novoY = Math.max(0, Math.min(novoY, getHeight() - 64));
        int margem = 40;
        boolean colideCasa = novoX < (CASA_CHAP_X + CASA_LARGURA - margem) && (novoX + 64) > (CASA_CHAP_X + margem) &&
                novoY < (CASA_CHAP_Y + CASA_ALTURA - margem) && (novoY + 64) > (CASA_CHAP_Y + margem);
        if (!colideCasa && canMoveTo(novoX, novoY)) {
            x = novoX; y = novoY;
        }

        // mover lobos: navegação simples pelo labirinto (escolhe vizinho aleatório quando atinge destino)
        for (Lobo l : listaLobos) {
            if (!l.hasTarget()) {
                // determinar célula atual do lobo
                int cgx = Math.max(0, Math.min(mazeCols-1, l.x / tileSize));
                int cgy = Math.max(0, Math.min(mazeRows-1, l.y / tileSize));
                List<int[]> neighbors = new ArrayList<>();
                int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
                for (int[] d : dirs) {
                    int nx = cgx + d[0];
                    int ny = cgy + d[1];
                    if (nx >= 0 && nx < mazeCols && ny >= 0 && ny < mazeRows && !cellOverlapsCasa(nx, ny)) {
                        // permitir que lobos escolham tanto tiles de caminho quanto grama
                        // dar leve preferência às células de caminho (adição dupla)
                        neighbors.add(new int[]{nx, ny});
                        if (maze[nx][ny]) neighbors.add(new int[]{nx, ny});
                    }
                }
                if (!neighbors.isEmpty()) {
                    int[] nb = neighbors.get(rand.nextInt(neighbors.size()));
                    int tx = nb[0] * tileSize;
                    int ty = nb[1] * tileSize;
                    l.setTarget(tx, ty);
                }
            }
            l.moveStep();
        }

        // mover lobos especiais: mesma lógica dos lobos normais
        for (LoboEspecial le : lobosEspeciais) {
            if (!le.hasTarget()) {
                int cgx = Math.max(0, Math.min(mazeCols-1, le.x / tileSize));
                int cgy = Math.max(0, Math.min(mazeRows-1, le.y / tileSize));
                List<int[]> neighbors = new ArrayList<>();
                int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
                for (int[] d : dirs) {
                    int nx = cgx + d[0];
                    int ny = cgy + d[1];
                    if (nx >= 0 && nx < mazeCols && ny >= 0 && ny < mazeRows && !cellOverlapsCasa(nx, ny)) {
                        neighbors.add(new int[]{nx, ny});
                        if (maze[nx][ny]) neighbors.add(new int[]{nx, ny});
                    }
                }
                if (!neighbors.isEmpty()) {
                    int[] nb = neighbors.get(rand.nextInt(neighbors.size()));
                    int tx = nb[0] * tileSize;
                    int ty = nb[1] * tileSize;
                    le.setTarget(tx, ty);
                }
            }
            le.moveStep();
        }

        // atualizar popups (efeitos visuais)
        for (int i = popups.size() - 1; i >= 0; i--) {
            Popup pp = popups.get(i);
            pp.ttl -= 1;
            pp.y -= 1; // sobe um pouco
            if (pp.ttl <= 0) popups.remove(i);
        }

        // reduzir área de colisão do jogador para minimizar alcance de lobos e exigir proximidade para coletar itens
        Rectangle playerRect = new Rectangle(x + 22, y + 22, 20, 20);

        // colisão com lobos — hitbox bem menor (impacto reduzido)
        for (Lobo l : listaLobos) {
            // hitbox reduzida do lobo (20x20) centrada no sprite
            Rectangle lRect = new Rectangle(l.x + 22, l.y + 22, 20, 20);
            // hitbox reduzida do jogador (20x20)
            Rectangle pRect = new Rectangle(x + 22, y + 22, 20, 20);
            if (pRect.intersects(lRect)) {
                tocarSomLoboComum();
                vidas--;
                resetarPosicao();
                if (vidas <= 0) {
                    setEstado(EstadoJogo.GAME_OVER);
                }
                break;
            }
        }

        // colisão com lobos especiais — ZERA AS VIDAS!
        for (LoboEspecial le : lobosEspeciais) {
            Rectangle leRect = new Rectangle(le.x + 22, le.y + 22, 20, 20);
            Rectangle pRect = new Rectangle(x + 22, y + 22, 20, 20);
            if (pRect.intersects(leRect)) {
                tocarSomLoboComum();
                vidas = 0; // zera as vidas imediatamente
                motivoGameOver = "loboEspecial";
                setEstado(EstadoJogo.GAME_OVER);
                break;
            }
        }

        // coleta de itens com efeito visual (popup) — exigir proximidade mais próxima
        for (int i = itens.size() - 1; i >= 0; i--) {
            Item it = itens.get(i);
            // diminuir a área de coleta do item (16x16 centrado)
            Rectangle itRect = new Rectangle(it.x + 4, it.y + 4, 16, 16);
            if (playerRect.intersects(itRect)) {
                int gained = 0;
                switch (it.tipo) {
                    case 0 -> gained = 10;   // maçã
                    case 1 -> gained = 15;   // morango
                    case 2 -> gained = 10;   // flor amarela
                    case 3 -> gained = 15;   // flor azul
                    case 4 -> gained = 20;   // flor rosa
                    case 5 -> gained = 50;   // pirulito
                    case 6 -> gained = 80;   // chocolate
                    case 7 -> gained = 150;  // cesta completa
                    default -> gained = 5;
                }
                pontuacao += gained;
                // criar popup visual
                Popup p = new Popup();
                p.x = it.x; p.y = it.y; p.text = "+" + gained; p.ttl = 40; // duração em frames
                popups.add(p);
                tocarSomBonus();
                itens.remove(i);
            }
        }

        // checar vitória: se a hitbox do jogador intersecta a casa da vovó
        int casaVovoX = getWidth() - CASA_LARGURA - 20;
        int casaVovoY = getHeight() - CASA_ALTURA - 80;
        Rectangle casaVovoRect = new Rectangle(casaVovoX, casaVovoY, CASA_LARGURA, CASA_ALTURA);
        if (playerRect.intersects(casaVovoRect)) {
            // desabilitar controles temporariamente
            upPressed = downPressed = leftPressed = rightPressed = false;
            setEstado(EstadoJogo.VENCEU);
            return;
        }
    }

    // Verifica se o jogador pode mover para (nx, ny).
    // Usa uma hitbox central menor para evitar ficar preso em interseções.
    private boolean canMoveTo(int nx, int ny) {
        if (maze == null) return true; // fallback

        // bloquear movimentação dentro da área interna da casa
        int margem = 40;
        boolean colideCasa = nx < (CASA_CHAP_X + CASA_LARGURA - margem) && (nx + 64) > (CASA_CHAP_X + margem) &&
                ny < (CASA_CHAP_Y + CASA_ALTURA - margem) && (ny + 64) > (CASA_CHAP_Y + margem);
        if (colideCasa) return false;

        // Definir uma hitbox menor que representamos para colisões (centralizada)
        int checkX = nx + 22; // alinhado com o usado nas colisões
        int checkY = ny + 22;
        int checkW = 20;
        int checkH = 20;

        // Amostrar uma grade 3x3 de pontos dentro da caixa de verificação.
        // Regra permissiva:
        // - se o centro e o pé (bottom-center) estiverem em caminho => permitir
        // - caso contrário, permitir se pelo menos 5 dos 9 pontos estiverem em caminho
        int centerX = checkX + checkW/2;
        int centerY = checkY + checkH/2;
        int footX = checkX + checkW/2;
        int footY = checkY + (int)(checkH * 0.9);

        int sx = 3, sy = 3;
        int good = 0;
        for (int ix = 0; ix < sx; ix++) {
            for (int iy = 0; iy < sy; iy++) {
                int px = checkX + (ix * checkW) / (sx - 1);
                int py = checkY + (iy * checkH) / (sy - 1);
                int gx = px / tileSize;
                int gy = py / tileSize;
                if (gx < 0 || gy < 0 || gx >= mazeCols || gy >= mazeRows) continue;
                if (maze[gx][gy]) good++;
            }
        }

        // checar centro e pé
        int cgx = centerX / tileSize; int cgy = centerY / tileSize;
        int fgx = footX / tileSize; int fgy = footY / tileSize;
        boolean centerOk = (cgx >= 0 && cgy >= 0 && cgx < mazeCols && cgy < mazeRows && maze[cgx][cgy]);
        boolean footOk = (fgx >= 0 && fgy >= 0 && fgx < mazeCols && fgy < mazeRows && maze[fgx][fgy]);

        if (centerOk && footOk) return true;
        // relaxamento pequeno: permitir movimento se maioria simples dos probes estiver em caminho (>=4/9)
        return good >= 4;
    }

    // Verifica se a célula do labirinto (gx,gy) faria um lobo ocupar/entrar na área da casa
    private boolean cellOverlapsCasa(int gx, int gy) {
        if (maze == null) return false;
        int lx = gx * tileSize;
        int ly = gy * tileSize;
        java.awt.Rectangle loboRect = new java.awt.Rectangle(lx, ly, 64, 64);
        java.awt.Rectangle casaRect = new java.awt.Rectangle(CASA_CHAP_X, CASA_CHAP_Y, CASA_LARGURA, CASA_ALTURA);
        return loboRect.intersects(casaRect);
    }

    // Posiciona o jogador sobre o tile de caminho mais próximo da sua posição atual (ou da casa)
    private void placePlayerOnPath() {
        if (maze == null) return;
        // ponto de partida: centro aproximado da casa da Chapeuzinho
        int startX = CASA_CHAP_X + CASA_LARGURA/2;
        int startY = CASA_CHAP_Y + CASA_ALTURA/2;
        int scgx = Math.max(0, Math.min(mazeCols-1, startX / tileSize));
        int scgy = Math.max(0, Math.min(mazeRows-1, startY / tileSize));

        // calcular área interna da casa (margem usada para colisão)
        int margem = 40;
        java.awt.Rectangle casaInner = new java.awt.Rectangle(CASA_CHAP_X + margem, CASA_CHAP_Y + margem,
                Math.max(1, CASA_LARGURA - 2*margem), Math.max(1, CASA_ALTURA - 2*margem));

        // função auxiliar para verificar se célula está fora da área da casa
        java.util.function.BiPredicate<Integer,Integer> isOutsideCasa = (gx,gy) -> {
            int px = gx * tileSize + (tileSize - 64)/2;
            int py = gy * tileSize + (tileSize - 64)/2;
            java.awt.Rectangle playerRect = new java.awt.Rectangle(px, py, 64, 64);
            return !playerRect.intersects(casaInner);
        };

        // Preferência: posicionar exatamente em frente à porta (lado direito da casa)
        int prefGx = Math.max(0, Math.min(mazeCols-1, (CASA_CHAP_X + CASA_LARGURA + 6) / tileSize));
        int prefGy = Math.max(0, Math.min(mazeRows-1, (CASA_CHAP_Y + CASA_ALTURA/2) / tileSize));
        if (prefGx >= 0 && prefGy >= 0 && prefGx < mazeCols && prefGy < mazeRows) {
            if (maze[prefGx][prefGy] && isOutsideCasa.test(prefGx, prefGy)) {
                snapPlayerToTile(prefGx, prefGy);
                return;
            } else {
                // se não for caminho, tentamos transformar esse tile em caminho e conectar
                if (!maze[prefGx][prefGy]) {
                    maze[prefGx][prefGy] = true;
                }
            }
        }

        // se a célula da casa for caminho e estiver fora da área interior, usa ela
        if (maze[scgx][scgy] && isOutsideCasa.test(scgx, scgy)) {
            snapPlayerToTile(scgx, scgy);
            return;
        }

        // busca em BFS a célula de caminho mais próxima que não esteja dentro da casa
        boolean[][] visited = new boolean[mazeCols][mazeRows];
        java.util.Queue<int[]> q = new java.util.ArrayDeque<>();
        q.add(new int[]{scgx, scgy});
        visited[scgx][scgy] = true;
        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
        int[] found = null;
        while (!q.isEmpty()) {
            int[] c = q.remove();
            int cx = c[0], cy = c[1];
            for (int[] d : dirs) {
                int nx = cx + d[0], ny = cy + d[1];
                if (nx < 0 || ny < 0 || nx >= mazeCols || ny >= mazeRows) continue;
                if (visited[nx][ny]) continue;
                visited[nx][ny] = true;
                if (maze[nx][ny] && isOutsideCasa.test(nx, ny)) {
                    found = new int[]{nx, ny};
                    break;
                }
                q.add(new int[]{nx, ny});
            }
            if (found != null) break;
        }

        if (found != null) {
            snapPlayerToTile(found[0], found[1]);
            return;
        }

        // Se não encontrou célula de caminho fora da casa (caso raro), cria um corredor até o caminho mais próximo
        // Procurar qualquer célula de caminho
        int[] anyPath = null;
        outer:
        for (int i = 0; i < mazeCols; i++) for (int j = 0; j < mazeRows; j++) if (maze[i][j]) { anyPath = new int[]{i,j}; break outer; }
        if (anyPath == null) {
            // fallback: posiciona em coordenadas padrão
            x = CASA_CHAP_X + CASA_LARGURA + 10;
            y = CASA_CHAP_Y + CASA_ALTURA/2;
            return;
        }

        // carrear do tile da casa até anyPath: caminhar horizontalmente depois verticalmente
        int cx = scgx, cy = scgy;
        while (cx != anyPath[0]) {
            if (anyPath[0] > cx) cx++; else cx--;
            if (cx >= 0 && cx < mazeCols && cy >= 0 && cy < mazeRows) maze[cx][cy] = true;
        }
        while (cy != anyPath[1]) {
            if (anyPath[1] > cy) cy++; else cy--;
            if (cx >= 0 && cx < mazeCols && cy >= 0 && cy < mazeRows) maze[cx][cy] = true;
        }

        // posiciona jogador no primeiro tile fora da casa se possível
        for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) {
            int nx = scgx + dx, ny = scgy + dy;
            if (nx >=0 && ny >=0 && nx < mazeCols && ny < mazeRows && maze[nx][ny] && isOutsideCasa.test(nx, ny)) {
                snapPlayerToTile(nx, ny);
                return;
            }
        }

        // fallback final
        x = CASA_CHAP_X + CASA_LARGURA + 10;
        y = CASA_CHAP_Y + CASA_ALTURA/2;
    }

    // Gera um labirinto usando algoritmo de backtracker em uma grade de tiles
    private void generateMazeIfNeeded() {
        int cols = Math.max(3, getWidth() / tileSize);
        int rows = Math.max(3, getHeight() / tileSize);
        // garantir impares para o algoritmo (paredes em células pares)
        if (cols % 2 == 0) cols--;
        if (rows % 2 == 0) rows--;
        if (mazeGenerated && cols == mazeCols && rows == mazeRows) return;
        mazeCols = cols; mazeRows = rows;
        maze = new boolean[mazeCols][mazeRows];

        // inicializa tudo como parede (false)
        for (int i = 0; i < mazeCols; i++) for (int j = 0; j < mazeRows; j++) maze[i][j] = false;

        // calcular célula inicial do labirinto de forma que comece abaixo da casa da Chapeuzinho
        Stack<int[]> stack = new Stack<>();
        int centerX = CASA_CHAP_X + CASA_LARGURA/2;
        int centerY = CASA_CHAP_Y + CASA_ALTURA + tileSize; // um tile abaixo da casa
        int startX = Math.max(1, Math.min(cols - 2, centerX / tileSize));
        int startY = Math.max(1, Math.min(rows - 2, centerY / tileSize));
        // garantir índices ímpares para o algoritmo de backtracker
        if (startX % 2 == 0) startX = Math.min(startX + 1, cols - 2);
        if (startY % 2 == 0) startY = Math.min(startY + 1, rows - 2);
        maze[startX][startY] = true;
        
        // Criar caminho explícito da porta da casa até o tile inicial
        int doorCellX = (CASA_CHAP_X + CASA_LARGURA/2) / tileSize;
        int doorCellY = (CASA_CHAP_Y + CASA_ALTURA) / tileSize;
        // Desenhar linha de caminho da porta até startX, startY
        int cx = doorCellX, cy = doorCellY;
        while (cy != startY) {
            if (cy >= 0 && cy < mazeRows && cx >= 0 && cx < mazeCols) maze[cx][cy] = true;
            if (startY > cy) cy++; else cy--;
        }
        while (cx != startX) {
            if (cy >= 0 && cy < mazeRows && cx >= 0 && cx < mazeCols) maze[cx][cy] = true;
            if (startX > cx) cx++; else cx--;
        }
        
        stack.push(new int[]{startX, startY});

        int[][] dirs = {{2,0},{-2,0},{0,2},{0,-2}};
        while (!stack.isEmpty()) {
            int[] cur = stack.peek();
            List<int[]> neighbors = new ArrayList<>();
            for (int[] d : dirs) {
                int nx = cur[0] + d[0];
                int ny = cur[1] + d[1];
                if (nx > 0 && nx < mazeCols && ny > 0 && ny < mazeRows && !maze[nx][ny]) {
                    neighbors.add(new int[]{nx, ny, d[0], d[1]});
                }
            }
            if (!neighbors.isEmpty()) {
                int[] nb = neighbors.get(rand.nextInt(neighbors.size()));
                int betweenX = cur[0] + nb[2]/2;
                int betweenY = cur[1] + nb[3]/2;
                maze[betweenX][betweenY] = true;
                maze[nb[0]][nb[1]] = true;
                stack.push(new int[]{nb[0], nb[1]});
            } else {
                stack.pop();
            }
        }

        // limpar itens e lobos e redistribuir
        itens.clear();
        listaLobos.clear();
        lobosEspeciais.clear();

        // reduzir densidade de caminhos (opcional) — transforma alguns caminhos em parede,
        // tentando preservar um caminho entre a casa da Chapeuzinho e a casa da vovó
        reducePathDensity(pathReduction); // usar valor configurável

        // Após reduzir densidade, garantir que apenas a componente conectada ao início exista
        // (remove bolsões desconectados)
        boolean[][] reachable = new boolean[mazeCols][mazeRows];
        if (maze != null && startX >= 0 && startY >= 0 && startX < mazeCols && startY < mazeRows && maze[startX][startY]) {
            java.util.ArrayDeque<int[]> q = new java.util.ArrayDeque<>();
            q.add(new int[]{startX, startY});
            reachable[startX][startY] = true;
            int[][] dirs4 = {{1,0},{-1,0},{0,1},{0,-1}};
            while (!q.isEmpty()) {
                int[] c = q.remove();
                for (int[] d : dirs4) {
                    int nx = c[0] + d[0], ny = c[1] + d[1];
                    if (nx < 0 || ny < 0 || nx >= mazeCols || ny >= mazeRows) continue;
                    if (reachable[nx][ny]) continue;
                    if (!maze[nx][ny]) continue;
                    reachable[nx][ny] = true; q.add(new int[]{nx, ny});
                }
            }
            // limpar células não alcançáveis
            for (int i = 0; i < mazeCols; i++) for (int j = 0; j < mazeRows; j++) if (maze[i][j] && !reachable[i][j]) maze[i][j] = false;
        }

        // coletar células de caminho disponíveis (recalcula após redução)
        List<int[]> pathCells = new ArrayList<>();
        for (int i = 0; i < mazeCols; i++) for (int j = 0; j < mazeRows; j++) if (maze[i][j]) pathCells.add(new int[]{i,j});

        // distribuir itens — ajustado para tela menor
        // garantir espaçamento vertical entre itens próximos
        int maxItems = Math.min(45, Math.max(8, pathCells.size()/7));
        int attemptsLimit = 8;
        int minVerticalSpacing = tileSize; // distância mínima vertical em pixels

        // Garante pelo menos 2 de cada tipo
        for (int tipo = 0; tipo <= 7; tipo++) {
            for (int rep = 0; rep < 2; rep++) {
                int attempts = 0;
                boolean placed = false;
                while (!placed && attempts < attemptsLimit) {
                    attempts++;
                    int[] c = pathCells.get(rand.nextInt(pathCells.size()));
                    Item it = new Item();
                    it.x = c[0]*tileSize + (tileSize-24)/2;
                    it.y = c[1]*tileSize + (tileSize-24)/2;
                    it.tipo = tipo;
                    boolean conflict = false;
                    for (Item exist : itens) {
                        if (Math.abs(exist.x - it.x) < tileSize && Math.abs(exist.y - it.y) < minVerticalSpacing) {
                            conflict = true; break;
                        }
                    }
                    if (!conflict) { itens.add(it); placed = true; }
                }
            }
        }
        // Preenche o restante normalmente
        int extra = maxItems - itens.size();
        for (int k = 0; k < extra; k++) {
            int attempts = 0;
            boolean placed = false;
            while (!placed && attempts < attemptsLimit) {
                attempts++;
                int[] c = pathCells.get(rand.nextInt(pathCells.size()));
                Item it = new Item();
                it.x = c[0]*tileSize + (tileSize-24)/2;
                it.y = c[1]*tileSize + (tileSize-24)/2;
                int r = rand.nextInt(100);
                int tipo;
                if (r < 25) tipo = 0;
                else if (r < 45) tipo = 1;
                else if (r < 60) tipo = 2;
                else if (r < 72) tipo = 3;
                else if (r < 82) tipo = 4;
                else if (r < 92) tipo = 5;
                else if (r < 98) tipo = 6;
                else tipo = 7;
                it.tipo = tipo;
                boolean conflict = false;
                for (Item exist : itens) {
                    if (Math.abs(exist.x - it.x) < tileSize && Math.abs(exist.y - it.y) < minVerticalSpacing) {
                        conflict = true; break;
                    }
                }
                if (!conflict) { itens.add(it); placed = true; }
            }
        }

        // distribuir lobos (reduzido): menos lobos e spawn menos denso
        // PRIORIDADE: posicionar lobos próximos a itens de maior valor (guardando-os)
        if (!itens.isEmpty()) {
            // calcular pontos por tipo
            java.util.Map<Integer,Integer> pontosTipo = new java.util.HashMap<>();
            pontosTipo.put(0, 5);   // maçã
            pontosTipo.put(1, 10);  // morango
            pontosTipo.put(2, 8);   // flor amarela
            pontosTipo.put(3, 12);  // flor azul
            pontosTipo.put(4, 15);  // flor rosa
            pontosTipo.put(5, 20);  // pirulito
            pontosTipo.put(6, 25);  // chocolate
            pontosTipo.put(7, 50);  // cesta completa

            List<Item> sorted = new ArrayList<>(itens);
            sorted.sort((a,b) -> Integer.compare(pontosTipo.getOrDefault(b.tipo,0), pontosTipo.getOrDefault(a.tipo,0)));
            int guards = Math.min(3, Math.max(1, sorted.size()/12));
            int placedGuards = 0;
            for (Item high : sorted) {
                if (placedGuards >= guards) break;
                int itx = high.x / tileSize;
                int ity = high.y / tileSize;
                // procurar célula de caminho na vizinhança (raio crescente)
                boolean placed = false;
                for (int r = 1; r <= 3 && !placed; r++) {
                    for (int dx = -r; dx <= r && !placed; dx++) for (int dy = -r; dy <= r && !placed; dy++) {
                        int nx = itx + dx, ny = ity + dy;
                        if (nx < 0 || ny < 0 || nx >= mazeCols || ny >= mazeRows) continue;
                        if (!maze[nx][ny]) continue;
                        if (cellOverlapsCasa(nx, ny)) continue;
                        // verificar se já existe lobo aqui
                        boolean occ = false;
                        for (Lobo l : listaLobos) if (l.x == nx*tileSize && l.y == ny*tileSize) occ = true;
                        for (LoboEspecial le : lobosEspeciais) if (le.x == nx*tileSize && le.y == ny*tileSize) occ = true;
                        if (occ) continue;
                        listaLobos.add(new Lobo(nx*tileSize, ny*tileSize));
                        placed = true; placedGuards++;
                    }
                }
            }
        }

        int maxLobos = Math.min(8, Math.max(2, pathCells.size()/50));
        // filtrar células que não sobrepõem a casa da Chapeuzinho
        List<int[]> spawnCandidates = new ArrayList<>();
        for (int[] c : pathCells) {
            if (!cellOverlapsCasa(c[0], c[1])) spawnCandidates.add(c);
        }
        if (spawnCandidates.isEmpty()) spawnCandidates = pathCells; // fallback caso raro
        for (int k = 0; k < maxLobos; k++) {
            int[] c = spawnCandidates.get(rand.nextInt(spawnCandidates.size()));
            int lx = c[0]*tileSize;
            int ly = c[1]*tileSize;
            listaLobos.add(new Lobo(lx, ly));
        }

        // distribuir lobos especiais (1 marrom e 1 preto) que zeram vidas
        int maxLobosEspeciais = Math.min(2, Math.max(1, spawnCandidates.size()/100));
        if (spawnCandidates.size() > 10) { // apenas se houver espaço suficiente
            for (int k = 0; k < maxLobosEspeciais; k++) {
                int[] c = spawnCandidates.get(rand.nextInt(spawnCandidates.size()));
                int lx = c[0]*tileSize;
                int ly = c[1]*tileSize;
                String tipo = (k % 2 == 0) ? "marrom" : "preto";
                lobosEspeciais.add(new LoboEspecial(lx, ly, tipo));
            }
        }

        mazeGenerated = true;
        // posicionar jogador sobre um tile de caminho para garantir que possa andar
        placePlayerOnPath();
        // remover itens inválidos (que não estão mais sobre caminhos)
        for (int i = itens.size() - 1; i >= 0; i--) {
            Item it = itens.get(i);
            int tx = it.x / tileSize;
            int ty = it.y / tileSize;
            if (tx < 0 || ty < 0 || tx >= mazeCols || ty >= mazeRows || !maze[tx][ty]) itens.remove(i);
        }
    }

    // Reduce path density by turning a fraction of path cells into walls while preserving
    // connectivity between the player's home area and the grandma house.
    private void reducePathDensity(double fraction) {
        if (maze == null) return;
        // compute start cell (near Chapeuzinho house center)
        int startX = CASA_CHAP_X + CASA_LARGURA/2;
        int startY = CASA_CHAP_Y + CASA_ALTURA/2;
        int scgx = Math.max(0, Math.min(mazeCols-1, startX / tileSize));
        int scgy = Math.max(0, Math.min(mazeRows-1, startY / tileSize));

        // compute goal cell (approx center of grandma house)
        int casaVovoX = Math.max(0, getWidth() - CASA_LARGURA - 20);
        int casaVovoY = Math.max(0, getHeight() - CASA_ALTURA - 80);
        int gcx = Math.max(0, Math.min(mazeCols-1, casaVovoX / tileSize));
        int gcy = Math.max(0, Math.min(mazeRows-1, casaVovoY / tileSize));

        List<int[]> candidates = new ArrayList<>();
        for (int i = 0; i < mazeCols; i++) for (int j = 0; j < mazeRows; j++) {
            if (!maze[i][j]) continue;
            // never remove start or goal or cells that overlap Chapeuzinho's house
            if ((i == scgx && j == scgy) || (i == gcx && j == gcy)) continue;
            if (cellOverlapsCasa(i, j)) continue;
            candidates.add(new int[]{i,j});
        }

        java.util.Collections.shuffle(candidates, rand);
        int target = (int) Math.round(candidates.size() * fraction);
        int removed = 0;
        for (int[] c : candidates) {
            if (removed >= target) break;
            int cx = c[0], cy = c[1];
            // tentative removal
            maze[cx][cy] = false;
            // check connectivity from start to goal
            if (isReachable(scgx, scgy, gcx, gcy)) {
                removed++;
            } else {
                // revert if disconnects
                maze[cx][cy] = true;
            }
        }
    }

    // BFS reachability check on maze grid
    private boolean isReachable(int sx, int sy, int gx, int gy) {
        if (sx < 0 || sy < 0 || gx < 0 || gy < 0) return false;
        boolean[][] visited = new boolean[mazeCols][mazeRows];
        java.util.ArrayDeque<int[]> q = new java.util.ArrayDeque<>();
        if (!maze[sx][sy] || !maze[gx][gy]) return false;
        q.add(new int[]{sx, sy});
        visited[sx][sy] = true;
        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
        while (!q.isEmpty()) {
            int[] cur = q.remove();
            if (cur[0] == gx && cur[1] == gy) return true;
            for (int[] d : dirs) {
                int nx = cur[0] + d[0];
                int ny = cur[1] + d[1];
                if (nx < 0 || ny < 0 || nx >= mazeCols || ny >= mazeRows) continue;
                if (visited[nx][ny]) continue;
                if (!maze[nx][ny]) continue;
                visited[nx][ny] = true;
                q.add(new int[]{nx, ny});
            }
        }
        return false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        switch (estado) {
            case TELA_INICIAL -> desenharTelaInicial(g);
            case TELA_INSTRUCOES -> desenharTelaInstrucoes(g);
            case JOGANDO -> desenharJogo(g);
            case VENCEU -> desenharTelaVenceu(g);
            case GAME_OVER -> desenharTelaGameOver(g);
            case PERDEU -> desenharTelaGameOver(g); // trata PERDEU igual ao GAME_OVER
        }

        // Esconde o botão se não estiver na tela de instruções
        if (btnInstrucoesMenu != null) {
            btnInstrucoesMenu.setVisible(estado == EstadoJogo.TELA_INSTRUCOES);
        }
    }

    private void desenharTelaInicial(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Imagem de fundo da tela inicial
        if (telaInicialImg != null) {
            g2.drawImage(telaInicialImg, 0, 0, getWidth(), getHeight(), this);
        } else {
            g2.setColor(new Color(250, 250, 240));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        // Barra preta sombreada na parte inferior
        int barraAltura = 80;
        int sombraAltura = 18;
        // Sombra
        for (int i = 0; i < sombraAltura; i++) {
            int alpha = (int)(60 * (1 - i / (float)sombraAltura));
            g2.setColor(new Color(0, 0, 0, alpha));
            g2.fillRect(0, getHeight() - barraAltura - sombraAltura + i, getWidth(), 1);
        }
        // Barra preta
        g2.setColor(new Color(0, 0, 0, 220));
        g2.fillRect(0, getHeight() - barraAltura, getWidth(), barraAltura);

        // Reposicionar botões no topo central
        if (btnSom != null && btnInstrucoes != null) {
            int bw = 160, bh = 32;
            int gap = 18;
            int totalW = bw * 2 + gap;
            int cx = getWidth() / 2;
            btnSom.setBounds(cx - totalW/2, 12, bw, bh);
            btnInstrucoes.setBounds(cx - totalW/2 + bw + gap, 12, bw, bh);
        }

        // Posicionar e estilizar botões Jogar / Sair dentro da barra
        if (btnStart != null && btnSair != null) {
            int bw = 180, bh = 38;
            int gap = 28;
            int totalW = bw * 2 + gap;
            int cx = getWidth() / 2;
            int y = getHeight() - barraAltura + (barraAltura - bh) / 2;
            btnStart.setBounds(cx - totalW/2, y, bw, bh);
            btnSair.setBounds(cx - totalW/2 + bw + gap, y, bw, bh);

            // Cor azul claro e fonte preta negrito
            Color azulClaro = new Color(120, 180, 255);
            btnStart.setBackground(azulClaro);
            btnSair.setBackground(azulClaro);
            btnStart.setForeground(Color.BLACK);
            btnSair.setForeground(Color.BLACK);
            btnStart.setFont(new Font("Verdana", Font.BOLD, 16));
            btnSair.setFont(new Font("Verdana", Font.BOLD, 16));

            // Alterar texto do botão Start para Jogar
            btnStart.setText("Jogar");
        }

        // Texto de instrução dentro de uma caixa branca sombreada
        String texto = "⭐ Pressione ENTER ou clique em Jogar para começar ⭐";
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        int boxW = g2.getFontMetrics().stringWidth(texto) + 48;
        int boxH = 44;
        int boxX = (getWidth() - boxW) / 2;
        int boxY = getHeight() - barraAltura - 44;
        // Sombra
        g2.setColor(new Color(0,0,0,60));
        g2.fillRoundRect(boxX+3, boxY+6, boxW, boxH, 18, 18);
        // Caixa branca
        g2.setColor(new Color(255,255,255,235));
        g2.fillRoundRect(boxX, boxY, boxW, boxH, 18, 18);
        // Texto centralizado na caixa
        g2.setColor(new Color(30, 30, 30));
        int x = boxX + (boxW - g2.getFontMetrics().stringWidth(texto)) / 2;
        int y = boxY + (boxH + g2.getFontMetrics().getAscent()) / 2 - 6;
        g2.drawString(texto, x, y);
    }

    private void desenharTelaInstrucoes(Graphics g) {
        g.setColor(new Color(250, 250, 240)); // off white
        g.fillRect(0, 0, getWidth(), getHeight());

        // Replicar sprites dos itens em mosaico por toda a tela
        int nItens = 8;
        int tamSprite = 28; // tamanho ainda menor
        Image[] sprites = new Image[] {
            maca, morango, pirulito, florAmarela, florAzul, florRosa, chocolateImg, cestaCompleta
        };
        int cols = getWidth() / tamSprite;
        int rows = getHeight() / tamSprite;
        int sobraX = getWidth() - (cols * tamSprite);
        int sobraY = getHeight() - (rows * tamSprite);
        int offsetX = sobraX / 2;
        int offsetY = sobraY / 2;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int idx = (row * cols + col) % nItens;
                if (sprites[idx] != null) {
                    int x = offsetX + col * tamSprite;
                    int y = offsetY + row * tamSprite;
                    g.drawImage(sprites[idx], x, y, tamSprite, tamSprite, null);
                }
            }
        }

        // Texto de instrução com fundo
        String[] texto = {
            "MISSÃO: O RESGATE DA VOVÓ",
            "A floresta é perigosa! Você deve observar, atravessar o labirinto, desviar da vigilância dos lobos e chegar à Casa da Vovó.",
            "CONTROLES",
            "Setas (← ↑ → ↓): Movimentam a Chapeuzinho.",
            "ESC: Retorna ao menu.",
            "COLETÁVEIS E PONTOS",
            "Maçã / Flor Amarela: 10 pts",
            "Morango / Flor Azul: 15 pts",
            "Flor Rosa: 20 pts",
            "Pirulito: 50 pts | Chocolate: 80 pts",
            "Cesta Completa: 150 pts",
            "PERIGOS DA FLORESTA",
            "Lobo Comum: Retira uma de suas vidas.",
            "Lobo Assustador: Causa Game Over instantâneo.",
            "TENHA CUIDADO!"
        };
        // Medir largura máxima e altura total do texto, com quebra de linha automática
        Font titleFont = new Font("Arial", Font.BOLD, 20);
        Font textFont = new Font("Arial", Font.PLAIN, 14);
        java.awt.FontMetrics fmTitle = g.getFontMetrics(titleFont);
        java.awt.FontMetrics fmText = g.getFontMetrics(textFont);
        int maxBoxW = Math.min(getWidth() - 40, 520); // largura máxima da caixa
        int paddingX = 36;
        int paddingY = 32;
        int lineH = fmText.getHeight();
        java.util.List<String> linhas = new java.util.ArrayList<>();
        linhas.add(texto[0]); // título
        for (int i = 1; i < texto.length; i++) {
            String t = texto[i];
            if (t.isEmpty()) {
                linhas.add("");
                continue;
            }
            // Quebra automática de linha para textos longos
            while (!t.isEmpty()) {
                int len = t.length();
                int cut = len;
                while (fmText.stringWidth(t.substring(0, cut)) > maxBoxW - 2*paddingX && cut > 0) cut--;
                // Evita cortar palavra no meio
                if (cut < len && cut > 10) {
                    int lastSpace = t.lastIndexOf(' ', cut);
                    if (lastSpace > 0) cut = lastSpace;
                }
                linhas.add(t.substring(0, cut).trim());
                t = t.substring(cut).trim();
            }
        }
        int boxW = maxBoxW;
        int boxH = (fmTitle.getHeight() + (linhas.size()-1)*lineH) + 2 * paddingY;
        int boxX = (getWidth() - boxW) / 2;
        int boxY = (getHeight() - boxH) / 2;
        g.setColor(new Color(120, 220, 140, 220)); // verde mais claro com transparência
        g.fillRoundRect(boxX, boxY, boxW, boxH, 22, 22);
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        int y = boxY + paddingY + fmTitle.getAscent();
        g.drawString(linhas.get(0), boxX + (boxW - fmTitle.stringWidth(linhas.get(0)))/2, y);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        y += 8;
        for (int i = 1; i < linhas.size(); i++) {
            y += lineH;
            String linha = linhas.get(i);
            if (linha.isEmpty()) continue;
            g.drawString(linha, boxX + (boxW - fmText.stringWidth(linha))/2, y);
        }

        // Posicionar e exibir o botão abaixo da caixa de texto
        if (btnInstrucoesMenu != null) {
            int btnW = 200, btnH = 36;
            int btnX = boxX + (boxW - btnW) / 2;
            int btnY = boxY + boxH + 18;
            btnInstrucoesMenu.setBounds(btnX, btnY, btnW, btnH);
            btnInstrucoesMenu.setVisible(true);
            btnInstrucoesMenu.repaint();
        }
    }

    private void desenharTelaVenceu(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        if (telaVitoriaImg != null) {
            g2.drawImage(telaVitoriaImg, 0, 0, getWidth(), getHeight(), this);
        } else {
            g2.setColor(new Color(20,120,40));
            g2.fillRect(0,0,getWidth(), getHeight());
        }
        g2.setFont(new Font("Verdana", Font.BOLD, 48));
        String msg = "Você Venceu!";
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(msg)) / 2;
        int y = getHeight()/2 - 40;
        // Sombra escura
        g2.setColor(new Color(0,0,0,180));
        g2.drawString(msg, x+3, y+3);
        // Texto principal
        g2.setColor(new Color(255,240,200));
        g2.drawString(msg, x, y);

        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        String sub = "Parabéns — a vovó está salva!";
        int xs = (getWidth() - g2.getFontMetrics().stringWidth(sub)) / 2;
        // Sombra escura
        g2.setColor(new Color(0,0,0,180));
        g2.drawString(sub, xs+2, y+42);
        // Texto principal
        g2.setColor(new Color(230,230,230));
        g2.drawString(sub, xs, y + 40);

        // Exibir pontuação final
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        String pontos = "Pontuação final: " + pontuacao;
        int xp = (getWidth() - g2.getFontMetrics().stringWidth(pontos)) / 2;
        g2.setColor(new Color(255, 215, 0));
        g2.drawString(pontos, xp, y + 80);

        // posicionar botões Retornar ao Menu / Sair
        if (btnVitoriaMenu != null && btnSair != null) {
            int bw = 180, bh = 44;
            int gap = 24;
            int totalW = bw * 2 + gap;
            int cx = getWidth() / 2;
            int by = y + 120;
            btnVitoriaMenu.setBounds(cx - totalW/2, by, bw, bh);
            btnSair.setBounds(cx - totalW/2 + bw + gap, by, bw, bh);
        }
    }

    // Botão estilizado com gradiente e hover
    private static class StyledButton extends JButton {
        private final Color start;
        private final Color end;
        private final Color borderColor;
        private boolean hover = false;
        public StyledButton(String text, Color start, Color end, Color border) {
            super(text);
            this.start = start;
            this.end = end;
            this.borderColor = border;
            setContentAreaFilled(false);
            setFocusPainted(false);
                                  setOpaque(false);
            setForeground(Color.BLACK);
            addMouseListener(new java.awt.event.MouseAdapter(){
                public void mouseEntered(java.awt.event.MouseEvent e){ hover = true; repaint(); }
                public void mouseExited(java.awt.event.MouseEvent e){ hover = false; repaint(); }
            });
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(); int h = getHeight();
            java.awt.geom.RoundRectangle2D rr = new java.awt.geom.RoundRectangle2D.Float(0,0,w-1,h-1,14,14);
            Color s = hover ? start.brighter() : start;
            Color e = hover ? end.brighter() : end;
            java.awt.GradientPaint gp = new java.awt.GradientPaint(0,0,s,0,h,e);
            g2.setPaint(gp);
            g2.fill(rr);
            g2.setColor(borderColor);
            g2.setStroke(new java.awt.BasicStroke(2));
            g2.draw(rr);
            // sombra inferior
            g2.setColor(new Color(0,0,0,40));
            g2.fillRoundRect(3, h-6, w-6, 4, 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
        @Override
        public void setBounds(int x, int y, int width, int height) {
            super.setBounds(x,y,width,height);
            setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        }
    }

    private void desenharTelaGameOver(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        // Fundo com imagem de game over
        if (telaGameOverImg != null) {
            g2.drawImage(telaGameOverImg, 0, 0, getWidth(), getHeight(), this);
        } else {
            g2.setColor(new Color(20, 20, 20));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        // Subtítulo dinâmico
        int y = getHeight()/2 - 60;
        g2.setFont(new Font("Arial", Font.PLAIN, 22));
        String sub;
        if ("loboEspecial".equals(motivoGameOver)) {
            sub = "Você foi pego pelo lobo!";
        } else if (vidas <= 0) {
            sub = "Suas vidas acabaram!";
        } else {
            sub = "Seu tempo acabou!";
        }
        int xs = (getWidth() - g2.getFontMetrics().stringWidth(sub)) / 2;
        g2.setColor(new Color(230, 230, 230));
        g2.drawString(sub, xs, y + 54);

        // Mostrar pontuação final
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        String pontos = "Pontuação final: " + pontuacao;
        int xp = (getWidth() - g2.getFontMetrics().stringWidth(pontos)) / 2;
        g2.setColor(new Color(255, 215, 0));
        g2.drawString(pontos, xp, y + 100);

        // Posicionar botões Retornar ao Menu / Sair
        if (btnGameOverHome != null && btnSair != null) {
            int bw = 180, bh = 44;
            int gap = 24;
            int totalW = bw * 2 + gap;
            int cx = getWidth() / 2;
            int by = y + 140;
            btnGameOverHome.setBounds(cx - totalW/2, by, bw, bh);
            btnSair.setBounds(cx - totalW/2 + bw + gap, by, bw, bh);
            btnGameOverHome.setText("Retornar ao Menu");
            btnGameOverHome.setBackground(new Color(60, 120, 220));
            btnGameOverHome.setForeground(Color.WHITE);
            btnGameOverHome.setFont(new Font("Arial", Font.BOLD, 18));
            btnGameOverHome.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(30, 60, 140), 3));
        }
    }

    private void desenharJogo(Graphics g) {
        g.setColor(new Color(34, 139, 34));
        g.fillRect(0, 0, getWidth(), getHeight());

        // ===== LABIRINTO: desenhar apenas os caminhos gerados =====
        generateMazeIfNeeded();
        // Desenhar tiles do labirinto
        for (int gx = 0; gx < mazeCols; gx++) {
            for (int gy = 0; gy < mazeRows; gy++) {
                if (maze[gx][gy]) {
                    int drawX = gx * tileSize;
                    int drawY = gy * tileSize;
                    g.drawImage(caminho, drawX, drawY, tileSize, tileSize, null);
                }
            }
        }

        // Desenha itens distribuídos no labirinto (suporta novos tipos)
        for (Item it : itens) {
            switch (it.tipo) {
                case 0 -> { if (maca != null) g.drawImage(maca, it.x, it.y, 24, 24, null); else g.drawImage(florAmarela, it.x, it.y, 24, 24, null); }
                case 1 -> g.drawImage(morango, it.x, it.y, 24, 24, null);
                case 2 -> g.drawImage(florAmarela, it.x, it.y, 24, 24, null);
                case 3 -> { if (florAzul != null) g.drawImage(florAzul, it.x, it.y, 24, 24, null); else g.drawImage(florAmarela, it.x, it.y, 24, 24, null); }
                case 4 -> { if (florRosa != null) g.drawImage(florRosa, it.x, it.y, 24, 24, null); else g.drawImage(florAmarela, it.x, it.y, 24, 24, null); }
                case 5 -> { if (pirulito != null) g.drawImage(pirulito, it.x, it.y, 24, 24, null); else g.drawImage(morango, it.x, it.y, 24, 24, null); }
                case 6 -> { if (chocolateImg != null) g.drawImage(chocolateImg, it.x, it.y, 24, 24, null); else g.drawImage(morango, it.x, it.y, 24, 24, null); }
                case 7 -> { if (cestaCompleta != null) g.drawImage(cestaCompleta, it.x, it.y, 28, 28, null); else g.drawImage(morango, it.x, it.y, 24, 24, null); }
                default -> g.drawImage(morango, it.x, it.y, 24, 24, null);
            }
        }

        // ===== PLACAR =====
        
        // Posicionar botão Gerar Labirinto no topo direito
        if (btnGerarLabirinto != null) {
            btnGerarLabirinto.setBounds(getWidth() - 150, 8, 140, 30);
        }
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("PONTOS: " + pontuacao, 15, 25); // Desenha o placar no topo

        // --- HUD: VIDAS ---
        if (coracao != null) {
            for (int i = 0; i < vidas; i++) {
                g.drawImage(coracao, 160 + (i * 28), 8, 24, 24, null);
            }
        }

        // ===== CASAS =====
        // Casa da Chapeuzinho
        g.drawImage(casaChapeuzinho, CASA_CHAP_X, CASA_CHAP_Y, CASA_LARGURA, CASA_ALTURA, null);
        
        // Casa da vovó (com fundo verde para evitar fundo preto)
        int casaVovoX = getWidth() - CASA_LARGURA - 20;
        int casaVovoY = getHeight() - CASA_ALTURA - 80;
        g.setColor(new Color(34, 139, 34));
        g.fillRect(casaVovoX, casaVovoY, CASA_LARGURA, CASA_ALTURA);
        g.drawImage(casaVovo, casaVovoX, casaVovoY, CASA_LARGURA, CASA_ALTURA, null);
        // desenhar a vovó próxima à casa (se disponível)
        if (vovoSprite != null) {
            int vx = casaVovoX + CASA_LARGURA/2 - 18;
            int vy = casaVovoY + CASA_ALTURA - 16;
            g.drawImage(vovoSprite, vx, vy, 36, 36, null);
        }

        // ===== LOBOS (DESENHA TODA A LISTA) =====
        for (Lobo l : listaLobos) {
            g.drawImage(lobo, l.x, l.y, 64, 64, null);
        }

        // ===== LOBOS ESPECIAIS (MARROM E PRETO) =====
        for (LoboEspecial le : lobosEspeciais) {
            if (le.tipo.equals("marrom")) {
                g.drawImage(loboMarrom, le.x, le.y, 64, 64, null);
            } else {
                g.drawImage(loboPreto, le.x, le.y, 64, 64, null);
            }
            // indicador de perigo (X vermelho)
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("☠", le.x + 22, le.y + 10);
        }

        // ===== PERSONAGEM =====
        g.drawImage(chapeuzinho, x, y, 64, 64, null);

        // ---- DEBUG: flags de teclado e posição (remover depois) ----
        g.setColor(Color.WHITE);
        // debug removido: não exibir flags na tela

        // desenhar popups (efeitos visuais de coleta)
        g.setFont(new Font("Arial", Font.BOLD, 14));
        for (Popup pp : popups) {
            g.setColor(new Color(255, 240, 200));
            g.drawString(pp.text, pp.x, pp.y);
        }
    }

    private void setEstado(EstadoJogo novoEstado) {
            // Sempre para todos os sons antes de tocar outro
            if (clipGameOver != null) clipGameOver.stop();
            if (clipVictory != null) clipVictory.stop();

            System.out.println("[DEBUG] setEstado chamado: novoEstado=" + novoEstado + ", somLigado=" + somLigado);
        // Limpa motivo do Game Over ao sair dessa tela
        if (novoEstado != EstadoJogo.GAME_OVER) {
            motivoGameOver = null;
        }
        // Controle da música de fundo e som de game over
        if (somLigado) {
            try {
                // Para a música da tela inicial/jogando ao entrar em GAME_OVER ou VENCEU
                if (novoEstado == EstadoJogo.GAME_OVER || novoEstado == EstadoJogo.VENCEU || novoEstado == EstadoJogo.TELA_INSTRUCOES) {
                    if (clipTelaInicial != null) {
                        clipTelaInicial.stop();
                        clipTelaInicial.close();
                        clipTelaInicial = null;
                    }
                } else {
                    if (clipTelaInicial == null) {
                        java.net.URL somUrl = getClass().getResource("/sons/tela_inicial_som.wav");
                        System.out.println("[DEBUG] (setEstado) Carregando som: /sons/tela_inicial_som.wav => " + somUrl);
                        if (somUrl != null) {
                            AudioInputStream audioIn = AudioSystem.getAudioInputStream(somUrl);
                            clipTelaInicial = AudioSystem.getClip();
                            clipTelaInicial.open(audioIn);
                        } else {
                            System.out.println("[ERRO] (setEstado) Não foi possível encontrar o som da tela inicial!");
                        }
                    }
                    if (clipTelaInicial != null) {
                        // Listener para reiniciar se acabar
                        clipTelaInicial.removeLineListener(null); // remove listeners antigos
                        clipTelaInicial.addLineListener(ev -> {
                            if (ev.getType() == LineEvent.Type.STOP && somLigado) {
                                clipTelaInicial.setFramePosition(0);
                                clipTelaInicial.loop(Clip.LOOP_CONTINUOUSLY);
                            }
                        });
                        // Só inicia se nunca tocou antes
                        if (!clipTelaInicial.isRunning() && clipTelaInicial.getFramePosition() == 0) {
                            clipTelaInicial.setFramePosition(0);
                            clipTelaInicial.loop(Clip.LOOP_CONTINUOUSLY);
                            System.out.println("[DEBUG] (setEstado) Música de fundo tocando.");
                        }
                        // Ajusta volume conforme estado
                        try {
                            FloatControl volumeControl = (FloatControl) clipTelaInicial.getControl(FloatControl.Type.MASTER_GAIN);
                            if (novoEstado == EstadoJogo.JOGANDO) {
                                volumeControl.setValue(-15.0f); // volume baixo (mais alto que antes)
                            } else {
                                volumeControl.setValue(-5.0f); // volume normal
                            }
                        } catch (Exception e) {
                            System.out.println("[ERRO] Não foi possível ajustar volume: " + e);
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("[ERRO] (setEstado) Falha ao tocar o som da tela inicial: " + e);
            }
            if (novoEstado == EstadoJogo.VENCEU) {
                try {
                    if (clipVictory != null) {
                        clipVictory.stop();
                        clipVictory.close();
                    }
                    java.net.URL somVictoryUrl = getClass().getResource("/sons/som_victory.wav");
                    System.out.println("[DEBUG] (setEstado) Reabrindo som: /sons/som_victory.wav => " + somVictoryUrl);
                    if (somVictoryUrl != null) {
                        AudioInputStream audioIn = AudioSystem.getAudioInputStream(somVictoryUrl);
                        clipVictory = AudioSystem.getClip();
                        clipVictory.open(audioIn);
                        clipVictory.start();
                        System.out.println("[DEBUG] (setEstado) Som de vitória tocando.");
                    } else {
                        System.out.println("[ERRO] (setEstado) Não foi possível encontrar o som de vitória!");
                    }
                } catch (Exception e) {
                    System.out.println("[ERRO] (setEstado) Falha ao reabrir o som de vitória: " + e);
                }
            } else {
                if (clipVictory != null && clipVictory.isRunning()) {
                    clipVictory.stop();
                }
            }
            if (novoEstado == EstadoJogo.GAME_OVER) {
                try {
                    if (clipGameOver != null) {
                        clipGameOver.stop();
                        clipGameOver.close();
                    }
                    java.net.URL somGameOverUrl = getClass().getResource("/sons/game_over_som.wav");
                    System.out.println("[DEBUG] (setEstado) Reabrindo som: /sons/game_over_som.wav => " + somGameOverUrl);
                    if (somGameOverUrl != null) {
                        AudioInputStream audioIn = AudioSystem.getAudioInputStream(somGameOverUrl);
                        clipGameOver = AudioSystem.getClip();
                        clipGameOver.open(audioIn);
                        clipGameOver.start();
                        System.out.println("[DEBUG] (setEstado) Som de game over tocando.");
                    } else {
                        System.out.println("[ERRO] (setEstado) Não foi possível encontrar o som de game over!");
                    }
                } catch (Exception e) {
                    System.out.println("[ERRO] (setEstado) Falha ao reabrir o som de game over: " + e);
                }
            } else {
                if (clipGameOver != null && clipGameOver.isRunning()) {
                    clipGameOver.stop();
                }
            }
        } else {
            if (clipTelaInicial != null) {
                clipTelaInicial.stop();
            }
            if (clipGameOver != null) {
                clipGameOver.stop();
            }
        }
        estado = novoEstado;
        atualizarBotoes();
        setFocusable(true);
        requestFocusInWindow();
        repaint();
    }

    private void atualizarBotoes() {
        boolean mostrarInicial = (estado == EstadoJogo.TELA_INICIAL);
        boolean mostrarVenceu = (estado == EstadoJogo.VENCEU);
        boolean mostrarGameOver = (estado == EstadoJogo.GAME_OVER);
        boolean mostrarJogando = (estado == EstadoJogo.JOGANDO);
        btnInstrucoes.setVisible(mostrarInicial);
        btnSom.setVisible(mostrarInicial);
        if (btnStart != null) btnStart.setVisible(mostrarInicial);
        if (btnSair != null) btnSair.setVisible(mostrarInicial || mostrarVenceu || mostrarGameOver);
        if (btnVitoriaMenu != null) btnVitoriaMenu.setVisible(mostrarVenceu);
        if (btnContinue != null) btnContinue.setVisible(false);
        if (btnGameOverContinue != null) btnGameOverContinue.setVisible(false);
        if (btnGameOverHome != null) btnGameOverHome.setVisible(mostrarGameOver);
        if (btnGerarLabirinto != null) btnGerarLabirinto.setVisible(mostrarJogando);
    }

    private void alternarSom() {
        somLigado = !somLigado;
        btnSom.setText(somLigado ? "Som: ON" : "Som: OFF");
        if (somLigado) {
            if (icSomOn != null) btnSom.setIcon(icSomOn);
            // Só toca música se não estiver em GAME_OVER ou VENCEU
            if (estado == EstadoJogo.TELA_INICIAL || estado == EstadoJogo.JOGANDO) {
                try {
                    if (clipTelaInicial != null) {
                        clipTelaInicial.stop();
                        clipTelaInicial.close();
                        clipTelaInicial = null;
                    }
                    java.net.URL somUrl = getClass().getResource("/sons/tela_inicial_som.wav");
                    System.out.println("[DEBUG] (ON) Reabrindo som: /sons/tela_inicial_som.wav => " + somUrl);
                    if (somUrl != null) {
                        AudioInputStream audioIn = AudioSystem.getAudioInputStream(somUrl);
                        clipTelaInicial = AudioSystem.getClip();
                        clipTelaInicial.open(audioIn);
                        clipTelaInicial.loop(Clip.LOOP_CONTINUOUSLY);
                        System.out.println("[DEBUG] (ON) Música de fundo tocando.");
                    } else {
                        System.out.println("[ERRO] (ON) Não foi possível encontrar o som da tela inicial!");
                    }
                } catch (Exception e) {
                    System.out.println("[ERRO] (ON) Falha ao reabrir o som da tela inicial: " + e);
                }
            }
            if (estado == EstadoJogo.GAME_OVER) {
                try {
                    if (clipGameOver != null) {
                        clipGameOver.stop();
                        clipGameOver.close();
                    }
                    java.net.URL somGameOverUrl = getClass().getResource("/sons/game_over_som.wav");
                    System.out.println("[DEBUG] (ON) Reabrindo som: /sons/game_over_som.wav => " + somGameOverUrl);
                    if (somGameOverUrl != null) {
                        AudioInputStream audioIn = AudioSystem.getAudioInputStream(somGameOverUrl);
                        clipGameOver = AudioSystem.getClip();
                        clipGameOver.open(audioIn);
                        clipGameOver.start();
                        System.out.println("[DEBUG] (ON) Som de game over tocando.");
                    } else {
                        System.out.println("[ERRO] (ON) Não foi possível encontrar o som de game over!");
                    }
                } catch (Exception e) {
                    System.out.println("[ERRO] (ON) Falha ao reabrir o som de game over: " + e);
                }
            }
            // Se não estiver jogando nem em game over, deixa para o setEstado garantir o som ao entrar nesses estados
        } else {
            if (icSomOff != null) btnSom.setIcon(icSomOff);
            // Sempre para todos os sons ao desativar
            if (clipTelaInicial != null) {
                clipTelaInicial.stop();
                clipTelaInicial.close();
                clipTelaInicial = null;
            }
            if (clipGameOver != null) {
                clipGameOver.stop();
            }
            System.out.println("[DEBUG] (OFF) Todos os sons parados.");
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int tecla = e.getKeyCode();
        if (estado == EstadoJogo.TELA_INICIAL && tecla == KeyEvent.VK_ENTER) {
            setEstado(EstadoJogo.JOGANDO);
            return;
        }
        if (estado == EstadoJogo.TELA_INSTRUCOES && tecla == KeyEvent.VK_ESCAPE) {
            setEstado(EstadoJogo.TELA_INICIAL);
            return;
        }
        if ((estado == EstadoJogo.VENCEU || estado == EstadoJogo.GAME_OVER || estado == EstadoJogo.PERDEU) && tecla == KeyEvent.VK_ESCAPE) {
            setEstado(EstadoJogo.TELA_INICIAL);
            return;
        }
        if (estado == EstadoJogo.JOGANDO) {
            if (tecla == KeyEvent.VK_ESCAPE) {
                setEstado(EstadoJogo.TELA_INICIAL);
                return;
            }
            int novoX = x; int novoY = y;
            // suportar setas e WASD
            if (tecla == KeyEvent.VK_UP || tecla == KeyEvent.VK_W) novoY -= VELOCIDADE;
            if (tecla == KeyEvent.VK_DOWN || tecla == KeyEvent.VK_S) novoY += VELOCIDADE;
            if (tecla == KeyEvent.VK_LEFT || tecla == KeyEvent.VK_A) novoX -= VELOCIDADE;
            if (tecla == KeyEvent.VK_RIGHT || tecla == KeyEvent.VK_D) novoX += VELOCIDADE;

            novoX = Math.max(0, Math.min(novoX, getWidth() - 64));
            novoY = Math.max(0, Math.min(novoY, getHeight() - 64));

            int margem = 40;
            boolean colideCasa = novoX < (CASA_CHAP_X + CASA_LARGURA - margem) && (novoX + 64) > (CASA_CHAP_X + margem) &&
                    novoY < (CASA_CHAP_Y + CASA_ALTURA - margem) && (novoY + 64) > (CASA_CHAP_Y + margem);
            // Garante que o labirinto foi gerado antes de validar movimento
            generateMazeIfNeeded();

            if (!colideCasa && canMoveTo(novoX, novoY)) {
                x = novoX; y = novoY;
                // coleta de itens agora é feita no loop principal (updateGame)
            }
            repaint();
        }
    }
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}