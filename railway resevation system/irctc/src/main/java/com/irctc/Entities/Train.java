package com.irctc.Entities;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class Train {

    private String trainNumber;
    private String trainName;
    private String source;
    private String destination;
    private int totalSeats;

    // Map of station names to arrival/departure times
    private Map<String, String> schedule;

    // 2D List representing seat availability by date
    // Example: seatAvailability.get(dayIndex).get(seatIndex)
    private List<List<Boolean>> seatAvailability;

    public Train() {}

    public Train(String trainNumber, String trainName, String source, String destination,
                 int totalSeats, Map<String, String > schedule, List<List<Boolean>> seatAvailability) {
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.source = source;
        this.destination = destination;
        this.totalSeats = totalSeats;
        this.schedule = schedule;
        this.seatAvailability = seatAvailability;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public String getTrainName() {
        return trainName;
    }

    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public Map<String, String> getSchedule() {
        return schedule;
    }

public void setSchedule(Map<String, String > schedule) {
        this.schedule = schedule;
    }

    public List<List<Boolean>> getSeatAvailability() {
        return seatAvailability;
    }

    public void setSeatAvailability(List<List<Boolean>> seatAvailability) {
        this.seatAvailability = seatAvailability;
    }

    @Override
    public String toString() {
        return "Train{" +
                "trainNumber='" + trainNumber + '\'' +
                ", trainName='" + trainName + '\'' +
                ", source='" + source + '\'' +
                ", destination='" + destination + '\'' +
                ", totalSeats=" + totalSeats +
                ", schedule=" + schedule +
                ", seatAvailability=" + seatAvailability +
                '}';
    }
}
