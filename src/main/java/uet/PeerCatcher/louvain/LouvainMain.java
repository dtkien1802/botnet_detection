package uet.PeerCatcher.louvain;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import uet.PeerCatcher.config.PeerCatcherConfigure;
import uet.PeerCatcher.main.FileModifier;

public class LouvainMain {

    public static void Louvain(String ID, double resolution_) throws IOException {
        String Graph = "Graph_" + ID;
        boolean update;
        double modularity, maxModularity, resolution, resolution2;
        int i, j, modularityFunction, nIterations, nRandomStarts;
        int[] cluster;
        long randomSeed;
        Network network;
        Random random;

        File f = new File(PeerCatcherConfigure.ROOT_LOCATION + Graph + "/louvain_communities_detection");
        if (!f.exists()) {
            if (f.mkdir()) {
                System.out.println("Directory " + PeerCatcherConfigure.ROOT_LOCATION + Graph
                        + "/louvain_communities_detection" + " is created!");
            } else {
                System.out.println("Failed to create directory!");
            }
        }

        modularityFunction = 1;
        resolution = resolution_;
        nRandomStarts = 100;
        nIterations = 100;
        randomSeed = 0;

        String inputFileName = PeerCatcherConfigure.ROOT_LOCATION + Graph + "/mutual_contact_graph/LouvainInput.txt";
        String outputFileName = PeerCatcherConfigure.ROOT_LOCATION + Graph + "/louvain_communities_detection/" + Graph + "_"
                + resolution + ".txt";

        network = readInputFile(inputFileName, modularityFunction);

        resolution2 = resolution / network.getTotalEdgeWeight();

        cluster = null;
        maxModularity = Double.NEGATIVE_INFINITY;
        random = new Random(randomSeed);
        for (i = 0; i < nRandomStarts; i++) {

            network.initSingletonClusters();

            j = 0;
            do {

                update = network.runLouvainAlgorithm(resolution2, random);
                j++;

                modularity = network.calcQualityFunction(resolution2);

            } while ((j < nIterations) && update);

            if (modularity > maxModularity) {
                network.orderClustersByNNodes();
                cluster = network.getClusters();
                maxModularity = modularity;
            }

        }

        assert cluster != null;
        writeOutputFile(outputFileName, cluster);

        System.out.println("Communities are OK!");
    }

    private static Network readInputFile(String fileName, int modularityFunction) throws IOException {
        BufferedReader bufferedReader;
        double[] edgeWeight1, edgeWeight2, nodeWeight;
        int i, j, nEdges, nLines, nNodes;
        int[] firstNeighborIndex, neighbor, nNeighbors, node1, node2;
        Network network;
        String[] splittedLine;

        bufferedReader = new BufferedReader(new FileReader(fileName));

        nLines = 0;
        while (bufferedReader.readLine() != null)
            nLines++;

        bufferedReader.close();

        bufferedReader = new BufferedReader(new FileReader(fileName));

        node1 = new int[nLines];
        node2 = new int[nLines];
        edgeWeight1 = new double[nLines];
        i = -1;
        for (j = 0; j < nLines; j++) {
            splittedLine = bufferedReader.readLine().split("\t");
            node1[j] = Integer.parseInt(splittedLine[0]);
            if (node1[j] > i)
                i = node1[j];
            node2[j] = Integer.parseInt(splittedLine[1]);
            if (node2[j] > i)
                i = node2[j];
            edgeWeight1[j] = (splittedLine.length > 2) ? Double.parseDouble(splittedLine[2]) : 1;
        }
        nNodes = i + 1;

        bufferedReader.close();

        nNeighbors = new int[nNodes];
        for (i = 0; i < nLines; i++)
            if (node1[i] < node2[i]) {
                nNeighbors[node1[i]]++;
                nNeighbors[node2[i]]++;
            }

        firstNeighborIndex = new int[nNodes + 1];
        nEdges = 0;
        for (i = 0; i < nNodes; i++) {
            firstNeighborIndex[i] = nEdges;
            nEdges += nNeighbors[i];
        }
        firstNeighborIndex[nNodes] = nEdges;

        neighbor = new int[nEdges];
        edgeWeight2 = new double[nEdges];
        Arrays.fill(nNeighbors, 0);
        for (i = 0; i < nLines; i++)
            if (node1[i] < node2[i]) {
                j = firstNeighborIndex[node1[i]] + nNeighbors[node1[i]];
                neighbor[j] = node2[i];
                edgeWeight2[j] = edgeWeight1[i];
                nNeighbors[node1[i]]++;
                j = firstNeighborIndex[node2[i]] + nNeighbors[node2[i]];
                neighbor[j] = node1[i];
                edgeWeight2[j] = edgeWeight1[i];
                nNeighbors[node2[i]]++;
            }

        if (modularityFunction == 1) {
            nodeWeight = new double[nNodes];
            for (i = 0; i < nEdges; i++)
                nodeWeight[neighbor[i]] += edgeWeight2[i];
            network = new Network(nNodes, firstNeighborIndex, neighbor, edgeWeight2, nodeWeight);
        } else
            network = new Network(nNodes, firstNeighborIndex, neighbor, edgeWeight2);

        return network;
    }

    public static void run(String ID) throws IllegalArgumentException, IOException {
        String Graph = "Graph_" + ID;
        FileModifier.deleteDir(new File(PeerCatcherConfigure.ROOT_LOCATION + Graph + "/louvain_communities_detection"));
        double i = PeerCatcherConfigure.LOUVAIN_COMMUNITY_DETECTION_RESOLUTION;
        LouvainMain.Louvain(ID, i);
    }

    private static void writeOutputFile(String fileName, int[] cluster) throws IOException {
        BufferedWriter bufferedWriter;
        int i;

        bufferedWriter = new BufferedWriter(new FileWriter(fileName));

        for (i = 0; i < cluster.length; i++) {
            bufferedWriter.write(i + "," + cluster[i]);
            bufferedWriter.newLine();
        }

        bufferedWriter.close();
    }
}