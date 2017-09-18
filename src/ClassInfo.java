import java.util.*;

public class ClassInfo {	
	
	String name="";
	ArrayList methodList = new ArrayList<Method>();
	ArrayList fieldList = new ArrayList<Field>();
	
	// 생성자
	public ClassInfo(String n){
		this.name = n;
	}
	
	// 메소드
	public String toString(){
		return this.name;
	}
	
	public String getName(){
		return this.name;
	}
	public ArrayList getMethodList(){
		return methodList;
	}
	public ArrayList getFieldList(){
		return fieldList;
	}
	public void addField(Field input){
		fieldList.add(input);
	}
	public void addMethod(Method input){
		methodList.add(input);
	}

}
