// ============================================================
// registry.js — 资产类型注册中心 (TypeRegistry)
// 统一管理方块、物品、工具、防具、生物、机器、药水、世界生成等全部类型定义
// 坚持绝对无 Emoji 规范，采用纯粹 Material 矢量图标映射
// ============================================================

(function (global) {
  'use strict';

  const TypeRegistry = {
    _types: {},
    _order: [],

    // 注册资产类型
    register(typeId, typeDefinition) {
      if (!typeId || !typeDefinition) return;
      this._types[typeId] = typeDefinition;
      if (!this._order.includes(typeId)) {
        this._order.push(typeId);
      }
    },

    // 获取特定资产类型
    get(typeId) {
      return this._types[typeId] || null;
    },

    // 获取全部类型列表
    getAll() {
      return this._order.map(id => this._types[id]);
    },

    // 获取全部类型 ID 列表
    getAllIds() {
      return [...this._order];
    },

    // 检查是否存在特定类型
    has(typeId) {
      return !!this._types[typeId];
    }
  };

  global.TypeRegistry = TypeRegistry;
})(window);
