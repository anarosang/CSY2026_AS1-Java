import java.awt.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;


class NetcatGUI extends JFrame

{

    public JButton sendButton;

    public JTextArea rxArea, txArea;

    public JScrollPane pane;

    public Container container;



    public NetcatGUI (String title)

    {

        super (title);



        container = getContentPane ();

        container.setLayout (new FlowLayout());



        txArea = new JTextArea (6, 40);

        rxArea = new JTextArea (6, 40);

        rxArea.setEditable (false);

        pane = new JScrollPane (rxArea);



        sendButton = new JButton ("Send");



        container.add (pane);

        container.add (txArea);

        container.add (sendButton);

    }

}



public class Netcat extends NetcatGUI

{

    String inputLine, outputLine;

    String role;

    static String remoteAddr;

    static String localPort, remotePort;



    class NetcatStartupGUI extends JFrame {

        public JButton startButton;

        JComboBox netcatRole;

        public JTextField remotePortTextField, remoteIPTextField, localPortTextField;

        public Container container;



        public NetcatStartupGUI (String title)

        {

            super (title);

            String[] netcatRoleString = {"                                              Netcat Role:                                         ", "TCP Server", "TCP Client", "UDP Server", "UDP Client"};



            container = getContentPane ();

            container.setLayout (new FlowLayout());

            netcatRole = new JComboBox(netcatRoleString);

            remotePortTextField = new JTextField (40);

            remoteIPTextField = new JTextField (40);

            localPortTextField = new JTextField (40);



            startButton = new JButton ("Start");



            container.add (new JLabel ("                                                       "));

            container.add (netcatRole);

            container.add (new JLabel ("Remote IP:"));

            container.add (remoteIPTextField);

            container.add (new JLabel ("Remote Port:"));

            container.add (remotePortTextField);

            container.add (new JLabel ("Local Port:"));

            container.add (localPortTextField);

            container.add (startButton);

        }

    }



    class NetcatStartup extends NetcatStartupGUI {

        public ButtonHandler bHandler;

        volatile boolean finished = false;



        public NetcatStartup (String title)

        {

            super (title);

            bHandler = new ButtonHandler ();

            startButton.addActionListener (bHandler);

        }



        private class ButtonHandler implements ActionListener

        {

            public void actionPerformed (ActionEvent event)

            {

                role = (String)netcatRole.getSelectedItem();

                remoteAddr = remoteIPTextField.getText ();

                remotePort = remotePortTextField.getText ();

                localPort = localPortTextField.getText ();

                finished = true;

            }

        }



        public boolean run () {
            while (!finished);
            return true;
        }

    }



    public Netcat (String title) throws IOException {
        super (title);
    }



    class TcpServer {
        ServerSocket serverSocket;
        Socket socket;
        PrintWriter out;
        BufferedReader in;
        ButtonHandler txButtonHandler;

        TcpServer (String port) throws IOException {

            serverSocket = new ServerSocket (Integer.parseInt (port));

            socket = serverSocket.accept ();

            out = new PrintWriter (socket.getOutputStream(), true);

            in = new BufferedReader (new InputStreamReader (socket.getInputStream ()));



            txButtonHandler = new ButtonHandler ();

            sendButton.addActionListener (txButtonHandler);



            rx ();



            socket.close ();

            serverSocket.close ();



            System.exit (1);

        }



        void tx () throws IOException

        {
          String outputLine = txArea.getText ();

          System.out.println ("Server/Me: " + outputLine);
          if(!rxArea.getText().equals("")){
            rxArea.setText (rxArea.getText() + "\nServer/Me: " + outputLine);
          }else {
            rxArea.setText ("Server/Me: " + outputLine);
          }


          out.println (outputLine);
          txArea.setText("");

        }



        private class ButtonHandler implements ActionListener

        {

            public void actionPerformed (ActionEvent event) //throws IOException

            {

                try{tx ();} catch (IOException e){}

            }

        }



        void rx () throws IOException

        {

            String fromClient;



            do

            {

              fromClient = in.readLine ();

                if (fromClient != null){
                  if (!rxArea.getText().equals("")) {
                    rxArea.setText (rxArea.getText() + "\nClient: " + fromClient);
                  }else{
                    rxArea.setText ("Client: " + fromClient);
                  }
                  System.out.println ("Client: " + fromClient);

                }

            }

            while (fromClient != null);

        }

    }

    class TcpClient{
      Socket socket;
      PrintWriter out;
      BufferedReader in;
      ButtonHandler txButtonHandler;

      TcpClient (String addr, String port) throws IOException {

          socket = new Socket (addr,Integer.parseInt (port));

          out = new PrintWriter (socket.getOutputStream(), true);

          in = new BufferedReader (new InputStreamReader (socket.getInputStream ()));



          txButtonHandler = new ButtonHandler ();

          sendButton.addActionListener (txButtonHandler);



          rx ();



          socket.close ();



          System.exit (1);

      }



      void tx () throws IOException

      {
        String outputLine = txArea.getText ();

        System.out.println ("Client/Me: " + outputLine);
        if(!rxArea.getText().equals("")){
          rxArea.setText (rxArea.getText() + "\nClient/Me: " + outputLine);
        }else{
          rxArea.setText ("Client/Me: " + outputLine);
        }

        out.println (outputLine);
        txArea.setText("");

      }



      private class ButtonHandler implements ActionListener

      {

          public void actionPerformed (ActionEvent event) //throws IOException

          {

              try{tx ();} catch (IOException e){}

          }

      }



      void rx () throws IOException

      {

          String fromServer;

          do

          {

            fromServer = in.readLine () ;

              if (fromServer != null){
                if (!rxArea.getText().equals("")) {
                  rxArea.setText (rxArea.getText() + "\nServer: " + fromServer);
                }else{
                  rxArea.setText ("Server: " + fromServer);
                }
                System.out.println ("Server: " + fromServer);

              }

          }

          while (fromServer != null);

      }
    }

    class UdpServer{
      DatagramSocket socket;
      ButtonHandler txButtonHandler;
      int port;
      InetAddress address = null;

      UdpServer (String sport) throws IOException {

        int i =0;

          socket = new DatagramSocket (Integer.parseInt (sport));

          txButtonHandler = new ButtonHandler ();

          sendButton.addActionListener (txButtonHandler);



          rx ();



          socket.close ();

          System.exit (1);

      }



      void tx () throws IOException {

               byte[] buf = new byte[256];

               String toClient;

                 toClient = txArea.getText();

                 buf = toClient.getBytes();

                 DatagramPacket packet = new DatagramPacket (buf, toClient.length(), address, port);

                 if(address != null) socket.send(packet);

                 System.out.println("Sent: " + packet);

                 if (!rxArea.getText().equals("")) {
                    rxArea.setText(rxArea.getText() + "\nServer/Me: " + toClient);
                  }else {
                    rxArea.setText("Server/Me: " + toClient);
                  }

                  txArea.setText("");
      }



      private class ButtonHandler implements ActionListener

      {

          public void actionPerformed (ActionEvent event) //throws IOException

          {

              try{tx ();} catch (IOException e){}

          }

      }



      void rx () throws IOException

      {
             byte[] buf = new byte[256];

             String fromClient;

             do{
               for(int i=0; i<256; i++) buf[i]=0;

                   DatagramPacket packet = new DatagramPacket (buf, buf.length);

                   socket.receive (packet);

                   fromClient = new String (packet.getData());

                   address = packet.getAddress();

                   port = packet.getPort();

                   if(fromClient != null) System.out.println("Recived: " + fromClient);

                   if (!rxArea.getText().equals("")) {
                     rxArea.setText (rxArea.getText() + "\nClient: " + fromClient);
                   }else {
                     rxArea.setText ("Client: " + fromClient);
                   }
             }while(fromClient != null);
      }
    }

    class UdpClient{
      DatagramSocket socket;
      InetAddress address = null;
      int remoteport;
      ButtonHandler txButtonHandler;

      UdpClient (String addr,String port) throws IOException {
        int i =0;

          socket = new DatagramSocket ();

          remoteport = Integer.parseInt (port);

          address = InetAddress.getByName(addr);

          txButtonHandler = new ButtonHandler ();

          sendButton.addActionListener (txButtonHandler);



          rx ();



          socket.close ();

          System.exit (1);

      }



      void tx () throws IOException

      {
               byte[] buf = new byte[1024];

               String toServer = txArea.getText ();

               buf = toServer.getBytes ();

               DatagramPacket packet = new DatagramPacket (buf, buf.length, address, remoteport);

               if (address != null) socket.send(packet);

               System.out.println ("Sent: " + toServer);

               if (!rxArea.getText().equals("")) {
                 rxArea.setText(rxArea.getText() + "\nClient/Me: " + toServer);
               }else {
                 rxArea.setText("Client/Me: " + toServer);
               }

               txArea.setText("");
      }



      private class ButtonHandler implements ActionListener

      {

          public void actionPerformed (ActionEvent event) //throws IOException

          {

              try{tx ();} catch (IOException e){}

          }

      }



      void rx () throws IOException
      {
        byte[] buf = new byte[256];

        String fromServer;

        do{
          for(int i=0; i<256; i++) buf[i]=0;

          DatagramPacket packet = new DatagramPacket (buf, buf.length);

          socket.receive (packet);

          fromServer = new String (packet.getData());

              if(fromServer != null) System.out.println("Received: " + fromServer);

              if (!rxArea.getText().equals("")) {
                rxArea.setText (rxArea.getText() + "\nServer: " + fromServer);
              }else {
                rxArea.setText ("Server: " + fromServer);
              }

        }while(fromServer != null);
      }
    }

    public void run () throws IOException {
        if (role.equals ("TCP Server")) {System.out.println ("nc -l " + localPort);                     new TcpServer (localPort);}

        if (role.equals ("TCP Client")) {System.out.println ("nc " + remoteAddr + " " + remotePort);    new TcpClient (remoteAddr, remotePort);}

        if (role.equals ("UDP Server")) {System.out.println ("nc -u -l " + localPort);                  new UdpServer (localPort);}

        if (role.equals ("UDP Client")) {System.out.println ("nc -u " + remoteAddr + " " + remotePort); new UdpClient (remoteAddr, remotePort);}
    }

    public void g (){
        NetcatStartup p = new NetcatStartup ("Netcat Connection");

        p.setSize(new Dimension (550,280));
        p.setResizable(false);
        p.setVisible (true);
        p.run ();
        p.dispose ();
    }

    public static void main (String[] args) throws IOException {
        Netcat f = new Netcat ("Netcat");

        f.g();
        f.setSize(new Dimension (510,280));
        f.setResizable(false);
        f.setVisible (true);
        f.run ();
    }

}
