package uet.PeerCatcher.mcg;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import uet.PeerCatcher.config.PeerCatcherConfigure;
import uet.PeerCatcher.main.FileModifier;

public class CalculateMutualContactScore {
    public static HashMap<String, Integer> map_p2p = new HashMap<>();
    public static HashMap<Integer, String> map_Id2Ip = new HashMap<>();

    private static final double mutual_contact_score_threshold = PeerCatcherConfigure.MUTUAL_CONTACT_SCORE_THRESHOLD;

    public static void Generate_Mutual_Contact_Graph(String Graph) throws IllegalArgumentException, IOException {
        String inputFolder = PeerCatcherConfigure.ROOT_LOCATION + Graph + "/INPUT/P2P_Legi_Map";
        File folder = new File(inputFolder + "/");
        File[] listOfFiles = folder.listFiles();
        HashMap<String, String> ipMap = new HashMap<>();

        if (listOfFiles == null) {
            throw new IOException("Error: No files found in the input folder");
        }

        buildIPMap(inputFolder, listOfFiles, ipMap);

        inputFolder = PeerCatcherConfigure.ROOT_LOCATION + Graph + "/mutual_contact_sets";

        FileModifier.deleteDir(new File(PeerCatcherConfigure.ROOT_LOCATION + Graph + "/mutual_contact_graph"));
        File f = new File(PeerCatcherConfigure.ROOT_LOCATION + Graph + "/mutual_contact_graph");

        boolean created = f.mkdir();
        if (created) {
            System.out.println("Directory mutual_contact_graph created successfully");
        } else {
            System.out.println("Failed to create directory mutual_contact_graph");
        }

        String outputFolder = PeerCatcherConfigure.ROOT_LOCATION + Graph + "/mutual_contact_graph/";
        folder = new File(inputFolder + "/");
        listOfFiles = folder.listFiles();

        PrintWriter writer_IDtoIP = new PrintWriter(outputFolder + "IDtoIP.txt", "UTF-8");

        int ID_NUM = 0;

        String line;
        assert listOfFiles != null;
        for (File file : listOfFiles) {
            if (file.isFile() && file.getName().charAt(0) != '.') {
                BufferedReader br = new BufferedReader(new FileReader(inputFolder + "/" + file.getName()));

                while (br.readLine() != null) {
                    ID_NUM++;
                }
                br.close();
            }
        }

        int ID_NUM_indx = 0;
        int i = 0;
        String[] ipAddresses = new String[ID_NUM];
        String[] contactSets = new String[ID_NUM];

        for (File file : listOfFiles) {

            if (file.isFile() && file.getName().charAt(0) != '.') {
                BufferedReader br = new BufferedReader(new FileReader(inputFolder + "/" + file.getName()));


                while ((line = br.readLine()) != null) {

                    String[] parts = line.split("\t");
                    String set = parts[1].replace("[", "");
                    set = set.replace("]", "");

                    ipAddresses[i] = parts[0];
                    contactSets[i] = set;

                    i++;

                    Set<String> Prefix16 = new HashSet<>();
                    Set<String> Prefix24 = new HashSet<>();
                    Set<String> Prefix32 = new HashSet<>();

                    for (String IPANDProto : set.split(", ")) {
                        String[] str = IPANDProto.split("\\.");

                        String P24 = str[0] + "." + str[1] + "." + str[2];
                        String P16 = str[0] + "." + str[1];
                        Prefix16.add(P16);
                        Prefix24.add(P24);
                        Prefix32.add(IPANDProto);
                    }
                    writer_IDtoIP.println(ID_NUM_indx + "\t" + parts[0] + "\t" + ipMap.getOrDefault(parts[0].split(",")[0], "Normal")
                            + "\t" + Prefix16.size() + "\t" + Prefix24.size() + "\t" + Prefix32.size());
                    ID_NUM_indx++;
                }
                br.close();
            }
        }
        writer_IDtoIP.close();

        generateHashMap();

        ExecutorService executor = Executors.newFixedThreadPool(256);

        for (int j = 0; j < ipAddresses.length; j++) {
            for (int k = j + 1; k < ipAddresses.length; k++) {
                String[] sts1 = ipAddresses[j].split(",");
                String[] sts2 = ipAddresses[k].split(",");

                String st1 = sts1[1] + "," + sts1[2] + "," + sts1[3];
                String st2 = sts2[1] + "," + sts2[2] + "," + sts2[3];

                if (st1.equals(st2)) {

                    GenerateMutualContactGraph R = new GenerateMutualContactGraph(outputFolder,
                            contactSets[j], contactSets[k], j, k, mutual_contact_score_threshold);
                    executor.execute(R);
                }
            }
        }
        executor.shutdown();
        System.out.println("Finished all threads!");

    }

    public static void buildIPMap(String inputFolder, File[] listOfFiles, HashMap<String, String> ipMap) throws IOException {
        for (File file : listOfFiles) {
            if (file.isFile() && file.getName().charAt(0) != '.') {
                BufferedReader br = new BufferedReader(new FileReader(inputFolder + "/" + file.getName()));
                String line;
                while ((line = br.readLine()) != null && line.contains(".")) {
                    String[] lines = line.split("\t");
                    ipMap.put(lines[0], lines[1]);
                }
                br.close();
            }
        }
    }


    public static void run(String ID) throws IllegalArgumentException, IOException {
        String Graph = "Graph_" + ID;
        Generate_Mutual_Contact_Graph(Graph);
    }


    public static void generateHashMap() throws IllegalArgumentException, IOException {
        BufferedReader br_Freq;
        try {
            br_Freq = new BufferedReader(new FileReader(
                    PeerCatcherConfigure.ROOT_LOCATION + "Graph_1/p2p_host_frequency/p2pFrequency.txt"
            ));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        String lineFreq;
        String[] temp;

        while ((lineFreq = br_Freq.readLine()) != null) {
            temp = lineFreq.split("\t");
            map_p2p.put(temp[0] + "\t" + temp[1], Integer.valueOf(temp[2]));
        }
        br_Freq.close();

        BufferedReader br_Id2Ip;
        try {
            br_Id2Ip = new BufferedReader(new FileReader(
                    PeerCatcherConfigure.ROOT_LOCATION + "Graph_1/mutual_contact_graph/IDtoIP.txt"
            ));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        String lineId2Ip;

        while ((lineId2Ip = br_Id2Ip.readLine()) != null) {
            temp = lineId2Ip.split("\t");
            map_Id2Ip.put(Integer.valueOf(temp[0]), temp[1]);
        }
        br_Id2Ip.close();
    }
}
