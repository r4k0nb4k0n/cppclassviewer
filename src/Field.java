import java.util.*;

public class Field {

	private String name;
	private String access;
	private String type;
	ArrayList usingMethodList = new ArrayList<String>();
	
	// 생성자
	public Field(String n,String a,String t){
		this.name = n;
		this.access = a;
		this.type = t;
	}
	
	// 메소드
	public String toString(){
		return this.name + ": " + this.type;
	}
	
	public String getName(){
		return this.name;
	}
	public String getAccess(){
		return this.access;
	}
	public String getType(){
		return this.type;
	}
	public ArrayList getUsingMethodList(){
		return this.usingMethodList;
	}
}
