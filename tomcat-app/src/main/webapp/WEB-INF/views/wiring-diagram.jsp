<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="wiring-diagram" tabindex="0" aria-label="Sơ đồ có thể cuộn ngang; danh sách chân ở form bên dưới">
    <svg viewBox="0 0 1000 1000" role="group" aria-label="Sơ đồ nối dây 2D">
    <c:forEach var="wiringConnection" items="${wiringDiagramConnections}">
        <line class="wiring-wire" data-pair="<c:out value="${wiringConnection.pairKey}"/>"
        <c:if test="${wiringDiagramEditable}">tabindex="0" role="button"</c:if>
        x1="${wiringConnection.first.x}" y1="${wiringConnection.first.y}"
        x2="${wiringConnection.second.x}" y2="${wiringConnection.second.y}" aria-label="Dây nối trong sơ đồ">
        <title><c:out value="${wiringConnection.first.label}"/> · <c:out value="${wiringConnection.second.label}"/></title>
        </line>
    </c:forEach>
    <c:forEach var="wiringTerminal" items="${wiringExercise.terminals}">
        <g class="wiring-terminal" aria-label="<c:out value="${wiringTerminal.label}"/>"
        <c:if test="${wiringDiagramEditable}">tabindex="0" role="button"</c:if>
        data-terminal-id="${wiringTerminal.id}" data-x="${wiringTerminal.x}" data-y="${wiringTerminal.y}">
        <circle cx="${wiringTerminal.x}" cy="${wiringTerminal.y}" r="24"/>
        <text x="${wiringTerminal.labelX}" y="${wiringTerminal.y + 5}" text-anchor="${wiringTerminal.labelAnchor}">
        <c:out value="${wiringTerminal.label}"/>
        </text>
        </g>
    </c:forEach>
    </svg>
</div>
