// ============================================================
// prompt-generator.js — 模块化提示词生成核心引擎
// 驱动 8 大资产类型、深浅色模式、智能缺省引擎、草稿箱管理与 Markdown 输出
// 严格遵守 Zero-Emoji 规范与 Google Gemini 设计语言
// ============================================================

(function (global) {
  'use strict';

  const GeneratorState = {
    currentMode: 'simple', // 'simple' | 'advanced'
    currentType: 'block', // 8大类型之一: block, item, tool, armor, entity, block_entity, mob_effect, worldgen
    config: {},
    activeStep: 1,
    theme: 'auto', // 'auto' | 'dark' | 'light'
    skillPath: 'd:/xiangmu/yezi/.agents/skills/moonmod/SKILL.md',
    drafts: [],
    customPresets: {},
    markdownPreview: '',

    init() {
      this.initTheme();
      this.initSkillPath();
      this.loadDraftsFromStorage();
      this.loadCustomPresets();
      this.resetConfigForType(this.currentType);
      this.renderMarkdown();
    },

    initSkillPath() {
      const savedPath = localStorage.getItem('mm233_skill_path');
      if (savedPath !== null) {
        this.skillPath = savedPath;
      }
    },

    setSkillPath(path) {
      this.skillPath = path || '';
      localStorage.setItem('mm233_skill_path', this.skillPath);
      this.renderMarkdown();
    },

    // 主题控制 (Auto / Dark / Light)
    initTheme() {
      const savedTheme = localStorage.getItem('mm233_theme_mode') || 'auto';
      this.setTheme(savedTheme);

      // 监听系统主题变化
      if (window.matchMedia) {
        window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
          if (this.theme === 'auto') {
            this.applyThemeToDOM('auto');
          }
        });
      }
    },

    setTheme(theme) {
      this.theme = theme;
      localStorage.setItem('mm233_theme_mode', theme);
      this.applyThemeToDOM(theme);
    },

    applyThemeToDOM(theme) {
      const root = document.documentElement;
      if (theme === 'auto') {
        root.removeAttribute('data-theme');
      } else {
        root.setAttribute('data-theme', theme);
      }
    },

    cycleTheme() {
      const modes = ['auto', 'dark', 'light'];
      const nextIdx = (modes.indexOf(this.theme) + 1) % modes.length;
      this.setTheme(modes[nextIdx]);
      return modes[nextIdx];
    },

    // 获取当前类型模块定义
    getTypeDefinition(typeId) {
      const id = typeId || this.currentType;
      if (global.TypeRegistry && global.TypeRegistry.has(id)) {
        return global.TypeRegistry.get(id);
      }
      return global.TypeBlock || null;
    },

    // 切换模式
    setMode(mode) {
      this.currentMode = mode;
      this.renderMarkdown();
    },

    // 切换资产类型
    setType(typeId) {
      this.currentType = typeId;
      this.activeStep = 1;
      this.resetConfigForType(typeId);
      this.renderMarkdown();
    },

    // 重置配置为该类型的默认值
    resetConfigForType(typeId) {
      const def = this.getTypeDefinition(typeId);
      if (def && def.defaultConfig) {
        this.config = JSON.parse(JSON.stringify(def.defaultConfig));
      } else {
        this.config = {};
      }
    },

    updateConfig(key, value) {
      this.config[key] = value;
      this.renderMarkdown();
    },

    updateConfigBatch(obj) {
      this.config = { ...this.config, ...obj };
      this.renderMarkdown();
    },

    applyPreset(presetKey) {
      const def = this.getTypeDefinition();
      if (!def || !def.presets) return false;
      const preset = def.presets[presetKey];
      if (preset) {
        this.currentMode = preset.mode || this.currentMode;
        this.config = { ...def.defaultConfig, ...preset.data };
        this.renderMarkdown();
        return true;
      }
      return false;
    },

    // Markdown 核心生成调度
    generateMarkdown() {
      const def = this.getTypeDefinition();
      const isAdv = this.currentMode === 'advanced';
      if (def && typeof def.generateMarkdown === 'function') {
        return def.generateMarkdown(this.config, isAdv, this.skillPath);
      }
      return `# 提示词生成器\n未加载到目标类型 [${this.currentType}] 的渲染逻辑。`;
    },

    renderMarkdown() {
      this.markdownPreview = this.generateMarkdown();
      if (typeof global.onPromptUpdated === 'function') {
        global.onPromptUpdated(this.markdownPreview);
      }
      return this.markdownPreview;
    },

    // 草稿箱管理 (LocalStorage)
    saveToDrafts(customTitle) {
      const def = this.getTypeDefinition();
      const typeLabel = def ? def.label : this.currentType;
      const title = customTitle || `${this.config.nameZh || typeLabel}_${new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`;
      
      const draft = {
        id: 'draft_' + Date.now(),
        title: title,
        timestamp: Date.now(),
        dateStr: new Date().toLocaleString('zh-CN'),
        mode: this.currentMode,
        type: this.currentType,
        config: JSON.parse(JSON.stringify(this.config)),
        snippet: `${typeLabel} · ${this.currentMode === 'advanced' ? '高级模式' : '普通模式'}`
      };

      this.drafts.unshift(draft);
      if (this.drafts.length > 50) this.drafts.pop();
      this.saveDraftsToStorage();
      return draft;
    },

    loadDraft(draftId) {
      const draft = this.drafts.find(d => d.id === draftId);
      if (draft) {
        this.currentType = draft.type || 'block';
        this.currentMode = draft.mode || 'simple';
        const def = this.getTypeDefinition(this.currentType);
        this.config = { ...(def ? def.defaultConfig : {}), ...draft.config };
        this.renderMarkdown();
        return true;
      }
      return false;
    },

    deleteDraft(draftId) {
      this.drafts = this.drafts.filter(d => d.id !== draftId);
      this.saveDraftsToStorage();
    },

    clearAllDrafts() {
      this.drafts = [];
      this.saveDraftsToStorage();
    },

    exportAllDraftsAsMarkdown() {
      if (this.drafts.length === 0) {
        return this.generateMarkdown();
      }
      let combined = `# Mod Prompt Generator 提示词草稿归档\n\n`;
      combined += `> 归档生成时间：${new Date().toLocaleString('zh-CN')}\n`;
      combined += `> 总草稿数量：${this.drafts.length} 篇\n\n---\n\n`;

      this.drafts.forEach((draft, idx) => {
        combined += `## [草稿 ${idx + 1}] ${draft.title} (${draft.dateStr})\n\n`;
        const def = this.getTypeDefinition(draft.type);
        if (def && typeof def.generateMarkdown === 'function') {
          combined += def.generateMarkdown(draft.config, draft.mode === 'advanced');
        }
        combined += `\n\n---\n\n`;
      });
      return combined;
    },

    saveDraftsToStorage() {
      try {
        localStorage.setItem('mm233_prompt_drafts', JSON.stringify(this.drafts));
      } catch (e) {}
    },

    loadDraftsFromStorage() {
      try {
        const raw = localStorage.getItem('mm233_prompt_drafts');
        if (raw) this.drafts = JSON.parse(raw);
      } catch (e) {
        this.drafts = [];
      }
    },

    loadCustomPresets() {
      try {
        const raw = localStorage.getItem('mm233_custom_presets');
        if (raw) this.customPresets = JSON.parse(raw);
      } catch (e) {
        this.customPresets = {};
      }
    }
  };

  global.GeneratorState = GeneratorState;
})(window);
