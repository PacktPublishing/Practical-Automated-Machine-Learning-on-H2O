import java.io.*;
import h2o-genmodel.genmodel.easy.RowData;
import h2o-genmodel.genmodel.easy.EasyPredictModelWrapper;
import h2o-genmodel.genmodel.easy.prediction.*;

public class main {
    private static String modelClassName = "DRF_1_AutoML_1_20220619_141718";

    public static void main(String[] args) throws Exception {
        hex.genmodel.GenModel rawModel;
        rawModel = (hex.genmodel.GenModel) Class.forName(modelClassName).newInstance();
        EasyPredictModelWrapper model = new EasyPredictModelWrapper(rawModel);

        RowData row = new RowData();
        row.put("Year", "1987");
        row.put("Month", "10");
        row.put("DayofMonth", "14");
        row.put("DayOfWeek", "3");
        row.put("CRSDepTime", "730");
        row.put("UniqueCarrier", "PS");
        row.put("Origin", "SAN");
        row.put("Dest", "SFO");

        BinomialModelPrediction p = model.predictBinomial(row);
        System.out.println("Label (aka prediction) is flight departure delayed: " + p.label);
        System.out.print("Class probabilities: ");
        for (int i = 0; i < p.classProbabilities.length; i++) {
            if (i > 0) {
                System.out.print(",");
            }
            System.out.print(p.classProbabilities[i]);
        }
        System.out.println("");
    }
}