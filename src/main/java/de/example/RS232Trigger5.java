package de.example;

import com.fazecast.jSerialComm.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RS232Trigger5 implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(RS232Trigger5.class);
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    private static SerialPortManager manager = null;

    private boolean init() {
        manager = new SerialPortManager();
        SerialPort[] ports = SerialPortManager.getAvailablePorts();

        if (ports.length == 0) {
            log.warn("No port available");
            return false;
        }

        manager.openPort(ports[0].getSystemPortName(), 9600);
        return true;
    }

    @Override
    public void run() {
        log.info("RS232Trigger5 started");

        Thread readerThread = new Thread(() -> {
            byte[] buffer = new byte[1024];
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (manager != null && manager.bytesAvailable() > 0) {
                        int bytesRead = manager.read(buffer);
                        if (bytesRead > 0) {
                            if (bytesRead == 7 && (buffer[0] == 0x01 && buffer[1] == 0x03 && buffer[2] == 0x02)) {
                                int rawFuelRate = ((buffer[3] & 0xff) << 8) | (buffer[4] & 0xff);
                                double fuelRate = rawFuelRate / 10.0;
                                UDPSender.fuelRate = fuelRate;
                            } else {
                                log.warn("Invalid bytes: " + bytesToHex(buffer, bytesRead));
                                UDPSender.fuelRate = -1.0;
                            }
                        }
                    }
                    Thread.sleep(50);
                } catch (IOException | InterruptedException e) {
                    break;
                }
            }
        });
        readerThread.start();

        while (true) {
            try {
                Thread.sleep(5000);

                if (!init()) continue;
                while (true) {
                    manager.write(Main.SEND_DATA);
                    log.info("Sent data " + bytesToHex(Main.SEND_DATA, Main.SEND_DATA.length));
                    Thread.sleep(Main.TRIGGER_TIME);
                }
            } catch (Exception e) {
                log.warn("Exception in RS232Trigger5", e);
            }
        }
    }

    private static String bytesToHex(byte[] bytes, int len) {
        char[] hexChars = new char[len * 2];
        for (int j = 0; j < len; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

}
