package com.github.pjpo.planning.ui.controller.utils;

import java.io.IOException;
import java.io.Writer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;

import com.github.pjpo.planning.model.Position;
import com.github.pjpo.planning.problem.Solution;

/**
 * Data access object for solution
 * @author jp@dm.lan
 *
 */
public class DaoSolution {

	/** Writer used to write into */
	private final Writer writer;

	/**
	 * Constructor for using this writer
	 * @param writer
	 */
	public DaoSolution(final Writer writer) {
		this.writer = writer;
	}

	/**
	 * Stores this solution into the writer
	 * @param solution
	 * @throws IOException 
	 */
	public void store(final Solution solution) throws IOException {
		try (CSVWriter csvWriter = new CSVWriter(writer)) {
			 
			// List of positions names and dates
			final List<String> positionsNames = new ArrayList<>(solution.getPositions().columnKeySet());
			final List<LocalDate> positionDates = new ArrayList<>(solution.getPositions().rowKeySet());
			Collections.sort(positionsNames);
			Collections.sort(positionDates);

			// WRITE HEADERS
			final ArrayList<String> header = new ArrayList<>(positionsNames.size() + 1);
			header.add("");
			header.addAll(positionsNames);
			csvWriter.writeNext(header.stream().toArray(size-> new String[size]));
			
			// WRITE CONTENTS
			for (final LocalDate localDate : positionDates) {
				final ArrayList<String> content = new ArrayList<>(positionsNames.size() + 1);
				content.add(localDate.toString());
				for (final String positionName : positionsNames) {
					final Position position = solution.getPositions().get(localDate, positionName);
					content.add(position == null ? "" : position.getWorker().getName());
				}
				csvWriter.writeNext(content.toArray(new String[content.size()]));
			}
		}
	}
	
}
