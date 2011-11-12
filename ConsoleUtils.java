//intentionally un-packaged to facilitate testing

public class ConsoleUtils {
	public static String formatDollar(Double amount, int width) {
		if (amount < 0) {
			throw new IllegalArgumentException("ERROR - dollar value must be non-negative");
		}
		StringBuilder strAmount = new StringBuilder("$");
		strAmount.append(String.valueOf(amount));
		// add trailing 0
		if ((strAmount.length() - (strAmount.indexOf(".") + 1)) == 1) {
			strAmount.append("0");
		}
		return fixWidth(strAmount.toString(), width, Boolean.TRUE);
	}

	public static String fixWidth(final String display, int width, Boolean leftPad) {
    	StringBuilder fixedDisplay = new StringBuilder(display);
    	// if it's greater than the max, truncate
   		if (fixedDisplay.length() > width) {
   			fixedDisplay.setLength(width);
   		}

   		// fill with spaces if necessary
   		while (fixedDisplay.length() < width) {
   			if (leftPad == Boolean.TRUE) {
   				fixedDisplay.insert(0, " "); 
   			}
   			else {
   				fixedDisplay.append(" ");
   			}
   			
   		}
    	return fixedDisplay.toString();
	}
}
