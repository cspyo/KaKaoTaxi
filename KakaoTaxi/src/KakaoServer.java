import java.awt.TrayIcon.MessageType;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;



public class KakaoServer {
	// 접속한 클라이언트의 사용자 이름과 출력 스트림을 해쉬 테이블에 보관
	// 나중에 특정 사용자에게 메시지를 보낼때 사용. 현재 접속해 있는 사용자의 전체 리스트를 구할때도 사용
	HashMap<String, ObjectOutputStream> customerOutputStreams =
			new HashMap<String, ObjectOutputStream>();
	HashMap<String, ObjectOutputStream> driverOutputStreams =
			new HashMap<String, ObjectOutputStream>();

	public static void main (String[] args) {
		new KakaoServer().go();
	}

	private void go () {
		try {
			ServerSocket serverSock = new ServerSocket(6000);	// 채팅을 위한 소켓 포트 5000 사용

			while(true) {
				Socket clientSocket = serverSock.accept();		// 새로운 클라이언트 접속 대기

				// 클라이언트를 위한 입출력 스트림 및 스레드 생성
				Thread t = new Thread(new ClientHandler(clientSocket));
				t.start();									
				System.out.println("S : 클라이언트 연결 됨");		// 상태를 보기위한 출력 메시지
			}
		} catch(Exception ex) {
			System.out.println("S : 클라이언트  연결 중 이상발생");	// 상태를 보기위한 출력  메시지
			ex.printStackTrace();
		}
	}

	// Client 와 1:1 대응하는 메시지 수신 스레드
	private class ClientHandler implements Runnable {
		Socket sock;					// 클라이언트 연결용 소켓
		ObjectInputStream reader;		// 클라이언트로 부터 수신하기 위한 스트림
		ObjectOutputStream writer;		// 클라이언트로 송신하기 위한 스트림

		// 구성자. 클라이언트와의 소켓에서 읽기와 쓰기 스트림 만들어 냄
		// 스트림을 만들때 InputStream을 먼저 만들면 Hang함. 그래서 OutputStream먼저 만들었음.
		// 이것은 클라이언트에서 InpitStreams을 먼저 만들기 때문임 안그러면 데드락
		public ClientHandler(Socket clientSocket) {
			try {
				sock = clientSocket;
				writer = new ObjectOutputStream(clientSocket.getOutputStream());
				reader = new ObjectInputStream(clientSocket.getInputStream());
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}

		// 클라이언트에서 받은 메시지에 따라 상응하는 작업을 수행
		public void run() {
			Message message;
			Message.MsgType type;
			try {
				while (true) {
					
					// 읽은 메시지의 종류에 따라 각각 할일이 정해져 있음
					message = (Message) reader.readObject();	  // 클라이언트의 전송 메시지 받음
					type = message.getType();
					if (type == Message.MsgType.DRIVER_LOGIN) {		  // 클라이언트 로그인 요청
						handleLogin(message.getSender(),writer,false);	  // 클아이언트 이름과 그에게 메시지를
						// 보낼 스트림을 등록
					}
					else if(type == Message.MsgType.DRIVER_LOGOUT) {
						handleLogout(message.getSender(),false);	  // 클아이언트 이름과 그에게 메시지를	
						writer.close(); reader.close(); sock.close(); // 이 클라이언트와 관련된 스트림들 닫기
						return;		
					}
					else if(type == Message.MsgType.CUSTOMER_LOGIN){
						handleLogin(message.getSender(), writer, true);
					}
					else if (type == Message.MsgType.CUSTOMER_LOGOUT) {	  // 클라이언트 로그아웃 요청
						handleLogout(message.getSender(),true);			  // 등록된 이름 및 이와 연결된 스트림 삭제
						writer.close(); reader.close(); sock.close(); // 이 클라이언트와 관련된 스트림들 닫기
						return;										  // 스레드 종료
					}
					else if (type == Message.MsgType.NO_ACT) {
						//  무시해도 되는 메시지
						continue;
					}
					else if(type == Message.MsgType.CALL_TAXI) {
						if(driverOutputStreams.isEmpty()) {
							toCustomer("", message.getSender(), new Message(Message.MsgType.CALL_FAIL));
						}
						else broadcastDriverMessage(new Message(Message.MsgType.CALL_TAXI,message.getSender(),"",message.getStart(),message.getDestination()));
					}
					else if(type == Message.MsgType.CALL_CANCEL) {
						broadcastDriverMessage(new Message(Message.MsgType.CALL_CANCEL,message.getSender()));
					}
					else if(type ==  Message.MsgType.CALL_ACCEPT) {
						toCustomer(message.getSender(), message.getReceiver(), message);
						broadcastDriverMessage(new Message(Message.MsgType.CALL_CANCEL2,message.getSender()));
					}
					else if(type ==  Message.MsgType.FINISH) {
						toCustomer(message.getSender(), message.getReceiver(), message);
					}
					else {
						// 정체가 확인되지 않는 이상한 메시지?
						throw new Exception("S : 클라이언트에서 알수 없는 메시지 도착했음");
					}
				}
			} catch(Exception ex) {
				System.out.println("S : 서버에서 메시지 전송 도중 오류 발생");				// 연결된 클라이언트 종료되면 예외발생
				// 이를 이용해 스레드 종료시킴
			}
		} // close run
	} // close inner class

	// 사용자 이름과 클라이언트로의 출력 스트림과 연관지어 해쉬 테이블에 넣어줌.
	// 이미 동일한 이름의 사용자가 있다면, 현재의 로그인은 실패 한것으로 클라이언트에게 알림
	// 그리고 새로운 접속자 리스트를 모든 접속자에게 보내줌
	// 해쉬 테이블의 접근에서는 경쟁조건 생기면 곤란 (not Thread-Safe. Synchronized로 상호배제 함.
	private synchronized void handleLogin(String user, ObjectOutputStream writer, boolean customer) {
		if(customer) {
			try {
				if (customerOutputStreams.containsKey(user)) {
					writer.writeObject(
							new Message(Message.MsgType.LOGIN_FAILURE));
					return;
				}
			} catch (Exception ex) {
				System.out.println("S : 서버에서 송신 중 이상 발생");
				ex.printStackTrace();
			}
			customerOutputStreams.put(user, writer);
		}
		else {
			try {
				if (driverOutputStreams.containsKey(user)) {
					writer.writeObject(
							new Message(Message.MsgType.LOGIN_FAILURE));
					return;
				}
			} catch (Exception ex) {
				System.out.println("S : 서버에서 송신 중 이상 발생");
				ex.printStackTrace();
			}
			driverOutputStreams.put(user, writer);
		}
	}  // close handleLogin

	// 주어진 사용자를 해쉬테이블에서 제거 (출력 스트림도 제거)
	// 그리고 업데이트된 접속자 리스트를 모든 접속자에게 보내줌
	private synchronized void handleLogout(String user,boolean customer) {
		if(customer) {
			customerOutputStreams.remove(user);
		}
		else {
			driverOutputStreams.remove(user);
		}
	}  // close handleLogout

	private synchronized void toDriver(String sender, String receiver, String contents) {
		
	}
	private synchronized void toCustomer(String sender, String receiver, Message m) {
		ObjectOutputStream write = customerOutputStreams.get(receiver);
		try {
			write.writeObject(new Message(m.getType(),sender,receiver));
		} catch (Exception ex) {
			System.out.println("S : 서버에서 송신 중 이상 발생");
			ex.printStackTrace();
		}
	}
	
	
	// 해쉬맵에 있는 모든 접속자들에게 주어진 메시지를 보내는 메소드.
	// 반드시 synchronized 된 메소드에서만 호출하기로 함
	private synchronized void broadcastCustomerMessage(Message message) {
		Set<String> s = customerOutputStreams.keySet();	// 먼저 등록된 사용자들을 추출하고 하나하나에 메시지 보냄
		// 그러기 위해서 먼저 사용자 리스트만 추출
		Iterator<String> it = s.iterator();
		String user;
		while(it.hasNext()) {
			user = it.next();
			try {
				ObjectOutputStream writer = customerOutputStreams.get(user);	// 대상 사용자와의 스트림 추출
				writer.writeObject(message);									// 그 스트림에 출력
				writer.flush();
			} catch(Exception ex) {
				System.out.println("S : 서버에서 송신 중 이상 발생");
				ex.printStackTrace();
			}
		} 
	}	
	public synchronized void broadcastDriverMessage(Message message) {
		
		Set<String> s = driverOutputStreams.keySet();	// 먼저 등록된 사용자들을 추출하고 하나하나에 메시지 보냄
		// 그러기 위해서 먼저 사용자 리스트만 추출
		Iterator<String> it = s.iterator();
		String user;
		while(it.hasNext()) {
			user = it.next();
			try {
				System.out.println(3);
				ObjectOutputStream writer = driverOutputStreams.get(user);	// 대상 사용자와의 스트림 추출
				writer.writeObject(message);									// 그 스트림에 출력
				writer.flush();
			} catch(Exception ex) {
				System.out.println("S : 서버에서 송신 중 이상 발생");
				ex.printStackTrace();
			}
		} // end while	   
	}	// end broadcastMessage


	//손님과 기사 리스트 만들기
	private String makeCustomerList() {
		Set<String> s = customerOutputStreams.keySet();	// 먼저 등록된 사용자들을 추출
		Iterator<String> it = s.iterator();
		String userList = "";
		while(it.hasNext()) {
			userList += it.next() + "/";					// 스트링 리스트에 추가하고 구분자 명시
		} // end while
		return userList;									 
	}	// makeClientList
	private String makeDriverList() {
		Set<String> s = driverOutputStreams.keySet();	// 먼저 등록된 사용자들을 추출
		Iterator<String> it = s.iterator();
		String userList = "";
		while(it.hasNext()) {
			userList += it.next() + "/";					// 스트링 리스트에 추가하고 구분자 명시
		} // end while
		return userList;									 
	}	// makeClientList
}
