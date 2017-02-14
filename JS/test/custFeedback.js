function custFeedback() {
    var IsPC = function() {
        var userAgentInfo = navigator.userAgent;
        var Agents = new Array("Android", "iPhone", "SymbianOS", "Windows Phone", "iPad", "iPod");
        var flag = true;
        for (var v = 0; v < Agents.length; v++) {
            if (userAgentInfo.indexOf(Agents[v]) > 0) {
                flag = false;
                break;
            }
        }
        return flag;
    }
    var isPc = IsPC() ? "PC" : "H5";
    var getCata = function() {
        var reg = new RegExp("https?://.+\.com/([^/]+)/([^/]+)/([^/]+)(/[^/]+)*\.shtml", "i");
        var reg2 = new RegExp("https?://.+\.com/([^/]+)/([^/]+)\.shtml", "i");
        var r = window.location.href.match(reg);
        if (r != null) {
            var result = RegExp.$1 + "_" + RegExp.$2 + (RegExp.$3 === "" ? "" : ("_" + RegExp.$3));
            var catalog = (RegExp.$1 + "_" + RegExp.$2).replace(RegExp.$1, "project");
            alert(result);
            return (catalog);
        } else {
            r = window.location.href.match(reg2);
            if (r != null) {
                alert(RegExp.$1);
                return RegExp.$1;
            }
        }
        return null;
    }
    var catalog = getCata();
    $.ajax({
        type: "get",
        async: false,
        url: "/api/stat/Cust/Feedback?" + "&source=" + isPc + "&catalog=" + catalog,
        success: function() {
            alert("cust-success");
        },
        error: function() {
            alert("cust-fail");
        }
    });
}
custFeedback();
