import java.awt.image.BufferedImage;

/*
  Calculates the RGB values of each pixal
  for a given image.

 */

public class RGBSplit {

    public RGBSplit() {

    }

    public int[] getR(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        int R[] = new int [w*h];

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int pixel = image.getRGB(j, i);
                R[i+j] = (pixel >>> 16) & 0xff;
            }
        }
        return R;
    }

    public int[] getG(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        int G[] = new int [w*h];

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int pixel = image.getRGB(j, i);
                G[i+j] = (pixel >>> 8) & 0xff;
            }
        }
        return G;
    }

    public int[] getB(BufferedImage image) {

        int w = image.getWidth();
        int h = image.getHeight();
        int B[] = new int [w*h];

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int pixel = image.getRGB(j, i);
                B[i+j] = (pixel) & 0xff;
            }
        }
        return B;
    }
 }
