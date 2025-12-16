package vn.parking.controller;

import vn.parking.model.*;
import vn.parking.repository.ParkingRepository;
import vn.parking.service.BillingService;
import vn.parking.service.ParkingService;
import vn.parking.view.ParkingView;
import java.util.Scanner;

/**
 * Controller điều khiển luồng xử lý của ứng dụng
 */
public class ParkingController {
    
    private ParkingRepository repository;
    private BillingService billingService;
    private ParkingService parkingService;
    private ParkingView view;
    private Scanner scanner;
    
    public ParkingController() {
        this.repository = new ParkingRepository();
        this.billingService = new BillingService(repository);
        this.parkingService = new ParkingService(repository, billingService);
        this.scanner = new Scanner(System.in);
        this.view = new ParkingView(scanner, repository);
    }
    
    /**
     * Khởi động ứng dụng
     */
    public void start() {
        // Load dữ liệu từ file khi khởi động
        repository.loadFromFile();
        
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║    SMART PARKING MANAGEMENT - CONSOLE APPLICATION    ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        
        while (true) {
            view.showMenu();
            int choice = view.inputMenuChoice();
            
            if (choice == -1) {
                System.out.println("Vui lòng nhập số hợp lệ.\n");
                continue;
            }
            
            try {
                switch (choice) {
                    case 1:
                        handleCheckIn();
                        break;
                    case 2:
                        handleCheckOutRealtime();
                        break;
                    case 3:
                        handleCheckOutSimulation();
                        break;
                    case 4:
                        handleLostTicket();
                        break;
                    case 5:
                        handleStatistics();
                        break;
                    case 6:
                        handleExit();
                        return;
                    default:
                        System.out.println("Lựa chọn không hợp lệ. Vui lòng chọn 1-6.\n");
                }
            } catch (Exception ex) {
                view.showError(ex.getMessage());
            }
        }
    }
    
    /**
     * Xử lý check-in
     */
    private void handleCheckIn() {
        System.out.println("\n--- CHECK-IN (Gửi xe) ---");
        
        VehicleType type = view.inputVehicleType();
        String plate = view.inputLicensePlate(type);
        FuelType fuelType = view.inputFuelType(type);
        boolean hasMonthlyCard = view.inputMonthlyCard();

        // Tạo đối tượng Vehicle cụ thể theo loại xe
        Vehicle vehicle;
        switch (type) {
            case CAR:
                vehicle = new Car(plate, fuelType, hasMonthlyCard);
                break;
            case BIKE:
                vehicle = new Motorbike(plate, fuelType, hasMonthlyCard);
                break;
            case BICYCLE:
            default:
                vehicle = new Bicycle(plate, fuelType, hasMonthlyCard);
                break;
        }
        Ticket ticket = parkingService.checkIn(vehicle);
        
        view.showCheckInSuccess(plate, ticket.getZone());
        
        // Tự động lưu sau mỗi thao tác
        repository.saveToFile();
    }
    
    /**
     * Xử lý check-out thực tế
     */
    private void handleCheckOutRealtime() {
        System.out.println("\n--- CHECK-OUT (Thực tế) ---");
        
        String plate = view.inputLicensePlateWithSearch();
        if (plate == null) {
            return; // Người dùng hủy
        }
        
        long fee = parkingService.checkOut(plate);
        view.showCheckOutFee(fee);
        
        // Tự động lưu sau mỗi thao tác
        repository.saveToFile();
    }
    
    /**
     * Xử lý check-out simulation
     */
    private void handleCheckOutSimulation() {
        System.out.println("\n--- CHECK-OUT (Simulation) ---");
        
        String plate = view.inputLicensePlateWithSearch();
        if (plate == null) {
            return; // Người dùng hủy
        }
        
        int[] monthsAndDays = view.inputSimulationMonthsAndDays();
        int months = monthsAndDays[0];
        int days = monthsAndDays[1];
        
        long fee = parkingService.checkOutSimulation(plate, months, days);
        view.showCheckOutFee(fee);
        
        // Tự động lưu sau mỗi thao tác
        repository.saveToFile();
    }
    
    /**
     * Xử lý mất vé
     */
    private void handleLostTicket() {
        System.out.println("\n--- BÁO MẤT VÉ ---");
        
        String plate = view.inputLicensePlateWithSearch();
        if (plate == null) {
            return; // Người dùng hủy
        }
        
        long fee = parkingService.processLostTicket(plate);
        view.showCheckOutFee(fee);
        
        // Tự động lưu sau mỗi thao tác
        repository.saveToFile();
    }
    
    /**
     * Xử lý thống kê
     */
    private void handleStatistics() {
        view.showStatistics(repository.getAllActiveTickets());
    }
    
    /**
     * Xử lý thoát
     */
    private void handleExit() {
        // Lưu dữ liệu trước khi thoát
        repository.saveToFile();
        view.showGoodbye();
        scanner.close();
    }
}

