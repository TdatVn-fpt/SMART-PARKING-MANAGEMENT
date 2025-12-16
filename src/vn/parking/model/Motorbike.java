package vn.parking.model;

/**
 * Đại diện cho xe máy
 */
public class Motorbike extends Vehicle {

    public Motorbike(String plate, FuelType fuelType, boolean hasMonthlyCard) {
        super(plate, VehicleType.BIKE, fuelType, hasMonthlyCard);
    }

    public Motorbike(String plate, FuelType fuelType, boolean hasMonthlyCard, String lastPaidMonth) {
        super(plate, VehicleType.BIKE, fuelType, hasMonthlyCard, lastPaidMonth);
    }

    @Override
    public String getTypeName() {
        return "Xe Máy";
    }
}

