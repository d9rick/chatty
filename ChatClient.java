import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ChatClient extends Thread{
    private BufferedReader chats;
    private PrintWriter output;
    private Scanner scan;
    private Socket sock;

    // initialize and run the chat client
    ChatClient(String hostname, int port, String username) {
        try {
            // connect to the server
            System.out.println("Attempting to connect to the chat server...");
            sock = new Socket(hostname, port);
            System.out.println("Connected to " + hostname + " on port " + port);
            
            // initialize io
            chats = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            output = new PrintWriter(sock.getOutputStream());

            // start the program
            this.start();

            // read in input from the iostream and send to server
            scan = new Scanner(System.in);
            while(sock.isConnected()) {
                String line = scan.nextLine();
                output.println(username + ": " + line);
                output.flush();
            }

        } catch (UnknownHostException uhe) {
            System.out.println("Could not find host! " + uhe.getMessage());
        } catch (IOException ioe) {
            System.out.println("! IOException on client creation !\n" + ioe.getMessage());
        }
        finally {
            // close open readers
            if (scan != null) {
                scan.close();
            }
            if (chats != null) {
                try {
                    chats.close();
                } catch (IOException ioe) {
                    System.out.println("Error closing chats. " + ioe.getMessage());
                }
            }
            if (output != null) {
                output.close();
            }
            if (sock != null) {
                try {
                    sock.close();
                } catch (IOException ioe) {
                    System.out.println("Error closing socket. " + ioe.getMessage());
                }
            }
        }
    }

    @Override
    public void run() {
        try {
            // read in data from the server as it comes, and print it out
            while(sock.isConnected()) {
                String line = chats.readLine();
                System.out.println("\n" + line);
            }
            System.out.println("Disconnected!");
        } catch (IOException ioe) {
            System.out.println("IOException in ChatClient.run(): " + ioe.getMessage());
        }
    }

    public static void main(String args[]) {
        // check for args
        if (args.length < 3) {
            System.out.println("Please provide arguments like so:");
            System.out.println("\t{hostname} {port} {username}");
            return;
        }

        // get args and initialize the client
        String hostname = args[0];
        int port;
        try {
            port = Integer.parseInt(args[1]);
        } catch(NumberFormatException nfe) {
            System.out.println("Error in parsing the port number. " + nfe.getMessage());
            return;
        }
        
        String username = args[2];

        @SuppressWarnings("unused")
        ChatClient client = new ChatClient(hostname, port, username);
    }
}