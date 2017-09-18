import java.util.*;

import javax.swing.JTextArea;

public class Method {

	String name;
	String access;
	String type;
	String className;
	String source = null;
	JTextArea srcArea = new JTextArea();
	boolean isStructor;
	ArrayList usingFieldList = new ArrayList<String>();
	
	// 생성자
	public Method(String n,String a,String t,String cN/*,String mS*/){
		this.name = n;
		this.access = a;
		this.type = t;
		this.className = cN;
		//this.source = mS;
	}
	
	// 메소드
	public String toString(){
		if(this.name.contains(":")){ // 생성자 또는 소멸자라면
			String[] token = this.name.split("[:()]+");
			return token[1]+"()";
		}
		else return this.name;
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
	public String getSource(){
		return this.source;
	}
	public JTextArea getSrcArea(){
		return this.srcArea;
	}
	public String getClassName(){
		return this.className;
	}
	public ArrayList getUsingFieldList(){
		return this.usingFieldList;
	}
	public void setSource(String str){
		this.source = str;
	}

}
