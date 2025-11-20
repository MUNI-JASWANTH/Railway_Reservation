
package com.irctc;

import com.irctc.Entities.Ticket;
import com.irctc.Entities.Train;
import com.irctc.Entities.User;
import com.irctc.Service.LoginService;
import com.irctc.Service.UserBookingService;
import com.irctc.localdb.LocalDB;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class App {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Paths (match your project layout)
        LocalDB<User> userDB = new LocalDB<>("src/main/java/com/irctc/localdb/users.json", User.class);
        LocalDB<Train> trainDB = new LocalDB<>("src/main/java/com/irctc/localdb/trains.json", Train.class);
        LocalDB<Ticket> ticketDB = new LocalDB<>("src/main/java/com/irctc/localdb/tickets.json", Ticket.class);

        List<User> users = userDB.readList();
        List<Train> trains = trainDB.readList();
        List<Ticket> tickets = ticketDB.readList();

        LoginService loginService = new LoginService(users, userDB);
        UserBookingService bookingService = new UserBookingService(
                loginService, trains, tickets, users, trainDB, ticketDB, userDB
        );

        outer:
        while (true) {
            printHeader("IRCTC BOOKING SYSTEM");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");

            String raw = sc.nextLine().trim();
            int choice;
            try { choice = Integer.parseInt(raw); } catch (Exception e) { System.out.println("Invalid input"); continue; }

            if (choice == 1) {
                System.out.print("Name: ");
                String name = sc.nextLine().trim();
                System.out.print("Email: ");
                String email = sc.nextLine().trim();
                System.out.print("Password: ");
                String pwd = sc.nextLine();
                boolean ok = loginService.register(name, email, pwd);
                System.out.println(ok ? "Registration successful." : "Email already exists.");
            } else if (choice == 2) {
                System.out.print("Email: ");
                String email = sc.nextLine().trim();
                System.out.print("Password: ");
                String pwd = sc.nextLine();
                boolean ok = loginService.login(email, pwd);
                if (!ok) { System.out.println("Login failed."); continue; }
                System.out.println("Login successful. Welcome, " + loginService.getLoggedInUser().getName() + "!");

                // user menu
                userMenu:
                while (loginService.isLoggedIn()) {
                    printHeader("USER MENU");
                    System.out.println("1. Search trains");
                    System.out.println("2. Book ticket");
                    System.out.println("3. Cancel ticket");
                    System.out.println("4. View my tickets");
                    System.out.println("5. Show seat map");
                    System.out.println("6. Logout");
                    System.out.print("Enter choice: ");
                    String r = sc.nextLine().trim();
                    int ch;
                    try { ch = Integer.parseInt(r); } catch (Exception e) { System.out.println("Invalid input"); continue; }

                    if (ch == 1) {
                        System.out.print("Source: ");
                        String src = sc.nextLine().trim();
                        System.out.print("Destination: ");
                        String dst = sc.nextLine().trim();
                        List<Train> res = bookingService.searchTrain(src, dst);
                        if (res.isEmpty()) System.out.println("No trains found.");
                        else {
                            System.out.println("Trains found:");
                            for (Train t : res) {
                                int days = (t.getSeatAvailability()!=null)?t.getSeatAvailability().size():0;
                                int seats = (days>0)?t.getSeatAvailability().get(0).size():0;
                                System.out.printf("%s - %s | %s -> %s | Days:%d Seats/day:%d%n",
                                        t.getTrainNumber(), t.getTrainName(), t.getSource(), t.getDestination(), days, seats);
                            }
                        }
                    } else if (ch == 2) {
                        System.out.print("Enter train number: ");
                        String tnum = sc.nextLine().trim();
                        Train train = trains.stream().filter(t->t.getTrainNumber().equals(tnum)).findFirst().orElse(null);
                        if (train==null) { System.out.println("Train not found."); continue; }
                        int maxDays = (train.getSeatAvailability()!=null)?train.getSeatAvailability().size():0;
                        int seatsPerDay = (maxDays>0)?train.getSeatAvailability().get(0).size():0;
                        if (maxDays==0 || seatsPerDay==0) { System.out.println("No seat availability info for this train."); continue; }

                        System.out.println("Enter travel date (yyyy-MM-dd) or day index (0 = today).");
                        System.out.print("Date or index: ");
                        String dayInput = sc.nextLine().trim();
                        Integer dayIndex = parseDayIndex(dayInput, maxDays);
                        if (dayIndex==null) { System.out.println("Invalid date/index (out of range)."); continue; }

                        System.out.printf("Available seats for day %d: %d%n", dayIndex, countAvailable(train, dayIndex));
                        System.out.print("Enter seat index (0.." + (seatsPerDay-1) + "): ");
                        String sRaw = sc.nextLine().trim();
                        int seat;
                        try { seat = Integer.parseInt(sRaw); } catch (Exception e) { System.out.println("Invalid seat index."); continue; }
                        if (seat<0 || seat>=seatsPerDay) { System.out.println("Seat index out of range."); continue; }

                        // attempt booking
                        Ticket ticket = bookingService.bookTicket(train, dayIndex, seat);
                        if (ticket!=null) {
                            System.out.println("Booked! Ticket ID: " + ticket.getTicketId());
                        } else {
                            System.out.println("Booking failed (maybe already booked).");
                        }

                    } else if (ch == 3) {
                        System.out.print("Enter ticket ID to cancel: ");
                        String tid = sc.nextLine().trim();
                        Optional<Ticket> tOpt = tickets.stream().filter(t->t.getTicketId().equals(tid)).findFirst();
                        if (tOpt.isEmpty()) { System.out.println("Ticket not found."); continue; }
                        Ticket tk = tOpt.get();
                        Train train = trains.stream().filter(t->t.getTrainNumber().equals(tk.getTrainNumber())).findFirst().orElse(null);
                        if (train==null) { System.out.println("Train not found for ticket."); continue; }
                        int maxDays = (train.getSeatAvailability()!=null)?train.getSeatAvailability().size():0;
                        System.out.println("Enter day index or date used for booking:");
                        String dayInput = sc.nextLine().trim();
                        Integer dayIndex = parseDayIndex(dayInput, maxDays);
                        if (dayIndex==null) { System.out.println("Invalid date/index."); continue; }
                        System.out.print("Enter seat index used: ");
                        String sRaw = sc.nextLine().trim();
                        int seat;
                        try { seat = Integer.parseInt(sRaw); } catch (Exception e) { System.out.println("Invalid seat index."); continue; }
                        boolean cancelled = bookingService.cancelTicket(tid, dayIndex, seat);
                        System.out.println(cancelled ? "Cancelled." : "Cancel failed.");
                    } else if (ch == 4) {
                        List<Ticket> my = bookingService.viewMyTickets();
                        if (my==null || my.isEmpty()) System.out.println("No tickets.");
                        else {
                            System.out.println("Your tickets:");
                            for (Ticket t: my) {
                                System.out.printf("%s | %s | %s -> %s | %s | %s%n",
                                        t.getTicketId(), t.getTrainNumber(), t.getSource(), t.getDestination(), t.getTravelDate(), t.getStatus());
                            }
                        }
                    } else if (ch == 5) {
                        System.out.print("Enter train number: ");
                        String tnum = sc.nextLine().trim();
                        Train train = trains.stream().filter(t->t.getTrainNumber().equals(tnum)).findFirst().orElse(null);
                        if (train==null) { System.out.println("Train not found."); continue; }
                        int maxDays = (train.getSeatAvailability()!=null)?train.getSeatAvailability().size():0;
                        if (maxDays==0) { System.out.println("No seat data."); continue; }
                        System.out.print("Enter day index (0.." + (maxDays-1) + "): ");
                        String dayRaw = sc.nextLine().trim();
                        int dayIdx;
                        try { dayIdx = Integer.parseInt(dayRaw); } catch (Exception e) { System.out.println("Invalid day index."); continue; }
                        if (dayIdx<0 || dayIdx>=maxDays) { System.out.println("Day out of range."); continue; }
                        List<Boolean> seats = train.getSeatAvailability().get(dayIdx);
                        int limit = Math.min(seats.size(), 100);
                        System.out.println("Seat map (first " + limit + " seats): true=booked false=free");
                        for (int i=0;i<limit;i++) {
                            System.out.printf("%4d:%5s", i, seats.get(i));
                            if ((i+1)%10==0) System.out.println();
                        }
                        System.out.println();
                    } else if (ch == 6) {
                        loginService.logout();
                        System.out.println("Logged out.");
                        break userMenu;
                    } else {
                        System.out.println("Unknown option.");
                    }
                }

            } else if (choice == 3) {
                System.out.println("Goodbye.");
                break outer;
            } else {
                System.out.println("Unknown choice.");
            }
        }

        sc.close();
    }

    private static void printHeader(String title) {
        System.out.println("=======================================");
        System.out.println("  " + title);
        System.out.println("=======================================");
    }

    // Parse either day index or yyyy-MM-dd date. Returns null if invalid.
    private static Integer parseDayIndex(String input, int maxDays) {
        if (input==null || input.isBlank()) return null;
        input = input.trim();
        // try integer
        try {
            int idx = Integer.parseInt(input);
            if (idx < 0 || idx >= maxDays) return null;
            return idx;
        } catch (Exception ignored) {}
        // try date
        try {
            LocalDate d = LocalDate.parse(input, DATE_FMT);
            LocalDate today = LocalDate.now();
            long diff = ChronoUnit.DAYS.between(today, d);
            if (diff < 0 || diff >= maxDays) return null;
            return (int) diff;
        } catch (Exception ignored) {}
        return null;
    }

    private static int countAvailable(Train train, int dayIndex) {
        List<List<Boolean>> avail = train.getSeatAvailability();
        if (avail==null || dayIndex<0 || dayIndex>=avail.size()) return 0;
        int c=0;
        for (Boolean b: avail.get(dayIndex)) if (!Boolean.TRUE.equals(b)) c++;
        return c;
    }
}
