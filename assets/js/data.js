"use strict";

window.COMPONENTS_DATA = [
  {
    id: "arduino-uno",
    name: "Mạch Arduino Uno R3",
    category: "Mạch điều khiển",
    image: "../assets/images/assembly/arduino-uno.png",
    description: "Bo mạch vi điều khiển trung tâm xử lý tín hiệu và điều khiển robot."
  },
  {
    id: "hc-sr04",
    name: "Cảm biến siêu âm HC-SR04",
    category: "Cảm biến",
    image: "../assets/images/assembly/hc-sr04.png",
    description: "Đo khoảng cách tới vật cản giúp robot né tránh chướng ngại vật."
  },
  {
    id: "sg90",
    name: "Động cơ Servo SG90",
    category: "Động cơ",
    image: "../assets/images/assembly/sg90.png", // Đảm bảo tệp sg90.png trong thư mục assembly đúng là hình Servo màu xanh dương
    description: "Động cơ góc quay nhỏ gọn dùng để xoay mắt cảm biến."
  },
  {
    id: "l298n",
    name: "Mạch điều khiển động cơ L298N",
    category: "Mạch công suất",
    image: "../assets/images/assembly/l298n.png",
    description: "Driver điều khiển hướng quay và tốc độ của động cơ DC."
  },
  {
    id: "chassis-2wd",
    name: "Khung xe 2 bánh",
    category: "Cơ khí",
    image: "../assets/images/assembly/chassis-2wd.png",
    description: "Khung Mica chịu lực gá lắp động cơ, bo mạch và nguồn pin."
  },
  {
    id: "dc-motor",
    name: "Động cơ DC TT & Bánh xe",
    category: "Động cơ",
    image: "../assets/images/assembly/dc-motor.png",
    description: "Động cơ giảm tốc cung cấp lực truyền động quay bánh xe robot."
  },
  {
    id: "line-sensor",
    name: "Cảm biến dò line hồng ngoại",
    category: "Cảm biến",
    image: "../assets/images/assembly/line-sensor.png",
    description: "Phát hiện vạch màu đen/trắng giúp robot bám quỹ đạo di chuyển."
  },
  {
    id: "battery-holder",
    name: "Hộp pin AA 4 cell",
    category: "Nguồn điện",
    image: "../assets/images/assembly/battery-holder.png",
    description: "Cung cấp nguồn DC 6V độc lập cho mạch công suất và động cơ."
  }
];

window.ROBOT_MODELS = [
  {
    id: "line-follower",
    name: "Robot dò đường",
    level: "Cơ bản",
    summary: "Robot hai bánh sử dụng cảm biến hồng ngoại để bám theo vạch màu trên mặt đường.",
    image: "../assets/images/assembly/arduino-uno.png",
    parts: [
      { name: "Khung xe 2 bánh", quantity: 1 },
      { name: "Arduino Uno", quantity: 1 },
      { name: "Động cơ DC", quantity: 2 },
      { name: "Bánh xe", quantity: 2 },
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
    ]
  },
  {
    id: "obstacle-avoider",
    name: "Robot tránh vật cản",
    level: "Cơ bản",
    summary: "Robot sử dụng cảm biến siêu âm để phát hiện và đổi hướng khi gặp vật cản.",
    image: "../assets/images/assembly/hc-sr04.png",
    parts: [
      { name: "Khung xe 2 bánh", quantity: 1 },
      { name: "Arduino Uno", quantity: 1 },
      { name: "Động cơ DC", quantity: 2 },
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
    ]
  },
  {
    id: "mini-arm",
    name: "Cánh tay robot mini",
    level: "Trung bình",
    summary: "Mô hình cánh tay nhiều khớp sử dụng động cơ servo để thực hiện thao tác gắp đơn giản.",
    image: "../assets/images/assembly/sg90.png",
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
    ]
  }
];