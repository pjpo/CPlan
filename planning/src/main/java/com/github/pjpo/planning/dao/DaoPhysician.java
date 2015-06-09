package com.github.pjpo.planning.dao;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Map.Entry;

import com.github.pjpo.planning.model.Physician;
import com.github.pjpo.planning.utils.IntervalDateTime;
import com.google.common.collect.HashMultimap;

public class DaoPhysician {
	
	private final BufferedWriter writer; 

	private final BufferedReader reader;
	
	public DaoPhysician(final BufferedWriter writer) {
		this.writer = writer;
		this.reader = null;
	}
	
	public DaoPhysician(final BufferedReader reader) {
		this.reader = reader;
		this.writer = null;
	}

	public void store(final Physician physician) throws IOException {

		if (writer == null)
			throw new IOException("This dao has no writer defined");
		
		writer.append("02:");
		writer.append(physician.getName() == null ? "N" : ":" + physician.getName()).append('\n');
		writer.append("03:");
		writer.append(physician.getTimePart() == null ? "N" : ":" + physician.getTimePart().toString()).append('\n');
		for (final IntervalDateTime interval : physician.getPaidVacation()) {
			writer.append("06:");
			writer.append(interval.getStart() == null ? "N" : interval.getStart().toString()).append(';');
			writer.append(interval.getEnd() == null ? "N" : interval.getEnd().toString()).append('\n');
		}
		for (final IntervalDateTime interval : physician.getUnpaidVacation()) {
			writer.append("07:");
			writer.append(interval.getStart() == null ? "N" : interval.getStart().toString()).append(';');
			writer.append(interval.getEnd() == null ? "N" : interval.getEnd().toString()).append('\n');
		}
		for (final Entry<LocalDate, String> entry : physician.getWorkedVacs().entries()) {
				writer.append("08:");
				writer.append(entry.getKey().toString()).append(';');
				writer.append(entry.getValue()).append('\n');
		}
		for (final String poste : physician.getRefusedPostes()) {
			writer.append("09:");
			writer.append(poste).append('\n');
		}
		writer.append("99:END").append('\n');
	}

	public Physician load() throws IOException {

		if (reader == null)
			throw new IOException("This dao has no reader defined");

		String readedLine = null;
		
		final Physician physician = new Physician();
		physician.setPaidVacation(new LinkedList<>());
		physician.setRefusedPostes(new LinkedList<>());
		physician.setUnpaidVacation(new LinkedList<>());
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
				physician.getPaidVacation().add(new IntervalDateTime(
						start.equals("N") ? null : LocalDateTime.parse(start),
								end.equals("N") ? null : LocalDateTime.parse(end)));
			} else if (readedLine.startsWith("07:")) {
				int splitPosition = readedLine.indexOf(';', 3);
				String start = readedLine.substring(3, splitPosition);
				String end = readedLine.substring(splitPosition + 1);
				physician.getUnpaidVacation().add(new IntervalDateTime(
						start.equals("N") ? null : LocalDateTime.parse(start),
								end.equals("N") ? null : LocalDateTime.parse(end)));
			} else if (readedLine.startsWith("08:")) {
				int splitPosition = readedLine.indexOf(';', 3);
				LocalDate date = LocalDate.parse(readedLine.substring(3, splitPosition));
				String poste = readedLine.substring(splitPosition + 1);
				physician.getWorkedVacs().put(date, poste);
			} else if (readedLine.startsWith("09:")) {
				physician.getRefusedPostes().add(readedLine.substring(3));
			} else if (readedLine.equals("99:END")) {
				return physician;
			}
		}

		return null;
	}
	
}
