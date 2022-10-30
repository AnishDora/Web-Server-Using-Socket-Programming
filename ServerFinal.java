import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

public class ServerFinal {

    public static void main(String args[]) throws IOException {
        //Checking the Command Line Parameters passed
        if(args.length>4)
        {
            System.out.println("Invalid Input! extra arguments are provided");
        }
         if(args.length<4)
        {
            System.out.println("Invalid Input! less arguments are provided");
        }
        //Converting the string input_Buffer to int
        int port_Num=Integer.parseInt(args[3]);
        System.out.println("Connected Successfully");

        //Server Socket Creation
        ServerSocket server = new ServerSocket(port_Num);
        //Infinite Loop for requests
        while (true)
        {   //Client listens to requests
            Socket client = server.accept(); 
            System.out.println("Received Connection");
            //Thread Creation for each process
            Thread tr = new Thread(new HttpRequest(client));
            tr.start(); 
        }
    }
}

class HttpRequest implements Runnable {
    Socket socket;
    //Constructor for the class
    public HttpRequest(Socket socket) throws IOException {
        this.socket = socket;
    }

    public void run() {
        try {
            requestHandler();
            System.out.println(socket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Function to modify/handle the request
    private void requestHandler() throws Exception {
        //To read the Input Stream
        BufferedReader input_Buffer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        //To print the output stream
        PrintStream output_Buffer = new PrintStream(new BufferedOutputStream(socket.getOutputStream()));
        //Retrieves the input_Buffer stream from command line 
        String user_Input = input_Buffer.readLine(); 
        System.out.println();
        System.out.println(user_Input);  

        // File to Display.
        String input_File = "";
        //Converting String to Tokens
        StringTokenizer t_kn = new StringTokenizer(user_Input);
        try {

            // Retrieves from the GET command
            if (t_kn.hasMoreElements() && t_kn.nextToken().equalsIgnoreCase("GET") && t_kn.hasMoreElements())
                input_File = t_kn.nextToken();
            else
                //Throws File Not Found Exception
                throw new FileNotFoundException();

            // Retrieve index.html for both / & /index.html"
            if (input_File.endsWith("/")) 
                input_File += "index.html";

            while (input_File.indexOf("/") == 0)
                input_File = input_File.substring(1);

            //Checking if File exists but not in a readable format
            if ((new File(input_File).exists()) && !(new File(input_File).canRead())) {
                output_Buffer.print("HTTP/1.0 403 Page Forbidden or not in a readable format\r\n" +
                        "/" + input_File + "/\r\n\r\n");
                output_Buffer.close();
                return;
            }

            
            try (FileInputStream file_input_str = new FileInputStream(input_File)) {
                //Declaring the type content to text/plain
                String content_Type = "text/plain";
                //Checking if the file has an extension of .html
                if (input_File.endsWith(".html") || input_File.endsWith(".htm"))
                    {content_Type = "text/html";}
                //Checking if the file has an extension of .jpg or jpeg
                else if (input_File.endsWith(".jpg") || input_File.endsWith(".jpeg"))
                    {content_Type = "image/jpeg";}
                //Checking if the file has an extension of .gif
                else if (input_File.endsWith(".gif"))
                    {content_Type = "image/gif";}
                else if (input_File.endsWith(".class"))
                    {content_Type = "application/octet-stream";}
                else{

                }
                output_Buffer.print("HTTP/1.0 200 OK\r\n" +
                        "Content-type: " + content_Type + "\r\n\r\n");

                //Creating temp buffer to read the response
                byte temp_Storage[] = new byte[6000];
                int x;
                //Reading and displaying
                while ((x = file_input_str.read(temp_Storage)) > 0)
                    output_Buffer.write(temp_Storage, 0, x);
            }
            //Closing Output Stream and Socket
            output_Buffer.close();
            socket.close();
            //System.out.println("connection closed");
            } 
            catch (FileNotFoundException fnfe) {
            //If File not found
            output_Buffer.print("HTTP/1.0 404 Page Not Found\r\n" +
                    "Content-type: text/html\r\n\r\n" +
                    "<html><head></head><body>HTTP/1.0 404 Page Not Found<br> Requested " + input_File + " does not exist</body></html>\n");
            //Closing Socket Input Output.

            output_Buffer.close();
            input_Buffer.close();
            socket.close();
        }
    }
}

