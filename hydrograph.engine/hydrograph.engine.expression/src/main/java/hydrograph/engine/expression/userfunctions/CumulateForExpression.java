/*******************************************************************************
 * Copyright 2017 Capital One Services, LLC and Bitwise, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 *******************************************************************************/
package hydrograph.engine.expression.userfunctions;

import hydrograph.engine.expression.utils.ExpressionWrapper;
import hydrograph.engine.transformation.userfunctions.base.CumulateTransformBase;
import hydrograph.engine.transformation.userfunctions.base.ReusableRow;

import java.util.ArrayList;
import java.util.Properties;

@SuppressWarnings("rawtypes")
public class CumulateForExpression implements CumulateTransformBase {

	private ExpressionWrapper expressionWrapper;
	private Object accumulatorValue;

	public void setValidationAPI(ExpressionWrapper expressionWrapper) {
		this.expressionWrapper = expressionWrapper;
	}


	public CumulateForExpression() {
	}

	public void init(){
			expressionWrapper.getValidationAPI().init(expressionWrapper.getIntialValueExpression());
			accumulatorValue = expressionWrapper.getValidationAPI()
					.exec(new Object[]{});
	}

	public void callPrepare(String[] inputFieldNames,String[] inputFieldTypes){
		try {
			String fieldNames[] = new String[inputFieldNames.length + 1];
			String fieldTypes[] = new String[inputFieldTypes.length + 1];
			for(int i=0;i<inputFieldNames.length;i++){
				fieldNames[i]=inputFieldNames[i];
				fieldTypes[i]=inputFieldTypes[i];
			}
			fieldNames[inputFieldNames.length] = "_accumulator";
			fieldTypes[inputFieldNames.length] = "Object";
			expressionWrapper.getValidationAPI().init(fieldNames,fieldTypes);
		} catch (Exception e) {
			throw new RuntimeException(
					"Exception in aggregate: "
							+ expressionWrapper.getIntialValueExpression() + ".", e);
		}
	}
	
	@Override
	public void prepare(Properties props, ArrayList<String> inputFields,
			ArrayList<String> outputFields, ArrayList<String> keyFields) {
	}

	@Override
	public void cumulate(ReusableRow input, ReusableRow output) {
		Object tuples[] = new Object[input.getFieldNames().size() + 1];
		int i = 0;
		for (; i < input.getFieldNames().size(); i++) {
			tuples[i] = input.getField(i);
		}
		tuples[i] = accumulatorValue;
		try {
			accumulatorValue = expressionWrapper.getValidationAPI().exec(
					tuples);
		} catch (Exception e) {
			throw new RuntimeException("Exception in cumulate expression: "
					+ expressionWrapper.getValidationAPI().getExpr() + ".\nRow being processed: "
					+ input.toString(), e);
		}
		output.setField(0, (Comparable) accumulatorValue);
	}

	@Override
	public void onCompleteGroup() {
		try {
			accumulatorValue = expressionWrapper.getValidationAPI()
					.execute(expressionWrapper.getIntialValueExpression());
		} catch (Exception e) {
			throw new RuntimeException(
					"Exception in cumulate initial value expression: "
							+ expressionWrapper.getIntialValueExpression() + ".", e);
		}
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}

}
