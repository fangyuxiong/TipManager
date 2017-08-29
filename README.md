# TipManager
## 效果图
![test](pic/test_tip.gif)

## 介绍
Android Tip显示解决方案。

## 使用方法

`build.gradle`中添加:
```
dependencies {
    compile 'com.xfy.tipviewmanager:library:1.4'
}
```

代码中:
```
//在targetView周围显示tip，具体位置由triangleDirection决定
TipManager.bindActivity(activity).showTipView(targetView, tipText, triangleDirection);

//隐藏targetView上的tip
TipManager.bindActivity(activity).hideTipView(targetView);

//删除targetView上的tip
TipManager.bindActivity(activity).removeTipView(targetView);

//销毁
TipManager.unbindActivity(activity);

更多使用方法看注释，我懒
```

## 注意
请在View布局后调用显示tip
## 最后
欢迎提出意见及建议。

* email: s18810577589@sina.com