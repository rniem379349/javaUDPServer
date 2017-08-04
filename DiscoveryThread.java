package udpserver;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * @author bacom
 */

public class DiscoveryThread implements Runnable 
{
    DatagramSocket serverSocket;
    int port = 8421;
    int bufsize = 2048;
    String stringDisplayedBeforeMsg = getClass().getName() + " >>> "; // string to be displayed before each command line message
    String packetIdentifier = "ZXF2L";
    String ethInfo = ""; //server network interface info
    String responseString = ""; //server response to the client
    byte[] sendData; //server response in byte array
    DatagramPacket sendPacket; //packet for server response
    
    @Override
    public void run() 
    {
        try
        {
            //open a socket to intercept broadcast traffic on specified port
            serverSocket = new DatagramSocket(port, InetAddress.getByName("0.0.0.0"));
            serverSocket.setBroadcast(true);

            while (true) 
            {
                System.out.println(stringDisplayedBeforeMsg + "Ready to receive packets...");

                //Receive a packet
                byte[] recvBuf = new byte[bufsize];
                DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                serverSocket.receive(packet);

                //Display received packet information
                System.out.println(stringDisplayedBeforeMsg + "Packet from: " + packet.getAddress().getHostAddress());
                System.out.println(stringDisplayedBeforeMsg + "Packet data: " + new String(packet.getData()));

                //See if the packet holds the right identifier 
                System.out.println(stringDisplayedBeforeMsg + "Checking packet identifier...");
                String message = new String(packet.getData()).trim();
                if (message.substring(0, packetIdentifier.length()).equals(packetIdentifier))
                {
                    //Send a response if identifier is correct
                    System.out.println(stringDisplayedBeforeMsg + "Packet identifier check - SUCCESS");
                    String command = message.substring(packetIdentifier.length());
                    System.out.println("comandooo: " + command);
                    try 
                    {
                        if(command.equals("broadcast_search"))
                        {
                            //get server and BAS info
                            //create extractors for getting server data and BAS config
                            FiledataExtractor fileExtr = new FiledataExtractor();
                            MACExtractor macExtr = new MACExtractor();
                            try 
                            {
                                ethInfo = "";
                                //get the eth0 info
                                NetworkInterface ethInterface = NetworkInterface.getByName("eth0");
                                ethInfo += "interface=" + ethInterface.getDisplayName() + ';';

                                //cycle through the internet addresses
                                Enumeration<InetAddress> Addresses = ethInterface.getInetAddresses();

                                while(Addresses.hasMoreElements()) 
                                {
                                    Addresses.nextElement();

                                    //get the rest of the interface info from /etc/network/interfaces and add the MAC
                                    byte[] mac = ethInterface.getHardwareAddress();
                                    ethInfo += fileExtr.extractData(new File("/etc/network/interfaces"));
                                    ethInfo += macExtr.getMacString(mac) + ';';
                                    ethInfo += fileExtr.extractData(new File("/home/pi/bas/config.ini"));
                                }
                            } catch (SocketException e) 
                            {
                                throw new RuntimeException(e);
                            }

                            //construct the return message and send it via broadcast
                            responseString = packetIdentifier + ';' + ethInfo;
                            System.out.println("Response: " + responseString);
                            sendData = responseString.getBytes();
                            sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), packet.getPort());
                            serverSocket.send(sendPacket);
                        }       
                        else
                        {
                            //construct the return message and send it via broadcast
                            responseString = "ERROR: Command not recognized";
                            System.out.println("Response: " + responseString);
                            sendData = responseString.getBytes();
                            sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), packet.getPort());
                            serverSocket.send(sendPacket);
                        }
                    } catch (Exception e) {}
                }
                System.out.println(stringDisplayedBeforeMsg + "Sent response packet to: " + sendPacket.getAddress().getHostAddress());
            }
        }
        catch (IOException e) 
        {
            Logger.getLogger(DiscoveryThread.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public static DiscoveryThread getThreadInstance() 
    {
        return DiscoveryThreadHolder.INSTANCE;
    }

    private static class DiscoveryThreadHolder 
    {
        private static final DiscoveryThread INSTANCE = new DiscoveryThread();
    }
    
    public static void main(String[] args) 
    {
        
    }
}

                    //get server and BAS info
                    //create extractors for getting server data and BAS config
//                    FiledataExtractor fileExtr = new FiledataExtractor();
//                    MACExtractor macExtr = new MACExtractor();
//                    try 
//                    {
//                        ethInfo = "";
//                        //get the eth0 info
//                        NetworkInterface ethInterface = NetworkInterface.getByName("eth0");
//                        ethInfo += "interface=" + ethInterface.getDisplayName() + ';';
//                        
//                        //cycle through the internet addresses
//                        Enumeration<InetAddress> Addresses = ethInterface.getInetAddresses();
//                        
//                        while(Addresses.hasMoreElements()) 
//                        {
//                            Addresses.nextElement();
//                            
//                            //get the rest of the interface info from /etc/network/interfaces and add the MAC
//                            byte[] mac = ethInterface.getHardwareAddress();
//                            ethInfo += fileExtr.extractData(new File("/etc/network/interfaces"));
//                            ethInfo += macExtr.getMacString(mac) + ';';
//                            ethInfo += fileExtr.extractData(new File("/home/pi/bas/config.ini"));
//                        }
//                    } catch (SocketException e) 
//                    {
//                        throw new RuntimeException(e);
//                    }
//                    
//                    //construct the return message and send it via broadcast
//                    String responseString = packetIdentifier + ';' + ethInfo;
//                    System.out.println("Response: " + responseString);
//                    byte[] sendData = responseString.getBytes();
//                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), packet.getPort());
//                    serverSocket.send(sendPacket);
//
//                    System.out.println(stringDisplayedBeforeMsg + "Sent response packet to: " + sendPacket.getAddress().getHostAddress());