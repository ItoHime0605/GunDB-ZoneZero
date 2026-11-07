# GunDB-ZoneZero｜零號禁區槍械維修台

Minecraft **1.21.1 / NeoForge**，以 **GunDB 2.2.2** 為基礎的非官方維修台與槍匠系統修改專案。

> **狀態：開發中，尚未產出已編譯、已驗證的 JAR。** 不要直接用原始碼 ZIP 取代 mods 裡的 JAR。

## 開發目標
- 取代 GunDB 原有鐵砧修槍功能，改為獨立三格維修台與像素工業風 GUI。
- 螺絲起子配方：鐵錠 ×1、瓶蓋 ×1、木棍 ×1。
- 各槍種固定消耗螺絲起子：一般手槍 4、衝鋒槍／步槍／霰彈槍／狙擊槍／DMR 5、機槍 6、火箭發射器 7。
- 特殊槍枝：Rhino 357、Taurus 943、Taurus 500 為 4；Springfield 1873 為 5；FN Evolys、Minigun 為 6；Lone Trail 為 7。
- 槍匠 Lv.0–3；每級維修需求減少 1 個螺絲起子。
- 修復前耐久度低於 50% 時，成功維修獲得槍匠經驗 5 點；升級需要 100／150／200 點。
- 維修台 GUI 左上角顯示玩家 Skin 頭像和槍匠等級。
- 保留 GunDB 既有卡彈和耐久系統、台灣繁體中文。

## 原始專案與授權
- 作者 GunDB 2.2.2 / 1.21.1 原始碼：<https://github.com/Corrinedev/GunDurability-1.21.1>
- GunDB 發佈頁：<https://www.curseforge.com/minecraft/mc-mods/tacz-durability>
- TaCZ 原始碼：<https://github.com/MCModderAnchor/TACZ>

GunDB 原始碼的 `gradle.properties` 與 CurseForge 標示 MIT；Modrinth 頁面則標示 GPL-3.0-only。公開發布修改版 JAR 前，需要釐清這項授權差異並保留原作者署名。**此倉庫不宣稱本專案是 GunDB 官方版本。**

## 後續建置
正在整合先前 v0.7.1 維修台原始碼與 GitHub Actions；程式碼尚未完成遷入，也尚未完成編譯及遊戲實測。
