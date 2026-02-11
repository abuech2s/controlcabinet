package de.example;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DataModel {

    private String source = "unknown";
    private long time;
    private String sensor = "ControlCabinet RS232";
    private double fuelRate;

}
