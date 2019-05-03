import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;

public class Receiver extends Thread{
	DatagramSocket mySocket;
	public Receiver() throws IOException {
		mySocket = new DatagramSocket(9876);
		this.start();
		System.out.println("Reciever");
		
	}
	public void run() {

		Thread receive =new Thread(new Runnable() {
			@Override
			public void run(){
				while (!mySocket.isClosed()) {
					try {
						System.out.println("I am waiting for Requests...");
						byte[] incomingData = new byte[1024];
						DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
						mySocket.receive(incomingPacket);

						byte[] data = incomingPacket.getData();
						ByteArrayInputStream BAIS = new ByteArrayInputStream(data);
						ObjectInputStream OIS = new ObjectInputStream(BAIS);

						FileDownloading CR = (FileDownloading) OIS.readObject();

						//System.out.println("CLient Requests is recived...");
						clientRequestParsing(CR);
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
		});

		
		receive.start();
//		Thread send=new Thread(new Runnable() {
//			@Override
//			public void run() {
//				while (!mySocket.isClosed()) {
//
//				}
//			}
//		});

	}

	private String clientRequestParsing(FileDownloading CR) throws IOException {
		String Directory="/home/mhamza/workspace/Lab8/Files";
		if(CR.getReponse()==0) {
			//System.out.println("List is Sending start...");
			InetAddress IPAddress = InetAddress.getByName("localhost");
			ListFiles list = new ListFiles(Directory);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(outputStream);
			//System.out.println(list.getReponse()+" "+list.getFilesInfo()+" "+list.getName());
			os.writeObject(list);
			byte[] data = outputStream.toByteArray();
			DatagramPacket sendlist = new DatagramPacket(data, data.length, IPAddress, 9877);
			mySocket.send(sendlist);
		}
		else if(CR.getReponse()==1) {			
			//System.out.println("FileSize is Sending start...");
			FileSize sizefile = new FileSize(Directory,CR.getName());
			InetAddress IPAddress = InetAddress.getByName("localhost");
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(outputStream);
			os.writeObject(sizefile);
			byte[] data = outputStream.toByteArray();
			DatagramPacket sendlist = new DatagramPacket(data, data.length, IPAddress, 9877);
			mySocket.send(sendlist);
			sendPackets(Directory+"/"+CR.getName());
		}
		return "";
	}

	@SuppressWarnings("resource")
	public boolean sendPackets(String fname) throws IOException {
		File file = new File(fname);
		FileInputStream fin = null;
		System.out.println(fname);
		// create FileInputStream object
		fin = new FileInputStream(file);
		byte fileContent[] = new byte[(int)file.length()];
		int division=(fileContent.length)/100;
		int mode=(fileContent.length)%100;
		byte[] content=new byte[100];
		fin.read(fileContent);
		int i=0;
		
		
		
		for ( ; i < division; i++) {
			for (int j = 0; j < 100; j++) {
				content[j]=fileContent[(i*100)+j];
			}
			
			//System.out.println("FileSize is Sending start...");
			SendFile sendFileData=new SendFile((short)12,(short)i, content);
			InetAddress IPAddress = InetAddress.getByName("localhost");
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(outputStream);
			//System.out.println(list.getReponse()+" "+list.getFilesInfo()+" "+list.getName());
			os.writeObject(sendFileData);
			byte[] data = outputStream.toByteArray();
			DatagramPacket sendlist = new DatagramPacket(data, data.length, IPAddress, 9877);
			mySocket.send(sendlist);
			
		}
		byte[] content1=new byte[mode];
		for (int j = 0; j <mode; j++) {
			content1[j]=fileContent[(i*100)+j];
		}
		//System.out.println("FileSize is Sending start...");
		SendFile sendFileData=new SendFile((short)12,(short)i, content1);
		InetAddress IPAddress = InetAddress.getByName("localhost");
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(outputStream);
		System.out.println(sendFileData.getReponse()+" "+sendFileData.getFilesInfo()+" "+sendFileData.getName());
		os.writeObject(sendFileData);
		byte[] data = outputStream.toByteArray();
		DatagramPacket sendlist = new DatagramPacket(data, data.length, IPAddress, 9877);
		mySocket.send(sendlist);
		//System.out.println();
		System.out.println("File Meta Data is sent...");
		return true;
	}

	public static void main(String args[]) throws IOException{
		@SuppressWarnings("unused")
		Receiver obj=new Receiver();
	}
}
