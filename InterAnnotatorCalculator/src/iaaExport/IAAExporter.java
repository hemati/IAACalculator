package iaaExport;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Display.Show;

public class IAAExporter {
	
	private static List<List<String>> stringArrayFusionNested(List<List<String[]>> stringArray){
		
		List<List<String>> fusedList = new ArrayList<List<String>>();
		
		for(int i = 0; i < stringArray.size(); i++) {
			List<String> tempList = new ArrayList<String>();
			for(int j = 0; j < stringArray.get(i).size(); j++) {
				String tmpString;
				tmpString = Arrays.deepToString(stringArray.get(i).get(j));
				tempList.add(tmpString);
			}
			tempList.add(" ");
			fusedList.add(tempList);
		}
		
		return fusedList;
	}
	
	private static List<String> stringArrayFusion(List<String[]> stringArray) {
		
		List<String> fusedList = new ArrayList<String>();
		
		for(int i = 0; i < stringArray.size(); i++) {
			String temp = Arrays.deepToString(stringArray.get(i));
			fusedList.add(temp);
		}
		fusedList.add(" ");
		return fusedList;
	}
	
	/**
	 * creates a file with a given name at the given paths last folder
	 * @param lines all lines that should be added
	 * @throws IOException 
	 */
	private static void writer(List<String> lines, String outputFilePath, String fileName) throws IOException {

		Files.write(Paths.get(outputFilePath, fileName),
                lines,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
	}
	
	/**
	 * Exports a List of Lists which hold Strings
	 * @param strList the nested List with Strings
	 * @param outputFilePath the output path
	 * @param fileName name of your file
	 * @throws IOException
	 */
	public static void exportStringNestedList(List<List<String>> strList, 
			String outputFilePath, String fileName) throws IOException {
		
		for(int i = 0; i < strList.size(); i++) {
			writer(strList.get(i), outputFilePath, fileName);
		}
	}
	
	/**
	 * Exports a double value with a information sentence(max 20chars) to a file at the given path that 
	 * is already existent or it creates the file at the given path.
	 * 
	 * @param information
	 * @param d
	 * @throws IOException
	 */
	public static void exportDouble(String information, Double d, 
							String outputFilePath, String fileName) throws IOException {

		String tempString = information;
		if(information.length() > 40) {
			tempString = information.substring(0, 40);
		}
		String tempLine = Double.toString(d);
		List<String> lines = new ArrayList<String>();
		lines.add(tempString);
		lines.add(tempLine);
		
		writer(lines, outputFilePath, fileName);
		
	}
	
	/**
	 * Exports a List to a file at the given path that is already existent or it creates the file 
	 * at the given path
	 * @param toExport
	 * @param fileName
	 * @throws IOException
	 */
	public static void exportList(List<String[]> toExport,String outputFilePath, String fileName) throws IOException {
		List<String> tmpList = stringArrayFusion(toExport);
		writer(tmpList,outputFilePath, fileName);
	}
	
	/**
	 * Exports a nested List to a file at the given path that is already existent or it creates the file 
	 * at the given path
	 * @param toExport the nested lists that should be exported
	 * @param fileName name for your file
	 * @throws IOException 
	 */
	public static void exportNestedList(List<List<String[]>> toExport, String outputFilePath,String fileName) throws IOException {
		List<List<String>> tmpList = stringArrayFusionNested(toExport);
		
		for(int i = 0; i < tmpList.size(); i++) {
			writer(tmpList.get(i), outputFilePath, fileName);
		}
	}
	
	
}
