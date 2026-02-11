package de.example;

import com.fazecast.jSerialComm.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RS232Trigger implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(RS232Trigger.class);
    private static SerialPort serialPort = null;
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    @Override
    public void run() {
        log.info("RS232Trigger started");
        while (true) {
            try {
                init();
                while (true) {

                    byte[] bytes = new byte[7]; //TODO 7 is a constant for our case here - it could be any length in general

//                    serialPort.getInputStream().skip(serialPort.bytesAvailable());
//                    serialPort.flushDataListener();


                    serialPort.writeBytes(Main.SEND_DATA, Main.SEND_DATA.length);
                    serialPort.flushDataListener();
                    log.info("Write bytes ({}): {}", Main.SEND_DATA.length, bytesToHex(Main.SEND_DATA));

                    int numRead = serialPort.readBytes(bytes, bytes.length);
                    log.info("Read bytes ({}): {}", numRead, bytesToHex(bytes));

                    int rawFuelRate = ((bytes[3] & 0xff) << 8) | (bytes[4] & 0xff);
                    UDPSender.fuelRate = rawFuelRate / 10.0;

                    Thread.sleep(Main.TRIGGER_TIME);

                }
            } catch(Exception e) {
                if (serialPort != null) serialPort.closePort();
                serialPort = null;
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
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 100, 100);
    }
}
