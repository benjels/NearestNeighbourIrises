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

	public static final String PATH_TO_TRAINING = "/home/brownmax1/assignment_programs/NearestNeighbourGIt/data/iris-training.txt";
	public static final String PATH_TO_TEST = "/home/brownmax1/assignment_programs/NearestNeighbourGIt/data/iris-test.txt";


	public static void main(String[] args) throws IOException{
		//create our training data
		HashMap<Double, String> training = generateNToClassMap(PATH_TO_TRAINING);
		//create our test data
		HashMap<Double, String> testResult = generateTestResults(PATH_TO_TEST, training);

	}

	private static HashMap<Double, String> generateTestResults(String pathToTest, HashMap<Double, String> training) throws IOException {


		return null;
	}

	/**
	 * takes the String path to a file of iris training data and creates the hashmap of n->class
	 * @throws IOException
	 */
	//TODO: should consider using a different data structure. We are going to be looking for keys that are SIMILAR to the n values we create in the test stage. Not exactly the same.
	public static HashMap<Double, String> generateNToClassMap(String path) throws IOException{
		ArrayList<String> lines = (ArrayList<String>) Files.readAllLines(Paths.get(path), Charset.forName("utf-8"));
		ArrayList<String[]> irises = new ArrayList<>();
		for(String eachLine: lines){
			irises.add(eachLine.split("  "));
		}
		for(String[] each: irises){
			for(String eachStr: each){
				System.out.println(eachStr);
			}
			System.out.println("======================");
			assert(each.length == 5):"wrong length " + each.length;
		}


probelm is that there are two trailing blank lines they should not be getting parses in. Let's just represent irises as arrays for now fuck oop
		return null;
	}





	/**
	 * takes an array of the iris data (4 decimal values) and generates the weight (n
	 */
	public static double generateWeight(){
		return 0.0;
	}



}
