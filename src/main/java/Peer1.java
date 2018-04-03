import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author Abdullah Ejaz
 *
 *
 * */



public class Peer1 extends Thread {

    private Socket s;
    private static String hostName_Server;

    public static String REGISTER_ACTION = "register";
    public static String SEARCH_ACTION = "search";
    public static final String RECEIVE = "receive";
    public static final String SEND = "send";
    public static final String RECEIVE_FILE_ACTION = "receiveFile";
    public static String DIRECTORY_PEER1 = "peer1";


    public Peer1(){
    }

    public Socket getS() {
        return s;
    }

    public void setS(Socket s) {
        this.s = s;
    }

    public Peer1(Socket s){
        this.s= s;
    }


/**********************************************************************************************************************************************************/

//Helps in registering Peer
    public void registration() throws Exception{

        s = null;

        Messages message = new Messages();
        message.setActionPerformed(REGISTER_ACTION);
        List<String> listOfFiles = new ArrayList<String>();

        //Adding all files in that are with peer1
        listOfFiles.add("1_p1.txt");
        listOfFiles.add("2_p1.txt");
        listOfFiles.add("3_p1.txt");
        listOfFiles.add("4_p1.txt");
        listOfFiles.add("5_p1.txt");
        listOfFiles.add("6_p1.txt");
        listOfFiles.add("7_p1.txt");
        listOfFiles.add("8_p1.txt");
        listOfFiles.add("9_p1.txt");
        listOfFiles.add("10_p1.txt");

        //Adding the files that are common between peers.
        listOfFiles.add("common_12.txt");
        listOfFiles.add("common_13.txt");

        //Connecting
        s = new Socket(hostName_Server, 8899);
        System.out.println("Server is registering PEER 1");
        System.out.println("                             ");
        ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());

        //Has all the details of the peer object
        ModelPeer peer1 = new ModelPeer(InetAddress.getLocalHost().getHostName(), "5555", listOfFiles, "");
        message.setModel(peer1);

        //Writing the message to output stream
        oos.writeObject(message);
        oos.flush();

        ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
        System.out.println("---------------------------------------------------------------------------");
        System.out.println("Server successfully registered PEER 1 at " + message.getModel().getId());
        System.out.println("---------------------------------------------------------------------------");
        System.out.println("                                                                                           ");
        Thread.sleep(500);

    }

/**********************************************************************************************************************************************************/

    private String absoluteFilePath(String fileName, String location){

        //get the path of the current class
        File file = new File(Peer1.class.getProtectionDomain().getCodeSource().getLocation().getPath());

        //Getting the parent directory of file path
        File file1 = new File(file.getParent().concat("\\")+DIRECTORY_PEER1.concat("\\") + location);

        //getting list
        File[] fileList = file1.listFiles();
        String path = file.getParent().concat("\\")+DIRECTORY_PEER1.concat("\\") + location;

         if("receive".equalsIgnoreCase(location)){
             path = file.getParent().concat("\\")+DIRECTORY_PEER1.concat("\\") + location;
             return path;
         }
            for (File name : fileList ){
                if (name.getName().equalsIgnoreCase(fileName)){
                    path = name.getAbsolutePath();
                }
            }
        return path;
    }


/**********************************************************************************************************************************************************/
        //receive file from peer
    public void obtain(String fileName, ModelPeer receivingPeer ){

        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;

        try{
            System.out.println("Peer started downloading " +fileName +" file from " +receivingPeer.getId());
            System.out.println("                                                   ");
            //Message object too sent to peer
            Messages message = new Messages();
            message.setActionPerformed(RECEIVE_FILE_ACTION);
            message.setFileSearch(fileName);
            Socket s = new Socket(receivingPeer.getIpAddress(), Integer.parseInt(receivingPeer.getPortNumber()));
            oos = new ObjectOutputStream(s.getOutputStream());
            //Writing to output stream
            oos.writeObject(message);
            oos.flush();
            //input stream for the response from server
            ois = new ObjectInputStream(s.getInputStream());
            String fileData = ois.readObject().toString();

            String path = absoluteFilePath(fileName, RECEIVE);
            path = path + "\\" + fileName;
            FileWriter fileWriter= new FileWriter(path, true);
            fileWriter.write(fileData);
            fileWriter.close();
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            System.out.println(fileName + " file downloaded successfully");
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        }catch(Exception e){

            try {
                ois.close();
                oos.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

    }


/**********************************************************************************************************************************************************/

//searching for a file requested by peer
    public void searching( String fileName) throws Exception{

        Messages message = new Messages();
        message.setActionPerformed(SEARCH_ACTION);
        message.setFileSearch(fileName);

        s = new Socket(hostName_Server, 8899);
        ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
        //writing message to output stream
        oos.writeObject(message);

        ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
        ResponseFromServer rfs = (ResponseFromServer) ois.readObject();

        if(rfs.getPeerModelList().size()>0){
            System.out.println("The file " +message.getFileSearch()+ " has been found at the following PEERS");
            System.out.println("                                                                            ");
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            System.out.println("                                                                            ");

            //print a list of each model having the searched file
            for (ModelPeer peerFound : rfs.getPeerModelList()){

                System.out.println("Peer ID: " + peerFound.getId());
                System.out.println("                                                                            ");
                System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            }

            //Obtaining the file from the peer.
            obtain(message.getFileSearch(), rfs.getPeerModelList().get(0));

        }
        else {
            System.out.println("!! File not available at any peer !!");
        }


    }


/**********************************************************************************************************************************************************/

    @Override
    public void run() {
        Messages message = null;
        boolean connected = true;
        try
        {
            ServerSocket ss = new ServerSocket(5555);

            while(true){
                System.out.println("                                  ");
                System.out.println("**********************************************************");
                System.out.println("PEER 1 started at port 5555");
                System.out.println("**********************************************************");
                Socket newPeerSocket = ss.accept();
                new Peer1(newPeerSocket).start();

            }

        }catch(Exception e){}



        do{
            try{

                ObjectInputStream inp = new ObjectInputStream(s.getInputStream());
                message = (Messages) inp.readObject();
                System.out.println("                                              ");
                System.out.println("The requested file is " +message.getFileSearch());
                String fileWanted = absoluteFilePath(message.getFileSearch(), SEND);
                String fileData = new String(Files.readAllBytes(Paths.get(fileWanted)));
                ObjectOutputStream oup = new ObjectOutputStream(s.getOutputStream());
                oup.writeObject(fileData);
                oup.flush();
                connected = false;

            }catch(Exception e){}

        }while(connected);


    }

    public static void main(String args[]) throws Exception
    {
        Scanner sc;
        System.out.println("Enter Server host name (For eg localhost " + InetAddress.getLocalHost().getHostName());
        hostName_Server = new Scanner(System.in).nextLine();
        new Peer1().start();
        while(true){
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            System.out.println("Select a number to perform an action");
            System.out.println("   ");
            System.out.println("Enter 1 to REGISTER peer 1");
            System.out.println("Enter 2 to SEARCH for a file");
            System.out.println("Enter 3 for 1000 sequential request");
            System.out.println("Enter 4 to exit");
            System.out.println("           ");
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            sc = new Scanner(System.in);
            String userInput = sc.nextLine();

            switch (userInput){

                case "1":
                    Peer1 peer1 = new Peer1();
                    peer1.registration();
                    break;


                case "2":
                    System.out.println("                 ");
                    System.out.println("Enter the file name to be searched");
                    sc = new Scanner(System.in);
                    String fileName = sc.nextLine();
                    System.out.println("                 ");
                    System.out.println("Peer 1 need a file named " + fileName + " and wants to download it.");
                    peer1 = new Peer1();
                    peer1.searching(fileName);
                    break;


                case "3" :
                    final long startTime = System.currentTimeMillis();
                    System.out.println("                          ");
                    System.out.println("Enter name of the file to search");
                    sc = new Scanner(System.in);
                    String fileToSearch = sc.nextLine();
                    System.out.println("                 ");
                    System.out.println("Peer 1 requested for file " + fileToSearch);
                    for (int i =0; i< 1000; i++){
                        peer1 = new Peer1();
                        peer1.searching(fileToSearch);
                    }

                    final long endTime = System.currentTimeMillis();
                    System.out.println("                 ");
                    System.out.println("The total time taken to process 1000 sequential requests is " +(endTime - startTime)+ " milliseconds");
                    break;

                case "4" :
                    System.out.println("You choose to exit the program. Have a good one. BYE!!!");
                    System.exit(0);
                    break;

                default:
                    System.out.println("Invalid Input");
                    break;

            }

        }




    }
}
