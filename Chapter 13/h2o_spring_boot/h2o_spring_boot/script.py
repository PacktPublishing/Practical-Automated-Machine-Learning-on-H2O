import h2o
import shutil
from h2o.automl import H2OAutoML

h2o.init()

print("Training models on dataset...")
shutil.rmtree("tmp")
loans = h2o.import_file(path = "src/main/resources/loan.csv")
loans["bad_loan"] = loans["bad_loan"].asfactor()

rand  = loans.runif(seed = 1234567)
train = loans[rand <= 0.8]
valid = loans[rand > 0.8]

myY = "bad_loan"
myX = ["loan_amnt", "longest_credit_length", "revol_util", "emp_length",
       "home_ownership", "annual_inc", "purpose", "addr_state", "dti",
       "delinq_2yrs", "total_acc", "verification_status", "term"]

aml_for_bad_loan = H2OAutoML(max_models=10, seed=123, exclude_algos=["StackedEnsemble"], max_runtime_secs=300)
aml_for_bad_loan.train(x = myX, y = myY, training_frame = train, validation_frame = valid)
model = aml_for_bad_loan.leader
model.model_id = "BadLoanModel"
print(model)

# Download generated POJO for model
# model.download_pojo(path = "src/main/java/com/h2o_loan_approver/demo")
model.download_pojo(path = "tmp")
with open("tmp/BadLoanModel.java", "r") as rawModelPOJO:
       with open("src/main/java/com/h2o_loan_approver/demo/model/BadLoanModel.java", "w") as modelPOJO:
              modelPOJO.write(f'package com.h2o_loan_approver.demo;\n' + rawModelPOJO.read())

myY = "int_rate"
myX = ["loan_amnt", "longest_credit_length", "revol_util", "emp_length",
       "home_ownership", "annual_inc", "purpose", "addr_state", "dti",
       "delinq_2yrs", "total_acc", "verification_status", "term"]
aml_for_interest_rate = H2OAutoML(max_models=10, seed=123, exclude_algos=["StackedEnsemble"], max_runtime_secs=300)
aml_for_interest_rate.train(x = myX, y = myY, training_frame = train, validation_frame = valid)
model = aml_for_interest_rate.leader
model.model_id = "InterestRateModel"
print(model)

# Download generated POJO for model
model.download_pojo(path = "tmp")
with open("tmp/InterestRateModel.java", "r") as rawModelPOJO:
       with open("src/main/java/com/h2o_loan_approver/demo/model/InterestRateModel.java", "w") as modelPOJO:
              modelPOJO.write(f'package com.h2o_loan_approver.demo;\n' + rawModelPOJO.read())


