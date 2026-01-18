# AIMS_TKXDPM - Payment API với VietQR

## 📋 Mô tả dự án

Dự án này cung cấp API để xử lý thanh toán qua VietQR QR Code. Hệ thống cho phép tạo QR code thanh toán, theo dõi trạng thái thanh toán và xác nhận thanh toán thành công thông qua frontend.

## 🏗️ Cấu trúc dự án

```
src/main/java/com/example/AIMS_TKXDPM/
├── controller/          # REST Controllers
│   └── PaymentController.java
├── dto/                 # Data Transfer Objects
│   ├── PaymentRequest.java
│   ├── PaymentResponse.java
│   └── ConfirmPaymentRequest.java
├── entity/              # JPA Entities
│   ├── Order.java
│   └── Payment.java
├── repository/          # Data Access Layer
│   ├── OrderRepository.java
│   └── PaymentRepository.java
├── service/             # Business Logic Layer
│   ├── PaymentService.java
│   ├── PaymentCodeGenerator.java
│   └── qr/
│       ├── QrCodeGenerator.java (Interface)
│       └── VietQrCodeGenerator.java (Implementation)
└── exception/           # Exception Handling
    └── GlobalExceptionHandler.java
```

## 🚀 API Endpoints

Base URL: `http://localhost:8080/api/payments`

### 1. Tạo Payment và Generate QR Code

**Endpoint:** `POST /api/payments`

**Mô tả:** Tạo một payment mới và generate QR code theo format VietQR để khách hàng quét và thanh toán.

**Request Body:**
```json
{
  "orderId": 1,                    // Required: ID của đơn hàng cần thanh toán
  "amount": 100000,                 // Required: Số tiền thanh toán (phải > 0)
  "description": "Thanh toán đơn hàng #1"  // Optional: Nội dung chuyển tiền
}
```

**Request Fields:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `orderId` | Integer | ✅ Yes | ID của đơn hàng trong database |
| `amount` | BigDecimal | ✅ Yes | Số tiền thanh toán (phải là số dương) |
| `description` | String | ❌ No | Nội dung mô tả thanh toán (sẽ hiển thị trong QR code) |

**Response:** `201 Created`
```json
{
  "id": 1,
  "paymentCode": "PAY-20240101120000-ABC12345",
  "orderId": 1,
  "amount": 100000.00,
  "description": "Thanh toán đơn hàng #1",
  "status": "PENDING",
  "qrCodeUrl": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
  "qrCodeData": "{\"accountNo\":\"1234567890\",\"accountName\":\"AIMS STORE\",\"acqId\":\"970415\",\"amount\":100000,\"addInfo\":\"Thanh toán đơn hàng #1\",\"template\":\"compact\"}",
  "createdAt": "2024-01-01T12:00:00",
  "expiresAt": "2024-01-02T12:00:00",
  "paymentMethod": "VIETQR"
}
```

**Response Fields:**
| Field | Type | Description |
|-------|------|-------------|
| `id` | Integer | ID của payment trong database |
| `paymentCode` | String | Mã thanh toán duy nhất (format: PAY-YYYYMMDDHHmmss-UUID) |
| `orderId` | Integer | ID của đơn hàng liên kết |
| `amount` | BigDecimal | Số tiền thanh toán |
| `description` | String | Nội dung mô tả |
| `status` | String | Trạng thái: `PENDING`, `COMPLETED`, `EXPIRED`, `CANCELLED` |
| `qrCodeUrl` | String | URL base64 của QR code image (có thể dùng trực tiếp trong thẻ `<img>`) |
| `qrCodeData` | String | Raw QR code data theo format VietQR (JSON string) |
| `createdAt` | LocalDateTime | Thời gian tạo payment |
| `expiresAt` | LocalDateTime | Thời gian hết hạn (24 giờ sau khi tạo) |
| `paymentMethod` | String | Phương thức thanh toán (mặc định: "VIETQR") |

**Error Responses:**

- `400 Bad Request` - Validation errors:
```json
{
  "orderId": "Order ID is required",
  "amount": "Amount must be positive"
}
```

- `400 Bad Request` - Order not found:
```json
{
  "error": "Order not found with id: 1"
}
```

**Ví dụ sử dụng với cURL:**
```bash
curl -X POST http://localhost:8080/api/payments \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 1,
    "amount": 100000,
    "description": "Thanh toán đơn hàng #1"
  }'
```

**Ví dụ sử dụng với JavaScript (Fetch API):**
```javascript
fetch('http://localhost:8080/api/payments', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    orderId: 1,
    amount: 100000,
    description: 'Thanh toán đơn hàng #1'
  })
})
.then(response => response.json())
.then(data => {
  console.log('Payment created:', data);
  // Hiển thị QR code
  document.getElementById('qr-code').src = data.qrCodeUrl;
  // Lưu paymentCode để xác nhận sau
  localStorage.setItem('paymentCode', data.paymentCode);
});
```

---

### 2. Xác nhận Thanh toán Thành công

**Endpoint:** `POST /api/payments/confirm`

**Mô tả:** Xác nhận thanh toán đã thành công. API này được gọi từ frontend khi người dùng nhấn nút "Xác nhận thanh toán" sau khi đã quét QR code và chuyển tiền.

**Request Body:**
```json
{
  "paymentCode": "PAY-20240101120000-ABC12345"  // Required: Mã thanh toán từ payment đã tạo
}
```

**Request Fields:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `paymentCode` | String | ✅ Yes | Mã thanh toán từ response của API tạo payment |

**Response:** `200 OK`
```json
{
  "id": 1,
  "paymentCode": "PAY-20240101120000-ABC12345",
  "orderId": 1,
  "amount": 100000.00,
  "description": "Thanh toán đơn hàng #1",
  "status": "COMPLETED",
  "qrCodeUrl": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
  "qrCodeData": null,
  "createdAt": "2024-01-01T12:00:00",
  "expiresAt": "2024-01-02T12:00:00",
  "paymentMethod": "VIETQR"
}
```

**Lưu ý:** 
- Sau khi xác nhận thành công, `status` sẽ chuyển từ `PENDING` sang `COMPLETED`
- `paidAt` sẽ được cập nhật với thời gian hiện tại (không hiển thị trong response nhưng có trong database)

**Error Responses:**

- `400 Bad Request` - Payment code không hợp lệ:
```json
{
  "error": "Payment not found with code: PAY-20240101120000-ABC12345"
}
```

- `400 Bad Request` - Payment đã được xác nhận trước đó:
```json
{
  "error": "Payment is already COMPLETED"
}
```

- `400 Bad Request` - Payment đã hết hạn:
```json
{
  "error": "Payment has expired"
}
```

**Ví dụ sử dụng với cURL:**
```bash
curl -X POST http://localhost:8080/api/payments/confirm \
  -H "Content-Type: application/json" \
  -d '{
    "paymentCode": "PAY-20240101120000-ABC12345"
  }'
```

**Ví dụ sử dụng với JavaScript:**
```javascript
// Lấy paymentCode từ localStorage (đã lưu khi tạo payment)
const paymentCode = localStorage.getItem('paymentCode');

fetch('http://localhost:8080/api/payments/confirm', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    paymentCode: paymentCode
  })
})
.then(response => response.json())
.then(data => {
  if (data.status === 'COMPLETED') {
    alert('Thanh toán thành công!');
    // Chuyển hướng đến trang thành công
    window.location.href = '/payment-success';
  }
})
.catch(error => {
  console.error('Error:', error);
  alert('Có lỗi xảy ra khi xác nhận thanh toán');
});
```

---

### 3. Lấy thông tin Payment theo Payment Code

**Endpoint:** `GET /api/payments/{paymentCode}`

**Mô tả:** Lấy thông tin chi tiết của một payment dựa trên payment code.

**Path Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `paymentCode` | String | ✅ Yes | Mã thanh toán cần tìm |

**Response:** `200 OK`
```json
{
  "id": 1,
  "paymentCode": "PAY-20240101120000-ABC12345",
  "orderId": 1,
  "amount": 100000.00,
  "description": "Thanh toán đơn hàng #1",
  "status": "PENDING",
  "qrCodeUrl": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
  "qrCodeData": null,
  "createdAt": "2024-01-01T12:00:00",
  "expiresAt": "2024-01-02T12:00:00",
  "paymentMethod": "VIETQR"
}
```

**Error Response:**

- `404 Not Found` - Payment không tồn tại:
```json
(Empty body)
```

**Ví dụ sử dụng với cURL:**
```bash
curl http://localhost:8080/api/payments/PAY-20240101120000-ABC12345
```

**Ví dụ sử dụng với JavaScript:**
```javascript
const paymentCode = 'PAY-20240101120000-ABC12345';

fetch(`http://localhost:8080/api/payments/${paymentCode}`)
  .then(response => {
    if (response.status === 404) {
      throw new Error('Payment not found');
    }
    return response.json();
  })
  .then(data => {
    console.log('Payment info:', data);
    // Kiểm tra trạng thái
    if (data.status === 'COMPLETED') {
      console.log('Payment đã được xác nhận');
    } else if (data.status === 'PENDING') {
      console.log('Payment đang chờ thanh toán');
    }
  });
```

---

## 🔄 Luồng xử lý thanh toán

```
1. Frontend gọi API: POST /api/payments
   ↓
2. Backend tạo payment và generate QR code
   ↓
3. Backend trả về paymentCode và QR code image
   ↓
4. Frontend hiển thị QR code cho khách hàng
   ↓
5. Khách hàng quét QR code và chuyển tiền
   ↓
6. Frontend hiển thị nút "Xác nhận thanh toán"
   ↓
7. Người dùng nhấn nút → Frontend gọi API: POST /api/payments/confirm
   ↓
8. Backend cập nhật status = COMPLETED
   ↓
9. Frontend xử lý logic tiếp theo (cập nhật đơn hàng, gửi email, etc.)
```

## 📊 Trạng thái Payment

| Status | Mô tả |
|--------|-------|
| `PENDING` | Đang chờ thanh toán (mặc định khi tạo) |
| `COMPLETED` | Đã thanh toán thành công |
| `EXPIRED` | Đã hết hạn (sau 24 giờ) |
| `CANCELLED` | Đã hủy |

## ⚙️ Cấu hình

### Database Configuration

File: `src/main/resources/application.properties`

```properties
spring.datasource.url=jdbc:mysql://192.168.0.102:3307/AIMS_DATABASE
spring.datasource.username=root
spring.datasource.password=trungson01
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

### Server Configuration

```properties
server.port=8080
spring.application.name=AIMS_TKXDPM
```

## 🎨 Design Patterns được sử dụng

1. **Strategy Pattern**: `QrCodeGenerator` interface cho phép dễ dàng thay đổi cách generate QR code
2. **Repository Pattern**: Sử dụng Spring Data JPA để truy cập database
3. **Service Layer Pattern**: Tách biệt business logic khỏi controller
4. **DTO Pattern**: Tách biệt entity và API contract
5. **SOLID Principles**:
   - **Single Responsibility**: Mỗi class có một trách nhiệm duy nhất
   - **Open/Closed**: Có thể mở rộng QR generator mà không cần sửa code hiện có
   - **Dependency Inversion**: Depend on abstractions (QrCodeGenerator interface)

## 📦 Dependencies chính

- **Spring Boot 4.0.1**: Framework chính
- **Spring Data JPA**: ORM và database access
- **MySQL Connector**: Kết nối MySQL database
- **ZXing**: Thư viện generate QR code
- **Lombok**: Giảm boilerplate code
- **Spring Validation**: Validate request data

## 🚦 Cách chạy dự án

1. **Cài đặt MySQL** và tạo database theo schema đã cung cấp
2. **Cấu hình database** trong `application.properties`
3. **Build project:**
   ```bash
   mvn clean install
   ```
4. **Chạy ứng dụng:**
   ```bash
   mvn spring-boot:run
   ```
   hoặc
   ```bash
   java -jar target/AIMS_TKXDPM-0.0.1-SNAPSHOT.jar
   ```
5. **Test API** bằng Postman, cURL hoặc frontend application

## 📝 Lưu ý quan trọng

1. **Không có callback từ VietQR**: Hệ thống không xử lý callback từ VietQR. Frontend sẽ có nút để xác nhận thanh toán thành công.

2. **QR Code Format**: QR code được generate theo format VietQR chuẩn:
   ```json
   {
     "accountNo": "1234567890",
     "accountName": "AIMS STORE",
     "acqId": "970415",
     "amount": 100000,
     "addInfo": "Nội dung chuyển tiền",
     "template": "compact"
   }
   ```

3. **Payment Expiration**: Payment tự động hết hạn sau 24 giờ kể từ khi tạo.

4. **Payment Code Format**: `PAY-YYYYMMDDHHmmss-UUID` (ví dụ: `PAY-20240101120000-ABC12345`)

5. **QR Code Image**: QR code được trả về dưới dạng base64 string, có thể sử dụng trực tiếp trong thẻ `<img>`:
   ```html
   <img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA..." alt="QR Code" />
   ```

## 🐛 Xử lý lỗi

Tất cả các lỗi được xử lý bởi `GlobalExceptionHandler`:

- **Validation Errors**: Trả về `400 Bad Request` với danh sách lỗi validation
- **Runtime Exceptions**: Trả về `400 Bad Request` với message lỗi
- **Not Found**: Trả về `404 Not Found` khi không tìm thấy resource

## 📞 Hỗ trợ

Nếu có thắc mắc hoặc vấn đề, vui lòng liên hệ team phát triển.
