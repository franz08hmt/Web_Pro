# ERD - Robot Lab

```mermaid
erDiagram

    USERS {
        BIGINT_UNSIGNED id PK
        VARCHAR email UK
        VARCHAR display_name
        VARCHAR password_hash
        ENUM role
        INT_UNSIGNED session_version
        TIMESTAMP created_at
    }

    ROBOTS {
        VARCHAR id PK
        VARCHAR name
        ENUM level
        TEXT summary
        VARCHAR image
        VARCHAR build_time
        VARCHAR main_sensor
        TEXT skills
        JSON wiring
    }

    COMPONENTS {
        VARCHAR id PK
        VARCHAR name
        VARCHAR category
        VARCHAR image
        TEXT description
        JSON specs
    }

    ROBOT_COMPONENTS {
        VARCHAR robot_id PK, FK
        VARCHAR component_id PK, FK
        INT quantity
    }

    ASSEMBLY_STEPS {
        VARCHAR id PK
        VARCHAR robot_id FK
        INT step_order
        VARCHAR title
        TEXT instruction
        JSON illustration
    }

    ASSEMBLY_SESSIONS {
        BIGINT_UNSIGNED id PK
        BIGINT_UNSIGNED user_id FK
        VARCHAR robot_id FK
        ENUM status
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    SESSION_COMPONENTS {
        BIGINT_UNSIGNED session_id PK, FK
        VARCHAR robot_id FK
        VARCHAR component_id PK, FK
        BOOLEAN prepared
        TIMESTAMP updated_at
    }

    SESSION_STEPS {
        BIGINT_UNSIGNED session_id PK, FK
        VARCHAR robot_id FK
        VARCHAR step_id PK, FK
        BOOLEAN completed
        JSON simulation_state
        TIMESTAMP updated_at
    }

    SESSION_VISUAL_PARTS {
        BIGINT_UNSIGNED session_id PK, FK
        VARCHAR robot_id FK
        VARCHAR component_id PK, FK
    }

    LIBRARY_RESOURCES {
        VARCHAR id PK
        VARCHAR robot_id FK
        VARCHAR title
        ENUM type
        VARCHAR url
        TEXT description
    }

    SCHEMA_MIGRATIONS {
        VARCHAR version PK
        TIMESTAMP applied_at
    }

    USERS ||--o{ ASSEMBLY_SESSIONS : creates

    ROBOTS ||--o{ ASSEMBLY_SESSIONS : has

    ROBOTS ||--o{ ASSEMBLY_STEPS : has

    ROBOTS ||--o{ ROBOT_COMPONENTS : requires

    COMPONENTS ||--o{ ROBOT_COMPONENTS : belongs_to

    ASSEMBLY_SESSIONS ||--o{ SESSION_COMPONENTS : tracks

    ROBOT_COMPONENTS ||--o{ SESSION_COMPONENTS : validates

    ASSEMBLY_SESSIONS ||--o{ SESSION_STEPS : tracks

    ASSEMBLY_STEPS ||--o{ SESSION_STEPS : contains

    ASSEMBLY_SESSIONS ||--o{ SESSION_VISUAL_PARTS : displays

    ROBOT_COMPONENTS ||--o{ SESSION_VISUAL_PARTS : validates

    ROBOTS o|--o{ LIBRARY_RESOURCES : has
```

Khóa ngoại kép: `(session_id, robot_id)` tham chiếu `assembly_sessions(id, robot_id)`;
`session_components(robot_id, component_id)` tham chiếu `robot_components`;
`session_steps(robot_id, step_id)` tham chiếu `assembly_steps(robot_id, id)`;
`session_visual_parts(robot_id, component_id)` tham chiếu `robot_components`.
`robot_components` dùng khóa chính ghép `(robot_id, component_id)` để biểu diễn
quan hệ nhiều-nhiều và lưu số lượng cần thiết. `assembly_steps` duy nhất thứ tự
bước trong từng robot. `library_resources.robot_id` được phép NULL để lưu tài
liệu dùng chung. Các khóa BIGINT là UNSIGNED.

`HttpSession`/cookie `JSESSIONID` là trạng thái runtime do Tomcat quản lý, không
phải bảng database. Bảng `users` lưu tài khoản, password hash và role; tiến độ
được lưu theo từng phiên lắp ráp trong `assembly_sessions` cùng các bảng con.
`schema_migrations` ghi phiên bản cấu trúc đã áp dụng. Đối chiếu chi tiết từng
cột và constraint tại `database/schema.sql` và các file trong
`database/migrations/`; các lớp `XxxDB` là nơi chạy SQL tương ứng.
