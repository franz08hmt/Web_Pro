"use strict";

/* Dữ liệu tĩnh của Robot Assembly Lab.
   Thông số kỹ thuật lấy theo datasheet phổ biến của từng linh kiện.
   Nơi giá trị thay đổi theo nhà sản xuất thì ghi khoảng giá trị thông dụng. */

window.COMPONENTS_DATA = [
  {
    id: "arduino-uno",
    name: "Mạch Arduino Uno R3",
    category: "Mạch điều khiển",
    image: "../assets/images/components/arduino-uno.png",
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
    image: "../assets/images/components/ultrasonic.png",
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
    image: "../assets/images/components/servo-motor.png",
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
    image: "../assets/images/components/l298n.png",
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
    image: "../assets/images/components/chassis-2wd.jpg",
    description: "Bộ khung hai tầng dùng để gá động cơ, bánh xe, bo mạch và nguồn pin.",
    specs: {
      "Vật liệu": "khung kim loại và chi tiết nhựa",
      "Kích thước tham khảo": "khoảng 156 × 103 mm khi lắp bánh",
      "Cấu hình": "2 động cơ dẫn động, 2 bánh xe và 1 bánh tự do",
      "Lắp đặt": "các lỗ bắt vít dành cho bo mạch và nguồn"
    }
  },
  {
    id: "arm-frame",
    name: "Bộ khung cánh tay",
    category: "Cơ khí",
    image: "../assets/images/components/arm-frame.jpg",
    description: "Cụm khung tay robot nhiều khớp dùng để gá cơ cấu truyền động và bộ kẹp.",
    specs: {
      "Cấu trúc": "đế xoay và các liên kết nhiều khớp",
      "Vai trò": "đỡ động cơ, truyền lực và định vị đầu kẹp",
      "Lắp đặt": "siết vít tại từng khớp theo thứ tự lắp ráp",
      "Lưu ý": "kiểm tra hành trình khớp trước khi cấp nguồn"
    }
  },
  {
    id: "caster-wheel",
    name: "Bánh tự do",
    category: "Cơ khí",
    image: "../assets/images/components/caster-wheel.jpg",
    description: "Bánh bi tự do đỡ đầu còn lại của khung xe và giúp robot đổi hướng linh hoạt.",
    specs: {
      "Cơ cấu": "bi cầu xoay tự do",
      "Vai trò": "đỡ tải và giữ cân bằng cho khung xe 2 bánh",
      "Lắp đặt": "bắt vít vào mặt đáy khung",
      "Lưu ý": "chọn chiều cao tương thích với bánh chủ động"
    }
  },
  {
    id: "mini-gripper",
    name: "Bộ kẹp mini",
    category: "Cơ khí",
    image: "../assets/images/components/mini-gripper.jpg",
    description: "Bộ kẹp hai ngón dùng ở đầu cánh tay để giữ các vật thể nhỏ.",
    specs: {
      "Cơ cấu": "hai ngón kẹp truyền động bằng bánh răng",
      "Điều khiển": "kết hợp động cơ servo phù hợp",
      "Vật liệu tham khảo": "nhựa in 3D",
      "Lưu ý": "giới hạn lực kẹp để tránh kẹt cơ cấu"
    }
  },
  {
    id: "dc-motor",
    name: "Động cơ DC TT & Bánh xe",
    category: "Động cơ",
    image: "../assets/images/components/motor-dc.png",
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
    image: "../assets/images/components/ir-sensor.png",
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
    image: "../assets/images/components/battery-holder.png",
    description: "Cung cấp nguồn DC 6V độc lập cho mạch công suất và động cơ.",
    specs: {
      "Cấu hình": "4 viên AA mắc nối tiếp",
      "Điện áp danh định": "6V (4 × 1.5V)",
      "Ngõ ra": "dây đỏ (+) và dây đen (−)",
      "Ghi chú": "cấp riêng cho mạch công suất, nối chung GND với Arduino"
    }
  },
  {
    id: "power-5v",
    name: "Nguồn 5V phù hợp",
    category: "Nguồn điện",
    image: "../assets/images/components/power-5v.jpg",
    description: "Module hạ áp DC-DC XL4015 dùng tạo nguồn 5V ổn định cho mạch logic hoặc servo.",
    specs: {
      "Loại": "bộ chuyển đổi hạ áp DC-DC XL4015",
      "Đầu ra": "điều chỉnh về 5V trước khi nối tải",
      "Ứng dụng": "cấp nguồn logic hoặc servo từ nguồn DC phù hợp",
      "Lưu ý": "kiểm tra dải điện áp đầu vào của đúng module; đo đầu ra và nối chung GND"
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
      {
        id: "chassis-2wd",
        name: "Khung xe 2 bánh",
        quantity: 1
      },
      {
        id: "arduino-uno",
        name: "Arduino Uno",
        quantity: 1
      },
      {
        id: "dc-motor",
        name: "Động cơ DC",
        quantity: 2
      },
      {
        id: "wheel",
        name: "Bánh xe",
        quantity: 2
      },
      {
        id: "caster-wheel",
        name: "Bánh tự do",
        quantity: 1
      },
      {
        id: "line-sensor",
        name: "Cảm biến dò line",
        quantity: 2
      },
      {
        id: "l298n",
        name: "Module L298N",
        quantity: 1
      },
      {
        id: "battery-holder",
        name: "Hộp pin",
        quantity: 1
      }
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
      { id: "chassis-2wd", name: "Khung xe 2 bánh", quantity: 1 },
      { id: "arduino-uno", name: "Arduino Uno", quantity: 1 },
      { id: "dc-motor", name: "Động cơ DC", quantity: 2 },
      { id: "wheel", name: "Bánh xe", quantity: 2 },
      { id: "caster-wheel", name: "Bánh tự do", quantity: 1 },
      { id: "hc-sr04", name: "Cảm biến siêu âm HC-SR04", quantity: 1 },
      { id: "l298n", name: "Module L298N", quantity: 1 },
      { id: "battery-holder", name: "Hộp pin", quantity: 1 }
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
      { id: "arm-frame", name: "Bộ khung cánh tay", quantity: 1 },
      { id: "arduino-uno", name: "Arduino Uno", quantity: 1 },
      { id: "sg90", name: "Động cơ servo", quantity: 4 },
      { id: "mini-gripper", name: "Bộ kẹp mini", quantity: 1 },
      { id: "power-5v", name: "Nguồn 5V phù hợp", quantity: 1 }
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
  },

  {
    id: "line-obstacle",
    name: "Robot dò line kết hợp tránh vật cản",
    level: "Trung bình",
    summary: "Robot bám vạch bằng hai cảm biến hồng ngoại; khi HC-SR04 phát hiện chướng ngại trong ngưỡng, robot dừng, né vật cản rồi tìm lại vạch.",
    image: "../assets/images/robots/robot-line-obstacle.png",
    buildTime: "90 – 120 phút",
    mainSensor: "Cảm biến dò line hồng ngoại và HC-SR04",
    skills: "Kết hợp tín hiệu số và đo khoảng cách; ưu tiên sự kiện, điều khiển PWM và tìm lại quỹ đạo",
    parts: [
      { id: "chassis-2wd", name: "Khung xe 2 bánh", quantity: 1 },
      { id: "arduino-uno", name: "Arduino Uno", quantity: 1 },
      { id: "dc-motor", name: "Động cơ DC", quantity: 2 },
      { id: "wheel", name: "Bánh xe", quantity: 2 },
      { id: "caster-wheel", name: "Bánh tự do", quantity: 1 },
      { id: "line-sensor", name: "Cảm biến dò line", quantity: 2 },
      { id: "hc-sr04", name: "Cảm biến siêu âm HC-SR04", quantity: 1 },
      { id: "l298n", name: "Module L298N", quantity: 1 },
      { id: "battery-holder", name: "Hộp pin", quantity: 1 }
    ],
    steps: [
      "Gắn hai động cơ, bánh xe và bánh tự do vào khung xe.",
      "Cố định Arduino Uno và L298N, chừa khoảng trống phía trước cho cảm biến.",
      "Gắn hai cảm biến dò line ở mặt dưới phía trước và HC-SR04 hướng về phía trước.",
      "Đấu L298N vào D5–D10, cảm biến line vào D2/D3, HC-SR04 vào D11/D12; nối chung GND.",
      "Nạp chương trình ưu tiên dừng/né khi vật cản gần, sau đó dò lại vạch; thử ở tốc độ thấp."
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
      { pin: "D11", target: "HC-SR04 — TRIG", note: "Ngõ ra phát xung đo khoảng cách" },
      { pin: "D12", target: "HC-SR04 — ECHO", note: "Ngõ vào đo độ rộng xung phản hồi" },
      { pin: "5V", target: "VCC hai cảm biến line và HC-SR04", note: "Nguồn logic theo thông số module" },
      { pin: "Hộp pin +", target: "L298N — Vs", note: "Nguồn động cơ; không đưa vào chân 5V Arduino" },
      { pin: "GND", target: "Arduino, cảm biến, L298N và cực âm hộp pin", note: "Bắt buộc nối chung mass" }
    ]
  },

  {
    id: "servo-scout",
    name: "Robot quét hướng tránh vật cản",
    level: "Trung bình",
    summary: "Servo SG90 quét HC-SR04 qua nhiều góc để so sánh khoảng trống, sau đó Arduino điều khiển xe né vật cản. Bản lắp thực tế cần thêm nguồn bàn DC 9V đủ dòng (không nằm trong bộ linh kiện tối thiểu); hộp pin 4AA 6V chỉ cấp phần động cơ.",
    image: "../assets/images/robots/robot-servo-scout.png",
    buildTime: "100 – 130 phút",
    mainSensor: "HC-SR04 gắn trên servo SG90",
    skills: "Đo thời gian truyền–nhận xung siêu âm, tạo PWM cho servo và chọn hướng theo số đo",
    parts: [
      { id: "chassis-2wd", name: "Khung xe 2 bánh", quantity: 1 },
      { id: "arduino-uno", name: "Arduino Uno", quantity: 1 },
      { id: "dc-motor", name: "Động cơ DC", quantity: 2 },
      { id: "wheel", name: "Bánh xe", quantity: 2 },
      { id: "caster-wheel", name: "Bánh tự do", quantity: 1 },
      { id: "hc-sr04", name: "Cảm biến siêu âm HC-SR04", quantity: 1 },
      { id: "sg90", name: "Động cơ servo SG90", quantity: 1 },
      { id: "l298n", name: "Module L298N", quantity: 1 },
      { id: "battery-holder", name: "Hộp pin", quantity: 1 },
      { id: "power-5v", name: "Module nguồn DC-DC 5V", quantity: 1 }
    ],
    steps: [
      "Lắp hai động cơ, bánh xe và bánh tự do vào khung xe.",
      "Cố định Arduino, L298N, hộp pin và module DC-DC ở vị trí chắc chắn.",
      "Gắn servo SG90 ở đầu xe, lắp giá đỡ HC-SR04 lên càng servo và hướng hai mắt cảm biến ra trước.",
      "Đấu L298N vào D5–D10, servo vào D3, TRIG/ECHO vào D11/D12. Bản lắp thực tế cần nguồn bàn DC 9V đủ dòng cấp cho VIN/DC jack Arduino và XL4015; hộp pin 4AA 6V chỉ cấp L298N Vs. Đo XL4015 đúng 5.0V rồi mới nối riêng SG90; cấp HC-SR04 và logic L298N từ Arduino 5V, kiểm tra jumper theo đúng module và nối chung GND.",
      "Nạp chương trình quét các góc trái/giữa/phải, chờ servo ổn định rồi đo; thử né vật cản ở tốc độ thấp."
    ],
    wiring: [
      { pin: "D5", target: "L298N — ENA", note: "PWM chỉnh tốc độ bánh trái" },
      { pin: "D6", target: "L298N — IN1", note: "Chọn chiều bánh trái" },
      { pin: "D7", target: "L298N — IN2", note: "Chọn chiều bánh trái" },
      { pin: "D8", target: "L298N — IN3", note: "Chọn chiều bánh phải" },
      { pin: "D9", target: "L298N — IN4", note: "Chọn chiều bánh phải" },
      { pin: "D10", target: "L298N — ENB", note: "PWM chỉnh tốc độ bánh phải" },
      { pin: "D3", target: "SG90 — Signal", note: "PWM điều khiển góc quét; giới hạn cơ khí 0–180°" },
      { pin: "D11", target: "HC-SR04 — TRIG", note: "Ngõ ra phát xung đo khoảng cách" },
      { pin: "D12", target: "HC-SR04 — ECHO", note: "Ngõ vào đo độ rộng xung phản hồi" },
      { pin: "Nguồn bàn DC 9V", target: "Arduino — VIN/DC jack và XL4015 — IN+", note: "Dải giao nhau theo datasheet: XL4015 8–36V, Uno 7–12V; cần nguồn đủ dòng, không có trong bộ linh kiện tối thiểu" },
      { pin: "5V XL4015", target: "SG90 — VCC", note: "Đo đúng 5.0V; chỉ cấp servo, không nối vào chân 5V Arduino" },
      { pin: "Arduino 5V", target: "HC-SR04 — VCC và L298N — logic 5V", note: "Không nối song song với nguồn logic 5V khác; cấu hình jumper 5V-EN theo đúng module L298N" },
      { pin: "Hộp pin +", target: "L298N — Vs", note: "Nguồn 6V riêng cho động cơ; không đưa vào Arduino hoặc đầu vào XL4015" },
      { pin: "GND", target: "Arduino, SG90, HC-SR04, L298N, DC-DC và cực âm hộp pin", note: "Bắt buộc nối chung mass" }
    ]
  }
];
