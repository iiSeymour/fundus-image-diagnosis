import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

/*  This class creates histograms of all the fundus images:

    Class requires:

    .jpg files

    Class produces:

    .txt files
*/

class ColourHistogram {

    public static void main(String[] arg) {

        BufferedImage f_image = null;
        FileOutputStream f_write = null;
        RGBSplit Splitter = new RGBSplit();

        if (!(new File("../img").exists())) {
            System.err.print("Directory ../img does not exist!");
            System.exit(-1);
        }

        ArrayList<File> images = new ArrayList<File>();
        File file_list = new File("../img/.");
        for(File f : file_list.listFiles()){
            images.add(f);
        }

        if (images.size() == 0) {
            System.out.println("No images found in ./img");
            System.exit(-1);
        }

        // Create the output directory if needed
        new File("../data/histograms").mkdirs();

        // For all images .,/img/*.jpg
        for (int i=0;i<images.size();i++) {

            // Display progress
            System.out.println("Image:"+(i+1)+"/"+images.size()+" "+ images.get(i).getName());

            try {
                // Get the fundus image
                f_image = ImageIO.read((images.get(i)));
                // Create a file to store the histogram
                f_write = new FileOutputStream(new File(resultsFile(images.get(i).getName())));
            }

            catch (IOException e) {
                System.err.println(e.getMessage());
                System.exit(-1);
            }

            PrintStream f_out = new PrintStream(f_write);

            int n = f_image.getWidth() * f_image.getHeight();
            int[] R,G,B;
            R = new int[n];
            G = new int[n];
            B = new int[n];

            // Get the RGB of the image
            R = Splitter.getR(f_image);
            G = Splitter.getG(f_image);
            B = Splitter.getB(f_image);

            // Create histogram[value][R/G/B] of RGB
            double histogram[][] = new double[256][3];

            for (int j=1;j<n;j++) {
                histogram[R[j]][0]++;
                histogram[G[j]][1]++;
                histogram[B[j]][2]++;
            }

            // Store histograms as percentages,
            // Ignore the high number of zero values
            for (int j=1;j<256;j++) {
                histogram[j][0] = histogram[j][0]/(n-histogram[0][0]);
                histogram[j][1] = histogram[j][1]/(n-histogram[0][1]);
                histogram[j][2] = histogram[j][2]/(n-histogram[0][2]);
            }

            // Write to file
            f_out.printf("%-5s\t%-12s\t%-12s\t%-12s\t%n","Value","R","G","B");

            for (int j=1;j<256;j++) {
                f_out.printf("%-5d\t%-12.10f\t%-12.10f\t%-12.10f\t%n",
                             j, histogram[j][0],histogram[j][1], histogram[j][2]);
            }
        }
        System.out.println("Done!");
    }

    public static String resultsFile(String s) {
        // Strip the image extension
        if (s==null) return null;
        int pos = s.lastIndexOf(".");
        if (pos == -1) return s;
        String r = "../data/histograms/" + s.substring(pos-1, pos) + "_" + s.substring(0,2) + ".txt";
        return r;
    }
}
