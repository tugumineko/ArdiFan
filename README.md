## ArdiFan

![ArdiFan](https://cdn.jsdelivr.net/gh/tugumineko/picture2@latest//ArdiFan.bmp)

根据华东师范大学《创客实践》课程期末考查方案相关要求，ArdiFan是基于Arduino Nano 3开发板的智能软硬件系统，旨在实现风扇的智能控制，本项目为本人作业上传。

ArdiFan名称Ardi取自Arduino、Remote、Detection、Intelligent的缩写，并且简要概括了该智能软硬件系统的核心功能。Fan取自项目类型，即电动风扇。

本项目包含三个组成部分：ArdiFan为主体硬件，ArdiFan.ino为arduino上运行的系统程序，AndroidClient是蓝牙控制Android平台的app工程源文件。

本项目包含超声波、按钮与蓝牙三种控制方式：当超声波检测到距离小于40cm时，自动启动风扇；距离大于40cm并持续3秒后，风扇自动关闭；按钮控制需要在超声波检测距离小于40cm的情况下方可操作。此外，蓝牙模块可通过手机APP远程控制风扇的开闭，进一步提升使用便捷性。本系统采用直流电机和继电器进行简单、低成本的风扇操作，且具备节能、远程控制等优势，适用于公共交通和车载服务等场景。

pic文件夹存放了一些功能示意图以及演示视频，src里存放有Arduino代码文件和Android Studio工程文件以及相关UI。

使用器材：

Arduino Nano 3     			--1 

Bluetooth Wireless HC-06	     	  	    	--1 

Distance sensor HC-SR04 --1

R3000C DC motor --1

Button switch				    --1

1 Channel 5v Relay Module		    	--1 

AAA Battery Holders     --1

Breadboard half-size		--2

AAA Battery				  --2 

导线若干

连线方式如下图：
![ardifan模拟连线](https://cdn.jsdelivr.net/gh/tugumineko/picture2@latest//ardifan模拟连线.png)

ArdiFan功能包括：

超声波自动控制风扇： 当超声波传感器检测到距离小于40cm时，风扇自动开启；当距离大于40cm并持续3秒时，风扇自动关闭。

按钮手动控制： 当超声波检测到距离小于40cm时，用户可通过按钮对风扇进行手动开启或关闭。

蓝牙远程控制： 通过蓝牙模块与手机APP连接，用户可以在任何距离下远程控制风扇的总开闭状态。

节能管理： 通过智能检测和控制逻辑，减少风扇不必要的运作，节约能源消耗。

Android Client功能包括：
![](https://cdn.jsdelivr.net/gh/tugumineko/picture2@latest//Screenshot_20240904_184101_com_example_heart_Main.jpg)
距离检测和风扇状态显示： Android客户端的最上方两行分别显示当前超声波检测到的距离（单位：cm）和风扇的运行状态（on或off）。

风扇控制： 在客户端的中间区域，提供两个按钮，分别用于手动控制风扇的开闭状态。

蓝牙连接管理： 客户端的最下方两行用于蓝牙功能，其中一个按钮用于连接蓝牙模块，另一个按钮用于断开连接。

项目运行流程图如下：
![运行流程图](https://cdn.jsdelivr.net/gh/tugumineko/picture2@latest//%E8%BF%90%E8%A1%8C%E6%B5%81%E7%A8%8B%E5%9B%BE.png)
