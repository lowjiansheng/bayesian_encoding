package bayesian_encoder;

import data_structures.BayesianClique;
import helper.Helper;
import helper.WeightsFileHelper;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class AltBayesianEncoder extends BayesianEncoder{

    private Set<Integer> sourceNodes;

    @Override
    public void encodeBayesianQueryIntoCNF(int numVariables,
                                           List<BayesianClique> cliques, Map<Integer,Boolean> evidence){
        String fileNameCNFEncoding = "output_encoding.cnf";
        String fileNameWeights = "output_weights.txt";
        // Create CNF file
        try {
            BufferedWriter encodingWriter = new BufferedWriter(new FileWriter(fileNameCNFEncoding));
            BufferedWriter weightsWriter = new BufferedWriter(new FileWriter(fileNameWeights));

            identifySourceNodes(cliques);

            createCNFFileHeaders(encodingWriter, weightsWriter, numVariables, cliques, evidence);
            createCNFsAndWeights(encodingWriter, weightsWriter, numVariables, cliques);
            createHints(encodingWriter, evidence);

            encodingWriter.close();
            weightsWriter.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void createCNFsAndWeights(BufferedWriter encoderWriter, BufferedWriter weightsWriter,
                                      int numVariables, List<BayesianClique> cliques) throws IOException {
        // The starting offset for the all the subsequent chance nodes.
        int literalId = numVariables;

        // This keeps track of State Nodes that have already been encoded to prevent double encoding.
        Set<Integer> stateNodeEncoded = new HashSet<>();

        for (BayesianClique clique : cliques) {
            List<Integer> variables = clique.getVariables();
            // Source node. Can just write in the weights for its Chance Nodes.
            if (variables.size() == 1) {
                WeightsFileHelper.writeWeightsLine(weightsWriter, variables.get(0),
                        clique.getFunctionTable()[0][1], false);
                WeightsFileHelper.writeWeightsLine(weightsWriter, variables.get(0),
                        clique.getFunctionTable()[0][0], true);
                stateNodeEncoded.add(variables.get(0));

            } else {
                // Creating weights for State Nodes of variables. They should all be of weight 1.
                for (int var : variables) {
                    if (!sourceNodes.contains(var) && !stateNodeEncoded.contains(var)) {
                        WeightsFileHelper.writeWeightsLine(weightsWriter, var,
                                1, false);
                        WeightsFileHelper.writeWeightsLine(weightsWriter, var,
                                1, true);
                        stateNodeEncoded.add(var);
                    }
                }

                /////// Create CLAUSES according to clique. ///////
                // This is the State ID of the bayesian variable getting implied.
                int impliedBayesianNodeStateVarId = variables.get(variables.size() - 1 );
                double numChanceNodes = Math.pow(2, variables.size() - 1);
                for (int chanceNodeId = 0 ; chanceNodeId < numChanceNodes; chanceNodeId++ ) {
                    boolean[] bits = Helper.getBitsOfInteger(variables.size() - 1, chanceNodeId);
                    // Left side of implication. All the State Nodes that imply the bayesian node.
                    String leftSideImplication = "";
                    for (int j = 0 ; j < bits.length; j++) {
                        if (bits[j]) {
                            leftSideImplication += "-" + variables.get(j) + " ";
                        } else {
                            leftSideImplication += variables.get(j) + " ";
                        }
                    }
                    int literalIdOfChanceNode = literalId + chanceNodeId;
                    // Right side of implication. Chance node with State Node of current Bayesian Node.
                    // Will need 2 of this.
                    String rightSideImplicationOne = "-" + impliedBayesianNodeStateVarId + " " + literalIdOfChanceNode + " 0\n";
                    String rightSideImplicationTwo = "-" + literalIdOfChanceNode + " " + impliedBayesianNodeStateVarId + " 0\n";
                    encoderWriter.write(leftSideImplication + rightSideImplicationOne);
                    encoderWriter.write(leftSideImplication + rightSideImplicationTwo);

                    // Create Weights for Chance Nodes according to clique.
                    WeightsFileHelper.writeWeightsLine(weightsWriter, literalIdOfChanceNode,
                            clique.getFunctionTable()[chanceNodeId][1], false);
                    WeightsFileHelper.writeWeightsLine(weightsWriter, literalIdOfChanceNode,
                            clique.getFunctionTable()[chanceNodeId][0], true);

                }
                literalId += numChanceNodes;
            }
        }
    }


    private void createCNFFileHeaders(BufferedWriter encoderWriter, BufferedWriter weightsWriter, int numVariables, List<BayesianClique> cliques,
                                      Map<Integer, Boolean> evidence) throws IOException {
        // Initialised to number of State Variables
        int totalNumVariables = numVariables;
        int totalNumClauses = 0;

        // Total number of chance variables
        for (BayesianClique clique : cliques){
            if (clique.getVariables().size() > 1 ) {
                totalNumVariables += Math.pow(2, clique.getVariables().size() - 1);
                totalNumClauses += 2 * Math.pow(2, clique.getVariables().size() - 1);
            }
        }
        // Add clauses created by evidence
        totalNumClauses += evidence.size();

        encoderWriter.write("c SAT CNF BAYESIAN ENCODING \n");
        encoderWriter.write("p cnf " + totalNumVariables + " " +  totalNumClauses + "\n");
        weightsWriter.write("p " + totalNumVariables + "\n");
    }

    private void identifySourceNodes(List<BayesianClique> cliques) {
        this.sourceNodes = new HashSet<>();
        for (BayesianClique clique : cliques) {
            if (clique.getVariables().size() == 1){
                sourceNodes.add(clique.getVariables().get(0));
            }
        }
    }

    private void createHints(BufferedWriter encoderWriter, Map<Integer,Boolean> evidence) throws IOException {
        for (Map.Entry<Integer, Boolean> entry : evidence.entrySet()) {
            String indicatorVariable;
            // Adding constraints to the State Nodes
            if (entry.getValue()) {
                indicatorVariable = "-" + entry.getKey();
            } else {
                indicatorVariable = entry.getKey().toString();
            }
            encoderWriter.write(indicatorVariable + " 0\n");
        }
    }
}
