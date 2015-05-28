var scriptEl = document.createElement("script");
scriptEl.setAttribute("type", "text/javascript");
scriptEl.setAttribute("src", "cnotebook.js");
scriptEl.addEventListener("load",
			  function() { goog.require("comportex_notebook.bridge");});
document.head.appendChild(scriptEl);
