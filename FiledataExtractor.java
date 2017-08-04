package udpserver;

import java.io.File;
import java.io.FileInputStream;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * @author bacom
 */
public class FiledataExtractor {
    /*
        Specific class for extracting network data (/etc/network/interfaces) and BAS server config (/home/pi/bas/config.ini).
        extractData will work provided the file structure of both the interfaces and the config.ini file remain unchanged.
    */
    String extractData(File file) 
    {
        String returnMessage = "";
        boolean exists = file.exists();
        Scanner scanner;
        String line, trimmedLine;

        if(exists)
        {
            try 
            {
                if (file.equals(new File("/etc/network/interfaces")))
                {
                    scanner = new Scanner(new FileInputStream(file));

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
                                    returnMessage += "ip_address=" + trimmedLine + ';';
                                }
                                else if(trimmedLine.startsWith("netmask"))
                                {
                                    trimmedLine = trimmedLine.substring("netmask".length()+1);
                                    returnMessage += "netmask=" + trimmedLine + ';';
                                }
                                else if(trimmedLine.startsWith("network"))
                                {
                                    trimmedLine = trimmedLine.substring("network".length()+1);
                                    returnMessage += "network=" + trimmedLine + ';';
                                }
                                else if(trimmedLine.startsWith("broadcast"))
                                {
                                    trimmedLine = trimmedLine.substring("broadcast".length()+1);
                                    returnMessage += "broadcast=" + trimmedLine + ';';
                                }
                                else if(trimmedLine.startsWith("gateway"))
                                {
                                    trimmedLine = trimmedLine.substring("gateway".length()+1);
                                    returnMessage += "gateway=" + trimmedLine + ';';
                                }
                                line = scanner.nextLine();
                                trimmedLine = line.trim();
                            }
                        }
                    }
                } 
                else if (file.equals(new File("/home/pi/bas/config.ini")))
                {
                    scanner = new Scanner(new FileInputStream(file));

                    while (scanner.hasNextLine())
                    {
                        //read file lines and extract data accordingly
                        line = scanner.nextLine();
                        trimmedLine = line.trim();
                        
                        if(trimmedLine.contains("="))
                        {
                            returnMessage += trimmedLine + ';';
                        }
                    }
                }
            } catch (Exception e)
            {
                Logger.getLogger(FiledataExtractor.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return returnMessage;
    }
}
