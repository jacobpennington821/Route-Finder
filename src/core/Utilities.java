package core;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A class for utility functions.
 * @author Jacob Pennington
 *
 */
public final class Utilities { // A class for utility functions
	
	private Utilities(){
		throw new AssertionError(); // Stops the class being initialised to an object
	}
	/**
	 * Compares two strings and returns whether they are equal.
	 * @param stringA - The first string to be compared.
	 * @param stringB - The second string to be compared.
	 * @return Whether the strings are equal.
	 */
	public static boolean checkStringsAreEqual(String stringA, String stringB){
		if(stringA == null || stringB == null){ // If either string is null
			if(stringA == stringB){ // Can't use .equals()
				return true;
			} else {
				return false;
			}
		} else {
			if(stringA.equals(stringB)){
				return true;
			} else {
				return false;
			}
		}
	}
	
	/**
	 * Converts a number to a place.
	 * @param number - The number to convert.
	 * @return A string formatted as either 1st, 2nd, 3rd, nth.
	 */
	public static String formatNumberToPlace(int number){
		switch(number){
		case 1:
			return number + "st";
		case 2:
			return number + "nd";
		case 3:
			return number + "rd";
		default:
			return number + "th";
		}
	}
	
	/**
	 * Rounds a number to the given number of decimal places.
	 * @param value - The number to round.
	 * @param places - The number of decimal places to round it to.
	 * @return A double with the given number of decimal places.
	 */
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();
	
	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
}
