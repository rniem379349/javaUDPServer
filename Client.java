package udpserver;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * @author bacom
 */

public class Client {
    void broadcastPacket () 
    {
        DatagramSocket clientSocket;
        int port = 8421;
        int bufsize = 2048;
        String stringDisplayedBeforeMsg = getClass().getName() + " >>> "; // string to be displayed before each command line message
        String packetIdentifier = "ZXF2L";
        String request = "broadcast_search";
        
        // Find the server using UDP broadcast
        try 
        {
            //Open a random port to send the package
            clientSocket = new DatagramSocket();
            clientSocket.setBroadcast(true);

            //request message
            byte[] sendData = (packetIdentifier + request).getBytes();

            
            try 
            {
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), port);
                clientSocket.send(sendPacket);
                System.out.println(stringDisplayedBeforeMsg + "Request packet sent to: 255.255.255.255 (DEFAULT) on port " + port);
            } catch (Exception e) {
            }

            // Broadcast the message over all the network interfaces
            Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) 
            {
                NetworkInterface networkInterface = (NetworkInterface) interfaces.nextElement();

                if (networkInterface.isLoopback() || !networkInterface.isUp()) 
                {
                    continue; // Don't want to broadcast to the loopback interface
                }

                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) 
                {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast == null) 
                    {
                        continue;
                    }

                    // Send the broadcast package
                    try 
                    {
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, port);
                        clientSocket.send(sendPacket);
                    } catch (Exception e) {
                    }

                    System.out.println(stringDisplayedBeforeMsg + "Request packet sent to: " + broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName() + " on port " + port);
                }
            }

            System.out.println(stringDisplayedBeforeMsg + "Broadcasting done. Checking for a response...");

            //Wait for a response
            byte[] recvBuf = new byte[bufsize];
            DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
            clientSocket.receive(receivePacket);

            //We have a response
            System.out.println(stringDisplayedBeforeMsg + "Broadcast response from: " + receivePacket.getAddress().getHostAddress());

            //Check if the message identifier is correct
            String message = new String(receivePacket.getData()).trim();
            if (message.substring(0, packetIdentifier.length()).equals(packetIdentifier)) {
                // display server message
                System.out.println("Incoming Message: \n" + message);
            }

            //Close the port
            clientSocket.close();
        } catch (IOException ex) 
        {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args) 
    {
        Client client = new Client();
        client.broadcastPacket();
    }
}
