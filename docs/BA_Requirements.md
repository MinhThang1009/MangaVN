# ĐẶC TẢ YÊU CẦU: MYBOOKSLIBRARY (LOCAL-FIRST)

## 1. Tổng quan Kiến trúc
- **UI Framework:** Kotlin + Jetpack Compose.
- **Navigation:** Jetpack Navigation Compose.
- **Local Database:** Room DB (Lưu user profile ảo, lịch sử đọc, yêu thích).
- **Local Preferences:** Jetpack DataStore (Lưu trạng thái đăng nhập, theme).
- **Network/API:** Retrofit + OkHttp (Call MangaDex API).
- **Image Loading:** Coil.
- **Architecture:** MVVM + Clean Architecture (UI - Domain - Data).

## 2. Thực thể Dữ liệu (Entities & Models)

### 2.1. Local Entities (Room Database)
* **UserEntity:** `id` (PK), `username`, `password` (mô phỏng), `avatar_path`, `created_at`.
* **LibraryItemEntity** (Quản lý sách user tương tác):
    * `manga_id` (PK - map với ID từ MangaDex).
    * `title`, `cover_url` (Lưu đệm để hiển thị nhanh không cần gọi lại API).
    * `status` (Enum: READING, COMPLETED, FAVORITE).
    * `last_read_chapter_id` (Lưu tiến độ đọc).
    * `last_read_page_index` (Lưu vị trí trang đang đọc dở).
    * `updated_at`.

### 2.2. Remote Models (MangaDex API)
*(Chỉ dùng để parse JSON trả về, không lưu vào Room)*
* **MangaModel:** `id`, `title`, `description`, `cover_art`, `rating`, `tags/genres`.
* **ChapterModel:** `id`, `manga_id`, `chapter_number`, `title`, `pages` (List URL ảnh).

## 3. Phân định Logic
- **MangaDex API:** Lấy danh sách truyện (Discover), tìm kiếm (Search), chi tiết truyện, và tải ảnh trang truyện.
- **Room Database:** Xác thực người dùng (Auth) và quản lý dữ liệu cá nhân (Library).

## 4. Luồng Điều hướng (Navigation Flow)

**A. Auth Flow**
- Khởi chạy -> Kiểm tra DataStore:
  - Nếu chưa đăng nhập: Hiện `LoginScreen` -> Nhập user/pass -> Lưu DB -> Chuyển sang Main Flow.
  - Nếu đã đăng nhập: Chuyển thẳng sang Main Flow.

**B. Main Flow (Bottom Navigation)**
1.  **Discover Tab (`DiscoverScreen`):** Gọi API lấy list truyện -> Click mở `MangaDetailScreen`.
2.  **Search Tab (`SearchScreen`):** Nhập keyword -> Gọi API tìm kiếm -> Hiển thị list (title, cover, rating) -> Click mở `MangaDetailScreen`.
3.  **My Library Tab (`LibraryScreen`):** Có top tab/filter (Lịch sử / Yêu thích). Query từ bảng `LibraryItemEntity` -> Click mở tiếp chapter đang đọc dở hoặc mở Detail.
4.  **User Setting Tab (`SettingScreen`):** Hiển thị Info User -> Các chức năng: Xóa Cache (Coil) / Đăng xuất (Clear DataStore, quay về Auth Flow).

**C. Detail & Reader Flow**
- **`MangaDetailScreen`:** Hiện mô tả, ảnh, list chapter. Có nút "Đọc ngay" và "Yêu thích" (Lưu xuống Room).
- **`ReaderScreen`:** Hiện trang truyện của 1 chapter. Khi vuốt đến trang cuối, tự động cập nhật `last_read_chapter_id` xuống DB.