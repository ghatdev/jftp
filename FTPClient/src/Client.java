import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        int port = 2020;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        FTPClient ftpClient = new FTPClient("127.0.0.1", port);
        Scanner scanner = new Scanner(System.in);

        loop:
        while(true) {
            String cmd = scanner.nextLine();
            String[] cmds = cmd.split(" ");

            switch (cmds[0]) {
                case "QUIT":
                    ftpClient.quitConn();
                    break loop;
                case "LIST":
                    ftpClient.cmdList(cmds[1]);
                    break;
                case "GET":
                    ftpClient.cmdGet(cmds[1]);
                    break;
                case "PUT":
                    ftpClient.cmdPut(cmds[1]);
                    break;
                case "CD":
                    ftpClient.cmdCd(cmds[1]);
                    break;
            }
        }
    }
}
