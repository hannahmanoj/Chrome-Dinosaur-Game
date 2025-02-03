import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import javax.swing.*;

public class ChromDino extends JPanel implements ActionListener, KeyListener{
    int boardWidth = 750;
    int boardHeight = 250;

    Image dinosaurImg;
    Image dinosaurDeadImg;
    Image dinosaurJumpImg;
    Image cactus1Image;
    Image cactus2Image;
    Image cactus3Image;

    class Block {
        int x;
        int y;
        int width;
        int height;
        Image img;

        Block(int x, int y, int width, int height, Image img) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.img = img;
        }
    }

    //Dino
    int dinoWidth = 88;
    int dinoHeight = 94;
    int dinoX = 50;
    int dinoY = boardHeight - dinoHeight;

    Block dinosaur;

    //cactus
    int cactus1width = 34;
    int cactus2width = 69;
    int cactus3width = 102;

    int cactusHeight = 70;
    int cactusX = 700;
    int cactusY = boardHeight - cactusHeight;
    ArrayList<Block> cactusArray;



    //physics
    int velocityX = -12; // cactus moves towards left speed
    int velocityY = 0; //dino jumping speed
    int gravity = 1;

    boolean gameOver = false;
    int score = 0;
    int highScore = 0;

    Timer gameLoop;
    Timer placeCactusTimer;


    public ChromDino() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.decode("#FFB6C1"));
        setFocusable(true);
        addKeyListener(this);

        dinosaurImg = new ImageIcon(getClass().getResource("./img/dino-run.gif")).getImage();
        dinosaurDeadImg = new ImageIcon(getClass().getResource("./img/dino-dead.png")).getImage();
        dinosaurJumpImg = new ImageIcon(getClass().getResource("./img/dino-jump.png")).getImage();
        cactus1Image = new ImageIcon(getClass().getResource("./img/cactus1.png")).getImage();
        cactus2Image = new ImageIcon(getClass().getResource("./img/cactus2.png")).getImage();
        cactus3Image = new ImageIcon(getClass().getResource("./img/cactus3.png")).getImage();

        dinosaur = new Block(dinoX, dinoY, dinoWidth, dinoHeight, dinosaurImg);
        cactusArray = new ArrayList<Block>();

        //game timer
        gameLoop = new Timer(1000/60, this); // 60 frames per second
        gameLoop.start();

        //cactus timer
        placeCactusTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                placeCactus();
            }
        }); 
        placeCactusTimer.start();
    }

    void placeCactus() {
        if (gameOver) {
            return;
        }
        double placeCactusChance = Math.random();
        if (placeCactusChance > .90) { //10% chance to get cactus 3
            Block cactus = new Block(cactusX, cactusY, cactus3width, cactusHeight, cactus3Image);
            cactusArray.add(cactus);
        }
        else if (placeCactusChance > .70) { //20% chance to get cactus 2
            Block cactus = new Block(cactusX, cactusY, cactus2width, cactusHeight, cactus2Image);
            cactusArray.add(cactus);
        }
        else if (placeCactusChance > .50) { //20% chance to get cactus 1
            Block cactus = new Block(cactusX, cactusY, cactus1width, cactusHeight, cactus1Image);
            cactusArray.add(cactus);
        }

        if (cactusArray.size() > 10) {
            cactusArray.remove(0); //remove first cactus from arraylist thus making sure we only have at most 10 cacti in the arrayList.
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        //dino
        g.drawImage(dinosaur.img, dinosaur.x, dinosaur.y, dinosaur.height, dinosaur.width, null);

        //cactus
        for (int i = 0; i < cactusArray.size(); i++) {
            Block cactus = cactusArray.get(i);
            g.drawImage(cactus.img, cactus.x, cactus.y, cactus.width, cactus.height, null);
        }

        //score
        g.setColor(Color.BLACK);
        g.setFont(new Font("Courier", Font.PLAIN, 25));

        if (gameOver){
            g.drawString("Game Over: " + String.valueOf(score), 10, 35);
            g.drawString("High Score: " + String.valueOf(highScore), 10, 60);
        }
        else {
            g.drawString(String.valueOf(score), 10, 35);
        }
    }

    public void move() {
        //dino
        velocityY += gravity;
        dinosaur.y += velocityY;

        if (dinosaur.y > dinoY) {
            dinosaur.y = dinoY;
            velocityY = 0;
            dinosaur.img = dinosaurImg;
        }
        //cactus
        for (int i = 0; i < cactusArray.size(); i++) {
            Block cactus = cactusArray.get(i);
            cactus.x += velocityX;

            if (collison(dinosaur, cactus)) {
                gameOver = true;
                dinosaur.img = dinosaurDeadImg;
            }
        }

        //score
        score++;
    }

    boolean collison(Block a, Block b){
        return a.x < b.x + b.width &&  //a's top left corner doesn't reach b's top right corner
               a.x + a.width > b.x &&  //a's top right corner passes b's top left corner
               a.y < b.y + b.height && //a's top left corner doesn't reach b's bottom left corner
               a.y + a.height > b.y;   //a's bottom left corner passes b's top left corner
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver){
            if (score > highScore){
                highScore = score;
            }
            placeCactusTimer.stop();
            gameLoop.stop();
        }
    }
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
           // System.out.println("JUMP!");

           if (dinosaur.y == dinoY) {
            velocityY = -17;
            dinosaur.img = dinosaurJumpImg;
           }
           if (gameOver){
            //restart game using resetting conditions
            dinosaur.y = dinoY;
            dinosaur.img = dinosaurImg;
            velocityY = 0;
            cactusArray.clear();
            score = 0;
            gameOver = false;
            gameLoop.start();
            placeCactusTimer.start();
           }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
