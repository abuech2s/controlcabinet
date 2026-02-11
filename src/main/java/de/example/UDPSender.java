package de.example;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPSender implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(UDPSender.class);
    private final Gson gson = new Gson();
    public static volatile double fuelRate = -1.0;
    private DatagramSocket clientSocket = null;

    @Override
    public void run() {

        while(true) {
            try {
                clientSocket = new DatagramSocket();

                while(true) {
                    Thread.sleep(1000);

                    DataModel model = new DataModel();
                    model.setTime(System.currentTimeMillis());
                    model.setFuelRate(fuelRate);

                    byte[] sendData = gson.toJson(model).getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(Main.TARGET_IP), Main.TARGET_PORT);


                    clientSocket.send(sendPacket);

                    log.info("Send udp from {}:{} to: {}:{}(Len:{})", clientSocket.getLocalAddress(), clientSocket.getPort(), Main.TARGET_IP, Main.TARGET_PORT, sendData.length);
                }

            } catch (Exception e) {
                if (clientSocket != null) clientSocket.close();
                log.warn("Exception:", e);
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

}
