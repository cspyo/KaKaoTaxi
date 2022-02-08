
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Customer {
	JFrame frame;
	String frameTitle = "Kakao Taxi";
	JPanel mainPanel;
	LoginPanel loginPanel;
	FirstPanel firstPanel;
	WaitPanel waitPanel;
	FinishPanel finishPanel;

	CardLayout card;

	Geocoding geocoding;
	NaverMap naverMap;

	JTextArea incoming;			// 수신된 메시지를 출력하는 곳
	JTextArea outgoing;			// 송신할 메시지를 작성하는 곳
	JList counterParts;			// 현재 로그인한 채팅 상대목록을 나타내는 리스트.
	ObjectInputStream reader;	// 수신용 스트림
	ObjectOutputStream writer;	// 송신용 스트림
	Socket sock;				// 서버 연결용 소켓
	String user;				// 이 클라이언트로 로그인 한 유저의 이름

	boolean callWait = false;
	double sX,sY,dX,dY;
	String start , destination,map = "지도";
	JTextField startField, destinationField,startField2, destinationField2,startField3, destinationField3;
	JLabel mapL; 
	JButton logButton,logoutButton,baechaButton,cancelButton,logoutButton2,logoutButton3,chatButton;
	String myDriver;

	public static void main(String[] args) {
		Customer client = new Customer();
		client.go();
	}

	private void go() {
		geocoding  = new Geocoding();
		naverMap = new NaverMap();
		card = new CardLayout(0,0);
		// build GUI
		frame = new JFrame(frameTitle);

		mainPanel =  new JPanel();
		mainPanel.setLayout(card);

		loginPanel = new LoginPanel();
		loginPanel.setLayout(null);
		logButton = new JButton();
		logButton.setBorderPainted(false);logButton.setFocusPainted(false);logButton.setContentAreaFilled(false);
		logButton.setBounds(200, 580, 300, 70);
		loginPanel.add(logButton);

		firstPanel = new FirstPanel();
		firstPanel.setLayout(null);
		startField = new JTextField(50);
		startField.setFont(new Font("고딕", Font.BOLD, 16));
		startField.setBounds(160,58,480,40);
		destinationField = new JTextField(50);
		destinationField.setFont(new Font("고딕", Font.BOLD, 16));
		destinationField.setBounds(160,160,480,40);
		naverMap.downloadMap(map, 126.932021, 36.771219);
		mapL = new JLabel(naverMap.getMap(map));
		mapL.setBounds(45, 260, 600, 500);
		naverMap.fileDelete(map);
		logoutButton = new JButton();
		logoutButton.setBorderPainted(false);logoutButton.setFocusPainted(false);logoutButton.setContentAreaFilled(false);
		logoutButton.setBounds(22, 783, 80, 80);
		baechaButton = new JButton();
		baechaButton.setBorderPainted(false);baechaButton.setFocusPainted(false);baechaButton.setContentAreaFilled(false);
		baechaButton.setBounds(195, 775, 300, 75);
		firstPanel.add(startField);
		firstPanel.add(destinationField);
		firstPanel.add(mapL);
		firstPanel.add(logoutButton);
		firstPanel.add(baechaButton);

		waitPanel = new WaitPanel();
		waitPanel.setLayout(null);
		startField2 = new JTextField(50);
		startField2.setFont(new Font("고딕", Font.BOLD, 16));
		startField2.setBounds(160,58,480,40);
		startField2.setText(start);
		startField2.setEditable(false);
		destinationField2 = new JTextField(50);
		destinationField2.setFont(new Font("고딕", Font.BOLD, 16));
		destinationField2.setBounds(160,160,480,40);
		destinationField2.setText(destination);
		destinationField2.setEditable(false);
		logoutButton2 = new JButton();
		logoutButton2.setBorderPainted(false);logoutButton2.setFocusPainted(false);logoutButton2.setContentAreaFilled(false);
		logoutButton2.setBounds(22, 783, 80, 80);
		cancelButton = new JButton();
		cancelButton.setBorderPainted(false);cancelButton.setFocusPainted(false);cancelButton.setContentAreaFilled(false);
		cancelButton.setBounds(190, 770, 300, 75);
		waitPanel.add(startField2);
		waitPanel.add(destinationField2);
		waitPanel.add(logoutButton2);
		waitPanel.add(cancelButton);

		finishPanel = new FinishPanel();
		finishPanel.setLayout(null);
		startField3 = new JTextField(50);
		startField3.setFont(new Font("고딕", Font.BOLD, 16));
		startField3.setBounds(160,58,480,40);
		startField3.setText(start);
		startField3.setEditable(false);
		destinationField3 = new JTextField(50);
		destinationField3.setFont(new Font("고딕", Font.BOLD, 16));
		destinationField3.setBounds(160,160,480,40);
		destinationField3.setText(destination);
		destinationField3.setEditable(false);
		logoutButton3 = new JButton();
		logoutButton3.setBorderPainted(false);logoutButton3.setFocusPainted(false);logoutButton3.setContentAreaFilled(false);
		logoutButton3.setBounds(22, 783, 80, 80);
		chatButton = new JButton();
		chatButton.setBorderPainted(false);chatButton.setFocusPainted(false);chatButton.setContentAreaFilled(false);
		chatButton.setBounds(190, 770, 300, 75);
		finishPanel.add(startField3);
		finishPanel.add(destinationField3);
		finishPanel.add(logoutButton3);
		finishPanel.add(chatButton);


		mainPanel.add("logP",loginPanel);
		mainPanel.add("firP",firstPanel);
		mainPanel.add("waitP",waitPanel);
		mainPanel.add("finP",finishPanel);




		///리스너 
		logButton.addActionListener(new LogButtonListener());
		LogoutButtonListener logout = new LogoutButtonListener();
		logoutButton.addActionListener(logout);
		logoutButton2.addActionListener(logout);
		logoutButton3.addActionListener(logout);
		LabelListener ll = new  LabelListener();
		startField.addActionListener(ll);
		destinationField.addActionListener(ll);
		baechaButton.addActionListener(new BaechaButtonListener());
		cancelButton.addActionListener(new CancelButtonListener());


		card.show(mainPanel, "logP");

		// 네트워킹을 시동하고, 서버에서 메시지를 읽을 스레드 구동
		setUpNetworking();
		Thread readerThread = new Thread(new IncomingReader());
		readerThread.start();

		// 클라이언드 프레임 창 조정
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(mainPanel);
		frame.setSize(700,940);
		frame.setVisible(true);

		// 프레임이 살아 있으므로 여기서 만들은 스레드는 계속 진행 됨
		// 이 프레임 스레드를 종료하면, 이 프레임에서 만든 스레드들은 예외를 발생하게되고
		// 이를 이용해 모든 스레드를 안전하게 종료 시키도록 함
	} // close go

	private void setUpNetworking() {  
		try {
			// sock = new Socket("220.69.203.11", 5000);		// 오동익의 컴퓨터
			sock = new Socket("127.0.0.1", 6000);			// 소켓 통신을 위한 포트는 5000번 사용키로 함
			reader = new ObjectInputStream(sock.getInputStream());
			writer = new ObjectOutputStream(sock.getOutputStream());
		} catch(Exception ex) {
			JOptionPane.showMessageDialog(null, "서버접속에 실패하였습니다. 접속을 종료합니다.");
			ex.printStackTrace();
			frame.dispose();		// 네트워크가 초기 연결 안되면 클라이언트 강제 종료
		}
	} // close setUpNetworking 
	class LoginPanel extends JPanel{
		public void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			ImageIcon loginImage = new ImageIcon("src/Image/customerLogin.png");
			g2d.drawImage(loginImage.getImage(), 0,0, 700, 900, this);
		}
	}
	class FirstPanel extends JPanel{
		public void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			ImageIcon firstImage = new ImageIcon("src/Image/customerFirst.png");
			g2d.drawImage(firstImage.getImage(), 0,0, 700, 900, this);
		}
	}
	class WaitPanel extends JPanel{
		public void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			ImageIcon waitImage = new ImageIcon("src/Image/customerWait.png");
			g2d.drawImage(waitImage.getImage(), 0,0, 700, 900, this);
		}
	}
	class FinishPanel extends JPanel{
		public void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			ImageIcon finishImage = new ImageIcon("src/Image/customerFinish2.png");
			g2d.drawImage(finishImage.getImage(), 0,0, 700, 900, this);
		}
	}



	// 로그인과 아웃을 담당하는 버튼의 감청자. 처음에는 Login 이었다가 일단 로그인 되고나면 Logout을 처리
	// 로그인과 아웃을 담당하는 버튼의 감청자. 처음에는 Login 이었다가 일단 로그인 되고나면 Logout을 처리
	private class LogButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			processLogin();
		}
		// 로그인 처리
		private void processLogin() {
			user = JOptionPane.showInputDialog("사용자 이름을 입력하세요");
			try {
				writer.writeObject(new Message(Message.MsgType.CUSTOMER_LOGIN, user));
				writer.flush();
				frame.setTitle(frameTitle + " (로그인 : " + user + ")");
			} catch(Exception ex) {
				JOptionPane.showMessageDialog(null, "로그인 중 서버접속에 문제가 발생하였습니다.");
				ex.printStackTrace();
			}
			card.show(mainPanel, "firP");
		}

	}  // close LoginButtonListener inner class

	private class LogoutButtonListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			processLogout();
		}
		// 로그아웃 처리
		private void processLogout() {
			int choice = JOptionPane.showConfirmDialog(null, "Logout합니다");
			if (choice == JOptionPane.YES_OPTION) {
				try {
					writer.writeObject(new Message(Message.MsgType.CUSTOMER_LOGOUT, user));
					writer.flush();
					// 연결된 모든 스트림과 소켓을 닫고 프로그램을 종료 함
					writer.close(); reader.close(); sock.close();
				} catch(Exception ex) {
					JOptionPane.showMessageDialog(null, "로그아웃 중 서버접속에 문제가 발생하였습니다. 강제종료합니다");
					ex.printStackTrace();
				} finally {
					System.exit(100);			// 클라이언트 완전 종료 
				}
			}
		}
	}
	public class LabelListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			start = startField.getText();
			destination = destinationField.getText();
			if(e.getSource() == startField && start != "") {
				geocoding.geocoding(start);
				naverMap.downloadMap(map, geocoding.getX(), geocoding.getY());
				mapL.setIcon(naverMap.getMap(map));
				naverMap.fileDelete(map);
				firstPanel.repaint();
			}
			else if(e.getSource() == destinationField && destination != "") {
				geocoding.geocoding(start);
				sX = geocoding.getX();
				sY = geocoding.getY();
				geocoding.geocoding(destination);
				dX = geocoding.getX();
				dY = geocoding.getY();
				naverMap.downloadMarkerMap(map, sX, sY,dX,dY);
				mapL.setIcon(naverMap.getMap(map));
				naverMap.fileDelete(map);
				firstPanel.repaint();
			}
		}
	}
	public class BaechaButtonListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			start = startField.getText();
			destination = destinationField.getText();

			if(startField.getText() == "") {
				JOptionPane.showMessageDialog(null, "출발 위치를 입력해주세요");
				return;
			}
			else if(destinationField.getText() =="") {
				JOptionPane.showMessageDialog(null, "도착 위치를 입력해주세요");
				return;
			}

			try {
				writer.writeObject(new Message(Message.MsgType.CALL_TAXI, user, "",start, destination));
				writer.flush();
			}catch(Exception ex) {
				JOptionPane.showMessageDialog(null, "메시지 전송중 문제가 발생하였습니다.");
				ex.printStackTrace();
			}

			startField2.setText(start);
			destinationField2.setText(destination);
			callWait = true;
			card.show(mainPanel, "waitP");
		}
	}
	public class CancelButtonListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			callWait = false;
			card.show(mainPanel, "firP");
			
			try {
				writer.writeObject(new Message(Message.MsgType.CALL_CANCEL, user));
				writer.flush();
			}catch(Exception ex) {
				JOptionPane.showMessageDialog(null, "메시지 전송중 문제가 발생하였습니다.");
				ex.printStackTrace();
			}
			
		}
		
	}

	public class SendButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			String to = (String) counterParts.getSelectedValue();
			if (to == null) {
				JOptionPane.showMessageDialog(null, "송신할 대상을 선택한 후 메시지를 보내세요");
				return;
			}
			try {
				incoming.append(user + " : " + outgoing.getText() + "\n"); // 나의 메시지 창에 보이기
				writer.writeObject(new Message(Message.MsgType.CLIENT_MSG, user, to, outgoing.getText()));
				writer.flush();
				outgoing.setText("");
				outgoing.requestFocus();
			} catch(Exception ex) {
				JOptionPane.showMessageDialog(null, "메시지 전송중 문제가 발생하였습니다.");
				ex.printStackTrace();
			}
		}
	}  // close SendButtonListener inner class

	// 서버에서 보내는 메시지를 받는 스레드 작업을 정의하는 클래스
	public class IncomingReader implements Runnable {
		public void run() {
			Message message;             
			Message.MsgType type;
			try {
				while (true) {
					message = (Message) reader.readObject();     	 // 서버로기 부터의 메시지 대기                   
					type = message.getType();
					if (type == Message.MsgType.LOGIN_FAILURE) {	 // 로그인이 실패한 경우라면
						JOptionPane.showMessageDialog(null, "Login이 실패하였습니다. 다시 로그인하세요");
						card.show(mainPanel, "logP");
						frame.setTitle(frameTitle + " : 로그인 하세요");
					} else if(type == Message.MsgType.CALL_FAIL){
						JOptionPane.showMessageDialog(null, "배차 실패");
						callWait = false;
						card.show(mainPanel, "firP");
					}
					else if(type == Message.MsgType.CALL_ACCEPT) {
						if(callWait) {
							callWait = false;
							myDriver = message.getSender();
							startField3.setText(start);
							destinationField3.setText(destination);
							card.show(mainPanel, "finP");
						}
					}
					else if (type == Message.MsgType.FINISH){
						try {
							writer.writeObject(new Message(Message.MsgType.CUSTOMER_LOGOUT, user));
							writer.flush();
							// 연결된 모든 스트림과 소켓을 닫고 프로그램을 종료 함
							writer.close(); reader.close(); sock.close();
						} catch(Exception ex) {
							JOptionPane.showMessageDialog(null, "로그아웃 중 서버접속에 문제가 발생하였습니다. 강제종료합니다");
							ex.printStackTrace();
						} finally {
							System.exit(100);			// 클라이언트 완전 종료 
						}
					}
					
					else if (type == Message.MsgType.NO_ACT){
						// 아무 액션이 필요없는 메시지. 그냥 스킵
					}
					
					else {
						// 정체가 확인되지 않는 이상한 메시지
						throw new Exception("서버에서 알 수 없는 메시지 도착했음");
					}
				} // close while
			} catch(Exception ex) {
				System.out.println("클라이언트 스레드 종료");		// 프레임이 종료될 경우 이를 통해 스레드 종료
			}
		} // close run

		// 주어진 String 배열을 정렬한 새로운 배열 리턴
		private String [] sortUsers(String [] users) {
			String [] outList = new String[users.length];
			ArrayList<String> list = new ArrayList<String>();
			for (String s : users) {
				list.add(s);
			}
			Collections.sort(list);				// Collections.sort를 사용해 한방에 정렬
			for (int i=0; i<users.length; i++) {
				outList[i] = list.get(i);
			}
			return outList;
		}
	} // close inner class     
}
