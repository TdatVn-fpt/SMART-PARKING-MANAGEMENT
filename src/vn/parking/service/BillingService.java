package vn.parking.service;

import vn.parking.model.Vehicle;
import vn.parking.repository.ParkingRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service xử lý logic tính phí đỗ xe
 */
public class BillingService {
    
    private ParkingRepository repository;
    
    private static final int FREE_HOURS = 24;           // 24 giờ đầu miễn phí
    private static final int FEE_PER_DAY = 5000;        // Phí mỗi ngày: 5.000 VNĐ
    private static final long MONTHLY_TICKET_FEE = 50000; // Phí vé tháng: 50.000 VNĐ
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MM/yyyy");
    
    public BillingService(ParkingRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Tính phí đỗ xe dựa trên Vehicle và thời gian check-out
     * 
     * @param vehicle Xe cần tính phí
     * @param checkoutTime Thời gian check-out (để xác định tháng cho vé tháng)
     * @param entryTime Thời gian vào bãi
     * @return Tổng phí phải trả (VNĐ)
     */
    public long calculateFee(Vehicle vehicle, LocalDateTime checkoutTime, LocalDateTime entryTime) {
        // Xe đạp: luôn miễn phí
        if (vehicle.getType() == vn.parking.model.VehicleType.BICYCLE) {
            return 0;
        }

        if (vehicle.hasMonthlyCard()) {
            // Logic VÉ THÁNG: Chỉ thu 1 lần/tháng (thực tế)
            return calculateMonthlyTicketFee(vehicle.getPlate(), checkoutTime);
        }

        // Logic VÉ LƯỢT
        return calculateGuestTicketFee(entryTime, checkoutTime);
    }
    
    /**
     * Tính phí vé tháng (One-Time Payment per Month)
     * - Lấy tháng check-out (Format "MM/yyyy")
     * - Lấy lastPaidMonth từ repository (Sổ cái lịch sử)
     * - So sánh: Nếu trùng -> 0 VNĐ (đã đóng), Nếu khác -> 50.000 VNĐ (chưa đóng)
     */
    private long calculateMonthlyTicketFee(String plate, LocalDateTime checkoutTime) {
        String currentMonth = checkoutTime.format(MONTH_FORMATTER);
        String recordedMonth = repository.getLastPaidMonth(plate); // Lấy từ sổ cái
        
        // Trường hợp 1: Trùng khớp -> Đã đóng tiền tháng này
        if (currentMonth.equals(recordedMonth)) {
            return 0;
        }
        
        // Trường hợp 2: Khác nhau hoặc null -> Chưa đóng
        return MONTHLY_TICKET_FEE;
    }
    
    /**
     * Tính phí vé lượt (Guest Ticket)
     * - 24h đầu tiên: 0 VNĐ
     * - Từ ngày thứ 2: Mỗi ngày 5.000 VNĐ
     * - Công thức: TotalDays = Math.ceil(duration_hours / 24.0)
     *   Nếu TotalDays <= 1: Phí = 0
     *   Ngược lại: (TotalDays - 1) * 5.000
     */
    private long calculateGuestTicketFee(LocalDateTime entryTime, LocalDateTime checkoutTime) {
        long hours = java.time.temporal.ChronoUnit.HOURS.between(entryTime, checkoutTime);
        
        // 24h đầu tiên: Miễn phí
        if (hours <= FREE_HOURS) {
            return 0;
        }
        
        // Tính số ngày (làm tròn lên)
        double totalDays = Math.ceil(hours / 24.0);
        
        // Ngày đầu tiên miễn phí, từ ngày thứ 2 trở đi tính phí
        if (totalDays <= 1) {
            return 0;
        }
        
        return (long)((totalDays - 1) * FEE_PER_DAY);
    }

    /**
     * Tính phí cho Simulation, hỗ trợ logic vé tháng cộng dồn theo tháng + ngày lẻ
     *
     * @param vehicle      Xe cần tính phí
     * @param monthsPassed Số tháng đã trôi qua (giả lập)
     * @param extraDays    Số ngày lẻ đã trôi qua (giả lập)
     * @param checkoutTime Thời điểm check-out giả lập (dùng để xác định tháng)
     * @param entryTime    Thời điểm check-in giả lập
     */
    public long calculateSimulationFee(Vehicle vehicle, int monthsPassed, int extraDays,
                                       LocalDateTime checkoutTime, LocalDateTime entryTime) {
        // Xe đạp: luôn miễn phí
        if (vehicle.getType() == vn.parking.model.VehicleType.BICYCLE) {
            return 0;
        }

        if (vehicle.hasMonthlyCard()) {
            // Phí tháng: kiểm tra đã đóng tháng hiện tại (theo checkoutTime) hay chưa
            long monthlyFee = calculateMonthlyTicketFee(vehicle.getPlate(), checkoutTime);
            long dailyFee = (long) extraDays * FEE_PER_DAY;
            return monthlyFee + dailyFee;
        }

        // Vé lượt: giữ nguyên cách tính theo thời gian
        return calculateGuestTicketFee(entryTime, checkoutTime);
    }
    
    /**
     * Lấy tháng hiện tại theo format "MM/yyyy"
     */
    public String getCurrentMonth(LocalDateTime dateTime) {
        return dateTime.format(MONTH_FORMATTER);
    }
}

