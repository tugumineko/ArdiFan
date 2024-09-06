#include <SoftwareSerial.h>

const int TrigPin = 2;
const int EchoPin = 3;
const int FAN_PIN = 4;
const int BUTTON_PIN = 5;

unsigned long fanOffBeginTime = 0;
unsigned long lastDebounceTime = 0;      // 上一次防抖时间
unsigned long lastButtonPressTime = 0;   // 上次按钮按下的时间
const unsigned long debounceDelay = 50;  // 防抖延迟常量

bool controlByBT = true;          // 蓝牙总控制

// 按钮操作标志
bool button2ON = false;  
bool button2OFF = false;    

SoftwareSerial BTSerial(10, 11);  // 创建软件串口

void setup() {
  pinMode(TrigPin, OUTPUT);
  pinMode(EchoPin, INPUT);
  pinMode(FAN_PIN, OUTPUT);
  pinMode(BUTTON_PIN, INPUT_PULLUP); // 启用内部上拉电阻
  Serial.begin(9600);
  BTSerial.begin(9600);
}

void loop() {
  static bool fanStatus = false;
  static int lastButtonStatus = HIGH;  // 默认状态是 HIGH，因为启用了内部上拉
  int distance = getAvgDistance();

  unsigned long currentMillis = millis();
  static unsigned long previousMillis = 0;
  const long interval = 1000;  // 设置时间间隔为100毫秒

  // 按钮模块
  int currentButtonStatus = digitalRead(BUTTON_PIN);
  if (currentButtonStatus != lastButtonStatus && millis() - lastDebounceTime > debounceDelay) {
    lastDebounceTime = millis();
    lastButtonStatus = currentButtonStatus;
    if (currentButtonStatus == LOW) {  // 按钮从 HIGH 变为 LOW
      (fanStatus ? button2OFF : button2ON) = true;
      lastButtonPressTime = millis();  // 记录按钮按下时间
    }
  }

  // 蓝牙板块
  if(BTSerial.available()){
    String state = BTSerial.readString();
    if(state == "on"){
      controlByBT = true;    // 如果接收到"on"，则 controlByBT 设置为 true
    }
    else if(state == "off"){
      controlByBT = false;   // 如果接收到"off"，则 controlByBT 设置为 false，关闭风扇
      fanStatus = false;
      button2ON = false;
      button2OFF = false;
    }
  }

  // 距离控制风扇逻辑
  if (controlByBT) {
    if (distance < 40) {
      if(button2OFF){
        fanStatus = false;
      }
      if(button2ON){
        fanStatus = true;
        button2ON = false;
        button2OFF = false;
      }
      if(!button2ON && !button2OFF){
        fanStatus = true;
      }
      fanOffBeginTime = millis();
    }
    if (millis() - fanOffBeginTime > 3000) {
      fanStatus = false;
      button2ON = false;
      button2OFF = false;
    }
  }

  digitalWrite(FAN_PIN, fanStatus ? HIGH : LOW);  // 控制风扇

  //蓝牙串口输出
  if (currentMillis - previousMillis >= interval) {
    previousMillis = currentMillis;
    BTSerial.print(distance);
    BTSerial.print(" ");
    BTSerial.println(fanStatus ? "on" : "off");
  }

}

// 防抖 多次测量求中位数
int getAvgDistance() {
  int readings[10];
  for (int i = 0; i < 10; i++) {
    digitalWrite(TrigPin, LOW);
    delayMicroseconds(5);
    digitalWrite(TrigPin, HIGH);
    delayMicroseconds(10);
    digitalWrite(TrigPin, LOW);
    readings[i] = pulseIn(EchoPin, HIGH) / 58.0;
    delay(10);  
  }
  qsort(readings, 10, sizeof(int), cmp);
  return readings[5];  // 返回中位数
}

int cmp(const void *a, const void *b) {
  return *(int*)a - *(int*)b;
}