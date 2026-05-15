# Piggy Bank (宝箱猪)

[English](#english) | [中文](#中文)

---

# **English**

## Introduction

Adds the adorable Piggy Bank from Minecraft Dungeons to your game!

## Features

**Unique Behavior**
- Attracted by gold ingots
- Runs away quickly when attacked
- Drops emeralds when hurt (more damage = more emeralds)
- Drops raw or cooked porkchops on death (depending on cause of death)
- Extra random loot system, can drop any item in the game

## Configuration

### Loot Settings
- Emerald drop limits and damage ratios
- Raw/cooked porkchop min/max drop counts
- Extra loot enable/disable
- Loot type count and stack size ranges
- Whitelist/Blacklist item IDs (supports regex)

### Spawn Settings
- Enable/disable natural spawning
- Spawn weight (lower value = rarer)
- Min/max spawn count per group
- Only spawns in Plains, Forest, and Swamp biomes

## Blacklist Examples
- Single item: `minecraft:barrier`
- Entire mod: `modid:.*` (e.g., `create:.*`)
- Regex pattern: `.*spawn_egg.*` (matches all spawn eggs)

---

# **中文**

## 简介

为游戏添加来自我的世界地下城的可爱宝箱猪！

## 特性

**独特行为**
- 被金锭吸引
- 受到攻击时快速逃跑
- 受伤时掉落绿宝石（伤害越高掉落越多）
- 死亡时根据死因掉落生猪肉或熟猪排
- 额外随机战利品系统，可掉落游戏中任何物品

## 配置说明

### 掉落物设置
- 绿宝石掉落上限和伤害比例
- 生/熟猪排最小/最大掉落数量
- 额外战利品启用/禁用
- 战利品种类数量和堆叠大小范围
- 白名单/黑名单物品 ID（支持正则表达式）

### 生成设置
- 启用/禁用自然生成
- 生成权重（数值越小越稀有）
- 每次生成的最小/最大数量
- 只在群系为平原/森林和沼泽中生成

## 黑名单示例
- 单个物品：`minecraft:barrier`
- 整个模组：`modid:.*`（如 `create:.*`）
- 正则表达式：`.*spawn_egg.*`（匹配所有刷怪蛋）