import java.io.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import weka.classifiers.functions.*;
import weka.core.*;
import weka.filters.*;
import weka.classifiers.*;
import weka.classifiers.meta.*;
import weka.classifiers.trees.*;
import weka.attributeSelection.*;
import weka.classifiers.functions.*;
import weka.classifiers.bayes.*;
import weka.core.converters.ConverterUtils.*;

/*
   This class diagnoses a fundus images with one
   of three ophthalmic conditions (healthy,glaucoma,
   diabetic retinopathy).

   Class requires:

   .jpg file

   Class produces:

   .arff file
*/

class Diagnose {

    public static final int var = 8;
    public static String headings[] = new String[2];
    public static String attributes[] = new String[var];
    public static String classes[] = new String[4];
    public static double x[] = new double[var];

    public static void main(String[] arg) throws Exception {

        //=====================================
        // MODEL IMAGE
        //=====================================

        // Get image as argument
        if (arg.length == 0) {
            System.err.println("Usage: java Diagnose [IMAGE]");
            System.exit(-1);
        }

        File image = new File(arg[0]);
        BufferedImage f_image = ImageIO.read(image);

        // Build histogram of image
        RGBSplit Splitter = new RGBSplit();

        double histogram[][] = new double[3][256];
        int n = f_image.getWidth()*f_image.getHeight();
        int R[],B[], G[];

        R = new int[n];
        B = new int[n];
        G = new int[n];

        R = Splitter.getR(f_image);
        B = Splitter.getB(f_image);
        G = Splitter.getG(f_image);

        for(int i=0;i<n;i++) {
            histogram[0][R[i]]++;
            histogram[1][B[i]]++;
            histogram[2][G[i]]++;
        }

        // Ignore the high number of zero values
        for(int i=1;i<256;i++) {
            histogram[0][i] = histogram[0][i]/(n-histogram[0][0]);
            histogram[1][i] = histogram[1][i]/(n-histogram[1][0]);
            histogram[2][i] = histogram[2][i]/(n-histogram[2][0]);
        }

        // Strings for ARFF file
        headings[0] = "@RELATION fundus\n";
        headings[1] = "@DATA";

        attributes[0] = "@ATTRIBUTE RED_AVG1 REAL";
        attributes[1] = "@ATTRIBUTE RED_AVG2 REAL";
        attributes[2] = "@ATTRIBUTE RED_MAX1 REAL";
        attributes[3] = "@ATTRIBUTE RED_MAX2 REAL";
        attributes[4] = "@ATTRIBUTE GREEN_AVG1 REAL";
        attributes[5] = "@ATTRIBUTE GREEN_MAX1 REAL";
        attributes[6] = "@ATTRIBUTE BLUE_AVG1 REAL";
        attributes[7] = "@ATTRIBUTE BLUE_MAX1 REAL";

        x[0] = getAverage(histogram,0,155,185);
        x[1] = getAverage(histogram,0,185,205);
        x[2] = getMax(histogram,0,155,185);
        x[3] = getMax(histogram,0,185,205);
        x[4] = getAverage(histogram,2,15,85);
        x[5] = getMax(histogram,2,15,85);
        x[6] = getAverage(histogram,1,10,45);
        x[7] = getMax(histogram,1,10,45);

        classes[0] = "@ATTRIBUTE class {g,h,r}\n";
        classes[1] = "@ATTRIBUTE class {r,¬r}\n";
        classes[2] = "@ATTRIBUTE class {g,¬g}\n";
        classes[3] = "@ATTRIBUTE class {h,¬h}\n";

        String[] models = new String[4];
        models[0] = "../data/models/fundus.model";
        models[1] = "../data/models/diabetic.model";
        models[2] = "../data/models/glaucoma.model";
        models[3] = "../data/models/healthy.model";

        // Create output directory
        new File("../data/diagnosis").mkdirs();

        String ARFFfile = "../data/diagnosis/unclassified";

        int[][] features = new int[4][var];

        // Each model uses different features

        // model 1 - fundus
        features[0][0] = 0;
        features[0][1] = 1;
        features[0][2] = 1;
        features[0][3] = 0;
        features[0][4] = 0;
        features[0][5] = 0;
        features[0][6] = 1;
        features[0][7] = 1;
        // model 2 - diabetic
        features[1][0] = 0;
        features[1][1] = 0;
        features[1][2] = 1;
        features[1][3] = 0;
        features[1][4] = 0;
        features[1][5] = 1;
        features[1][6] = 0;
        features[1][7] = 1;
        // model 3 - glaucoma
        features[2][0] = 0;
        features[2][1] = 0;
        features[2][2] = 0;
        features[2][3] = 1;
        features[2][4] = 0;
        features[2][5] = 0;
        features[2][6] = 0;
        features[2][7] = 1;
        // model 4 - healthy
        features[3][0] = 1;
        features[3][1] = 0;
        features[3][2] = 1;
        features[3][3] = 0;
        features[3][4] = 0;
        features[3][5] = 0;
        features[3][6] = 1;
        features[3][7] = 1;

        //=====================================
        //CLASSIFY
        //=====================================

        DataSource source = null;
        Instances data = null;
        Classifier c = null;
        String[] p = new String[4];
        double pred;

        // i = 1: don't use fundus model
        for (int i = 1;i<4;i++) {
            // Write arff file for each model
            writeARFF(ARFFfile+i+".arff",features,i);
            source = new DataSource(ARFFfile+i+".arff");
            data = source.getDataSet();
            if (data.classIndex() == -1)
                data.setClassIndex(data.numAttributes()-1);
            // Load model
            c = (Classifier) weka.core.SerializationHelper.read(models[i]);
            // Make prediction
            pred = c.classifyInstance(data.instance(data.numInstances()-1));
            p[i] = data.classAttribute().value((int) pred);
        }

        // Check if  models agree
        int agreement = 0, diagnosis = 0;

        String[] condition = new String[4];

        condition[0] = "Diagnosis could not be reached!";
        condition[1] = "Diagnosis: Diabetic Retinopathy";
        condition[2] = "Diagnosis: Glaucoma";
        condition[3] = "Diagnosis: Healthy";

        if (p[1].equals("¬r"))
            agreement++;
        else
            diagnosis = 1;
        if (p[2].equals("¬g"))
            agreement++;
        else
            diagnosis = 2;
        if (p[3].equals("¬h"))
            agreement++;
        else
            diagnosis = 3;

        // One condition
        if (agreement == 2)
            System.out.println(condition[diagnosis]);
        // All conditions or none
        if (agreement == 0 || agreement == 3)
            System.out.println(condition[0]);

        // false negative case
        if (agreement == 1) {
            if (p[3].equals("¬h"))
                System.out.println(condition[0]);
            else {
                // else healthy + condition
                if (p[1].equals("¬r"))
                    // condition glaucoma
                    System.out.println(condition[2]);
                else
                    // condition diabetic retinopathy
                    System.out.println(condition[1]);
            }
        }

    }

    public static void writeARFF(String ARFFfile, int[][] f, int m) {
        try {
            FileOutputStream f_write = new FileOutputStream(new File(ARFFfile));
            PrintStream f_out = new PrintStream(f_write);

            // f = features of model
            // m = model number

            f_out.println(headings[0]);
            for (int i=0;i<var;i++) {
                if (f[m][i] == 1)
                    f_out.println(attributes[i]);
            }

            f_out.println(classes[m]);
            f_out.println(headings[1]);

            for (int i=0;i<var;i++) {
                if (f[m][i] == 1)
                    f_out.printf("%12.10f,",x[i]);
            }

            f_out.print("?");

        }

        catch (Exception e) {
            System.err.println(e.getStackTrace());
        }
    }

    // Methods that model the images

    public static double getAverage(double[][] x, int h, int a, int b) {
        // Return the average number of pixals between a and b
        double avg = 0.0;

        for(int i=a;i<b;i++)
            avg += x[h][i];

        avg = avg/(b-a);
        return avg;
    }

    public static double getMax(double[][] x, int h, int a, int b) {
        // Return the value with highest count between a and b
        double p = 0.0;
        for(int i=a;i<b;i++) {
            if (x[h][i] > p) {
                p = x[h][i];
            }
        }

        return p;
    }

}
