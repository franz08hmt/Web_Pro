# Giao diện Robot Assembly Lab

## Phase 2 — Dark theme thống nhất, ảnh nền thật, dữ liệu kỹ thuật

Nhánh `feature/ui-enhancement-phase-2`. Phase 2 giữ nguyên hướng industrial của ảnh
tham chiếu (nền graphite, điểm nhấn cam) và nâng lên một bậc: dark theme cho cả sáu
trang, ảnh chụp thật làm nền, dữ liệu kỹ thuật sâu hơn và một số tính năng web hiện đại.

### Quyết định chính

| Chủ đề | Phase 1 | Phase 2 |
|---|---|---|
| Tông màu | 3 trang tối, 3 trang sáng | Cả 6 trang cùng một bảng màu tối |
| Cơ chế | Class `.dark-page` phủ lên `:root` sáng | Dark là mặc định của `:root`, không còn biến thể |
| Điều hướng | Sidebar cố định | Sidebar cố định + nút thu gọn, nhớ trạng thái |
| Nền | Nền phẳng + hoa văn chấm | Ảnh chụp thật, luôn có lớp scrim phía trên |
| Cỡ chữ gốc | 14px | 16px, không còn giá trị nào dưới 12px |

Lý do bỏ `.dark-page`: giữ hai bảng màu song song khiến việc chuyển trang tạo cảm giác
đứt gãy và làm mọi component phải viết hai lần. Một hệ token duy nhất vừa nhất quán
vừa dễ bảo trì.

### Bảng token màu

```
--bg-base   #0e1215   nền trang
--bg-raised #151b1f   dải section xen kẽ, thanh ngữ cảnh
--surface   #1b2226   thẻ
--surface-2 #232c31   thẻ lồng, input, checklist
--rail      #0a0e10   sidebar và footer

--line      #2c363b   --line-strong #3d4a50
--ink       #eef3f2   (≈15:1 trên --bg-base)
--muted     #a9b6b4   (≈9:1 trên --bg-base, ≈7.7:1 trên --surface)

--accent    #ff8b4b   cam kỹ thuật: CTA, tiến độ, số bước, nav đang chọn
--primary   #2dd4bf   xanh robot: dữ liệu, cột nhấn trong biểu đồ
--success   #4ade80   trạng thái đã đủ linh kiện
```

Cam giữ vai trò màu nhận diện đúng như ảnh tham chiếu. Xanh `--primary` chỉ dùng cho
lớp dữ liệu để không cạnh tranh với cam.

### Thang typography

| Vai trò | Giá trị |
|---|---|
| `body` | 16px / 1.7 |
| Body copy, mô tả thẻ | 15 – 16px |
| Caption, footer, ghi chú bảng | 13 – 14px |
| Eyebrow, label uppercase | 12px (giá trị nhỏ nhất toàn site) |
| `h1` | `clamp(34px, 4vw, 52px)` |
| `h2` | `clamp(24px, 2.4vw, 34px)` |
| `h3` | 20px |
| Nút, input, select | ≥ 15px, cao ≥ 44px |

Đã đo bằng trình duyệt trên cả sáu trang: cỡ chữ hiển thị nhỏ nhất là **12px**.

### Ảnh nền và cách giữ tương phản

Ảnh lưu tại `assets/images/backgrounds/`, hai kích thước 1600px và 900px, tổng **604 KB**.
Nguồn và giấy phép ghi trong `assets/images/backgrounds/CREDITS.md`, đồng thời hiển thị
tại mục "Nguồn hình ảnh" trong trang Thư viện vì giấy phép CC BY yêu cầu ghi công.

Quy tắc bắt buộc: **chữ không bao giờ đặt trực tiếp lên ảnh.** Mọi vùng ảnh đều có lớp
`--scrim` phủ lên trên, kết thúc bằng `--bg-base` đặc để ảnh tan dần vào trang thay vì
để lại một đường cắt ngang.

| Trang | Ảnh nền |
|---|---|
| Trang chủ | Hero: bàn thực hành điện tử |
| Mẫu robot, Thành viên | Dải đầu trang: bàn thực hành |
| Linh kiện, Thư viện | Dải đầu trang: bo mạch Arduino cận cảnh |
| Lắp ráp | **Không có ảnh nền** — xem lý do bên dưới |

Trang Lắp ráp cố ý giữ nền phẳng. Đây là trang thao tác: người dùng đọc checklist, theo
dõi tiến độ và tra bảng chân cắm. Ảnh nền ở đây làm giảm khả năng đọc đúng phần quan
trọng nhất của website.

Ảnh linh kiện là ảnh sản phẩm nền trắng. Thay vì để nền trắng đâm vào giao diện tối,
`.card-image` dùng một tấm nền sáng có chủ đích, đọc như một trang thông số sản phẩm.

### Dữ liệu bổ sung

Toàn bộ nằm trong `assets/js/data.js`, không tạo nguồn dữ liệu thứ hai.

- `COMPONENTS_DATA` thêm trường `specs` cho **10 linh kiện** (điện áp, dòng, chân kết
  nối, tầm đo…). Số liệu lấy theo datasheet phổ biến; chỗ nào thay đổi theo nhà sản
  xuất thì ghi khoảng giá trị. Hiển thị bằng `<dl>/<dt>/<dd>` trong `<details>`.
- `ROBOT_MODELS` thêm `buildTime`, `mainSensor`, `skills` cho bảng so sánh, và `wiring`
  — sơ đồ chân Arduino của từng mẫu, đổ ra `<table>` cập nhật theo mẫu đang chọn.
- Biểu đồ cột phân bố linh kiện ở trang Linh kiện là **SVG tĩnh viết thẳng trong HTML**,
  kèm `<title>` và một danh sách text chứa đúng các con số, nên vẫn đọc được khi tắt
  JavaScript và với trình đọc màn hình.
- Vòng tiến độ ở trang Lắp ráp là SVG có `aria-hidden="true"`: nó chỉ lặp lại bằng hình
  ảnh những gì `<progress>` và vùng `role="status"` đã truyền đạt.

Số lượng thẻ linh kiện trong `pages/linh-kien.html` giữ nguyên **9 thẻ** và 5 bộ lọc.
Thông số được chèn vào các thẻ sẵn có qua `data-component-id`, không thêm bớt thẻ nào,
vì `tests/ui-smoke.cjs` kiểm tra cứng bộ số 9 / 2 / 2 / 3 / 2.

### Tính năng web hiện đại

| Tính năng | Dùng ở đâu | Cách suy biến |
|---|---|---|
| `@layer` | `style.css` chia thành tokens / base / layout / components / pages / motion / responsive | Trình duyệt cũ bỏ qua, thứ tự nguồn vẫn đúng |
| Container queries | `.featured-card`, `.robot-card` đổi bố cục theo bề rộng **container** | Không hỗ trợ thì giữ bố cục mặc định |
| `:has()` | Checklist đổi màu khi tick; cả panel đổi trạng thái khi `#parts-progress[value="100"]` | Chỉ mất phần trang trí, logic vẫn do JS |
| `color-mix()` | Sinh sắc độ phụ từ `--accent` / `--primary` thay vì thêm hằng số hex | Trình duyệt cũ dùng màu nền gần nhất |
| `image-set()` | Chọn ảnh nền 900px hay 1600px | Có `background-image` dự phòng |
| View Transitions | `@view-transition { navigation: auto }` cho chuyển trang | Trình duyệt không hỗ trợ thì điều hướng bình thường |
| `IntersectionObserver` | Hiệu ứng xuất hiện khi cuộn | Không có observer thì nội dung hiển thị luôn |
| `localStorage` | Trạng thái thu gọn sidebar, mẫu robot và checklist đang chọn | Bọc `try/catch`, không có giá trị lưu vẫn chạy đúng |

Nút thu gọn sidebar có `aria-expanded` và `aria-controls`. Khi thu gọn, nhãn của mỗi mục
được ẩn bằng `clip-path` chứ không phải `display: none`, nên trình đọc màn hình vẫn đọc
được tên trang; JavaScript đồng thời gắn `title` để người dùng chuột thấy chú thích.

### Ba lỗi phát hiện trong lúc kiểm thử

1. **Scroll reveal dùng `opacity` làm hỏng tương phản.** axe báo `color-contrast` trên
   hàng loạt phần tử: chữ mờ, kể cả trong lúc transition, không đạt WCAG AA. Đã đổi sang
   reveal **chỉ bằng `transform`**, không dùng `opacity`.
2. **Reveal áp lên cả nội dung trong màn hình đầu.** Phần hero bị dịch 16px ngay khi tải,
   khiến nút CTA vẫn đang di chuyển lúc người dùng đưa chuột tới, và làm điểm bắt đầu
   của thứ tự Tab bị sai (nhấn Tab lần đầu không vào được skip link). Đã giới hạn reveal
   cho phần nằm dưới màn hình đầu.
3. **`.sr-only` thoát khỏi vùng cuộn ngang.** Các `<span class="sr-only">` trong bảng dùng
   `position: absolute` nhưng `.table-scroll` không phải positioned ancestor, nên chúng
   định vị theo tổ tiên khác và kéo giãn cả trang ở 320px. Đã thêm `position: relative`
   cho `.table-scroll`.

Vùng cuộn ngang của bảng có `tabindex="0"` để người dùng bàn phím cuộn được — nếu không,
axe báo `scrollable-region-focusable`.

### Kiểm thử

Khởi động trong thư mục dự án:

```powershell
python -m http.server 4173 --bind 127.0.0.1
```

Terminal khác:

```powershell
node tests/ui-smoke.cjs
```

Đặt `NODE_PATH` trỏ tới thư mục `node_modules` có Playwright nếu gói này nằm ở môi
trường riêng. `AXE_PATH` trỏ tới `axe.min.js` để bật kiểm tra WCAG tự động, `ARTIFACT_DIR`
để lưu ảnh chụp màn hình.

Kết quả đã chạy trên Chrome headless:

- 6 trang × 4 breakpoint (320 / 768 / 1024 / 1440): không tràn ngang, đúng một `h1`,
  không trùng ID, heading không nhảy cấp, ảnh có `alt` và tải được, Poppins tải được.
- Bộ lọc linh kiện: 9 / 2 / 2 / 3 / 2 với `aria-pressed` đúng.
- Tìm kiếm có kết quả và trạng thái rỗng; liên kết chọn mẫu giữ đúng mô hình qua query string.
- Cả ba mẫu: preview đúng, tiến độ 0 → 100 → bỏ tick trở lại chưa đủ.
- Ba kiểm tra chuyển động: `.button` có transition `transform`, nhấc lên khi hover,
  `transition-duration` về đúng `0s` khi bật `prefers-reduced-motion`.
- Tab tới skip link, Enter đưa focus vào `main`.
- **axe-core 4.10.3: 0 violation** ở nhóm WCAG 2 A/AA và 2.1 AA tại 320px và 1440px.

Kiểm tra thủ công bổ sung:

- Tắt JavaScript trên cả sáu trang: không phần tử nào trong `main` bị ẩn, không tràn
  ngang, heading còn nguyên. Trang Mẫu robot và Lắp ráp có `<noscript>` nói rõ phần nào cần JavaScript.
- `prefers-reduced-motion: reduce`: `transition-duration` = `0s`, `animation-duration` =
  `0.01ms`, `scroll-behavior` = `auto`, cờ `reveal-ready` không được bật.
- Thu gọn / mở sidebar bằng chuột và bàn phím: `aria-expanded` đổi đúng, trạng thái được
  nhớ sau khi chuyển trang.
- Kiểm tra tương phản thủ công cho chữ đặt trên ảnh — axe chỉ trả về `incomplete` với
  `background-image` nên không thay thế được bước này.

### Nguồn tài nguyên

- [Heroicons 2.2.0](https://github.com/tailwindlabs/heroicons/tree/v2.2.0) — MIT, giấy phép tại `assets/icons/LICENSE.txt`.
- [Poppins](https://fonts.google.com/specimen/Poppins) — SIL OFL, giấy phép tại `assets/fonts/OFL.txt`.
- Ảnh nền — xem `assets/images/backgrounds/CREDITS.md`.
- Ảnh robot và linh kiện — tài nguyên sẵn có của nhóm.

Website chạy offline hoàn toàn: không CDN, không build step, không phụ thuộc npm khi chạy.

---

## Phase 1 — Nền tảng industrial workspace

Tham chiếu ban đầu: bảng điều khiển công nghiệp, thẻ kính với ảnh lớn, nền graphite với
điểm nhấn cam và lưới kỹ thuật.

Các quyết định của Phase 1 vẫn còn hiệu lực ở Phase 2:

- Giữ HTML/CSS/JavaScript thuần. Quy tắc LogiRoute/Tailwind trong `../.agents/AGENTS.md`
  thuộc dự án khác và không áp dụng cho dự án này; chỉ những nguyên tắc chung
  (không dữ liệu giả, tách data/presentation/logic, mobile-first, tương phản cao,
  có phương án dự phòng) được áp dụng.
- Poppins lưu local bốn weight 400/500/600/700; SVG Heroicons dùng inline, giới hạn kích
  thước và `aria-hidden`.
- Giữ nguyên `assets/js/data.js` về mặt ID ba mẫu robot, các selector tìm kiếm / lọc /
  checklist và công thức tiến độ.
- Mỗi trang một `h1`, tiêu đề và mô tả riêng, `main` và điều hướng có tên rõ ràng.
- Skip link tới `main`; focus bàn phím hiển thị; bộ lọc có `aria-pressed`; tiến độ thông
  báo qua `role="status"`; ảnh có `alt`, `width`/`height` và lazy-loading.
- FAQ trang chủ có nội dung hiển thị trùng với JSON-LD `FAQPage`. Đây là mô tả nội dung,
  không phải cam kết được công cụ tìm kiếm hiển thị rich results.
- Không dùng dữ liệu giám sát máy móc, đánh giá, giá bán hay tính năng 2D chưa tồn tại.

## Phase 3 — Layout dùng chung của Tài (2026-09-12)

Giữ thiết kế Phase 2 hiện tại. Bổ sung thanh vị trí cuộn, chỉ báo hover/focus
sidebar và reveal hiện tức thì khi nhận focus. Giảm chuyển động được xử lý cả
khi người dùng thay đổi tùy chọn trong phiên. Chi tiết và kết quả kiểm tra ở
[MOTION.md](MOTION.md).

Không tạo xưởng kéo-thả vì đó là phần của Tuấn Anh; nội dung và dữ liệu thuộc Nhi.
Trong lượt này chỉ sửa ui.js, layer motion/reduced-motion của style.css và tài
liệu. Các file HTML, data.js, main.js, home.js, components-filter.js và test
được giữ nguyên so với đầu lượt, bao gồm thay đổi Phase 2 chưa commit sẵn có.
