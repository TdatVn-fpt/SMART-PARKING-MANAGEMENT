package vn.parking.service;

import vn.parking.model.Ticket;
import vn.parking.model.Vehicle;

import java.util.Collection;

/**
 * Service interface định nghĩa các thao tác chính với bãi đỗ xe
 */
public interface IParkingService {

    Ticket checkIn(Vehicle vehicle);

    long checkOut(String plate);

    /**
     * Lấy danh sách tất cả phương tiện đang được quản lý trong hệ thống
     */
    Collection<Vehicle> getVehicleList();
}
