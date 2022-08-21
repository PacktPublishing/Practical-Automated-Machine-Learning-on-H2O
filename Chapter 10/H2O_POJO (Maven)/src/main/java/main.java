import hex.genmodel.easy.RowData;
import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.prediction.*;

public class main {
    private static final String modelPOJOClassName = "DRF_1_AutoML_1_20220619_210236";

    public static void main(String[] args) throws Exception {
        hex.genmodel.GenModel rawModel;
        rawModel = (hex.genmodel.GenModel) Class.forName(modelPOJOClassName).getDeclaredConstructor().newInstance();
        EasyPredictModelWrapper model = new EasyPredictModelWrapper(rawModel);

        RowData row = new RowData();
        row.put("C1", 5.1);
        row.put("C2", 3.5);
        row.put("C3", 1.4);
        row.put("C4", 0.2);

        MultinomialModelPrediction predictionResultHandler = model.predictMultinomial(row);
        System.out.println("Predicted Class of Iris flower is: " + predictionResultHandler.label);
        System.out.println("Class probabilities are: ");
        for (int labelClassIndex = 0; labelClassIndex < predictionResultHandler.classProbabilities.length; labelClassIndex++) {
            System.out.println(predictionResultHandler.classProbabilities[labelClassIndex]);
        }
    }
}