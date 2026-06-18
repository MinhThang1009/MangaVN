# 🎯 Danh sách Câu hỏi Vấn đáp (Q&A) dự kiến lúc Demo

Dưới đây là danh sách các câu hỏi "kinh điển" mà các thầy cô thường dùng để test xem sinh viên có thực sự hiểu luồng (logic flow) và kiến trúc (architecture) của ứng dụng hay không. Các câu hỏi này tập trung vào mức độ trừu tượng (abstract), thiết kế hệ thống, thay vì hỏi "dòng code này làm gì".

---

## 1. Kiến trúc tổng thể (Architecture & Design Pattern)
1. **Kiến trúc ứng dụng:** Nhóm đang sử dụng mô hình kiến trúc nào cho project này (MVVM, MVI, MVC...)? Tại sao lại chọn nó thay vì các mô hình khác?
2. **Luồng dữ liệu (Data Flow):** Hãy mô tả luồng dữ liệu một chiều (Unidirectional Data Flow) từ lúc người dùng bấm một nút trên màn hình cho đến khi UI được cập nhật.
3. **Phân lớp (Layering):** Các lớp (Layer) trong app được chia như thế nào? (UI Layer, Domain Layer, Data Layer). Chức năng của từng lớp là gì?
4. **Dependency Injection:** Nhóm quản lý việc khởi tạo các đối tượng và truyền phụ thuộc (dependencies) bằng thư viện nào (Hilt)? Tại sao phải dùng nó?

## 2. Jetpack Compose & UI State
5. **Quản lý State (State Management):** UI State được quản lý ở đâu và bằng cách nào? App xử lý việc xoay màn hình (Configuration changes) mà không bị mất dữ liệu như thế nào?
6. **Recomposition:** Cơ chế Recomposition của Compose hoạt động ra sao? Nhóm đã làm gì để tối ưu hiệu năng, tránh việc một widget bị vẽ lại (recompose) không cần thiết?
7. **Responsive Design:** Ứng dụng xử lý việc thay đổi giao diện khi xoay ngang điện thoại (Landscape) hoặc dùng trên Tablet như thế nào? Logic đó được quyết định ở đâu?
8. **Thay đổi Theme/Ngôn ngữ:** Việc đổi Dark/Light mode hay đổi Tiếng Việt/Tiếng Anh lúc runtime (không cần khởi động lại app) được thiết kế luồng như thế nào?

## 3. Data Layer (Mạng & Cơ sở dữ liệu)
9. **Gọi API (Network):** Luồng gọi API MangaDex diễn ra như thế nào? Khi mất mạng hoặc API trả về lỗi thì app bắt lỗi (Error Handling) ở tầng nào và báo lên UI ra sao?
10. **Lưu trữ Cục bộ (Local Database):** Thư viện (Library) và Lịch sử đọc (Reading History) được lưu bằng công nghệ gì (Room)? Tại sao lại lưu ở Local thay vì trên Server?
11. **Đồng bộ Dữ liệu (Source of Truth):** Khi một truyện vừa có dữ liệu lưu dưới Local (Room) vừa cần update từ Network (API), thì tầng Data (Repository) quyết định trả về dữ liệu nào cho UI? (Cơ chế Single Source of Truth).
12. **DataStore (Preferences):** App làm sao biết được người dùng là "lần đầu tiên mở app" để hiện màn hình Onboarding (Welcome Carousel)? 

## 4. Các tính năng cốt lõi (Core Features)
13. **Tính năng Tải Offline:** Khi bấm nút "Tải xuống", luồng logic diễn ra ở đâu? App sử dụng cơ chế nào để tải ngầm (Background task) ngay cả khi người dùng thoát màn hình hoặc đóng app (WorkManager/Coroutines)?
14. **Tính năng Đọc truyện (Reader):** Dữ liệu các trang truyện (ảnh) được load và hiển thị như thế nào để đọc trơn tru? App có cơ chế cache ảnh không (Coil)?
15. **Tính năng Thống kê (Statistics):** Biểu đồ trong app lấy dữ liệu từ đâu? Các phép toán đếm số chương, truyện yêu thích được tính toán dưới Database (bằng SQL query) hay đem lên RAM (ViewModel) mới tính?

## 5. Testing & CI/CD
16. **Kiểm thử (Testing):** Nhóm có viết Unit Test không? Đối với giao diện (Compose) thì test kiểu gì (Screenshot test / Roborazzi)? Mục đích của việc test này là gì?
17. **Cơ chế CI/CD (GitHub Actions):** Khi push code lên nhánh chính (main), điều gì sẽ xảy ra? Tại sao nhóm lại cần các quy trình tự động chặn code lỗi (Pre-commit hook/Coverage threshold)?

---
> 💡 **Tip:** Đọc lướt qua danh sách này xem câu nào nhóm bạn còn "lấn cấn". Hãy phản hồi lại những câu bạn muốn mình giải thích chi tiết, mình sẽ mô tả câu trả lời dưới dạng sơ đồ luồng (flow) dễ hiểu nhất cho bạn!
