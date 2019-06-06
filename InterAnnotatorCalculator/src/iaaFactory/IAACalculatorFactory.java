package iaaFactory;

import java.util.ArrayList;
import java.util.List;

import Display.Show;
import iaaGenerator.IAACalculator;
import iaaGenerator.IAAExtractor;

public class IAACalculatorFactory {
	
	public Double _overallMult; //iaa value comparing all annotators over all rows
	public Double _sentenceSenseCorr; // corr between #sentences and #senses
	public Double _iaaVerbSenseCorr; //corr iaa value per verb to senses per verb
	//overall iaa(multiple annot study) per pair
	public List<List<String[]>> _overallPair = new ArrayList<List<String[]>>();
	//iaa per pair per verb
	public List<List<String[]>> _verbsPair = new ArrayList<List<String[]>>(); 
	//all verbs multiple anno study
	public List<String[]> _verbsMul = new ArrayList<String[]>(); 
	
	//holds a count of senses per verb
	public List<String[]> _sensesPV = new ArrayList<String[]>();
	//holds a count of sentences per verb
	public List<String[]> _sentencePV = new ArrayList<String[]>();
	
		
	public IAACalculatorFactory(IAACalculator iaaCalc, IAAExtractor iaaExtr, 
											IAACalculator.IAAMethods iaaMethod){
		processData(iaaExtr, iaaCalc, iaaMethod);
	}
	
	/**
	 * Processes all data from the extractor and calculates 
	 * the IAA per verb, per sense for multiple annotator's and pairs.
	 * Furthermore, it calculates the amount of sentence per verb and 
	 * how many senses a verb has.
	 * @param extractor an IAAExtractor object that already extracted information
	 * @param iaaCalc provides functions for the factory
	 * @param iaaMethod
	 */
	public void processData(IAAExtractor extractor, IAACalculator iaaCalc, IAACalculator.IAAMethods iaaMethod) {
		
		System.out.println("calculating overall multiple annotators iaa");
		//calculates the iaa for all sentences for all annotators
		_overallMult = iaaCalc.multipleAnnotatorsStudy(iaaMethod,  extractor._parsedTsvNoHead, false);
			
		System.out.println("calculating iaa per verb for multiple annotators");
		//calculates the iaa per verb
		_verbsMul = this.multipleAnnoStudyFactory(iaaMethod, iaaCalc, extractor._finalVerbLists, false);
			
		System.out.println("calculating amount of senses per verb");
		//calculates the # sense per verb
		_sensesPV = iaaCalc.calcSensesPerVerb();
		
		System.out.println("calculating amount of sentences per verb");
		//calculates # sentences per verb
		_sentencePV = iaaCalc.calcSentencePerVerb();
		
		System.out.println("calculating corr between #sentencesPV and #sensesPV");
		//calculates #sentence per verb and #senses per verb 
		_sentenceSenseCorr = iaaCalc.calculateCorr(_sentencePV, _sensesPV, false, false);
		
		System.out.println("calculating corr between iaa per verb and #sensesPV");
		//calculates the corr between iaa per verb and #senses per verb
		_iaaVerbSenseCorr = iaaCalc.calculateCorr(_sensesPV, _verbsMul, false, true);
		
		System.out.println("calculating overall iaa for every row for pairs");
		//calculates the overall iaa for every row for every pair
		_overallPair = this.pairASF2(iaaMethod,  iaaCalc, extractor._parsedTsvNHSplit, false);
		
		//Show.showNestedList(extractor._finalVerbLists);
		System.out.println("calculating the iaa for every verb for every annotator");
		//calculates the iaa for every verb for every annotator(iaa per pair)
		_verbsPair = this.pairASF(iaaMethod, iaaCalc, extractor._finalVerbLists, false);
	}
	
	/**
	 * Calculates the IAA for multiple annotator's for multiple studies
	 * @param iaaMethod the calculation method
	 * @param parsedTsvNoHead a list of lists that contains String[] with the following structure
	 * 			[verb, iaaresult]
	 * @return a list with results after calculation
	 */
	public List<String[]> multipleAnnoStudyFactory(IAACalculator.IAAMethods iaaMethod, 
															IAACalculator iaaCalc,List<List<String[]>> parsedTsvNoHead, boolean isSenses) {

		List<List<String[]>> parsedList = parsedTsvNoHead;
		
		List<String[]> resultSet = new ArrayList<String[]>();
		
		String[] usedMethod = new String[1];
		usedMethod[0] = iaaMethod.toString();
		resultSet.add(usedMethod);
		
		
		for(int i = 0; i < parsedList.size(); i++) {
			String[] temp;
			
			temp = new String[2];
			try {
				double result = iaaCalc.multipleAnnotatorsStudy(iaaMethod, parsedList.get(i), isSenses);
				
				temp[0] = parsedList.get(i).get(0)[0]; //verb
				temp[1] = Double.toString(result);
				
				resultSet.add(temp);

			}catch(Exception e) {
				System.out.println("verb nr" + i +":>" + parsedList.get(i).get(0)[0] + "< not calculated");
				
				System.out.println(e);
			}
		}

		return resultSet;
	}
	
	/**
	 * ASF = (annotator study factory)
	 * Calculates a pair IAA score for the packedVerbs 
	 * @param iaaMethod a calculation method
	 * @return a List that contains Lists with calculated Results for a specific
	 * 			verb and for every annotator. E.g first list for verb "sagen" s
	 * 			that has the following structure [verb, Annotator 1, Annotator 2, iaascore]
	 */
	public List<List<String[]>> pairASF(IAACalculator.IAAMethods iaaMethod, 
										IAACalculator iaaCalc, List<List<String[]>> parsedTsvNoHead, boolean isSenses){
		
		List<List<String[]>> packedVerbs = parsedTsvNoHead;
		List<List<String[]>> resultSet = new ArrayList<List<String[]>>();
		
		
		for(int i = 0; i < packedVerbs.size(); i++) {
			try {
				List<String[]> result = iaaCalc.pairASNEW1(iaaMethod, packedVerbs.get(i), isSenses);
				
				String[] usedMethod = new String[1];
				usedMethod[0] = iaaMethod.toString();
				result.add(usedMethod);
				
				resultSet.add(result);

			}catch(Exception e) {
				System.out.println("verb nr" + i +":>" + packedVerbs.get(i).get(0)[0] + "< not calculated");
				System.out.println(e);
			}
		}		
		return resultSet;
	}
	
	/*
	 * ASF = (annotator study factory)
	 * Calculates a pair IAA score for the packedVerbs 
	 * but with the constrain that for every inner list the
	 * size is 1, which means the list holds only 1 String[].
	 * @param iaaMethod a calculation method
	 * @return a List that contains Lists with calculated Results for a specific
	 * 			verb and for every annotator. E.g first list for verb "sagen" s
	 * 			that has the following structure [verb, Annotator 1, Annotator 2, iaascore]
	 */
	public List<List<String[]>> pairASF2(IAACalculator.IAAMethods iaaMethod, 
										IAACalculator iaaCalc, List<List<String[]>> parsedTsvNoHead, boolean isSenses){
		
		List<List<String[]>> packedVerbs = parsedTsvNoHead;
		List<List<String[]>> resultSet = new ArrayList<List<String[]>>();
		
		
		for(int i = 0; i < packedVerbs.size(); i++) {
			try {
				List<String[]> result = iaaCalc.pairASNEW2(iaaMethod, packedVerbs.get(i), isSenses);
				
				String[] usedMethod = new String[1];
				usedMethod[0] = iaaMethod.toString();
				result.add(usedMethod);
				
				//Show.showList(result);
				resultSet.add(result);

			}catch(Exception e) {
				System.out.println("verb nr" + i +":>" + packedVerbs.get(i).get(0)[0] + "< not calculated");
				System.out.println(e);
			}
		}		
		
		//Show.showNestedList(resultSet);
		return resultSet;
	}
	
}
