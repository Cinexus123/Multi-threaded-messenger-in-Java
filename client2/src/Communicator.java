import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class Communicator implements ActionListener {

    private Thread t1;
    private Boolean flag = true;
    private JFrame f;
    private JFrame frameMessage = new JFrame("Error");
    private JPanel p = new JPanel();
    private JButton connectionStart = new JButton("Start");
    private JButton connectionStop = new JButton("Stop");
    private JButton sendInfo = new JButton("Send");
    private JButton clearTable = new JButton("Clear");
    private JTextField user = new JTextField("");
    private JTextField message = new JTextField("");
    private JTextField targetUser = new JTextField("");
    DefaultListModel<String> model = new DefaultListModel<>();
    private JList<String> list = new JList<>(model);
    DefaultListModel<String> model1 = new DefaultListModel<>();
    private JList<String> list1 = new JList<>(model1);
    private  JLabel l1 = new JLabel("Client 2");

    private SocketAddress socketAddress;
    private Socket socketClient;
    private OutputStream os;
    private OutputStreamWriter osw;
    private BufferedWriter bw;

    String frameToSend = "";
    String sendMessage;
    String[] words;
    String messageBack = "";
    String substringDate = "";

    public Communicator() {

        socketClient = new Socket();
        socketAddress=new InetSocketAddress("127.0.0.1", 8080);
        gui();
    }

    private void gui() {
        connectionStart.setLayout(null);
        connectionStop.setLayout(null);
        sendInfo.setLayout(null);
        clearTable.setLayout(null);
        user.setLayout(null);
        f = new JFrame("Panel");
        f.setVisible(true);
        f.setSize(800,600);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        p.setLayout(null);
        sendInfo.setBounds(140,250,100,30);
        clearTable.setBounds(140,300,100,30);
        connectionStart.setBounds(20,350,100,30);
        connectionStop.setBounds(20,400,100,30);
        user.setBounds(250,100, 100,30);
        message.setBounds(250,150, 100,30);
        targetUser.setBounds(250,200, 100,30);
        list.setBounds(400,100, 150,300);
        list1.setBounds(600,100, 150,300);
        l1.setBounds(50,50, 100,30);

        f.add(l1);
        p.add(connectionStart);
        p.add(connectionStop);
        p.add(sendInfo);
        p.add(clearTable);
        p.add(user);
        p.add(message);
        p.add(targetUser);
        p.add(list);
        p.add(list1);
        f.add(p);

        sendInfo.addActionListener(this);
        clearTable.addActionListener(this);
        connectionStart.addActionListener(this);
        connectionStop.addActionListener(this);

        f.addWindowListener(new WindowListener() {

            @Override
            public void windowOpened(WindowEvent e) {
                t1 = new Thread(() -> {
                    while(flag)
                    {
                        try {
                            ReceiveInformation();
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    }

                });
                t1.start();
            }

            @Override
            public void windowClosing(WindowEvent e) {

            }

            @Override
            public void windowClosed(WindowEvent e) {

            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }


        });

    }

    public void SendMessage() {


        try {
                String infoTextBoxMessage = message.getText();

                String user1 = user.getText();
                String targetUser1 = targetUser.getText();
                Instant timestamp = Instant                // Represent a moment in UTC.
                        .now()                 // Capture the current moment. Returns a `Instant` object.
                        .truncatedTo(          // Lop off the finer part of this moment.
                                ChronoUnit.MICROS  // Granularity to which we are truncating.
                        );
                String timestamp1 = timestamp.toString();
                frameToSend = user1 + "@" + infoTextBoxMessage + "@" + targetUser1 + "@" + timestamp1;

                if (!socketClient.isConnected()) {
                    JOptionPane.showMessageDialog(frameMessage, "Please start connection");
                    return;
                }

                sendMessage = frameToSend;
                bw.write(sendMessage);
                bw.newLine();
                bw.flush();

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

    }

    public static void main(String[] args) {
        new Communicator();

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if(source == sendInfo)
        {
            SendMessage();
        }
        if(source == clearTable)
        {
            ClearTable();
        }
        if(source == connectionStart)
        {
            try {
                ConnectionStart();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        if(source == connectionStop)
        {
            try {
                ConnectionStop();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    private void ConnectionStop() throws IOException {
        if(socketClient.isConnected())
        {
            socketClient.close();
        }
    }

    private void ConnectionStart() throws IOException {

        if(!socketClient.isConnected())
        {
            socketClient = new Socket("127.0.0.1",8080);
            os = socketClient.getOutputStream();
            osw = new OutputStreamWriter(os);
            bw = new BufferedWriter(osw);
        }
        else
        {
            JOptionPane.showMessageDialog(frameMessage,"You are already connected");
        }

    }

    private void ClearTable() {
        model.removeAllElements();
    }

    private void ReceiveInformation() throws IOException {
        String receivedMessage = "";
        InputStream is = socketClient.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        try
        {
            if (socketClient.isConnected())
            {

                receivedMessage = br.readLine();

                if (receivedMessage.length() == 0)
                {
                    socketClient.close();
                    return;
                }


                if (receivedMessage.equals("error"))
                {
                    JOptionPane.showMessageDialog(frameMessage,"Please connect again before changing user. This message will not be sent");
                    return;
                }

                words = receivedMessage.split("@");
                messageBack = words[0] + ":" + words[1];

                if(Pattern.compile( "[0-9]" ).matcher(words[2]).find())
                {
                    substringDate = words[2];
                }
                else
                {
                    substringDate = words[3];
                }
                String time = substringDate.substring(11);
                String replaceString=time.replaceAll("[.]",":");//replaces all occurrences of "a" to "e"
                String[] table = replaceString.split(":");
                String removeLastSign = table[3].substring(0,table[3].length()-1);
                int hours = Integer.parseInt(table[0]);
                int minutes = Integer.parseInt(table[1]);
                int seconds = Integer.parseInt(table[2]);
                int mili = Integer.parseInt(removeLastSign);

                long resultInMili = (hours * 60 * 60 * 1000000) + (minutes * 60 * 1000000) + (seconds * 1000000) + mili;

                Instant timestamp = Instant                // Represent a moment in UTC.
                        .now()                 // Capture the current moment. Returns a `Instant` object.
                        .truncatedTo(          // Lop off the finer part of this moment.
                                ChronoUnit.MICROS  // Granularity to which we are truncating.
                        );
                String timestamp1 = timestamp.toString();

                String currTime = timestamp1.substring(11);
                String replaceString1=currTime.replaceAll("[.]",":");
                String[] table1 = replaceString1.split(":");
                String removeLastSign1 = table1[3].substring(0,table1[3].length()-1);
                int currHours = Integer.parseInt(table1[0]);
                int currMinutes = Integer.parseInt(table1[1]);
                int currSeconds = Integer.parseInt(table1[2]);
                int currMili = Integer.parseInt(removeLastSign1);

                long currResultInMili = (currHours * 60 * 60 * 1000000) + (currMinutes * 60 * 1000000) + (currSeconds * 1000000) + currMili;
                long finalResultTime = currResultInMili - resultInMili;
                String finalResult = Long.toString(finalResultTime);
                model1.addElement(finalResult);
                model.addElement(messageBack);


                receivedMessage = "";
                messageBack = "";
                substringDate = "";
                timestamp1 = "";
            }
        }
        catch (Exception e)
        {
            socketClient.close();
        }
    }
}
