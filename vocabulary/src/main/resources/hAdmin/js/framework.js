/**
 * Created by lvtu on 2017/8/1.
 */

/**
 * 绑定实际的Html view,对ViewStack的类数组操作会同步修改实际的Html view.
 *
 * @constructor
 */
function ViewStack() {
    var numPerRow;
    var count;
}

var e = ViewStack.prototype = {};

e.config = function (config) {

};

e.push = function (viewHtml) {
    var numPerRow = 3;
    var count = count;

    if (count / numPerRow != 0) {
        //在最后一行添加view
        var _$lastRow = $("#table-01 .row:last-child");
        var _$lastColumn = _$lastRow.find(".column:last-child");
    } else {
        //添加新的一行
        //再添加view
    }
};
