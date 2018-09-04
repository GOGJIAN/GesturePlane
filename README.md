# GesturePlane
手势解锁的View，无逻辑处理，只包含显示相关
# 效果展示
<img src="https://github.com/GOGJIAN/GesturePlane/blob/master/demo.gif" width="300">

可自定义显示的颜色和线的粗细  
自定义密码的密度，默认为3*3，大小会根据密度自动调整  
只包含View相关的显示操作，方便使用MVP架构，需要带有逻辑判断的可参考[_GestureCipher_](https://github.com/GOGJIAN/GestureCipher)  

# 使用方法
## 添加依赖  
```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
 ```
```
dependencies {
	        implementation 'com.github.GOGJIAN:GesturePlane:v1.0'
	}
```
## 使用
### 在xml中添加组件
```
<com.jianjian.gestureview.gesturePasswordView.GesturePlane
        android:id="@+id/gesture"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
//当长宽不同时，手势密码区将按照短边为基准居中显示
//可修改属性
        app:count="3" //密码点密度 默认3*3
        app:point_area_width="230dp" //手势密码区尺寸，默认match_parent,width和height必须相同，否则可能显示不全
        app:point_area_height="230dp"//手势密码区尺寸，默认match_parent
        app:line_width="20"//线宽，为密码圆圈的  1/X  这里X为20
        app:color_no_finger="#FFD8D8D8" //设置颜色，自定义，默认如上图
        app:color_finger_on_background=""
        app:color_finger_on_center=""
        app:color_incorrect_background=""
        app:color_incorrect_center=""
```
### 在代码中使用
```
private GesturePlane mGesturePlane;

mGesturePlane = findViewById(R.id.gesture);
        mGesturePlane.setResultListener(new GesturePlane.ResultListener() {
            @Override
            public void onResult(String result) {
                //根据结果进行相应的处理
            }
        });
        
mGesturePlane.setError();//调用该方法显示错误样式
```
