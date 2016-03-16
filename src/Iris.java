/**
 * absurdly making a separate class for this because otherwise need 2d array for the neighbours set and we do not like
 * just a plain old java object
 * @author brownmax1
 *
 */



	/*	   1. sepal length in cm
	   2. sepal width in cm
	   3. petal length in cm
	   4. petal width in cm
	   5. class:
	      -- Iris Setosa
	      -- Iris Versicolour
	      -- Iris Virginica*/

public class Iris {

public final double  sepalLength;
public final double sepalWidth;
public final double petalLength;
public final double petalWidth;
public final String className;


public Iris(double sepalLength, double sepalWidth, double petalLength, double petalWidth, String className) {
	this.sepalLength = sepalLength;
	this.sepalWidth = sepalWidth;
	this.petalLength = petalLength;
	this.petalWidth = petalWidth;
	this.className = className;
}



}
