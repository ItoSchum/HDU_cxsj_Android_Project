# HDU CXSJ Android App: BusRoute
> 智能公交移动应用平台

- **Reference:** <http://a.amap.com/lbs/static/unzip/Android_Map_Doc/index.html>

## 1. App Funtion
	
1. 公共交通线路查询，了解公交运行路线
2. 公共交通站点查询，自动定位周边站点，显示周边站点所有公交线路
及到站距离
3. 公共交通换乘查询，实时查询全面快捷的乘车方案
4. 公共交通实时车辆状态查询，一目了然车辆行驶状态，到达位置，拥
挤程度等
5. 公交车辆到站手机提前闹铃提醒，到站短信息
6. 公交车辆乘坐预约，在线
7. 一卡通余额查询，在线充值，在线购物等
  
## 2. Development Environment

- **Java & IDE**
	- Android Studio 3.1.3
		- *JRE:* 1.8.0_152-release-1024-b01 x86_64
		- *JVM:* OpenJDK 64-Bit Server VM 

- **Build Envirnoment**
	- *Gradle Ver. :* 4.4
	- *API Platform Ver. :* 27 & 17

- **Third-party Lib**
	- *高德开放平台：*<http://lbs.amap.com/api/android-sdk/>

## 3. App Design
### 3.1 Overall

- **Sequence**
	![](/Users/shenito/Desktop/Screen Shot 2018-06-11 at 08.50.22.png
)

- **Overall Directory**
	![](/Users/shenito/Desktop/7E83203F-7AB3-46A9-825F-AE556501D850.png)

### 3.2 MAIN View

- activity_main.xml
	![](/Users/shenito/Desktop/99572E33-C306-4B6C-8975-94A57728E131.png)
- activity_busroute.xml
	![](/Users/shenito/Desktop/E3DA81EE-47B0-49EC-BA76-FDC4D62633C8.png)

### 3.3 PAY View

- activity_payment.xml
	![](/Users/shenito/Desktop/6D87122D-7AC3-4E3D-AEDD-A09C03E95EC8.png)  


## 4. Improvement Orientation
- 多线路显示
- 多优先级排序选项
- PAY View 
	- 调用 WebView 实现不用跳转应用即可充值
	- （或）使用 Alipay SDK 

