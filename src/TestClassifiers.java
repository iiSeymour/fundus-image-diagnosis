import java.io.*;
import java.util.Random;
import weka.core.*;
import weka.filters.*;
import weka.classifiers.*;
import weka.classifiers.misc.*;
import weka.classifiers.meta.*;
import weka.classifiers.trees.*;
import weka.classifiers.rules.*;
import weka.attributeSelection.*;
import weka.classifiers.functions.*;
import weka.classifiers.bayes.*;
import weka.classifiers.lazy.*;
import weka.core.converters.ConverterUtils.*;
import weka.filters.unsupervised.attribute.Remove;

/*
  This class runs the set of chosen classifiers on each
  .arff file contained in ../data/arff/* and produces a
  results file for each variable combination (feature selection).

  Class requires:

  .arff files

  Class produces:

  .txt files
*/

class TestClassifiers{

    // Number of classifier to use
    private static final int classNum = 65;
    // Number of experiments i.e 2^number of varibles -1
    private static final int experiments = 255;

    public static void main(String[] arg) throws Exception {

        DataSource source = null;
        Instances data = null;
        Evaluation eval = null;

        Classifier c[] = new Classifier[classNum];

        int classLow = 0;
        int classHigh = 0;

        // Options for selection of classifiers
        try {
            if (arg[0].equalsIgnoreCase("--bayes")) {
                classLow = 0;
                classHigh = 6;
            }
            else if (arg[0].equalsIgnoreCase("--function")) {
                classLow = 7;
                classHigh = 11;
            }
            else if (arg[0].equalsIgnoreCase("--lazy")) {
                classLow = 12;
                classHigh = 15;
            }
            else if (arg[0].equalsIgnoreCase("--meta")) {
                classLow = 16;
                classHigh = 39;
            }
            else if (arg[0].equalsIgnoreCase("--misc")) {
                classLow = 40;
                classHigh = 45;
            }
            else if (arg[0].equalsIgnoreCase("--rules")) {
                classLow = 46;
                classHigh = 53;
            }
            else if (arg[0].equalsIgnoreCase("--trees")) {
                classLow = 54;
                classHigh = 64;
            }
            else if (arg[0].equalsIgnoreCase("--all")) {
                classLow = 0;
                classHigh = 64;
            }
            else if (arg[0].equalsIgnoreCase("--help") || arg[0].equalsIgnoreCase("-h")) {
                System.out.print("Usage: java classify [OPTIONS]\n\n" +
                                 "Classification options\n" +
                                 "  --bayes\tUse the Bayesian set of classifiers\n" +
                                 "  --functions\tUse the Function set of classifiers\n" +
                                 "  --lazy\tUse the Lazy set of classifiers\n" +
                                 "  --meta\tUse the Meta set of classifiers\n" +
                                 "  --misc\tUse the Misc set of classifiers\n" +
                                 "  --rules\tUse the Rules set of classifiers\n" +
                                 "  --trees\tUse the Trees set of classifiers\n" +
                                 "  --all \tUse the all of the classifiers\n\n" +
                                 "Help options\n" +
                                 "  -h, --help\tDisplays this message\n"
                                 );
                System.exit(0);
            }
            else {
                System.err.print("Unknown option: " + arg[0] + "\nUse --help for list of options\n");
                System.exit(-1);
            }

        }

        catch (Exception e) {
            System.err.print("Usage: java classify [OPTIONS]\n");
            System.exit(-1);
        }

        // Create instance of all classifiers

        // bayes - run time 74 seconds
        c[0] = new ComplementNaiveBayes();
        c[1] = new DMNBtext();
        c[2] = new NaiveBayes();
        c[3] = new NaiveBayesMultinomial();
        c[4] = new NaiveBayesMultinomialUpdateable();
        c[5] = new NaiveBayesSimple();
        c[6] = new NaiveBayesUpdateable();
        // function - run time 3339 seconds
        c[7] = new Logistic();
        c[8] = new MultilayerPerceptron();
        c[9] = new RBFNetwork();
        c[10] = new SimpleLogistic();
        c[11] = new SMO();
        // lazy - run time 57 seconds
        c[12] = new IB1();
        c[13] = new IBk();
        c[14] = new KStar();
        c[15] = new LWL();
        // meta - run time 14979 seconds
        c[16] = new AdaBoostM1();
        c[17] = new AttributeSelectedClassifier();
        c[18] = new Bagging();
        c[19] = new ClassificationViaClustering();
        c[20] = new ClassificationViaRegression();
        c[21] = new CVParameterSelection();
        c[22] = new Dagging();
        c[23] = new Decorate();
        c[24] = new END();
        //c[25] = new EnsembleSelection();
        c[26] = new FilteredClassifier();
        c[27] = new Grading();
        c[28] = new LogitBoost();
        c[29] = new MultiBoostAB();
        c[30] = new MultiClassClassifier();
        c[31] = new MultiScheme();
        c[32] = new OrdinalClassClassifier();
        c[33] = new RacedIncrementalLogitBoost();
        c[34] = new RandomCommittee();
        c[35] = new RandomSubSpace();
        c[36] = new RotationForest();
        c[37] = new Stacking();
        c[38] = new StackingC();
        c[39] = new Vote();
        // misc - run time 78 seconds
        //c[40] = new FLR();
        //c[41] = new HyperPipes();
        //c[42] = new MinMaxExtension();
        //c[43] = new OLM();
        //c[44] = new OSDL();
        c[45] = new VFI();
        // rules - run time 350 seconds
        c[46] =  new ConjunctiveRule();
        c[47] =  new DecisionTable();
        c[48] =  new DTNB();
        c[49] =  new NNge();
        c[50] =  new OneR();
        c[51] =  new PART();
        c[52] =  new Ridor();
        c[53] =  new ZeroR();
        // trees - run time 3099 seconds
        c[54] = new BFTree();
        c[55] = new DecisionStump();
        c[56] = new FT();
        c[57] = new J48();
        c[58] = new J48graft();
        c[59] = new LADTree();
        c[60] = new LMT();
        c[61] = new NBTree();
        c[62] = new RandomForest();
        c[63] = new RandomTree();
        c[64] = new REPTree();

        // Check input dir
        if(!(new File("../data/arff/").exists())) {
            System.err.println("Directory ../data/arff/ does not exist!");
            System.exit(-1);
        }

        if(new File("../data/arff/").list().length == 0) {
            System.err.println("Directory ../data/arff/ is empty!");
            System.exit(-1);
        }

        // Create output directory
        new File("../data/results").mkdirs();

        // Label each classifier
        String s[] = new String[classNum];
        for (int i=0;i<classNum;i++){
            s[i] = "classifier "+i;
        }

        // Names of models for ARFF files
        String arff[] = new String[4];
        arff[0] = "fundus";
        arff[1] = "diabetic";
        arff[2] = "glaucoma";
        arff[3] = "healthy";

        // Time experiments
        double accuracy;
        long t1 = System.nanoTime();

        try {

            FileOutputStream f_write = null;
            PrintStream f_out = null;

            // Feature selection
            for (int k=0;k<experiments;k++) {
                // Unique results file for every experiment
                f_write = new FileOutputStream(new File("../data/results/result"+(k+1)+".txt"));
                f_out = new PrintStream(f_write);

                // For each model
                for (int j=0;j<4;j++) {
                    source = new DataSource("../data/arff/"+arff[j]+(k+1)+".arff");
                    data = source.getDataSet();
                    if (data.classIndex() == -1)
                        data.setClassIndex(data.numAttributes() - 1);
                    eval = new Evaluation(data);

                    // For all selected classifiers
                    for (int i=classLow;i<=classHigh;i++) {
                        System.out.println(i);
                        c[i].buildClassifier(data);
                        eval.crossValidateModel(c[i], data, 10, new Random(1));
                        f_out.printf("%-5s%-12s%-21s%4.2f%s%n",k+1,arff[j],s[i],eval.pctCorrect(),"%");
                    }
                }
                // Update progress
                System.out.print("====> File: "+(k+1)+" done <====");
                System.out.print("====> File: "+(k+1)+" done <====");
                System.out.println("====> File: "+(k+1)+" done <====");
            }

        }

        catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }

        // Total running time
        long t2 = System.nanoTime();
        System.out.println("\nRunning Time: " + (t2-t1)/1000000000 + " seconds");
    }
}
