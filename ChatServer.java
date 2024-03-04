import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class ChatServer {
    private ServerSocket server;
    private Vector<ClientThread> clients;

    private class ClientThread extends Thread {
        private ChatServer server;
        private BufferedReader input;
        private PrintWriter output;

        // initializes member variables to ready client for sending messages
        ClientThread(Socket sock, ChatServer server) {
            this.server = server;

            // initialize the readers & writers with the sock streams
            try {
                input  = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                output = new PrintWriter(sock.getOutputStream());

                // STARTIN THE THREADDDD
                this.start();

            } catch(IOException ioe) {
                System.out.println("! Error in initializing the client. ! \nError Message: " + ioe.getMessage());
            }
        }

        // literally just wait for a string to be sent... and then send it lols
        public void run() {
            while(true) {
                try {
                    String message = input.readLine();
                    server.broadcast(message, this);
                } catch(IOException ioe) {
                    System.out.println("IOE while running clientThread: " + ioe.getMessage());
                }
            }
        }

        // prints the receieved message
        public void sendMessage(String message) {
            output.println(message);
            output.flush();
        }
    }


    ChatServer(int port) {
        try {
            // open serversocket on the provided port
            System.out.println("Binding to the port: " + port);
            this.server = new ServerSocket(port);
            System.out.println("Successfully bound to port!");

            // initialize list of clients
            clients = new Vector<ClientThread>();

        } catch(IOException ioe) {
            System.out.println("! Error creating server. Terminating. !\n Error Message: " + ioe.getMessage());
            return;
        }
    }

    public void start() {
        // get all connections
        while(true) {
            try {
                // accept incoming client,
                Socket sock = server.accept(); // blocking

                // then print their address,
                InetAddress clientAddress = sock.getInetAddress();
                System.out.println("Incoming connection from " + clientAddress);

                // and create a thread to send stuff to
                ClientThread client = new ClientThread(sock, this); // automatically started in constructor 
                clients.add(client);

            } catch (IOException ioe) {
                System.out.println("Error in accepting client." + ioe.getMessage());
                continue;
            }
        }
    }

    // broadcasts a message to all connected clients
    public void broadcast(String message, ClientThread client) {
        // ignore empty messages
        if(message == null) {
            return;
        }

        // send the message to all clients that arent the one who sent the message
        for(ClientThread thread : clients) {
            // do not send message to the client that sent the message
            if(thread == client) {
                continue;
            }

            thread.sendMessage(message);
        }

        // log the messages to standard out
        System.out.println(message);
    }

    public static void main(String args[]) {
        // check if port number was provided
        if(args.length != 1) {
            System.out.println("Please provide a port number!");
        }

        // start the server
        try{
            int port = Integer.parseInt(args[0]);
            System.out.println("Starting chat server on port: " + port);
            ChatServer server = new ChatServer(port);
            server.start();

        } catch (NumberFormatException nfe) {
            System.out.println("Error parsing the port: " + nfe.getMessage());
        }
    }
}