package vn.parking.model;

/**
 * Đại diện cho xe đạp
 */
public class Bicycle extends Vehicle {

    public Bicycle(String plate, FuelType fuelType, boolean hasMonthlyCard) {
        super(plate, VehicleType.BICYCLE, fuelType, hasMonthlyCard);
    }

    public Bicycle(String plate, FuelType fuelType, boolean hasMonthlyCard, String lastPaidMonth) {
        super(plate, VehicleType.BICYCLE, fuelType, hasMonthlyCard, lastPaidMonth);
    }

    @Override
    public String getTypeName() {
        return "Xe Đạp";
    }
}

