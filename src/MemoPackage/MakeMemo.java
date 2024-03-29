package MemoPackage;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

class MemoFrame extends JFrame implements ActionListener, WindowListener,Runnable{

	JTextArea memoTextArea = new JTextArea(30, 20); //メモの記入欄
	JMenuBar menuBar = new JMenuBar(); //メニューバー
	JMenu memoList = new JMenu("メモ一覧"); //保存済みのメモのタイトルを一覧表示
	ArrayList<JMenuItem> memoItems = new ArrayList<>(); //保存済みのメモのタイトル
	JButton saveButton = new JButton("保存"); //保存ボタン
	JButton deleteButton = new JButton("削除"); //削除ボタン
	JButton newMemoButton = new JButton("新規作成"); //新規作成ボタン
	JButton referenceButton = new JButton("参照"); //他のファイルから参照する際のボタン
	JButton titleButton = new JButton("タイトルを変える"); //タイトル変更を可能にするボタン
	JButton titleButton2 = new JButton("タイトル決定"); //タイトルを変更するボタン
	JTextField titleTextField = new JTextField("新しいメモ", 20); //タイトルの表示欄
	JPanel panel = new JPanel(); //タイトル関係のコンポーネントを乗せるパネル
	JFileChooser chooser = new JFileChooser(); //参照する際に使用
	File fileChoose; //参照した際に使う変数
	
	String path; //メモ保管庫へのパスを格納
	String newSentence = ""; //他のメモを開こうとする直前の文章を格納する変数
	String preSentence = ""; //メモを開いた直後の文章を格納する変数
	String newTitle = ""; //他のメモを開こうとする直前のタイトルを格納する変数
	String preTitle = ""; //メモを開いた直後のタイトルを格納する変数
	String[] fileList; //メモ一覧にタイトルを載せるために、メモ保管庫のメモファイル名を格納する配列
	static int flag = 0; //runメソッドで使用
	int windowFlag = 0; //ConfirmFrameが開いている際にMemoFrameを閉じれなくするために使用
	static int j = 0; //ConfirmFrame生成の際に数値を受け渡しするための変数
	int n = 1; //同一タイトルをナンバリングする際に使用
	
	File dir; //初めにメモ保管庫を作る時やメモ保管庫内のメモファイル一覧を取得するときに使用
	File file; //メモの入出力に使用
	File oldFile; //タイトルを変更する際に使用
	FileWriter fw; 
	FileReader fr;
	PrintWriter pw;
	BufferedWriter bw;
	BufferedReader br;
	
	MemoFrame(){
		setTitle("メモ");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		JScrollPane scrollPane = new JScrollPane(memoTextArea);
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		//メモファイルを保存するディレクトリ（メモ保管庫）を作成
		dir = new File("メモ保管庫");
		if(dir.mkdir()) {
			System.out.println("メモ保管庫を作成しました");
		} else {
			System.out.println("メモ保管庫の作成に失敗したか、既に作成されています");
		}
		//メモ保管庫へのパスを取得
		path = dir.getAbsolutePath();
		//確認
		System.out.println(path);
		//メモ一覧、保存・削除ボタン、新規作成ボタン、参照ボタンをメニューバーに追加し、メニューバーをコンテナに追加
		fileList = dir.list();
		int i = 0;
		for(String s : fileList) {
			//メモタイトルから「.txt」を削除
			String sRemoved = s.replace(".txt", "");
			//メモ一覧に追加するためのメモアイテムを作成
			memoItems.add(new JMenuItem(sRemoved));
			//メモ一覧にメモタイトルを追加
			memoList.add(memoItems.get(i)); 
			i++;
		}
		
		menuBar.add(memoList);
		menuBar.add(saveButton);
		menuBar.add(deleteButton);
		menuBar.add(newMemoButton);
		menuBar.add(referenceButton);
		this.setJMenuBar(menuBar);
		//メモタイトルの入力フォームとメモタイトルの設定ボタンをパネルに配置しコンテナに追加
		panel.setLayout(new FlowLayout());
		panel.add(titleButton);
		panel.add(titleButton2);
		panel.add(titleTextField);
		getContentPane().add(panel);
		//テキストを表示するエリアをコンテナに追加
		getContentPane().add(scrollPane);
		//イベント設定
		this.addWindowListener(this);
		//メモ一覧のメモタイトルそれぞれにアクションリスナーを設定
		for(i = 0; i < memoItems.size(); i++) {
			memoItems.get(i).addActionListener(this);
		}
		memoList.addActionListener(this);
		saveButton.addActionListener(this);
		deleteButton.addActionListener(this);
		newMemoButton.addActionListener(this);
		titleButton.addActionListener(this);
		titleButton2.addActionListener(this);
		referenceButton.addActionListener(this);
		//タイトル変更は「タイトルを変える」ボタンを押さないとできないようにする
		titleButton2.setEnabled(false); 
		titleTextField.setEnabled(false);
		//デフォルトのタイトルを「新しいメモ」としているが、すでに同じ名前のファイルが存在する場合がある
		//よってナンバリング処理をしておく
		numbering();
		
		setSize(700,700);
		setVisible(true);
	}
	
	public void actionPerformed(ActionEvent ae) {
		//メモを保存。ディレクトリ「メモ保管庫」にファイルを作成し、メモ一覧にメモタイトルを追加する
		if(ae.getSource() == saveButton) {
			save();
		}
		//確認画面を出し、ディレクトリからメモをを削除する。
		else if(ae.getSource() == deleteButton) {
			//確認画面を表示する
			//削除してよろしいですか? flag : 1削除、2キャンセル
			flag = 0; 
			//新しいフレームを生成して選択を待つ
			ConfirmFrame cm = new ConfirmFrame();
			Thread thread = new Thread(this);
			thread.start();
		}
		//新規作成。画面を白紙に戻し、タイトルは「新しいメモ」にする。
		else if(ae.getSource() == newMemoButton) {
			//直前に作業していた文章に変更がないか比較確認するために現在の文章を取得 
			newSentence = memoTextArea.getText();
			newTitle = titleTextField.getText();
			//確認
			System.out.println(preSentence.equals(newSentence));
			//直前に作業していたメモに変更がない場合
			if(preSentence.equals(newSentence)) {
				//白紙にする
				memoTextArea.setText(""); 
				titleTextField.setText("新しいメモ");
				//タイトルを「新しいメモ」にする。タイトルが重複している場合、「新しいメモ(n)」とする
				numbering();
				//作業している文章に変更がないか比較確認するために現在の文章を取得
				preSentence = memoTextArea.getText();
				preTitle = titleTextField.getText();
				//確認
				System.out.println(preTitle + "が作成されました");
			} 
			//直前に作業していたメモに変更があった場合
			else {
				//確認画面を表示する
				//変更が保存されていません。flag : 4保存、5保存しない、6キャンセル
				flag = 3; 
				//新しいフレームを生成して選択を待つ
				ConfirmFrame cm = new ConfirmFrame();
				Thread thread = new Thread(this);
				thread.start();
			}
		}
		//タイトルを変更可能にする
		else if(ae.getSource() == titleButton) {
			//いったんすべてのコンポーネントを使用不可する
			changeEnable(false); 
			//タイトル変更に必要なコンポーネントのみ使用可能にする
			titleButton2.setEnabled(true); 
			titleTextField.setEnabled(true);
			//変更前のタイトルを取得
			preTitle = titleTextField.getText(); 
		}
		//タイトルを変更する
		else if(ae.getSource() == titleButton2){
			//変更後のタイトルを取得
			newTitle = titleTextField.getText();
			//変更前後のタイトルが同じ、または空欄である場合
			if(preTitle.equals(newTitle) || newTitle.equals("")) { 
				//元のタイトルにする
				titleTextField.setText(preTitle);
				//コンポーネントの有効・無効を元に戻す
				changeEnable(true);
				titleButton2.setEnabled(false);
				titleTextField.setEnabled(false);
				return;
			}
			//タイトルに変更がある場合、ディレクトリに保存されている当該メモファイルの名前を変更する
			//renameToメソッドを使用するために前のタイトルのファイルを生成
			oldFile = new File(path + "\\" + preTitle + ".txt");
			//renameToメソッドを使用するために新タイトルのファイルを生成
			file = new File(path + "\\" + newTitle + ".txt"); 
				//他のファイルと名前が重複している場合はそのメモを上書きするか確認 
				if(file.exists()) {
					//確認
					System.out.println("ファイル名 [" + newTitle + "] は既に存在しています");
					//同じ名前のファイルが既に存在しています。 8上書き保存、9別名で保存、10キャンセル
					flag = 7;
					//新しいフレームを生成して選択を待つ
					ConfirmFrame cm = new ConfirmFrame(newTitle); 
					Thread thread = new Thread(this);
					thread.start();
					return;
				}
			//他のファイルとタイトルが重複していなかった場合、タイトル変更を実行
			oldFile.renameTo(file);
			//確認
			System.out.println("タイトルを [" + newTitle + "] に変更しました");
			//タイトル変更に伴いメモ一覧を整理
			arrange(); 
			//コンポーネントの有効・無効を元に戻す
			changeEnable(true); 
			titleButton2.setEnabled(false);
			titleTextField.setEnabled(false);
		}
		//他のファイルから文章を参照する
		else if(ae.getSource() == referenceButton) { 
			//テキストファイルのみ表示されるようにする
		    FileNameExtensionFilter filter = new FileNameExtensionFilter("テキストファイル(*.txt)", "txt");
		    chooser.setFileFilter(filter);
		    //デフォルトでMemoProjectのフォルダが開くようにする
			chooser.setSelectedFile(new File(path));
			//新ウィンドウを開く
			//「開く」ボタンが押された場合
			if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				//「開く」ボタンが押されたときに選択しているファイルを変数に格納
				fileChoose = chooser.getSelectedFile();
				//直前に作業していた文章に変更がないか比較確認するために現在の文章を取得
				newSentence = memoTextArea.getText();
				//ファイルが選択されていればその文章を取得する
				if(fileChoose != null) {
					//直前に作業していたメモに変更がなければ、そのまま取得した文章を表示する
					if(preSentence.equals(newSentence)) { 
						choose();
						//確認
						//タイトルが重複していてナンバリング処理された場合、ナンバリングを削除したものをコンソールで表示する
						String str = newTitle.replace( "(" + n + ")", ""); 
						System.out.println("[" + str + "] を参照しました");
					}
					//直前に作業していたメモに変更があった場合
					else {
						//変更が保存されていません。flag : 12保存、13保存しない、14キャンセル
						flag = 11; 
						//新しいフレームを生成して選択を待つ
						ConfirmFrame cm = new ConfirmFrame(); 
						Thread thread = new Thread(this);
						thread.start();
						return;
					}
				}else {
					//確認
					System.out.println("ファイルが選択されていません"); 
				}
			} else {
				//確認
				System.out.println("「取消」もしくは×が押されました"); 
			}
		}
		//メモ一覧内のメモのタイトルがクリックされた場合、そのタイトルのファイルを開く
		else {  
			//メモ一覧内のどのメモタイトルが押されたか、メモ一覧を順に確認する
			for(int i = 0; i < memoItems.size(); i++) { 
				if(ae.getSource() == memoItems.get(i)) { 	
					//確認
					System.out.println(memoItems.get(i).getText() + "がクリックされました");
					//直前に作業していた文章に変更がないか比較確認するために現在の文章を取得
					newSentence = memoTextArea.getText(); 
					//作業中の文章に変更がなければ、そのままファイルから文章を取得
					if(preSentence.equals(newSentence)) {
						acquire(i); 
					}
					//直前に作業していたメモに変更があった場合
					else {
						//変更が保存されていません。flag : 16保存、17保存しない、18キャンセル
						flag = 15; 
						j = i;
						//新しいフレームを生成して選択を待つ
						ConfirmFrame cm = new ConfirmFrame(); 
						Thread thread = new Thread(this);
						thread.start();
					}
				}
			}
		}
	}
	
	//文章を保存するメソッド
	public void save() { 
		try {
			//現在の文章とタイトルを取得
			newSentence = memoTextArea.getText();
			newTitle = titleTextField.getText();
			//取得したタイトルでテキストファイルを作成
			file = new File(path + "\\" + newTitle + ".txt");
			//作成したファイルに取得した文章を書きこむ
			fw = new FileWriter(file);
			bw = new BufferedWriter(fw);
			pw = new PrintWriter(bw);
			pw.println(newSentence);
			pw.flush();
			pw.close();
			//確認
			System.out.println("タイトル" + newTitle + "で" + newSentence + "を書き込みました");
			//作業している文章に変更がないか比較確認するために現在の文章を取得
			preSentence = memoTextArea.getText();
			//メモ一覧を整理
			arrange(); 
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	//メモ一覧を整理するメソッド
	public void arrange() {
		//いったんすべて削除
		memoList.removeAll(); 
		memoItems.removeAll(memoItems);
		//メモ保管庫のファイル一覧を取得
		fileList = dir.list();
		int i = 0;
		for(String s : fileList) {
			//メモタイトルから「.txt」を削除
			String sRemoved = s.replace(".txt", "");
			//メモ一覧に追加するためのメモアイテムを作成
			memoItems.add(new JMenuItem(sRemoved));
			//メモ一覧にメモタイトルを追加
			memoList.add(memoItems.get(i));
			//アクションリスナーを設定
			memoItems.get(i).addActionListener(this);
			i++;
		}
	}
	
	//メモ保管庫から文章を取得するメソッド
	public void acquire(int i) {
		try {
			//メモの入力欄を白紙にする
			memoTextArea.setText("");
			//タイトル欄に選択したメモのタイトルを表示
			titleTextField.setText(memoItems.get(i).getText());
			//文章を取得
			file = new File(path + "\\" + memoItems.get(i).getText() + ".txt");
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			while((newSentence = br.readLine()) != null) {
				memoTextArea.append(newSentence + "\n");
				memoTextArea.setCaretPosition(memoTextArea.getText().length());
			}
			br.close();
			//作業している文章に変更がないか比較確認するために現在の文章を取得
			preSentence = memoTextArea.getText();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	//参照したテキストファイルから文章を取得するメソッド
	public void choose() { 
		try {
			//メモの入力欄とタイトル欄を白紙にする
			memoTextArea.setText("");
			titleTextField.setText("");
			//文章を取得
			file = new File(fileChoose.getPath());
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			while((newSentence = br.readLine()) != null) {
				memoTextArea.append(newSentence + "\n");
				memoTextArea.setCaretPosition(memoTextArea.getText().length());
			}
			//参照したファイルのファイル名を取得
			String s = fileChoose.getName();
			String sRemoved = s.replace(".txt", "");
			//取得したファイル名をタイトル欄に表示
			titleTextField.setText(sRemoved);
			//すでに同じメモタイトルが存在する場合、ナンバリングする
			numbering(); 
			br.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	//コンポーネント使用の可能・不可能を切り替えるメソッド
	public void changeEnable(boolean b) { 
		memoList.setEnabled(b);
		saveButton.setEnabled(b);
		deleteButton.setEnabled(b);
		newMemoButton.setEnabled(b);
		titleButton.setEnabled(b);
		titleButton2.setEnabled(b);
		titleTextField.setEnabled(b);
		memoTextArea.setEnabled(b);
		referenceButton.setEnabled(b);
	}
	
	 //タイトルが重複している場合、「タイトル(n)」とするメソッド
	public void numbering() {
		 n = 1;
		 //現在のタイトルを取得
		 newTitle = titleTextField.getText();
	 	 file = new File(path + "\\" + newTitle + ".txt");
	 	 //同じファイル名がすでに存在している場合
		 while(file.exists()) {
			 //すでにナンバリングされている場合、いったん（n）を削除する
			 newTitle = newTitle.replace( "(" + n + ")", "");
			 //数字を増やして再度ナンバリング
			 n++;
			 newTitle = newTitle + "(" + n + ")";
			 //新たなファイル名でメモ保管庫にファイルを作成
			 file = new File(path + "\\" + newTitle + ".txt");
		 }
		 titleTextField.setText(newTitle);
	}
	
	public void run() {
		System.out.println("スレッドスタート");
		//確認画面が開いている間、MemoFrameのコンポーネントを使えなくする
		changeEnable(false); 
		//MemoFrameを閉じれなくする
		windowFlag = 1;
		//保存する、しない等が選択されるまで繰り返す
		while(flag == 0 || flag == 3 || flag == 7 || flag == 11 || flag == 15 || flag == 19) { 
			try {
				System.out.println(flag);
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//確認
		System.out.println(flag);
		//選択に応じてflagの値が変化する
		switch(flag) {
		//削除する際の分岐(1～2)
		case 1: //削除
			//メモタイトルを取得
			newTitle = titleTextField.getText();
			//そのタイトルのファイルをメモ保管庫から削除する
			file = new File(path + "\\" + newTitle + ".txt");
			if(file.delete()) {
				System.out.println(newTitle + "を削除しました");
				memoTextArea.setText(""); //白紙に戻す
				titleTextField.setText("");
			}
			//作業している文章に変更がないか比較確認するために現在の文章を取得
			preSentence = memoTextArea.getText();
			//メモ一覧を整理
   			arrange(); 
			break;
		case 2: //キャンセル。何もしない
			break;
		//新規作成の際の分岐(4～6)
		case 4: //保存してから新規作成
			save();
			memoTextArea.setText("");
			titleTextField.setText("新しいメモ");
			//タイトルが重複している場合があるためナンバリングメソッド使用
			numbering();
			//作業している文章に変更がないか比較確認するために現在の文章を取得
			preSentence = memoTextArea.getText();
			System.out.println(preTitle + "が作成されました");
			break;
		case 5: //保存せず新規作成
			memoTextArea.setText("");
			titleTextField.setText("新しいメモ");
			//タイトルが重複している場合があるためナンバリングメソッド使用
			numbering();
			//作業している文章に変更がないか比較確認するために現在の文章を取得
			preSentence = memoTextArea.getText();
			System.out.println(preTitle + "が作成されました");
		case 6: //キャンセル。何もしない
			break;
		//タイトル変更の際の分岐(8～10)
		case 8: //上書き保存する
			save();
			oldFile.delete();
			//作業している文章に変更がないか比較確認するために現在の文章を取得
			preSentence = memoTextArea.getText();
			//タイトル変更に伴いメモ一覧を整理
			arrange();
			System.out.println("[" + newTitle + "] を上書き保存しました");
			break;
		case 9: //別名で保存する
			//タイトルを「タイトル(n)」とする
			numbering();
			//タイトル変更を実行
			oldFile.renameTo(file);
			//確認
			System.out.println("タイトルを [" + newTitle + "] に変更しました");
			//作業している文章に変更がないか比較確認するために現在の文章を取得
			preSentence = memoTextArea.getText();
			//タイトル変更に伴いメモ一覧を整理
			arrange();
			break;
		case 10: //キャンセル
			//タイトルをもとに戻す
			titleTextField.setText(preTitle);
			break;
		//参照から開く際の分岐(12～14)
		case 12: //保存してから文章取得
			save();
			choose();
			break;
		case 13: //保存せず文章取得
			choose();
		case 14: //キャンセル。何もしない
			break;
		//メモ一覧から選んだ際の分岐(16～18)
		case 16: //保存してから文章取得
			save(); 
			acquire(j);
			break;
		case 17: //保存せず文章取得
			acquire(j);
		case 18: //キャンセル。何もしない
			break;
		//ウィンドウを閉じる際の分岐(20～22)
		case 20: //保存してから閉じる
			save();
			System.out.println("プログラムを終了します。");
	    	System.exit(EXIT_ON_CLOSE);
		case 21: //保存せず閉じる
			System.out.println("プログラムを終了します。");
	    	System.exit(EXIT_ON_CLOSE);		
		case 22: //キャンセル。何もしない
		}
		//タイトル変更ボタンを除いて元フレームのコンポーネントを使えるようにする
		changeEnable(true); 
		//MemoFrameを閉じれるようにする
		windowFlag = 0;
		titleButton2.setEnabled(false);
		titleTextField.setEnabled(false);
	}
	
	public void windowClosing(WindowEvent e) {
		//確認画面(ConfirmFrame)が開いていなければMemoFrameを閉じれる
		if(windowFlag == 0) {
			//直前に作業していた文章に変更がないか比較確認するために現在の文章を取得 
			newSentence = memoTextArea.getText();
			//確認
			System.out.println(preSentence.equals(newSentence));
			//直前に作業していたメモに変更がない場合
			if(preSentence.equals(newSentence)) {
				//そのままプログラムを終了する
				System.out.println("プログラムを終了します。");
		    	System.exit(EXIT_ON_CLOSE);		
			} 
			else {
				//直前に作業していたメモに変更があった場合
				flag = 19; //変更が保存されていません。flag : 20保存、21保存しない、22キャンセル
				//新しいフレームを生成して選択を待つ
				ConfirmFrame cm = new ConfirmFrame();
				Thread thread = new Thread(this);
				thread.start();
			}
		}
	}

	public void windowOpened(WindowEvent e) {}
	public void windowClosed(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
}

//確認画面を出すクラス
class ConfirmFrame extends JFrame  implements ActionListener, WindowListener{ 
	JButton deleteButton = new JButton("削除する");
	JButton deleteCancelButton = new JButton("キャンセル");
	JButton saveButton = new JButton("保存する");
	JButton noSaveButton = new JButton("保存しない");
	JButton cancelButton = new JButton("キャンセル");
	JButton overWriteButton = new JButton("上書き保存する");
	JButton differentNameButton = new JButton("別名で保存する");
	JLabel label = new JLabel("<html>選択中のメモを削除してよろしいですか？<html>");
	JLabel label2 = new JLabel("<html>編集中のメモが保存されていません。<br>保存しますか？<html>");
	JPanel panel = new JPanel();
	JPanel panel2 = new JPanel();
	JPanel panel3 = new JPanel();
	
	//メモを削除するか確認する際の、または、メモ入力欄の文章の変更を保存するか確認する際のコンストラクタ
	ConfirmFrame(){
		//メモを削除するか確認する場合
		if(MemoFrame.flag == 0) {
			setTitle("確認");
			setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
			//ラベルを表示するパネルをコンテナに追加
			panel2.setLayout(new FlowLayout());
			panel2.add(label);
			getContentPane().add(panel2);
			//ボタンをパネルに配置しコンテナに追加
			panel.setLayout(new FlowLayout());
			panel.add(deleteButton);
			panel.add(deleteCancelButton);
			getContentPane().add(panel);
			//イベント設定
			this.addWindowListener(this);
			deleteButton.addActionListener(this);
			deleteCancelButton.addActionListener(this);
			setSize(350,200);
			setVisible(true);
		}
		//メモの変更を保存するか確認する場合
		else {
			setTitle("確認");
			setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
			//ラベルを表示するパネルをコンテナに追加
			panel2.setLayout(new FlowLayout());
			panel2.add(label2);
			getContentPane().add(panel2);
			//ボタンをパネルに配置しコンテナに追加
			panel.setLayout(new FlowLayout());
			panel.add(saveButton);
			panel.add(noSaveButton);
			panel.add(cancelButton);
			getContentPane().add(panel);
			//イベント設定
			this.addWindowListener(this);
			saveButton.addActionListener(this);
			noSaveButton.addActionListener(this);
			cancelButton.addActionListener(this);
			setSize(350,200);
			setVisible(true);
		}
	}
	
	//重複しているタイトルを上書保存するか確認する際のコンストラクタ
	ConfirmFrame(String newTitle){
		setTitle("確認");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		//ラベル作成し、ラベルを表示するパネルをコンテナに追加
		panel3.setLayout(new FlowLayout());
		JLabel label3 = new JLabel("<html>ファイル名 [<html>" + newTitle + "<html>] は既に存在しています。<html>");
		panel3.add(label3);
		getContentPane().add(panel3);
		//ボタンをパネルに配置しコンテナに追加
		panel.setLayout(new FlowLayout());
		panel.add(overWriteButton);
		panel.add(differentNameButton);
		panel.add(cancelButton);
		getContentPane().add(panel);
		//イベント設定
		this.addWindowListener(this);
		overWriteButton.addActionListener(this);
		differentNameButton.addActionListener(this);
		cancelButton.addActionListener(this);
		setSize(350,200);
		setVisible(true);
		
	}
	
	public void actionPerformed(ActionEvent ae) {
		//選択に応じてflagの値を変化させる
		if(ae.getSource() == deleteButton) {
			//削除する
			MemoFrame.flag += 1;
		} else if(ae.getSource() == deleteCancelButton) {
			//キャンセル。何も起こらない
			MemoFrame.flag += 2;
		} else if(ae.getSource() == saveButton) {
			//保存する
			MemoFrame.flag += 1;
		} else if(ae.getSource() == noSaveButton) {
			//保存しない
			MemoFrame.flag += 2;
		} else if(ae.getSource() == cancelButton) {
			//キャンセル。何も起こらない
			MemoFrame.flag += 3;
		} else if(ae.getSource() == overWriteButton) {
			//上書き保存する
			MemoFrame.flag += 1;
		} else if(ae.getSource() == differentNameButton) {
			//別名で保存する
			MemoFrame.flag += 2;
		}
		setVisible(false);
	}

	public void windowClosing(WindowEvent e) {
		//キャンセルと同じ。何も起こらない
		MemoFrame.flag = 2;
	}
	
	public void windowOpened(WindowEvent e) {}
	public void windowClosed(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
}

public class MakeMemo {
	public static void main(String[] args) {
		new MemoFrame();
	}
}
