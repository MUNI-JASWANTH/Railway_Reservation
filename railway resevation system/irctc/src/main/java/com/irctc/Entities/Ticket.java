package com.irctc.Entities;

public class Ticket {

    private String ticketId;
    private String userId;
    private String trainNumber;
    private String source;
    private String destination;
    private String travelDate;  // yyyy-MM-dd
    private String status;      // BOOKED / CANCELLED

    public Ticket() {}

    public Ticket(String ticketId, String userId, String trainNumber, String source,
                  String destination, String travelDate, String status) {
        this.ticketId = ticketId;
        this.userId = userId;
        this.trainNumber = trainNumber;
        this.source = source;
        this.destination = destination;
        this.travelDate = travelDate;
        this.status = status;
    }

    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTrainNumber() { return trainNumber; }
    public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getTravelDate() { return travelDate; }
    public void setTravelDate(String travelDate) { this.travelDate = travelDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "Ticket{" +
                "ticketId='" + ticketId + '\'' +
                ", userId='" + userId + '\'' +
                ", trainNumber='" + trainNumber + '\'' +
                ", source='" + source + '\'' +
                ", destination='" + destination + '\'' +
                ", travelDate='" + travelDate + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
