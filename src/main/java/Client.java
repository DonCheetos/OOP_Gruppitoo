import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 1337);
             DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
             DataInputStream dataInputStream = new DataInputStream(socket.getInputStream())) {

            int numRequests = args.length / 2;
            dataOutputStream.writeInt(numRequests);

            for (int i = 0; i < args.length; i += 2) {
                String requestType = args[i];
                String requestData = args[i + 1];
                dataOutputStream.writeInt(requestType.equals("file") ? 2 : 1);
                dataOutputStream.writeUTF(requestData);
            }
            dataOutputStream.flush();

            for (int i = 0; i < numRequests; i++) {
                int statusCode = dataInputStream.readInt();
                if (statusCode == 0) {
                    if (args[i * 2].equals("file")) {
                        int fileSize = dataInputStream.readInt();
                        byte[] fileData = new byte[fileSize];
                        dataInputStream.readFully(fileData);
                        String fileName = args[i * 2 + 1];
                        try (FileOutputStream fileOutputStream = new FileOutputStream("received/" + fileName)) {
                            fileOutputStream.write(fileData);
                            System.out.println("Received file: " + fileName);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        String echoResponse = dataInputStream.readUTF();
                        System.out.println("Echo response: " + echoResponse);
                    }
                } else {
                    String error = dataInputStream.readUTF();
                    System.out.println("Error: " + error);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
