package minesweeper_java;

import javax.swing.JFrame;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class Main {

    static Grid gr;

    public static void main(String[] args) {
//        Integer[] toto = new Integer[10];
//        for (int i = 0; i < 10; i++) toto[i] = i+1;
//        Grid.shuffle(toto);
//        System.out.println(Arrays.toString(toto));
        gr = new Grid();//5,6,.2f);
        EventQueue.invokeLater(() -> {
            JFrame mfw = new MainWindow();
            mfw.setVisible(true);
        });
        //System.out.println(gr);
        //Scanner scan = new Scanner(System.in);
//        while (true) {// System.out.println(gr.
//          leftOrRight(scan.nextInt(),scan.nextInt()));
//            int n = scan.nextInt();
//            if (n < 0) {
//                gr.shuffle();
//            } else {
//                gr.click(n,scan.nextInt());
//            }
//        }
        //gr.move(scan.nextInt(),scan.nextInt(),0);
//        gr.cycle(2);
//        System.out.println(gr);
//        gr.cycle(-3);
//        System.out.println(gr);
    }

    static class MainWindow extends JFrame implements MouseListener,
            MouseMotionListener {
        int x, y = 0;
        AnimatedPanel an = new AnimatedPanel(520,400,Double.NaN) {
            @Override
            public void draw(Graphics g) {
                gr.draw(g);
                g.drawString(Float.toString(an.frameRate),
                        an.getWidth()-100, an.getHeight()-20);
            }
        };

        public MainWindow() {
            initMainWindow();
        }

        private void initMainWindow() {
            an.addMouseListener(this);
            an.addMouseMotionListener(this);
            this.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent ce) {
                    super.componentResized(ce);
                    gr.updateSize(an.getHeight(),an.getWidth());
                }
            });
            this.add(an);
            //this.setResizable(false);
            this.pack();

            this.setTitle("Minesweeper");
            this.setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        }

        @Override
        public void mouseClicked(MouseEvent me) {
            if (me.getButton() == MouseEvent.BUTTON3) {
                gr.rightClick(me.getPoint());
            } else if (me.getButton() == MouseEvent.BUTTON1) {
                gr.leftClick(me.getPoint());
            } else if (me.getButton() == MouseEvent.BUTTON2) {
                gr.middleClick(me.getPoint());
            }
        }

        @Override
        public void mousePressed(MouseEvent me) {

        }

        @Override
        public void mouseReleased(MouseEvent me) {

        }

        @Override
        public void mouseEntered(MouseEvent mouseEvent) {

        }

        @Override
        public void mouseExited(MouseEvent me) {

        }

        @Override
        public void mouseDragged(MouseEvent me) {

        }

        @Override
        public void mouseMoved(MouseEvent me) {
            gr.mouseMoved(me.getPoint());
        }
    }
}
