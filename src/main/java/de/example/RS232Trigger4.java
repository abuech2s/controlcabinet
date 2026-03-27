package de.example;

import com.fazecast.jSerialComm.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class RS232Trigger4 implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(RS232Trigger4.class);
    private static SerialPort serialPort = null;
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    @Override
    public void run() {
        log.info("RS232Trigger4 started");
        InputStream inputStream = serialPort.getInputStream();

        while(true) {
            try {
                init();
                while (true) {
                    try {
                        Thread.sleep(Main.TRIGGER_TIME);

                        byte[] bytes = new byte[7]; //TODO 7 is a constant for our case here - it could be any length in general

                        log.info("Write bytes ({}): {}", Main.SEND_DATA.length, bytesToHex(Main.SEND_DATA));
                        serialPort.writeBytes(Main.SEND_DATA, Main.SEND_DATA.length);
                        serialPort.flushDataListener();

                        int idx = 0;
                        while (inputStream.available() > 0) {
                            int readByte = inputStream.read();
                            if (readByte == -1) continue;

                            bytes[idx] = (byte)readByte;
                            idx++;
                            idx = idx % 7;
                        }

                        //Bytes [3] and [4] represents the data (fuelRate). We ignore all other bytes.
                        int rawFuelRate = ((bytes[3] & 0xff) << 8) | (bytes[4] & 0xff);
                        double fuelRate = rawFuelRate / 10.0;
                        UDPSender.fuelRate = fuelRate;

                        log.info("Read bytes: {}. Calculated fuel rate: {}", bytesToHex(bytes), fuelRate);


                    } catch(Exception e) {
                        if (serialPort != null) serialPort.closePort();
                        serialPort = null;
                        log.warn(e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.warn(e.getMessage());
            }
        }
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    private void init() throws InterruptedException, NoPortException {
        SerialPort[] serialPorts = SerialPort.getCommPorts();
        if  (serialPorts.length == 0) {
            Thread.sleep(5000);
            throw new NoPortException("No ports are found");
        }

        log.info("Found {} ports:", serialPorts.length);
        for (SerialPort port : serialPorts) {
            log.warn("   -> " + port.getSystemPortName());
        }

        serialPort = serialPorts[0];

        log.info("Open port: {}", serialPort.getSystemPortName());
        serialPort.setComPortParameters(9600, 8, 1, SerialPort.NO_PARITY);
        serialPort.openPort();
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0);
        serialPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
    }
}
