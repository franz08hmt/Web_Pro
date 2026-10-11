
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Lịch sử chẩn đoán | Robot Assembly Lab</title>

    <style>
        :root {
            color-scheme: dark;
            --bg: #0e1214;
            --surface: #161c1f;
            --surface-hover: #1d2529;
            --border: #354147;
            --text: #f4f7f8;
            --muted: #a5b0b6;
            --accent: #ff914d;
            --accent-hover: #ffab73;
        }

        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            color: var(--text);
            background: var(--bg);
            font-family: Arial, Helvetica, sans-serif;
            line-height: 1.6;
        }

        a {
            color: var(--accent);
            text-underline-offset: 4px;
        }

        a:hover {
            color: var(--accent-hover);
        }

        a:focus-visible {
            outline: 3px solid var(--accent);
            outline-offset: 4px;
            border-radius: 4px;
        }

        .site-header {
            background: #151b1e;
            border-bottom: 1px solid var(--border);
        }

        .nav-inner {
            width: min(1240px, 100%);
            min-height: 88px;
            margin: 0 auto;
            padding: 16px 24px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 24px;
        }

        .brand {
            display: flex;
            align-items: center;
            gap: 12px;
            color: var(--text);
            text-decoration: none;
        }

        .brand-icon {
            width: 42px;
            height: 42px;
            display: grid;
            place-items: center;
            border-radius: 12px;
            background: var(--accent);
            color: #15191b;
            font-size: 23px;
            font-weight: 800;
        }

        .brand-name {
            display: block;
            font-size: 15px;
            font-weight: 800;
            line-height: 1.3;
        }

        .brand-subtitle {
            display: block;
            margin-top: 3px;
            color: var(--muted);
            font-size: 10px;
            letter-spacing: 1.5px;
        }

        .nav-links {
            display: flex;
            flex-wrap: wrap;
            align-items: center;
            gap: 24px;
        }

        .nav-links a {
            color: #d6dfe3;
            font-size: 14px;
            text-decoration: none;
        }

        .nav-links a:hover,
        .nav-links a.active {
            color: var(--accent);
        }

        main {
            width: min(1100px, 100%);
            margin: 0 auto;
            padding: 38px 24px 56px;
        }

        .back-link {
            display: inline-block;
            margin-bottom: 20px;
            font-size: 14px;
            font-weight: 600;
            text-decoration: none;
        }

        .page-heading {
            margin-bottom: 28px;
        }

        h1 {
            margin: 0 0 10px;
            font-size: clamp(27px, 4vw, 36px);
            line-height: 1.25;
            letter-spacing: -0.7px;
        }

        h2 {
            margin: 0 0 8px;
            font-size: 21px;
        }

        p {
            margin: 0 0 12px;
        }

        .muted {
            color: var(--muted);
        }

        .page-description {
            max-width: 700px;
            color: var(--muted);
            font-size: 15px;
        }

        .summary-grid {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 16px;
            margin-bottom: 24px;
        }

        .summary-card {
            padding: 22px 24px;
            background: var(--surface);
            border: 1px solid var(--border);
            border-radius: 14px;
        }

        .summary-label {
            color: var(--muted);
            font-size: 14px;
        }

        .summary-value {
            margin-top: 6px;
            color: var(--accent);
            font-size: 30px;
            font-weight: 800;
            line-height: 1.3;
        }

        .history-card {
            overflow: hidden;
            background: var(--surface);
            border: 1px solid var(--border);
            border-radius: 14px;
        }

        .card-heading {
            padding: 22px 24px;
            border-bottom: 1px solid var(--border);
        }

        .card-heading p {
            margin: 4px 0 0;
            color: var(--muted);
            font-size: 14px;
        }

        .table-wrap {
            width: 100%;
            overflow-x: auto;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            text-align: left;
        }

        th,
        td {
            padding: 17px 20px;
            border-bottom: 1px solid #2c363b;
            vertical-align: middle;
        }

        th {
            color: var(--muted);
            background: #192024;
            font-size: 12px;
            font-weight: 700;
            letter-spacing: 0.5px;
            text-transform: uppercase;
            white-space: nowrap;
        }

        td {
            font-size: 14px;
            overflow-wrap: anywhere;
        }

        tbody tr:last-child td {
            border-bottom: 0;
        }

        tbody tr {
            transition: background 0.15s ease;
        }

        tbody tr:hover {
            background: var(--surface-hover);
        }

        .scenario-name {
            min-width: 210px;
            font-weight: 700;
        }

        .robot-name {
            min-width: 130px;
            color: var(--muted);
        }

        .score {
            color: var(--accent);
            font-weight: 800;
            white-space: nowrap;
        }

        .submitted-time {
            min-width: 150px;
            color: var(--muted);
            white-space: nowrap;
        }

        .result-link {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            padding: 8px 13px;
            border: 1px solid #805333;
            border-radius: 8px;
            background: #292019;
            color: var(--accent);
            font-size: 13px;
            font-weight: 700;
            text-decoration: none;
            white-space: nowrap;
            transition: background 0.15s ease;
        }

        .result-link:hover {
            background: #3a2a20;
            color: var(--accent-hover);
        }

        .empty {
            padding: 52px 24px;
            text-align: center;
        }

        .empty-icon {
            width: 58px;
            height: 58px;
            margin: 0 auto 18px;
            display: grid;
            place-items: center;
            border: 1px solid var(--border);
            border-radius: 16px;
            background: #20272b;
            color: var(--accent);
            font-size: 27px;
        }

        .empty p {
            max-width: 460px;
            margin: 8px auto 22px;
            color: var(--muted);
        }

        .primary-button {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            padding: 11px 18px;
            border: 1px solid var(--accent);
            border-radius: 9px;
            background: var(--accent);
            color: #171a1c;
            font-size: 14px;
            font-weight: 800;
            text-decoration: none;
            transition: background 0.15s ease;
        }

        .primary-button:hover {
            background: var(--accent-hover);
            color: #171a1c;
        }

        .pagination {
            display: flex;
            flex-wrap: wrap;
            align-items: center;
            justify-content: center;
            gap: 8px;
            padding: 22px;
            border-top: 1px solid var(--border);
        }

        .pagination a,
        .pagination span {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            min-height: 38px;
            padding: 7px 12px;
            border: 1px solid var(--border);
            border-radius: 8px;
            color: var(--text);
            font-size: 13px;
            text-decoration: none;
        }

        .pagination a:hover {
            border-color: var(--accent);
            color: var(--accent);
        }

        .pagination .current {
            border-color: var(--accent);
            background: var(--accent);
            color: #171a1c;
            font-weight: 800;
        }

        @media (max-width: 760px) {
            .nav-inner {
                align-items: flex-start;
                flex-direction: column;
                gap: 16px;
                padding: 16px;
            }

            .nav-links {
                gap: 16px;
            }

            main {
                padding: 28px 16px 40px;
            }

            .summary-grid {
                gap: 10px;
            }

            .summary-card {
                padding: 17px;
            }

            .card-heading {
                padding: 18px;
            }

            th,
            td {
                padding: 13px 14px;
            }
        }

        @media (max-width: 420px) {
            .summary-grid {
                grid-template-columns: 1fr;
            }

            .nav-links {
                gap: 12px;
            }

            .nav-links a {
                font-size: 13px;
            }

            main {
                padding-right: 12px;
                padding-left: 12px;
            }
        }

        @media (prefers-reduced-motion: reduce) {
            *,
            *::before,
            *::after {
                transition: none !important;
            }
        }
    </style>
</head>

<body>
<header class="site-header">
    <div class="nav-inner">
        <a class="brand"
           href="${pageContext.request.contextPath}/diagnosis"
           aria-label="Robot Assembly Lab - Luyện chẩn đoán">
            <span class="brand-icon" aria-hidden="true">⚙</span>
            <span>
                <span class="brand-name">ROBOT</span>
                <span class="brand-subtitle">ASSEMBLY LAB</span>
            </span>
        </a>

        <nav class="nav-links" aria-label="Điều hướng chính">
            <a href="${pageContext.request.contextPath}/pages/tai-khoan.html">Tài khoản</a>
            <a href="${pageContext.request.contextPath}/tasks">Nhiệm vụ</a>
            <a href="${pageContext.request.contextPath}/diagnosis">Luyện chẩn đoán</a>
        </nav>
    </div>
</header>

<main>
    <a class="back-link"
       href="${pageContext.request.contextPath}/diagnosis">
        ← Quay lại luyện chẩn đoán
    </a>

    <section class="page-heading">
        <h1>Lịch sử chẩn đoán</h1>
        <p class="page-description">
            Theo dõi các lượt luyện đã hoàn thành và xem lại kết quả
            chẩn đoán của bạn, từ mới nhất đến cũ nhất.
        </p>
    </section>

    <section class="summary-grid" aria-label="Thống kê lịch sử">
        <div class="summary-card">
            <div class="summary-label">Tổng số lượt đã nộp</div>
            <div class="summary-value">
                <c:out value="${historyTotalItems}"/>
            </div>
        </div>

        <div class="summary-card">
            <div class="summary-label">Trang hiện tại</div>
            <div class="summary-value">
                <c:out value="${historyPage}"/>
                <span style="color: var(--muted); font-size: 18px; font-weight: 500;">
                    / <c:out value="${historyTotalPages}"/>
                </span>
            </div>
        </div>
    </section>

    <section class="history-card">
        <div class="card-heading">
            <h2>Kết quả gần đây</h2>
            <p>Các lượt PRACTICE đã nộp được lưu tại đây.</p>
        </div>

        <c:choose>
            <c:when test="${empty diagnosisHistory}">
                <div class="empty">
                    <div class="empty-icon" aria-hidden="true">⌁</div>
                    <h2>Chưa có lịch sử chẩn đoán</h2>
                    <p>
                        Khi bạn hoàn thành và nộp một lượt luyện tập,
                        kết quả sẽ xuất hiện ở đây để bạn xem lại.
                    </p>
                    <a class="primary-button"
                       href="${pageContext.request.contextPath}/diagnosis">
                        Bắt đầu luyện tập →
                    </a>
                </div>
            </c:when>

            <c:otherwise>
                <div class="table-wrap">
                    <table>
                        <thead>
                        <tr>
                            <th scope="col">Tình huống</th>
                            <th scope="col">Mẫu robot</th>
                            <th scope="col">Điểm</th>
                            <th scope="col">Thời gian nộp</th>
                            <th scope="col">Kết quả</th>
                        </tr>
                        </thead>

                        <tbody>
                        <c:forEach var="item" items="${diagnosisHistory}">
                            <tr>
                                <td class="scenario-name">
                                    <c:out value="${item.scenarioTitle}"/>
                                </td>

                                <td class="robot-name">
                                    <c:out value="${item.robotName}"/>
                                </td>

                                <td class="score">
                                    <c:out value="${item.score}"/> / 10
                                </td>

                                <td class="submitted-time">
                                    <c:choose>
                                        <c:when test="${not empty item.submittedAt}">
                                            <fmt:formatDate
                                                    value="${item.submittedAt}"
                                                    pattern="dd/MM/yyyy HH:mm:ss"/>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="muted">Không có dữ liệu</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>

                                <td>
                                    <a class="result-link"
                                       href="${pageContext.request.contextPath}/diagnosis?action=result&amp;id=${item.id}">
                                        Xem lại →
                                    </a>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>

                <nav class="pagination" aria-label="Phân trang lịch sử">
                    <c:if test="${historyPage > 1}">
                        <a href="${pageContext.request.contextPath}/diagnosis?action=history&amp;page=${historyPage - 1}">
                            ← Trang trước
                        </a>
                    </c:if>

                    <span class="current">
                        Trang <c:out value="${historyPage}"/>
                        / <c:out value="${historyTotalPages}"/>
                    </span>

                    <c:if test="${historyPage < historyTotalPages}">
                        <a href="${pageContext.request.contextPath}/diagnosis?action=history&amp;page=${historyPage + 1}">
                            Trang sau →
                        </a>
                    </c:if>
                </nav>
            </c:otherwise>
        </c:choose>
    </section>
</main>
</body>
</html>
