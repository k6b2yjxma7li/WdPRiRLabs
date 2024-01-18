package WdPRiRLabs.Exercises.Ex2;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.BorderLayout;


public class Viewer {
    private static int selfHeight = -1;
    private static int selfWidth = -1;

    private static JFrame selfJFrame = null;
    private static JLabel selfJLabel = null;

    public Viewer() {
        selfJFrame = new JFrame();
        selfJLabel = new JLabel();
        selfJFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public Viewer(int width, int height) {
        selfHeight = height;
        selfWidth = width;
        selfJFrame = new JFrame();
        selfJLabel = new JLabel();
        selfJFrame.setSize(selfWidth, selfHeight);
        selfJFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public Viewer(int width, int height, String title) {
        selfHeight = height;
        selfWidth = width;
        selfJFrame = new JFrame();
        selfJLabel = new JLabel();
        selfJFrame.setTitle(title);
        selfJFrame.setSize(selfWidth, selfHeight);
        selfJFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public static ImageIcon resizeImageIcon(ImageIcon imageIcon, int imageWidth, int imageHeight) {
        Image oldImage = imageIcon.getImage();
        Image newImage = oldImage.getScaledInstance(
            imageWidth,
            imageHeight,
            java.awt.Image.SCALE_SMOOTH
        );
        return new ImageIcon(newImage);  // transform it back
    }

    public static void displayBufferedImage(BufferedImage myBufferedImage) {
        if( selfWidth < 0 || selfHeight < 0) {
            selfWidth = myBufferedImage.getWidth();
            selfHeight = myBufferedImage.getHeight();
        }
        ImageIcon myImageIcon = resizeImageIcon(
            new ImageIcon(myBufferedImage),
            selfWidth, selfHeight);
        selfJLabel.setIcon(myImageIcon);
        selfJFrame.getContentPane().add(selfJLabel, BorderLayout.CENTER);
        selfJFrame.setLocationRelativeTo(null);
        selfJFrame.pack();
        selfJFrame.setVisible(true);
    }
    
}
