/**
 * Created by lvtu on 2017/7/28.
 */


Date.prototype.format = function (fmt) {
    var o = {
        "M+": this.getMonth() + 1,                 //月份
        "d+": this.getDate(),                    //日
        "h+": this.getHours(),                   //小时
        "m+": this.getMinutes(),                 //分
        "s+": this.getSeconds(),                 //秒
        "q+": Math.floor((this.getMonth() + 3) / 3), //季度
        "S": this.getMilliseconds()             //毫秒
    };
    if (/(y+)/.test(fmt)) {
        fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    }
    for (var k in o) {
        if (new RegExp("(" + k + ")").test(fmt)) {
            fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
        }
    }
    return fmt;
};


$(document).ready(function () {
    var choosedDate = null;
    //外部js调用
    laydate({
        elem: '#date', //目标元素。由于laydate.js封装了一个轻量级的选择器引擎，因此elem还允许你传入class、tag但必须按照这种方式 '#id .class'
        event: 'focus', //响应事件。如果没有传入event，则按照默认的click
        format: 'YYYY-MM-DD',
        min: '2010-01-01', //设定最小日期为当前日期
        max: '2099-01-01', //最大日期
        istime: false,
        istoday: true,
        issure: false,
        choose: function (date) { //选择好日期的回调
            choosedDate = date;
            container.emptyView();
            container.refreshViews(choosedDate);
        }
    });
    var today = new Date().format("yyyy-MM-dd");
    $("#date").val(today);
    choosedDate = today;

    Dropzone.autoDiscover = false;


    //把请求的数据封装成word对象
    function Word(word) {
        if (word == null) {
            return;
        }
        this.date = word.date;
        this.word = word.word;
        this.image = word.image;
    }

    function Container(el) {
        var _this = this;

        var viewWrappers = [];
        viewWrappers.push($(el + " .view1"));
        viewWrappers.push($(el + " .view2"));
        viewWrappers.push($(el + " .view3"));
        viewWrappers.push($(el + " .view4"));
        viewWrappers.push($(el + " .view5"));
        viewWrappers.push($(el + " .view6"));

        var viewTpls = [];

        function fillView(index, viewTpl) {
            viewTpls[index] = viewTpl;
            viewWrappers[index].html(viewTpl.html);
            if (viewTpl instanceof DefaultViewTpl) {
                var _$currentViewWrapper = viewWrappers[index];
                var el = _$currentViewWrapper.selector;
                var myDropzone = new Dropzone(el + " .upload-word-file", {
                    url: "/upload",
                    autoProcessQueue: false,
                    dictDefaultMessage: "add file",
                    maxFiles: 1

                });
                _$currentViewWrapper.data("dropzone", myDropzone);
            }
            if (viewTpl instanceof WordViewTpl) {

            }
        }

        function emptyView() {
            $.each(viewWrappers,function (index,view) {
                view.empty();
            });
            viewTpls = [];
        }

        function MarkViewTpl() {
            var source = $("#addVocabulary-template").html();
            this.html = source;
        }

        function DefaultViewTpl() {
            var source = $("#default-vocabulary-template").html();
            this.html = source;
        }

        function WordViewTpl(word) {
            var source = $("#show-vocabulary-template").html();
            var template = Handlebars.compile(source);
            this.html = template(word);
        }

        var markViewTpl = new MarkViewTpl();
        var defaultViewTpl = new DefaultViewTpl();

        function addMark() {
            var length = viewTpls.length;
            fillView(length, markViewTpl);
        }

        function moveMark() {
            var length = viewTpls.length;
            if (length == 0) {
                fillView(length, markViewTpl);
            } else {
                fillView(length - 1, defaultViewTpl);
                fillView(length, markViewTpl);
            }
        }

        this.emptyView = emptyView;
        this.moveMark = moveMark;
        this.refreshViews = function (date) {
            $.get("/everydayWords", "date=" + date)
                .done(function (result) {
                    console.log(result);
                    var words = result.data;
                    if (words != null && words.length > 0) {
                        $.each(words, function (index, word) {
                            var w = new Word(word);
                            var wordTpl = new WordViewTpl(w);
                            fillView(index, wordTpl);
                        });
                    }
                    addMark();
                })
                .fail(function () {

                });
        }
    }

    var defaultWord = new Word();
    var container = new Container("#table-01");
    // container.moveMark();
    container.refreshViews(choosedDate);

    $("#table-01").on("click", ".extend-vocabulary", function () {
        container.moveMark();
    });

    //双击修改单词的事件
    function bindEvent1() {
        $("#table-01").on("dblclick", ".event-new-word", function () {
            var e1 = $(this).find(".show-on");
            var e2 = $(this).find(".show-off");
            e1.removeClass("show-on").addClass("show-off");
            e2.removeClass("show-off").addClass("show-on");
            $("#table-01").off("dblclick");
        });
    }

    bindEvent1();

    $("#table-01").on("click", ".event-ok", function () {
        var e1 = $(this).closest(".event-new-word").find(".show-on");
        var e2 = $(this).closest(".event-new-word").find(".show-off");
        e1.removeClass("show-on").addClass("show-off");
        e2.removeClass("show-off").addClass("show-on");
        var _$input = $(this).closest(".event-new-word").find("input");
        var _$span = $(this).closest(".event-new-word").find("span");
        var word = _$input.val();
        _$span.text(word);
        bindEvent1();

        var _$view = $(this).closest('div[class*="view"]');
        var myDropzone = _$view.data("dropzone");
        myDropzone.on("sending", function (file, xhr, formdata) {
            formdata.set("date", choosedDate);
            formdata.set("word", word);
        });
        myDropzone.processQueue();
    });

    $("#table-01").on("click", ".event-cancel", function () {
        var e1 = $(this).closest(".event-new-word").find(".show-on");
        var e2 = $(this).closest(".event-new-word").find(".show-off");
        e1.removeClass("show-on").addClass("show-off");
        e2.removeClass("show-off").addClass("show-on");
        bindEvent1();
    });

    $("#table-01").on("focusin",function () {
        $('.fancybox').fancybox({
            openEffect: 'none',
            closeEffect: 'none'
        });
    });

    $("#refresh-day-words").on("click",function () {
        container.emptyView();
        container.refreshViews(choosedDate);
    });
});