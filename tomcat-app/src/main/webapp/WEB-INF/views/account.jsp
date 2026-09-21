<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Tài khoản | Robot Assembly Lab</title>
    <style>
        body { font-family: Arial, sans-serif; max-width: 760px; margin: 3rem auto; padding: 0 1.5rem; line-height: 1.55; }
        article { border: 1px solid #ccd6dd; border-radius: .7rem; padding: 1.5rem; }
        dt { font-weight: 700; margin-top: .75rem; } dd { margin: .15rem 0; }
    </style>
</head>
<body>
<h1>Tài khoản thực hành</h1>
<article>
    <p>Trang được <code>AccountPageServlet</code> bảo vệ bằng <code>HttpSession</code>,
       sau đó controller đặt JavaBean <code>user</code> vào request và forward đến JSP.</p>
    <dl>
        <dt>Họ tên</dt><dd>${user.fullName}</dd>
        <dt>Email</dt><dd>${user.email}</dd>
        <dt>Vai trò</dt><dd>${user.role}</dd>
    </dl>
    <p><a href="${pageContext.request.contextPath}/index.html">Về trang chủ</a></p>
</article>
</body>
</html>
