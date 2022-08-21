import h2o
h2o.init()
data_frame = h2o.import_file("Dataset/iris.data")
features = data_frame.columns
label = "C5"
features.remove(label)
aml=h2o.automl.H2OAutoML(max_models=10, seed = 5)
aml.train(x = features, y = label, training_frame = data_frame)
h2o.download_pojo(aml.leader)