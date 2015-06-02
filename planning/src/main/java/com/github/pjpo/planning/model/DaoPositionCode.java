package com.github.pjpo.planning.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class DaoPositionCode {

	private final BufferedWriter writer; 

	private final BufferedReader reader;
	
	public DaoPositionCode(final BufferedWriter writer) {
		this.writer = writer;
		this.reader = null;
	}
	
	public DaoPositionCode(final BufferedReader reader) {
		this.reader = reader;
		this.writer = null;
	}
	
	public void store(final PositionCode positionCode) throws IOException {
		
		if (writer == null)
			throw new IOException("This dao has no writer defined");
		
		if (positionCode.getName().contains("\n") || positionCode.getName().contains("\r"))
			throw new IOException("Position name invalid");
		
		writer.append(positionCode.getName()).append("\n");
		
		final StringBuilder tokenBuilder = new java.lang.StringBuilder("<<>>");
		
		while (positionCode.getCode().contains(tokenBuilder)) {
			tokenBuilder.append(">").insert(0, "<");
		}
		
		writer.append(tokenBuilder).append("\n");
		writer.append(positionCode.getCode()).append("\n");
		writer.append(tokenBuilder).append("\n");
	}	

	public PositionCode load() throws IOException {
		
		String positionName = null;
		String separator = null;
		final StringBuilder script = new StringBuilder();
		String readed = null;
		
		while ((readed = reader.readLine()) != null) {
			// positionName not defined => define it
			if (positionName == null) {
				positionName = readed;
			}
			// positionName has been defined ; if separator has not been defined, define it
			else if (separator == null) {
				separator = readed;
			}
			// positionName and separator have been defined, see if we reached end of script
			else if (readed.equals(separator)) {
				// We have a new position defined, return it
				final PositionCode positionCode = new PositionCode();
				positionCode.setName(positionName);
				positionCode.setCode(script.toString());
				return positionCode;
			}
			// WE ARE IN SCRIPT, STORE IT
			else {
				script.append(readed);
			}
		}
		
		return null;
	}
}
