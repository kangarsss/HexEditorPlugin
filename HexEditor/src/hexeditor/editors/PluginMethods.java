package hexeditor.editors;

public class PluginMethods {
	String beforeText;
	
	public PluginMethods(){
		
	}

	
	/**
	 * Generates offset from given number
	 * 
	 * @param num - number
	 * @return
	 */
	public String generateOffset(int num) {
		String begin = "000000";

		for (int i = 0; i <= num; i++)
			if (i == 16 || i == 256 || i == 4096 || i == 65536 || i == 1048576
					|| i == 16777216)
				begin = begin.substring(0, begin.length() - 1);

		begin += Integer.toHexString(num) + "0";

		return begin;
	}
	
	/**
	 * Transforms table text from ASCII to hexadecimal values
	 * @param hexText - text to transformed
	 * @return
	 */
	public String getFontString(String hexText) {
		String temp = "";

		for (int i = 0; i < hexText.length(); i++) {
			char c = hexText.charAt(i);
			String s = "\\u" + Integer.toHexString((int) c + 57360);
			char a = (char) Integer.parseInt(s.substring(2), 16);
			temp += String.valueOf(a);
		}

		return temp;
	}
	
	/**
	 * Transforms editors text from ASCII to hexadecimal values
	 * @param tableText - text to transform
	 * @return
	 */
	public String getTableString(String tableText) {
		if (tableText.matches("[0-9A-Fa-f]{2}")) {
			String temp1 = "\\u00" + tableText;
			char temp2 = (char) Integer.parseInt(temp1.substring(2), 16);

			String s = "\\u" + Integer.toHexString((short) temp2 + 57360);
			char a = (char) Integer.parseInt(s.substring(2), 16);
			return String.valueOf(a);
		} else {
			return beforeText;
		}

	}

}
