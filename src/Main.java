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

	//my machine hard coded paths
	//public static final String PATH_TO_TRAINING = "C:\\school_work\\2016tri1\\AI\\assign1\\part1NearestNeighbour\\NearestNeighbourGit\\data\\iris-training.txt";
	//public static final String PATH_TO_TEST = "C:\\school_work\\2016tri1\\AI\\assign1\\part1NearestNeighbour\\NearestNeighbourGit\\data\\iris-test.txt";

	public static final String PATH_TO_TRAINING = "data/iris-training.txt";
	public static final String PATH_TO_TEST = "data/iris-test.txt";
//TODO: care some divide by zero error when we are finding the closeness measure that can apparently happen



	public static final int k = 3;//TODO: the code down the bottom that deals with talleys only works with like a talley size of 3. also the logic is all hardcoded to be 3...

	public static void main(String[] args) throws IOException{
		System.out.println("starting...");///THREE EXTRA 0S IN CONSOLE BUFFER
		//load in the training data from the file
		ArrayList<String[]> trainingIrises = loadIrisDataFromFile(PATH_TO_TRAINING);
		//load in the test data from the file
		ArrayList<String[]> testIrises = loadIrisDataFromFile(PATH_TO_TEST);
		//find the ranges of the four measurements
		//double[] ranges = findRanges(trainingIrises, testIrises);
		//TODO: maybe this shouldnnt be hardcoded
		double[] ranges = {3.6, 2.4, 5.9, 2.4};
		//map the test irises to a class using the training irises
		HashMap<String[], String> results = classify(k, ranges,  trainingIrises, testIrises);


	}


//vvv this is the version where I am going to use a three indexed array for all of the neighbours

	private static HashMap<String[], String> classify(int k, double[] ranges, ArrayList<String[]> trainingIrises,
			ArrayList<String[]> testIrises) {
		HashMap<String[], String> results = new HashMap<>();


		for(String[] eachTestIris: testIrises){
			System.out.println("about to find the neighbours for some test iris : " + eachTestIris[4]);
			Iris[] neighbours = new Iris[k];
			//populate the closest neighbours with filler values to start out
			//WE REMOVE THESE NEIGHBOURS FROM THE trainingIrises list as we go so that
			//we do not encounter them when we are traversing all of the training irises later one
			//(when we do that, we get the problem of generating exactly the same key/distance as we have generated before
			//which "overwrites an entry in the map. We were still taking one out, but
			//TODO: could just put three values in with double.maxValue as their weight so that they definitely get replaced <<this might not be a problem when we arent using the map
			for(int i = 0; i < k; i++){
				neighbours.put(findDistance(ranges, eachTestIris, trainingIrises.get(i)), trainingIrises.get(i));
				neighbours[i][]
			}
			System.out.println(neighbours.size());
			assert(k == 3):"k should be 3 atm";
			assert(neighbours.size() == k):"there should be k neighbours:" + neighbours.size();
			//now go through all of the irises in the training set and store the closest ones to this test iris as neighbours NOTE: if two training irises are equal distance from our test iris, the first one encountered is saved. This should probably be randomised because it favours the class that appears in the file first.
			for(String[] eachTrainingIris: trainingIrises){
				assert(neighbours.size() == k):"there should only be k neighbours:" + neighbours.size();
				//calculate the distance from the training iris to the test iris
				double distance = findDistance(ranges, eachTestIris, eachTrainingIris);
				//check if the distance is less than the distance to any of the current neighbours
				Iterator<Map.Entry<Double, String[]>> iter = neighbours.entrySet().iterator();
				while(iter.hasNext()){
					Map.Entry<Double, String[]> eachNeighbour = iter.next();
					if(distance < eachNeighbour.getKey()){
						//we found a closer neighbour than one that is currently in the neighbours
						//we need to take the old one out, put the new one in, and then stop comparing to neighbours
						assert(neighbours.size() == k):"there should only be k neighbours:" + neighbours.size();
						//assert(!neighbours.keySet().contains(distance)):"noooo looks like we are trying to put something in the map that is already there :))):" + distance;
						neighbours.put(distance, eachTrainingIris);
						//ONLY remove the one we just iterated over in the case that when we inserted the new key, we didn't just overwrite an older entry
						//NOTE: THIS IS INCORRECT. e.g. if you had 20, 15, 25 in the neighbours and you encounter 20. It will just replace 20 instead of 25, so you don't have optimal configuration. TODO: MOST SRS TODO: you should not be using a map. It is allowed to have repeats. You should also not re-iterate over the first K training irises because then e.g. if the first iris in the training list was really close, we might get it once in the dummy arbitrary fill up period, and then again in the actual traversal period. wew.
						if(neighbours.size() == k + 1){
							iter.remove();
						}
						assert(neighbours.size() == k):"there should be k neighbours:" + neighbours.size();
						break;
					}
				}
			}
			System.out.println(" \n \n \n these are the neighbours neighbours for a " + eachTestIris[4] + ": ");
			System.out.println(eachTestIris[0] + " " + eachTestIris[1] + " " + eachTestIris[2] + " " + eachTestIris[3]);
			for(double eachNeighbour: neighbours.keySet()){
				System.out.println(eachNeighbour);
				System.out.println(neighbours.get(eachNeighbour)[4]);
			}
			System.out.println("\n \n \n");
			//now we have all of the neighbours of this node, find the majority class
			//NOTE: there should be enums e.g. 1 -> Iris Setosa etc so that we can just do the tallies and then easily tell which is the majority
			ArrayList<Integer> tally = new ArrayList<>(Arrays.asList(0, 0, 0));
			for(double eachNeighbourDist: neighbours.keySet()){
				assert(tally.size() == k);
				if(neighbours.get(eachNeighbourDist)[4].equals("Iris-setosa")){
					tally.set(0, tally.get(0) + 1);
				}else if(neighbours.get(eachNeighbourDist)[4].equals("Iris-versicolor")){
					tally.set(1, tally.get(1) + 1);
				}else if(neighbours.get(eachNeighbourDist)[4].equals("Iris-virginica")){
					tally.set(2, tally.get(2) + 1);
				}else{
					assert(false): "has to be one of those three" + neighbours.get(eachNeighbourDist)[4];
				}
			}
			//so we tallied the neighbours, map this test iris to the class that has the highest tally
			//NOTE: need to take care of the case at the top where they are all equal, in which case a random class should be chosen or something. Atm setosa gets advantage.
			if(tally.get(0) >= tally.get(1) && tally.get(0) >= tally.get(2)){
				results.put(eachTestIris, "Iris-setosa");
			}else if(tally.get(1) >= tally.get(0) && tally.get(1) >= tally.get(2)){
				results.put(eachTestIris, "Iris-versicolor");
			}else if(tally.get(2) >= tally.get(0) && tally.get(2) >= tally.get(1)){
				results.put(eachTestIris, "Iris-virginica");
			}else{
				assert(false): "has to be one of those";
			}
		}

		return results;
	}












/*	private static HashMap<String[], String> classify(int k, double[] ranges, ArrayList<String[]> trainingIrises,
			ArrayList<String[]> testIrises) {
		HashMap<String[], String> results = new HashMap<>();


		for(String[] eachTestIris: testIrises){
			System.out.println("about to find the neighbours for some test iris : " + eachTestIris[4]);
			ConcurrentHashMap<Double, String[]> neighbours = new ConcurrentHashMap<>();
			System.out.println(neighbours.size());
			//populate the closest neighbours with filler values to start out
			//WE REMOVE THESE NEIGHBOURS FROM THE trainingIrises list as we go so that
			//we do not encounter them when we are traversing all of the training irises later one
			//(when we do that, we get the problem of generating exactly the same key/distance as we have generated before
			//which "overwrites an entry in the map. We were still taking one out, but
			//TODO: could just put three values in with double.maxValue as their weight so that they definitely get replaced
			for(int i = 0; i < k; i++){
				neighbours.put(findDistance(ranges, eachTestIris, trainingIrises.get(i)), trainingIrises.get(i));
			}
			System.out.println(neighbours.size());
			assert(k == 3):"k should be 3 atm";
			assert(neighbours.size() == k):"there should be k neighbours:" + neighbours.size();
			//now go through all of the irises in the training set and store the closest ones to this test iris as neighbours NOTE: if two training irises are equal distance from our test iris, the first one encountered is saved. This should probably be randomised because it favours the class that appears in the file first.
			for(String[] eachTrainingIris: trainingIrises){
				assert(neighbours.size() == k):"there should only be k neighbours:" + neighbours.size();
				//calculate the distance from the training iris to the test iris
				double distance = findDistance(ranges, eachTestIris, eachTrainingIris);
				//check if the distance is less than the distance to any of the current neighbours
				Iterator<Map.Entry<Double, String[]>> iter = neighbours.entrySet().iterator();
				while(iter.hasNext()){
					Map.Entry<Double, String[]> eachNeighbour = iter.next();
					if(distance < eachNeighbour.getKey()){
						//we found a closer neighbour than one that is currently in the neighbours
						//we need to take the old one out, put the new one in, and then stop comparing to neighbours
						assert(neighbours.size() == k):"there should only be k neighbours:" + neighbours.size();
						//assert(!neighbours.keySet().contains(distance)):"noooo looks like we are trying to put something in the map that is already there :))):" + distance;
						neighbours.put(distance, eachTrainingIris);
						//ONLY remove the one we just iterated over in the case that when we inserted the new key, we didn't just overwrite an older entry
						//NOTE: THIS IS INCORRECT. e.g. if you had 20, 15, 25 in the neighbours and you encounter 20. It will just replace 20 instead of 25, so you don't have optimal configuration. TODO: MOST SRS TODO: you should not be using a map. It is allowed to have repeats. You should also not re-iterate over the first K training irises because then e.g. if the first iris in the training list was really close, we might get it once in the dummy arbitrary fill up period, and then again in the actual traversal period. wew.
						if(neighbours.size() == k + 1){
							iter.remove();
						}
						assert(neighbours.size() == k):"there should be k neighbours:" + neighbours.size();
						break;
					}
				}
			}
			System.out.println(" \n \n \n these are the neighbours neighbours for a " + eachTestIris[4] + ": ");
			System.out.println(eachTestIris[0] + " " + eachTestIris[1] + " " + eachTestIris[2] + " " + eachTestIris[3]);
			for(double eachNeighbour: neighbours.keySet()){
				System.out.println(eachNeighbour);
				System.out.println(neighbours.get(eachNeighbour)[4]);
			}
			System.out.println("\n \n \n");
			//now we have all of the neighbours of this node, find the majority class
			//NOTE: there should be enums e.g. 1 -> Iris Setosa etc so that we can just do the tallies and then easily tell which is the majority
			ArrayList<Integer> tally = new ArrayList<>(Arrays.asList(0, 0, 0));
			for(double eachNeighbourDist: neighbours.keySet()){
				assert(tally.size() == k);
				if(neighbours.get(eachNeighbourDist)[4].equals("Iris-setosa")){
					tally.set(0, tally.get(0) + 1);
				}else if(neighbours.get(eachNeighbourDist)[4].equals("Iris-versicolor")){
					tally.set(1, tally.get(1) + 1);
				}else if(neighbours.get(eachNeighbourDist)[4].equals("Iris-virginica")){
					tally.set(2, tally.get(2) + 1);
				}else{
					assert(false): "has to be one of those three" + neighbours.get(eachNeighbourDist)[4];
				}
			}
			//so we tallied the neighbours, map this test iris to the class that has the highest tally
			//NOTE: need to take care of the case at the top where they are all equal, in which case a random class should be chosen or something. Atm setosa gets advantage.
			if(tally.get(0) >= tally.get(1) && tally.get(0) >= tally.get(2)){
				results.put(eachTestIris, "Iris-setosa");
			}else if(tally.get(1) >= tally.get(0) && tally.get(1) >= tally.get(2)){
				results.put(eachTestIris, "Iris-versicolor");
			}else if(tally.get(2) >= tally.get(0) && tally.get(2) >= tally.get(1)){
				results.put(eachTestIris, "Iris-virginica");
			}else{
				assert(false): "has to be one of those";
			}
		}

		return results;
	}
*/




	/**
	 * finds kinda normalised euclidean distance between two irises
	 * @param ranges the absolute difference between the smallest and largest measurement in each of the 4 categories
	 * @param eachTestIris an iris to compare (5 value vector but we only look at first four)
	 * @param eachTrainingIris an iris to compare
	 * @returnthe normalised difference
	 */
	private static double findDistance(double[] ranges, String[] eachTestIris, String[] eachTrainingIris) {
		double sum = 0.0;
		for(int eachAttributeIndex = 0; eachAttributeIndex < ranges.length; eachAttributeIndex ++){
			double difference = Double.valueOf(eachTestIris[eachAttributeIndex]) - Double.valueOf(eachTrainingIris[eachAttributeIndex]);
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
