package com.irctc.Service;

import com.irctc.Entities.Ticket;
import com.irctc.Entities.Train;
import com.irctc.Entities.User;
import com.irctc.localdb.LocalDB;

import java.text.SimpleDateFormat;
import java.util.*;

public class UserBookingService {

    private final List<Train> trainsDb;
    private final List<Ticket> ticketsDb;
    private final List<User> usersDb;

    private final LocalDB<Train> trainDB;
    private final LocalDB<Ticket> ticketDB;
    private final LocalDB<User> userDB;

    private final LoginService loginService;

    public UserBookingService(LoginService loginService,
                              List<Train> trainsDb,
                              List<Ticket> ticketsDb,
                              List<User> usersDb,
                              LocalDB<Train> trainDB,
                              LocalDB<Ticket> ticketDB,
                              LocalDB<User> userDB) {

        this.loginService = loginService;
        this.trainsDb = trainsDb;
        this.ticketsDb = ticketsDb;
        this.usersDb = usersDb;
        this.trainDB = trainDB;
        this.ticketDB = ticketDB;
        this.userDB = userDB;
    }

    // SEARCH TRAIN
    public List<Train> searchTrain(String source, String destination) {
        List<Train> result = new ArrayList<>();
        if (source == null || destination == null) return result;

        for (Train t : trainsDb) {
            if (t.getSource() != null && t.getDestination() != null &&
                    t.getSource().equalsIgnoreCase(source) &&
                    t.getDestination().equalsIgnoreCase(destination)) {
                result.add(t);
            }
        }
        return result;
    }

    // CHECK SEAT (defensive)
    public boolean isSeatAvailable(Train train, int dayIndex, int seatIndex) {
        if (train == null) return false;
        List<List<Boolean>> seatsByDay = train.getSeatAvailability();
        if (seatsByDay == null) return false;
        if (dayIndex < 0 || dayIndex >= seatsByDay.size()) return false;
        List<Boolean> daySeats = seatsByDay.get(dayIndex);
        if (daySeats == null) return false;
        if (seatIndex < 0 || seatIndex >= daySeats.size()) return false;
        return !Boolean.TRUE.equals(daySeats.get(seatIndex));
    }

    // BOOK TICKET (synchronized to reduce races in CLI mode)
    public synchronized Ticket bookTicket(Train train, int dayIndex, int seatIndex) {

        if (!loginService.isLoggedIn()) {
            System.out.println("Please log in first!");
            return null;
        }

        if (train == null) {
            System.out.println("Invalid train.");
            return null;
        }

        // bounds checks
        List<List<Boolean>> avail = train.getSeatAvailability();
        if (avail == null || dayIndex < 0 || dayIndex >= avail.size()) {
            System.out.println("Invalid day index.");
            return null;
        }
        List<Boolean> seats = avail.get(dayIndex);
        if (seats == null || seatIndex < 0 || seatIndex >= seats.size()) {
            System.out.println("Invalid seat index.");
            return null;
        }

        if (!isSeatAvailable(train, dayIndex, seatIndex)) {
            System.out.println("Seat not available!");
            return null;
        }

        User user = loginService.getLoggedInUser();
        if (user == null) {
            System.out.println("User session is invalid.");
            return null;
        }

        // Mark the seat booked
        seats.set(seatIndex, true);

        Ticket ticket = new Ticket();
        ticket.setTicketId(generateTicketId());
        ticket.setUserId(user.getUserId());
        ticket.setTrainNumber(train.getTrainNumber());
        ticket.setSource(train.getSource());
        ticket.setDestination(train.getDestination());
        ticket.setTravelDate(getTodayDate()); // keep as today or change to accept explicit date
        ticket.setStatus("BOOKED");

        // Save ticket
        ticketsDb.add(ticket);
        if (ticketDB != null) ticketDB.writeList(ticketsDb);

        // Ensure user's ticket id list exists
        if (user.getBookedTickets() == null) {
            user.setBookedTickets(new ArrayList<>());
        }
        user.getBookedTickets().add(ticket.getTicketId());
        if (userDB != null) userDB.writeList(usersDb);

        // Save updated train
        if (trainDB != null) trainDB.writeList(trainsDb);

        return ticket;
    }

    // CANCEL (synchronized)
    public synchronized boolean cancelTicket(String ticketId, int dayIndex, int seatIndex) {

        if (!loginService.isLoggedIn()) {
            System.out.println("Please login!");
            return false;
        }

        if (ticketId == null) return false;

        User user = loginService.getLoggedInUser();
        if (user == null) {
            System.out.println("Invalid session.");
            return false;
        }

        Optional<Ticket> ticketOpt = ticketsDb.stream()
                .filter(t -> ticketId.equals(t.getTicketId()))
                .findFirst();

        if (ticketOpt.isEmpty()) {
            System.out.println("Invalid ticket!");
            return false;
        }

        Ticket ticket = ticketOpt.get();

        if (!ticket.getUserId().equals(user.getUserId())) {
            System.out.println("This ticket does not belong to you!");
            return false;
        }

        Train train = trainsDb.stream()
                .filter(t -> t.getTrainNumber().equals(ticket.getTrainNumber()))
                .findFirst()
                .orElse(null);

        if (train == null) {
            System.out.println("Train not found!");
            return false;
        }

        // validate indices and free seat
        List<List<Boolean>> avail = train.getSeatAvailability();
        if (avail == null || dayIndex < 0 || dayIndex >= avail.size()) {
            System.out.println("Invalid day index.");
            return false;
        }
        List<Boolean> seats = avail.get(dayIndex);
        if (seats == null || seatIndex < 0 || seatIndex >= seats.size()) {
            System.out.println("Invalid seat index.");
            return false;
        }

        // Free seat (if it is currently booked)
        seats.set(seatIndex, false);
        if (trainDB != null) trainDB.writeList(trainsDb);

        // Update ticket status
        ticket.setStatus("CANCELLED");
        if (ticketDB != null) ticketDB.writeList(ticketsDb);

        // Remove ticket id from user list
        if (user.getBookedTickets() != null) {
            user.getBookedTickets().remove(ticketId);
            if (userDB != null) userDB.writeList(usersDb);
        }

        return true;
    }

    // View my tickets (safe - return empty list if none)
    public List<Ticket> viewMyTickets() {
        if (!loginService.isLoggedIn()) return Collections.emptyList();

        User user = loginService.getLoggedInUser();
        if (user == null || user.getBookedTickets() == null) return Collections.emptyList();

        List<Ticket> result = new ArrayList<>();
        for (String id : user.getBookedTickets()) {
            ticketsDb.stream()
                    .filter(t -> id.equals(t.getTicketId()))
                    .findFirst()
                    .ifPresent(result::add);
        }
        return result;
    }

    private String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    private String generateTicketId() {
        return "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
