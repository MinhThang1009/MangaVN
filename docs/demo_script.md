# 🎬 Kịch bản Demo: MyBooksLibrary (Android App)

Chào bạn, dựa vào cấu trúc và thiết kế (đặc biệt là bản redesign giao diện Cinematic cực xịn xò) của project **MyBooksLibrary**, mình đã soạn cho nhóm bạn một kịch bản demo thật sự "ăn điểm". 

Kịch bản được chia làm 2 cột chính: **Thao tác (Show)** và **Lời thoại (Nói)**, kết hợp với các điểm cần nhấn mạnh để giáo viên/khán giả thấy được sự đầu tư về kĩ thuật (100% Compose, Hilt, Room, Flow, Animation).

> **Chuẩn bị trước Demo:** 
> - Xóa dữ liệu app (Clear Data) hoặc gỡ cài đặt rồi cài lại để hiển thị được luồng Onboarding (Welcome Carousel) và Splash screen.
> - Bật mạng ổn định để gọi API MangaDex.
> - Có thể cắm máy ảo (Emulator) hoặc cast màn hình điện thoại thật lên màn hình lớn.

---

## Phân cảnh 1: Mở màn & First Impression (Tạo ấn tượng ban đầu)

| 📱 Thao tác trên App (Show) | 🗣 Lời thoại (Nói) |
|:---|:---|
| **Bấm mở App.**<br>*(Màn hình Splash Screen hiện lên với logo rồi chuyển mượt sang màn Welcome Carousel).* | "Chào thầy/cô và các bạn, đây là **MyBooksLibrary** - ứng dụng đọc Manga đa ngôn ngữ do nhóm chúng em phát triển dựa trên nguồn dữ liệu khổng lồ từ MangaDex API." |
| **Vuốt nhẹ qua lại 3 trang Onboarding (Welcome Carousel).**<br>*(Để lộ hiệu ứng Parallax trượt giữa hình ảnh và chữ, nhấn mạnh vào Dark-theme và Font chữ đẹp).* | "Ngay từ lần đầu vào app, người dùng sẽ được đón chào bởi luồng Onboarding. Giao diện được nhóm thiết kế theo ngôn ngữ **Cinematic Dark-first** - tối ưu cho việc đọc truyện, mang lại cảm giác cao cấp như Netflix hay Crunchyroll. Nhóm em sử dụng 100% Jetpack Compose kết hợp Material 3 cho toàn bộ UI." |
| **Bấm "Bắt đầu" ở trang cuối.**<br>*(Chuyển sang màn hình Đăng nhập).* | "Ứng dụng hỗ trợ đăng nhập qua tài khoản hoặc dùng Google Sign-In để vào màn hình chính." |

---

## Phân cảnh 2: Khám phá truyện (Discover) & Navigation

| 📱 Thao tác trên App (Show) | 🗣 Lời thoại (Nói) |
|:---|:---|
| **Đăng nhập xong, vào thẳng tab Discover.**<br>*(Từ từ cuộn xuống. Các bìa truyện sẽ có hiệu ứng Staggered fade-up hiện lên từng cái một).* | "Đây là màn hình Khám phá. Điểm nhấn kĩ thuật ở đây là thanh điều hướng **Floating Pill NavBar** nằm lơ lửng dưới đáy màn hình với hiệu ứng kính mờ (Glassmorphism), tự động ẩn/hiện khi cuộn." |
| **Chỉ vào cấu trúc màn hình (Hero banner & Shelves).** | "Các truyện được tải bất đồng bộ (Asynchronous) qua Coroutines và Flow, với hiệu ứng hiện dần (Staggered fade-up) rất mượt mà. Ảnh bìa cũng được bo góc, tỷ lệ chuẩn 2:3 đồng nhất toàn app." |
| **Bấm sang tab Tìm kiếm (Search).**<br>*(Bấm thử vào nút Filter để mở Bottom Sheet).* | "Người dùng có thể tìm kiếm truyện hoặc dùng bộ lọc thông minh qua Bottom Sheet để lọc theo thể loại, trạng thái truyện." |

---

## Phân cảnh 3: Chi tiết truyện & Tải Offline (Trọng tâm)

| 📱 Thao tác trên App (Show) | 🗣 Lời thoại (Nói) |
|:---|:---|
| **Bấm vào một truyện bất kỳ ở trang chủ.**<br>*(Màn hình Detail mở ra).* | "Khi bấm vào một truyện, màn hình chi tiết hiện ra với giao diện Edge-to-Edge (tràn viền)." |
| **Vuốt cuộn lên xuống màn hình chi tiết.**<br>*(Cho thấy hiệu ứng Parallax của ảnh bìa Hero và Header).* | "Bọn em đã cài đặt hiệu ứng Parallax cho ảnh bìa, khi cuộn lên thì ảnh sẽ trượt chậm lại so với nội dung, tạo chiều sâu cho giao diện." |
| **Bấm icon "Tải xuống" ở một chapter bất kỳ.**<br>*(Icon chuyển trạng thái sang Downloading, thanh tiến trình chạy).* | "Ứng dụng cũng hỗ trợ tải Chapter về để đọc Offline. Tiến trình tải được quản lý bằng Room Database và chạy ngầm, ngay cả khi người dùng thoát màn hình này." |

---

## Phân cảnh 4: Trải nghiệm đọc (Immersive Reader)

| 📱 Thao tác trên App (Show) | 🗣 Lời thoại (Nói) |
|:---|:---|
| **Bấm "Đọc ngay".**<br>*(Lần đầu tiên vào Reader sẽ hiện Reader Spotlight Overlay - hướng dẫn thao tác).* | "Khi người dùng lần đầu vào màn hình đọc, hệ thống sẽ hiện overlay (Spotlight) hướng dẫn vùng chạm lật trang. Trạng thái này được lưu vào DataStore để không hiển thị lại ở lần sau." |
| **Chạm vào giữa màn hình.**<br>*(Hiện lên Glass Bars ở trên và dưới).* | "Giao diện đọc được thiết kế tràn viền hoàn toàn (Immersive). Chạm vào giữa sẽ gọi các thanh công cụ kính mờ. Nhóm em đã xử lý tinh tế vùng khuyết (Notch) trên màn hình để chữ không bị che." |
| **Bấm nút Settings dưới thanh bar (Đổi chế độ đọc).**<br>*(Đổi từ dọc Webtoon sang lật ngang).* | "Hệ thống hỗ trợ nhiều chế độ đọc: cuộn dọc kiểu Webtoon, hoặc lật trang ngang truyền thống." |

---

## Phân cảnh 5: Quản lý Cá nhân & Thống kê

| 📱 Thao tác trên App (Show) | 🗣 Lời thoại (Nói) |
|:---|:---|
| **Bấm Back, quay ra ngoài và vào tab Thư viện (Library) hoặc Profile.** | "Toàn bộ lịch sử đọc, truyện đang theo dõi sẽ được lưu lại đồng bộ." |
| **Vào trang Cá nhân (Profile) -> Thống kê (Statistics).**<br>*(Màn hình hiện ra các biểu đồ cột, biểu đồ tròn phân tích dữ liệu).* | "Đặc biệt, nhóm em phát triển tính năng Thống Kê (Statistics). Dựa trên cơ sở dữ liệu local Room, ứng dụng tự tính toán và vẽ các biểu đồ (Pie Chart, Column Chart) trực quan về tiến độ đọc, số chương đã cày trong tuần, và top truyện yêu thích." |

---

## Phân cảnh 6: Responsive & Settings (Chốt hạ Kỹ thuật)

| 📱 Thao tác trên App (Show) | 🗣 Lời thoại (Nói) |
|:---|:---|
| **Vào màn hình Cài đặt (Settings). Đổi thử ngôn ngữ hoặc Theme (Giao diện).**<br>*(Chuyển ngay lập tức không cần restart lại App).* | "Ứng dụng được thiết kế linh hoạt. Việc đổi ngôn ngữ Tiếng Anh/Tiếng Việt hay giao diện Sáng/Tối diễn ra ngay lập tức (Runtime) mà không cần khởi động lại Activity." |
| **Xoay ngang điện thoại (Landscape mode).**<br>*(Thanh điều hướng dưới đáy tự động biến thành thanh điều hướng dọc (Nav Rail) ở bên trái, các Grid truyện tăng số cột).* | "Cuối cùng, ứng dụng hỗ trợ hoàn hảo Responsive Design. Khi xoay ngang thiết bị, thanh Navigation sẽ tự morphing biến thành thanh dọc bên trái (Nav Rail), nội dung tự dàn lại cột để tối ưu không gian hiển thị cho màn hình lớn." |
| **Đứng ở trang chủ và chốt.** | "Đó là toàn bộ luồng chức năng chính của MyBooksLibrary. Cảm ơn thầy/cô và các bạn đã theo dõi bản demo." |

---

## 💡 Các "Keyword" ăn điểm cần nói lúc Demo:
1. **100% Jetpack Compose & Material 3:** Bắt kịp công nghệ mới nhất của Android.
2. **Glassmorphism & Cinematic Design:** Giao diện rất đẹp, hiện đại, không dùng thiết kế Material cũ, có dark theme chuẩn.
3. **Responsive Design / WindowSizeClass:** Xoay dọc ngang mượt mà, app tự thích ứng UI.
4. **Clean Architecture & MVI:** Bọn em phân lớp code rõ ràng (Mặc dù không nhìn thấy qua UI nhưng thầy cô hỏi thì báo cáo là có sử dụng ViewModel, Hilt DI, Flow).
5. **A11y (Accessibility) / Reduce-motion:** Chăm chút từng chi tiết, có xử lý giảm hiệu ứng cho máy yếu và hỗ trợ các kích cỡ màn hình khác nhau.
