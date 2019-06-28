package iaaMain;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.io.FileUtils;
import org.dkpro.statistics.agreement.InsufficientDataException;
import org.dkpro.statistics.agreement.coding.CodingAnnotationStudy;
import org.dkpro.statistics.agreement.coding.RandolphKappaAgreement;

import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;

public class IAAMain {
	
	

	public static Object[]subarray(Object[]input,int...indicies){
		ArrayList<Integer>selectedIndicies = new ArrayList<>();
		for (int integer : indicies) {
			if(!selectedIndicies.contains(integer))
				selectedIndicies.add(integer);	
		}
		Collections.sort(selectedIndicies);


		ArrayList<Object> output = new ArrayList<>();
		for (int i = 0; i < input.length; i++) {
			if(selectedIndicies.contains(i))
				output.add(input[i]);
		}
		return output.toArray();
	}

	public static void main(String[] args) throws IOException {
		TsvParserSettings settings = new TsvParserSettings();
		TsvParser parser = new TsvParser(settings);

		String inPath = "/home/s3035016/eclipse-workspace/VerbsAnnotator/statistics/iaa.tsv";
		List<String[]> allRows = parser.parseAll(new FileReader(inPath));
		Map<String, List<String[]>> verbrows = new HashMap<String, List<String[]>>();
		String[] header = allRows.get(0);
		ArrayList<String> annotator_names = new ArrayList<String>();
		for (int i = 3; i < header.length; i++) {
			annotator_names.add(header[i]);
		}
		allRows = allRows.subList(1, allRows.size());
		for (String[] row : allRows) {
			String verb = row[1];
			if (verbrows.containsKey(verb)) {
				List<String[]> rows = verbrows.get(verb);
				rows.add(row);
			} else {
				ArrayList<String[]> tmp = new ArrayList<String[]>();
				tmp.add(row);
				verbrows.put(verb, tmp);
			}
		}
		List<Integer> annotators = IntStream.range(0, annotator_names.size()).boxed().collect(Collectors.toList());
		int[] annotator_array = new int[annotator_names.size()];
		for (int i = 0; i < annotator_names.size(); i++) {
			annotator_array[i] = annotators.get(i);
		}
		
		double[][] confusion_matrix = iaamatrix(allRows, annotators);
		double overall_score = iaascore(allRows, annotator_array);
		
		List<String> scores_out = new ArrayList<String>();
		List<String> matrix_out = new ArrayList<String>();
		scores_out.add("Overall Score:\t" + overall_score);
		matrix_out.add("Overall Score:\t" + overall_score);
		String pretty_matrix = pretty_print_cm(confusion_matrix, annotator_names);
		matrix_out.add(pretty_matrix + System.lineSeparator());
		System.out.println("Overall score: " + overall_score);
		System.out.println(pretty_matrix);	
		
		
		ArrayList<String> low_n_verbs = new ArrayList<String>();
		Map<String, Double> verbscores = new HashMap<String, Double>();
		for (String verb : verbrows.keySet()) {
			System.out.println("\n" + verb + ":");
			if (verbrows.get(verb).size() < 1) {
				low_n_verbs.add(verb);
				continue;
			}
			
			int n_annotators = annotator_names.size();
			int[] annot_counts = new int[n_annotators];
			for (String[] strings : verbrows.get(verb)) {
				for (int i = 3; i < strings.length; i++) {
					if (!strings[i].equals("*")) {
						annot_counts[i-3] += 1;
					}
				}
			}
			
			List<Integer> filtered_annotators = new ArrayList<Integer>();
			for (int i = 0; i < n_annotators; i++) {
				if (!(annot_counts[i] < 1)) {
					filtered_annotators.add(i);
				}
			}
			
			n_annotators = filtered_annotators.size();
			int[] filtered_array = new int[n_annotators];
			for (int i = 0; i < n_annotators; i++) {
				filtered_array[i] = filtered_annotators.get(i);
			}
			
			overall_score = iaascore(verbrows.get(verb), filtered_array);
			verbscores.put(verb, overall_score);
			confusion_matrix = iaamatrix(verbrows.get(verb), filtered_annotators);
			
			matrix_out.add(verb + ":\t" + overall_score);
			pretty_matrix = pretty_print_cm(confusion_matrix, filtered_annotators.stream().map(x -> annotator_names.get(x)).collect(Collectors.toList()));
			matrix_out.add(pretty_matrix + System.lineSeparator());
			System.out.println("Overall score: " + overall_score);
			System.out.println(pretty_matrix);	
			
			
		}
		
		System.out.println(low_n_verbs);
		Map<String, Double> sortedByValue = verbscores.entrySet()

                .stream()

                .sorted(Map.Entry.comparingByValue())

                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		
		for (String verb: sortedByValue.keySet()) scores_out.add(verb + ":\t" + sortedByValue.get(verb));
		
		FileUtils.writeLines(new File("scores"), scores_out);
		FileUtils.writeLines(new File("matrices"), matrix_out);
	}
	
	public static double[][] iaamatrix(List<String[]> input, List<Integer> annotators) throws IOException {
		int n_annotators = annotators.size();
		
		//TODO: Fix alignment
		double[][] confusion_matrix = new double[n_annotators][n_annotators];
		for (int i = 0; i < n_annotators; i++) {
			for (int j = 0; j < n_annotators; j++) {
				if (i == j) {
					confusion_matrix[i][j] = 1;
					continue;
				}
				int[] selectedAnnotators = new int[]{annotators.get(i),annotators.get(j)};
				
				double score = iaascore(input, selectedAnnotators);
				confusion_matrix[i][j] = score;
			}
		}
		return confusion_matrix;
	}
	
	
	public static String pretty_print_cm(double[][] confusion_matrix, List<String> annotator_names) {
		int n_annotators = confusion_matrix.length;
		StringBuilder sb = new StringBuilder();
		sb.append("Confusion Matrix: ").append(System.lineSeparator());
		sb.append(String.format("%25s", ""));
		for (int entry = 0; entry < n_annotators; entry++) {
			sb.append(String.format("%25s", annotator_names.get(entry)));
		}
		sb.append(System.lineSeparator());
		for (int row = 0; row < n_annotators; row++) {
			for (int entry = 0; entry < n_annotators + 1; entry++) {
				if (entry == 0) {
					sb.append(String.format("%25s", annotator_names.get(row)));
				} else {
					sb.append(String.format("%25s", confusion_matrix[row][entry-1]));
				}
			}
			sb.append(System.lineSeparator());
		}
		return sb.toString();
	}
	
	public static double iaascore(List<String[]> input, int[] annotators) throws IOException {
		CodingAnnotationStudy study = new CodingAnnotationStudy(annotators.length);

		//TODO: Add categories

		//TODO: Load verbLemmaIds
		HashMap<String, List<String>> verbLemmaIds = new HashMap<String, List<String>>();
		List<String>lines = FileUtils.readLines(new File("/home/s3035016/eclipse-workspace/VerbsAnnotator/verbLemmaIds"), "UTF-8");
		for (String line: lines) {
			String[] line_split = line.split("\t");
			String verb = line_split[0];
			List<String> categories = new ArrayList<String>();
			for (int i = 1; i < line_split.length; i++) {
				categories.add(line_split[i]);
			}
			verbLemmaIds.put(verb, categories);
		}
		// Get all verbs in input
		HashSet<String> verblist = new HashSet<String>();
		for (String[] string : input) {
			verblist.add(string[1]);
		}
		
		// Pull all categories for relevant verbs
		// iterate over categories and add them to study:
		for (String verb : verblist) {
			if (! verbLemmaIds.keySet().contains(verb)) {
				//Throw an error of some kind
			} else {
				for (String category: verbLemmaIds.get(verb)) {
					study.addCategory(category);
				}
			}
		}
		for (String[] strings : input) {
			String[] result = Arrays.asList(strings).subList(3, strings.length).stream().map(x -> x.equals("*")?null:x).toArray(String[]::new);
			study.addItemAsArray(subarray(result, annotators));
		}
		double score;
		try {
			RandolphKappaAgreement rka = new RandolphKappaAgreement(study);
			score = rka.calculateAgreement();
		} catch (InsufficientDataException e) {
			score = -9999;
		}
		return score;
	}

}
