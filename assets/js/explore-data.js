"use strict";

// Editorial material only. Assembly models, parts and wiring stay in data.js.
window.LAB_REFERENCES = [
  {
    id: "arduino-robot", kind: "robots", name: "Arduino Robot", category: "Robot giáo dục",
    image: "assets/images/reference/arduino-robot.webp", alt: "Arduino Robot nhìn từ trên với bo mạch tròn màu xanh",
    summary: "Nền tảng robot hai bánh của Arduino, kết hợp bo điều khiển cảm biến và bo điều khiển động cơ.",
    detail: "Hai bo mạch đảm nhận hai vai trò: đọc cảm biến, ra quyết định và điều khiển chuyển động. Đây là một ví dụ để tìm hiểu cách tổ chức phần cứng của robot di động.",
    facts: ["Hai bo mạch", "Robot di động"],
    source: "https://docs.arduino.cc/retired/getting-started-guides/Robot/",
    photoSource: "https://commons.wikimedia.org/wiki/File:Arduino_Robot_Top.jpg",
    author: "Arduino SA", license: "CC BY-SA 3.0", licenseUrl: "https://creativecommons.org/licenses/by-sa/3.0/"
  },
  {
    id: "dobot-arm", kind: "robots", name: "Cánh tay Dobot", category: "Tự động hóa",
    image: "assets/images/reference/dobot-arm.webp", alt: "Cánh tay Dobot trong mô hình thực hành với các khối màu",
    summary: "Quan sát cơ cấu gắp và cách một cánh tay robot làm việc với các vật thể trên bàn thực hành.",
    detail: "Ảnh minh họa cánh tay robot và các khối màu trong một mô hình thực hành. Tư liệu giúp đối chiếu đế, khớp, bộ gắp và vùng thao tác với cánh tay mini của nhóm.",
    facts: ["Cơ cấu gắp", "Vùng thao tác"],
    source: "https://commons.wikimedia.org/wiki/File:Dobot-_A_Robot_arm.png",
    photoSource: "https://commons.wikimedia.org/wiki/File:Dobot-_A_Robot_arm.png",
    author: "DangerAlpha", license: "CC0 1.0", licenseUrl: "https://creativecommons.org/publicdomain/zero/1.0/"
  },
  {
    id: "esp32-board", kind: "components", name: "ESP32", category: "Điều khiển & kết nối",
    image: "assets/images/reference/esp32-board.webp", alt: "Bo phát triển ESP32 với module ESP-WROOM-32",
    summary: "Vi điều khiển tích hợp Wi-Fi và Bluetooth, phù hợp để tìm hiểu truyền dữ liệu và điều khiển thiết bị không dây.",
    detail: "ESP32 mở rộng khả năng kết nối cho các dự án nhúng. Khi chọn bo phát triển, cần đối chiếu sơ đồ chân và mức điện áp của chính phiên bản đang dùng trước khi ghép cảm biến.",
    facts: ["Wi-Fi", "Bluetooth"],
    source: "https://www.espressif.com/en/products/socs/esp32",
    photoSource: "https://commons.wikimedia.org/wiki/File:ESP32_Espressif_ESP-WROOM-32_Dev_Board.jpg",
    author: "Ubahnverleih", license: "CC0 1.0", licenseUrl: "https://creativecommons.org/publicdomain/zero/1.0/"
  },
  {
    id: "raspberry-pi-pico", kind: "components", name: "Raspberry Pi Pico", category: "Vi điều khiển",
    image: "assets/images/reference/raspberry-pi-pico.webp", alt: "Raspberry Pi Pico nhìn nghiêng, có chip RP2040 và cổng micro USB",
    summary: "Bo mạch RP2040 nhỏ gọn, hỗ trợ C/C++ và MicroPython để thực hành đọc cảm biến, điều khiển và lập trình nhúng.",
    detail: "Pico thế hệ đầu dùng RP2040, có 26 GPIO đa chức năng và 2 MB Flash trên bo. Pico trong ảnh không tích hợp Wi-Fi; tính năng không dây thuộc các phiên bản Pico W.",
    facts: ["RP2040", "26 GPIO"],
    source: "https://www.raspberrypi.com/products/raspberry-pi-pico/",
    photoSource: "https://commons.wikimedia.org/wiki/File:Raspberry_pi_pico_oben.jpg",
    author: "Michael H. (Laserlicht)", license: "CC BY-SA 4.0", licenseUrl: "https://creativecommons.org/licenses/by-sa/4.0/"
  }
];
