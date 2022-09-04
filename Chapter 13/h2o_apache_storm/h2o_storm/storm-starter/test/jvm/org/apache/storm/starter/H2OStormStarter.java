package org.apache.storm.starter;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Map;


public class H2OStormStarter {


  /**
   * The ScoreBolt is responsible for obtaining class probabilities from the score pojo.
   * It emits these probabilities to a ClassifierBolt, which classifies the observation as "cat" or "dog".
   */
  public static class PredictionBolt extends BaseRichBolt {
    OutputCollector _collector;

    @Override
    public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
      _collector = collector;
    }

    @Override
    public void execute(Tuple tuple) {

      HeartFailureComplications h2oModel = new HeartFailureComplications();

      // get the input tuple as a String[]
      ArrayList<String> stringData = new ArrayList<String>();
      for (Object tuple_value : tuple.getValues()) stringData.add((String) tuple_value);
      String[] rawData = stringData.toArray(new String[stringData.size()]);

      // the score pojo requires a single double[] of input.
      // We handle all of the categorical mapping ourselves
      double data[] = new double[rawData.length - 1]; //drop the Label

      String[] columnName = tuple.getFields().toList().toArray(new String[tuple.size()]);

      // if the column is a factor column, then look up the value, otherwise put the double
      for (int i = 1; i < rawData.length; ++i) {
        data[i - 1] = h2oModel.getDomainValues(columnName[i]) == null
                ? Double.valueOf(rawData[i])
                : h2oModel.mapEnum(h2oModel.getColIdx(columnName[i]), rawData[i]);
      }

      // get the predictions
      double[] predictions = new double[h2oModel.nclasses() + 1];
      //h2oModel.predict(data, predictions);
      h2oModel.score0(data, predictions);

      // emit the results
      _collector.emit(tuple, new Values(rawData[0], predictions[1]));
      _collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
      declarer.declare(new Fields("expected_class", "dogProbability"));
    }
  }

  /**
   * The ClassifierBolt receives the input probabilities and then makes a classification.
   * It uses a threshold value to determine how to classify the observation, which is computed based on the validation
   * done during model fitting.
   */
  public static class ClassifierBolt extends BaseRichBolt {
    OutputCollector _collector;
    final double _threshold = 0.54;

    @Override
    public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
      _collector = collector;
    }

    @Override
    public void execute(Tuple tuple) {
      String expected = tuple.getString(0);
      double complicationProb = tuple.getDouble(1);
      _collector.emit(tuple, new Values(expected, complicationProb <= _threshold ? "No Complication" : "Possible Complication"));
      _collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
      declarer.declare(new Fields("expected_class", "class"));
    }
  }

  @Test
  public static void h2o_storm() throws Exception {
    TopologyBuilder builder = new TopologyBuilder();

    builder.setSpout("inputDataRow", new H2ODataSpout(), 10);
    builder.setBolt("scoreProbabilities", new PredictionBolt(), 3).shuffleGrouping("inputDataRow");
    builder.setBolt("classifyResults", new ClassifierBolt(), 3).shuffleGrouping("scoreProbabilities");

    Config conf = new Config();
    conf.setDebug(true);

    LocalCluster cluster = new LocalCluster();
    cluster.submitTopology("HeartComplicationPredictor", conf, builder.createTopology());
    Utils.sleep(1000 * 60 * 60);
    cluster.killTopology("HeartComplicationPredictor");
    cluster.shutdown();
  }
}
