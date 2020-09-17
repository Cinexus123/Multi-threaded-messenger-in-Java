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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class Communicator implements ActionListener {

    private Thread t1;
    private Boolean flag = true;
    private JFrame f;
    private JFrame frameMessage = new JFrame("Error");
    private JPanel p = new JPanel();
    private JButton connectionStart = new JButton("Connection Start");
    private JButton connectionStop = new JButton("Connection Stop");
    private JButton sendInfo = new JButton("Send info");
    private JButton clearTable = new JButton("Clear table");
    private JTextField user = new JTextField("");
    private JTextField message = new JTextField("");
    private JTextField targetUser = new JTextField("");
    DefaultListModel<String> model = new DefaultListModel<>();
    private JList<String> list = new JList<>(model);
    DefaultListModel<String> model1 = new DefaultListModel<>();
    private JList<String> list1 = new JList<>(model1);
    private  JLabel l1 = new JLabel("Client 1");
    private  JLabel l2 = new JLabel("user");
    private  JLabel l3 = new JLabel("message");
    private  JLabel l4 = new JLabel("targetUser");
    private  JLabel l5 = new JLabel("Typ transmisji:");
    private JButton median = new JButton("Median");
    private JButton firstQuartile = new JButton("First quartile");
    private JButton thirdQuartile = new JButton("Third quartile");
    private JButton minValue = new JButton("minValue");
    private JButton maxValue = new JButton("maxValue");
    private JButton generateDigit = new JButton("Print numbers");
    private JTextField result = new JTextField("");
    private JTextField result1 = new JTextField("");
    JRadioButton r1=new JRadioButton("TCP");
    JRadioButton r2=new JRadioButton("UDP");

    private SocketAddress socketAddress;
    private Socket socketClient;
    private OutputStream os;
    private OutputStreamWriter osw;
    private BufferedWriter bw;

    List<String> numbers = new ArrayList<String>();
    List<Long> numbers1 = new ArrayList<Long>();
    List<String> firstQuartile2 = new ArrayList<String>();
    List<Long> firstQuartile1 = new ArrayList<Long>();

    List<String> thirdQuartile2 = new ArrayList<String>();
    List<Long> thirdQuartile1 = new ArrayList<Long>();

    List<String> maxNumbers = new ArrayList<String>();
    List<Long> maxNumbers1 = new ArrayList<Long>();

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
        r1.setBounds(100,450,100,30);
        r2.setBounds(100,480,100,30);
        l5.setBounds(5,465,90,30);
        sendInfo.setBounds(230,200,100,30);
        clearTable.setBounds(230,250,100,30);
        connectionStart.setBounds(70,300,140,30);
        connectionStop.setBounds(70,350,140,30);
        l2.setBounds(170,50,100,30);
        l3.setBounds(150,100,100,30);
        l4.setBounds(140,150,100,30);
        user.setBounds(220,50, 120,30);
        message.setBounds(220,100, 120,30);
        targetUser.setBounds(220,150, 120,30);
        list.setBounds(400,50, 150,250);
        list1.setBounds(600,50, 150,250);
        l1.setBounds(30,30, 100,30);

        result.setBounds(620,410, 100,30);
        result1.setBounds(620,480, 100,30);
        median.setBounds(20,410,100,30);
        firstQuartile.setBounds(140,410,100,30);
        thirdQuartile.setBounds(260,410,100,30);
        minValue.setBounds(380,410,100,30);
        maxValue.setBounds(500,410,100,30);
        generateDigit.setBounds(470,480,130,30);

        f.add(l1);
        f.add(r1);
        f.add(r2);
        f.add(l5);
        f.add(l2);
        f.add(l3);
        f.add(l4);
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

        p.add(result);
        p.add(result1);
        p.add(median);
        p.add(firstQuartile);
        p.add(thirdQuartile);
        p.add(minValue);
        p.add(maxValue);
        p.add(generateDigit);

        sendInfo.addActionListener(this);
        clearTable.addActionListener(this);
        connectionStart.addActionListener(this);
        connectionStop.addActionListener(this);

        median.addActionListener(this);
        firstQuartile.addActionListener(this);
        thirdQuartile.addActionListener(this);
        minValue.addActionListener(this);
        maxValue.addActionListener(this);
        generateDigit.addActionListener(this);


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

        if (!socketClient.isConnected())
        {
            JOptionPane.showMessageDialog(frameMessage,"Please start connection");
            return;
        }
        try {
            sendMessage = frameToSend;
            bw.write(sendMessage);
            bw.newLine();
            bw.flush();
            message.setText("");
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
        if(source == generateDigit)
        {
            GenerateDigit();
        }
        if(source == median)
        {
            Median();
        }
        if(source == firstQuartile)
        {
            FirstQuartile();
        }
        if(source == thirdQuartile)
        {
            ThirdQuartile();
        }
        if(source == minValue)
        {
            MinValue();
        }
        if(source == maxValue)
        {
            MaxValue();
        }
    }

    private void MinValue() {
        long  wynik;
        wynik = numbers1.get(0);
        for (int i = 0; i < numbers1.size(); i++)
        {
            if (wynik > numbers1.get(i))
            {
                wynik = numbers1.get(i);
            }
        }
        result.setText(String.valueOf(wynik));
    }

    private void MaxValue() {
        for (int i = 1; i < model1.size(); i++)
        {
            maxNumbers.add(model1.get(i));
        }
        for (var item : maxNumbers)
        {
            maxNumbers1.add(Long.parseLong(item));
        }
        Collections.sort(maxNumbers1);
        long wynik;
        wynik = maxNumbers1.get(0);
        for (int i = 0; i < numbers1.size() -1; i++)
        {
            if (wynik < maxNumbers1.get(i))
            {
                wynik = maxNumbers1.get(i);
            }
        }
        result.setText(String.valueOf(wynik));
    }

    private void ThirdQuartile() {
        for (int i = 250; i < numbers1.size() ; i++)
        {
            String str = Long.toString(numbers1.get(i));
            thirdQuartile2.add(str);
        }
        for (var item : thirdQuartile2)
        {
            thirdQuartile1.add(Long.parseLong(item));
        }
        Collections.sort(thirdQuartile1);

        long median = (thirdQuartile1.get(125) + thirdQuartile1.get(126)) / 2;
        result.setText(String.valueOf(median));
    }

    private void FirstQuartile() {
        for (int i = 0; i < numbers1.size() - 250; i++)
        {
            String str = Long.toString(numbers1.get(i));
            firstQuartile2.add(str);
        }
        for (var item : firstQuartile2)
        {
            firstQuartile1.add(Long.parseLong(item));
        }
        Collections.sort(firstQuartile1);

        long median = (firstQuartile1.get(125) + firstQuartile1.get(126)) / 2;
        result.setText(String.valueOf(median));
    }

    private void Median() {
        for (int i = 0; i < model1.size(); i++)
        {
            numbers.add(model1.get(i));
        }
        for(var item : numbers)
        {
            numbers1.add(Long.parseLong(item));
        }
        Collections.sort(numbers1);

        long median = (numbers1.get(250) + numbers1.get(251)) / 2;
        result.setText(String.valueOf(median));
    }

    private void GenerateDigit() {
        String name = "";
        for(int i=0;i< numbers.size();i++)
        {
            name += numbers.get(i) + ",";
        }
        result1.setText(name);
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
                String replaceString=time.replaceAll("[.]",":");
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
