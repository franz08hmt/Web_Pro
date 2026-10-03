"use strict";

(function () {
  const button = document.getElementById("print-profile");
  if (!button) return;
  button.addEventListener("click", function () {
    window.print();
  });
})();
