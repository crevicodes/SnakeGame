import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Random;

public class SnakeGame {
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                JFrame fr = new SnakeFrame();
                fr.pack();
                fr.setResizable(true);
                fr.setLocationRelativeTo(null);
            }
        });
    }
}
enum DIRECTION { UP, DOWN, LEFT, RIGHT}
class SnakeFrame extends JFrame {
    public SnakeFrame() {
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(new SnakePanel());
    }
}
class SnakePanel extends JPanel {
    int compCounter = 0;
    final int DELAY = 2500;
    Toolkit tkit = Toolkit.getDefaultToolkit();
    Dimension dim = tkit.getScreenSize();
    final int DEFAULT_WIDTH = dim.width/2, DEFAULT_HEIGHT = dim.height/2;
    private final int cellWidth = 20;
    private final int cellHeight = cellWidth;
    private int horiCells;
    private int vertCells;
    Cell[][] grid;
    ArrayList<Cell> apples = new ArrayList<>();
    //THREADS
    ArrayList<Thread> threads = new ArrayList<>();
    //SNAKE STUFF
    ArrayList<Snake> snakes = new ArrayList<>();
    public void add(Snake s) {
        snakes.add(s);
    }
    public void kill(Snake s) {
        s.body.clear();
        snakes.remove(s);
        s = null;
    }
    private final Color[] snakeColors = {Color.RED, Color.YELLOW, Color.BLUE, Color.ORANGE, Color.GREEN,
            Color.MAGENTA, Color.CYAN, Color.PINK, Color.WHITE, Color.DARK_GRAY};
    public void addSnake() {
        int size = new Random().nextInt(4,10);
        Color color = snakeColors[new Random().nextInt(10)];
        DIRECTION direction = DIRECTION.values()[new Random().nextInt(4)];
        int randX;
        int randY;
        //int rollCount = 0;
        boolean roll = true;
        do {
            if (direction == DIRECTION.UP) {
                randX = new Random().nextInt(horiCells);
                randY = new Random().nextInt(1, vertCells - size);
            } else if (direction == DIRECTION.DOWN) {
                randX = new Random().nextInt(horiCells);
                randY = new Random().nextInt(size, vertCells - 1);
            } else if (direction == DIRECTION.LEFT) {
                randX = new Random().nextInt(1, horiCells - size);
                randY = new Random().nextInt(vertCells);
            } else {
                randX = new Random().nextInt(size, horiCells - 1);
                randY = new Random().nextInt(vertCells);
            }
            //MORE OPTIMIZED SPAWNING SYSTEM BUT LOOP GLITCHES AFTER A FEW BUTTON CLICKS
            /*for(int i=0; i<size;i++){
                switch (direction) {
                    case UP:
                        if(grid[randX][randY+i].color == Color.LIGHT_GRAY) rollCount++;
                        break;
                    case DOWN:
                        if(grid[randX][randY-i].color == Color.LIGHT_GRAY) rollCount++;
                        break;
                    case LEFT:
                        if(grid[randX+i][randY].color == Color.LIGHT_GRAY) rollCount++;
                        break;
                    case RIGHT:
                        if(grid[randX-i][randY].color == Color.LIGHT_GRAY) rollCount++;
                        break;
                }
            }
            if(rollCount == size) roll = false;*/
            if(grid[randX][randY].color == Color.LIGHT_GRAY) roll = false;
        } while (roll);
        Snake snake = new Snake(size, color, direction, randX, randY);
        this.add(snake);
        Thread thread = new Thread(new Runnable() {
            @Override public void run() {
                try{
                    do {
                        snake.move(grid, horiCells, vertCells, snakes, apples);
                        for (int i = 0; i < snake.size - 1; i++) {
                            snake.body.get(i).move(snake, snake.body, grid);
                        }
                        if (snake.size <= 2) {
                            kill(snake);
                        }
                        repaint();
                        Thread.sleep(400);
                    }
                    while(!(snake.size<=2));
                } catch (InterruptedException e) {}
            }
        });
        thread.start();
        threads.add(thread);
    }
    @Override public Dimension getPreferredSize() {
        return new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }
    public SnakePanel() {
        JButton snakeButton = new JButton("Add Snake");
        snakeButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                addSnake();
            }
        });
        this.add(snakeButton);
    }
    @Override public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        int innerWidth = ((int) (getWidth() * 0.045)) * 20;
        int innerHeight = ((int) (getHeight() * 0.045)) * 20;
        int innerX = (getWidth() - innerWidth) / 2;
        int innerY = (getHeight() - innerHeight) / 2;
        int outerWidth = innerWidth + 8;
        int outerHeight = innerHeight + 8;
        int outerX = innerX - 4;
        int outerY = innerY - 4;
        horiCells = innerWidth/cellWidth;
        vertCells = innerHeight/cellHeight;
        g2.setColor(new Color(6,122,41));
        g2.fillRect(0,0,getWidth(),getHeight());
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(8));
        g2.drawRect(outerX, outerY, outerWidth, outerHeight);
        if(compCounter == 0){
            grid = new Cell[horiCells][vertCells];
            for (int i = 0; i < horiCells; i++) {
                for (int j = 0; j < vertCells; j++) {
                    grid[i][j] = new Cell(innerX + i * cellWidth, innerY + j * cellHeight, cellWidth, i, j);
                }
            }
        }
        else {
            for (int i = 0; i < horiCells; i++) {
                for (int j = 0; j < vertCells; j++) {
                    for(Snake s : snakes){
                        if(!s.body.contains(grid[i][j]) && grid[i][j].col != Color.LIGHT_GRAY )
                            grid[i][j] = new Cell(innerX + i * cellWidth, innerY + j * cellHeight, cellWidth, i, j);
                    }
                }
            }
        }
        for(int i=0;i< snakes.size();i++){
            grid[snakes.get(i).headX][snakes.get(i).headY].color = snakes.get(i).color;
            for(int j=0; j< snakes.get(i).body.size();j++){
                grid[snakes.get(i).body.get(j).cellX][snakes.get(i).body.get(j).cellY].color = snakes.get(i).color;
            }
        }
        if(compCounter == 1) {
            Thread appleThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while(true) {
                            boolean roll = true;
                            int appX;
                            int appY;
                            do {
                                appX = new Random().nextInt(horiCells);
                                appY = new Random().nextInt(vertCells);
                                if(grid[appX][appY].color == Color.LIGHT_GRAY) {roll = false;}
                            } while (roll);
                            grid[appX][appY].col = Color.LIGHT_GRAY;
                            apples.add(grid[appX][appY]);
                            Thread.sleep(DELAY);
                        }
                    } catch (InterruptedException e) {
                    }
                }
            });
            appleThread.start();
        }
        compCounter++;
        for(int i=0; i<horiCells; i++){
            for(int j=0; j<vertCells; j++) {
                g2.setColor(grid[i][j].color);
                g2.fill(grid[i][j].cell);
                if(grid[i][j].color != Color.LIGHT_GRAY) {
                    g2.setStroke(new BasicStroke(1));
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.draw(grid[i][j].cell);
                }
            }
        }
        for(int i=0;i< snakes.size();i++){
            Ellipse2D head = new Ellipse2D.Double(innerX + snakes.get(i).headX * cellWidth + cellWidth/4,
                    innerY + snakes.get(i).headY * cellHeight + cellHeight/4, cellWidth/2, cellHeight/2);
            g2.setColor(Color.WHITE);
            g2.fill(head);
            g2.setStroke(new BasicStroke(2));
            g2.setColor(Color.BLACK);
            g2.draw(head);
        }
        for(Cell a : apples) {
            Ellipse2D appE = new Ellipse2D.Double(innerX + a.cellX * cellWidth + cellWidth / 6.0,
                    innerY + a.cellY * cellHeight + cellHeight / 6.0, cellWidth * 2 / 3.0, cellHeight * 2 / 3.0);
            if(a.col == Color.LIGHT_GRAY) {
                g2.setColor(Color.RED);
                g2.fill(appE);
                g2.setColor(Color.GREEN);
                g2.draw(appE);
            }
        }
    }
}
class Snake {
    DIRECTION direction;
    int size;
    Color color;
    ArrayList<Cell> body = new ArrayList<>();
    int headX;
    int headY;
    public Snake(int size, Color color, DIRECTION direction, int headX, int headY){
        this.size = size;
        this.color = color;
        this.direction = direction;
        this.headX = headX;
        this.headY = headY;
        for(int i=0;i<this.size-1;i++){
            this.body.add(new Cell());
            this.body.get(i).color = this.color;
            this.body.get(i).direction = this.direction;
            switch (this.direction) {
                case UP:
                    this.body.get(i).cellX = this.headX;
                    this.body.get(i).cellY = this.headY + i;
                    break;
                case DOWN:
                    this.body.get(i).cellX = this.headX;
                    this.body.get(i).cellY = this.headY - i;
                    break;
                case LEFT:
                    this.body.get(i).cellX = this.headX + i;
                    this.body.get(i).cellY = this.headY;
                    break;
                case RIGHT:
                    this.body.get(i).cellX = this.headX - i;
                    this.body.get(i).cellY = this.headY;
                    break;
                default:
                    break;
            }
        }
    }
    public void move(Cell[][] grid, int horiCells, int vertCells, ArrayList<Snake> snakes, ArrayList<Cell> apples) {
        switch (direction) {
            case UP:
                if (headY == 0) {
                    hit(snakes);
                    turn(grid, horiCells, vertCells);
                    move(grid,horiCells,vertCells, snakes, apples);
                }
                else {
                    if (grid[headX][headY - 1].color == Color.LIGHT_GRAY && grid[headX][headY - 1].col == Color.LIGHT_GRAY) {
                        eat(grid, headX, headY-1, apples);
                        headY--;
                        //System.out.println("ALELELELELOLILU");
                    }
                    else if (grid[headX][headY - 1].color == Color.LIGHT_GRAY) headY--;
                    else {
                        hit(headX, headY-1, snakes);
                        turn(grid, horiCells, vertCells);
                        move(grid,horiCells,vertCells, snakes, apples);
                    }
                }
                break;
            case DOWN:
                if (headY == vertCells-1) {
                    hit(snakes);
                    turn(grid, horiCells, vertCells);
                    move(grid,horiCells,vertCells, snakes, apples);
                }
                else {
                    if (grid[headX][headY + 1].color == Color.LIGHT_GRAY && grid[headX][headY+1].col == Color.LIGHT_GRAY) {
                        eat(grid, headX, headY+1, apples);
                        headY++;
                    }
                    else if (grid[headX][headY + 1].color == Color.LIGHT_GRAY) headY++;
                    else {
                        hit(headX, headY + 1, snakes);
                        turn(grid, horiCells, vertCells);
                        move(grid,horiCells,vertCells, snakes, apples);
                    }
                }
                break;
            case LEFT:
                if (headX == 0) {
                    hit(snakes);
                    turn(grid, horiCells, vertCells);
                    move(grid,horiCells,vertCells, snakes, apples);
                }
                else {
                    if (grid[headX - 1][headY].color == Color.LIGHT_GRAY && grid[headX-1][headY].col == Color.LIGHT_GRAY) {
                        eat(grid, headX-1, headY, apples);
                        headX--;
                    }
                    else if (grid[headX - 1][headY].color == Color.LIGHT_GRAY) headX--;
                    else {
                        hit(headX - 1, headY, snakes);
                        turn(grid, horiCells, vertCells);
                        move(grid,horiCells,vertCells, snakes, apples);
                    }
                }
                break;
            case RIGHT:
                if(headX == horiCells-1) {
                    hit(snakes);
                    turn(grid, horiCells, vertCells);
                    move(grid,horiCells,vertCells, snakes, apples);
                }
                else {
                    if (grid[headX + 1][headY].color == Color.LIGHT_GRAY && grid[headX+1][headY].col == Color.LIGHT_GRAY) {
                        eat(grid, headX+1 , headY, apples);
                        headX++;
                    }
                    else if (grid[headX + 1][headY].color == Color.LIGHT_GRAY) headX++;
                    else {
                        hit(headX + 1, headY, snakes);
                        turn(grid, horiCells, vertCells);
                        move(grid,horiCells,vertCells, snakes, apples);
                    }
                }
                break;
            default:
                break;
        }
    }
    public void turn(Cell[][] grid, int horiCells, int vertCells) {
        DIRECTION directionH = DIRECTION.values()[new Random().nextInt(2,4)];
        DIRECTION directionV = DIRECTION.values()[new Random().nextInt(2)];
        switch(this.direction) {
            case UP:
                if(this.headX == horiCells-1 && this.headY == 0)
                    directionH = DIRECTION.LEFT;
                else if(this.headX != horiCells-1 && grid[headX+1][headY].color != Color.LIGHT_GRAY)
                    directionH = DIRECTION.LEFT;
                if(this.headX == 0 && this.headY == 0)
                    directionH = DIRECTION.RIGHT;
                else if(this.headX != 0 && grid[headX-1][headY].color != Color.LIGHT_GRAY)
                    directionH = DIRECTION.RIGHT;
                this.direction = directionH;
                break;
            case DOWN:
                if(this.headX == 0 && this.headY == vertCells-1)
                    directionH = DIRECTION.RIGHT;
                else if(this.headX != 0 && grid[headX-1][headY].color != Color.LIGHT_GRAY)
                    directionH = DIRECTION.RIGHT;
                if(this.headX == horiCells-1 && this.headY == vertCells-1)
                    directionH = DIRECTION.LEFT;
                else if(this.headX != horiCells-1 && grid[headX+1][headY].color != Color.LIGHT_GRAY)
                    directionH = DIRECTION.LEFT;
                this.direction = directionH;
                break;
            case LEFT:
                if(this.headX == 0 && this.headY == vertCells-1)
                    directionV = DIRECTION.UP;
                else if(this.headY != vertCells-1 && grid[headX][headY+1].color != Color.LIGHT_GRAY)
                    directionV = DIRECTION.UP;
                if(this.headX == 0 && this.headY == 0)
                    directionV = DIRECTION.DOWN;
                else if(this.headY != 0 && grid[headX][headY-1].color != Color.LIGHT_GRAY)
                    directionV = DIRECTION.DOWN;
                this.direction = directionV;
                break;
            case RIGHT:
                if(this.headX == horiCells-1 && this.headY == vertCells-1)
                    directionV = DIRECTION.UP;
                else if(this.headY != vertCells-1 && grid[headX][headY+1].color != Color.LIGHT_GRAY)
                    directionV = DIRECTION.UP;
                if(this.headX == horiCells-1 && this.headY == 0)
                    directionV = DIRECTION.DOWN;
                else if(this.headY != 0 && grid[headX][headY-1].color != Color.LIGHT_GRAY)
                    directionV = DIRECTION.DOWN;
                this.direction = directionV;
                break;
            default:
                break;
        }
    }
    public void hit(ArrayList<Snake> snakes) {
        this.size--;
        this.body.remove(this.body.get(this.body.size()-1));
        if(this.size<=2) {
            kill(this, snakes);
        }
    }
    public void hit(int x, int y, ArrayList<Snake> snakes) {
        Snake s = find(x,y,snakes);
        if(s == null) this.hit(snakes);
        else {
            s.hit(snakes);
            if(s.size<=2) {
                kill(s, snakes);
            }
            this.hit(snakes);
        }
    }
    public void kill(Snake s, ArrayList<Snake> snakes) {
        s.body.clear();
        snakes.remove(s);
    }
    public Snake find(int x, int y, ArrayList<Snake> snakes){
        for(Snake s : snakes){
            for(int i=0;i<s.size-1; i++){
                if(s.body.get(i).cellX == x && s.body.get(i).cellY == y) return s;
            }
        }
        return null;
    }
    public void eat(Cell[][] grid, int x, int y, ArrayList<Cell> apples) {
        apples.remove(grid[x][y]);
        grid[x][y].col = Color.BLACK;
        this.size++;
        Cell tail = new Cell();
        this.body.add(tail);
        tail.cellX = this.body.get(this.body.indexOf(tail)-1).oldX;
        tail.cellY = this.body.get(this.body.indexOf(tail)-1).oldY;
        this.body.get(this.body.indexOf(tail)).color = this.color;
        this.body.get(this.body.indexOf(tail)).direction = this.body.get(this.body.indexOf(tail)-1).direction;
    }
}
class Cell {
    Color color = Color.LIGHT_GRAY;
    Color col = Color.BLACK;
    Rectangle2D cell = null;
    int cellX;
    int cellY;
    int oldX;
    int oldY;
    DIRECTION direction;
    public Cell(double x, double y, double s, int cX, int cY) {
        this.cell = new Rectangle2D.Double( x, y, s, s);
        this.cellX = cX;
        this.cellY = cY;
    }
    public Cell() {}
    public void move(Snake snake, ArrayList<Cell> body, Cell[][] grid) {
        oldX = cellX;
        oldY = cellY;
        if(body.indexOf(this) == 0) {
            cellX = snake.headX;
            cellY = snake.headY;
        }
        else {
            cellX = body.get(body.indexOf(this)-1).oldX;
            cellY = body.get(body.indexOf(this)-1).oldY;
        }
        if(body.indexOf(this) == body.size()-1) grid[oldX][oldY].color = Color.LIGHT_GRAY;
    }
}
