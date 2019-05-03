import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Scanner;

public class Sender extends Thread{
	DatagramSocket mySocket;
	Thread send,receive;
	public String menu() {
		System.out.println("Type List for ListOfFiles");
		System.out.println("Type Select File Name That you want to Download");
		System.out.println("Tpe quit for Close the Connection" );
		System.out.print("Type Option:");
		@SuppressWarnings("resource")
		Scanner input=new Scanner(System.in);
		return input.next();
	}
	public Sender()  throws Exception{
		mySocket = new DatagramSocket(9877);
		this.start();
	}

	public void run() {

		//		receive =new Thread(new Runnable() {
		//			@Override
		//			public void run(){
		//				while (!mySocket.isClosed()) {
		//					System.out.println("Waiting for response...");
		//					try {
		//						byte[] incomingData = new byte[1024];
		//						DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
		//						mySocket.receive(incomingPacket);
		//						byte[] data = incomingPacket.getData();
		//						ByteArrayInputStream BAIS = new ByteArrayInputStream(data);
		//						ObjectInputStream OIS = new ObjectInputStream(BAIS);
		//						FileDownloading fileInfo = (FileDownloading) OIS.readObject();
		//						System.out.println(fileInfo.getReponse()+ " "+fileInfo.getFilesInfo()+" "+ fileInfo.getName());
		//						getInfoObject(fileInfo);
		//					} catch (ClassNotFoundException e) {
		//						e.printStackTrace();
		//					} catch (IOException e) {
		//						// TODO Auto-generated catch block
		//						e.printStackTrace();
		//					}
		//					System.out.println("File is info is recieved...");
		//					send.start();
		//				}
		//			}
		//		});
		//		
		//		

		send=new Thread(new Runnable() {
			@Override
			public void run(){
				String str=null;
				while (!mySocket.isClosed()) {
					//mySocket = new DatagramSocket();
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					try {
						InetAddress IPAddress = InetAddress.getByName("localhost");
						ObjectOutputStream OOS = new ObjectOutputStream(outputStream);
						str=menu();
						if(str.equals("list") || str.equals("List")) {
							OOS.writeObject(new ClientRequest((short)0,"")); // create an object	
						}
						else if(str.equals("Download") || str.equals("download")) {
							OOS.writeObject(new ClientRequest((short)1,"file1"));
						}
						else {
							break;
						}
						byte[] data = outputStream.toByteArray();
						DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, 9876);
						mySocket.send(sendPacket);

					} catch (UnknownHostException e2) {
						e2.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//					System.out.println("client request is sent...");
					//					System.out.println("Waiting for response...");
					try {
						byte[] incomingData = new byte[1024];
						DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
						mySocket.receive(incomingPacket);
						byte[] data = incomingPacket.getData();
						ByteArrayInputStream BAIS = new ByteArrayInputStream(data);
						ObjectInputStream OIS = new ObjectInputStream(BAIS);
						FileDownloading fileInfo = (FileDownloading) OIS.readObject();
						//System.out.println(fileInfo.getReponse()+ " "+fileInfo.getFilesInfo()+" "+ fileInfo.getName());
						getInfoObject(fileInfo);
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//System.out.println("File is info is recieved...");			
				}
				mySocket.close();
			}
		});
		send.start();
		//receive.start();

	}

	private void CollectFileData(short SIZE) throws IOException, ClassNotFoundException {
		System.out.println("\nThis is the File Data...");
		for (int i = 0; i < (SIZE/100); i++) {
			try {
				byte[] incomingData = new byte[1024];
				DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
				mySocket.receive(incomingPacket);
				byte[] data = incomingPacket.getData();
				ByteArrayInputStream BAIS = new ByteArrayInputStream(data);
				ObjectInputStream OIS = new ObjectInputStream(BAIS);
				FileDownloading fileInfo = (FileDownloading) OIS.readObject();
				getInfoObject(fileInfo);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		byte[] incomingData = new byte[1024];
		DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
		mySocket.receive(incomingPacket);
		byte[] data = incomingPacket.getData();
		ByteArrayInputStream BAIS = new ByteArrayInputStream(data);
		ObjectInputStream OIS = new ObjectInputStream(BAIS);
		FileDownloading fileInfo = (FileDownloading) OIS.readObject();
		getInfoObject(fileInfo);

		System.out.println("File is Downloads...");
	}

	private void getInfoObject(FileDownloading obj) throws ClassNotFoundException, IOException {
		if(obj.getReponse()==10) {
			System.err.println(obj.getReponse()+ " "+obj.getFilesInfo()+" "+ obj.getName());
		}
		else if(obj.getReponse()==11) {
			System.err.println(obj.getReponse()+" "+ obj.getName()+ " "+obj.getFilesInfo());
			CollectFileData(obj.getFilesInfo());
		}
		else if(obj.getReponse()==12) {
			System.err.print(obj.getName());
			File TextFile = new File("OutputData.txt");
			FileWriter fw = new FileWriter(TextFile,true);
			fw.write(obj.getName());
			fw.close();
		}

	}
	public static void main(String[] args) throws Exception {
		@SuppressWarnings("unused")
		Sender obj=new Sender();
	}
}