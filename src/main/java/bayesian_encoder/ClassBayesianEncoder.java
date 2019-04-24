package bayesian_encoder;

import data_structures.*;
import helper.Helper;
import helper.WeightsFileHelper;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ClassBayesianEncoder extends BayesianEncoder{

    private String CNF_ENDER = "0\n";

    @Override
    public void encodeBayesianQueryIntoCNF(int numVariables,
                                           List<BayesianClique> cliques, Map<Integer,Boolean> evidence) {
        String fileNameCNFEncoding = "output_encoding.cnf";
        String fileNameWeights = "output_weights.txt";
        // Create CNF file
        try {
            BufferedWriter encodingWriter = new BufferedWriter(new FileWriter(fileNameCNFEncoding));
            BufferedWriter weightsWriter = new BufferedWriter(new FileWriter(fileNameWeights));

            createCNFHeaders(encodingWriter, weightsWriter, numVariables, cliques, evidence);
            createTypeOneConstraints(encodingWriter, weightsWriter, numVariables);
            createTypeTwoConstraints(encodingWriter, weightsWriter, numVariables, cliques);
            createHints(encodingWriter, evidence);

            encodingWriter.close();
            weightsWriter.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void createCNFHeaders(BufferedWriter encoderWriter, BufferedWriter weightsWriter,int numVariables, List<BayesianClique> cliques,
                                  Map<Integer, Boolean> evidence) throws IOException{
        int totalNumVariables = 0;
        int totalNumClauses = 0;
        // Indicator variables
        totalNumVariables += 2 * numVariables;
        totalNumClauses += 2 * numVariables;
        // Parameter variables
        for (BayesianClique clique : cliques) {
            totalNumVariables += Math.pow(2,clique.getVariables().size());
            totalNumClauses += Math.pow(2, clique.getVariables().size()) * (clique.getVariables().size() + 1);
        }
        // Evidence
        totalNumClauses += evidence.size();
        encoderWriter.write("c SAT CNF Class Encoding Scheme \n");
        encoderWriter.write("p cnf " + totalNumVariables + " " +  totalNumClauses + "\n");
        weightsWriter.write("p " + totalNumVariables + "\n");
    }


    /**
     * Creates the CNF that fulfils Type 1 constraints. Only 1 indicator variable allowed.
     */
    private void createTypeOneConstraints(BufferedWriter encoderWriter, BufferedWriter weightsWriter, int numVariables) throws IOException{
        if (numVariables == 0) {
            return;
        }
        for (int i = 0 ; i < numVariables; i++) {
            // Ia1 -> Ia2
            String rightImplication = "-" + (2*i) + " " + (2*i+1) + " " +CNF_ENDER;
            encoderWriter.write(rightImplication);
            // Ia2 -> Ia1
            String leftImplication = "-" + (2*i+1) + " " + (2*i) + " " + CNF_ENDER;
            encoderWriter.write(leftImplication);

            // All indicator variables will have weights of 1
            WeightsFileHelper.writeWeightsLine(weightsWriter, 2*i, 1, false);
            WeightsFileHelper.writeWeightsLine(weightsWriter, 2*i, 1, true);
            WeightsFileHelper.writeWeightsLine(weightsWriter, 2*i+1, 1, false);
            WeightsFileHelper.writeWeightsLine(weightsWriter, 2*i+1, 1, true);
        }
    }

    private void createTypeTwoConstraints(BufferedWriter encoderWriter, BufferedWriter weightsWriter,
                                          int numVariables, List<BayesianClique> cliques) throws IOException {
        int parameterVariableID = 2 * numVariables;
        for (BayesianClique clique : cliques) {
            // The last variable is the Bayesian Node being implied.
            List<Integer> variables = clique.getVariables();
            double totalNumCombinations = Math.pow(2, variables.size());

            // Creating Parameter Variables
            for (int i = 0 ; i < totalNumCombinations; i++) {
                // Get the bits of i to retrieve the value of indicator variable
                // If bit is true, we will use 2n*variable_value
                // If bit is false, we will use 2n*variable_value + 1
                if (variables.size() == 0) {
                    System.out.println("Parser/file error. Not possible for a clique to contain 0 variables.");
                    System.exit(1);
                }
                // Bit Array is Big Endian : integerBits[0] is the Bayesian Node being implied.
                boolean[] integerBits = Helper.getBitsOfInteger(variables.size(), i);

                StringBuilder leftImplication = new StringBuilder();
                for (int j = 0 ; j < integerBits.length; j++) {
                    int indicatorVariable;
                    int variableId = variables.get(integerBits.length - 1 - j);
                    // Mapping Integer bits to its respective Indicator Variable.
                    if (integerBits[j])
                        indicatorVariable = 2 * variableId;
                    else {
                        indicatorVariable = 2 * variableId + 1;
                    }

                    String rightImplication = "-" + parameterVariableID + " " + indicatorVariable + " " + CNF_ENDER;
                    encoderWriter.write(rightImplication);
                    leftImplication.append("-").append(indicatorVariable).append(" ");
                }
                leftImplication.append(parameterVariableID).append(" ").append(CNF_ENDER);
                encoderWriter.write(leftImplication.toString());

                float positiveWeightValue = findWeightValue(integerBits, clique.getFunctionTable());
                WeightsFileHelper.writeWeightsLine(weightsWriter, parameterVariableID, positiveWeightValue, false);
                WeightsFileHelper.writeWeightsLine(weightsWriter, parameterVariableID, 1, true);
                parameterVariableID++;
            }
        }
    }


    /**
     * findWeightValue maps a particular Parameter variable to its weight value
     * @param integerBits
     * @param functionTable
     * @return
     */
    private float findWeightValue(boolean[] integerBits, float[][] functionTable) {
        int secondIndex;
        // Bit Array is Big Endian : integerBits[0] is the Bayesian Node being implied.
        if (integerBits[0]) {
            secondIndex = 1;
        } else {
            secondIndex = 0;
        }
        int firstIndex = Helper.bitsToInteger(Arrays.copyOfRange(integerBits, 1, integerBits.length));
        return functionTable[firstIndex][secondIndex];
    }


    private void createHints(BufferedWriter writer, Map<Integer, Boolean> evidence) throws IOException {
        for (Map.Entry<Integer, Boolean> entry : evidence.entrySet()) {
            int indicatorVariable;
            if (entry.getValue()) {
                indicatorVariable = 2 * entry.getKey();
            } else {
                indicatorVariable = 2 * entry.getKey() + 1;
            }
            writer.write(Integer.toString(indicatorVariable) + " 0\n");
        }
    }
}
