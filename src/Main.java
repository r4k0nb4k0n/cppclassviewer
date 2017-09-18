import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.util.*;

public class Main {
	static ArrayList Classes = new ArrayList<ClassInfo>(); // 클래스 구현순서대로 저장되어 있는 ArrayList
	static ArrayList implementOrder = new ArrayList<Method>(); // 메소드 구현순서대로 저장되어 있는 ArrayList
	static String firstName; // 메소드 구현부 첫 줄 저장.
	
	// 메인 프레임
	static class MainFrame extends JFrame{ // GUI 및 IO 담당 클래스.
		private StringBuffer buffer = new StringBuffer();
		private JFileChooser fileChooser = new JFileChooser();
		private JTree tree = new JTree();
		private JPanel left = new JPanel();
		private JPanel right = new JPanel();
		private JPanel outer = new JPanel();
		private JTextArea sourceMethod = new JTextArea();
		private JTable tableClass = new JTable();
		private JTable tableField = new JTable();	
		private JTextArea listField = new JTextArea();
		private JDialog howtoDialog = new JDialog(this, "How To");
		private JDialog aboutDialog = new JDialog(this, "About");
			
		public MainFrame(){ // 생성자
			//프레임 기본설정
			setSize(800, 600);
			setTitle("C++ Class Viewer");
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			//스크린 정중앙에 실행되게끔 설정.
			Dimension frameSize = this.getSize(); // 프레임의 크기 구하기
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); // 실행되는 스크린의 크기 구하기
			/*
			 프레임의 위치를
			 (모니터화면 가로 - 프레임화면 가로) / 2
			 (모니터화면 가로 - 프레임화면 세로) / 2
			 */
			setLocation((screenSize.width - frameSize.width)/2, (screenSize.height - frameSize.height)/2);
			// 레이아웃 설정
			left.setLayout(new GridLayout(2, 1));
			JScrollPane treeScrollPane = new JScrollPane(tree);
			left.add(treeScrollPane);
			JScrollPane listScrollPane = new JScrollPane(listField);
			left.add(listScrollPane);
			
			JScrollPane srcScrollPane = new JScrollPane(sourceMethod);
			right.setLayout(new GridLayout(1, 1));
			right.add(srcScrollPane);
			outer.setLayout(new GridLayout(1, 2));
			outer.add(left);
			outer.add(right);
			add(outer);
			
			sourceMethod.append("sourceMethod");
			listField.append("listField");
			
			// 파일 선택기 // 기본디렉토리설정
			fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
			
			// 메뉴바
			// 생성후 프레임에 붙이기
			JMenuBar menubar = new JMenuBar();
			setJMenuBar(menubar);
			// 항목 만들기
			JMenu fileMenu = new JMenu("File");
			JMenu helpMenu = new JMenu("Help");
			menubar.add(fileMenu);
			menubar.add(helpMenu);
			// 항목 내부의 멤버를 만들어 넣기
			JMenuItem openAction = new JMenuItem("Open");
			JMenuItem saveAction = new JMenuItem("Save");
			JMenuItem exitAction = new JMenuItem("Exit");
			fileMenu.add(openAction);
			fileMenu.add(saveAction);
			fileMenu.add(exitAction);
			JMenuItem howtoAction = new JMenuItem("How To?");
			JMenuItem aboutAction = new JMenuItem("About");
			helpMenu.add(howtoAction);
			helpMenu.add(aboutAction);
			
			//openAction Listener 무명클래스
			openAction.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e){
					int result = fileChooser.showOpenDialog(MainFrame.this);
					int b = 0;
					FileInputStream file = null;
					if (result == JFileChooser.APPROVE_OPTION) {
						try{
							// 새로 열때 초기화.
							Classes.clear();
							implementOrder.clear(); // implementOrder.get(X); == implementOrder[X];
							firstName = null;
							buffer = new StringBuffer();
							file = new FileInputStream(fileChooser.getSelectedFile());	
							b = file.read();
							while(b!=-1){
								buffer.append((char)b);
								b = file.read();
							}
						} catch(FileNotFoundException ex){
							System.out.println("Oops : FileNotFoundException");
						} catch(IOException ex){
							System.out.println("Input error");
						}
    					// 파싱...
						parse(buffer);
						
						// 트리 업데이트...
						updateActionMethod();
					}
				}
			});
			
			//saveAction Listener 무명클래스
			saveAction.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					FileInputStream file = null;
					int b;
					int result = fileChooser.showSaveDialog(MainFrame.this);
					int j=0; // 토큰 인덱스
					int stack = 0; // 중괄호 스택 카운트.
					String totalString = ""; // 최종 결합 스트링
					String[] totalToken = buffer.toString().split("\\n"); // 불러왔던 파일을 라인별로 토큰 짜름.
					String typeString; // 각 메소드의 타입을 저장하는 스트링
					ClassInfo lastClass = null;
					
					while(j<totalToken.length){
						// lastClass 찾기
						if(totalToken[j].contains("class")){ // 클래스 부분 찾기
							do{
								totalString = totalString + totalToken[j] + "\n"; // 클래스 부분 받는거
								j++;
							}
							while(!totalToken[j].contains("}")); // 클래스 부분 넘기기
							totalString = totalString + "}" + "\n"; // 클래스 닫는 중괄호 추가.
							j++;
						}
						//ArrayList implementOrder = new ArrayList<Method>(); // implementOrder.get(X); == implementOrder[X];
						if(totalToken[j].contains(firstName)){ // 메소드 구현부 만나면
							for(Object oo : implementOrder){
								Method m = (Method)oo;
								String[] sourceToken = m.getSource().split("\\n"); // 해당 메소드의 소스를 라인별로 토큰.
								if(m.getName().contains(":"))
									totalString = totalString + m.getName();
								else{
									typeString = m.getType();
									totalString = totalString + typeString + " " + m.getClassName() + "::" + m.getName();
								}
								totalString = totalString + "\n{\n";
			
								for(String s : sourceToken){
									totalString = totalString + "\t" + s + "\n";
								}
								totalString = totalString + "}\n";
							}
							break;
						}
						
						j++;
					}
					
					// 텍스트 파일로 만들어놓기
					if (result == JFileChooser.APPROVE_OPTION) {
						try{
							File saveFile = fileChooser.getSelectedFile();
							FileOutputStream o = new FileOutputStream(saveFile);
							o.write(totalString.getBytes());
							o.close();
						}
						catch (IOException ex){
							ex.printStackTrace();
						}
					}
					
					// 저장한 파일로 다시 열기
					Classes.clear();
					implementOrder.clear(); // implementOrder.get(X); == implementOrder[X];
					firstName = null;
					buffer = new StringBuffer();
					try{
						file = new FileInputStream(fileChooser.getSelectedFile());	
						b = file.read();
						while(b!=-1){
							buffer.append((char)b);
							b = file.read();
						}
					} catch(FileNotFoundException ex){
						System.out.println("Oops : FileNotFoundException");
					} catch(IOException ex){
						System.out.println("Input error");
					}
					
					// 파싱...
					parse(buffer);
						
					// 트리 업데이트...
					updateActionMethod();
				}
			});
			
			//exitAction Listener 무명클래스
			exitAction.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					System.exit(0);
				}
			});
			
			howtoAction.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					howtoDialog.setVisible(true);
					howtoDialog.setSize(100, 100);
					howtoDialog.setLocation((screenSize.width - howtoDialog.getSize().width)/2, (screenSize.height - howtoDialog.getSize().height)/2);
					howtoDialog.setDefaultCloseOperation(HIDE_ON_CLOSE);
				}
			});
			
			aboutAction.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					aboutDialog.setVisible(true);
					aboutDialog.setSize(400, 125);
					JPanel aboutPanel = new JPanel();
					aboutPanel.add(new JLabel("OOP PROJECT #2 : CPP Class Viewer"));
					aboutPanel.add(new JLabel("Dept. of Computer Science / 2016920060 / Choi Hyeong Jin"));
					aboutPanel.add(new JLabel("Dept. of Computer Science / 2016920020 / Seo Ye Chan"));
					aboutDialog.add(aboutPanel);
					aboutDialog.setLocation((screenSize.width - aboutDialog.getSize().width)/2, (screenSize.height - aboutDialog.getSize().height)/2);
					aboutDialog.setDefaultCloseOperation(HIDE_ON_CLOSE);
				}
			});
			
			setVisible(true);
		}
	
		void updateActionMethod(){
			left.removeAll(); // 기존 정보 지우기.
			tree = null;
			DefaultMutableTreeNode root = new DefaultMutableTreeNode("Class"); // 트리 루트 노드 생성.
			ArrayList childClass = new ArrayList<DefaultMutableTreeNode>(); // 클래스 자식 노드들 저장하는 어레이리스트 생성.
			for(Object object : Classes){ // 모든 클래스들 집어넣기
				ClassInfo now = (ClassInfo)object;
				DefaultMutableTreeNode temp = new DefaultMutableTreeNode(now); // 해당 클래스에다가
				// 메소드
				for(Object object1 : now.getMethodList()){ // 메소드들 집어넣기
					Method nowMethod = (Method)object1;
					temp.add(new DefaultMutableTreeNode(nowMethod));
				}
				// 필드
				for(Object object2 : now.getFieldList()){ // 필드들 집어넣기
					Field nowField = (Field)object2;
					temp.add(new DefaultMutableTreeNode(nowField));
				}
				childClass.add(temp);
				root.add((DefaultMutableTreeNode)childClass.get(childClass.size()-1)); // 클래스 그때마다 트리에 추가.
			}
			tree = new JTree(root);
			// https://docs.oracle.com/javase/tutorial/uiswing/events/treeselectionlistener.html
			// http://docs.oracle.com/javase/tutorial/uiswing/components/tree.html#select
			tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			tree.addTreeSelectionListener(new TreeSelectionListener(){
				public void valueChanged(TreeSelectionEvent e){
					//returns the last path element of the selection.
					DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
					if(node == null) return; // Nothing is selected.
					
					Object nodeInfo = node.getUserObject();
					if(nodeInfo instanceof ClassInfo){
						//클래스인 경우, 멤버들 이름, 타입, 그리고 접근지정자를 나타내는 JTable을 출력해야 한다.
						ClassInfo temp = (ClassInfo)nodeInfo;
						showClassInfo(temp);
					}
					else if(nodeInfo instanceof Method){
						//메소드인 경우, 소스와 사용하는 필드 리스트를 출력해야 한다.
						Method temp = (Method)nodeInfo;
						sourceMethod.setText(temp.getSource());
						showMethodInfo(temp);
					}
					else if(nodeInfo instanceof Field){
						//필드인 경우, 필드 이름과 해당 필드가 쓰이는 메소드들을 나타내는 JTable을 출력해야 한다.
						Field temp = (Field)nodeInfo;
						showFieldInfo(temp);
					}
				}
			});
			
			tree.setVisibleRowCount(10);
			JScrollPane scroll = new JScrollPane(tree);
			left.add(scroll);
			left.add(listField);
			
			scroll.revalidate();
			//scroll.repaint();
			left.revalidate();
		}
		
		void showClassInfo(ClassInfo input){//클래스인 경우, 멤버들 이름, 타입, 그리고 접근지정자를 나타내는 JTable을 출력해야 한다.
			right.removeAll();
			listField.setText("");
			
			String[] header = {"Name","Type","Access"};
			String[][] contents = null;
			Object[] rowData = null;
			
			DefaultTableModel model = new DefaultTableModel(contents, header);
			// 메소드 채우기
			for(Object o : input.getMethodList()){
				Method temp = (Method)o;
				rowData = new Object[]{temp, temp.getType(), temp.getAccess()};
				model.addRow(rowData);
			}
			// 필드 채우기
			for(Object o : input.getFieldList()){
				Field temp = (Field)o;
				rowData = new Object[]{temp, temp.getType(), temp.getAccess()};
				model.addRow(rowData);
			}
			
			tableClass = new JTable(model);
			JScrollPane scrollpane = new JScrollPane(tableClass);
			
			right.add(scrollpane);
			right.revalidate();
		}
		
		void showMethodInfo(Method input){//메소드인 경우, 소스와 사용하는 필드 리스트를 출력해야 한다.
			right.removeAll();
			listField.setText("");
			input.getSrcArea().setText("");
			input.getSrcArea().setText(input.getSource());

			sourceMethod.getDocument().addDocumentListener(new DocumentListener() { // 소스 변경사항 실시간 저장.
		        @Override
		        public void removeUpdate(DocumentEvent e) {
		        	input.setSource(input.getSrcArea().getText());
		        	input.getSrcArea().revalidate();
		        }

		        @Override
		        public void insertUpdate(DocumentEvent e) {
		        	input.setSource(input.getSrcArea().getText());
		        	input.getSrcArea().revalidate();
		        }

		        @Override
		        public void changedUpdate(DocumentEvent arg0) {
		        	input.setSource(input.getSrcArea().getText());
		        	input.getSrcArea().revalidate();
		        }
		    });
			
			listField.setText("use: \n");
			for(Object object : input.getUsingFieldList()){
				Field field = (Field)object;
				if(!listField.getText().contains(field.getName()))
					listField.append(field.getName()+"\n");
			}
			
			JScrollPane scrollpane = new JScrollPane(input.getSrcArea());
			
			input.getSrcArea().revalidate();
			scrollpane.revalidate();
			right.add(scrollpane);
			right.revalidate();
			left.revalidate();
			listField.revalidate();
		}
		
		void showFieldInfo(Field input){//필드인 경우, 필드 이름과 해당 필드가 쓰이는 메소드들을 나타내는 JTable을 출력해야 한다.
			right.removeAll();
			listField.setText("");
			
			String methods = "";
			for(Object object : input.getUsingMethodList()){
				Method method = (Method)object;
				if(!methods.contains(method.getName())){
					methods += method+", ";	
				}
			}
			if(methods.length()!=0)
				methods = methods.substring(0, methods.length()-", ".length());
			String[] header = {"Name","methods"};
			String[][] contents = {{input.getName(),methods}};
			DefaultTableModel model = new DefaultTableModel(contents, header);
			tableField = new JTable(model);
			JScrollPane scrollpane = new JScrollPane(tableField);
			
			right.add(scrollpane);
			right.revalidate();
			//right.repaint();
			left.revalidate();
			listField.revalidate();
		}
	}
	
	public static void main(String[] args){
		MainFrame f = new MainFrame(); // GUI
	}
	
	// 파싱 메소드
	static void parse(StringBuffer buffer){
		String total = buffer.toString();

		// 클래스 파싱을 위한 쪼개기.
		String[] token = total.split("[ ;:{\\n\\t\\r]+");
		
		// 파싱 테스트.
		System.out.println("Parsing Classes...");
		for(String s : token)
			System.out.println(s);
		
		// 클래스 파싱.
		for(int i=0;i<token.length;i++){
			ClassInfo lastClass = null;
			if(Classes.size()!=0){
				lastClass = (ClassInfo)Classes.get(Classes.size()-1);
			}
			if(token[i].equals("class")){
				Classes.add(new ClassInfo(token[++i])); // "class" 다음 토큰은 클래스 이름.
				lastClass = (ClassInfo)Classes.get(Classes.size()-1);
				// 클래스 내 멤버 파싱
				String name=null, access=null, type=null;

				for(++i;!token[i].contains("}");i++){
					if(Keyword.isAccessModifier(token[i])){
						access = token[i]; // 접근지정자 저장
					}
					else if(Keyword.isDataType(token[i])){
						type = token[i++]; // 1번째 토큰은 데이터 타입.
						name = token[i];
						// 2번째 토큰 파싱
						if(name.contains("(")){ // 메소드일 때
							String className = lastClass.getName();
							lastClass.addMethod(new Method(name, access, type, className));
						}
						else if(name.contains("[")){ // 배열 필드일 때
							type = type + "[]";
							name = name.substring(0, name.indexOf('['));
							lastClass.addField(new Field(name, access, type));
						}
						else // 필드일 때
							lastClass.addField(new Field(name, access, type));
					}
					else{ // 생성자 또는 소멸자일 때
						name = lastClass.getName()+"::"+token[i]; // 생성자 소멸자 구분하기 위한 명명 규칙.
						type = "void";
						String className = lastClass.getName();
						if(name.contains("~")) // 소멸자
							lastClass.addMethod(new Method(name, access, type, className));
						else // 생성자
							lastClass.addMethod(new Method(name, access, type, className));
					}
				}
				// firstNames
			}
		}

		// 메소드 파싱을 위한 쪼개기.
		token = total.split("\\n");
		
		// 파싱 테스트.
		System.out.println("Parsing Methods...");
		for(String s : token)
			System.out.println(s);
		
		// 메소드 파싱.
		ClassInfo lastClass = null;
		for(int i=0;i<token.length;i++){
			if(token[i].contains("class")){ // 클래스 부분 찾기
				do{
					i++;
				}
				while(!token[i].contains("}")); // 클래스 부분 넘기기
				i++;
			}
			// firstName
			firstName = token[i];
			
		
			for(Object o : Classes){ // 메소드의 소속 클래스를 찾는다.
				ClassInfo c = (ClassInfo)o;
				if(token[i].contains(c.getName())){
					lastClass = c;
					//i++;
					break;
				}
			}
			
			for(Object o : lastClass.getMethodList()){
				Method m = (Method)o;
				int stack=0;
				String source="";
				int j=i;
				while(j<token.length){
					if(token[j].contains(m.getName().substring(0, m.getName().indexOf("(")-1))){
						// 메소드 구현 순서 저장.
						implementOrder.add(m);
						while(j<token.length){
							//source = source + token[j] + "\n"; 메소드명까지 받음
							if(token[j].contains("{")){
								stack++;
								if(stack>1) // 메소드 내부 코드만 입력받음.
								source = source + token[j].substring(1, token[j].length()) + "\n";
							}
							else if(token[j].contains("}")){
								stack--;
								if(stack==0) break;
								else if(stack>0) // 메소드 내부 코드만 입력받음.
								source = source + token[j].substring(1, token[j].length()) + "\n";
							}
							else{
								if(stack>0) // 메소드 내부 코드만 입력받음.
								source = source + token[j].substring(1, token[j].length()) + "\n";
							}
							
							for(Object oo : lastClass.getFieldList()){
								Field f = (Field)oo;
								if(token[j].contains(f.getName()) && (m.getUsingFieldList().indexOf(f.getName()))==-1){
									m.getUsingFieldList().add(f); // 메소드가 쓰는 필드 추가
									f.getUsingMethodList().add(m); // 필드가 쓰이는 메소드 추가
									//break;
								}
							}
							
							j++;
						}
						if(m.getSource() == null)
							m.setSource(source); // 소스 저장하기.
						i=j+1;
						break;
					}
					j++;
				}
			}
		} // 메소드 파싱 끝
	} // 파싱 메소드 끝
}