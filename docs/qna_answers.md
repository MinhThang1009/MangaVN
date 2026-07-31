# 💡 Câu trả lời Q&A: Focus vào Logic và Implementation

Dưới đây là phần trả lời cho các câu hỏi quan trọng, mình đã bỏ qua các câu 1, 2, 3, 5 theo yêu cầu. Lý thuyết sẽ nói cực kỳ ngắn gọn, tập trung chủ yếu vào **"Nó nằm ở file nào?"** và **"Logic chạy ra sao?"** để bạn dễ dàng trình bày với thầy.

---

## Phần 1: Kiến trúc & Jetpack Compose

### 4. Dependency Injection (Hilt)
- **Lý thuyết:** Tự động khởi tạo và truyền các cục "phụ thuộc" (như Database, API) cho các class cần dùng (như ViewModel), giúp code không bị dính chặt vào nhau.
- **Implement ở đâu:** 
  - File `MyApp.kt` (kế thừa Application) có gắn `@HiltAndroidApp`.
  - Các file cấu hình nằm trong thư mục `di/` (ví dụ: `DatabaseModule.kt`, `NetworkModule.kt`).
  - Ở các ViewModel (VD: `ProfileViewModel`), chỉ cần thêm annotation `@HiltViewModel` và ném repository vào constructor, Hilt sẽ tự nhét (inject) object vào.

### 6. Tối ưu Recomposition
- **Logic:** Tránh vẽ lại toàn bộ màn hình khi chỉ một nút bị thay đổi trạng thái.
- **Implement:** 
  - Sử dụng từ khóa `remember` cho các state nhỏ. 
  - Dùng `Flow` kết hợp `collectAsStateWithLifecycle()` ở UI để UI chỉ cập nhật (recompose) khi Flow nhả ra data mới.
  - Quan trọng nhất: Ở các danh sách dài `LazyColumn` hoặc `LazyVerticalGrid` (ví dụ list truyện trong `LibraryScreen.kt`), luôn truyền thuộc tính `key = { it.mangaId }`. Nhờ có `key`, Compose phân biệt được item nào đã đổi và chỉ vẽ lại đúng item đó, thay vì vẽ lại cả cái list.

### 7. Responsive Design (Xoay màn hình)
- **Logic:** Thay vì hardcode cứng kích cỡ, app dựa vào thư viện `material3-window-size-class` để phân loại độ rộng màn hình (Compact - đt dọc, Medium - đt ngang, Expanded - tablet). 
- **Implement ở đâu:** 
  - Tại `MainActivity.kt` tính toán `WindowSizeClass`.
  - Ở các component điều hướng (VD: `MainNavGraph.kt` hoặc `FloatingPillNavBar.kt`), logic quy định: Nếu màn hình là Compact -> Render thanh điều hướng ngang lơ lửng dưới đáy (Bottom Pill Bar). Nếu là Medium trở lên -> Render thanh dọc bên trái (Nav Rail). 
  - Tương tự trong `LibraryScreen.kt` hay `SearchScreen.kt`, số cột của `LazyVerticalGrid` sẽ thay đổi linh hoạt (ví dụ: màn dọc 3 cột, màn ngang 5-6 cột).

### 8. Đổi Theme và Ngôn ngữ Runtime (Không giật/restart)
- **Logic:** Cách truyền thống của Android khi đổi ngôn ngữ sẽ làm Activity `recreate()` (khởi động lại, gây chớp màn hình). App chúng ta không dùng cách đó. Ngôn ngữ được lấy từ DataStore và đẩy xuống UI như một State.
- **Implement ở đâu:** 
  - Nằm ở file `ui/util/LocaleHelper.kt`. Chúng ta có biến môi trường `LocalAppLocale`.
  - App tự chế hàm `appString(R.string.key)` thay cho `stringResource()` mặc định. Hàm này tự động đọc String tương ứng từ resource `values/` hoặc `values-vi/` dựa vào giá trị Locale hiện tại.
  - Khi user vào `SettingScreen.kt` đổi ngôn ngữ -> Cập nhật State -> Jetpack Compose tự động vẽ lại (recompose) các node Text ngay lập tức, cực kỳ mượt mà.

---

## Phần 2: Data Layer (API & Database)

### 9. Gọi API (Network) & Bắt lỗi
- **Logic:** UI không bao giờ chạm trực tiếp vào Mạng. Logic là: `UI -> ViewModel -> UseCase/Repository -> API`. 
- **Implement ở đâu:** 
  - API định nghĩa bằng Retrofit trong `data/remote/MangaDexApi.kt`.
  - Trong `RepositoryImpl.kt`, các call API được bọc trong khối `try-catch`. Nếu có lỗi (ví dụ `IOException` rớt mạng), Repository sẽ bắt lại và return ra lớp bao bọc (Wrapper) `Result.Error` hoặc ném ra Exception tùy thiết kế.
  - Sau đó `ViewModel` nhận kết quả, nếu lỗi, sẽ đổi UI state sang trạng thái `Error`, UI (như `SearchScreen.kt`) sẽ tự động render component `ErrorState` (hiện ảnh lỗi + nút Thử lại).

### 10. Local Database (Room) & 11. Single Source of Truth
- **Logic (Rất quan trọng - nên trình bày kỹ):** Nhóm áp dụng nguyên lý **Single Source of Truth** (Nguồn sự thật duy nhất). Giao diện (UI) chỉ quan sát (Observe) và hiển thị dữ liệu từ Database (Room), KHÔNG đọc trực tiếp từ Network. 
- **Implement ở đâu:**
  - File định nghĩa bảng nằm ở `data/local/LibraryItemEntity.kt`.
  - Khi user mở màn hình Library, `LibraryViewModel` thu thập luồng dữ liệu (Flow) từ `LibraryDao`. 
  - Nếu cần đồng bộ data mới từ Server, `LibraryRepository.kt` sẽ gọi lên mạng. Có kết quả từ mạng -> cập nhật thẳng vào Local Database. Room Database sau khi được ghi đè sẽ "phát" (emit) tín hiệu Flow lên UI tự động cập nhật. Cơ chế này giúp app chạy được Offline (vì UI vẫn luôn đọc được từ Database).

### 12. DataStore (Preferences & Onboarding)
- **Logic:** App muốn lưu cờ (flag) kiểu boolean: "User này đã xem màn hình giới thiệu chưa?".
- **Implement ở đâu:** 
  - Lưu tại `data/local/UserPreferencesDataStore.kt` (key `onboarding_welcome_done`).
  - Trong `MainNavGraph.kt` (luồng điều hướng chính), có gắn một guard kiểm tra. Nếu flag = `false`, lập tức đẩy người dùng vào `WelcomeCarouselScreen.kt`. Người dùng bấm "Bắt đầu" -> đổi flag thành `true` -> lần mở app sau sẽ đi thẳng vào màn đăng nhập.

---

## Phần 3: Core Features (Tính năng trọng tâm)

### 13. Tính năng Tải Offline (Download)
- **Logic:** Tải nhiều ảnh về máy là tác vụ nặng, mất thời gian. Nếu viết trong ViewModel, khi user tắt app, Android sẽ kill tác vụ đi. Giải pháp là dùng Worker (chạy ngầm, được OS bảo vệ).
- **Implement ở đâu:** 
  - Tác vụ ngầm được viết trong class `ChapterDownloadWorker.kt` (sử dụng thư viện WorkManager). 
  - Thông tin "Đang tải tới đâu" lưu bằng `DownloadQueueEntity.kt` trong Room Database.
  - Ở lớp đáy, `OfflineDownloadStorage.kt` thực hiện công việc đọc luồng byte từ Network và ghi cục bộ thành các tệp bitmap (hình ảnh) nằm trong thẻ nhớ/bộ nhớ trong của máy. 
  - File UI `ChapterComponents.kt` sẽ đọc Database và hiển thị nút "Downloading" xoay xoay hoặc nút "V" (Đã tải xong).

### 14. Tính năng Đọc Truyện (Immersive Reader)
- **Logic:** Nạp hàng chục ảnh truyện tranh vào bộ nhớ RAM sẽ gây tràn (Crash OOM). Nhóm xử lý bằng thư viện nạp ảnh thông minh (Coil) và cuộn lười (Lazy).
- **Implement ở đâu:** 
  - Logic gọi danh sách URL các trang ảnh ở `domain/usecase/LoadReaderPagesUseCase.kt`.
  - Tại `ui/screens/reader/`, màn hình sử dụng `LazyColumn` (để cuộn dọc như đọc Webtoon) hoặc `HorizontalPager` (để vuốt lật trang qua ngang). Các ảnh được nạp bằng thẻ `<AsyncImage>` (từ thư viện Coil), tự động load khi lướt tới, tự động cache trên đĩa để không tải lại khi vuốt lên xuống. 
  - Các thanh công cụ mờ mờ ở trên/dưới nằm trong `ReaderBars.kt`.

### 15. Tính năng Thống kê (Statistics - Chart)
- **Logic:** Nhóm tự làm thuật toán đếm tổng số chương đã đọc, truyện yêu thích. Tuy nhiên, tính toán trực tiếp trên hàng vạn dòng dữ liệu ở bộ nhớ (RAM) là không tối ưu. Nhóm tính toán thẳng từ dưới Database (SQL).
- **Implement ở đâu:** 
  - Các phép đếm (`COUNT`), gom nhóm (`GROUP BY`) được viết ngay bằng lệnh SQL trong file DAO (ví dụ data class `TopMangaCount` lưu số liệu top truyện).
  - Tầng `StatisticsViewModel.kt` chỉ gọi hàm DAO, nhận về những con số "đã được xào nấu", và đưa sang UI cho màn hình `StatisticsScreen.kt` vẽ lên các biểu đồ Tròn (PieChart) và biểu đồ Cột (ColumnChart).

---

## Phần 4: Quality & CI/CD

### 16. Unit Test & Screenshot Test
- **Logic:** Test tự động để đảm bảo code không bị hỏng khi thêm tính năng mới. Đặc biệt, UI trên Compose có một kiểu test cực mạnh.
- **Implement ở đâu:** 
  - Logic nghiệp vụ (ViewModel, UseCase) dùng Unit Test với JUnit.
  - Giao diện được kiểm thử bằng kỹ thuật **Roborazzi Screenshot Testing**. App tự động vẽ component ra thành 1 bức ảnh `.png` (ở thư mục `app/src/test/screenshots/`). Hệ thống sẽ tự so ảnh đó với tấm ảnh "gốc chuẩn" (Golden Image) lưu trên kho. Lệch 1 pixel hay 1 mã màu do code sai, test sẽ báo Failed ngay. Đảm bảo UI luôn chính xác tuyệt đối ở từng commit.

### 17. Tự động hóa CI/CD
- **Logic:** Đảm bảo source code trên nhánh chính `main` luôn sạch sẽ, không có code thối rác hay code lỗi build.
- **Implement ở đâu:** 
  - Dùng GitHub Actions (Các file YAML định nghĩa ở thư mục `.github/workflows/`). 
  - Bất cứ ai tạo Pull Request muốn gộp code, GitHub Actions sẽ tạo một máy chủ ảo từ xa (CI), chạy thử 3 lệnh: `build-test` (biên dịch thử), `static-analysis` (như linter/detekt soi lỗi thụt đầu dòng, định dạng, cảnh báo code thừa) và `emulator-test` (cắm máy ảo chạy androidTest). Phải "Xanh" cả 3 dấu tick thì GitHub mới cho bấm nút Merge code.
