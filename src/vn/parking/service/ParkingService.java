package vn.parking.service;

import vn.parking.model.*;
import vn.parking.repository.ParkingRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;

/**
 * Service xử lý logic nghiệp vụ đỗ xe
 */
public class ParkingService implements IParkingService {
    
    private ParkingRepository repository;
    private BillingService billingService;
    
    private static final long LOST_TICKET_FINE = 50000; // Phạt mất vé: 50.000 VNĐ
    
    public ParkingService(ParkingRepository repository, BillingService billingService) {
        this.repository = repository;
        this.billingService = billingService;
    }
    
    /**
     * Tìm kiếm thông minh: Tìm ticket theo biển số hoặc phần biển số
     * @param plate Biển số hoặc phần biển số
     * @return Ticket nếu tìm thấy, null nếu không
     */
    public Ticket findTicketSmart(String plate) {
        // Tìm chính xác trước
        Ticket ticket = repository.findTicketByPlate(plate);
        if (ticket != null) {
            return ticket;
        }
        return null;
    }
    
    /**
     * Xử lý check-in: Xe vào bãi đỗ
     * Tự động phân zone theo quy tắc:
     * - CAR -> Gate B -> Zone B
     * - BIKE/BICYCLE -> Gate A
     *   - ELECTRIC/BICYCLE -> Zone A1 (ưu tiên)
     *   - GASOLINE -> Zone A2
     * 
     * @param vehicle Xe vào
     * @return Ticket được tạo
     */
    @Override
    public Ticket checkIn(Vehicle vehicle) {
        // Kiểm tra xe đã đỗ chưa (sử dụng chuẩn hóa để so sánh)
        if (repository.isVehicleParked(vehicle.getPlate())) {
            throw new IllegalStateException("Xe " + vehicle.getPlate() + " đã đang đỗ trong bãi!");
        }
        
        // Xác định zone theo quy tắc
        String zone = determineZone(vehicle);
        
        // Tạo ticket
        LocalDateTime entryTime = LocalDateTime.now();
        Ticket ticket = new Ticket(
            vehicle.getPlate(),
            entryTime,
            vehicle.hasMonthlyCard(),
            zone
        );
        
        // Lưu vào repository
        repository.saveTicket(ticket);
        repository.saveVehicle(vehicle);
        
        // In thông báo
        System.out.println("✓ Xe " + vehicle.getPlate() + " vào bãi -> Vào Zone " + zone);
        
        return ticket;
    }
    
    /**
     * Xác định zone cho xe dựa trên loại xe và nhiên liệu
     */
    private String determineZone(Vehicle vehicle) {
        VehicleType type = vehicle.getType();
        FuelType fuelType = vehicle.getFuelType();
        
        // Ô tô: Bắt buộc đi Cổng B -> Zone B
        if (type == VehicleType.CAR) {
            return "B";
        }
        
        // Xe máy/Xe đạp: Đi Cổng A
        // Ưu tiên: Xe ELECTRIC và BICYCLE -> Zone A1
        // Xe GASOLINE -> Zone A2
        if (fuelType == FuelType.ELECTRIC || type == VehicleType.BICYCLE) {
            return "A1";
        } else if (fuelType == FuelType.GASOLINE) {
            return "A2";
        }
        
        // Mặc định (trường hợp NONE không phải BICYCLE)
        return "A1";
    }
    
    /**
     * Xử lý check-out: Xe ra khỏi bãi đỗ (Thực tế)
     * 
     * @param plate Biển số xe (đã được chuẩn hóa từ View)
     * @return Tổng phí phải trả
     */
    @Override
    public long checkOut(String plate) {
        // Tìm ticket (plate đã được chuẩn hóa và trả về biển số gốc từ View)
        Ticket ticket = repository.findTicketByPlate(plate);
        
        if (ticket == null) {
            throw new IllegalStateException("Không tìm thấy xe " + plate + " trong bãi!");
        }
        
        Vehicle vehicle = repository.findVehicleByPlate(plate);
        if (vehicle == null) {
            throw new IllegalStateException("Không tìm thấy thông tin xe " + plate);
        }
        
        // Tính thời gian đỗ (giờ) - Thực tế
        LocalDateTime exitTime = LocalDateTime.now();
        LocalDateTime entryTime = ticket.getEntryTime();
        long hours = ChronoUnit.HOURS.between(entryTime, exitTime);
        double duration = hours;
        
        // Tính phí với logic mới
        long fee = billingService.calculateFee(vehicle, exitTime, entryTime);
        
        // Nếu là vé tháng và phải thu tiền (fee > 0), cập nhật lịch sử đóng tiền
        if (vehicle.hasMonthlyCard() && fee > 0) {
            String currentMonth = billingService.getCurrentMonth(exitTime);
            repository.updatePaymentStatus(plate, currentMonth); // Lưu vào sổ cái lịch sử (cũ)
            repository.saveMonthlyPayment(plate, currentMonth);  // Lưu vào file monthly_payment.csv
        }
        
        // Lấy lastPaidMonth từ sổ cái để hiển thị
        String lastPaidMonth = repository.getLastPaidMonth(plate);
        
        // In hóa đơn
        printInvoice(plate, entryTime, exitTime, duration, fee, vehicle.hasMonthlyCard(), lastPaidMonth);
        
        // Xóa ticket khỏi repository
        repository.removeTicket(plate);
        
        return fee;
    }

    /**
     * Trả về danh sách tất cả phương tiện đang được quản lý
     * (wrapper mỏng cho repository để tuân thủ IParkingService)
     */
    @Override
    public Collection<Vehicle> getVehicleList() {
        return repository.getAllVehicles();
    }
    
    /**
     * Xử lý check-out Simulation: Cho phép nhập tháng và ngày thủ công để test
     * 
     * @param plate Biển số xe (đã được chuẩn hóa từ View)
     * @param months Số tháng đã trôi qua (dùng để giả lập tháng check-out cho vé tháng)
     * @param days Số ngày lẻ đã trôi qua
     * @return Tổng phí phải trả
     */
    public long checkOutSimulation(String plate, int months, int days) {
        // Tìm ticket (plate đã được chuẩn hóa và trả về biển số gốc từ View)
        Ticket ticket = repository.findTicketByPlate(plate);
        
        if (ticket == null) {
            throw new IllegalStateException("Không tìm thấy xe " + plate + " trong bãi!");
        }
        
        Vehicle vehicle = repository.findVehicleByPlate(plate);
        if (vehicle == null) {
            throw new IllegalStateException("Không tìm thấy thông tin xe " + plate);
        }
        
        // Tính tổng số ngày: 1 Tháng = 30 ngày
        int totalDays = (months * 30) + days;
        
        // Giả lập: Lùi entryTime về quá khứ
        LocalDateTime fakeEntryTime = LocalDateTime.now().minusDays(totalDays);
        ticket.setEntryTime(fakeEntryTime);
        
        // Giả lập checkoutTime: Lùi về quá khứ theo số tháng/ngày nhập vào
        LocalDateTime fakeCheckoutTime = LocalDateTime.now().minusMonths(months).minusDays(days);
        
        // Tính phí với logic mới cho Simulation
        long fee = billingService.calculateSimulationFee(vehicle, months, days, fakeCheckoutTime, fakeEntryTime);
        
        // Nếu là vé tháng và phải thu tiền (fee > 0), cập nhật lịch sử đóng tiền
        if (vehicle.hasMonthlyCard() && fee > 0) {
            String currentMonth = billingService.getCurrentMonth(fakeCheckoutTime);
            repository.updatePaymentStatus(plate, currentMonth); // Lưu vào sổ cái lịch sử (cũ)
            repository.saveMonthlyPayment(plate, currentMonth);  // Lưu vào file monthly_payment.csv
        }
        
        LocalDateTime exitTime = LocalDateTime.now();
        
        // Lấy lastPaidMonth từ sổ cái để hiển thị
        String lastPaidMonth = repository.getLastPaidMonth(plate);
        
        // In hóa đơn simulation với thông tin chi tiết
        printSimulationInvoice(plate, fakeEntryTime, exitTime, months, days, totalDays, fee, 
                vehicle.hasMonthlyCard(), lastPaidMonth);
        
        // Xóa ticket khỏi repository
        repository.removeTicket(plate);
        
        return fee;
    }
    
    /**
     * Xử lý trường hợp mất vé
     * 
     * @param plate Biển số xe (đã được chuẩn hóa từ View)
     * @return Tổng phí phải trả (bao gồm phạt mất vé)
     */
    public long processLostTicket(String plate) {
        // Tìm xe trong repository (plate đã được chuẩn hóa và trả về biển số gốc từ View)
        Ticket ticket = repository.findTicketByPlate(plate);
        
        if (ticket == null) {
            throw new IllegalStateException("Không tìm thấy xe " + plate + " trong bãi!");
        }
        
        Vehicle vehicle = repository.findVehicleByPlate(plate);
        if (vehicle == null) {
            throw new IllegalStateException("Không tìm thấy thông tin xe " + plate);
        }
        
        // Lấy entryTime thực tế từ ticket
        LocalDateTime entryTime = ticket.getEntryTime();
        LocalDateTime exitTime = LocalDateTime.now();
        
        // Tính thời gian đỗ (giờ)
        long hours = ChronoUnit.HOURS.between(entryTime, exitTime);
        double duration = hours;
        
        // Tính phí đỗ xe (theo công thức mới)
        long parkingFee = billingService.calculateFee(vehicle, exitTime, entryTime);
        
        // Nếu là vé tháng và phải thu tiền (parkingFee > 0), cập nhật lịch sử đóng tiền
        if (vehicle.hasMonthlyCard() && parkingFee > 0) {
            String currentMonth = billingService.getCurrentMonth(exitTime);
            repository.updatePaymentStatus(plate, currentMonth); // Lưu vào sổ cái lịch sử (cũ)
            repository.saveMonthlyPayment(plate, currentMonth);  // Lưu vào file monthly_payment.csv
        }
        // Tổng phí = Phạt mất vé + Phí đỗ xe
        long totalFee = LOST_TICKET_FINE + parkingFee;
        
        // Lấy lastPaidMonth từ sổ cái để hiển thị
        String lastPaidMonth = repository.getLastPaidMonth(plate);
        
        // In hóa đơn mất vé
        printLostTicketInvoice(plate, entryTime, exitTime, duration, parkingFee, totalFee, 
                vehicle.hasMonthlyCard(), lastPaidMonth);
        
        // Xóa ticket khỏi repository
        repository.removeTicket(plate);
        
        return totalFee;
    }
    
    /**
     * In hóa đơn check-out bình thường
     */
    private void printInvoice(String plate, LocalDateTime entryTime, LocalDateTime exitTime, 
                             double duration, long fee, boolean hasMonthlyCard, String lastPaidMonth) {
        System.out.println("\n========================================");
        System.out.println("        HÓA ĐƠN THANH TOÁN");
        System.out.println("========================================");
        System.out.println("Biển số xe: " + plate);
        System.out.println("Giờ vào: " + entryTime);
        System.out.println("Giờ ra: " + exitTime);
        System.out.println("Thời gian đỗ: " + String.format("%.1f", duration) + " giờ");
        System.out.println("Thẻ tháng: " + (hasMonthlyCard ? "Có" : "Không"));
        if (hasMonthlyCard && lastPaidMonth != null) {
            System.out.println("Tháng đã đóng: " + lastPaidMonth);
        }
        System.out.println("----------------------------------------");
        System.out.println("TỔNG PHÍ: " + String.format("%,d", fee) + " VNĐ");
        System.out.println("========================================\n");
    }
    
    /**
     * In hóa đơn mất vé
     */
    private void printLostTicketInvoice(String plate, LocalDateTime entryTime, LocalDateTime exitTime,
                                       double duration, long parkingFee, long totalFee, boolean hasMonthlyCard, String lastPaidMonth) {
        System.out.println("\n========================================");
        System.out.println("     HÓA ĐƠN MẤT VÉ");
        System.out.println("========================================");
        System.out.println("Biển số xe: " + plate);
        System.out.println("Giờ vào: " + entryTime);
        System.out.println("Giờ ra: " + exitTime);
        System.out.println("Thời gian đỗ: " + String.format("%.1f", duration) + " giờ");
        System.out.println("Thẻ tháng: " + (hasMonthlyCard ? "Có" : "Không"));
        if (hasMonthlyCard && lastPaidMonth != null) {
            System.out.println("Tháng đã đóng: " + lastPaidMonth);
        }
        System.out.println("----------------------------------------");
        System.out.println("Phí đỗ xe: " + String.format("%,d", parkingFee) + " VNĐ");
        System.out.println("Phạt mất vé: " + String.format("%,d", LOST_TICKET_FINE) + " VNĐ");
        System.out.println("----------------------------------------");
        System.out.println("TỔNG PHÍ: " + String.format("%,d", totalFee) + " VNĐ");
        System.out.println("========================================\n");
    }
    
    /**
     * In hóa đơn check-out simulation
     */
    private void printSimulationInvoice(String plate, LocalDateTime entryTime, LocalDateTime exitTime,
                                       int months, int days, int totalDays, long fee, boolean hasMonthlyCard, String lastPaidMonth) {
        System.out.println("\n========================================");
        System.out.println("   HÓA ĐƠN THANH TOÁN (SIMULATION)");
        System.out.println("========================================");
        System.out.println("Biển số xe: " + plate);
        System.out.println("Giờ vào (giả lập): " + entryTime);
        System.out.println("Giờ ra: " + exitTime);
        System.out.println("Thời gian giả lập: " + months + " tháng " + days + " ngày (Tổng " + totalDays + " ngày)");
        System.out.println("Thẻ tháng: " + (hasMonthlyCard ? "Có" : "Không"));
        if (hasMonthlyCard && lastPaidMonth != null) {
            System.out.println("Tháng đã đóng: " + lastPaidMonth);
        }
        System.out.println("----------------------------------------");
        if (hasMonthlyCard) {
            if (fee > 0) {
                System.out.println("Phí vé tháng: " + String.format("%,d", fee) + " VNĐ (Chưa đóng tháng này)");
            } else {
                System.out.println("Phí vé tháng: 0 VNĐ (Đã đóng tháng này)");
            }
        } else {
            if (totalDays > 1) {
                System.out.println("Phí phạt: " + String.format("%,d", fee) + " VNĐ");
                System.out.println("   (Ngày đầu miễn phí, các ngày tiếp theo: " + String.format("%,d", (totalDays - 1)) + " ngày × 5.000 VNĐ)");
            } else {
                System.out.println("Phí phạt: 0 VNĐ (Ngày đầu tiên - miễn phí)");
            }
        }
        System.out.println("----------------------------------------");
        System.out.println("TỔNG PHÍ: " + String.format("%,d", fee) + " VNĐ");
        System.out.println("========================================\n");
    }
}

