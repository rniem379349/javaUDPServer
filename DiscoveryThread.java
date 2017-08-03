package udpserver;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Scanner;
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

                //See if the packet holds the right command (message)
                System.out.println(stringDisplayedBeforeMsg + "Checking packet identifier...");
                String message = new String(packet.getData()).trim();
                if (message.substring(0, packetIdentifier.length()).equals(packetIdentifier))
                {
                    //Send a response if identifier is correct
                    System.out.println(stringDisplayedBeforeMsg + "Packet identifier check - SUCCESS");
                    
                    //construct a list of the server's network interfaces
                    String ethInfo = "";
                    try 
                    {
                        NetworkInterface ethInterface = NetworkInterface.getByName("eth0");
                        {
                            ethInfo += ethInterface.getDisplayName() + ';';
                            Enumeration<InetAddress> Addresses = ethInterface.getInetAddresses();
                            while(Addresses.hasMoreElements()) 
                            {
                                //extract the MAC and Internet addresses to send back info
                                InetAddress addr = Addresses.nextElement();
                                byte[] mac = ethInterface.getHardwareAddress();
                                String macString = "";
                                for (int i = 0; i < mac.length; i++) 
                                {
                                    macString += String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : "");
                                }
                                
                                //get the rest of the interface info from /etc/network/interfaces
                                File interfacesFile = new File("/etc/network/interfaces");
                                boolean exists = interfacesFile.exists();
                                Scanner scanner;
                                PrintWriter writer;
                                String line, trimmedLine;
                                
                                if(exists)
                                {
                                    scanner = new Scanner(new FileInputStream(interfacesFile));
                                    
                                    while (scanner.hasNextLine())
                                    {
                                        line = scanner.nextLine();
                                        trimmedLine = line.trim();
                                        if (trimmedLine.startsWith("iface eth0 inet static"))
                                        {
                                            while(!trimmedLine.isEmpty())
                                            {
                                                if(trimmedLine.startsWith("address"))
                                                {
                                                    trimmedLine = trimmedLine.substring("address".length()+1);
                                                    ethInfo += trimmedLine + ';';
                                                }
                                                else if(trimmedLine.startsWith("netmask"))
                                                {
                                                    trimmedLine = trimmedLine.substring("netmask".length()+1);
                                                    ethInfo += trimmedLine + ';';
                                                }
                                                else if(trimmedLine.startsWith("network"))
                                                {
                                                    trimmedLine = trimmedLine.substring("network".length()+1);
                                                    ethInfo += trimmedLine + ';';
                                                }
                                                else if(trimmedLine.startsWith("broadcast"))
                                                {
                                                    trimmedLine = trimmedLine.substring("broadcast".length()+1);
                                                    ethInfo += trimmedLine + ';';
                                                }
                                                else if(trimmedLine.startsWith("gateway"))
                                                {
                                                    trimmedLine = trimmedLine.substring("gateway".length()+1);
                                                    ethInfo += trimmedLine + ';';
                                                }
                                                line = scanner.nextLine();
                                                trimmedLine = line.trim();
                                            }
                                        }
                                    }
                                }
                                //construct the actual message
                                ethInfo += macString + ';';
                            }
                        }
                    } catch (SocketException e) 
                    {
                        throw new RuntimeException(e);
                    }
                    
                    //construct the return message and send it
                    String responseString = packetIdentifier + ';' + ethInfo;
                    System.out.println("Respo " + responseString);
                    byte[] sendData = responseString.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
                    serverSocket.send(sendPacket);

                    System.out.println(stringDisplayedBeforeMsg + "Sent response packet to: " + sendPacket.getAddress().getHostAddress());
                }
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

//                    //construct a list of the server's network interfaces
//                    String interfaceList = "";
//                    try 
//                    {
//                        //iterate through each network interface on the server
//                        Enumeration<NetworkInterface> Interfaces = NetworkInterface.getNetworkInterfaces();
//                        while (Interfaces.hasMoreElements()) 
//                        {
//                            NetworkInterface iface = Interfaces.nextElement();
//                            
//                            //filter out 127.0.0.1 and inactive interfaces
//                            if (iface.isLoopback() || !iface.isUp())
//                                continue;
//                            
//                            Enumeration<InetAddress> Addresses = iface.getInetAddresses();
//                               
//                            while(Addresses.hasMoreElements()) 
//                            {
//                                //extract the MAC and Internet addresses to send back info
//                                InetAddress addr = Addresses.nextElement();
//                                byte[] mac = iface.getHardwareAddress();
//                                String macString = "";
//                                for (int i = 0; i < mac.length; i++) 
//                                {
//                                    macString += String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : "");
//                                }
//                                
//                                //construct the actual message
//                                interfaceList += iface.getDisplayName() + ';';
//                                interfaceList += addr.getHostAddress() + ';';
//                                interfaceList += macString + ';';
//                            }
//                        }
//                    } catch (SocketException e) 
//                    {
//                        throw new RuntimeException(e);
//                    }
//                    
//                    //construct the return message and send it
//                    String responseString = packetIdentifier + ';' + interfaceList;
//                    byte[] sendData = responseString.getBytes();
//                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
//                    serverSocket.send(sendPacket);
//
//                    System.out.println(stringDisplayedBeforeMsg + "Sent response packet to: " + sendPacket.getAddress().getHostAddress());