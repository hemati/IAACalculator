package iaaMain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.dkpro.statistics.agreement.coding.CodingAnnotationStudy;
import org.dkpro.statistics.agreement.coding.PercentageAgreement;
import org.dkpro.statistics.agreement.coding.RandolphKappaAgreement;
import org.dkpro.statistics.agreement.coding.WeightedAgreement;

import Display.Show;
import iaaExport.IAAExporter;
import iaaFactory.IAACalculatorFactory;
import iaaGenerator.IAACalculator;
import iaaGenerator.IAAExtractor;

public class IAAMain {

	public static void main(String[] args) throws IOException {

		System.out.println("-----------------important message------------------");
		System.out.println("Do not run this jar twice without");
		System.out.println("saving your first result, because the second");
		System.out.println("run will be appended to your first output files");
		System.out.println("-----------------------------------------------------");
		System.out.println(" ");
		System.out.println(" ");
		System.out.println("Please insert your path to your iaa tsv.");
		System.out.println("Important: the tsv file must have the following structure");
		System.out.println("satzID, verb, satz, annotator1, ......, AnotatorN");
		System.out.print("in: ");
		
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String inPath = reader.readLine();
        System.out.println("given input path: " + inPath);   
        
        System.out.println("please insert your output path");
		System.out.print("out: ");
        String out = reader.readLine();
        
        System.out.println("please insert for your desired calculation method.");
        System.out.println("Percentage, RandolphKappa, Bennetts");
 		
        System.out.print("Method: "); 
        String iaaMethod = reader.readLine();
        
        
        
		System.out.println("extractor start!-");
		IAAExtractor iaaEx = new IAAExtractor(inPath, 0);
		System.out.println("extractor end");
		
        
        /*
		System.out.println("----------------extractor start-------------------------");
		IAAExtractor iaaEx = new IAAExtractor("./TSVFiles/iaa.tsv", 0);
		System.out.println("------------------extractor end-------------------------");
		*/
		System.out.println("caluclator start");
		IAACalculator iaaCalc = new IAACalculator(iaaEx);
		System.out.println(" data for calculator factory ready");
		
		System.out.println("start calculator factory");
		IAACalculatorFactory iaaCalcFactory = new IAACalculatorFactory(iaaCalc, iaaEx, 
									iaaCalc.parseIAAMethod(iaaMethod));
		System.out.println("end calculator fatory");
		
		
		//String out = "./iaaOutput";
		//System.out.println("-------get lists---------");
		
		
		
		System.out.println("start exports");
		System.out.println("");
		System.out.println("export overall result");
		
		IAAExporter.exportDouble("overall iaa", iaaCalcFactory._overallMult , out,"Multiple_IAA_OverAll_Rows.txt");
		
		System.out.println("export #sentence / #verb correlation");
		
		IAAExporter.exportDouble("Correlation between #Sentence and #Verbs", 
				iaaCalcFactory._sentenceSenseCorr , out,"NumberSentences_And_#Verbs_CORR.txt");
		
		System.out.println("export iaa per verb / senses per verb correlation");
		
		IAAExporter.exportDouble("Correlation between iaaPV and SensesPV", 
				iaaCalcFactory._iaaVerbSenseCorr , out,"IAAPerVerb_And_SensesPerVerb_CORR.txt");
		
		System.out.println("");
		System.out.println("export overall pairs result set");
		
		IAAExporter.exportNestedList( iaaCalcFactory._overallPair, out, "Multiple_Overall_Sentences_Pairwise.txt");
		
		//Show.showList(iaaCalcFactory._overall);
		
		System.out.println("verbs");
		
		List<String[]> verbsMul = iaaCalcFactory._verbsMul;
		List<List<String[]>> verbsPair = iaaCalcFactory._verbsPair;
		
		System.out.println("");
		System.out.println("export overall result multiple verbs");
		
		IAAExporter.exportList(verbsMul ,out, "Multiple_Per_Verb.txt");
		
		System.out.println("");
		System.out.println("export result for every verb");
		
		IAAExporter.exportNestedList(verbsPair ,out, "Pairwise_For_Verbs.txt");
		
		System.out.println("");
		System.out.println("");
		System.out.println("verbsPerSentenc");
		
		List<String[]> sentencePV = iaaCalcFactory._sentencePV;
		List<String[]> sensesPV = iaaCalcFactory._sensesPV;
		
		
		System.out.println("");
		System.out.println("export verbs per sentence count");
		
		IAAExporter.exportList(sentencePV ,out, "SentencesPerVerb_Count.txt");
		
		System.out.println("");
		System.out.println("export senses per verb count");
		
		IAAExporter.exportList(sensesPV ,out, "SensesPerVerb_Count.txt");
		
		System.out.println("");
		
		
		System.out.println("");
		System.out.println("export senses per verb list");
		
		IAAExporter.exportStringNestedList(iaaEx._allSensesList ,out, "SenseNumbers_PerVerbList.txt");
		
		System.out.println("finished");
	}

}
