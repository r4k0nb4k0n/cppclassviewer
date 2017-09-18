

public final class Keyword {
	
	final static String[] accessModifier = {"public", "private"};
	final static String[] dataType = {"void", "signed", "unsigned", "short", "int", "long", "float", "double", "bool", "char"};
	final static String[] controlKeyword = {"return", "if", "else", "for", "do", "while", "switch", "case", "continue", "break", "goto", "default"};
	
	public static boolean isAccessModifier(String input){
		for(String s : accessModifier){
			if(input.equals(s))
				return true;
		}
		return false;
	}
	
	public static boolean isDataType(String input){
		for(String s : dataType){
			if(input.equals(s))
				return true;
		}
		return false;
	}
}
