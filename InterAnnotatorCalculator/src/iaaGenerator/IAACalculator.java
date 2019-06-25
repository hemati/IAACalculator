package iaaGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.dkpro.statistics.agreement.coding.BennettSAgreement;
import org.dkpro.statistics.agreement.coding.CodingAnnotationStudy;
import org.dkpro.statistics.agreement.coding.CohenKappaAgreement;
import org.dkpro.statistics.agreement.coding.FleissKappaAgreement;
import org.dkpro.statistics.agreement.coding.HubertKappaAgreement;
import org.dkpro.statistics.agreement.coding.KrippendorffAlphaAgreement;
import org.dkpro.statistics.agreement.coding.PercentageAgreement;
import org.dkpro.statistics.agreement.coding.RandolphKappaAgreement;
import org.dkpro.statistics.agreement.coding.ScottPiAgreement;
import org.dkpro.statistics.agreement.coding.WeightedKappaAgreement;

import Display.Show;

public class IAACalculator {

	public static enum IAAMethods{
							 Percentage,
							 Bennetts,
							 RandolphKappa,
							 FleissKappa};
	IAAExtractor _extractor;


	/**
	 * Constructor that initializes the calculator with an extractor
	 * to deliver methods to calculate the IAA for a set(two or more) of annotator's.
	 * 
	 * @param extractor
	 */
	public IAACalculator(IAAExtractor extractor) {
		_extractor = extractor;
		
		
	}
	
	
	public IAACalculator.IAAMethods parseIAAMethod(String iaaMethod) {
		
		if(iaaMethod.equals("Percentage")) {
			return IAACalculator.IAAMethods.Percentage;
		}else if(iaaMethod.equals("RandolphKappa")) {
			return IAACalculator.IAAMethods.RandolphKappa;
		}else if(iaaMethod.equals("Bennetts")) { 
			return IAACalculator.IAAMethods.Bennetts;
		}
		return null;
	}
	
	/**
	 * Set up a study with two or more annotator's for
	 * calculationg the IAA using the given data from the extractor
	 */
	public Double multipleAnnotatorsStudy(IAACalculator.IAAMethods iaaMethod, 
												List<String[]> parsedNoHead, boolean isSenses) {
		
		//initialize a study that must be filled with values 
		//the number of anootators is the annotatorAray - 1(first on is "verb")
		CodingAnnotationStudy study = 
					new CodingAnnotationStudy(_extractor.getNumberOfAnnotator());
		//add categories
		study = addCategoriesToStudy(study);
		//get the parsed tsv file without head(satzID, satz)
		List<String[]> listNoHead = parsedNoHead;
		
		boolean lIsSenses = isSenses;
		
		//every row as object, because the study uses objects as parameter
		for(int i = 0; i < listNoHead.size(); i++) {
			
			if(lIsSenses) {
				i = 1;
			}
			
			//we don't need the length with verb. Therefore we decrease the length by 1
			String[] temp = new String[listNoHead.get(i).length-1];
			
			for(int j = 1; j < listNoHead.get(i).length; j++) {
				
				temp[j-1] = listNoHead.get(i)[j]; //e.g temp[0] now contains annotato's A value
			}
			System.out.println(Arrays.toString(temp));
			//convert temp string[] to obj for DRPro addItem function
			Object[] obj =  temp;
			//System.out.println(" ");
			//add the row. Every column represents an annotator
			study.addItemAsArray(obj);
		}
		
		double cal = useIAAAlgorithm(study, iaaMethod, _extractor.getNumberOfAnnotator());
		return cal; //returns the overall IAAScore 
	}

	
	public List<String[]> calcSensesPerVerb() {
		List<List<String[]>> sensesPV = _extractor._allSensesSeparated;
		List<String[]> sensesVPCount = new ArrayList<String[]>();
		
		for(int i = 0; i < sensesPV.size(); i++){
			String[] tmp = new String[2];
			tmp[0] = sensesPV.get(i).get(0)[0];//add the verb
			tmp[1] = Integer.toString(sensesPV.get(i).size()-1); //size of array, -1 since first is the verb itself
			sensesVPCount.add(tmp);
		}
		return sensesVPCount;
	}
	
	public List<String[]> calcSentencePerVerb() {
		List<List<String[]>> verbPS = _extractor._finalVerbLists;
		List<String[]> verbsPSCount = new ArrayList<String[]>();
		
		for(int i = 0; i < verbPS.size(); i++){
			String[] tmp = new String[2];
			tmp[0] = verbPS.get(i).get(0)[0];//add the verb
			tmp[1] = Integer.toString(verbPS.get(i).size()); //size of array
			verbsPSCount.add(tmp);
		}
		return verbsPSCount;
	}
	
	/**
	 * Calculates the ratio between verbs per sentence and senses per Verb.
	 * Important: the List must have a the following structure:
	 * [name, value] 
	 * and both list must have the same length.
	 * 
	 * -hasHeader is the first row (pos 0) that contains the column definitions;
	 * 		
	 * @param sentencePV the calculated sentence per verb inside a List
	 * @param sensesPV the calculated senses per verb inside a List
	 * @param hasHeaderX true if x list has a header, false if not.
	 * @param hasHeaderY true if y list has a header, false if not.
	 * @return a List with ratios
	 */
	public Double calculateCorr(List<String[]> xVal, List<String[]> yVal, boolean xHasHeader, boolean yHasHeader){

		PearsonsCorrelation perCor = new  PearsonsCorrelation();
		double[] x = new double[xVal.size()];
		double[] y = new double[yVal.size()];
		
		if(xHasHeader == true && yHasHeader == false) {
			x = new double[xVal.size()-1];
			y = new double[yVal.size()];
			
		}else if(xHasHeader == false && yHasHeader == true) {
			x = new double[xVal.size()];
			y = new double[yVal.size()-1];
		}else {
			x = new double[xVal.size()];
			y = new double[yVal.size()];
		}

		//for every row get the value
		//if the first row is a header like [Calculation], skip it
		for(int i = 0; i < x.length; i++) {
			
			if(xVal.get(0).length == 1) {
				if(xVal.get(0).length == 1 && i == 0) {
					i = 1;
				}
				Double tmpD = Double.parseDouble(yVal.get(i)[1]);
				x[i-1] = tmpD;
			}else {
				//System.out.println(xVal.get(i)[1]);
				Double tmpD = Double.parseDouble(xVal.get(i)[1]);
				x[i] = tmpD;
			}
		}
		
		//same for y
		for(int j = 0; j < y.length; j++) {
			//System.out.println(j);
			if(yVal.get(0).length == 1) {
				if(yVal.get(0).length == 1 && j == 0) {
					j = 1;
				}
				Double tmpD = Double.parseDouble(yVal.get(j)[1]);
				y[j-1] = tmpD;
			}else {
				Double tmpD = Double.parseDouble(yVal.get(j)[1]);
				y[j] = tmpD;
			}
		}
		
		return perCor.correlation(x, y);
	}
	
	public List<String[]> correlation(){
		
		PearsonsCorrelation perCor = new  PearsonsCorrelation();
		
		return null;
	}
	
	
	public CodingAnnotationStudy addCategoriesToStudy(CodingAnnotationStudy study) {
		
		List<List<String>> tmp = _extractor._allSensesList;
		
		CodingAnnotationStudy tmpStudy = study;
		
		for(int i = 0; i < tmp.size(); i++) {
			for(int j = 1; j < tmp.get(i).size(); j++) {
				tmpStudy.addCategory(tmp.get(i).get(j));
			}
		}
		
		return tmpStudy;
	}
	
	/**
	 *Chooses a desired IAA method and calculates the agreement value.
	 * By Default it uses Percentage Agreement
	 * @param study the study that contains all rows from your tsv
	 * @param iaaMethod an enum that represents the name of the algorithm
	 * 				(e.g Percentage for Percentage agreement)
	 * @return the calculated IAA agreement value 
	 */
	private Double useIAAAlgorithm(CodingAnnotationStudy study, IAACalculator.IAAMethods iaaMethod, int numAnnotators) {
		
		Double iaaResult;
		switch(iaaMethod) 
		{
		
			case Percentage:
				PercentageAgreement pa = new PercentageAgreement(study);
				iaaResult = pa.calculateAgreement();
				break;
		
			case Bennetts:
				if(numAnnotators > 2) {
					System.out.println("Bennetts algorithm is not applicable for more than 2 annotators");
					iaaResult = -999.9;//very large negative to show an error
					break;
				}
				BennettSAgreement bs = new BennettSAgreement (study);
				iaaResult = bs.calculateAgreement();
				break;

			case RandolphKappa:	
				RandolphKappaAgreement  rka = new RandolphKappaAgreement (study);
				iaaResult = rka.calculateAgreement();
				break;

			default:
				PercentageAgreement paDefault = new PercentageAgreement(study);
				iaaResult = paDefault.calculateAgreement();
				break;
		}
		
		return iaaResult;
	}
	
	
	/**
	 * Compare multiple annotator's as pairs of two
	 */
	public List<String[]> pairASNEW1(IAACalculator.IAAMethods iaaMethod, 
								List<String[]> parsedNoHead, boolean isSenses) {
		//retrieve all annotators
		String[] annotatorsArray = _extractor.getAnnotatorArray();
		
		//save all IAA results inside a string array with form:
		//[verb, Annotator 1, Annotator 2, result]
		List<String[]> resultSet = new ArrayList<String[]>();

		
		//get the parsed tsv without head(satzID,verb,satz)
		List<String[]> listNoHead = parsedNoHead;		
		
		//Show.showList(listNoHead);
		
		/*for all permuations of annotators
		 * we calculate a study.
		 */
		//first annotator
		int verbPos = 0;
		
		//String[] result = new String[4];
		
		for(int i = 1; i < annotatorsArray.length; i++) {
			//annotator 
			//String[] result = new String[4];
			for(int j = 1; j < annotatorsArray.length; j++) {
				
				//initialize a study with 2 annotator's(a pair)
				CodingAnnotationStudy study = 
						new CodingAnnotationStudy(2);
			
				study = addCategoriesToStudy(study);
				
				
				String[] result = new String[4];
				result[1] = annotatorsArray[i];//add first annotator
				result[2] = annotatorsArray[j];//add (first) following annotators
				
				//for both go through the list without head
				//and create the rows a.k.a Items for the study
				for(int k = 1; k < listNoHead.size(); k++) {
					//we are looking for the information inside the column of
					//the annotator.
					
					if(isSenses) {
						result[0] = listNoHead.get(0)[0];//add verb 
					}else {
						result[0] = listNoHead.get(k)[verbPos];//add verb 
					}
					Object obj1 = listNoHead.get(k)[i]; 
					Object obj2 = listNoHead.get(k)[j];
					Object[] objArray = {obj1, obj2};
					//System.out.println( listNoHead.get(k)[0] + " " +  listNoHead.get(k)[1]);
					study.addItemAsArray(objArray);
				}
				//calculate the IAA with a IAA calculation Method
				Double cal = useIAAAlgorithm(study, iaaMethod, 2);
				result[3] = Double.toString(cal);
				//System.out.println(result[0] + " " + result[1] + " " + result[2] + " " + result[3]);
				
				resultSet.add(result);	
			}
		}

		//Show.showList(resultSet);	
		return resultSet;
		
	}
	
	/**
	 * Compare multiple annotator's as pairs of two
	 */
	public List<String[]> pairASNEW2(IAACalculator.IAAMethods iaaMethod, 
								List<String[]> parsedNoHead, boolean isSenses) {
		//retrieve all annotators
		String[] annotatorsArray = _extractor.getAnnotatorArray();
		
		//save all IAA results inside a string array with form:
		//[verb, Annotator 1, Annotator 2, result]
		List<String[]> resultSet = new ArrayList<String[]>();
		

		//get the parsed tsv without head(satzID,verb,satz)
		List<String[]> listNoHead = parsedNoHead;		
		
		int verbPos = 0;
		
		for(int i = 1; i < annotatorsArray.length; i++) {

			//initialize a study with 2 annotator's(a pair)

		
			//study = addCategoriesToStudy(study);
			
			//for every annotator j that will be compare to annotator i
			for(int j = 1; j < annotatorsArray.length; j++) {
				
				CodingAnnotationStudy study = 
						new CodingAnnotationStudy(2);
				
				study = addCategoriesToStudy(study);
				
				String[] result = new String[4];
				result[1] = annotatorsArray[i];//add first annotator
				result[2] = annotatorsArray[j];//add (first) following annotators
				
				//for both go through the list without head
				//and create the rows a.k.a Items for the study
				for(int k = 0; k < listNoHead.size(); k++) {
					//we are looking for the information inside the column of
					//the annotator.
					//System.out.println(annotatorsArray[1] + " " + annotatorsArray[j]);
					//System.out.println(listNoHead.get(k)[i+1] + " " + listNoHead.get(k)[j] + " ");
		
					if(isSenses) {
						result[0] = listNoHead.get(0)[0];//add verb 
					}else {
						result[0] = listNoHead.get(k)[verbPos];//add verb 
					}
					
					//result[0] = listNoHead.get(k)[verbPos];//add verb 
					Object obj1 = listNoHead.get(k)[i]; 
					Object obj2 = listNoHead.get(k)[j];
					Object[] objArray = {obj1, obj2};
					//System.out.println( listNoHead.get(k)[0] + " " +  listNoHead.get(k)[1]);
					study.addItemAsArray(objArray);
				}
				
				//calculate the IAA with a IAA calculation Method
				Double cal = useIAAAlgorithm(study, iaaMethod, 2);
				result[3] = Double.toString(cal);
				//System.out.println(result[0] + " " + result[1] + " " + result[2] + " " + result[3]);
				
				resultSet.add(result);	
			}
			
		}
	
		//Show.showList(resultSet);
		return resultSet;
	}
	
	
}
