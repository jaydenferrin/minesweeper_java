package minesweeper_java;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Grid {
    private final int[] size = {18,14};
    private float freq = .16f;
    private boolean[] grid;
    private int[] bombs;
    public int n = 40;
    public int[] numGrid;
    private int zero;
    public Mask mask;
    private int blockSize = 24;
    private final Color[] emptyBlock = {new Color(0xd7b899),
            new Color(0xe5c29f)};
    private final Color[] coveredBlock = {new Color(0x8ecc39),
            new Color(0xa7d948)};
    private final Color[] numbers = {new Color(0x1976d2),
            new Color(0x388e3c),
            new Color(0xd32f2f),
            new Color(0x7b1fa2),
            new Color(0xFF9100),
            new Color(0x4ac0fd),
            new Color(0xD7CA1E),
            new Color(0x010101)};
    private final Color BOMB_COLOR = new Color(0x000000);
    private final Color flag = new Color(0xf00000);
    private int LEFT_B;
    private int TOP_B;
    private int WIDTH = 520;
    private int HEIGHT = 400;
    private boolean end = false;
    private int flags;
    public int clearedBlocks = 0;
    public boolean won = false;
    int mX = -1;
    int mY = -1;

    public void draw(Graphics g) {
        Font fon = g.getFont();
        g.setFont(new Font(fon.getFontName(), Font.BOLD,
                (int) ((double) blockSize / 6d * 5d)));
        //System.out.println(fon.getAttributes());
        g.setColor(Color.white);
        g.drawString("Flags: " + flags, LEFT_B, blockSize);
        for (int y = 0; y < mask.Y; y++) {
            for (int x = 0; x < mask.X; x++) {
                int xNow = LEFT_B + x * blockSize;
                int yNow = TOP_B + y * blockSize;
                g.setColor(emptyBlock[(x+y)%2]);
                g.fillRect(xNow, yNow, blockSize, blockSize);
                int num = numGrid[coordConvert(x, y)];
                if (num != 0) {
                    try {
                        g.setColor(numbers[num - 1]);
                        g.drawString(Integer.toString(num), xNow + blockSize / 6,
                                yNow + 5 * blockSize/6);
                    } catch (ArrayIndexOutOfBoundsException aioobe) {
                        g.setColor(BOMB_COLOR);
                        g.fillRect(xNow, yNow, blockSize, blockSize);
                    }
                }
                if (mask.val(x, y) != 0) {
                    g.setColor((mask.val(x, y) == 2) ?
                            flag : coveredBlock[(x + y) % 2]);
                    if (x == mX && y == mY) {
                        g.setColor(g.getColor().brighter());
                    }
                    g.fillRect(xNow, yNow, blockSize, blockSize);
                }
                if (end && numGrid[coordConvert(x, y)] == -1) {
                    g.setColor(BOMB_COLOR);
                    g.fillRect(xNow, yNow, blockSize, blockSize);
                }
                if (won) {
                    g.setColor(Color.white);
                    g.drawString("YOU WON",
                            LEFT_B + 5 * blockSize, blockSize);
                }
            }
        }
    }

    //private Point po = new Point(0,0);
    /*public void MouseMoved(Point po) {
        this.po = po;
    }//*/
    public void mouseMoved(Point po) {
        try {
            this.mX = coordConvert(coordConvert(po))[0];
            this.mY = coordConvert(coordConvert(po))[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            this.mX = -1;
            this.mY = -1;
        }
    }

    class Mask {
        int[][] mask;
        final int X, Y;

        public Mask(int x, int y) {
            X = x;
            Y = y;
            mask = new int[y][x];
            for (int i = 0; i < y; i++) {
                for (int j = 0; j < x; j++) {
                    mask[i][j] = 1;
                }
            }
        }

        public void leftClickM(int x, int y) {
            if (mask[y][x] == 1) {
                clearedBlocks++;
                mask[y][x]--;
            } else if (mask[y][x] == 2){
                //System.out.println("ok");
                flags++;
                mask[y][x]--;
            }
            if (numGrid[coordConvert(x,y)] == -1 &&
                    val(x,y) == 0 && mask[y][x] != 2) end = true;
            if (clearedBlocks == grid.length - n) won = end = true;
        }

        public void middleClick(int x, int y) {
            if (mask[y][x] == 0 && countFlags(x, y) == numGrid[coordConvert(x,y)]) {
                for (int i = -1; i < 2; i++) {
                    for (int j = -1; j < 2; j++) {
                        try {
                            if (mask[y][x] != 2) floodFill(x + i, y + j);
                        } catch (ArrayIndexOutOfBoundsException e) {}
                    }
                }
            }
        }

        public void rightClick(int x, int y) {
            //if (mask[y][x] == 1) mask[x][y] = 2;
            //else if (mask[y][x] == 2) mask[x][y] = 1;
            int m = mask[y][x];
            if (m != 0) {
                flags += Grid.leftOrRight(m, 2);
                mask[y][x] -= Grid.leftOrRight(m, 2);
            }
        }

        private int countFlags(int x, int y) {
            int total = 0;
            for (int i = -1; i < 2; i++) {
                for (int j = -1; j < 2; j++) {
                    try {
                        total += (mask[y + i][x + j] == 2) ? 1 : 0;
                    } catch (ArrayIndexOutOfBoundsException aioobe) {}
                }
            }
            return total;
        }

        public boolean at(int x, int y) {
            return (mask[y][x] != 1);
        }
        public int val(int x, int y) {
            return mask[y][x];
        }

        @Override
        public String toString() {
            String str = "";
            for (int[] m : mask) {
                str = str.concat(Arrays.toString(m) + "\n");
            }
            return str;
            //return Arrays.deepToString(this.mask);
        }
    }

    public Grid(int length, int width, float freq) {
        size[0] = length;
        size[1] = width;
        //System.out.println(Arrays.toString(size));
        updateSize(HEIGHT, WIDTH, size[0], size[1], 100, 100);
        //LEFT_B = (WIDTH - size[0] * blockSize) / 2;
        //TOP_B = (HEIGHT - (size[1] * blockSize)) / 2;
        this.freq = freq;
        this.n = (int) (freq * length*width);
        initGrid();
    }

    public Grid() {
        updateSize(HEIGHT, WIDTH);
        initGrid();
        //LEFT_B = (WIDTH - size[0] * blockSize) / 2;
        //TOP_B = (HEIGHT - (size[1] * blockSize)) / 2;
    }

    public void updateSize
            (int height, int width, int x, int y, int excludeY, int excludeX) {
        blockSize = Math.min((height - excludeY) / y, (width - excludeX) / x);
        HEIGHT = height;
        WIDTH = width;
        LEFT_B = (WIDTH - size[0] * blockSize) / 2;
        TOP_B = (HEIGHT + height / 30 - (size[1] * blockSize)) / 2;
    }

    public void updateSize(int height, int width) {
        updateSize(height, width, size[0], size[1], height / 5, 100);
    }

    private void initGrid() {
        grid = new boolean[size[0] * size[1]];
        //int split = (int) (freq * grid.length);
        //System.out.println(split);
        bombs = new int[n];
        for (int i = 0; i < grid.length; i++) grid[i] = (i < n);
        //n = split;
        //shuffle(grid);
        updateGrid();
        //initNumGrid();
//        if (zero == 0) {
//            for (int i = 0; i < 3; i++) {
//                shuffle();
//                if (zero != 0) break;
//            }
//        }
        mask = new Mask(size[0], size[1]);
        flags = n;
    }

    private void initNumGrid() {
        numGrid = new int[grid.length];
        for (int i = 0; i < numGrid.length; i++) {
            int[] xy = coordConvert(i);
            numGrid[i] = countBombs(xy[0], xy[1]);
        }
        zero = countZeros();
        System.out.println(Arrays.toString(numGrid));
    }

    public void shuffle() {
        shuffle(grid);
        updateGrid();
    }

    public void shuffle(int x, int y) {
        initGrid();
        int safeSize = 0;
        //int[] safeSpaces = new int[9];
        ArrayList<Integer> safeSpaces = new ArrayList<>();
        //for (int i = 0; i < 9; i++) safeSpaces[i] = -1;
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++ ) {
                if (allowed(x + i, y + j)) {
                    //safeSpaces[(i + 1) * 3 + y + 1] = coordConvert(x + i, y + j);
                    safeSpaces.add(coordConvert(x + i, y + j));
                    safeSize++;
                }
            }
        }
        //Arrays.sort(safeSpaces);
        boolean[] grid2 = new boolean[grid.length - safeSize];
        System.arraycopy(grid,0,grid2,0,grid2.length);
        shuffle(grid2);
        int posOffset = 0;
        //System.out.println(safeSpaces);
        for (int i = 0; i < grid.length; i++) {
            if (safeSpaces.contains(i)) {
                grid[i] = false;
                posOffset++;
            } else grid[i] = grid2[i - posOffset];
        }
//        int jk = 0;
//        for (boolean j : grid) jk += (j) ? 1 : 0;
//        int g2 = 0;
//        for (boolean i: grid2) g2 += (i) ? 1 : 0;
        updateGrid();
        System.out.println(this);
        System.out.println(countTrue(grid2));
        System.out.println(countTrue(grid));
    }

    // TODO remove
    private int countTrue(boolean[] bools) {
        int total = 0;
        for (boolean bool : bools) {
            total += (bool) ? 1 : 0;
        }
        return total;
    }

    private boolean allowed(int x, int y) {
        int pos = coordConvert(x, y);
        if (pos < 0 || pos > grid.length) return false;
        if (x % size[0] == 0 && x != 0) return false;
        return x >= 0 && y >= 0;
    }

    public void shuffle(boolean[] deck) {
        Random r = new Random();
        int rn;// = r.nextInt(deck.length);
        boolean t;// = deck[rn];
//        int tTracker = 0;
        for (int i = 0; i < deck.length; i++) {
            rn = r.nextInt(deck.length - i);
            t = deck[rn];
//            if (t){
//                bombs[tTracker++] = i;
//            }
            if (deck.length - 1 - rn >= 0)
                System.arraycopy(deck, rn + 1, deck, rn, deck.length - 1 - rn);
            deck[deck.length - 1] = t;
        }
        //Arrays.sort(bombs);
        //System.out.println(Arrays.toString(bombs));
        //initNumGrid();
        //System.out.println(this);
    }

    @Deprecated
    public void cycle(int times) {
        times %= grid.length;
        System.out.println(times);
        if (times == 0) return;
        //TODO fix this code pls is bad
        if (times > 0) {
            for (int i = 0; i < times; i++) {
                boolean r2 = grid[0];
                System.arraycopy(grid, 1, grid, 0, grid.length - 1);
                grid[grid.length - 1] = r2;
                cycleBombs(times);
            }
        } else {
            for (int i = times; i < 0; i++) {
                boolean r2 = grid[grid.length-1];
                System.arraycopy(grid, 0, grid, 1, grid.length - 1);
                grid[0] = r2;
                cycleBombs(times);
            }
        }
        Arrays.sort(bombs);
        initNumGrid();
    }

    public void cycle(int times, boolean[] deck) {
        times %= deck.length;
        if (times == 0) return;
        if (times > 0) {
            for (int i = 0; i < times; i++) {
                boolean r2 = deck[0];
                System.arraycopy(deck, 1, deck, 0, deck.length - 1);
                deck[deck.length - 1] = r2;
            }
        } else {
            for (int i = times; i < 0; i++) {
                boolean r2 = deck[deck.length - 1];
                System.arraycopy(deck, 0, deck, 1, deck.length - 1);
                deck[0] = r2;
            }
        }
    }

    private void cycleBombs(int dir) {
        for (int i = 0; i < bombs.length; i++) {
            bombs[i] = addMod(bombs[i], -leftOrRight(dir,0), grid.length);
        }
    }

    private void updateGrid() {
        int tTracker = 0;
        for (int i = 0; i < grid.length; i++) {
            if (grid[i]) {
                bombs[tTracker] = i;
                tTracker++;
            }
        }
        Arrays.sort(bombs);
        initNumGrid();
        countZeros();
    }

//    private void nudge(int pos) {
//        if (grid[pos]) return;
//        int[] xy = coordConvert(pos);
//        if (numGrid[coordConvert(xy[0]-1,)])
////        for (int i = -1; i < 2; i++) {
////            for (int j = -1; j < 2; j++) {
////                int ix = xy[0] + i;
////                int jy = xy[1] + j;
////                if
////            }
////        }
//        //return pos;
//    }

    private void floodFill(int x, int y) {
        try {
            if (mask.at(x, y)) return;
            mask.leftClickM(x, y);
            if (numGrid[coordConvert(x, y)] != 0) return;
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            //System.out.println(x + " " + y);
            return;
        }
        floodFill(x-1, y);
        floodFill(x-1, y-1);
        floodFill(x, y-1);
        floodFill(x+1, y-1);
        floodFill(x+1, y);
        floodFill(x+1, y+1);
        floodFill(x, y+1);
        floodFill(x-1, y+1);
    }

    public void middleClick(Point p) {
        int[] yes = coordConvert(coordConvert(p));
        middleClick(yes[0], yes[1]);
    }

    public void middleClick(int x, int y) {
        if (!end && start) {
            mask.middleClick(x, y);
            System.out.println(clearedBlocks);
        }
    }

    public void rightClick(Point p) {
        int[] sam = coordConvert(coordConvert(p));
        rightClick(sam[0],sam[1]);
    }

    public void leftClick(Point p) {
        try {
            int[] ben = coordConvert(coordConvert(p));
            //System.out.println(Arrays.toString(ben));
            leftClick(ben[0], ben[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            leftClick(-1, -1);
        }
    }

    public void rightClick(int x, int y) {
        if (!end && start)
            mask.rightClick(x, y);
    }

    private boolean start = false;
    public void leftClick(int x, int y) {
        if (end) {
            reset();
            return;
        }
        try {
            if (!start) shuffle(x, y);
            start = true;
            if (mask.val(x, y) != 2) {
                floodFill(x, y);
            } else mask.leftClickM(x, y);
            //if (numGrid[coordConvert(x,y)] == -1 && mask.val(x,y) == 0) end = true;
            //System.out.println(mask);
            //if (clearedBlocks == grid.length - n) won = end = true;
            System.out.println(clearedBlocks);
        } catch (ArrayIndexOutOfBoundsException e) {}
    }

    private void reset() {
        mask = new Mask(mask.X, mask.Y);
        end = false;
        start = false;
        n = (int) (grid.length * freq);
        flags = n;
        clearedBlocks = 0;
        won = false;
    }

    public int coordConvert(Point p) {
        double px = p.getX();
        if (px < LEFT_B || px > LEFT_B + size[0] * blockSize)
            throw new ArrayIndexOutOfBoundsException((int)px);
        double py = p.getY();
        int x = (int) ((px - LEFT_B) / blockSize);
        int y = (int) ((py - TOP_B) / blockSize);
        if (x < 0 || x > size[0]) y = -1;
        if (px == LEFT_B + size[0] * blockSize) x--;
        return coordConvert(x, y);
    }

    @Deprecated
    public void start(int x, int y) {
        int pos = coordConvert(x, y);
        shuffle(x,y);
//        if (numGrid[pos] != 0) {
//            int[] idealPos = new int[2];
//            zeros = 0;
//            try {
//                floodFillStart(x, y, new boolean[grid.length],
//                        idealPos, new int[]{x, y});
//                System.out.println(Arrays.toString(idealPos));
//                move(idealPos[0] - x, y - idealPos[1], pos);
//            } catch (IllegalStateException ise) {
//                moveBomb(pos);
//            }
//            //Random r = new Random();
////            int tmp = biSearch(pos,Arrays.binarySearch(bombs,pos));
////            System.out.println(tmp);
////            cycle(leftOrRight(tmp, 0) *
////                    (new Random().nextInt(Math.abs(tmp)) + 1) );
////            if (numGrid[pos] > 0) {
////
////            }
//
//            System.out.println(Arrays.toString(bombs));
//            System.out.println(this);
////            int dir = leftOrRight(r.nextInt(2),1);
////            int iBomb = Arrays.binarySearch(bombs, pos);
////            int upIBomb = addMod(iBomb,1,bombs.length);
////            int downIBomb = addMod(iBomb,-1,bombs.length);
////            int range;
////            if (upIBomb == downIBomb) range =
////                    grid.length-1-Math.abs(bombs[upIBomb]-bombs[iBomb]);
////            else {
////                if (upIBomb < iBomb) {
////
////                }
////            }
//////            Math.max(addMod(Arrays.binarySearch(bombs,pos), -1, bombs.length),
//////                    addMod(Arrays.binarySearch(bombs,pos), 1, bombs.length);
////            System.out.println(dir);
////            int tmp = Math.abs(bombs[addMod(
////                    Arrays.binarySearch(bombs,pos), dir, bombs.length)]-pos-1);
////            System.out.println(tmp);
////            if (!(tmp > 0)) {
////                dir = -dir;
////                tmp = Math.abs(bombs[addMod(
////                        Arrays.binarySearch(bombs,pos), dir, bombs.length)]-pos-1);
////                System.out.println(tmp);
////            }
////            int tst = dir * (1 + r.nextInt(tmp));
////            System.out.println(tst);
////            cycle(tst);
////            System.out.println(this);
////            System.out.println(Arrays.toString(bombs));
//        }
        start = true;
    }

    @Deprecated
    private void moveBomb(int pos) {
        System.out.println("oops");
        int bombs = numGrid[pos];
        Random r = new Random();
        try {
            while (bombs == -1) {
                int pos1 = r.nextInt(grid.length);
                boolean b = grid[pos];
                grid[pos] = grid[pos1];
                grid[pos1] = b;
                updateGrid();
                bombs = numGrid[pos];
            }
            int[] xy = coordConvert(pos);
            while (bombs != 0) {
                for (int i = -1; i < 2; i++) {
                    for (int j = -1; j < 2; j++) {
                        int x = xy[0] + i;
                        int y = xy[1] + j;
                        if (advancedCheck(x, y, grid) && !(i == 0 && j == 0)) {
                            int pos1;
                            int pos0 = coordConvert(x,y);
                            while ((pos1 = r.nextInt(grid.length)) == pos0 &&
                                    grid[pos1]);
                            boolean b = grid[pos0];
                            grid[pos0] = grid[pos1];
                            grid[pos1] = b;
                            updateGrid();
                            bombs = numGrid[pos];
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("bomb removal not possible");
        }
    }

    @Deprecated
    public void move(int dx, int dy, int pos) {
        dx %= size[0];
        dy %= size[1];
        boolean[][] gridG = convert(grid,size[0],size[1]);
//        for (int i = 0; i < size[1]; i++ ){
//            boolean[] tmp = new boolean[size[0]];
//            System.arraycopy(grid, coordConvert(0, i), tmp, 0, size[0]);
//            cycle(dx, tmp);
//            System.arraycopy(tmp,0,grid,coordConvert(0,i),tmp.length);
//        }
        for (int i = 0; i < gridG.length; i++) {
            cycle(dx,gridG[i]);
        }
        if (dy != 0) {
            if (dy < 0) dy += size[1];
            final int L = gridG.length - 1;
            for (int i = 0; i < dy; i++) {
                boolean[] tmp = new boolean[size[0]];
                System.arraycopy(gridG[L], 0, tmp, 0, size[0]);
                for (int j = 0; j < size[1]-1; j++) {
                    System.arraycopy(gridG[L - 1 - j], 0,
                            gridG[L - j], 0, size[0]);
                }
                System.arraycopy(tmp, 0, gridG[0], 0, tmp.length);
            }
        }
        this.grid = convert(gridG);
        updateGrid();
        if (numGrid[pos] != 0) throw new IllegalStateException("move failed");
    }

    private boolean[][] convert(boolean[] in, int x, int y) {
        if(x * y > in.length) throw new IllegalArgumentException();
        boolean[][] ret = new boolean[y][x];
        for (int i = 0; i < y; i++) {
            System.arraycopy(in, i * x, ret[i], 0, x);
        }
        return ret;
    }

    private boolean[] convert(boolean[][] in) {
        final int X = in[0].length;
        boolean[] ret = new boolean[in.length*X];
        for (int i = 0; i < in.length; i++) {
            System.arraycopy(in[i], 0, ret, i * X, X);
        }
        return ret;
    }

    @Deprecated
    private int biSearch(int pos, int bombPos) {
        int offset = 1;
        int left = -1;
        int right = -1;
        do {
            int rightPos = addMod(pos,offset,grid.length);
            int leftPos = addMod(pos,-offset,grid.length);
            if(!grid[rightPos] && right == -1) right = rightPos;
            if(!grid[leftPos] && left == -1) left = leftPos;
            offset++;
        } while (left == -1 || right == -1);
        offset = 1;
        boolean leftb = false;
        while (!grid[addMod(right,offset,grid.length)] &&
                !grid[addMod(left, -offset, grid.length)]) {
            leftb = (grid[addMod(left, -offset-1, grid.length)] &&
                    !grid[addMod(right, offset+1, grid.length)]);
            offset++;
        }
        return distance(leftb, pos, bombPos);//(leftb)? addMod(pos,-left,grid.length):
                //addMod(pos, right, grid.length), bombPos);
    }

    private int distance(boolean dir, int pos, int bombPos) {
        int i = addMod(Arrays.binarySearch(bombs,pos), (dir)? 1: -1, bombs.length);
        if ((i < bombPos && dir) || (i > bombPos && !dir))
            return (grid.length - Math.abs(bombs[i] - bombs[bombPos]) - 1) *
                    ((dir)? 1: -1);
        return (Math.abs(bombs[i] - bombs[bombPos]) - 1) * ((dir)? -1: 1);
    }

    public static int leftOrRight(int n, int compare) {
        return ((n - compare) >> 31) | 1;
    }

    private int addMod(int i1, int i2, int mod) {
        int ret = (i1 + i2) % mod;
        if (ret < 0) ret += mod;
        return ret % mod;
    }

    private int coordConvert(int x, int y) {
        if (y < 0 || y > size[1]) throw new ArrayIndexOutOfBoundsException(y);
        if (x < 0 || x > size[0]) throw new ArrayIndexOutOfBoundsException(x);
        return y * size[0] +x;
    }
    private int[] coordConvert(int pos) {
        if (pos < 0 || pos > grid.length) throw new
                ArrayIndexOutOfBoundsException(pos);
        return new int[] {pos % size[0], pos / size[0]};
    }

    private int countBombs(int x, int y) {
        if (advancedCheck(x,y, grid)) return -1;
        int total = 0;
//        if (up(x,y)) total++;
//        if (down(x,y)) total++;
//        if (left(x,y)) total++;
//        if (right(x,y)) total++;
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if (advancedCheck(x + i, y + j, grid) &&
                        !(i == 0 && j == 0)) total++;
            }
        }
        return total;
    }

    private int advancedCheck(int x, int y, int[] numGrid) {
        if ((x % size[0] == 0) && !(x==0)) return -2;
        try {
            return numGrid[coordConvert(x, y)];
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            return -2;
        }
    }

    private boolean advancedCheck(int x, int y, boolean[] grid) {
        if (x < 0 || y < 0) return false;
        if ((x % size[0] == 0) && !(x == 0)) return false;
        try {
            return (grid[coordConvert(x, y)]);
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            return false;
        }
    }

    @Deprecated
    private int countZeros() {
        int zero = 0;
        for (int j : numGrid) {
            zero += (j == 0) ? 1 : 0;
        }
        return zero;
    }
    @Deprecated
    private int zeros = 0;
    @Deprecated
    private void floodFillStart(int x, int y, boolean[] grid,
                                int[] idealPos, int[] pos) {
        if (this.zero == 0) throw new IllegalStateException("no zeros");
        if (idealPos.length != 2) throw new IllegalArgumentException();
        if (this.zero == zeros) return;
        try {
            if (grid[coordConvert(x, y)]) return;
        } catch (ArrayIndexOutOfBoundsException aioobe) {return;}
        int check = advancedCheck(x,y,numGrid);
        grid[coordConvert(x,y)] = true;
        if (check == -2 || check == 0) {
            if (check == 0) {
                zeros++;
                if ((pythagoreanDistance(x, y, pos[0], pos[1]) <
                        pythagoreanDistance(idealPos[0], idealPos[1], pos[0], pos[1]))
                        || idealPos[0] + idealPos[1] == 0) {
                    idealPos[0] = x;
                    idealPos[1] = y;
                }
            }
            return;
        }
        floodFillStart(x-1,y,grid,idealPos,pos);
        floodFillStart(x,y-1,grid,idealPos,pos);
        floodFillStart(x+1,y,grid,idealPos,pos);
        floodFillStart(x,y+1,grid,idealPos,pos);
    }

    private double pythagoreanDistance(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(Math.abs(x1 - x2), 2) +
                Math.pow(Math.abs(y1 - y2), 2));
    }

//    private boolean up(int x, int y) {
//        try {
//            return grid[coordConvert(x, y - 1)];
//        } catch (ArrayIndexOutOfBoundsException aioob) {
//            return false;
//        }
//    }
//
//    private boolean down(int x, int y) {
//        try {
//            return grid[coordConvert(x, y + 1)];
//        } catch (ArrayIndexOutOfBoundsException aioob) {
//            return false;
//        }
//    }
//
//    private boolean right(int x, int y) {
//        try {
//            return grid[coordConvert(x + 1, y)];
//        } catch (ArrayIndexOutOfBoundsException aioob) {
//            return false;
//        }
//    }
//
//    private boolean left(int x, int y) {
//        try {
//            return grid[coordConvert(x - 1, y)];
//        } catch (ArrayIndexOutOfBoundsException aioob) {
//            return false;
//        }
//    }

    public String toString() {
        String ret = "[";
        for (int i = 0; i < size[1]; i++) {
            if (i != 0) ret = ret.concat("]\n[");
            for (int j = 0; j < size[0]; j++) {
                ret = ret.concat(grid[coordConvert(j,i)] +
                        ((j == size[0]-1)? "": ", "));
            }
        }
        return ret + "]";
        //return Arrays.toString(this.grid);
    }
}
