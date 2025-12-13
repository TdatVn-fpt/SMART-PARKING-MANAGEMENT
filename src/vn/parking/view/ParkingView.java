package vn.parking.view;

import vn.parking.model.*;
import vn.parking.repository.ParkingRepository;
import java.util.*;

/**
 * View class xử lý toàn bộ giao diện Console và nhập liệu
 */
public class ParkingView {
    
    private Scanner scanner;
    private ParkingRepository repository;
    
    public ParkingView(Scanner scanner, ParkingRepository repository) {
        this.scanner = scanner;
        this.repository = repository;
    }
    
    /**
     * Hiển thị menu chính
     */
    public void showMenu() {
        System.out.println("\n================= MENU =================");
        System.out.println("1. Check-in (Gửi xe)");
        System.out.println("2. Check-out (Thực tế)");
        System.out.println("3. Check-out (Test giả lập thời gian - Simulation)");
        System.out.println("4. Báo mất vé (Lost Ticket)");
        System.out.println("5. Thống kê xe đang đỗ");
        System.out.println("6. Thoát");
        System.out.println("========================================");
    }
    
    /**
     * Chuẩn hóa input: Xóa khoảng trắng, viết hoa, loại bỏ khoảng trắng giữa
     * @param input Chuỗi cần chuẩn hóa
     * @return Chuỗi đã được chuẩn hóa
     */
    private String normalizeInput(String input) {
        if (input == null) {
            return "";
        }
        // Xóa khoảng trắng đầu đuôi, viết hoa, xóa hết khoảng trắng ở giữa
        return input.trim().toUpperCase().replaceAll("\\s+", "");
    }
    
    /**
     * Nhập và validate biển số xe theo loại xe
     */
    public String inputLicensePlate(VehicleType type) {
        // Xe đạp: Tự động sinh mã
        if (type == VehicleType.BICYCLE) {
            String autoPlate = generateBicyclePlate();
            System.out.println("✓ Hệ thống tự động tạo mã định danh: " + autoPlate);
            return autoPlate;
        }
        
        // Xe máy và Ô tô: Yêu cầu nhập
        while (true) {
            System.out.print("Nhập biển số xe: ");
            String rawInput = scanner.nextLine();
            String plate = normalizeInput(rawInput);
            
            if (plate.isEmpty()) {
                System.out.println("❌ Biển số không được để trống. Vui lòng nhập lại.");
                continue;
            }
            
            if (validateLicensePlate(plate, type)) {
                return plate;
            }
        }
    }
    
    /**
     * Validate biển số theo loại xe
     * Bắt buộc có dấu gạch ngang (-) để ngăn cách
     */
    private boolean validateLicensePlate(String plate, VehicleType type) {
        if (type == VehicleType.BIKE) {
            // Xe máy: [3-4 ký tự chữ số] - [4-5 chữ số]
            // Tổng độ dài: 9-10 ký tự (tính cả dấu gạch)
            // Regex: ^[A-Z0-9]{3,4}-[0-9]{4,5}$
            if (!plate.matches("^[A-Z0-9]{3,4}-[0-9]{4,5}$")) {
                System.out.println("❌ Biển số xe máy không đúng định dạng.");
                System.out.println("   Vui lòng nhập đúng định dạng có dấu gạch ngang.");
                System.out.println("   Ví dụ: 29S6-62360, 26S-62353, 29H1-1234");
                return false;
            }
            // Kiểm tra độ dài tổng
            if (plate.length() < 9 || plate.length() > 10) {
                System.out.println("❌ Biển số xe máy phải có 9-10 ký tự (tính cả dấu gạch).");
                System.out.println("   Ví dụ: 29S6-62360 (9 ký tự), 26S-62353 (9 ký tự)");
                return false;
            }
            return true;
            
        } else if (type == VehicleType.CAR) {
            // Ô tô: Bắt buộc có dấu gạch ngang
            // Format: 30A-123.45 hoặc 30A-12345
            // Regex: ^\d{2}[A-Z]{1,2}-\d{1,5}(\.\d{1,2})?$
            if (!plate.matches("^\\d{2}[A-Z]{1,2}-\\d{1,5}(\\.\\d{1,2})?$")) {
                System.out.println("❌ Biển số ô tô không đúng định dạng.");
                System.out.println("   Vui lòng nhập đúng định dạng có dấu gạch ngang.");
                System.out.println("   Ví dụ: 30A-123.45 hoặc 30A-12345");
                return false;
            }
            return true;
        }
        
        return false;
    }
    
    /**
     * Tự động sinh mã định danh cho xe đạp
     */
    private String generateBicyclePlate() {
        // Format: BIKE-XXXXX (với X là số ngẫu nhiên)
        Random random = new Random();
        int randomNum = random.nextInt(99999);
        return "BIKE-" + String.format("%05d", randomNum);
    }
    
    /**
     * Nhập loại xe
     */
    public VehicleType inputVehicleType() {
        while (true) {
            System.out.println("Chọn loại xe: (1) Xe máy, (2) Ô tô, (3) Xe đạp");
            System.out.print("Lựa chọn: ");
            String input = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(input);
                switch (choice) {
                    case 1:
                        return VehicleType.BIKE;
                    case 2:
                        return VehicleType.CAR;
                    case 3:
                        return VehicleType.BICYCLE;
                    default:
                        System.out.println("❌ Lựa chọn không hợp lệ, vui lòng chọn 1/2/3.");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Vui lòng nhập số hợp lệ.");
            }
        }
    }
    
    /**
     * Nhập loại nhiên liệu
     */
    public FuelType inputFuelType(VehicleType type) {
        // Xe đạp: luôn NONE
        if (type == VehicleType.BICYCLE) {
            return FuelType.NONE;
        }
        
        while (true) {
            System.out.println("Chọn nhiên liệu: (1) Xăng, (2) Điện");
            System.out.print("Lựa chọn: ");
            String input = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(input);
                switch (choice) {
                    case 1:
                        return FuelType.GASOLINE;
                    case 2:
                        return FuelType.ELECTRIC;
                    default:
                        System.out.println("❌ Lựa chọn không hợp lệ, vui lòng chọn 1/2.");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Vui lòng nhập số hợp lệ.");
            }
        }
    }
    
    /**
     * Nhập loại vé
     */
    public boolean inputMonthlyCard() {
        while (true) {
            System.out.println("Chọn vé: (1) Vé lượt, (2) Vé tháng");
            System.out.print("Lựa chọn: ");
            String input = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(input);
                switch (choice) {
                    case 1:
                        return false;
                    case 2:
                        return true;
                    default:
                        System.out.println("❌ Lựa chọn không hợp lệ, vui lòng chọn 1/2.");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Vui lòng nhập số hợp lệ.");
            }
        }
    }
    
    /**
     * Nhập biển số với smart search (gợi ý khi nhập thiếu)
     */
    public String inputLicensePlateWithSearch() {
        while (true) {
            System.out.print("Nhập biển số xe (hoặc một phần): ");
            String rawInput = scanner.nextLine();
            String input = normalizeInput(rawInput);
            
            if (input.isEmpty()) {
                System.out.println("❌ Vui lòng nhập biển số.");
                continue;
            }
            
            // Tìm chính xác (đã chuẩn hóa)
            String foundPlate = repository.findTicketByPlateNormalized(input);
            if (foundPlate != null) {
                return foundPlate;
            }
            
            // Tìm kiếm gợi ý (chứa chuỗi)
            List<String> suggestions = findSuggestions(input);
            
            if (suggestions.isEmpty()) {
                System.out.println("❌ Không tìm thấy xe nào khớp với: " + input);
                System.out.print("   Bạn có muốn thử lại? (y/n): ");
                String retry = scanner.nextLine().trim().toLowerCase();
                if (!retry.equals("y") && !retry.equals("yes")) {
                    return null; // Hủy
                }
                continue;
            }
            
            // Hiển thị danh sách gợi ý
            if (suggestions.size() == 1) {
                System.out.println("✓ Tìm thấy: " + suggestions.get(0));
                return suggestions.get(0);
            }
            
            // Nhiều gợi ý: cho người dùng chọn
            System.out.println("\n📋 Có phải ý bạn là:");
            for (int i = 0; i < suggestions.size(); i++) {
                System.out.println("   " + (i + 1) + ". " + suggestions.get(i));
            }
            
            while (true) {
                System.out.print("Chọn số thứ tự (1-" + suggestions.size() + ") hoặc 0 để nhập lại: ");
                String choiceStr = scanner.nextLine().trim();
                try {
                    int choice = Integer.parseInt(choiceStr);
                    if (choice == 0) {
                        break; // Quay lại nhập
                    }
                    if (choice >= 1 && choice <= suggestions.size()) {
                        return suggestions.get(choice - 1);
                    }
                    System.out.println("❌ Lựa chọn không hợp lệ.");
                } catch (NumberFormatException e) {
                    System.out.println("❌ Vui lòng nhập số hợp lệ.");
                }
            }
        }
    }
    
    /**
     * Tìm danh sách biển số gợi ý
     */
    private List<String> findSuggestions(String partialPlate) {
        // Input đã được chuẩn hóa, chỉ cần tìm kiếm
        return repository.searchByKeyword(partialPlate);
    }
    
    /**
     * Nhập tháng và ngày cho simulation
     * @return Mảng [months, days]
     */
    public int[] inputSimulationMonthsAndDays() {
        int months = 0;
        int days = 0;
        
        // Nhập số tháng
        while (true) {
            System.out.print("Nhập số THÁNG đã trôi qua: ");
            String input = scanner.nextLine().trim();
            try {
                months = Integer.parseInt(input);
                if (months < 0) {
                    System.out.println("❌ Số tháng phải >= 0.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("❌ Vui lòng nhập số hợp lệ.");
            }
        }
        
        // Nhập số ngày
        while (true) {
            System.out.print("Nhập số NGÀY lẻ đã trôi qua: ");
            String input = scanner.nextLine().trim();
            try {
                days = Integer.parseInt(input);
                if (days < 0) {
                    System.out.println("❌ Số ngày phải >= 0.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("❌ Vui lòng nhập số hợp lệ.");
            }
        }
        
        return new int[]{months, days};
    }
    
    /**
     * Hiển thị thông báo check-in thành công
     */
    public void showCheckInSuccess(String plate, String zone) {
        System.out.println("\n✓ Xe " + plate + " vào bãi -> Vào Zone " + zone);
        System.out.println("✓ Xe " + plate + " hãy vào Zone " + zone + ".\n");
    }
    
    /**
     * Hiển thị thông báo check-out
     */
    public void showCheckOutFee(long fee) {
        System.out.println("\nTỔNG PHÍ: " + String.format("%,d", fee) + " VNĐ\n");
    }
    
    /**
     * Hiển thị thống kê
     */
    public void showStatistics(Collection<Ticket> tickets) {
        System.out.println("\n--- THỐNG KÊ XE ĐANG ĐỖ ---");
        if (tickets.isEmpty()) {
            System.out.println("Không có xe nào trong bãi.\n");
            return;
        }
        
        System.out.println("Tổng số xe: " + tickets.size());
        for (Ticket ticket : tickets) {
            System.out.println("- Biển số: " + ticket.getPlate()
                    + " | Zone: " + ticket.getZone()
                    + " | Giờ vào: " + ticket.getEntryTime());
        }
        System.out.println();
    }
    
    /**
     * Hiển thị lỗi
     */
    public void showError(String message) {
        System.out.println("❌ Lỗi: " + message + "\n");
    }
    
    /**
     * Hiển thị thông báo thoát
     */
    public void showGoodbye() {
        System.out.println("Tạm biệt!");
    }
    
    /**
     * Nhập lựa chọn menu
     * Lưu ý: Sử dụng nextLine() để tránh trôi lệnh
     */
    public int inputMenuChoice() {
        System.out.print("Chọn chức năng: ");
        String input = scanner.nextLine().trim();
        try {
            int choice = Integer.parseInt(input);
            // Clear buffer sau khi parse (phòng trường hợp có dữ liệu thừa)
            return choice;
        } catch (NumberFormatException e) {
            return -1; // Invalid
        }
    }
}

