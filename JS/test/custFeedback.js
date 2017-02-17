function custFeedback() {
    var IsPC = function () {
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
    var getCata = function () {
        var reg = new RegExp("https?://.+\.com/([^/]+)/([^/]+)/([^/]+)(/[^/]+)*\.shtml", "i");
        var reg2 = new RegExp("https?://.+\.com/([^/]+)/([^/]+)\.shtml", "i");
        var r = window.location.href.match(reg);
        if (r != null) {
            var catalog = RegExp.$1 + "_" + RegExp.$2;
            return catalog;
        } else {
            r = window.location.href.match(reg2);
            if (r != null) {
                return RegExp.$1 + "";
            }
        }
        return "";
    }
    var catalog = getCata();
    $.ajax({
        type: "get",
        async: true,
        url: "/api/stat/Cust/Feedback?" + "&source=" + isPc + "&catalog=" + catalog,
        success: function () {
            // alert("cust-success");
        },
        error: function () {
            // alert("cust-fail");
        }
    });
}
custFeedback();