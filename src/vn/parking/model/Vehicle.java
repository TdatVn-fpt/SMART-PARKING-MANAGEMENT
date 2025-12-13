package vn.parking.model;

/**
 * Entity đại diện cho một phương tiện
 */
public class Vehicle {
    private String plate;           // Biển số xe
    private VehicleType type;       // Loại xe (CAR, BIKE, BICYCLE)
    private FuelType fuelType;      // Loại nhiên liệu (GASOLINE, ELECTRIC, NONE)
    private boolean hasMonthlyCard; // Có thẻ tháng hay không
    private String lastPaidMonth;   // Tháng đã đóng tiền vé tháng (Format: "MM/yyyy", ví dụ: "11/2025")
    
    public Vehicle(String plate, VehicleType type, FuelType fuelType, boolean hasMonthlyCard) {
        this(plate, type, fuelType, hasMonthlyCard, null);
    }
    
    public Vehicle(String plate, VehicleType type, FuelType fuelType, boolean hasMonthlyCard, String lastPaidMonth) {
        this.plate = plate;
        this.type = type;
        this.fuelType = fuelType;
        this.hasMonthlyCard = hasMonthlyCard;
        this.lastPaidMonth = lastPaidMonth;
    }
    
    // Getters
    public String getPlate() {
        return plate;
    }
    
    public VehicleType getType() {
        return type;
    }
    
    public FuelType getFuelType() {
        return fuelType;
    }
    
    public boolean hasMonthlyCard() {
        return hasMonthlyCard;
    }
    
    public String getLastPaidMonth() {
        return lastPaidMonth;
    }
    
    // Setters
    public void setPlate(String plate) {
        this.plate = plate;
    }
    
    public void setType(VehicleType type) {
        this.type = type;
    }
    
    public void setFuelType(FuelType fuelType) {
        this.fuelType = fuelType;
    }
    
    public void setHasMonthlyCard(boolean hasMonthlyCard) {
        this.hasMonthlyCard = hasMonthlyCard;
    }
    
    public void setLastPaidMonth(String lastPaidMonth) {
        this.lastPaidMonth = lastPaidMonth;
    }
    
    /**
     * Chuyển đổi Vehicle thành chuỗi CSV
     * Format: type,licensePlate,fuelType,ticketType,lastPaidMonth
     */
    public String toCSV() {
        return String.format("%s,%s,%s,%s,%s",
                type.name(),
                plate,
                fuelType.name(),
                hasMonthlyCard ? "MONTHLY" : "SINGLE",
                lastPaidMonth != null ? lastPaidMonth : ""
        );
    }
    
    @Override
    public String toString() {
        return String.format("Vehicle[Plate: %s, Type: %s, Fuel: %s, MonthlyCard: %s, LastPaidMonth: %s]", 
                plate, type, fuelType, hasMonthlyCard ? "Yes" : "No", 
                lastPaidMonth != null ? lastPaidMonth : "None");
    }
}

