package uet.PeerCatcher.config;

public class PeerCatcherConfigure {
    public static int P2P_HOST_DETECTION_THRESHOLD_DEFAULT = 30;
    public static int P2P_HOST_DETECTION_THRESHOLD_NumberOfIPs = 0;
    public static int FREQUENCY_THRESHOLD = 4;
    public static double MUTUAL_CONTACT_SCORE_THRESHOLD = 0;
    public static double LOUVAIN_COMMUNITY_DETECTION_RESOLUTION = 1.0;
    public static String ROOT_LOCATION = "/home/dtkien/Downloads/datachay/sality/";

    public static int PORT_DIVERSITY_THRESHOLD = 2100;

    //    avgmcr
    public static double[] BOTNET_DETECTION_THRESHOLD_MCS_SET = { 0.2 };

    //    avgddr
    public static double[] BOTNET_DETECTION_THRESHOLD_BGP_SET = { 0.9 };


}
