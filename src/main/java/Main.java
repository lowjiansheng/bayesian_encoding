import bayesian_encoder.AltBayesianEncoder;
import bayesian_encoder.BayesianEncoder;
import bayesian_encoder.ClassBayesianEncoder;
import data_structures.BayesianClique;
import parser.UAIParser;

import java.util.List;
import java.util.Map;

public class Main {

    public enum ENCODER {
        CLASS_ENCODER, ALT_ENCODER
    }

    public static void main(String[] args) {
        String modelFileName = args[0];
        String evidenceFileName = args[1];
        ENCODER encoderUsed = ENCODER.valueOf(args[2]);

        UAIParser parser = new UAIParser();
        parser.parse(modelFileName, UAIParser.FILE_TYPE.MODEL);
        List<BayesianClique> cliques = parser.getCliques();

        parser.parse(evidenceFileName, UAIParser.FILE_TYPE.EVIDENCE);

        // Instantiate either the ClassBayesianEncoder or the AltBayesianEncoder
        BayesianEncoder encoder = chooseEncoder(encoderUsed);

        encoder.encodeBayesianQueryIntoCNF(parser.getNumVariables(),
                cliques, parser.getQueryValues());

        System.out.println("Finished encoding network in CNF.");
    }


    private static BayesianEncoder chooseEncoder(ENCODER encoderSelected) {
        BayesianEncoder encoder = null;
        switch (encoderSelected) {
            case CLASS_ENCODER:
                System.out.println("Using the encoding scheme taught in class...");
                encoder = new ClassBayesianEncoder();
                break;
            case ALT_ENCODER:
                System.out.println("Using an alternative encoding scheme...");
                encoder = new AltBayesianEncoder();
                break;
            default:
                System.out.println("Please input a legal encoder type.");
                System.exit(1);
        }
        return encoder;
    }

}
