import java.io.*;
import java.util.ArrayList;

/*
  This class models the fundus image histograms
  created from ColourHistogram class, all combinations for a
  given number of variables are created (feature selection)

  Class requires:

  .txt files

  Class produces:

  .arff files
*/

class FeatureSelection {

    // Number of varibles to include
    private static final int numVar = 8;

    public static void main(String[] arg) {

        BufferedReader f_in = null;
        double sigma;
        double[][] r,g,b,m;
        String name[];

        if(!(new File("../data/histograms/").exists())) {
            System.err.println("Directory ../data/histograms/ does not exist!");
            System.exit(-1);
        }

        ArrayList<File> histograms = new ArrayList<File>();
        File h_gram = new File("../data/histograms/");
        for(File h : h_gram.listFiles()){
            histograms.add(h);
        }

        if (histograms.size()== 0) {
            System.err.println("Directory ../data/histograms/ empty!");
            System.exit(-1);
        }

        int n = histograms.size();
        m = new double[n][255];
        r = new double[n][255];
        g = new double[n][255];
        b = new double[n][255];
        name = new String[n];
        String line = null;

        // For each histogram
        for (int i=0;i<histograms.size();i++) {

            try {
                f_in = new BufferedReader(new FileReader(histograms.get(i)));
                name[i] = histograms.get(i).getName();

                // Skip header in rile
                f_in.readLine();

                for (int j=0;j<255;j++) {
                    line = f_in.readLine();
                    String s[] = line.split("\t");
                    r[i][j] = Double.valueOf(s[1]);
                    g[i][j] = Double.valueOf(s[2]);
                    b[i][j] = Double.valueOf(s[3]);
                    sigma = (r[i][j]+g[i][j]+b[i][j])/3;
                    m[i][j] = sigma;
                }
            }

            catch (IOException e) {
                System.err.println(e.getMessage());
                System.exit(-1);
            }
        }

        // File strings
        String headings[] = new String[2];
        headings[0] = "@RELATION fundus\n";
        headings[1] = "@DATA";

        String attributes[] = new String[12];
        attributes[0] = "@ATTRIBUTE RED_AVG1 REAL";
        attributes[1] = "@ATTRIBUTE RED_AVG2 REAL";
        attributes[2] = "@ATTRIBUTE RED_MAX1 REAL";
        attributes[3] = "@ATTRIBUTE RED_MAX2 REAL";
        attributes[4] = "@ATTRIBUTE GREEN_AVG1 REAL";
        attributes[5] = "@ATTRIBUTE GREEN_MAX1 REAL";
        attributes[6] = "@ATTRIBUTE BLUE_AVG1 REAL";
        attributes[7] = "@ATTRIBUTE BLUE_MAX1 REAL";
        attributes[8] = "@ATTRIBUTE AVG1 REAL";
        attributes[9] = "@ATTRIBUTE AVG2 REAL";
        attributes[10] = "@ATTRIBUTE MAX1 REAL";
        attributes[11] = "@ATTRIBUTE MAX2 REAL";

        String classes[] = new String[4];
        classes[0] = "@ATTRIBUTE class {g,h,r}\n";
        classes[1] = "@ATTRIBUTE class {r,¬r}\n";
        classes[2] = "@ATTRIBUTE class {g,¬g}\n";
        classes[3] = "@ATTRIBUTE class {h,¬h}\n";

        double x[][] = new double[histograms.size()][12];
        String condition[][] = new String[histograms.size()][4];

        // Calculate values
        for (int i=0;i<histograms.size();i++) {
            x[i][0] = getAverage(r,i,155,185);
            x[i][1] = getAverage(r,i,185,205);
            x[i][2] = getMax(r,i,155,185);
            x[i][3] = getMax(r,i,185,205);
            x[i][4] = getAverage(g,i,15,85);
            x[i][5] = getMax(g,i,15,85);
            x[i][6] = getAverage(b,i,10,45);
            x[i][7] = getMax(b,i,10,45);
            x[i][8] = getAverage(m,i,25,45);
            x[i][9] = getAverage(m,i,25,45);
            x[i][10] = getMax(m,i,205,225);
            x[i][11] = getMax(m,i,205,225);
            condition[i][0] = getClass(histograms.get(i).getName());
            if (condition[i][0].equals("r"))
                condition[i][1] = "r";
            else
                condition[i][1] = "¬r";
            if (condition[i][0].equals("g"))
                condition[i][2] = "g";
            else
                condition[i][2] = "¬g";
            if (condition[i][0].equals("h"))
                condition[i][3] = "h";
            else
                condition[i][3] = "¬h";
        }

        // Create output directory
        new File("../data/arff").mkdirs();

        String files[] = new String[4];
        files[0] = "../data/arff/fundus";
        files[1] = "../data/arff/diabetic";
        files[2] = "../data/arff/glaucoma";
        files[3] = "../data/arff/healthy";

        // Create all combinations of variables (i.e 2^numVar)
        boolean combin[][] = new boolean[255][numVar];

        for (int i=1;i<Math.pow(2,numVar);i++) {
            StringBuilder binary = new StringBuilder(Integer.toBinaryString(i));
            // pad out the zeros
            for(int j=binary.length();j<numVar;j++)
                binary.insert(0,'0');
            // map to bool
            for(int j=0;j<numVar;j++) {
                if (binary.substring(j,j+1).equals("1"))
                    combin[i-1][j] = true;
                else
                    combin[i-1][j] = false;
            }
        }

        try {
            FileOutputStream f_write = null;
            PrintStream f_out = null;

            // 255 combinations, 8 variables, 8^2-1
            for (int k=0;k<Math.pow(2,numVar)-1;k++) {
                // for all files
                for (int f=0;f<4;f++) {

                    f_write = new FileOutputStream(new File(files[f]+(k+1)+".arff"));
                    f_out = new PrintStream(f_write);

                    // print headings
                    f_out.println(headings[0]);
                    for (int i=0;i<8;i++) {
                        if (combin[k][i])
                            f_out.println(attributes[i]);
                    }

                    f_out.println(classes[f]);
                    f_out.println(headings[1]);

                    // print values
                    for (int i=0;i<histograms.size();i++) {
                        for (int j=0;j<8;j++) {
                            if (combin[k][j])
                                f_out.printf("%12.10f,",x[i][j]);
                        }
                        f_out.printf("%s%n",condition[i][f]);
                    }
                }
            }
            System.out.println("Done!");
         }

        catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }

    //Methods that model the images

    public static double getAverage(double[][] x, int h, int a, int b) {
        // Return the average number of pixals between a and b
        double avg = 0.0;

        for(int i=a;i<b;i++)
            avg += x[h][i];

        avg = avg/(b-a);
        return avg;
    }

    public static double getMax(double[][] x, int h, int a, int b) {
        // Return the value with highest count betwenn a and b
        double p = 0.0;
        for(int i=a;i<b;i++) {
            //Find the highest count
            if (x[h][i] > p) {
                //Store the value
                p = x[h][i];
            }
        }
        return p;
    }

    public static String getClass(String s) {
        // Return the class of the image, 'g','h','r'
        if (s==null) return null;
        int pos = s.lastIndexOf("_");
        if (pos == -1) return s;
        String r = s.substring(pos-1,pos);
        return r;
    }

}
