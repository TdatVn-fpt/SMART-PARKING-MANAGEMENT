package vn.parking.repository;

import vn.parking.model.*;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Repository quản lý dữ liệu của hệ thống
 */
public class ParkingRepository implements IRepository<Vehicle> {
    // Map lưu trữ ticket theo biển số xe
    private Map<String, Ticket> ticketsByPlate;
    
    // Map lưu trữ vehicle theo biển số xe
    private Map<String, Vehicle> vehicles;
    
    // Map lưu trữ lịch sử đóng tiền vé tháng (Key: Biển số, Value: Tháng đã đóng gần nhất)
    private Map<String, String> monthlyPaymentHistory;
    
    private static final String DEFAULT_FILENAME = "parking_data.csv";
    private static final String MONTHLY_HISTORY_FILENAME = "monthly_history.csv";
    private static final String MONTHLY_PAYMENT_FILENAME = "monthly_payment.csv";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public ParkingRepository() {
        this.ticketsByPlate = new HashMap<>();
        this.vehicles = new HashMap<>();
        this.monthlyPaymentHistory = new HashMap<>();
    }
    
    /**
     * Triển khai IRepository<Vehicle> - trả về danh sách tất cả Vehicle
     */
    @Override
    public List<Vehicle> getAll() {
        return new ArrayList<>(vehicles.values());
    }

    /**
     * Triển khai IRepository<Vehicle> - thêm một Vehicle mới
     */
    @Override
    public void add(Vehicle item) {
        if (item != null) {
            saveVehicle(item);
        }
    }

    /**
     * Triển khai IRepository<Vehicle> - xóa một Vehicle
     */
    @Override
    public void remove(Vehicle item) {
        if (item != null) {
            vehicles.remove(item.getPlate());
        }
    }

    /**
     * Lưu ticket vào repository
     */
    public void saveTicket(Ticket ticket) {
        ticketsByPlate.put(ticket.getPlate(), ticket);
    }
    
    /**
     * Chuẩn hóa biển số: Xóa khoảng trắng, viết hoa, loại bỏ khoảng trắng giữa
     */
    private String normalizePlate(String plate) {
        if (plate == null) {
            return "";
        }
        return plate.trim().toUpperCase().replaceAll("\\s+", "");
    }
    
    /**
     * Tìm ticket theo biển số xe (không chuẩn hóa - dùng cho nội bộ)
     */
    public Ticket findTicketByPlate(String plate) {
        if (plate == null) {
            return null;
        }
        return ticketsByPlate.get(plate);
    }
    
    /**
     * Tìm ticket theo biển số xe (có chuẩn hóa - dùng cho tìm kiếm từ người dùng)
     * @param inputPlate Biển số từ input người dùng (có thể có khoảng trắng, viết thường)
     * @return Biển số chính xác nếu tìm thấy, null nếu không
     */
    public String findTicketByPlateNormalized(String inputPlate) {
        if (inputPlate == null || inputPlate.isEmpty()) {
            return null;
        }
        
        String normalizedInput = normalizePlate(inputPlate);
        
        // Tìm kiếm trong tất cả tickets
        for (String storedPlate : ticketsByPlate.keySet()) {
            String normalizedStored = normalizePlate(storedPlate);
            if (normalizedStored.equals(normalizedInput)) {
                return storedPlate; // Trả về biển số gốc (đã lưu trong hệ thống)
            }
        }
        
        return null;
    }
    
    /**
     * Kiểm tra xe có đang đỗ không (có chuẩn hóa)
     */
    public boolean isVehicleParked(String plate) {
        if (plate == null) {
            return false;
        }
        String normalizedInput = normalizePlate(plate);
        
        // Kiểm tra trong tất cả tickets
        for (String storedPlate : ticketsByPlate.keySet()) {
            String normalizedStored = normalizePlate(storedPlate);
            if (normalizedStored.equals(normalizedInput)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Xóa ticket khi xe ra
     */
    public void removeTicket(String plate) {
        ticketsByPlate.remove(plate);
    }
    
    /**
     * Lưu vehicle vào repository
     */
    public void saveVehicle(Vehicle vehicle) {
        vehicles.put(vehicle.getPlate(), vehicle);
    }
    
    /**
     * Tìm vehicle theo biển số xe (không chuẩn hóa - dùng cho nội bộ)
     */
    public Vehicle findVehicleByPlate(String plate) {
        if (plate == null) {
            return null;
        }
        return vehicles.get(plate);
    }
    
    /**
     * Tìm vehicle theo biển số xe (có chuẩn hóa)
     */
    public Vehicle findVehicleByPlateNormalized(String inputPlate) {
        if (inputPlate == null || inputPlate.isEmpty()) {
            return null;
        }
        
        String normalizedInput = normalizePlate(inputPlate);
        
        // Tìm kiếm trong tất cả vehicles
        for (String storedPlate : vehicles.keySet()) {
            String normalizedStored = normalizePlate(storedPlate);
            if (normalizedStored.equals(normalizedInput)) {
                return vehicles.get(storedPlate);
            }
        }
        
        return null;
    }
    
    /**
     * Tìm kiếm gợi ý theo từ khóa (tương đối)
     * @param keyword Từ khóa tìm kiếm (đã được chuẩn hóa)
     * @return Danh sách biển số gợi ý
     */
    public List<String> searchByKeyword(String keyword) {
        List<String> suggestions = new ArrayList<>();
        
        if (keyword == null || keyword.isEmpty()) {
            return suggestions;
        }
        
        String normalizedKeyword = normalizePlate(keyword);
        
        // Duyệt tất cả tickets
        for (Ticket ticket : ticketsByPlate.values()) {
            String plate = ticket.getPlate();
            String normalizedPlate = normalizePlate(plate);
            
            // Tìm kiếm: biển số chứa từ khóa
            if (normalizedPlate.contains(normalizedKeyword)) {
                suggestions.add(plate); // Trả về biển số gốc
            }
        }
        
        return suggestions;
    }
    
    /**
     * Lấy tất cả tickets đang active
     */
    public Collection<Ticket> getAllActiveTickets() {
        return new ArrayList<>(ticketsByPlate.values());
    }
    
    /**
     * Lấy tất cả vehicles
     */
    public Collection<Vehicle> getAllVehicles() {
        return new ArrayList<>(vehicles.values());
    }
    
    /**
     * Lưu toàn bộ dữ liệu ra file CSV
     * Format: type,licensePlate,entryTime,fuelType,ticketType,lastPaidMonth
     */
    public void saveToFile(String filename) {
        if (filename == null || filename.isEmpty()) {
            filename = DEFAULT_FILENAME;
        }
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename, false))) {
            // Ghi header
            writer.println("type,licensePlate,entryTime,fuelType,ticketType,lastPaidMonth");
            
            // Ghi dữ liệu
            for (Ticket ticket : ticketsByPlate.values()) {
                Vehicle vehicle = vehicles.get(ticket.getPlate());
                if (vehicle != null) {
                    writer.printf("%s,%s,%s,%s,%s,%s%n",
                        vehicle.getType().name(),
                        ticket.getPlate(),
                        ticket.getEntryTime().format(DATE_FORMATTER),
                        vehicle.getFuelType().name(),
                        ticket.isMonthlyTicket() ? "MONTHLY" : "SINGLE",
                        vehicle.getLastPaidMonth() != null ? vehicle.getLastPaidMonth() : ""
                    );
                }
            }
            
            // Lưu cả vehicles không có ticket (đã check-out nhưng còn thông tin)
            for (Vehicle vehicle : vehicles.values()) {
                if (!ticketsByPlate.containsKey(vehicle.getPlate())) {
                    // Vehicle đã check-out, chỉ lưu thông tin vehicle
                    writer.printf("%s,%s,,%s,%s,%s%n",
                        vehicle.getType().name(),
                        vehicle.getPlate(),
                        vehicle.getFuelType().name(),
                        vehicle.hasMonthlyCard() ? "MONTHLY" : "SINGLE",
                        vehicle.getLastPaidMonth() != null ? vehicle.getLastPaidMonth() : ""
                    );
                }
            }
            
            System.out.println("✓ Đã lưu dữ liệu vào file: " + filename);
        } catch (IOException e) {
            System.err.println("❌ Lỗi khi lưu file: " + e.getMessage());
        }
    }
    
    /**
     * Đọc dữ liệu từ file CSV
     * Format: type,licensePlate,entryTime,fuelType,ticketType,lastPaidMonth
     * Hỗ trợ file cũ (không có cột lastPaidMonth) để tránh lỗi
     */
    public void loadFromFile(String filename) {
        if (filename == null || filename.isEmpty()) {
            filename = DEFAULT_FILENAME;
        }
        
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("ℹ File không tồn tại: " + filename + " (Sẽ tạo mới khi lưu)");
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String headerLine = reader.readLine(); // Đọc header để kiểm tra số cột
            if (headerLine == null) {
                return;
            }
            
            String[] headerParts = headerLine.split(",");
            boolean hasLastPaidMonth = headerParts.length >= 6;
            
            int count = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                try {
                    String[] parts = line.split(",");
                    if (parts.length < 5) continue;
                    
                    // Parse dữ liệu cơ bản
                    VehicleType type = VehicleType.valueOf(parts[0].trim());
                    String plate = parts[1].trim();
                    FuelType fuelType = FuelType.valueOf(parts[3].trim());
                    boolean isMonthly = parts[4].trim().equals("MONTHLY");
                    
                    // Parse lastPaidMonth (có thể không có trong file cũ)
                    String lastPaidMonth = null;
                    if (hasLastPaidMonth && parts.length >= 6) {
                        String lastPaidMonthStr = parts[5].trim();
                        if (!lastPaidMonthStr.isEmpty()) {
                            lastPaidMonth = lastPaidMonthStr;
                        }
                    }
                    
                    // Tạo Vehicle cụ thể dựa trên VehicleType
                    Vehicle vehicle;
                    switch (type) {
                        case CAR:
                            vehicle = new Car(plate, fuelType, isMonthly, lastPaidMonth);
                            break;
                        case BIKE:
                            vehicle = new Motorbike(plate, fuelType, isMonthly, lastPaidMonth);
                            break;
                        case BICYCLE:
                        default:
                            vehicle = new Bicycle(plate, fuelType, isMonthly, lastPaidMonth);
                            break;
                    }
                    
                    // Parse entryTime (có thể rỗng nếu vehicle đã check-out)
                    String entryTimeStr = parts[2].trim();
                    if (!entryTimeStr.isEmpty()) {
                        LocalDateTime entryTime = LocalDateTime.parse(entryTimeStr, DATE_FORMATTER);
                        String zone = determineZoneFromVehicle(vehicle);
                        Ticket ticket = new Ticket(plate, entryTime, isMonthly, zone);
                        ticketsByPlate.put(plate, ticket);
                    }
                    
                    // Lưu vehicle vào repository
                    vehicles.put(plate, vehicle);
                    count++;
                    
                } catch (Exception e) {
                    System.err.println("❌ Lỗi khi đọc dòng: " + line + " - " + e.getMessage());
                }
            }
            
            System.out.println("✓ Đã tải " + count + " xe từ file: " + filename);
        } catch (IOException e) {
            System.err.println("❌ Lỗi khi đọc file: " + e.getMessage());
        }
    }
    
    /**
     * Xác định zone từ vehicle (giống logic trong ParkingService)
     */
    private String determineZoneFromVehicle(Vehicle vehicle) {
        VehicleType type = vehicle.getType();
        FuelType fuelType = vehicle.getFuelType();
        
        if (type == VehicleType.CAR) {
            return "B";
        }
        
        if (fuelType == FuelType.ELECTRIC || type == VehicleType.BICYCLE) {
            return "A1";
        } else if (fuelType == FuelType.GASOLINE) {
            return "A2";
        }
        
        return "A1";
    }
    
    /**
     * Lưu dữ liệu với tên file mặc định
     */
    public void saveToFile() {
        saveToFile(DEFAULT_FILENAME);
    }
    
    /**
     * Đọc dữ liệu với tên file mặc định
     */
    public void loadFromFile() {
        loadFromFile(DEFAULT_FILENAME);
        loadMonthlyHistory(); // Load lịch sử đóng tiền khi khởi động
    }
    
    /**
     * Đọc lịch sử đóng tiền vé tháng từ file monthly_history.csv
     * Format: LicensePlate,LastPaidMonth
     */
    public void loadMonthlyHistory() {
        File file = new File(MONTHLY_HISTORY_FILENAME);
        if (!file.exists()) {
            System.out.println("ℹ File lịch sử đóng tiền không tồn tại: " + MONTHLY_HISTORY_FILENAME + " (Sẽ tạo mới khi có dữ liệu)");
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(MONTHLY_HISTORY_FILENAME))) {
            String headerLine = reader.readLine(); // Bỏ qua header
            if (headerLine == null) {
                return;
            }
            
            int count = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                try {
                    String[] parts = line.split(",");
                    if (parts.length < 2) continue;
                    
                    String plate = parts[0].trim();
                    String lastPaidMonth = parts[1].trim();
                    
                    if (!plate.isEmpty() && !lastPaidMonth.isEmpty()) {
                        monthlyPaymentHistory.put(plate, lastPaidMonth);
                        count++;
                    }
                } catch (Exception e) {
                    System.err.println("❌ Lỗi khi đọc dòng lịch sử: " + line + " - " + e.getMessage());
                }
            }
            
            System.out.println("✓ Đã tải " + count + " bản ghi lịch sử đóng tiền từ file: " + MONTHLY_HISTORY_FILENAME);
        } catch (IOException e) {
            System.err.println("❌ Lỗi khi đọc file lịch sử: " + e.getMessage());
        }
    }
    
    /**
     * Lưu lịch sử đóng tiền vé tháng ra file monthly_history.csv
     * Format: LicensePlate,LastPaidMonth
     */
    public void saveMonthlyHistory() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(MONTHLY_HISTORY_FILENAME, false))) {
            // Ghi header
            writer.println("LicensePlate,LastPaidMonth");
            
            // Ghi dữ liệu
            for (Map.Entry<String, String> entry : monthlyPaymentHistory.entrySet()) {
                writer.printf("%s,%s%n", entry.getKey(), entry.getValue());
            }
            
            System.out.println("✓ Đã lưu lịch sử đóng tiền vào file: " + MONTHLY_HISTORY_FILENAME);
        } catch (IOException e) {
            System.err.println("❌ Lỗi khi lưu file lịch sử: " + e.getMessage());
        }
    }
    
    /**
     * Cập nhật trạng thái đóng tiền vé tháng cho một biển số
     * @param plate Biển số xe
     * @param month Tháng đã đóng (Format: "MM/yyyy")
     */
    public void updatePaymentStatus(String plate, String month) {
        if (plate == null || month == null || plate.isEmpty() || month.isEmpty()) {
            return;
        }
        
        monthlyPaymentHistory.put(plate, month);
        saveMonthlyHistory(); // Lưu ngay lập tức
    }
    
    /**
     * Lấy tháng đã đóng tiền gần nhất của một biển số
     * @param plate Biển số xe
     * @return Tháng đã đóng (Format: "MM/yyyy") hoặc null nếu chưa đóng
     */
    public String getLastPaidMonth(String plate) {
        if (plate == null || plate.isEmpty()) {
            return null;
        }
        
        // Tìm kiếm với chuẩn hóa để đảm bảo tìm đúng
        String normalizedPlate = normalizePlate(plate);
        
        for (Map.Entry<String, String> entry : monthlyPaymentHistory.entrySet()) {
            String storedPlate = entry.getKey();
            String normalizedStored = normalizePlate(storedPlate);
            if (normalizedStored.equals(normalizedPlate)) {
                return entry.getValue();
            }
        }
        
        return null;
    }

    /**
     * Kiểm tra một biển số đã đóng vé tháng cho một tháng cụ thể hay chưa
     * Đọc từ file monthly_payment.csv với format: LicensePlate,MonthYear
     */
    public boolean hasPaidMonthly(String plate, String monthYear) {
        if (plate == null || monthYear == null || plate.isEmpty() || monthYear.isEmpty()) {
            return false;
        }

        File file = new File(MONTHLY_PAYMENT_FILENAME);
        if (!file.exists()) {
            return false;
        }

        String normalizedPlate = normalizePlate(plate);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // có thể là header
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length < 2) continue;

                String storedPlate = parts[0].trim();
                String storedMonth = parts[1].trim();

                if (normalizePlate(storedPlate).equals(normalizedPlate)
                        && storedMonth.equals(monthYear)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Lỗi khi đọc file monthly_payment: " + e.getMessage());
        }

        return false;
    }

    /**
     * Lưu bản ghi đã đóng vé tháng vào file monthly_payment.csv
     * Format: LicensePlate,MonthYear
     */
    public void saveMonthlyPayment(String plate, String monthYear) {
        if (plate == null || monthYear == null || plate.isEmpty() || monthYear.isEmpty()) {
            return;
        }

        File file = new File(MONTHLY_PAYMENT_FILENAME);
        boolean writeHeader = !file.exists() || file.length() == 0;

        try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
            if (writeHeader) {
                writer.println("LicensePlate,MonthYear");
            }
            writer.printf("%s,%s%n", plate, monthYear);
        } catch (IOException e) {
            System.err.println("❌ Lỗi khi ghi file monthly_payment: " + e.getMessage());
        }
    }
}

