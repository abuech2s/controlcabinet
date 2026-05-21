package de.example;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DataModel {

    private String source = "unknown";
    private long timestampoflastdata;
    private String sensor = "Cabinet";
    private double fuelRate;

}
