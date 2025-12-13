package vn.parking.model;

/**
 * Entity đại diện cho một chỗ đỗ xe
 */
public class ParkingSlot {
    private String slotId;      // Mã chỗ đỗ
    private String zone;        // Zone (A1, A2, B)
    private boolean isOccupied; // Đang có xe hay không
    private Vehicle vehicle;    // Xe đang đỗ (null nếu trống)
    
    public ParkingSlot(String slotId, String zone) {
        this.slotId = slotId;
        this.zone = zone;
        this.isOccupied = false;
        this.vehicle = null;
    }
    
    // Getters
    public String getSlotId() {
        return slotId;
    }
    
    public String getZone() {
        return zone;
    }
    
    public boolean isOccupied() {
        return isOccupied;
    }
    
    public Vehicle getVehicle() {
        return vehicle;
    }
    
    // Setters
    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }
    
    public void setZone(String zone) {
        this.zone = zone;
    }
    
    public void setOccupied(boolean isOccupied) {
        this.isOccupied = isOccupied;
    }
    
    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
        this.isOccupied = (vehicle != null);
    }
    
    /**
     * Giải phóng chỗ đỗ
     */
    public void release() {
        this.vehicle = null;
        this.isOccupied = false;
    }
    
    @Override
    public String toString() {
        return String.format("ParkingSlot[ID: %s, Zone: %s, Occupied: %s]", 
                slotId, zone, isOccupied ? "Yes" : "No");
    }
}

