package de.example.serialport;

import java.io.IOException;
import java.util.Scanner;

import com.fazecast.jSerialComm.SerialPort;

/**
 * Example application demonstrating serial port usage.
 */
public class SerialPortExample {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static void main(String[] args) {
        System.out.println("=== Serial Port Example ===\n");

        // List available ports
        SerialPort[] ports = SerialPortManager.getAvailablePorts();
        System.out.println("Available Serial Ports:");
        for (int i = 0; i < ports.length; i++) {
            System.out
                .println((i + 1) + ". " + ports[i].getSystemPortName() + " - " + ports[i].getDescriptivePortName());
        }

        if (ports.length == 0) {
            System.out.println("No serial ports found!");
            return;
        }

        // Interactive mode
        Scanner scanner = new Scanner(System.in);
        System.out.print("\nEnter port number to open (or 0 to exit): ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        if (choice <= 0 || choice > ports.length) {
            System.out.println("Exiting...");
            return;
        }

        String portName = ports[choice - 1].getSystemPortName();

        System.out.print("Enter baud rate (default 9600): ");
        String baudInput = scanner.nextLine();
        int baudRate = baudInput.isEmpty() ? 9600 : Integer.parseInt(baudInput);

        // Open the port
        SerialPortManager manager = new SerialPortManager();
        if (manager.openPort(portName, baudRate)) {
            System.out.println("Port opened successfully: " + portName + " @ " + baudRate + " baud");

            // Simple echo loop
            System.out.println("\nEntering echo mode. Type messages (in hex) and press Enter.");
            System.out
                .println(
                    "Example: 01 03 00 35 00 01 94 04\nJust copy and paste this example into the console and press Enter.\n");
            System.out.println("Type 'quit' to exit.\n");

            // Start a reader thread and let it running while the port is opened
            Thread readerThread = new Thread(() -> {
                byte[] buffer = new byte[1024];
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        if (manager.bytesAvailable() > 0) {
                            int bytesRead = manager.read(buffer);
                            if (bytesRead > 0) {
                                // String received = new String(buffer, 0, bytesRead);
                                String received = bytesToHex(buffer, bytesRead);
                                System.out.println("Received: " + received);
                            }
                        }
                        Thread.sleep(50);
                    }
                    catch (IOException | InterruptedException e) {
                        break;
                    }
                }
            });
            readerThread.start();

            // Main write loop
            while (true) {
                String message = scanner.nextLine();
                if ("quit".equalsIgnoreCase(message)) {
                    break;
                }

                try {
                    message = message.replaceAll(" ", "");
                    byte[] decodeHexString = decodeHexString(message);
                    manager.write(decodeHexString);
                    // manager.write(message + "\n");
                    System.out.println("Sent: " + message);
                }
                catch (IOException e) {
                    System.err.println("Error writing to port: " + e.getMessage());
                }
                catch (Exception e) {
                    System.err.println("Error writing to port: " + e.getMessage());
                }
            }

            // Cleanup
            readerThread.interrupt();
            manager.closePort();
            System.out.println("Port closed.");

        }
        else {
            System.err.println("Failed to open port: " + portName);
        }

        scanner.close();
    }

    private static byte[] decodeHexString(String hexString) {
        if (hexString.length() % 2 == 1) {
            throw new IllegalArgumentException("Invalid hexadecimal String supplied.");
        }

        byte[] bytes = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length(); i += 2) {
            bytes[i / 2] = hexToByte(hexString.substring(i, i + 2));
        }
        return bytes;
    }

    private static byte hexToByte(String hexString) {
        int firstDigit = toDigit(hexString.charAt(0));
        int secondDigit = toDigit(hexString.charAt(1));
        return (byte) ((firstDigit << 4) + secondDigit);
    }

    private static int toDigit(char hexChar) {
        int digit = Character.digit(hexChar, 16);
        if (digit == -1) {
            throw new IllegalArgumentException("Invalid Hexadecimal Character: " + hexChar);
        }
        return digit;
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
