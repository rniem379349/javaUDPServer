package udpserver;

/*
 * @author bacom
 */

public class UDPServer {
    public static void main(String[] args) 
    {
        Thread discoveryThread = new Thread(DiscoveryThread.getThreadInstance());  
        discoveryThread.start();
    }
}
