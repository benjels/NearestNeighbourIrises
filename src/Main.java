import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import com.opencsv.CSVReader;

/**
 * everything in here til structure emerges tbh
 * @author brownmax1
 *
 */
public class Main {

	//TODO: we will probably want to track the og 4 values as we go through because just outputting a map that is n->class for the test data is not going to let us see what irises ended up where. maybe n -> irisResult{vector, class}

	public static final String PATH_TO_TRAINING = "C:\\school_work\\2016tri1\\AI\\assign1\\part1NearestNeighbour\\NearestNeighbourGit\\data\\iris-training.txt";
	public static final String PATH_TO_TEST = "C:\\school_work\\2016tri1\\AI\\assign1\\part1NearestNeighbour\\NearestNeighbourGit\\data\\iris-test.txt";


	public static void main(String[] args) throws IOException{
		
		//create our training data
		ArrayList<String[]> trainingIrises = loadIrisDataFromFile(PATH_TO_TRAINING);
		HashMap<Float, String> training = generateWeightToClassMap(trainingIrises);
		for(Float eachWeight: training.keySet()){
			System.out.println(eachWeight);
			System.out.println(training.get(eachWeight));
		}
		
		//create our test data
		ArrayList<String[]> testingIrises = loadIrisDataFromFile(PATH_TO_TEST);
		HashMap<String[], String> testResult = generateTestResults(5, testingIrises, training);
		
		//print out our test result
		for(String[] eachIris: testResult.keySet()){
			System.out.println("this is the actual information for an iris: ");
			System.out.println(eachIris[0]);
			System.out.println(eachIris[1]);
			System.out.println(eachIris[2]);
			System.out.println(eachIris[3]);
			System.out.println(eachIris[4]);
			System.out.println("and this is the class the ai came up with: ");
			System.out.println(testResult.get(eachIris));
			System.out.println("\n ======================== \n");
		}
	}


	/**
	 * uses training data to map irises to the class they seems to belong to
	 * @param irises the irises we are mapping using our training data
	 * @param training the map we produced from our training that is weight->class
	 * @param k the amount of neighbours to compare each iris to
	 * @return iris -> class map (NOTE THAT THE iris COMPONENT IN THIS CASE INCLUDES THE /ACTUAL/ CLASS OF THE IRIS. THIS WILL ALLOW EASY INTUITIVE CHECKING OF HOW WELL MY CLASSIFIER DOES)
	 */
	//TODO: the part that looks for closest neighbour ONLY replaces the current neighbour if we find one less than. Not less than or equal to. So it favours the first one it encounters.
	private static HashMap<String[], String> generateTestResults(int k, ArrayList<String[]> irises, HashMap<Float, String> training){
		assert(k < training.keySet().size()):"k must be less than the amount of irises in the training map";
			
		HashMap<String[], String> irisToClass = new HashMap<>();
		
		for(String[] eachIris: irises){
			//find the weight for this test iris
			float[] irisMeasurements = {Float.valueOf(eachIris[0]), Float.valueOf(eachIris[1]), Float.valueOf(eachIris[2]), Float.valueOf(eachIris[3])};
			float irisWeight = generateWeight(irisMeasurements);
			//now we have the weight of this iris, try to find which of the training weights it is closest to
			float[] neighbours = new float[k];
			//populate the closest neighbours with filler values to start out with (just get the first values from the training map)
			for(int i = 0; i < k; i++){
				neighbours[i] = (float) training.keySet().toArray()[i]; //TODO: should optimise it a lil by only converting to array once
			}
			//now store the k closest weights in the neighbours array NOTE: they are not stored in any particular order. (actually they might get put in order because I'm always checking to replace the first index first, so it only won't go there if the first index is already bigger but others aren't.)
			for(float eachTrainingWeight: training.keySet()){
				for(int i = 0; i < k; i++){
					if(Math.abs(eachTrainingWeight - irisWeight) < neighbours[i]){
						//we found a closer neighbour, so add that neighbour to the neighbour array and stop comparing to other neighbours
						neighbours[i] = eachTrainingWeight;
						break;
					}
				}
			}
			//just a test that neighbours are validish
			for(int i = 0; i < k; i++){
				assert(training.keySet().contains(neighbours[i])):"the stored neighbours values must be keys in the training map";
			}
			//whichever class appears the most times in the neighbours is the class we will map this iris to
			//TODO: maybe make this class tally a map of className->tally. THIS IS REALLY BAD DESIGN NOT EASILY EXTENSIBLE FOR MORE CLASSES OF IRIS ETC
			int[] classTally = new int[3];//0 is Iris-setosa, 1 is Iris-versicolor, 2 is Iris-virginica
			for(int i = 0; i < k; i++){
				if(training.get(neighbours[i]).equals("Iris-setosa")){
					classTally[0]++;
				}else if(training.get(neighbours[i]).equals("Iris-versicolor")){
					classTally[1]++;
				}else{
					assert(training.get(neighbours[i]).equals("Iris-virginica"));
					classTally[2]++;
				}
			}
			//finally, map this iris to the class that it seems to belong to
			if(classTally[0] > classTally[1] && classTally[0] > classTally[2]){
				irisToClass.put(eachIris, "Iris-setosa");
			}else if(classTally[1] > classTally[0] && classTally[1] > classTally[2]){
				irisToClass.put(eachIris, "Iris-versicolor");
			}else{//TODO: when there is a three way tie, we will be putting the iris into this class. it should be random.
				irisToClass.put(eachIris, "Iris-virginica");
			}
			
		}
		
		
		return irisToClass;
	}
	
	
	
	
	
	
	/**
	 * takes a list of iris data and creates a trained map thing weight -> class
	 * @throws IOException
	 */
	//TODO: should consider using a different data structure. We are going to be looking for keys that are SIMILAR to the n values we create in the test stage. Not exactly the same.
	public static HashMap<Float, String> generateWeightToClassMap(ArrayList<String[]> irises) throws IOException{
		
		HashMap<Float, String> trainingMap = new HashMap<>();
		for(String[] eachIris: irises){
			float[] irisMeasurements = {Float.valueOf(eachIris[0]), Float.valueOf(eachIris[1]), Float.valueOf(eachIris[2]), Float.valueOf(eachIris[3])};
			float weight = generateWeight(irisMeasurements);
			trainingMap.put(weight, eachIris[4]);//TODO: should really try putting something in here other than just the class so that I can identify which source data got assigned which weight etc
		}
		return trainingMap;
	}




	

	
	/*	   1. sepal length in cm
	   2. sepal width in cm
	   3. petal length in cm
	   4. petal width in cm
	   5. class: 
	      -- Iris Setosa
	      -- Iris Versicolour
	      -- Iris Virginica*/

	/**
	 * takes an array of the iris data (4 decimal values) and generates the weight value which is what will be used to map to classes
	 * 
	 */
	public static float generateWeight(float[] irisMeasurements){
		assert(irisMeasurements.length == 4):"I can't generate a weight with an array the wrong size";
		return irisMeasurements[0] + irisMeasurements[1] + irisMeasurements[2] + irisMeasurements[3];
	}


	/**
	 * creates a list of irises. Each iris is represented by a 5 indexed array
	 * @param path to the irises we want to load in
	 * @return the list of iris data
	 * @throws IOException if problem when reading lines from file
	 */
	public static ArrayList<String[]> loadIrisDataFromFile(String path) throws IOException{
		//read the training data into a list of arrays of strings.
		ArrayList<String> lines = (ArrayList<String>) Files.readAllLines(Paths.get(path), Charset.forName("utf-8"));
		ArrayList<String[]> irises = new ArrayList<>();
		for(String eachLine: lines){
			//there are some blank empty lines in the text files
			if(!eachLine.equals("")){
				irises.add(eachLine.split("  "));//the values are separated by double spacing not tabs...
			}
		}
		return irises;
	}

}
