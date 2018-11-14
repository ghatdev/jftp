/***
 * server.Handler class
 * FTPServer에서 socket연결 요청을 accept한 후에 accept 한 소켓을 받아 실제 FTP 클라이언트와의 상호작용을 시작하는 클래스
 * server.Handler(Accepted socket) 으로 생성한 후 run 메소드를 호출하면 된다.
 */

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.NotDirectoryException;
import java.nio.file.Paths;
import java.util.Arrays;

// TODO: Implement commands
// TODO: Handle exceptions
public class Handler implements Runnable {
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;

    private String curDir;

    /**
     * accept socket and handle commands
     * @param socket connected socket.
     */
    public Handler(Socket socket) {
        this.socket = socket;

        this.curDir = new File(".").getAbsolutePath();

        try {
            this.outputStream = socket.getOutputStream();
            this.inputStream = socket.getInputStream();
        }
        catch (IOException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run() {
        while(true) {
            if (parseCmd()<0) {
                return;
            }
        }
    }


    /**
     * write byte buffer to output stream.
     * wrapped method.
     * @param os output stream to write buffer
     * @param buffer buffer to write
     * @return -1 when failed, written size when succeed
     */
    private int write(OutputStream os, byte[] buffer, String statusCode, String type) {
        ByteBuffer bf = ByteBuffer.allocate(12+buffer.length);

        bf.put(Arrays.copyOf(type.getBytes(), 4));
        bf.put(Arrays.copyOf(statusCode.getBytes(), 4));
        bf.put(Arrays.copyOf(String.valueOf(buffer.length).getBytes(), 4));
        bf.put(buffer);

        try {
            os.write(bf.array());
            os.flush();
        }
        catch (IOException e) {
            return -1;
        }

        return buffer.length;
    }

    private int writeToClient(byte[] buffer, String statusCode, String type) {
        write(outputStream, buffer, statusCode, type);

        return 0;
    }

    private int writeToClient(String buffer, String statusCode, String type) {
        write(outputStream, buffer.getBytes(), statusCode, type);

        return 0;
    }

    /**
     * Wrapper function to read input stream to byte array.
     * @param dst destination buffer
     * @return read size. -1 means error.
     */
    private int readToBuf(byte[] dst) {
        int result;

        try {
            result = inputStream.read(dst);
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }

        return result;
    }

    private String getFileList(String path) throws FileNotFoundException, NotDirectoryException {
        File dir = new File(path);
        File[] files = dir.listFiles();

        if (!dir.exists()) {
            throw new FileNotFoundException();
        }

        if (!dir.isDirectory()) {
            throw new NotDirectoryException("Not a directory");
        }

        String out = "";

        for (File f:files) {
            out +=  f.getName();
            out += "\n";
        }

        return out;
    }

    /**
     * Parse buffer and run command.
     */
    private int parseCmd() {
        byte[] header = new byte[8];
        try {
            inputStream.read(header, 0, 8);
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }


        String cmd = new String(Arrays.copyOfRange(header, 0, 4)).trim();
        //cmd = cmd.replaceAll("\\s+", "");
        int argSize = -1;
        try {
            argSize = Integer.parseInt(new String(ByteBuffer.wrap(Arrays.copyOfRange(header, 4,8)).array()).trim());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return -1;
        }


        byte[] dataBuffer = new byte[argSize];
        try {
            inputStream.read(dataBuffer, 0, argSize);
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }

        switch (cmd) {
            case "PUT":
                processPutFile(dataBuffer);
                break;
            case "LIST":
                String arg = new String(dataBuffer);
                processList(arg);
                break;
            case "GET":
                processGetFile(dataBuffer);
                break;
            case "CD":
                processChDir(dataBuffer);
                break;
            case "QUIT":
                return -1;
            default: // CASE: ?

        }

        return 0;
    }

    private void processChDir(byte[] buffer) {
        String path = new String(buffer).trim();
        String result = curDir;
        if (path.equals("..")) {
            File f = new File(curDir);
            curDir = f.getParent();
        } else {
            File f = new File(path);
            if (f.exists()) {
                if (!f.isDirectory()) {
                    writeToClient("Not directory\n", "400", "ERR");
                    return;
                }
                curDir = f.getAbsolutePath();
            } else {
                writeToClient("Dir not exists\n", "400", "ERR");
                return;
            }
        }

        writeToClient(curDir, "200", "RST");
    }

    private void processPutFile(byte[] buffer) {
        String filename = new String(Arrays.copyOfRange(buffer, 0, 255)).trim();

        FileOutputStream fileOutputStream;
        File f = new File(Paths.get(this.curDir, filename).toString());
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        try {
            fileOutputStream = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            // WTF?
            return;
        }

        try {
            fileOutputStream.write(Arrays.copyOfRange(buffer,255, buffer.length));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void processGetFile(byte[] buffer) {
        String filename = new String(Arrays.copyOfRange(buffer, 0, 255)).trim();

        File f = new File(Paths.get(this.curDir, filename).toString());
        if (!f.exists()) {
            writeToClient("", "404", "ERR");
            return;
        }

        FileInputStream fileInputStream = null;

        try {
            fileInputStream = new FileInputStream(f);

        } catch (FileNotFoundException e) {
            // WTF?
        }

        byte[] out = new byte[(int)f.length()];
        try {
            fileInputStream.read(out);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteBuffer bf = ByteBuffer.allocate(255 + (int)f.length());
        bf.put(Arrays.copyOf(f.getName().getBytes(), 255));
        bf.put(out);

        writeToClient(bf.array(), "200", "FILE");
    }

    private void processList(String arg) {
        String result = "";
        String path = "";
        if (arg.equals(".")) {
            path = curDir;
        } else if (arg.equals("..")) {
            path = Paths.get(curDir, "..").toAbsolutePath().toString();
        } else {
            path = arg;
        }

        try {
            result = getFileList(path);
        } catch (FileNotFoundException fe) {
            writeToClient("", "404", "ERR");
        } catch (NotDirectoryException nde) {
            writeToClient("", "400", "ERR");
        }
        writeToClient(result, "200", "STR");
    }

}
