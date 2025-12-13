# 🚗 Smart Parking Management System

Hệ thống quản lý bãi đỗ xe thông minh - Ứng dụng Console cho Bảo vệ

## 📋 Mục lục

- [Giới thiệu](#giới-thiệu)
- [Chức năng chính](#chức-năng-chính)
- [Cấu trúc dự án](#cấu-trúc-dự-an)
- [Hướng dẫn sử dụng](#hướng-dẫn-sử-dụng)
- [Quy tắc tính phí](#quy-tắc-tính-phí)
- [Quy tắc phân zone](#quy-tắc-phân-zone)

## 🎯 Giới thiệu

Smart Parking Management System là hệ thống quản lý bãi đỗ xe được xây dựng bằng Java, sử dụng kiến trúc MVC (Model-View-Controller). Hệ thống hỗ trợ quản lý các loại xe (Xe máy, Ô tô, Xe đạp) với các tính năng:

- ✅ Check-in/Check-out tự động
- ✅ Tính phí linh hoạt (24h đầu miễn phí)
- ✅ Phân zone thông minh
- ✅ Tìm kiếm biển số gợi ý
- ✅ Giả lập thời gian để test
- ✅ Xử lý mất vé
- ✅ Lưu trữ dữ liệu CSV

## 🚀 Chức năng chính

### 1. Check-in (Gửi xe)
- Nhập thông tin xe: Biển số, Loại xe, Nhiên liệu, Loại vé
- Tự động phân zone theo quy tắc
- Validation biển số nghiêm ngặt

### 2. Check-out (Thực tế)
- Tính phí dựa trên thời gian thực
- Tìm kiếm thông minh với gợi ý khi nhập thiếu biển số

### 3. Check-out (Simulation)
- Giả lập thời gian bằng cách nhập tháng và ngày
- Tính phí theo công thức: (TotalDays - 1) × 5.000 VNĐ
- Hữu ích cho việc test tính năng phạt tiền

### 4. Báo mất vé
- Phạt 50.000 VNĐ + Phí đỗ xe
- Tự động tìm xe trong hệ thống

### 5. Thống kê
- Xem danh sách tất cả xe đang đỗ
- Hiển thị thông tin: Biển số, Zone, Giờ vào

## 📁 Cấu trúc dự án

```
src/vn/parking/
├── controller/          # Điều khiển luồng xử lý
│   └── ParkingController.java
├── view/                # Giao diện Console và nhập liệu
│   └── ParkingView.java
├── service/             # Logic nghiệp vụ
│   ├── ParkingService.java
│   └── BillingService.java
├── repository/          # Quản lý dữ liệu và File IO
│   └── ParkingRepository.java
├── model/               # Entity classes
│   ├── Vehicle.java
│   ├── Ticket.java
│   ├── ParkingSlot.java
│   ├── VehicleType.java
│   └── FuelType.java
└── main/                # Entry point
    └── Main.java
```

### Mô tả các package:

- **controller**: Điều khiển luồng xử lý, kết nối View và Service
- **view**: Xử lý giao diện console, validation, nhập liệu từ người dùng
- **service**: Chứa logic nghiệp vụ (check-in, check-out, tính phí)
- **repository**: Quản lý dữ liệu trong memory và lưu/đọc file CSV
- **model**: Các entity classes đại diện cho dữ liệu

## 📖 Hướng dẫn sử dụng

### Cách nhập biển số chuẩn

#### Xe máy (MOTORBIKE)
- **Định dạng**: `[3-4 ký tự chữ số] - [4-5 chữ số]`
- **Tổng độ dài**: 9-10 ký tự (tính cả dấu gạch)
- **Ví dụ hợp lệ**:
  - `29S6-62360` (9 ký tự)
  - `26S-62353` (9 ký tự)
  - `29H1-1234` (9 ký tự)
- **Ví dụ sai**: `29S662360` (thiếu gạch), `29-123` (quá ngắn)

#### Ô tô (CAR)
- **Định dạng**: Bắt buộc có dấu gạch ngang `-`
- **Ví dụ hợp lệ**:
  - `30A-123.45`
  - `30A-12345`
  - `29B-1234`
- **Ví dụ sai**: `30A12345` (thiếu gạch)

#### Xe đạp (BICYCLE)
- **Tự động**: Hệ thống tự động tạo mã định danh
- **Format**: `BIKE-XXXXX` (với X là số ngẫu nhiên)
- **Không cần nhập**: Chỉ cần chọn loại xe là Xe đạp

### Cách test giả lập thời gian

1. Chọn menu **3. Check-out (Test giả lập thời gian - Simulation)**
2. Nhập biển số xe (hoặc một phần để tìm kiếm)
3. Nhập số **THÁNG** đã trôi qua (ví dụ: 0)
4. Nhập số **NGÀY** lẻ đã trôi qua (ví dụ: 3)
5. Hệ thống sẽ tính:
   - **TotalDays** = (months × 30) + days
   - **Phí** = (TotalDays - 1) × 5.000 VNĐ
   - Ngày đầu tiên: **Miễn phí**

#### Ví dụ:
- Nhập: 0 tháng, 1 ngày → **0 VNĐ** (ngày đầu miễn phí)
- Nhập: 0 tháng, 3 ngày → **(3-1) × 5.000 = 10.000 VNĐ**
- Nhập: 1 tháng, 5 ngày → **(35-1) × 5.000 = 170.000 VNĐ**

### Tìm kiếm thông minh

Khi check-out, nếu nhập thiếu biển số:
- Hệ thống sẽ quét và hiển thị danh sách gợi ý
- Chọn số thứ tự của xe đúng để tiếp tục

**Ví dụ**:
- Nhập: `29S6` → Hệ thống tìm thấy `29S6-62360`, `29S6-78901`
- Chọn số thứ tự để check-out

## 💰 Quy tắc tính phí

### Check-out thực tế
- **24 giờ đầu**: **0 VNĐ** (Miễn phí)
- **Quá 24 giờ**: **5.000 VNĐ** cho mỗi block 24h tiếp theo
- **Công thức**: `Fee = (duration <= 24) ? 0 : Math.ceil((duration - 24) / 24.0) * 5000`

### Check-out Simulation
- **Ngày đầu tiên**: **0 VNĐ** (Miễn phí)
- **Các ngày tiếp theo**: **(TotalDays - 1) × 5.000 VNĐ**
- **Công thức**: `Fee = (TotalDays - 1) × 5000`

### Thẻ tháng
- **Giá mua**: 50.000 VNĐ/tháng
- **Ưu tiên**: Nếu có thẻ tháng còn hạn → **Phí luôn là 0 VNĐ** (kể cả quá giờ)

### Mất vé
- **Phạt mất vé**: **50.000 VNĐ** (cố định)
- **Phí đỗ xe**: Tính theo công thức bình thường
- **Tổng phí**: Phạt mất vé + Phí đỗ xe

## 🗺️ Quy tắc phân zone

Hệ thống tự động phân zone khi check-in:

| Loại xe | Nhiên liệu | Zone | Cổng |
|---------|-----------|------|------|
| **Ô tô** | Bất kỳ | **B** | Cổng B |
| **Xe máy** | Điện | **A1** | Cổng A |
| **Xe máy** | Xăng | **A2** | Cổng A |
| **Xe đạp** | NONE | **A1** | Cổng A |

### Quy tắc chi tiết:
- **Ô tô (CAR)**: Bắt buộc đi **Cổng B** → **Zone B**
- **Xe máy/Xe đạp**: Đi **Cổng A**
  - **Xe Điện** và **Xe đạp** → **Zone A1** (Vị trí ưu tiên)
  - **Xe Xăng** → **Zone A2** (Phía sau)

## 💾 Lưu trữ dữ liệu

- **File CSV**: `parking_data.csv` (tự động tạo trong thư mục gốc)
- **Format**: `type,licensePlate,entryTime,fuelType,ticketType`
- **Tự động lưu**: Sau mỗi thao tác quan trọng (check-in, check-out)
- **Tự động load**: Khi khởi động chương trình

## 🛠️ Công nghệ sử dụng

- **Java 8+**
- **Kiến trúc**: MVC (Model-View-Controller)
- **Lưu trữ**: CSV file
- **Giao diện**: Console Application

## 📝 Lưu ý

- Biển số xe máy và ô tô **bắt buộc** có dấu gạch ngang `-`
- Xe đạp không cần nhập biển số, hệ thống tự động tạo
- Dữ liệu được lưu tự động, không cần lo mất khi tắt app
- Sử dụng chức năng Simulation để test tính năng phạt tiền mà không cần chờ 24h

## 👨‍💻 Tác giả

Smart Parking Management System - PRO192.M.BL5

---

**Chúc bạn sử dụng hệ thống hiệu quả!** 🚀

