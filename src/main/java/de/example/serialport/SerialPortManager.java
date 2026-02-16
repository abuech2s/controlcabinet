package de.example.serialport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.fazecast.jSerialComm.SerialPort;

/**
 * Manager class for serial port communication using jSerialComm.
 */
public class SerialPortManager {

    private SerialPort serialPort;

    private InputStream inputStream;

    private OutputStream outputStream;

    /**
     * Lists all available serial ports on the system.
     * 
     * @return Array of available serial ports
     */
    public static SerialPort[] getAvailablePorts() {
        return SerialPort.getCommPorts();
    }

    /**
     * Opens a serial port by port name.
     * 
     * @param portName
     *            The name of the port (e.g., "COM3" on Windows, "/dev/ttyUSB0" on Linux)
     * @param baudRate
     *            Baud rate for communication
     * @return true if port opened successfully, false otherwise
     */
    public boolean openPort(String portName, int baudRate) {
        serialPort = SerialPort.getCommPort(portName);
        serialPort.setBaudRate(baudRate);
        serialPort.setNumDataBits(8);
        serialPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
        serialPort.setParity(SerialPort.NO_PARITY);

        // Set timeouts
        serialPort
            .setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING, 1000, // read
                                                                                                                 // timeout
                                                                                                                 // (ms)
                1000 // write timeout (ms)
            );

        if (serialPort.openPort()) {
            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();

            // flush io buffers and data listeners
            serialPort.flushIOBuffers();
            serialPort.flushDataListener();

            return true;
        }
        return false;
    }

    /**
     * Writes data to the serial port.
     * 
     * @param data
     *            The byte array to write
     * @throws IOException
     *             if write fails
     */
    public void write(byte[] data) throws IOException {
        if (outputStream == null) {
            throw new IOException("Port not opened");
        }
        outputStream.write(data);
        outputStream.flush();
    }

    /**
     * Writes a string to the serial port.
     * 
     * @param message
     *            The string to write
     * @throws IOException
     *             if write fails
     */
    public void write(String message) throws IOException {
        write(message.getBytes());
    }

    /**
     * Reads available data from the serial port.
     * 
     * @param buffer
     *            Buffer to read into
     * @return Number of bytes read
     * @throws IOException
     *             if read fails
     */
    public int read(byte[] buffer) throws IOException {
        if (inputStream == null) {
            throw new IOException("Port not opened");
        }
        return inputStream.read(buffer);
    }

    /**
     * Reads available bytes from the serial port.
     * 
     * @return Number of bytes available to read
     */
    public int bytesAvailable() {
        if (serialPort == null) {
            return 0;
        }
        return serialPort.bytesAvailable();
    }

    /**
     * Closes the serial port.
     */
    public void closePort() {
        if (serialPort != null && serialPort.isOpen()) {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            serialPort.closePort();
        }
    }

    /**
     * Checks if the port is currently open.
     * 
     * @return true if port is open, false otherwise
     */
    public boolean isOpen() {
        return serialPort != null && serialPort.isOpen();
    }

    /**
     * Gets the underlying SerialPort object.
     * 
     * @return The SerialPort instance
     */
    public SerialPort getSerialPort() {
        return serialPort;
    }
}
