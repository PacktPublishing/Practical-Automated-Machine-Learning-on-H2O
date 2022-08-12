package com.h2o_loan_approver.demo.service;

import java.util.Map;

import com.h2o_loan_approver.demo.model.BadLoanModel;
import com.h2o_loan_approver.demo.model.InterestRateModel;
import hex.genmodel.easy.prediction.BinomialModelPrediction;
import hex.genmodel.easy.prediction.RegressionModelPrediction;
import hex.genmodel.easy.*;
import org.springframework.stereotype.Service;

@Service
public class PredictionService {
    // Set to true for demo mode (to print the predictions to stdout).
    // Set to false to get better throughput.
    static final boolean VERBOSE = true;

    static EasyPredictModelWrapper badLoanModel;
    static EasyPredictModelWrapper interestRateModel;

    static {
        BadLoanModel rawBadLoanModel = new BadLoanModel();
        badLoanModel = new EasyPredictModelWrapper(rawBadLoanModel);

        InterestRateModel rawInterestRateModel = new InterestRateModel();
        interestRateModel = new EasyPredictModelWrapper(rawInterestRateModel);
    }

    @SuppressWarnings("unchecked")
    private RowData fillRowDataFromHttpRequest(Map<String, Object> featureMap) {
        RowData row = new RowData();
        if (VERBOSE) System.out.println();
        for (Map.Entry<String, Object> entry : featureMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().toString();
            if (value.length() > 0) {
                row.put(key, value);
            }
        }
        return row;
    }

    private BinomialModelPrediction predictBadLoan (RowData row) throws Exception {
        return badLoanModel.predictBinomial(row);
    }

    private RegressionModelPrediction predictInterestRate (RowData row) throws Exception {
        return interestRateModel.predictRegression(row);
    }

    private String createJsonResponse(BinomialModelPrediction p, RegressionModelPrediction p2) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"labelIndex\" : ").append(p.labelIndex).append(",\n");
        sb.append("  \"label\" : \"").append(p.label).append("\",\n");
        sb.append("  \"classProbabilities\" : ").append("[\n");
        for (int i = 0; i < p.classProbabilities.length; i++) {
            double d = p.classProbabilities[i];
            if (Double.isNaN(d)) {
                throw new RuntimeException("Probability is NaN");
            }
            else if (Double.isInfinite(d)) {
                throw new RuntimeException("Probability is infinite");
            }

            sb.append("    ").append(d);
            if (i != (p.classProbabilities.length - 1)) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("  ],\n");
        sb.append("\n");
        sb.append("  \"interestRate\" : ").append(p2.value).append("\n");
        sb.append("}\n");

        return sb.toString();
    }

    public String getPrediction (Map<String, Object> featureMap)  {
        RowData row = fillRowDataFromHttpRequest(featureMap);

        try {
            BinomialModelPrediction predictionForBadLoan = predictBadLoan(row);
            RegressionModelPrediction predictionForInterestRate = predictInterestRate(row);
            String predictionResult = createJsonResponse(predictionForBadLoan, predictionForInterestRate);

            if (VERBOSE) System.out.println("prediction(bad loan)     : " + predictionForBadLoan.classProbabilities[1]);
            if (VERBOSE) System.out.println("prediction(interest rate): " + predictionForInterestRate.value);

            return predictionResult;
        }
        catch (Exception e) {
            // Prediction failed.
            System.out.println(e.getMessage());
            // response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, e.getMessage());
            return "Prediction Failed";
        }
    }
}
