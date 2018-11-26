// TODO: Exception handling

public class FTPServer {
    public static void main(String[] args) {
        int port = 2020;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        Server ftpServer = new Server(port);  // Default 127.0.0.1:2020
        ftpServer.listen();                         // endless loop for handling
    }

}
