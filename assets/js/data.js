"use strict";

window.ROBOT_MODELS = [
  {
    id: "line-follower",
    name: "Robot dò đường",
    level: "Cơ bản",
    summary: "Robot hai bánh sử dụng cảm biến hồng ngoại để bám theo vạch màu trên mặt đường.",
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

