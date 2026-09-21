# ERD - Robot Lab

```mermaid
erDiagram

    USERS {
        BIGINT_UNSIGNED id PK
    }

    ROBOTS {
        VARCHAR id PK
    }

    COMPONENTS {
        VARCHAR id PK
    }

    ROBOT_COMPONENTS {
        VARCHAR robot_id PK, FK
        VARCHAR component_id PK, FK
        INT quantity
    }

    ASSEMBLY_STEPS {
        VARCHAR id PK
        VARCHAR robot_id FK
    }

    ASSEMBLY_SESSIONS {
        BIGINT_UNSIGNED id PK
        BIGINT_UNSIGNED user_id FK
        VARCHAR robot_id FK
    }

    SESSION_COMPONENTS {
        BIGINT_UNSIGNED session_id PK, FK
        VARCHAR robot_id FK
        VARCHAR component_id PK, FK
    }

    SESSION_STEPS {
        BIGINT_UNSIGNED session_id PK, FK
        VARCHAR robot_id FK
        VARCHAR step_id PK, FK
    }

    SESSION_VISUAL_PARTS {
        BIGINT_UNSIGNED session_id PK, FK
        VARCHAR robot_id FK
        VARCHAR component_id PK, FK
    }

    LIBRARY_RESOURCES {
        VARCHAR id PK
        VARCHAR robot_id FK
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
Các khóa BIGINT là UNSIGNED. Xác thực runtime dùng `HttpSession` do Tomcat quản
lý; bảng `users` chỉ lưu tài khoản, mật khẩu đã băm và role. Xem
`database/schema.sql` và `docs/ARCHITECTURE.md` để đối chiếu SQL với DAO.
