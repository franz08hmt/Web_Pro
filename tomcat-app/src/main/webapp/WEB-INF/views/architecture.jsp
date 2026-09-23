<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Luồng Servlet/JSP | Robot Assembly Lab</title>
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.55; max-width: 900px; margin: 3rem auto; padding: 0 1.5rem; }
        code { background: #eef2f5; padding: .15rem .35rem; border-radius: .25rem; }
        .flow { background: #10212f; color: #e8f4ff; border-radius: .5rem; padding: 1.25rem; white-space: pre-wrap; }
    </style>
</head>
<body>
<h1>Luồng Servlet/JSP của Robot Assembly Lab</h1>
<p>Trang này do <code>${controllerName}</code> đặt dữ liệu vào request rồi
   <code>forward()</code> sang JSP. JSP chỉ hiển thị, không truy vấn MySQL.</p>
<pre class="flow">Browser (view: HTML/JSP)
  → ${controllerName} (controller: servlet)
  → JavaBean trong package business (model)
  → ${databaseLayer} (data access layer)
  → MySQL

JSON contract: ${apiContract}</pre>
<p><a href="${pageContext.request.contextPath}/api/health">Mở response JSON health</a></p>
</body>
</html>
