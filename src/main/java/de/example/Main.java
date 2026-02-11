package de.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.UnknownHostException;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static String TARGET_IP = "192.168.1.5";
    public static int TARGET_PORT = 8890;
    public static int RS232TRIGGER = 1;
    public static String LOCAL_IP = "";
    public static byte[] SEND_DATA = new byte[]{(byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x35, (byte) 0x00, (byte) 0x01, (byte) 0x94, (byte) 0x04};
    public static int TRIGGER_TIME = 1000;

    public static void main(String[] args) {

        log.info("Initializing...");

        if (args.length > 0) {
            for (String arg : args) {
                if (arg.contains("=")) {
                    String[] elem = arg.split("=");
                    switch (elem[0].toLowerCase()) {
                        case "ip":
                            TARGET_IP = elem[1];
                            break;
                        case "port":
                            TARGET_PORT = Integer.parseInt(elem[1]);
                            break;
                        case "trigger":
                            RS232TRIGGER = Integer.parseInt(elem[1]);
                            break;
                        case "triggertime":
                            TRIGGER_TIME = Integer.parseInt(elem[1]);
                            break;
                        default:
                            log.info("Could not parse command: {}", arg);
                    }
                }
            }
        } else {
            printHelp();
            System.exit(0);
        }

        log.info("Take target IP: " + TARGET_IP);
        log.info("Take target Port: " + TARGET_PORT);
        log.info("Take RS232 trigger: " + RS232TRIGGER);
        log.info("Take TRIGGER time: " + TRIGGER_TIME);

        //Determining local ip address
        try {
            LOCAL_IP = Inet4Address.getLocalHost().getHostAddress();
            log.warn("Local IP address is set to : " + LOCAL_IP);
        } catch (UnknownHostException e) {
            log.warn("Could not resolve local host address");
            throw new RuntimeException(e);
        }

        //All these implementations are just for testing/debugging. Cleanup needed at the end.
        Thread triggerThread = null;
        switch (RS232TRIGGER) {
            case 1:
                triggerThread = new Thread(new RS232Trigger());
                break;
            case 2:
                triggerThread = new Thread(new RS232Trigger2());
                break;
            case 3:
                triggerThread = new Thread(new RS232Trigger3());
                break;
            default:
                triggerThread = new Thread(new RS232Trigger());
        }

        triggerThread.start();

        Thread udpSenderThread = new Thread(new UDPSender());
        udpSenderThread.start();

        log.info("Application started.");
    }

    private static void printHelp() {
        log.info("java -jar *.jar [ip=<ip>] [port=<port>] [trigger={1,2,3}] [triggertime=1000]");
    }

}