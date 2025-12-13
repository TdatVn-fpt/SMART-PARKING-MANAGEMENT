package vn.parking.model;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho vé gửi xe
 */
public class Ticket {
    private String plate;                    // Biển số xe
    private LocalDateTime entryTime;         // Thời gian vào
    private boolean isMonthlyTicket;         // Có phải thẻ tháng không
    private String zone;                     // Zone được phân (A1, A2, B)
    
    public Ticket(String plate, LocalDateTime entryTime, boolean isMonthlyTicket, String zone) {
        this.plate = plate;
        this.entryTime = entryTime;
        this.isMonthlyTicket = isMonthlyTicket;
        this.zone = zone;
    }
    
    // Getters
    public String getPlate() {
        return plate;
    }
    
    public LocalDateTime getEntryTime() {
        return entryTime;
    }
    
    public boolean isMonthlyTicket() {
        return isMonthlyTicket;
    }
    
    public String getZone() {
        return zone;
    }
    
    // Setters
    public void setPlate(String plate) {
        this.plate = plate;
    }
    
    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }
    
    public void setMonthlyTicket(boolean isMonthlyTicket) {
        this.isMonthlyTicket = isMonthlyTicket;
    }
    
    public void setZone(String zone) {
        this.zone = zone;
    }
    
    @Override
    public String toString() {
        return String.format("Ticket[Plate: %s, EntryTime: %s, Zone: %s, MonthlyTicket: %s]", 
                plate, entryTime, zone, isMonthlyTicket ? "Yes" : "No");
    }
}

