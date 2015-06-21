package com.github.pjpo.planning.model.dao;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Map.Entry;

import com.github.pjpo.planning.model.Worker;
import com.github.pjpo.planning.utils.IntervalDateTime;
import com.google.common.collect.HashMultimap;

/**
 * Data access object for physicians : can read or write a physician on a
 * writer or from a reader
 * @author jp@dm.lan
 *
 */
public class DaoPhysician {
	
	private final BufferedWriter writer; 

	private final BufferedReader reader;
	
	/**
	 * Creates the Dao for writing
	 * @param writer
	 */
	public DaoPhysician(final BufferedWriter writer) {
		this.writer = writer;
		this.reader = null;
	}
	
	/**
	 * Creates the Dao for reading 
	 * @param reader
	 */
	public DaoPhysician(final BufferedReader reader) {
		this.reader = reader;
		this.writer = null;
	}

	/**
	 * Stores the employee defintions on the writer
	 * @param physician
	 * @throws IOException
	 */
	public void store(final Worker physician) throws IOException {

		if (writer == null)
			throw new IOException("This dao has no writer defined");
		
		writer.append("02:");
		writer.append(physician.getName() == null ? "N" : ":" + physician.getName()).append('\n');
		writer.append("03:");
		writer.append(physician.getTimePart() == null ? "N" : ":" + physician.getTimePart().toString()).append('\n');
		for (final IntervalDateTime interval : physician.getPaidVacations()) {
			writer.append("06:");
			writer.append(interval.getStart() == null ? "N" : interval.getStart().toString()).append(';');
			writer.append(interval.getEnd() == null ? "N" : interval.getEnd().toString()).append('\n');
		}
		for (final IntervalDateTime interval : physician.getUnpaidVacations()) {
			writer.append("07:");
			writer.append(interval.getStart() == null ? "N" : interval.getStart().toString()).append(';');
			writer.append(interval.getEnd() == null ? "N" : interval.getEnd().toString()).append('\n');
		}
		for (final Entry<LocalDate, String> entry : physician.getWorkedPositions().entries()) {
				writer.append("08:");
				writer.append(entry.getKey().toString()).append(';');
				writer.append(entry.getValue()).append('\n');
		}
		for (final String poste : physician.getRefusedPositions()) {
			writer.append("09:");
			writer.append(poste).append('\n');
		}
		writer.append("99:END").append('\n');
	}

	/**
	 * Loads the employee from the reader
	 * @return
	 * @throws IOException
	 */
	public Worker load() throws IOException {

		if (reader == null)
			throw new IOException("This dao has no reader defined");

		String readedLine = null;
		
		final Worker physician = new Worker();
		physician.setPaidVacations(new LinkedList<>());
		physician.setRefusedPositions(new LinkedList<>());
		physician.setUnpaidVacations(new LinkedList<>());
		physician.setWorkedVacs(HashMultimap.create());

		while ((readedLine = reader.readLine()) != null && !readedLine.startsWith("01:plages")) {

			if (readedLine.startsWith("02:") && readedLine.charAt(3) == ':') {
				physician.setName(readedLine.substring(4));
			} else if (readedLine.startsWith("03:") && readedLine.charAt(3) == ':') {
				physician.setTimePart(Integer.decode(readedLine.substring(4)));
			} else if (readedLine.startsWith("06:")) {
				int splitPosition = readedLine.indexOf(';', 3);
				String start = readedLine.substring(3, splitPosition);
				String end = readedLine.substring(splitPosition + 1);
				physician.getPaidVacations().add(new IntervalDateTime(
						start.equals("N") ? null : LocalDateTime.parse(start),
								end.equals("N") ? null : LocalDateTime.parse(end)));
			} else if (readedLine.startsWith("07:")) {
				int splitPosition = readedLine.indexOf(';', 3);
				String start = readedLine.substring(3, splitPosition);
				String end = readedLine.substring(splitPosition + 1);
				physician.getUnpaidVacations().add(new IntervalDateTime(
						start.equals("N") ? null : LocalDateTime.parse(start),
								end.equals("N") ? null : LocalDateTime.parse(end)));
			} else if (readedLine.startsWith("08:")) {
				int splitPosition = readedLine.indexOf(';', 3);
				LocalDate date = LocalDate.parse(readedLine.substring(3, splitPosition));
				String poste = readedLine.substring(splitPosition + 1);
				physician.getWorkedPositions().put(date, poste);
			} else if (readedLine.startsWith("09:")) {
				physician.getRefusedPositions().add(readedLine.substring(3));
			} else if (readedLine.equals("99:END")) {
				return physician;
			}
		}

		return null;
	}
	
}
