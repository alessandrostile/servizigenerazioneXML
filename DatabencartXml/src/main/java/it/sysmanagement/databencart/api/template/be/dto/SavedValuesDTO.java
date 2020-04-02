package it.sysmanagement.databencart.api.template.be.dto;

import javax.servlet.http.HttpServletResponse;

public class SavedValuesDTO {

	public SavedValuesDTO(String typeOfOutput, HttpServletResponse response, String response2, String dataValue) {
		this.setTypeOfOutput(typeOfOutput);
		this.setResponse(response);
		this.setResponse2(response2);
		this.setDataValue(dataValue);		
	}

	public SavedValuesDTO(String typeOfOutput, HttpServletResponse response, String response2,
			boolean collectionClausole, String dataType, String dataValue) {
		this.setTypeOfOutput(typeOfOutput);
		this.setResponse(response);
		this.setResponse2(response2);
		this.setCollectionClausole(collectionClausole);
		this.setDataType(dataType);
		this.setDataValue(dataValue);

	}

	public String getTypeOfOutput() {
		return typeOfOutput;
	}

	public void setTypeOfOutput(String typeOfOutput) {
		this.typeOfOutput = typeOfOutput;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	public String getResponse2() {
		return response2;
	}

	public void setResponse2(String response2) {
		this.response2 = response2;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public boolean isCollectionClausole() {
		return collectionClausole;
	}

	public void setCollectionClausole(boolean collectionClausole) {
		this.collectionClausole = collectionClausole;
	}

	public String getDataValue() {
		return dataValue;
	}

	public void setDataValue(String dataValue) {
		this.dataValue = dataValue;
	}

	private String typeOfOutput;
	private HttpServletResponse response;
	private String response2;
	private boolean collectionClausole;
	private String dataType;
	private String dataValue;

}
