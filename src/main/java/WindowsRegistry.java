package tuner;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * From http://stackoverflow.com/questions/62289/read-write-to-windows-registry-using-java
 * @author Oleg Ryaboy, based on work by Miguel Enriquez 
 */
public class WindowsRegistry {

    /**
     * 
     * @param location path in the registry
     * @return registry value or null if not found
     */
    public static final String readRegistry(String location){
        try {
            // Run reg query, then read output with StreamReader (internal class)
            Process process = Runtime.getRuntime().exec("reg query " + location);

            StreamReader reader = new StreamReader(process.getInputStream());
            reader.start();
            process.waitFor();
            reader.join();
            return reader.getResult();
        }
        catch (Exception e) {
            return null;
        }

    }

    /**
     * 
     * @param location path in the registry
     * @param key registry key
     * @return registry value or null if not found
     */
    public static final String readRegistry(String location, String key){
        try {
            // Run reg query, then read output with StreamReader (internal class)
            String cmd = "reg query " + '"'+ location + "\" /v " + key;
            System.out.println("cmd " + cmd);
            Process process = Runtime.getRuntime().exec("reg query " + 
                    '"'+ location + "\" /v " + key);

            StreamReader reader = new StreamReader(process.getInputStream());
            reader.start();
            process.waitFor();
            reader.join();
            String output = reader.getResult();

            String parsed[] = output.split("\\s\\s+", 5);
            // Output has the following format:
            // \n<Version information>\n\n<key>\t<registry type>\t<value>
            if(parsed.length != 5){
                return null;
            }

            // Parse out the value
            return parsed[parsed.length-1].trim();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    static class StreamReader extends Thread {
        private InputStream is;
        private StringWriter sw= new StringWriter();

        public StreamReader(InputStream is) {
            this.is = is;
        }

        public void run() {
            try {
                int c;
                while ((c = is.read()) != -1)
                    sw.write(c);
            }
            catch (IOException e) { 
            }
        }

        public String getResult() {
            return sw.toString();
        }
    }
    /*
    public static void main(String[] args) {

        // Sample usage
        String value = WindowsRegistry.readRegistry("HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\" 
                 + "Explorer\\Shell Folders", "Personal");
        System.out.println(value);
    }
    */
}

