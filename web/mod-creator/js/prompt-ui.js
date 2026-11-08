// ============================================================
// prompt-ui.js — Gemini 风格交互界面驱动与表单联动渲染
// 驱动 8 大资产类型无缝切换、多步表单流、深浅色白天黑夜模式
// 严格遵守 Zero-Emoji 规范与 Google Gemini 官方设计语言
// ============================================================

(function (global) {
  'use strict';

  if (!global.GeneratorState) {
    console.error('GeneratorState not found! Ensure prompt-generator.js is loaded first.');
    return;
  }

  const UI = {
    state: global.GeneratorState,

    init() {
      this.state.init();
      this.bindGlobalEvents();
      this.renderSidebarDrafts();
      this.renderMainStage();
      this.updateMarkdownPreview();
      this.updateThemeButton();
    },

    bindGlobalEvents() {
      global.onPromptUpdated = (mdText) => {
        this.updateMarkdownPreview(mdText);
      };

      window.addEventListener('keydown', (e) => {
        if ((e.ctrlKey || e.metaKey) && e.key === 's') {
          e.preventDefault();
          this.quickSaveDraft();
        }
      });
    },

    // 渲染中央主舞台 (8 大资产类型 + Gemini 官方居中输入框)
    renderMainStage() {
      const main = document.getElementById('mainContainer');
      if (!main) return;

      const isAdv = this.state.currentMode === 'advanced';
      const allTypes = global.TypeRegistry ? global.TypeRegistry.getAll() : [global.TypeBlock];

      main.innerHTML = `
        <div class="gemini-hero-container">
          <!-- 1. 中央大标题 -->
          <div class="gemini-hero-header">
            <h1 class="gemini-hero-title">需要我为你做些什么？</h1>
          </div>

          <!-- 2. 标志性居中大胶囊输入框 -->
          <div class="gemini-omni-box">
            <div class="omni-input-wrapper">
              <button class="omni-btn-add" title="查看语法与技巧提示" onclick="PromptUI.showQuickTips()">
                ${SVG_LIB.add || '+'}
              </button>
              
              <input type="text" id="omniSearchInput" 
                     placeholder="输入中文名称快速起步，或在此描述提示词意图..." 
                     value="${this.state.config.nameZh || ''}"
                     oninput="PromptUI.handleOmniInput(this.value)"
                     onkeydown="if(event.key==='Enter') PromptUI.scrollToConfig();" />
              
              <div class="omni-actions-right">
                <button class="omni-pill-model" onclick="PromptUI.toggleModeDropdown()">
                  <span class="pulse-sparkle-svg">${SVG_LIB.sparkle || ''}</span>
                  <span id="currentModelBadgeText">${isAdv ? '高级模式 工业级' : '普通模式 速填'}</span>
                  <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M7 10l5 5 5-5z"/></svg>
                </button>

                <button class="omni-btn-mic" title="清空与重置" onclick="PromptUI.clearOrReset()">
                  <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                    <path d="M12 14c1.66 0 3-1.34 3-3V5c0-1.66-1.34-3-3-3S9 3.34 9 5v6c0 1.66 1.34 3 3 3zm5.3-3c0 3-2.54 5.1-5.3 5.1S6.7 14 6.7 11H5c0 3.41 2.72 6.23 6 6.72V21h2v-3.28c3.28-.49 6-3.31 6-6.72h-1.7z"/>
                  </svg>
                </button>
              </div>
            </div>
          </div>

          <!-- 2.5 项目总规范 Skill 外部挂载栏 -->
          <div class="skill-mount-bar">
            <div class="skill-mount-left">
              <span class="skill-mount-icon">${SVG_LIB.file_code}</span>
              <div class="skill-mount-info">
                <span class="skill-mount-label">项目总规范 Skill 外部挂载</span>
                <span class="skill-mount-hint">平台环境与架构规范由该 Skill 统一管辖，已自动注入 Prompt</span>
              </div>
            </div>
            <div class="skill-mount-input-box">
              <input type="text" class="skill-mount-input" id="inputProjectSkillPath" 
                     value="${this.state.skillPath || ''}" 
                     placeholder="输入外部挂载的 SKILL.md 路径 (如 d:/xiangmu/yezi/.agents/skills/moonmod/SKILL.md)"
                     oninput="PromptUI.updateSkillPath(this.value)" />
              <button type="button" class="btn-skill-reset" title="填入默认 MoonMod 模组技能规范" 
                      onclick="PromptUI.updateSkillPath('d:/xiangmu/yezi/.agents/skills/moonmod/SKILL.md'); const el = document.getElementById('inputProjectSkillPath'); if(el) el.value='d:/xiangmu/yezi/.agents/skills/moonmod/SKILL.md';">
                <span>填入默认规范</span>
              </button>
            </div>
          </div>

          <!-- 3. 普通模式 vs 高级模式切换药丸 (Zero-Emoji) -->
          <div class="gemini-mode-selector">
            <button class="mode-pill ${!isAdv ? 'active' : ''}" onclick="PromptUI.switchMode('simple')">
              <span class="mode-icon-svg">${SVG_LIB.flash_on || ''}</span>
              <span class="mode-text">普通模式 (零基础速填)</span>
            </button>
            <button class="mode-pill ${isAdv ? 'active' : ''}" onclick="PromptUI.switchMode('advanced')">
              <span class="mode-icon-svg">${SVG_LIB.build || ''}</span>
              <span class="mode-text">高级模式 (工业级架构)</span>
            </button>
          </div>

          <!-- 4. 8 大资产类型全量选择卡片网格 -->
          <div class="type-selector-deck" id="typeSelectorDeck">
            ${allTypes.map(t => {
              const isActive = t.id === this.state.currentType;
              const iconSvg = SVG_LIB[t.iconName] || SVG_LIB.mc_block;
              return `
                <div class="type-card ${isActive ? 'active' : ''}" onclick="PromptUI.switchType('${t.id}')">
                  ${isActive ? '<div class="type-badge-active">当前启用</div>' : ''}
                  <div class="type-icon-box">
                    ${iconSvg}
                  </div>
                  <div class="type-info">
                    <h3>${t.label}</h3>
                    <p>${t.description}</p>
                  </div>
                </div>
              `;
            }).join('')}
          </div>

          <!-- 5. 混合交互面板 (动态根据当前选中的 Type 渲染) -->
          <div class="gemini-interactive-flow" id="configFlowContainer">
            ${this.renderConfigFlow()}
          </div>
        </div>

        <!-- 6. 实时 Markdown 预览抽屉 -->
        <div class="markdown-preview-drawer" id="previewDrawer">
          <div class="preview-header">
            <div class="preview-title">
              <span class="preview-sparkle">${SVG_LIB.sparkle}</span>
              <span>实时生成的 GitHub-Ready 提示词</span>
              <span class="badge-char-count" id="badgeCharCount">0 字符</span>
            </div>
            <div class="preview-actions">
              <button class="btn-preview-action" onclick="PromptUI.quickSaveDraft()" title="保存到本地草稿箱 (Ctrl+S)">
                ${SVG_LIB.archive}
                <span>存草稿</span>
              </button>
              <button class="btn-preview-action highlight" onclick="PromptUI.copyMarkdown()" title="一键复制到剪贴板">
                ${SVG_LIB.copy}
                <span id="btnCopyText">复制提示词</span>
              </button>
              <button class="btn-preview-action" onclick="PromptUI.exportMarkdownFile()" title="导出为 .md 文件">
                ${SVG_LIB.download}
                <span>导出 .md</span>
              </button>
            </div>
          </div>
          <div class="preview-body" id="markdownCodeBody">
            <pre><code id="markdownCodeContent"></code></pre>
          </div>
        </div>
      `;
    },

    // 调用当前类型的表单渲染器
    renderConfigFlow() {
      const def = this.state.getTypeDefinition();
      const isAdv = this.state.currentMode === 'advanced';
      if (def && typeof def.renderConfigFlow === 'function') {
        return def.renderConfigFlow(this.state.config, isAdv, this.state.activeStep);
      }
      return '<div class="config-section-card">未加载到当前类型的表单结构。</div>';
    },

    // 渲染左侧抽屉侧边栏 (Zero-Emoji)
    renderSidebarDrafts() {
      const aside = document.getElementById('sidebarContainer');
      if (!aside) return;

      const drafts = this.state.drafts || [];
      const currentDef = this.state.getTypeDefinition();
      const presets = currentDef && currentDef.presets ? Object.keys(currentDef.presets) : [];

      aside.innerHTML = `
        <div class="sidebar-brand-top">
          <div class="gemini-logo-mark">
            <svg viewBox="0 0 28 28" width="22" height="22" fill="none">
              <path d="M14 2C14 8.627 8.627 14 2 14C8.627 14 14 19.373 14 26C14 19.373 19.373 14 26 14C19.373 14 8.627 14 2Z" fill="url(#geminiGradAside)"/>
              <defs>
                <linearGradient id="geminiGradAside" x1="2" y1="2" x2="26" y2="26" gradientUnits="userSpaceOnUse">
                  <stop stop-color="#4285F4"/>
                  <stop offset="0.45" stop-color="#9B72CB"/>
                  <stop offset="0.85" stop-color="#D96570"/>
                  <stop offset="1" stop-color="#F28B82"/>
                </linearGradient>
              </defs>
            </svg>
          </div>
          <span class="sidebar-title">Gemini Prompt</span>
        </div>

        <div class="sidebar-action-box">
          <button class="btn-new-chat" onclick="PromptUI.resetToNew()">
            ${SVG_LIB.add}
            <span>发起新提示词创作</span>
          </button>
        </div>

        <div class="sidebar-nav-section">
          <div class="sidebar-nav-item active" onclick="PromptUI.scrollToTop()">
            ${SVG_LIB.sparkle}
            <span class="nav-label">提示词工作台</span>
          </div>
          <div class="sidebar-nav-item" onclick="PromptUI.exportAllDrafts()">
            ${SVG_LIB.download}
            <span class="nav-label">一键导出全部草稿</span>
          </div>
          <div class="sidebar-nav-item" onclick="PromptUI.showPresetPicker()">
            ${SVG_LIB.gem}
            <span class="nav-label">官方预设模板库</span>
          </div>
        </div>

        <div class="sidebar-group-title">
          <span>草稿箱 (${drafts.length})</span>
          ${drafts.length > 0 ? `<button class="btn-text-clear" onclick="PromptUI.clearDrafts()">清空</button>` : ''}
        </div>
        <div class="sidebar-scrollable-list">
          ${drafts.length === 0 ? `
            <div class="sidebar-empty-state">
              <span>暂无历史草稿</span>
              <p>填写并点击“存草稿”将自动在此常驻记录</p>
            </div>
          ` : drafts.map(d => `
            <div class="draft-item-card" onclick="PromptUI.loadDraft('${d.id}')">
              <div class="draft-title-row">
                <span class="draft-icon-svg">${SVG_LIB.sparkle}</span>
                <span class="draft-title" title="${d.title}">${d.title}</span>
                <button class="draft-btn-del" onclick="event.stopPropagation(); PromptUI.deleteDraft('${d.id}')" title="删除草稿">×</button>
              </div>
              <div class="draft-meta-row">
                <span>${d.snippet || ''}</span>
              </div>
            </div>
          `).join('')}
        </div>

        ${presets.length > 0 ? `
        <div class="sidebar-group-title">
          <span>${currentDef.label} 经典预设</span>
        </div>
        <div class="preset-pill-list">
          ${presets.map(k => `
            <div class="preset-pill-item" onclick="PromptUI.loadPreset('${k}')">
              <span class="preset-icon-svg">${SVG_LIB.gem}</span>
              <span class="preset-name">${currentDef.presets[k].name.split(' ')[0]}</span>
            </div>
          `).join('')}
        </div>` : ''}

        <div class="sidebar-user-footer">
          <div class="user-avatar-circle">CN</div>
          <div class="user-info-text">
            <div class="user-name">cainiao</div>
            <div class="user-badge-pro">Pro · Vibe Coding</div>
          </div>
          <button class="user-btn-settings" title="配置与关于" onclick="PromptUI.showSettingsModal()">
            ${SVG_LIB.settings}
          </button>
        </div>
      `;
    },

    // 更新实时 Markdown 预览
    updateMarkdownPreview(text) {
      const md = text || this.state.generateMarkdown();
      const codeElem = document.getElementById('markdownCodeContent');
      const badgeCount = document.getElementById('badgeCharCount');

      if (codeElem) codeElem.textContent = md;
      if (badgeCount) badgeCount.textContent = `${md.length} 字符 · ${md.split('\n').length} 行`;
    },

    // 更新顶栏黑白白天模式按钮状态
    updateThemeButton() {
      const btn = document.getElementById('btnThemeToggle');
      if (!btn) return;
      const theme = this.state.theme;
      let icon = SVG_LIB.auto_mode;
      let label = '主题: 跟随';
      if (theme === 'dark') {
        icon = SVG_LIB.dark_mode;
        label = '主题: 深色';
      } else if (theme === 'light') {
        icon = SVG_LIB.light_mode;
        label = '主题: 白天';
      }
      btn.innerHTML = `${icon}<span>${label}</span>`;
    },

    toggleTheme() {
      const nextTheme = this.state.cycleTheme();
      this.updateThemeButton();
      this.showToast(`主题已切换为：${nextTheme === 'auto' ? '跟随浏览器系统' : nextTheme === 'dark' ? '深色暗黑' : '白天浅色'}`);
    },

    // 交互操作
    switchMode(mode) {
      this.state.setMode(mode);
      this.renderMainStage();
      this.showToast(`已切换至：${mode === 'advanced' ? '高级模式 (工业级架构)' : '普通模式 (极简速填)'}`);
    },

    switchType(typeId) {
      this.state.setType(typeId);
      this.renderMainStage();
      this.renderSidebarDrafts();
      const def = this.state.getTypeDefinition();
      this.showToast(`已选定资产类型：${def ? def.label : typeId}`);
    },

    toggleModeDropdown() {
      const nextMode = this.state.currentMode === 'simple' ? 'advanced' : 'simple';
      this.switchMode(nextMode);
    },

    updateVal(key, value) {
      this.state.updateConfig(key, value);
    },

    updateSkillPath(path) {
      this.state.setSkillPath(path);
      this.updateMarkdownPreview();
    },

    handleOmniInput(val) {
      this.state.updateConfig('nameZh', val);
      if (!this.state.config.idKey || this.state.config.idKey.startsWith('custom_')) {
        const simpleEng = val.toLowerCase().replace(/[^a-z0-9]/g, '_');
        if (simpleEng) {
          const typeKey = this.state.currentType === 'block' ? 'blockId' : 'itemId';
          this.state.updateConfig(typeKey, simpleEng);
        }
      }
    },

    syncOmniSearch(val) {
      const omni = document.getElementById('omniSearchInput');
      if (omni && omni.value !== val) omni.value = val;
    },

    goToStep(step) {
      this.state.activeStep = step;
      this.rerenderFlow();
    },

    nextStep() {
      const maxStep = this.state.currentMode === 'advanced' ? 3 : 2;
      if (this.state.activeStep < maxStep) {
        this.state.activeStep++;
        this.rerenderFlow();
      } else {
        this.scrollToPreview();
      }
    },

    prevStep() {
      if (this.state.activeStep > 1) {
        this.state.activeStep--;
        this.rerenderFlow();
      }
    },

    rerenderFlow() {
      const container = document.getElementById('configFlowContainer');
      if (container) container.innerHTML = this.renderConfigFlow();
    },

    scrollToConfig() {
      const elem = document.getElementById('configFlowContainer');
      if (elem) elem.scrollIntoView({ behavior: 'smooth' });
    },

    scrollToPreview() {
      const drawer = document.getElementById('previewDrawer');
      if (drawer) drawer.scrollIntoView({ behavior: 'smooth' });
    },

    scrollToTop() {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    },

    clearOrReset() {
      this.state.resetConfigForType(this.state.currentType);
      this.renderMainStage();
      this.showToast('已重置当前类型为初始默认值');
    },

    resetToNew() {
      this.state.activeStep = 1;
      this.state.resetConfigForType(this.state.currentType);
      this.renderMainStage();
      this.showToast('已开启全新的提示词工程');
    },

    loadPreset(key) {
      if (this.state.applyPreset(key)) {
        this.renderMainStage();
        this.showToast('已成功套用预设模板');
      }
    },

    showPresetPicker() {
      const def = this.state.getTypeDefinition();
      if (!def || !def.presets) {
        this.showToast('当前类型暂无预设');
        return;
      }
      const keys = Object.keys(def.presets);
      const listHtml = keys.map(k => {
        const p = def.presets[k];
        return `
          <div class="modal-preset-card" onclick="PromptUI.loadPreset('${k}'); PromptUI.closeModal();">
            <h4>${p.name}</h4>
            <p>${p.desc}</p>
          </div>
        `;
      }).join('');
      this.openModal(`选择 [${def.label}] 官方工程模板`, listHtml);
    },

    quickSaveDraft() {
      const draft = this.state.saveToDrafts();
      this.renderSidebarDrafts();
      this.showToast(`已保存草稿：${draft.title}`);
    },

    loadDraft(id) {
      if (this.state.loadDraft(id)) {
        this.renderMainStage();
        this.renderSidebarDrafts();
        this.showToast('已成功载入该草稿内容');
      }
    },

    deleteDraft(id) {
      this.state.deleteDraft(id);
      this.renderSidebarDrafts();
      this.showToast('已移除该草稿');
    },

    clearDrafts() {
      if (confirm('确认清空所有历史草稿？')) {
        this.state.clearAllDrafts();
        this.renderSidebarDrafts();
        this.showToast('草稿箱已清空');
      }
    },

    exportAllDrafts() {
      const allMd = this.state.exportAllDraftsAsMarkdown();
      this.downloadFile('All_Mod_Prompt_Drafts.md', allMd);
      this.showToast('已将全部草稿打包导出为 Markdown 文件');
    },

    copyMarkdown() {
      const text = this.state.generateMarkdown();
      navigator.clipboard.writeText(text).then(() => {
        const btnText = document.getElementById('btnCopyText');
        if (btnText) {
          btnText.textContent = '已复制！';
          setTimeout(() => { btnText.textContent = '复制提示词'; }, 2000);
        }
        this.showToast('已复制完整 GitHub-Ready 提示词，可直接粘贴给 Cursor / Claude Code！');
      }).catch(err => {
        console.error('Copy failed:', err);
      });
    },

    exportMarkdownFile() {
      const md = this.state.generateMarkdown();
      const idKey = this.state.config.blockId || this.state.config.itemId || this.state.config.entityId || this.state.currentType;
      const filename = `${idKey}_prompt_spec.md`;
      this.downloadFile(filename, md);
      this.showToast(`已成功下载文件：${filename}`);
    },

    downloadFile(filename, text) {
      const blob = new Blob([text], { type: 'text/markdown;charset=utf-8' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = filename;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
    },

    showToast(msg) {
      let toast = document.getElementById('geminiToast');
      if (!toast) {
        toast = document.createElement('div');
        toast.id = 'geminiToast';
        toast.className = 'gemini-toast-bubble';
        document.body.appendChild(toast);
      }
      toast.textContent = msg;
      toast.classList.add('show');
      clearTimeout(this._toastTimer);
      this._toastTimer = setTimeout(() => { toast.classList.remove('show'); }, 2500);
    },

    openModal(title, contentHtml) {
      let modal = document.getElementById('geminiModal');
      if (!modal) {
        modal = document.createElement('div');
        modal.id = 'geminiModal';
        modal.className = 'gemini-modal-overlay';
        modal.innerHTML = `
          <div class="gemini-modal-dialog">
            <div class="modal-header">
              <h3 id="modalTitle"></h3>
              <button class="btn-close-modal" onclick="PromptUI.closeModal()">×</button>
            </div>
            <div class="modal-body" id="modalBody"></div>
          </div>
        `;
        document.body.appendChild(modal);
      }
      document.getElementById('modalTitle').textContent = title;
      document.getElementById('modalBody').innerHTML = contentHtml;
      modal.classList.add('open');
    },

    closeModal() {
      const modal = document.getElementById('geminiModal');
      if (modal) modal.classList.remove('open');
    },

    showQuickTips() {
      this.openModal('快捷技巧与提示', `
        <div style="line-height:1.8; color:var(--gemini-text-secondary);">
          <p>1. <strong>8 大类型全量支持</strong>：在主舞台上方点击任意卡片（方块、物品、工具、防具、生物、机器、药水、世界生成）即可无缝切换。</p>
          <p>2. <strong>黑白昼夜自适应</strong>：点击顶栏主题按钮可在跟随系统、强制暗黑、强制浅色之间循环切换。</p>
          <p>3. <strong>零 Emoji 规范</strong>：全系统采用纯粹 Google Material 矢量矢量图标，保持专业严谨工业级风格。</p>
        </div>
      `);
    },

    showSettingsModal() {
      this.openModal('系统设置与移动端说明', `
        <div style="line-height:1.8; color:var(--gemini-text-secondary);">
          <p><strong>版本</strong>：Gemini Mod Prompt Studio v2.0 (Full 8 Types Edition)</p>
          <p><strong>离线 PWA 支持</strong>：已在浏览器注册 Service Worker 与 Manifest，手机端可直接添加到主屏幕作为原生 App 离线使用。</p>
          <p><strong>iOS / 安卓打包</strong>：可随时使用 Capacitor 或 Tauri 几分钟封装为 Xcode IPA 与 Android APK。</p>
        </div>
      `);
    },

    // ============================================================
    // 素材与视觉资源选择器核心组件 (MCreator 风格素材绑定)
    // ============================================================
    renderAssetPicker(opts) {
      const key = opts.key;
      const label = opts.label || '素材路径';
      const hint = opts.hint || '支持相对路径、工程路径或绝对路径';
      const value = opts.value || '';
      const placeholder = opts.placeholder || '如 assets/modid/textures/...';
      const defaultPath = opts.defaultPath || '';
      const accept = opts.accept || '.png,.json,.ogg,.nbt';
      const prefix = opts.prefix || '';
      const iconSvg = opts.icon === 'audio' ? (SVG_LIB.audio || SVG_LIB.folder) :
                      opts.icon === 'model' ? (SVG_LIB.file_code || SVG_LIB.folder) :
                      opts.icon === 'layers' ? (SVG_LIB.layers || SVG_LIB.folder) :
                      (SVG_LIB.image || SVG_LIB.folder);

      return `
        <div class="asset-picker-group">
          <div class="asset-picker-header">
            <div class="asset-picker-title">
              <span class="asset-picker-icon">${iconSvg}</span>
              <span class="asset-picker-label">${label}</span>
            </div>
            ${hint ? `<div class="asset-picker-hint">${hint}</div>` : ''}
          </div>
          <div class="asset-picker-input-row">
            <input type="text" class="asset-picker-input" id="asset_input_${key}"
                   value="${value}" 
                   placeholder="${placeholder}"
                   oninput="PromptUI.updateVal('${key}', this.value)" />
            <div class="asset-picker-actions">
              <button type="button" class="btn-asset-action" title="选取本地文件以提取文件名或相对路径"
                      onclick="PromptUI.triggerAssetFileSelect('${key}', '${accept}', '${prefix}')">
                ${SVG_LIB.folder_open || SVG_LIB.folder}
                <span>浏览...</span>
              </button>
              ${defaultPath ? `
              <button type="button" class="btn-asset-action primary" title="填入标准规范推荐路径"
                      onclick="PromptUI.fillStandardAssetPath('${key}', '${defaultPath}')">
                ${SVG_LIB.check || ''}
                <span>标准路径</span>
              </button>` : ''}
              ${value ? `
              <button type="button" class="btn-asset-action danger" title="清空"
                      onclick="PromptUI.updateVal('${key}', ''); PromptUI.rerenderFlow();">
                <span>清空</span>
              </button>` : ''}
            </div>
          </div>
        </div>
      `;
    },

    triggerAssetFileSelect(key, accept, prefix) {
      let fileInput = document.getElementById('globalAssetPickerInput');
      if (!fileInput) {
        fileInput = document.createElement('input');
        fileInput.type = 'file';
        fileInput.id = 'globalAssetPickerInput';
        fileInput.style.display = 'none';
        document.body.appendChild(fileInput);
      }
      fileInput.accept = accept || '.png,.json,.ogg,.nbt';
      fileInput.onchange = (e) => {
        const file = e.target.files && e.target.files[0];
        if (file) {
          const resolvedPath = prefix ? (prefix.endsWith('/') ? prefix : prefix + '/') + file.name : file.name;
          this.updateVal(key, resolvedPath);
          const inputElem = document.getElementById(`asset_input_${key}`);
          if (inputElem) inputElem.value = resolvedPath;
          this.showToast(`已选定素材文件：${file.name}`);
          this.rerenderFlow();
        }
        fileInput.value = '';
      };
      fileInput.click();
    },

    fillStandardAssetPath(key, path) {
      this.updateVal(key, path);
      const inputElem = document.getElementById(`asset_input_${key}`);
      if (inputElem) inputElem.value = path;
      this.showToast(`已填入标准规范路径：${path}`);
      this.rerenderFlow();
    }
  };

  global.toggleSidebar = function () {
    const aside = document.getElementById('sidebarContainer');
    if (aside) aside.classList.toggle('collapsed');
  };

  global.PromptUI = UI;

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => UI.init());
  } else {
    UI.init();
  }
})(window);
