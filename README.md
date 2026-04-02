# ComposeYdWeather

易得天气 Jetpack Compose 版本，基于[和风天气 API](https://dev.qweather.com/) 开发的 Android 天气应用。

<!-- 如有截图可放在此处
<p align="center">
  <img src="screenshots/home.png" width="240" />
  <img src="screenshots/city.png" width="240" />
  <img src="screenshots/detail.png" width="240" />
</p>
-->

## 功能特性

- **实时天气** — 当前温度、天气状况、天气图标
- **逐小时预报** — 未来 24 小时天气趋势
- **多日预报** — 多日气温折线图
- **空气质量** — AQI 指数及详情查询
- **生活指数** — 紫外线、体感温度、风向、日出日落、气压、能见度等
- **气象预警** — 恶劣天气实时告警
- **观测数据** — 8 类气象观测项，支持独立图表展示
- **城市搜索** — 中文 / 拼音搜索，热门国内外城市快速选择
- **多城市管理** — 最多 20 个城市，左右滑动切换，快照卡片预览
- **卡片排序** — 自定义天气信息卡片显示顺序
- **iOS 风格弹性滚动** — 过度滚动回弹动效
- **Backdrop 模糊** — 毛玻璃视觉效果
- **定位城市** — 自动获取当地天气

## 技术栈

| 分类 | 库 | 版本 |
|------|-----|------|
| UI | Jetpack Compose + Material3 | BOM 2026.02.00 |
| 导航 | Navigation Compose（Type-Safe Routes） | 2.9.7 |
| 依赖注入 | Hilt + KSP | 2.57.2 |
| 数据库 | Room | 2.8.4 |
| 键值存储 | MMKV | 2.3.0 |
| 网络 | Retrofit + OkHttp + Kotlinx Serialization | 3.0.0 / 5.3.2 |
| 图片 | Coil | 3.4.0 |
| 异步 | Kotlin Coroutines + Flow | — |
| 语言 | Kotlin | 2.2.21 |

## 环境要求

- Android Studio Ladybug 及以上
- Min SDK 24（Android 7.0）
- Target / Compile SDK 36
- JDK 11

## 快速开始

1. **克隆项目**

```bash
git clone https://github.com/pangyu646182805/ComposeYdWeather.git
```

2. **配置和风天气 API Key**

   前往 [和风天气开发者平台](https://dev.qweather.com/) 注册并获取 API Key，然后填入项目中对应的网络请求配置。

3. **运行**

   直接运行 `app` 模块即可，签名配置已内置。

## 项目结构

```
app/src/main/java/com/yd/weather/
├── app/            # AppState、ViewState、Hilt Module
├── component/      # 通用 Compose 组件（弹性滚动、搜索栏、缩放动画等）
├── config/         # 常量、主题偏好设置
├── db/             # Room 数据库（CityData、SimpleWeatherData）
├── dialog/         # 弹窗组件（城市选择器等）
├── launch/         # 启动页
├── main/           # 主页
├── manager/        # 主题偏好管理
├── model/          # API 数据模型
├── navigation/     # AppNavHost、导航事件、转场动画
├── net/            # Retrofit 网络层（Service、Repository、DataSource）
├── res/            # 设计系统（Color、Icon、Theme、Type）
├── routes/         # 类型安全路由定义
├── selectcity/     # 城市选择页
├── utils/          # 工具类（Toast、Log、MMKV、运行时数据）
├── viewmodel/      # ViewModel 层
└── weatherpreview/ # 天气预览页
```

## 架构

采用单模块 **MVVM** 架构，DataSource → Repository → ViewModel → UI 单向数据流。

```
┌─────────────────────────────────┐
│         UI (Compose)            │
└──────────────┬──────────────────┘
               │
┌──────────────▼──────────────────┐
│      ViewModel (Hilt)           │
└──────┬───────────────┬──────────┘
       │               │
┌──────▼──────┐ ┌──────▼──────────┐
│  WeatherRepo│ │ WeatherDbRepo   │
│  (Retrofit) │ │ (Room)          │
└──────┬──────┘ └──────┬──────────┘
       │               │
┌──────▼──────┐ ┌──────▼──────────┐
│  和风天气API │ │  SQLite         │
└─────────────┘ └─────────────────┘
```

## License

[Apache License 2.0](LICENSE)
