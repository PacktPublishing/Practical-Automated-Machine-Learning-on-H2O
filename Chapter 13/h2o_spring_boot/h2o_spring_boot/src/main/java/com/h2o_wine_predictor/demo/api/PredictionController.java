package com.h2o_loan_approver.demo.api;

import com.h2o_loan_approver.demo.service.PredictionService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping(value="api/v1/prediction")
@RestController
public class PredictionController {
    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @PostMapping
    public String makePrediction(@RequestBody Map<String, Object> requestBody) {
        return predictionService.getPrediction(requestBody);
    }
}
