package iaaGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;

import Display.Show;

public class IAAExtractor {

	private String _filePath;
	private TsvParser _parser;
	private List<String[]> _parsedTsvList = new ArrayList<String[]>();
	public  List<String[]> _parsedTsvNoHead = new ArrayList<String[]>();
	public  List<List<String>> _allSensesList = new ArrayList<List<String>>();
	public  List<List<String[]>> _allSensesSeparated = new ArrayList<List<String[]>>();
	public  List<List<String[]>> _parsedTsvNHSplit = new ArrayList<List<String[]>>();
	public  List<List<String[]>> _finalVerbLists = new ArrayList<List<String[]>>();

	private String[] _annotators;
	private int numAnnotators;
	
	/**
	 * Constructor for extracting raw annotation data from a tsv file.
	 * line separators(settings):
	 * 0 => \n
	 * 1 => \r
	 * 2 => \t
	 * 3 => \\
	 * @param filePath path to the tsv file
	 * @param setting that separate lines (e.g 0 for \t)
	 */
	public IAAExtractor(String filePath, int setting) 
	{

		System.out.println("init parser");
		//first step:initialize parse and get filePath
		_parser = new TsvParser(this.setParserSetting(setting));
		_filePath = filePath; //used for extracting and parsing
		

		System.out.println("Extract all annotators in the first column");
		//second step: extract information about annotator's 
		//and create annotators array [verb, annotator1, .., annotatorN]
		extractAnnotators();
		
		System.out.println("extract all verbs from the tsv file.");
		//third step:
		//extract information from the tsv file in raw order
		startNormalExtraction();
		startVerbExtraction();
		extractAllSenses2();
		
		//System.out.println("extract all senses");
		//fourth step:
		//extract and reorder verbs after senses
		//startSenseExtraction();	
	
	}
	
	/**
	 * parses the raw tsv file
	 * @param filePath
	 * @return
	 */
	public List<String[]> parseFile() {
		
		//allRows is the parsedRaw data
		File file = new File(_filePath);
		List<String[]> allRows = _parser.parseAll(file);
			
		for(int i = 0; i < allRows.size(); i++) {

			//check all words inside the array
			for(int j = 0; j < allRows.get(i).length; j++) {
				String star= "*";
				
				if(star.equals(allRows.get(i)[j])) {
					allRows.get(i)[j] = null;
				}
			}
		}
		return allRows;
	}
	
	/**
	 * Parse an tsv file with a specific line separator
	 * line sepperators(Settings):
	 * 0 => \n
	 * 1 => \r
	 * 2 => \t
	 * 3 => \\
	 * @param setting an integer number representing your line seperator
	 */
	public TsvParserSettings setParserSetting(int setting) {
		TsvParserSettings settings = new TsvParserSettings();
		
		switch(setting) {
		
		case 0:
			settings.getFormat().setLineSeparator("\n");
			break;
		case 1:
			settings.getFormat().setLineSeparator("\r");
			break;
		case 2:
			settings.getFormat().setLineSeparator("\t");
			break;
		default:
			settings.getFormat().setLineSeparator(" \\ ");
			break;
		}
		return settings;
	}
	
	
	/**
	 * extracts all relevant information regarding annotators
	 * and fills the annotatorsArray that has following structure
	 * [verb, annotator1,....,AnnotatorN]
	 */
	public void extractAnnotators() {
		
		List<String[]> allRows = this.parseFile();
		
		//initialize annotator's array
		_annotators = new String[allRows.get(0).length-3];
		
		//insert the word "verb" at position 0
		_annotators[0] = allRows.get(0)[1];
		//position 1 and following contain the annotator names
		for(int i = 3; i < allRows.get(0).length-1; i++) {
			
			_annotators[i-2] = allRows.get(0)[i]; //add annotator 

		}
		//save how many annotator's are mentioned
		numAnnotators = _annotators.length-1;
	}
	
	
	/**
	 * ("normal", because i keeps the verb/satzID structure order untouched) 
	 * Extracts all informations given in your tsv file
	 *  => First it extracts the annotator's creating the annotator array
	 * 	   of form [verb, anootator i, ...., annotator n] n a natural number
	 *  => Followed by replacing all * with null (for DKPro structure) and
	 *     inserting the relevant informations for the Lists parsedTsvList and
	 *     parsedTsvNoHead.
	 *  
	 * @param filePath the path to your tsv file
	 * @return 
	 */
	public void startNormalExtraction() {

		List<String[]> allRows = this.parseFile();
		
		//remove the "*" and replace it with null
		//we start at row 0 until the end
		for(int i = 0; i < allRows.size(); i++) {
			
			String[] temp = new String[allRows.get(i).length-1];
			//fill the temporary array
			for(int j = 0; j < allRows.get(i).length-1; j++) {
				
				String compare = "*";
				
				if(compare.equals(allRows.get(i)[j])) {
					temp[j] = null;
				}else {
					temp[j] = allRows.get(i)[j];
				}
			}
			
			//calculation for the list without head (satzID, satz)
			//and cut of the last null part (reduce array by 4)
			int arrayLength = allRows.get(i).length-3;
			String[] temp2 = new String[arrayLength];
			String[] temp3 = new String[arrayLength];
			List<String[]> rowHolder  = new ArrayList<String[]>();
			
			//fill the temporary array with entries from string[]
			//starting at j = 3 cuts of the first 3 columns
			for(int j = 3; j <= arrayLength; j++) {
				String compare = "*";

				//first is "verb"
				temp2[0] = allRows.get(i)[1];
				
				if(compare.equals(allRows.get(i)[j])) {
					temp2[j-2] = null;
				}else {
					temp2[j-2] = allRows.get(i)[j];
				}
				temp3 = temp2;
				temp3[0] = allRows.get(i)[0]+ " " + allRows.get(i)[1]; 
			}
			
			rowHolder.add(temp3);
			_parsedTsvNHSplit.add(rowHolder);
			
			//add the array
			_parsedTsvList.add(temp);

			_parsedTsvNoHead.add(temp2);
			//System.out.println(" ");

			//showParsedTsv(1);
		}	
		//Show.showNestedList(_parsedTsvNHSplit);
		//Show.showList(_parsedTsvNoHead);
	}
	

	
	/**
	 * extracts all senses for every verb.
	 * Important that the inner List has at first position a string that contains
	 * the verb 
	 * @return List with Lists that hold Strings, where every string is a sense
	 */
	private void extractAllSenses2() {
		List<List<String[]>> parsedVerbsNoHead = this._finalVerbLists;
		
		//will hold all Senses
		List<List<String>> allSenses = new ArrayList<List<String>>();
		List<List<String[]>> allSensesSeparated = new ArrayList<List<String[]>>();
		
		//for all Lists in parsedVerbsNoHead
		for(int i = 0; i < parsedVerbsNoHead.size(); i++) {
			List<String> tempList = new ArrayList<String>();
			List<String[]> tempListSep = new ArrayList<String[]>();
			
			//take a List and for all Elements (here: String[])
			for(int j = 0; j < parsedVerbsNoHead.get(i).size(); j++)  {
				String tempSense;
				int countSenses = 0;
				//this for loop counts how many senses are inside the array
				for(int k = 0; k < parsedVerbsNoHead.get(i).get(j).length; k++) {
					//fill the array with senses 
					if(parsedVerbsNoHead.get(i).get(j)[k] != null) {
						//get the sense
						tempSense = parsedVerbsNoHead.get(i).get(j)[k].toString();
						//add the sense only if it isn't already inside the List!
						if(!tempList.contains(tempSense)) {
							countSenses++;
						}
					}
				}
				
				String[] tmp2Sense = new String[countSenses];
				int counterPos = 0;
				//for every element inside the String[]
				//save the verb at the beginning
				for(int k = 0; k < parsedVerbsNoHead.get(i).get(j).length; k++) {
					//fill the array with senses 
					if(parsedVerbsNoHead.get(i).get(j)[k] != null) {
						//get the sense
						tempSense = parsedVerbsNoHead.get(i).get(j)[k].toString();
						//add the sense only if it isn't already inside the List!
						if(!tempList.contains(tempSense)) {
							
							tmp2Sense[counterPos] = tempSense;
							
							tempListSep.add(tmp2Sense);
							
							tempList.add(tempSense);
							counterPos++;
						}
					}
				}
			}
			allSensesSeparated.add(tempListSep);
			allSenses.add(tempList);
			
		}
		_allSensesSeparated = allSensesSeparated;
		_allSensesList = allSenses;
	}
	
	
	/**
	 * Parses the parsedVerbsNoHead list for 
	 * every sense and creates a new List that holds
	 * the row as  String[] in which the sense was mentioned
	 * E.G
	 * 	Sense: 78741
	 *   sagen null 78741 null null null null null 78848
	 *   wissen null null null 78848 null 78741 null null null 
	 *   machen 78848 null null null 78741 78848 null null null 
	 *  Sense: 78848 
     *   sagen null 78741 null null null null null 78848 null 
     *   übrig#lassen 78848 null null 78848 78848 null null null null
	 * @return 
	 * 
	 */
	public List<List<String[]>> startSenseExtraction() {
		List<List<String[]>> parsedVerbsNoHead = this._finalVerbLists;
		
		//will hold all Senses grouped by their related verb
		List<List<String[]>> parsedSensesNoHead = new ArrayList<List<String[]>>();
		//will hold all Senses
		List<List<String>> allSenses = this._allSensesList;
		//just for debuggin 
		//int counter = 0;
		/*
		for(int i = 0; i < allSenses.size(); i++) {
			for(int j = 0; j <allSenses.get(i).size(); j++) {
				System.out.print(allSenses.get(i).get(j));
				System.out.print(" ");
			}
			System.out.println("");
		}
		*/
		//IMPORTANT: now we have to go through the allSenses list
		//to work for every sense! 
		//for every verb in all senses
		for(int z = 0; z < allSenses.size(); z++) {
			//List<String[]> tempList = new ArrayList<String[]>();
			
			//for every sense 
			for(int y = 1; y < allSenses.get(z).size(); y++) {
				List<String[]> tempList = new ArrayList<String[]>();
				//at the beginning mention the used Sense
				
				String[] temp2 = new String[10];
				temp2[0] = "Sense: " + allSenses.get(z).get(y);
				temp2[1] = null;
				temp2[2] = null;
				temp2[3] = null;
				temp2[4] = null;
				temp2[5] = null;
				temp2[6] = null;
				temp2[7] = null;
				temp2[8] = null;
				temp2[9] = null;
				tempList.add(temp2);
						
				//for every verb in parsedVerbsNoHead
				for(int i = 0; i < parsedVerbsNoHead.size(); i++) {
	
					//we look at the specific verb and we do smth for every element (here:String[])
					for(int j = 0; j < parsedVerbsNoHead.get(i).size(); j++) {
						
								
						//for every sense we have to check if that sense is inside the
						//row. If the sense is inside the row, we have to add it to our temp list
						//k = 1 because we don't look at the verb itself only at the numbers
						for(int k = 1; k < parsedVerbsNoHead.get(i).get(j).length; k++) {

							if(allSenses.get(z).get(y).equals(parsedVerbsNoHead.get(i).get(j)[k])) {
								tempList.add(parsedVerbsNoHead.get(i).get(j));
								break;
							}
						}
					}
				}
				parsedSensesNoHead.add(tempList);
			}
		}
		
		
		return parsedSensesNoHead;
	}
	
	
	
	
	/**
	 * Extracts all information for every verb
	 */
	public void startVerbExtraction() {
		List<List<String[]>> finalVerbLists = new ArrayList<List<String[]>>();
		
		
		List<String[]> allRows = parseFile();
		

		//get all used verbs
		List<String> allVerbs = extractAllVerbs();
		/*for testing
		List<String> allVerbsi = extractAllVerbs();
		for(int aa = 0; aa < allVerbsi.size(); aa++) {
			System.out.println(allVerbsi.get(aa));
		}
		*/
		/*for testing
		  List<String> allVerbs = new ArrayList<String>();
		  allVerbs.add("sagen");
		  allVerbs.add("machen");
		  allVerbs.add("wissen");
		  allVerbs.add("laufen");
		*/
		//for every row in the raw data
		//we don't want the first row
		
		//System.out.println(allVerbs.size());
		

		//for every verb in allVerbs create an annotator set 
		Iterator<String> verbs = allVerbs.iterator();
		while(verbs.hasNext()) {
			List<String[]> tempVerbList = new ArrayList<String[]>();
			String tempVerb = verbs.next();
			
			//System.out.println(tempVerb);
			//extract verbs
			for(int i = 1; i < allRows.size(); i++) {

				//only if the tempVerb and the verb inside the row are equals
				//the row will be added
				if(allRows.get(i)[1].equals(tempVerb)) {
					
					//System.out.println(allRows.get(i)[1]);
					
					int arrayLength = allRows.get(i).length-3;
					String[] temp = new String[arrayLength];
					
					temp[0] = allRows.get(i)[1];
					for(int j = 3; j <= arrayLength; j++) {

						temp[j-2] = allRows.get(i)[j];
					}
					
					tempVerbList.add(temp);
					
					//add new string
					/*
					for(int b = 0; b <temp.length; b++) {
						System.out.print(temp[b] + " ");
					}
					System.out.println( " ");
					*/
				}
				
			}
			
			//showVerbsParsed(tempVerbList);
			//now add the list for the specific verb to the main list that holds
			//all lists of lists(37 Lists)
			finalVerbLists.add(tempVerbList);
			//System.out.println("temp verb list Size: " + tempVerbList.size());
			
			//System.out.println(" ");
			//remove all temporary string[] added for the next iteration
		}
		//System.out.println("finalSize: " + finalVerbLists.size());
		_finalVerbLists = finalVerbLists;
	}
	

	private List<String> extractAllVerbs() {
		List<String[]> allRows = parseFile();
		//get all used verbs
		List<String> usedVerbs = new ArrayList<String>();
		
		for(int i = 1; i < allRows.size(); i++) {
			String compareVerb = allRows.get(i)[1];

			//if usedVerbs list doesn't contain the verb as String => add it
			if(!(usedVerbs.contains(compareVerb))) {
				usedVerbs.add(compareVerb);
			}
		}
		
		return usedVerbs;
	}
	

	public void showVerbsParsed(List<String[]> tempVerbList) {
		for(int g = 0; g < tempVerbList.size(); g++) {
			
			for(int b = 0; b <tempVerbList.get(g).length; b++) {
				System.out.print(tempVerbList.get(g)[b] + " ");
			}
			System.out.println(" ");
		}
		System.out.println("\n");
	}

	
	/**
	 * Get the data from the tsv file after parsing
	 * @return the parsed tsv file
	 */
	public List<String[]> getParsedTsv(){
		return _parsedTsvList;
	}
	
	/**
	 * The names representing the annotator's
	 * @return amount as integer number
	 */
	public String[] getAnnotatorArray() {
		return _annotators;
	}
	
	/**
	 * The path to the tsv file
	 * @return file path as string
	 */
	public String getFilePath() {
		return _filePath;
	}
	
	public int getNumberOfAnnotator() {
		return numAnnotators;
	}
}
