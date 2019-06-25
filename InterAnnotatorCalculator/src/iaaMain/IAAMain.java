package iaaMain;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

		String inPath = "/home/staff_homes/ahemati/projects/VerbsAnnotator/statistics/iaa.tsv";
		for (int i = 0; i <= 13; i++) {
			if(i == 7)
				continue;
			System.out.println("annotator:" +i);
			int[]selectedAnnotators = new int[]{7,i};
			List<String[]> allRows = parser.parseAll(new FileReader(inPath));
			allRows = allRows.subList(1, allRows.size());
			CodingAnnotationStudy study = new CodingAnnotationStudy(selectedAnnotators.length);

			for (String[] strings : allRows) {
				String[] result = Arrays.asList(strings).subList(3, strings.length).stream().map(x -> x.equals("*")?null:x).toArray(String[]::new);
				study.addItemAsArray(subarray(result, selectedAnnotators));
			}
			RandolphKappaAgreement  rka = new RandolphKappaAgreement (study);
			System.out.println(rka.calculateAgreement());	
		}
	}

}
