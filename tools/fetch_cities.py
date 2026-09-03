#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
生成离线市级中心点数据 app/src/main/assets/cities.json

用途：系统 Geocoder 不可用时的逆地理编码兜底（见 OfflineCityResolver.kt）。

为什么是市级而不是区县级：
  实测用区县中心点做最近邻，主城区准确率只有 5/8——市中心的区又小又密，
  中心点间距和区半径同量级，"最近的中心点"经常落到隔壁区。
  换成市级后同一批用例 17/17 全中（含临安、崇明、昆山等边界地带），
  且数据从 172KB 降到 23KB。天气数据本身是市级的，同市各区无差别。

数据源：阿里云 DataV.GeoAtlas（免费可商用），坐标系 GCJ-02。
  https://geo.datav.aliyun.com/areas_v3/bound/{adcode}_full.json

输出：[{"n": 市名, "p": 省名, "x": 经度, "y": 纬度}, ...]
  市名写法必须与天气城市库一致，否则 searchCity 匹配不上。

行政区划调整时重跑此脚本即可。
"""
import json
import os
import sys
import urllib.request
from concurrent.futures import ThreadPoolExecutor

BASE = "https://geo.datav.aliyun.com/areas_v3/bound/{}_full.json"
OUT = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "assets", "cities.json")
TIMEOUT = 30

# 台湾省补丁：DataV 不提供台湾下级区划（710000_full.json 返回 404），
# 缺了会导致在台湾定位时最近邻误匹配到福建。
# 名字取自天气城市库 cityid 101340xxx 段的写法；坐标来自 OpenStreetMap(WGS84)。
# 台湾不适用 GCJ-02 偏移，与大陆数据有约 500m 的制式差异，远小于城市间距，可忽略。
TAIWAN_CITIES = [
    {"n": "云林", "p": "台湾省", "x": 120.5246, "y": 23.6991},
    {"n": "南投", "p": "台湾省", "x": 120.6909, "y": 23.9023},
    {"n": "台东", "p": "台湾省", "x": 121.1506, "y": 22.7554},
    {"n": "台中", "p": "台湾省", "x": 120.6448, "y": 24.1586},
    {"n": "台北", "p": "台湾省", "x": 121.4453, "y": 25.0351},
    {"n": "嘉义", "p": "台湾省", "x": 120.4004, "y": 23.4883},
    {"n": "宜兰", "p": "台湾省", "x": 121.7631, "y": 24.7303},
    {"n": "屏东", "p": "台湾省", "x": 120.4879, "y": 22.6828},
    {"n": "彰化", "p": "台湾省", "x": 120.5445, "y": 24.0756},
    {"n": "新竹", "p": "台湾省", "x": 121.0273, "y": 24.8199},
    {"n": "桃园", "p": "台湾省", "x": 121.301, "y": 24.993},
    {"n": "花莲", "p": "台湾省", "x": 121.6197, "y": 23.9913},
    {"n": "苗栗", "p": "台湾省", "x": 120.8205, "y": 24.5648},
    {"n": "高雄", "p": "台湾省", "x": 120.3438, "y": 22.6306},
]

# DataV 用全称，天气城市库用简称的少数条目
NAME_FIXES = {
    "香港特别行政区": "香港",
    "澳门特别行政区": "澳门",
}


def fetch(adcode):
    for attempt in range(3):
        try:
            req = urllib.request.Request(BASE.format(adcode), headers={"User-Agent": "Mozilla/5.0"})
            with urllib.request.urlopen(req, timeout=TIMEOUT) as r:
                return json.loads(r.read().decode("utf-8"))
        except Exception as e:
            if attempt == 2:
                print(f"  !! 拉取失败 {adcode}: {e}", file=sys.stderr)
    return None


def named_features(data):
    if not data:
        return []
    return [f for f in data.get("features", []) if f.get("properties", {}).get("name")]


def handle_province(pf):
    p = pf["properties"]
    pname = p["name"]
    if pname == "台湾省":
        # 台湾下钻会 404，由 TAIWAN_CITIES 补丁覆盖。
        # 若不跳过，会走下面的"省即是市"分支塞进一条 name="台湾省" 的条目，
        # 它的中心点紧挨台北(121.509 vs 121.445)，会抢走台北的最近邻匹配，
        # 而城市库里没有叫"台湾省"的城市，命中即定位失败。
        return []
    subs = named_features(fetch(p["adcode"]))
    levels = {f["properties"].get("level") for f in subs}
    out = []
    if "city" in levels:
        for f in subs:
            q = f["properties"]
            if q.get("center"):
                out.append({"n": q["name"], "p": pname,
                            "x": round(q["center"][0], 4), "y": round(q["center"][1], 4)})
    elif p.get("center"):
        # 直辖市 / 特别行政区：省本身就是"市"这一级
        out.append({"n": pname, "p": pname,
                    "x": round(p["center"][0], 4), "y": round(p["center"][1], 4)})
    return out


def main():
    print("[1/2] 拉取省级列表 ...")
    provinces = named_features(fetch(100000))
    print(f"      省级行政区 {len(provinces)} 个")

    print("[2/2] 下钻省 -> 市 ...")
    records = []
    with ThreadPoolExecutor(max_workers=8) as ex:
        for out in ex.map(handle_province, provinces):
            records.extend(out)
    print(f"      拿到 {len(records)} 个市级条目")

    records.extend(TAIWAN_CITIES)

    for r in records:
        r["n"] = NAME_FIXES.get(r["n"], r["n"])

    seen, deduped = set(), []
    for r in records:
        key = (r["p"], r["n"])
        if key not in seen:
            seen.add(key)
            deduped.append(r)
    deduped.sort(key=lambda r: (r["p"], r["n"]))

    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    with open(OUT, "w", encoding="utf-8") as f:
        json.dump(deduped, f, ensure_ascii=False, separators=(",", ":"))
    print(f"\n完成：{len(deduped)} 条 -> {os.path.normpath(OUT)}  "
          f"({os.path.getsize(OUT)/1024:.1f} KB)")


if __name__ == "__main__":
    main()
