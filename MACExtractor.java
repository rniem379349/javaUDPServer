package udpserver;

/*
 * @author bacom
 */

public class MACExtractor {
    /*
        Class for extracting Hardware address from given NetworkInterface object
    */
    
    String getMacString(byte[] mac)
    {
        String macString = "";
        for (int i = 0; i < mac.length; i++) 
        {
            macString += String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : "");
        }
        return "mac=" + macString;
    }
}
