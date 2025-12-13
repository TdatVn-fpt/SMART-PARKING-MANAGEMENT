package vn.parking.main;

import vn.parking.controller.ParkingController;

/**
 * Main class - Chỉ khởi tạo Controller và gọi start()
 * Không chứa logic nhập xuất
 */
public class Main {
    
    public static void main(String[] args) {
        ParkingController controller = new ParkingController();
        controller.start();
    }
}
