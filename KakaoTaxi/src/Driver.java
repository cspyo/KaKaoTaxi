
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;


import java.awt.*;
import java.awt.event.*;

public class Driver {
	JFrame frame;
	String frameTitle = "Kakao Taxi for Drivers";
	JPanel mainPanel;
	LoginPanel loginPanel;
	FirstPanel firstPanel;
	WaitPanel waitPanel;
	CallPanel callPanel;
	DrivingPanel drivingPanel;

	CardLayout card;
	Geocoding geocoding;

	JTextArea incoming;			// 수신된 메시지를 출력하는 곳
	JTextArea outgoing;			// 송신할 메시지를 작성하는 곳
	JList counterParts;			// 현재 로그인한 채팅 상대목록을 나타내는 리스트.
	ObjectInputStream reader;	// 수신용 스트림
	ObjectOutputStream writer;	// 송신용 스트림
	Socket sock;				// 서버 연결용 소켓
	String user;				// 이 클라이언트로 로그인 한 유저의 이름
	boolean callStart = false;
	boolean driving = false;
	boolean callRequest = false;

	String myCustomer;
	String start, destination;
	String nowLocation = "온양온천역";
	JTextField nowLocationField,nowLocationField2;
	JTextField startField, destinationField;
	JButton logButton,logoutButton,callStartButton,cancelButton,logoutButton2,logoutButton3,logoutButton4,acceptButton,refuseButton,finishButton;			// 토글이 되는 로그인/로그아웃 버튼


	public static void main(String[] args) {
		Driver client = new Driver();
		client.go();
	}

	private void go() {
		geocoding = new Geocoding();
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
		nowLocationField = new JTextField(50);
		nowLocationField.setFont(new Font("고딕", Font.BOLD, 18));
		nowLocationField.setBounds(195,59,470,50);
		logoutButton = new JButton();
		logoutButton.setBorderPainted(false);logoutButton.setFocusPainted(false);logoutButton.setContentAreaFilled(false);
		logoutButton.setBounds(31, 787, 80, 80);
		callStartButton = new JButton();
		callStartButton.setBorderPainted(false);callStartButton.setFocusPainted(false);callStartButton.setContentAreaFilled(false);
		callStartButton.setBounds(199, 743, 300, 75);
		firstPanel.add(nowLocationField);
		firstPanel.add(logoutButton);
		firstPanel.add(callStartButton);

		waitPanel = new WaitPanel();
		waitPanel.setLayout(null);
		nowLocationField2 = new JTextField(50);
		nowLocationField2.setFont(new Font("고딕", Font.BOLD, 18));
		nowLocationField2.setBounds(195,59,470,50);
		nowLocationField2.setText(nowLocation);
		nowLocationField2.setEditable(false);
		logoutButton2 = new JButton();
		logoutButton2.setBorderPainted(false);logoutButton2.setFocusPainted(false);logoutButton2.setContentAreaFilled(false);
		logoutButton2.setBounds(31, 787, 80, 80);
		cancelButton = new JButton();
		cancelButton.setBorderPainted(false);cancelButton.setFocusPainted(false);cancelButton.setContentAreaFilled(false);
		cancelButton.setBounds(199, 743, 300, 75);
		waitPanel.add(nowLocationField2);
		waitPanel.add(logoutButton2);
		waitPanel.add(cancelButton);

		callPanel = new CallPanel();
		callPanel.setLayout(null);
		startField = new JTextField(50);
		startField.setFont(new Font("고딕", Font.BOLD, 16));
		startField.setBounds(175,325,435,40);
		startField.setText(start);
		startField.setEditable(false);
		destinationField = new JTextField(50);
		destinationField.setFont(new Font("고딕", Font.BOLD, 16));
		destinationField.setBounds(175,427,435,40);
		destinationField.setText(destination);
		destinationField.setEditable(false);
		acceptButton = new JButton();
		acceptButton.setBorderPainted(false);acceptButton.setFocusPainted(false);acceptButton.setContentAreaFilled(false);
		acceptButton.setBounds(97, 622, 205, 75);
		refuseButton = new JButton();
		refuseButton.setBorderPainted(false);refuseButton.setFocusPainted(false);refuseButton.setContentAreaFilled(false);
		refuseButton.setBounds(370, 623, 205, 75);
		logoutButton3 = new JButton();
		logoutButton3.setBorderPainted(false);logoutButton3.setFocusPainted(false);logoutButton3.setContentAreaFilled(false);
		logoutButton3.setBounds(31, 787, 80, 80);
		callPanel.add(startField);
		callPanel.add(destinationField);
		callPanel.add(acceptButton);
		callPanel.add(refuseButton);
		callPanel.add(logoutButton3);

		drivingPanel = new DrivingPanel();
		drivingPanel.setLayout(null);
		logoutButton4 = new JButton();
		logoutButton4.setBorderPainted(false);logoutButton4.setFocusPainted(false);logoutButton4.setContentAreaFilled(false);
		logoutButton4.setBounds(31, 787, 80, 80);
		finishButton = new JButton();
		finishButton.setBorderPainted(false);finishButton.setFocusPainted(false);finishButton.setContentAreaFilled(false);
		finishButton.setBounds(199, 743, 300, 75);
		drivingPanel.add(logoutButton4);
		drivingPanel.add(finishButton);

		mainPanel.add("logP",loginPanel);
		mainPanel.add("firP",firstPanel);
		mainPanel.add("waitP",waitPanel);
		mainPanel.add("callP",callPanel);
		mainPanel.add("drvP",drivingPanel);

		logButton.addActionListener(new LogButtonListener());
		LogoutButtonListener logout = new LogoutButtonListener();
		logoutButton.addActionListener(logout);
		logoutButton2.addActionListener(logout);
		logoutButton3.addActionListener(logout);
		logoutButton4.addActionListener(logout);
		callStartButton.addActionListener(new CallStartButtonListener());
		acceptButton.addActionListener(new AcceptButtonListener());
		refuseButton.addActionListener(new RefuseButtonListener());
		finishButton.addActionListener(new FinishButtonListener());
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
			ImageIcon loginImage = new ImageIcon("src/Image/driverLogin.png");
			g2d.drawImage(loginImage.getImage(), 0,0, 700, 900, this);
		}
	}
	class FirstPanel extends JPanel{
		public void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			ImageIcon firstImage = new ImageIcon("src/Image/driverFirst.png");
			g2d.drawImage(firstImage.getImage(), 0,0, 700, 900, this);
		}
	}
	class WaitPanel extends JPanel{
		public void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			ImageIcon waitImage = new ImageIcon("src/Image/driverWait.png");
			g2d.drawImage(waitImage.getImage(), 0,0, 700, 900, this);
		}
	}
	class DrivingPanel extends JPanel{
		public void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			ImageIcon finishImage = new ImageIcon("src/Image/driverDriving.png");
			g2d.drawImage(finishImage.getImage(), 0,0, 700, 900, this);
		}
	}
	class CallPanel extends JPanel{
		public void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			ImageIcon finishImage = new ImageIcon("src/Image/driverCall.png");
			g2d.drawImage(finishImage.getImage(), 0,0, 700, 900, this);
		}
	}



	// 로그인과 아웃을 담당하는 버튼의 감청자. 처음에는 Login 이었다가 일단 로그인 되고나면 Logout을 처리
	private class LogButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			processLogin();
		}
		// 로그인 처리
		private void processLogin() {
			user = JOptionPane.showInputDialog("사용자 이름을 입력하세요");
			try {
				writer.writeObject(new Message(Message.MsgType.DRIVER_LOGIN, user));
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
					writer.writeObject(new Message(Message.MsgType.DRIVER_LOGOUT, user));
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
	public class CallStartButtonListener implements ActionListener{
		String now;
		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			now = nowLocationField.getText();
			if(now == null) {
				JOptionPane.showMessageDialog(null, "현위치를 입력한 후 콜을 시작해주세요");
				return;
			}
			nowLocation = nowLocationField.getText();
			nowLocationField2.setText(nowLocation);
			callStart = true;
			card.show(mainPanel, "waitP");
			
		}
	}
	public class AcceptButtonListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			try {
				writer.writeObject(new Message(Message.MsgType.CALL_ACCEPT, user, myCustomer));
				writer.flush();
			}catch(Exception ex) {
				JOptionPane.showMessageDialog(null, "메시지 전송중 문제가 발생하였습니다.");
				ex.printStackTrace();
			}
			driving = true;
			card.show(mainPanel, "drvP");
		}
	}
	public class RefuseButtonListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			callStart = true;
			callRequest = false;
			card.show(mainPanel, "waitP");
			myCustomer = "";
		}	
	}
	public class FinishButtonListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				writer.writeObject(new Message(Message.MsgType.FINISH, user, myCustomer));
				writer.flush();
			}catch(Exception ex) {
				JOptionPane.showMessageDialog(null, "메시지 전송중 문제가 발생하였습니다.");
				ex.printStackTrace();
			}
			driving = false;
			callStart = false;
			callRequest = false;
			nowLocationField.setText("");
			myCustomer = "";
			nowLocation = "";
			card.show(mainPanel, "firP");
		}
		
	}
	public class CancelButtonListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			callStart = false;
			callRequest = false;
			myCustomer = "";
			card.show(mainPanel, "firP");
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
						frame.setTitle(frameTitle + " : 로그인 하세요");
						logButton.setText("Login");
					} 
					else if(type == Message.MsgType.CALL_TAXI){
						if(callStart && !callRequest && !driving) {
							geocoding.geocoding(nowLocation);
							start = message.getStart();
							destination = message.getDestination();
							startField.setText(start);
							destinationField.setText(destination);
							if(geocoding.getDistance(message.getStart()) < 10000.0) {
								card.show(mainPanel, "callP");
								callRequest = true;
								callStart = false;
								myCustomer = message.getSender();
							}
						}
					}
					else if(type == Message.MsgType.CALL_CANCEL) {
						if(myCustomer.equals(message.getSender())) {
							card.show(mainPanel, "waitP");
							callRequest = false;
							callStart = true;
							myCustomer = "";
						}
					}
					else if(type == Message.MsgType.CALL_CANCEL2) {
						if(!user.equals(message.getSender())) {
							card.show(mainPanel, "waitP");
							callRequest = false;
							callStart = true;
							myCustomer = "";
						}
					}
					else if (type == Message.MsgType.NO_ACT){

						// 아무 액션이 필요없는 메시지. 그냥 스킵
					} else {
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
