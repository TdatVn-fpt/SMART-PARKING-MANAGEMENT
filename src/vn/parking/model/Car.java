package vn.parking.model;

/**
 * Đại diện cho xe ô tô
 */
public class Car extends Vehicle {

    public Car(String plate, FuelType fuelType, boolean hasMonthlyCard) {
        super(plate, VehicleType.CAR, fuelType, hasMonthlyCard);
    }

    public Car(String plate, FuelType fuelType, boolean hasMonthlyCard, String lastPaidMonth) {
        super(plate, VehicleType.CAR, fuelType, hasMonthlyCard, lastPaidMonth);
    }

    @Override
    public String getTypeName() {
        return "Ô Tô";
    }
}

