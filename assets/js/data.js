"use strict";

/* Dữ liệu tĩnh của Robot Assembly Lab.
   Thông số kỹ thuật lấy theo datasheet phổ biến của từng linh kiện.
   Nơi giá trị thay đổi theo nhà sản xuất thì ghi khoảng giá trị thông dụng. */

window.COMPONENTS_DATA = [
  {
    id: "arduino-uno",
    name: "Mạch Arduino Uno R3",
    category: "Mạch điều khiển",
    image: "../assets/images/assembly/arduino-uno.png",
    description: "Bo mạch vi điều khiển trung tâm xử lý tín hiệu và điều khiển robot.",
    specs: {
      "Vi điều khiển": "ATmega328P",
      "Điện áp logic": "5V",
      "Điện áp cấp ngoài": "7 – 12V qua chân VIN",
      "Chân số": "14 chân, trong đó 6 chân PWM",
      "Chân analog": "6 chân (A0 – A5)",
      "Bộ nhớ chương trình": "32 KB Flash",
      "Tần số": "16 MHz"
    }
  },
  {
    id: "hc-sr04",
    name: "Cảm biến siêu âm HC-SR04",
    category: "Cảm biến",
    image: "../assets/images/assembly/hc-sr04.png",
    description: "Đo khoảng cách tới vật cản giúp robot né tránh chướng ngại vật.",
    specs: {
      "Điện áp hoạt động": "5V DC",
      "Dòng tiêu thụ": "khoảng 15 mA",
      "Tầm đo": "2 – 400 cm",
      "Sai số": "khoảng ± 3 mm",
      "Góc đo": "khoảng 15°",
      "Chân kết nối": "VCC, TRIG, ECHO, GND"
    }
  },
  {
    id: "sg90",
    name: "Động cơ Servo SG90",
    category: "Động cơ",
    image: "../assets/images/assembly/sg90.png",
    description: "Động cơ góc quay nhỏ gọn dùng để xoay mắt cảm biến.",
    specs: {
      "Điện áp hoạt động": "4.8 – 6V DC",
      "Góc quay": "0 – 180°",
      "Mô-men xoắn": "khoảng 1.8 kg·cm ở 4.8V",
      "Tín hiệu điều khiển": "PWM chu kỳ 20 ms (50 Hz)",
      "Khối lượng": "khoảng 9 g",
      "Chân kết nối": "VCC (đỏ), GND (nâu), Signal (cam)"
    }
  },
  {
    id: "l298n",
    name: "Mạch điều khiển động cơ L298N",
    category: "Mạch công suất",
    image: "../assets/images/assembly/l298n.png",
    description: "Driver điều khiển hướng quay và tốc độ của động cơ DC.",
    specs: {
      "Điện áp cấp động cơ": "5 – 35V DC",
      "Điện áp logic": "5V",
      "Dòng mỗi kênh": "2A liên tục, đỉnh khoảng 3A",
      "Số kênh": "2 cầu H, điều khiển 2 động cơ DC",
      "Chân điều khiển": "IN1 – IN4 chọn chiều, ENA/ENB nhận PWM",
      "Tản nhiệt": "có sẵn, cần thoáng khi tải nặng"
    }
  },
  {
    id: "chassis-2wd",
    name: "Khung xe 2 bánh",
    category: "Cơ khí",
    image: "../assets/images/assembly/chassis-2wd.png",
    description: "Khung Mica chịu lực gá lắp động cơ, bo mạch và nguồn pin.",
    specs: {
      "Vật liệu": "Mica (acrylic) dày 3 – 5 mm",
      "Kích thước tấm": "khoảng 220 × 150 mm",
      "Cấu hình": "2 động cơ dẫn động, 1 bánh tự do",
      "Lỗ bắt vít": "chuẩn cho Arduino Uno và L298N"
    }
  },
  {
    id: "dc-motor",
    name: "Động cơ DC TT & Bánh xe",
    category: "Động cơ",
    image: "../assets/images/assembly/dc-motor.png",
    description: "Động cơ giảm tốc cung cấp lực truyền động quay bánh xe robot.",
    specs: {
      "Điện áp hoạt động": "3 – 6V DC",
      "Tỉ số truyền": "1 : 48",
      "Tốc độ": "khoảng 200 vòng/phút ở 6V",
      "Dòng không tải": "khoảng 150 mA",
      "Đường kính bánh xe": "khoảng 65 mm"
    }
  },
  {
    id: "line-sensor",
    name: "Cảm biến dò line hồng ngoại",
    category: "Cảm biến",
    image: "../assets/images/assembly/line-sensor.png",
    description: "Phát hiện vạch màu đen/trắng giúp robot bám quỹ đạo di chuyển.",
    specs: {
      "Linh kiện cảm biến": "TCRT5000 hồng ngoại phản xạ",
      "Điện áp hoạt động": "3.3 – 5V DC",
      "Khoảng cách đọc": "khoảng 2 – 15 mm",
      "Tín hiệu ra": "mức số 0/1, có thêm ngõ analog",
      "Hiệu chỉnh": "biến trở chỉnh ngưỡng trên mạch"
    }
  },
  {
    id: "battery-holder",
    name: "Hộp pin AA 4 cell",
    category: "Nguồn điện",
    image: "../assets/images/assembly/battery-holder.png",
    description: "Cung cấp nguồn DC 6V độc lập cho mạch công suất và động cơ.",
    specs: {
      "Cấu hình": "4 viên AA mắc nối tiếp",
      "Điện áp danh định": "6V (4 × 1.5V)",
      "Ngõ ra": "dây đỏ (+) và dây đen (−)",
      "Ghi chú": "cấp riêng cho mạch công suất, nối chung GND với Arduino"
    }
  },
  {
    id: "wheel",
    name: "Bánh xe robot",
    category: "Cơ khí",
    image: "../assets/images/components/wheel.png",
    description: "Bánh xe gắn vào trục động cơ để truyền chuyển động xuống mặt sàn.",
    specs: {
      "Đường kính": "khoảng 65 mm",
      "Bề rộng": "khoảng 26 mm",
      "Lỗ trục": "trục chữ D, khoảng 5.4 mm",
      "Vật liệu": "vành nhựa, lốp cao su",
      "Ghi chú": "lắp trực tiếp lên động cơ DC TT"
    }
  },
  {
    id: "jumper-wire",
    name: "Dây nối Jumper",
    category: "Kết nối",
    image: "../assets/images/components/jumper-wire.png",
    description: "Dây cắm nhanh dùng để nối Arduino với cảm biến và module điều khiển.",
    specs: {
      "Chiều dài phổ biến": "10 cm và 20 cm",
      "Tiết diện lõi": "khoảng 24 AWG",
      "Đầu nối": "Dupont bước 2.54 mm",
      "Loại": "đực–đực, đực–cái, cái–cái",
      "Ghi chú": "phân màu để tránh nhầm VCC, GND và chân tín hiệu"
    }
  }
];

window.ROBOT_MODELS = [
  {
    id: "line-follower",
    name: "Robot dò đường",
    level: "Cơ bản",
    summary: "Robot hai bánh sử dụng cảm biến hồng ngoại để bám theo vạch màu trên mặt đường.",
    image: "../assets/images/robots/robot-do-line.png",
    buildTime: "60 – 90 phút",
    mainSensor: "Cảm biến dò line hồng ngoại",
    skills: "Đọc tín hiệu số, điều khiển động cơ bằng PWM",
    parts: [
      { name: "Khung xe 2 bánh", quantity: 1 },
      { name: "Arduino Uno", quantity: 1 },
      { name: "Động cơ DC", quantity: 2 },
      { name: "Bánh xe", quantity: 2 },
      { name: "Bánh tự do", quantity: 1 },
      { name: "Cảm biến dò line", quantity: 2 },
      { name: "Module L298N", quantity: 1 },
      { name: "Hộp pin", quantity: 1 }
    ],
    steps: [
      "Gắn hai động cơ và bánh xe vào khung.",
      "Cố định Arduino và module L298N lên khung.",
      "Gắn hai cảm biến dò line ở phía trước.",
      "Kết nối động cơ, cảm biến và nguồn theo sơ đồ.",
      "Nạp chương trình và kiểm tra khả năng bám vạch."
    ],
    wiring: [
      { pin: "D5", target: "L298N — ENA", note: "PWM chỉnh tốc độ bánh trái" },
      { pin: "D6", target: "L298N — IN1", note: "Chọn chiều bánh trái" },
      { pin: "D7", target: "L298N — IN2", note: "Chọn chiều bánh trái" },
      { pin: "D8", target: "L298N — IN3", note: "Chọn chiều bánh phải" },
      { pin: "D9", target: "L298N — IN4", note: "Chọn chiều bánh phải" },
      { pin: "D10", target: "L298N — ENB", note: "PWM chỉnh tốc độ bánh phải" },
      { pin: "D2", target: "Cảm biến dò line trái — OUT", note: "Ngõ vào số" },
      { pin: "D3", target: "Cảm biến dò line phải — OUT", note: "Ngõ vào số" },
      { pin: "5V", target: "VCC hai cảm biến dò line", note: "Nguồn logic" },
      { pin: "GND", target: "GND cảm biến, L298N và hộp pin", note: "Bắt buộc nối chung mass" }
    ]
  },
  {
    id: "obstacle-avoider",
    name: "Robot tránh vật cản",
    level: "Cơ bản",
    summary: "Robot sử dụng cảm biến siêu âm để phát hiện và đổi hướng khi gặp vật cản.",
    image: "../assets/images/robots/robot-tranh-vat-can.png",
    buildTime: "75 – 100 phút",
    mainSensor: "Cảm biến siêu âm HC-SR04",
    skills: "Đo khoảng cách bằng xung, xử lý rẽ nhánh theo điều kiện",
    parts: [
      { name: "Khung xe 2 bánh", quantity: 1 },
      { name: "Arduino Uno", quantity: 1 },
      { name: "Động cơ DC", quantity: 2 },
      { name: "Bánh xe", quantity: 2 },
      { name: "Bánh tự do", quantity: 1 },
      { name: "Cảm biến siêu âm HC-SR04", quantity: 1 },
      { name: "Module L298N", quantity: 1 },
      { name: "Hộp pin", quantity: 1 }
    ],
    steps: [
      "Lắp động cơ, bánh xe và bánh tự do vào khung.",
      "Cố định Arduino và module điều khiển động cơ.",
      "Gắn cảm biến siêu âm ở phía trước robot.",
      "Đấu nối nguồn, động cơ và chân tín hiệu cảm biến.",
      "Nạp chương trình và thử nghiệm khoảng cách phát hiện."
    ],
    wiring: [
      { pin: "D5", target: "L298N — ENA", note: "PWM chỉnh tốc độ bánh trái" },
      { pin: "D6", target: "L298N — IN1", note: "Chọn chiều bánh trái" },
      { pin: "D7", target: "L298N — IN2", note: "Chọn chiều bánh trái" },
      { pin: "D8", target: "L298N — IN3", note: "Chọn chiều bánh phải" },
      { pin: "D9", target: "L298N — IN4", note: "Chọn chiều bánh phải" },
      { pin: "D10", target: "L298N — ENB", note: "PWM chỉnh tốc độ bánh phải" },
      { pin: "D11", target: "HC-SR04 — TRIG", note: "Ngõ ra, phát xung 10 µs" },
      { pin: "D12", target: "HC-SR04 — ECHO", note: "Ngõ vào, đo độ rộng xung" },
      { pin: "5V", target: "HC-SR04 — VCC", note: "Nguồn logic" },
      { pin: "GND", target: "GND cảm biến, L298N và hộp pin", note: "Bắt buộc nối chung mass" }
    ]
  },
  {
    id: "mini-arm",
    name: "Cánh tay robot mini",
    level: "Trung bình",
    summary: "Mô hình cánh tay nhiều khớp sử dụng động cơ servo để thực hiện thao tác gắp đơn giản.",
    image: "../assets/images/robots/robot-arm-mini.png",
    buildTime: "110 – 150 phút",
    mainSensor: "Không dùng cảm biến, điều khiển theo góc đặt trước",
    skills: "Điều khiển servo theo góc, phối hợp nhiều khớp",
    parts: [
      { name: "Bộ khung cánh tay", quantity: 1 },
      { name: "Arduino Uno", quantity: 1 },
      { name: "Động cơ servo", quantity: 4 },
      { name: "Bộ kẹp mini", quantity: 1 },
      { name: "Nguồn 5V phù hợp", quantity: 1 }
    ],
    steps: [
      "Lắp đế và các khớp của cánh tay.",
      "Cố định servo vào đúng vị trí từng khớp.",
      "Gắn bộ kẹp vào khớp cuối.",
      "Kết nối servo với Arduino và nguồn ngoài.",
      "Nạp chương trình và hiệu chỉnh góc quay."
    ],
    wiring: [
      { pin: "D3", target: "Servo khớp đế — Signal", note: "Chân PWM, xoay trái/phải" },
      { pin: "D5", target: "Servo khớp vai — Signal", note: "Chân PWM, nâng/hạ" },
      { pin: "D6", target: "Servo khớp khuỷu — Signal", note: "Chân PWM, gập/duỗi" },
      { pin: "D9", target: "Servo bộ kẹp — Signal", note: "Chân PWM, đóng/mở kẹp" },
      { pin: "5V ngoài", target: "VCC của cả bốn servo", note: "Không lấy 5V từ Arduino vì dòng không đủ" },
      { pin: "GND", target: "GND servo và GND Arduino", note: "Bắt buộc nối chung mass" }
    ]
  }
];
