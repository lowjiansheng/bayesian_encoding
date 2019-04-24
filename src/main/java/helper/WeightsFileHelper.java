package helper;

import java.io.BufferedWriter;
import java.io.IOException;

import static helper.Helper.LINE_END;

public class WeightsFileHelper {

    public static void writeWeightsLine(BufferedWriter writer,
                                        int literalId, float weight, boolean isNegated) throws IOException {
        if (isNegated) {
            writer.write("w -" + literalId + " " + weight + " " + LINE_END);
        } else {
            writer.write("w " + literalId + " " + weight + " " + LINE_END);
        }
    }

}
