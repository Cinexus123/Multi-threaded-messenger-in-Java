import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.*;

public class Server {

    private Thread t1;
    private Thread t2;
    public static ServerSocket server;
    private SocketAddress socketAddress;
    Boolean flag = true;
    List<SocketInformation> bytesMessage = new ArrayList<SocketInformation>();
    List<SocketInformation> addPendingInformation = new ArrayList<SocketInformation>();
    int rower = 0;

    public Server() throws IOException {
        server = new ServerSocket(8080);
        socketAddress=new InetSocketAddress("127.0.0.1", 8080);
    }

    public void OnStart() throws IOException {

        t1 = new Thread(){
            @Override
            public void run() {
                try {
                    while(flag)
                    {
                        GenerateNewUserConnection();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        t1.start();
    }

    public void GenerateNewUserConnection() throws IOException {

        Socket socket = server.accept();
        t2 = new Thread(){
            @Override
            public void run() {
                int countIfSocketClose = 0;
                while (flag)
                {
                    if (countIfSocketClose > 0)
                    {
                        return;
                    }
                    countIfSocketClose++;
                    try {
                        MainConversationLogic(socket);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        t2.start();
    }

    public void MainConversationLogic (Socket connection) throws IOException, InterruptedException {
        SocketInformation socketInfo = null;
        SocketInformation checkValueObject = null;
        SocketInformation setSocketInClass = null;
        String answer = "";
        int checkObject = 0;
        InputStream is = connection.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);


        while(true)
        {
            answer = br.readLine();
            if(answer.length() == 0)
            {
                FindReceiverFromStack(socketInfo, answer.length(), answer);
                return;
            }
            String[] words = answer.split("@");
            socketInfo = checkIfUserIsBlocked(socketInfo, connection, words);
            setSocketInClass = socketInfo;

            if (checkObject >= 1 && ((!checkValueObject.getUser().equals(setSocketInClass.getUser()))))
            {
                String changeUser = "error";
                FindReceiverFromStack(checkValueObject, answer.length(), changeUser);
                return;
            }
            else
            {
                checkValueObject = setSocketInClass;
                checkObject++;
            }

            FindReceiverFromStack(setSocketInClass, answer.length(), answer);
        }
    }

    private Socket FindReceiverFromStack(SocketInformation frameObject1, int answerLength, String answer ) throws InterruptedException, IOException {
        synchronized(this) {
            Socket returnSocket = null;
            if (answerLength == 0) {
                for (var element : bytesMessage) {
                    if (element.getUser().equals(frameObject1.getUser())) {
                        element.setBlocked(true);
                    }
                }
                return null;
            }
            returnSocket = giveSocketToCommunication(frameObject1);

            if (returnSocket != null) {
                OutputStream os = returnSocket.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os);
                BufferedWriter bw = new BufferedWriter(osw);
                bw.write(answer);
                bw.newLine();
                bw.flush();
            } else {
                addPendingInformation.add(frameObject1);
            }
            return null;
        }

    }

    private Socket giveSocketToCommunication(SocketInformation frameObject1) throws InterruptedException, IOException {
        Boolean objectExist = false;
        Socket sendToUser = null;

        if (!bytesMessage.isEmpty())
        {
            for (var element : bytesMessage)
            {
                if (element.getUser().equals(frameObject1.getUser()))
                {
                    objectExist = true;
                    CheckIfUserHasIncomingMessage(frameObject1);
                    break;
                }
            }
            if (!objectExist)
            {
                bytesMessage.add(frameObject1);
                CheckIfUserHasIncomingMessage(frameObject1);
            }

            for (var element : bytesMessage)
            {
                if (element.getUser().equals(frameObject1.getTargetUser()) && element.getBlocked() == false)
                {
                    sendToUser = element.getSocketInfo();
                }
            }
        }
        else
        {
            bytesMessage.add(frameObject1);
        }

        return sendToUser;
    }

    private void CheckIfUserHasIncomingMessage(SocketInformation frameObject) throws InterruptedException, IOException {
        synchronized(this) {
            if (addPendingInformation.isEmpty() || bytesMessage.isEmpty()) {
                return;
            }

            SocketInformation[] findAndRemoveElement = new SocketInformation[addPendingInformation.size()];
            int counter = 0;
            for (var element1 : addPendingInformation) {
                if (element1.getTargetUser().equals(frameObject.getUser()) && frameObject.getBlocked() == false) {
                    Socket findActiveUser = frameObject.getSocketInfo();
                    String buffer2 = element1.getUser() + "@" + element1.getMessage() + "@" + element1.getTime();
                    Thread.sleep(50);

                    OutputStream os = findActiveUser.getOutputStream();
                    OutputStreamWriter osw = new OutputStreamWriter(os);
                    BufferedWriter bw = new BufferedWriter(osw);
                    bw.write(buffer2);
                    bw.newLine();
                    bw.flush();
                    findAndRemoveElement[counter] = element1;
                    counter++;
                }
            }

            for (int i = 0; i < findAndRemoveElement.length; i++) {
                addPendingInformation.remove(findAndRemoveElement[i]);
            }
        }
    }
    private SocketInformation checkIfUserIsBlocked(SocketInformation socketInfo, Socket connection, String[] words)
    {
        synchronized(this) {
            socketInfo = new SocketInformation();
            socketInfo.setUser(words[0]);
            socketInfo.setMessage(words[1]);
            socketInfo.setTargetUser(words[2]);
            socketInfo.setTime(words[3]);
            socketInfo.setBlocked(false);
            socketInfo.setSocketInfo(connection);

            SocketInformation changeUser = null;
            for (var element : bytesMessage) {
                if (element.getUser().equals(socketInfo.getUser()) && element.getBlocked() == true) {
                    element.setSocketInfo(socketInfo.getSocketInfo());
                    element.setTargetUser(socketInfo.getTargetUser());
                    element.setMessage(socketInfo.getMessage());
                    element.setBlocked(false);
                    element.setTime(socketInfo.getTime());
                    changeUser = element;
                    return changeUser;
                }
            }
            return socketInfo;
        }
    }
    protected void OnStop()
    {
        flag = false;
    }

    public static void main(String[]args) throws IOException {
        Server server = new Server();
        server.OnStart();

    }

    public class SocketInformation
    {
        private String targetUser;
        private String user;
        private String message;
        private Boolean isBlocked;
        private Socket socketInfo;
        private String time;

        public String getTargetUser() {
            return targetUser;
        }

        public void setTargetUser(String targetUser) {
            this.targetUser = targetUser;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Boolean getBlocked() {
            return isBlocked;
        }

        public void setBlocked(Boolean blocked) {
            isBlocked = blocked;
        }

        public Socket getSocketInfo() {
            return socketInfo;
        }

        public void setSocketInfo(Socket socketInfo) {
            this.socketInfo = socketInfo;
        }

        public SocketInformation()
        {

        }
    }
}
