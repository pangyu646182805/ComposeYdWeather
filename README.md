# ComposeYdWeather

易得天气 Jetpack Compose 版本，基于和风天气 API 开发的 Android 天气应用。

## 功能

- 城市搜索（中文 / 拼音）与热门城市快速选择
- 定位城市自动获取当地天气
- 多城市管理
- 实时天气、逐小时预报、多日预报

## 技术栈

| 分类 | 库 |
|------|-----|
| UI | Jetpack Compose + Material3 |
| 导航 | Navigation Compose（Type-Safe Routes） |
| 依赖注入 | Hilt + KSP |
| 本地存储 | Room + MMKV |
| 网络 | Retrofit2 + OkHttp3 + Kotlinx Serialization |
| 异步 | Kotlin Coroutines + Flow |

## 环境要求

- Android Studio Ladybug 及以上
- Min SDK 24（Android 7.0）
- Target SDK 36
- JDK 11

## 快速开始

1. 克隆项目

```bash
git clone https://github.com/yourname/ComposeYdWeather.git
```

2. 在 `app/src/main/java/com/yd/weather/config/Constants.kt` 中填入和风天气 API Key

3. 直接运行 `app` 模块即可，签名配置已内置

## 项目结构

```
app/src/main/java/com/yd/weather/
├── app/            # AppState、ViewState、Hilt Module
├── component/      # 通用 Compose 组件
├── config/         # 常量、主题偏好设置
├── db/             # Room 数据库（CityData、SimpleWeatherData）
├── launch/         # 启动页
├── main/           # 主页
├── navigation/     # AppNavHost、导航事件、转场动画
├── net/            # Retrofit 网络层
├── res/            # 设计系统（Color、Icon、Theme、Type）
├── routes/         # 类型安全路由定义
├── selectcity/     # 城市选择页
├── utils/          # 工具类（Toast、Log、MMKV、定位）
└── weatherpreview/ # 天气预览页
```

## 架构

采用单模块 MVVM 架构，DataSource → Repository → ViewModel → UI 单向数据流。

```
UI (Compose)
  └── ViewModel (Hilt)
        ├── WeatherRepository   ← Retrofit / OkHttp
        └── WeatherDbRepository ← Room
```
