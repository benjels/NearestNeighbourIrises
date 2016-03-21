import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.opencsv.CSVReader;

/**
 * everything in here til structure emerges tbh
 * @author brownmax1
 *
 */
public class Main {public Main() {
	// TODO Auto-generated constructor stub
}
	//TODO: we will probably want to track the og 4 values as we go through because just outputting a map that is n->class for the test data is not going to let us see what irises ended up where. maybe n -> irisResult{vector, class}


	public static final String PATH_TO_TRAINING = "data/iris-training.txt"; //THIS IS THE STANDARD ORDERING OF TRAIN/TEST
	public static final String PATH_TO_TEST = "data/iris-test.txt";
//	public static final String PATH_TO_TRAINING = "data/iris-test.txt";		//THIS USES TEST AS TRAINING
//	public static final String PATH_TO_TEST = "data/iris-training.txt";


//TODO: care some divide by zero error when we are finding the closeness measure that can apparently happen



	public static final int k = 3;//TODO: the code down the bottom that deals with talleys only works with like a talley size of 3. also the logic is all hardcoded to be 3...

	public static void main(String[] args) throws IOException{
		System.out.println("starting...");///THREE EXTRA 0S IN CONSOLE BUFFER
		//load in the training data from the file
		ArrayList<Iris> trainingIrises = loadIrisDataFromFile(PATH_TO_TRAINING);
		//load in the test data from the file
		ArrayList<Iris> testIrises = loadIrisDataFromFile(PATH_TO_TEST);
		//find the ranges of the four measurements
		//double[] ranges = findRanges(trainingIrises, testIrises);
		//TODO: maybe this shouldnnt be hardcoded
		double[] ranges = findRange(trainingIrises);//{3.6, 2.4, 5.9, 2.4};
		//map the test irises to a class using the training irises
		HashMap<Iris, String> results = classify(k, ranges,  trainingIrises, testIrises);
		//print the results to console...
		printResults(results, testIrises.size());
	}

	/**
	 * takes a collection of irises and returns a 4 entried array with all of the measurement ranges
	 * @param trainingIrises a collection of irises
	 * @return the range
	 */
	private static double[] findRange(ArrayList<Iris> trainingIrises) {
		double[] max = {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
		double[] min = {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
		for(Iris eachIris: trainingIrises){
			double[] vector = eachIris.vector;
			for(int i = 0; i < 4; i++){
				//if we found a measurement that's smaller than one of our mins, replace it
				if(vector[i] < min[i]){
					min[i] = vector[i];
				}
				//if we found a measurement that's larger than one of our maxs, replace it
				if(vector[i] > max[i]){
					max[i] = vector[i];
				}
			}

		}
		//generate the range
		double[] range = new double[4];
		for(int i = 0; i < 4; i++){
			range[i] = max[i] - min[i];
		}

		System.out.println(range[0]);
		System.out.println(range[1]);
		System.out.println(range[2]);
		System.out.println(range[3]);
		return range;
	}


	public static void printResults(HashMap<Iris, String> results, int testSetSize){
		System.out.println("PRINTING RESULTS: \n\n\n");
		System.out.println("\nMAPPINGS:");
		for(Iris eachKey: results.keySet()){
			System.out.println("mapped a " + eachKey.className + " to the class : " + results.get(eachKey));
		}
		System.out.println("\nMISTAKES:"); //and count up stats as we go
		int mistakesCount = 0;
		for(Iris eachKey: results.keySet()){
			if(!results.get(eachKey).equals(eachKey.className)){
				System.out.println("mapped a " + eachKey.className + " to the class : " + results.get(eachKey));
				mistakesCount++;
			}
		}
		int successCount = testSetSize - mistakesCount;
		System.out.println("\nSTATS:");
		System.out.println("successfully mapped: " + successCount);
		System.out.println("mistakes: " + mistakesCount);
		System.out.println("success rate: " + ((double)successCount/testSetSize) + "%");
	}

//vvv this is the version where I am going to use a three indexed array for all of the neighbours

	private static HashMap<Iris, String> classify(int k, double[] ranges, ArrayList<Iris> trainingIrises,
			ArrayList<Iris> testIrises) {
		HashMap<Iris, String> results = new HashMap<>();


		for(Iris eachTestIris: testIrises){
			//System.out.println("about to find the neighbours for some test iris : " + eachTestIris.className);
			Iris[] neighboursIrises = new Iris[k];//neighbours is like a k cardinality map that can tolerate repetitions of keys. key, value, key, value etc.
			double[] neighboursWeights = new double[k];
			//populate the closest neighbours with filler values to start out
			//WE REMOVE THESE NEIGHBOURS FROM THE trainingIrises list as we go so that
			//we do not encounter them when we are traversing all of the training irises later one
			//(when we do that, we get the problem of generating exactly the same key/distance as we have generated before
			//which "overwrites an entry in the map. We were still taking one out, but
			//TODO: could just put three values in with double.maxValue as their weight so that they definitely get replaced <<this might not be a problem when we arent using the map
			for(int i = 0; i < k; i++){
				neighboursWeights[i] = findDistance(ranges, eachTestIris, trainingIrises.get(i));
				neighboursIrises[i] = trainingIrises.get(i);
			}
			assert( k == neighboursIrises.length && neighboursWeights.length == k):"all these are 3s";
			//now go through all of the irises in the training set and store the closest ones to this test iris as neighbours NOTE: if two training irises are equal distance from our test iris, the first one encountered is saved. This should probably be randomised because it favours the class that appears in the file first.
			for(Iris eachTrainingIris: trainingIrises){
				assert(  k == neighboursIrises.length && neighboursWeights.length == k):"all these are 3s";
				//calculate the distance from the training iris to the test iris
				double distance = findDistance(ranges, eachTestIris, eachTrainingIris);
				//check if the distance is less than the distance to any of the current neighbours
				for(int i = 0; i < k; i++){
					assert( k == neighboursIrises.length && neighboursWeights.length == k):"all these are 3s";
					if(distance < neighboursWeights[i]){
						//we found a closer neighbour than the one that is currently in the neighbours[i]
						neighboursWeights[i] = distance;
						neighboursIrises[i] = eachTrainingIris;
						//we don't want to replace all of the neighbours just because we found a really close one!
						break;
					}
				}
			}
			//System.out.println(" \n \n \n these are the neighbours for a " + eachTestIris.className + ": ");
			//System.out.println(eachTestIris.vector[0] + " " + eachTestIris.vector[1] + " " + eachTestIris.vector[2] + " " + eachTestIris.vector[3]);
			for(int i = 0; i < k; i++){
			//	System.out.println(neighboursWeights[i]);
			//	System.out.println(neighboursIrises[i].className);
			}
			//System.out.println("\n \n \n");
			//now we have all of the neighbours of this node, find the majority class
			//NOTE: there should be enums e.g. 1 -> Iris Setosa etc so that we can just do the tallies and then easily tell which is the majority
			int[] classTally = new int[3];
			for(int i = 0; i < k; i++){
				if(neighboursIrises[i].className.equals("Iris-setosa")){
					classTally[0] ++;
				}else if(neighboursIrises[i].className.equals("Iris-versicolor")){
					classTally[1] ++;
				}else if(neighboursIrises[i].className.equals("Iris-virginica")){
					classTally[2] ++;
				}else{
					assert(false): "has to be one of those three";
				}
			}
			//so we tallied the neighbours, map this test iris to the class that has the highest tally
			//NOTE: need to take care of the case at the top where they are all equal, in which case a random class should be chosen or something. Atm setosa gets advantage.
			if(classTally[0] >= classTally[1] && classTally[0] >= classTally[2]){
				results.put(eachTestIris, "Iris-setosa");
			}else if(classTally[1] >= classTally[0] && classTally[1] >= classTally[2]){
				results.put(eachTestIris, "Iris-versicolor");
			}else if(classTally[2] >= classTally[0] && classTally[2] >= classTally[1]){
				results.put(eachTestIris, "Iris-virginica");
			}else{
				assert(false): "has to be one of those";
			}

		}
		//now each of the test Irises is mapped to the class it seems to belong to
		return results;
	}





	/**
	 * finds kinda normalised euclidean distance between two irises
	 * @param ranges the absolute difference between the smallest and largest measurement in each of the 4 categories
	 * @param eachTestIris an iris to compare (5 value vector but we only look at first four)
	 * @param iris an iris to compare
	 * @returnthe normalised difference
	 */
	private static double findDistance(double[] ranges, Iris eachTestIris, Iris iris) {
		double sum = 0.0;
		for(int eachAttributeIndex = 0; eachAttributeIndex < ranges.length; eachAttributeIndex ++){
			double difference = Double.valueOf(eachTestIris.vector[eachAttributeIndex]) - Double.valueOf(iris.vector[eachAttributeIndex]);
			double differenceSquared = difference * difference;
			double range = ranges[eachAttributeIndex];
			double normalised = differenceSquared / (range * range);
			sum += normalised;
		}
		double result = Math.sqrt(sum);

		return result;
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
	 * creates a list of irises. Each iris is represented by a 5 indexed array
	 * @param path to the irises we want to load in
	 * @return the list of iris data
	 * @throws IOException if problem when reading lines from file
	 */
	public static ArrayList<Iris> loadIrisDataFromFile(String path) throws IOException{
		//read the training data into a list of arrays of strings.
		ArrayList<String> lines = (ArrayList<String>) Files.readAllLines(Paths.get(path), Charset.forName("utf-8"));
		ArrayList<Iris> irises = new ArrayList<>();
		for(String eachLine: lines){
			//there are some blank empty lines in the text files
			if(!eachLine.equals("")){
				//put the line into an array of strings
				String[] iris = eachLine.split("  ");//the values are separated by double spacing not tabs...
				//fill out the iris object and add it to the collection of irises
				irises.add(new Iris(Double.valueOf(iris[0]).doubleValue(), Double.valueOf(iris[1]).doubleValue(), Double.valueOf(iris[2]).doubleValue(), Double.valueOf(iris[3]).doubleValue(), iris[4]));
			}
		}
		return irises;
	}

}
