package core;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Utilities {
	
	private Utilities(){
		throw new AssertionError();
	}
	
	public static boolean checkStringsAreEqual(String stringA, String stringB){
		if(stringA == null || stringB == null){
			if(stringA == stringB){
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
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();
	
	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
}
