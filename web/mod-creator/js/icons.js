// ============================================================
// icons.js — Minecraft 像素体素风格 × Google Gemini 标志性渐变配色
// 100% 经典 Minecraft 像素结构 (16x16 / Isometric 3D Voxel)
// 纯正 Google Gemini 渐变色系 (#4285F4, #9B72CB, #D96570, #81C995)
// 严格遵守 Zero-Emoji 规范，矢量 SVG 极致清晰锐利
// ============================================================

const SVG_LIB = {
  // 品牌星芒 (Gemini Sparkle × MC 下界之星像素切面)
  sparkle: `<svg viewBox="0 0 24 24" width="16" height="16" fill="none">
    <defs>
      <linearGradient id="gGradSparkle" x1="2" y1="2" x2="22" y2="22" gradientUnits="userSpaceOnUse">
        <stop offset="0%" stop-color="#4285F4"/>
        <stop offset="35%" stop-color="#9B72CB"/>
        <stop offset="70%" stop-color="#D96570"/>
        <stop offset="100%" stop-color="#81C995"/>
      </linearGradient>
    </defs>
    <!-- MC 像素四尖星芒 -->
    <path d="M11 2h2v4h-2V2zM11 18h2v4h-2v-4zM2 11h4v2H2v-2zM18 11h4v2h-4v-2z
             M9 6h6v2H9V6zm0 10h6v2H9v-2zm-3-3h2v-2h2V7h4v2h2v2h2v2h-2v2h-2v2H9v-2H7v-2H6v-2z" fill="url(#gGradSparkle)"/>
    <!-- 核心高光像素 -->
    <rect x="11" y="11" width="2" height="2" fill="#FFFFFF" opacity="0.9"/>
  </svg>`,

  // 1. 方块 (Block): Minecraft 经典等轴测 3D 像素方块 (Isometric Voxel Block)
  mc_block: `<svg viewBox="0 0 24 24" width="24" height="24" fill="none">
    <defs>
      <linearGradient id="gGradBlockTop" x1="2" y1="2" x2="22" y2="10" gradientUnits="userSpaceOnUse">
        <stop offset="0%" stop-color="#78D9EC"/>
        <stop offset="100%" stop-color="#81C995"/>
      </linearGradient>
      <linearGradient id="gGradBlockLeft" x1="2" y1="8" x2="12" y2="22" gradientUnits="userSpaceOnUse">
        <stop offset="0%" stop-color="#4285F4"/>
        <stop offset="100%" stop-color="#9B72CB"/>
      </linearGradient>
      <linearGradient id="gGradBlockRight" x1="12" y1="8" x2="22" y2="22" gradientUnits="userSpaceOnUse">
        <stop offset="0%" stop-color="#6750A4"/>
        <stop offset="100%" stop-color="#302060"/>
      </linearGradient>
    </defs>
    <!-- 顶面 (Top Face) - 带像素阶梯草皮 -->
    <path d="M12 2l8 4.5-8 4.5L4 6.5 12 2z" fill="url(#gGradBlockTop)"/>
    <!-- 顶面像素网格纹理 -->
    <path d="M10 4l2 1.1-2 1.1-2-1.1L10 4zm4 2.2l2 1.1-2 1.1-2-1.1 2-1.1z" fill="#FFFFFF" opacity="0.35"/>
    <!-- 左面 (Left Face) - 蓝紫渐变侧壁 -->
    <path d="M4 6.5v9L12 20v-9L4 6.5z" fill="url(#gGradBlockLeft)"/>
    <!-- 左面像素地层纹理 -->
    <path d="M6 9.5l2 1.1v2.2L6 11.7V9.5zm2 4.5l2 1.1v2.2L8 16.2V14z" fill="#FFFFFF" opacity="0.2"/>
    <!-- 右面 (Right Face) - 暗紫阴影侧壁 -->
    <path d="M12 11v9l8-4.5v-9L12 11z" fill="url(#gGradBlockRight)"/>
    <!-- 右面像素地层纹理 -->
    <path d="M14 12.6l2-1.1v2.2l-2 1.1v-2.2zm2 4.5l2-1.1v2.2l-2 1.1v-2.2z" fill="#000000" opacity="0.25"/>
  </svg>`,

  // 2. 普通物品 (Item): Minecraft 经典 16x16 像素八面体宝石 / 钻石水晶 (Diamond Gem)
  mc_item: `<svg viewBox="0 0 24 24" width="24" height="24" fill="none">
    <defs>
      <linearGradient id="gGradItemGem" x1="4" y1="2" x2="20" y2="22" gradientUnits="userSpaceOnUse">
        <stop offset="0%" stop-color="#A8C7FA"/>
        <stop offset="45%" stop-color="#D0BCFF"/>
        <stop offset="100%" stop-color="#F28B82"/>
      </linearGradient>
      <linearGradient id="gGradItemFacet" x1="4" y1="8" x2="20" y2="16" gradientUnits="userSpaceOnUse">
        <stop offset="0%" stop-color="#FFFFFF"/>
        <stop offset="100%" stop-color="#4285F4"/>
      </linearGradient>
    </defs>
    <!-- 经典钻石像素切面外轮廓 -->
    <path d="M8 2h8v2h4v4h2v4h-2v4h-4v4h-4v2h-4v-2H4v-4H2v-4h2V8h4V4h4V2z" fill="url(#gGradItemGem)"/>
    <!-- 内部几何高光腰线与反光切面 -->
    <path d="M8 4h8v2H8V4zm-2 4h12v2H6V8zm-2 4h16v2H4v-2zm4 4h8v2H8v-2zm2 4h4v2h-4v-2z" fill="#FFFFFF" opacity="0.25"/>
    <path d="M10 8h4v4h-4V8zm-4 4h4v2H6v-2zm8 0h4v2h-4v-2z" fill="url(#gGradItemFacet)" opacity="0.45"/>
    <!-- 左上角像素反光斑 -->
    <rect x="7" y="5" width="2" height="3" fill="#FFFFFF" opacity="0.85"/>
  </svg>`,

  // 3. 武器工具 (Tool): Minecraft 经典 45° 像素采矿镐 (Pixel Mining Pickaxe)
  mc_tool: `<svg viewBox="0 0 24 24" width="24" height="24" fill="none">
    <defs>
      <linearGradient id="gGradToolHead" x1="6" y1="2" x2="22" y2="18" gradientUnits="userSpaceOnUse">
        <stop offset="0%" stop-color="#78D9EC"/>
        <stop offset="50%" stop-color="#4285F4"/>
        <stop offset="100%" stop-color="#9B72CB"/>
      </linearGradient>
      <linearGradient id="gGradToolHandle" x1="2" y1="22" x2="16" y2="8" gradientUnits="userSpaceOnUse">
        <stop offset="0%" stop-color="#D0BCFF"/>
        <stop offset="100%" stop-color="#6750A4"/>
      </linearGradient>
    </defs>
    <!-- 镐头拱形像素刃部 -->
    <path d="M14 2h7v3h-2v2h-2v2h-2v2h-2v-2h2V7h2V5h-5V2zm-2 2h3v2h-3V4zm-2 2h2v2h-2V6zm-2 2h2v2H8V8zm-2 2h2v2H6v-2z" fill="url(#gGradToolHead)"/>
    <path d="M4 12h2v2H4v-2zM2 14h2v3H2v-3z" fill="url(#gGradToolHead)"/>
    <!-- 镐柄 45° 阶梯像素手柄 -->
    <path d="M13 11l2-2 2 2-2 2-2-2zm-3 3l2-2 2 2-2 2-2-2zm-3 3l2-2 2 2-2 2-2-2zm-3 3l2-2 2 2-2 2-2-2z" fill="url(#gGradToolHandle)"/>
    <!-- 镐柄边缘像素暗边 -->
    <path d="M3 21l1-1 1 1-1 1-1-1zm3-3l1-1 1 1-1 1-1-1zm3-3l1-1 1 1-1 1-1-1zm3-3l1-1 1 1-1 1-1-1z" fill="#302060" opacity="0.6"/>
    <!-- 镐尖高光锋芒 -->
    <rect x="19" y="3" width="2" height="2" fill="#FFFFFF" opacity="0.9"/>
  </svg>`,

  // 4. 防具装备 (Armor): Minecraft 经典战甲胸铠 (Pixel Armor Chestplate)
  mc_armor: `<svg viewBox="0 0 24 24" width="24" height="24" fill="none">
    <defs>
      <linearGradient id="gGradArmor" x1="2" y1="2" x2="22" y2="22" gradientUnits="userSpaceOnUse">
        <stop offset="0%" stop-color="#D0BCFF"/>
        <stop offset="50%" stop-color="#9B72CB"/>
        <stop offset="100%" stop-color="#4285F4"/>
      </linearGradient>
    </defs>
    <!-- 经典宽肩、平领、下摆两翼像素胸甲 -->
    <path d="M4 3h5v3h6V3h5v6h-2v3h-1v8H7v-8H6V9H4V3z" fill="url(#gGradArmor)"/>
    <!-- 护甲侧板与胸口凹槽阴影 -->
    <path d="M9 3h6v3h-6V3zm-3 6h2v6H6V9zm10 0h2v6h-2V9z" fill="#302060" opacity="0.35"/>
    <!-- 腹部护板像素层次 -->
    <path d="M9 13h6v6H9v-6z" fill="#FFFFFF" opacity="0.2"/>
    <!-- 肩甲边缘高光 -->
    <path d="M4 3h4v1H4V3zm12 0h4v1h-4V3z" fill="#FFFFFF" opacity="0.75"/>
  </svg>`,

  // 5. 生物实体 (Entity / Mob): Minecraft 经典苦力怕面容 (Pixel Creeper Face)
  mc_entity: `<svg viewBox="0 0 24 24" width="24" height="24" fill="none">
    <defs>
      <linearGradient id="gGradCreeperBg" x1="2" y1="2" x2="22" y2="22" gradientUnits="userSpaceOnUse">
        <stop offset="0%" stop-color="#81C995"/>
        <stop offset="60%" stop-color="#34A853"/>
        <stop offset="100%" stop-color="#4285F4"/>
      </linearGradient>
    </defs>
    <!-- 苦力怕正方形像素头部底座 -->
    <rect x="2" y="2" width="20" height="20" rx="3" fill="url(#gGradCreeperBg)"/>
    <!-- 头部体素像素网格斑驳暗纹 -->
    <rect x="4" y="4" width="3" height="3" fill="#FFFFFF" opacity="0.2"/>
    <rect x="17" y="4" width="3" height="3" fill="#000000" opacity="0.15"/>
    <rect x="2" y="17" width="4" height="3" fill="#000000" opacity="0.2"/>
    <rect x="18" y="16" width="4" height="4" fill="#FFFFFF" opacity="0.2"/>
    <!-- 苦力怕传奇双眼与口鼻像素黑洞 -->
    <path d="M6 7h4v4H6V7zm8 0h4v4h-4V7zm-4 4h4v3h-4v-3zm-2 3h8v6h-2v-4h-4v4H8v-6z" fill="#131314"/>
  </svg>`,

  // 6. 机器方块实体 (BlockEntity + GUI): Minecraft 像素机械工业箱与能量核心 (Machine with Power Core)
  mc_block_entity: `<svg viewBox="0 0 24 24" width="24" height="24" fill="none">
    <defs>
      <linearGradient id="gGradMachineBody" x1="2" y1="2" x2="22" y2="22" gradientUnits="userSpaceOnUse">
        <stop offset="0%" stop-color="#333537"/>
        <stop offset="100%" stop-color="#1E1F20"/>
      </linearGradient>
      <linearGradient id="gGradEnergyCore" x1="7" y1="7" x2="17" y2="17" gradientUnits="userSpaceOnUse">
        <stop offset="0%" stop-color="#78D9EC"/>
        <stop offset="50%" stop-color="#4285F4"/>
        <stop offset="100%" stop-color="#9B72CB"/>
      </linearGradient>
    </defs>
    <!-- 工业机械外壳 -->
    <rect x="2" y="2" width="20" height="20" rx="2" fill="url(#gGradMachineBody)" stroke="#4285F4" stroke-width="1.2"/>
    <!-- 四角像素铆钉 -->
    <rect x="4" y="4" width="2" height="2" fill="#A8C7FA"/>
    <rect x="18" y="4" width="2" height="2" fill="#A8C7FA"/>
    <rect x="4" y="18" width="2" height="2" fill="#A8C7FA"/>
    <rect x="18" y="18" width="2" height="2" fill="#A8C7FA"/>
    <!-- 中心能量反应堆窗口 -->
    <rect x="7" y="7" width="10" height="10" fill="#131314" stroke="#9B72CB" stroke-width="0.8"/>
    <!-- 十字发光像素能量核心 -->
    <path d="M11 8h2v8h-2V8zm-3 3h8v2H8v-2z" fill="url(#gGradEnergyCore)"/>
    <!-- 核心高光点 -->
    <rect x="11" y="11" width="2" height="2" fill="#FFFFFF"/>
  </svg>`,

  // 7. 药水效果 (MobEffect): Minecraft 经典 16x16 像素细颈药水瓶 (Pixel Potion Bottle)
  mc_mob_effect: `<svg viewBox="0 0 24 24" width="24" height="24" fill="none">
    <defs>
      <linearGradient id="gGradPotionLiquid" x1="6" y1="10" x2="18" y2="22" gradientUnits="userSpaceOnUse">
        <stop offset="0%" stop-color="#D96570"/>
        <stop offset="60%" stop-color="#9B72CB"/>
        <stop offset="100%" stop-color="#4285F4"/>
      </linearGradient>
    </defs>
    <!-- 瓶塞木质像素 -->
    <rect x="10" y="2" width="4" height="2" fill="#FDD663"/>
    <!-- 细长瓶颈 -->
    <rect x="11" y="4" width="2" height="4" fill="#C4C7C5"/>
    <path d="M9 8h6v2H9V8z" fill="#C4C7C5"/>
    <!-- 阶梯圆底瓶身外轮廓 -->
    <path d="M6 10h12v2h2v4h-1v4h-2v2H7v-2H5v-4H4v-4h2v-2z" fill="rgba(255,255,255,0.12)" stroke="#C4C7C5" stroke-width="1"/>
    <!-- 瓶内充满活力的 Gemini 渐变魔力药水 -->
    <path d="M6 13h12v3h-1v3h-2v1H9v-1H7v-3H6v-3z" fill="url(#gGradPotionLiquid)"/>
    <!-- 药水内部上升的像素魔力气泡 -->
    <rect x="8" y="15" width="2" height="2" fill="#FFFFFF" opacity="0.85"/>
    <rect x="13" y="14" width="1.5" height="1.5" fill="#FFFFFF" opacity="0.6"/>
    <rect x="11" y="17" width="1.5" height="1.5" fill="#FFFFFF" opacity="0.75"/>
    <!-- 玻璃瓶反光边缘 -->
    <path d="M7 11h2v1H7v-1zm10 0h-2v1h2v-1z" fill="#FFFFFF" opacity="0.5"/>
  </svg>`,

  // 8. 世界生成 (WorldGen Feature): Minecraft 像素矿脉与地层晶簇 (Pixel Ore & Strata)
  mc_worldgen: `<svg viewBox="0 0 24 24" width="24" height="24" fill="none">
    <defs>
      <linearGradient id="gGradOreRock" x1="2" y1="2" x2="22" y2="22" gradientUnits="userSpaceOnUse">
        <stop offset="0%" stop-color="#28292A"/>
        <stop offset="100%" stop-color="#1F2022"/>
      </linearGradient>
      <linearGradient id="gGradOreCrystal" x1="5" y1="5" x2="19" y2="19" gradientUnits="userSpaceOnUse">
        <stop offset="0%" stop-color="#78D9EC"/>
        <stop offset="50%" stop-color="#4285F4"/>
        <stop offset="100%" stop-color="#9B72CB"/>
      </linearGradient>
    </defs>
    <!-- 原生岩石方块底座 -->
    <rect x="2" y="2" width="20" height="20" rx="3" fill="url(#gGradOreRock)" stroke="rgba(255,255,255,0.1)" stroke-width="1"/>
    <!-- 岩石自然纹理裂缝 -->
    <path d="M3 8h4v2H3V8zm14 8h4v2h-4v-2zm-6-3h4v1h-4v-1z" fill="#131314" opacity="0.8"/>
    <!-- 散落的像素宝石/矿脉晶簇 (Gemini Crystal Clusters) -->
    <path d="M5 5h3v3H5V5zm11 3h3v3h-3V8zm-4 5h3v3h-3v-3zm-5 4h3v3H7v-3zm9 0h2v2h-2v-2z" fill="url(#gGradOreCrystal)"/>
    <!-- 宝石晶簇高光反光点 -->
    <rect x="6" y="6" width="1" height="1" fill="#FFFFFF"/>
    <rect x="17" y="9" width="1" height="1" fill="#FFFFFF"/>
    <rect x="13" y="14" width="1" height="1" fill="#FFFFFF"/>
  </svg>`,

  // 模式与通用动作图标 (全部像素风格化 + Gemini 渐变)
  dark_mode: `<svg viewBox="0 0 24 24" width="16" height="16" fill="none">
    <defs>
      <linearGradient id="gGradDarkMoon" x1="4" y1="2" x2="20" y2="22" gradientUnits="userSpaceOnUse">
        <stop offset="0%" stop-color="#D0BCFF"/>
        <stop offset="100%" stop-color="#4285F4"/>
      </linearGradient>
    </defs>
    <path d="M12 2h4v2h-2v2h-2v2h-2v4h2v2h2v2h2v2h-4v-2h-3v-2H7v-4H5v-4h2V6h2V4h3V2z" fill="url(#gGradDarkMoon)"/>
  </svg>`,

  light_mode: `<svg viewBox="0 0 24 24" width="16" height="16" fill="none">
    <defs>
      <linearGradient id="gGradLightSun" x1="2" y1="2" x2="22" y2="22" gradientUnits="userSpaceOnUse">
        <stop offset="0%" stop-color="#FDD663"/>
        <stop offset="100%" stop-color="#D96570"/>
      </linearGradient>
    </defs>
    <rect x="8" y="8" width="8" height="8" fill="url(#gGradLightSun)"/>
    <path d="M11 2h2v4h-2V2zm0 16h2v4h-2v-4zM2 11h4v2H2v-2zm16 0h4v2h-4v-2zM5 5h2v2H5V5zm12 0h2v2h-2V5zM5 17h2v2H5v-2zm12 0h2v2h-2v-2z" fill="url(#gGradLightSun)"/>
  </svg>`,

  auto_mode: `<svg viewBox="0 0 24 24" width="16" height="16" fill="none">
    <rect x="3" y="4" width="18" height="12" rx="1" fill="#28292A" stroke="#4285F4" stroke-width="1.2"/>
    <path d="M3 4h9v12H3V4z" fill="#4285F4" opacity="0.3"/>
    <path d="M9 18h6v2H9v-2zm-2 2h10v2H7v-2z" fill="#9B72CB"/>
  </svg>`,

  // 通用动作图标 (MC 像素化)
  add: `<svg viewBox="0 0 24 24" width="18" height="18" fill="none">
    <path d="M10 4h4v6h6v4h-6v6h-4v-6H4v-4h6V4z" fill="#4285F4"/>
  </svg>`,

  copy: `<svg viewBox="0 0 24 24" width="16" height="16" fill="none">
    <defs>
      <linearGradient id="gGradCopy" x1="4" y1="2" x2="20" y2="22" gradientUnits="userSpaceOnUse">
        <stop offset="0%" stop-color="#78D9EC"/>
        <stop offset="100%" stop-color="#9B72CB"/>
      </linearGradient>
    </defs>
    <!-- 后层像素纸张 -->
    <path d="M4 2h10v3h-2V4H6v12h2v2H4V2z" fill="#8E918F"/>
    <!-- 前层像素纸张 -->
    <path d="M8 6h12v16H8V6zm2 2v12h8V8h-8z" fill="url(#gGradCopy)"/>
    <path d="M12 10h4v2h-4v-2zm0 4h4v2h-4v-2z" fill="url(#gGradCopy)"/>
  </svg>`,

  download: `<svg viewBox="0 0 24 24" width="16" height="16" fill="none">
    <defs>
      <linearGradient id="gGradDownload" x1="4" y1="2" x2="20" y2="22" gradientUnits="userSpaceOnUse">
        <stop offset="0%" stop-color="#4285F4"/>
        <stop offset="100%" stop-color="#81C995"/>
      </linearGradient>
    </defs>
    <!-- MC 漏斗式像素下落箭头 -->
    <path d="M10 2h4v8h4v2h-2v2h-2v2h-4v-2H8v-2H6v-2h4V2z" fill="url(#gGradDownload)"/>
    <rect x="4" y="18" width="16" height="3" fill="#9B72CB"/>
  </svg>`,

  // 像素宝箱 (Chest)
  folder: `<svg viewBox="0 0 24 24" width="16" height="16" fill="none">
    <rect x="3" y="4" width="18" height="16" rx="2" fill="#D0BCFF" opacity="0.2" stroke="#9B72CB" stroke-width="1.2"/>
    <path d="M3 10h18v2H3v-2z" fill="#9B72CB"/>
    <rect x="10" y="9" width="4" height="4" fill="#FDD663"/>
  </svg>`,

  // 打开的像素宝箱
  folder_open: `<svg viewBox="0 0 24 24" width="16" height="16" fill="none">
    <path d="M2 6h16l4 4H6L2 6z" fill="#4285F4" opacity="0.6"/>
    <path d="M3 10h18v10H3V10z" fill="#9B72CB" opacity="0.3" stroke="#9B72CB" stroke-width="1.2"/>
    <rect x="10" y="11" width="4" height="3" fill="#FDD663"/>
  </svg>`,

  // 像素红石中继器 (Settings / Redstone)
  settings: `<svg viewBox="0 0 24 24" width="18" height="18" fill="none">
    <rect x="3" y="4" width="18" height="16" rx="2" fill="#28292A" stroke="#8E918F" stroke-width="1.2"/>
    <!-- 红石火把 1 -->
    <rect x="7" y="7" width="3" height="7" fill="#6750A4"/>
    <rect x="7" y="6" width="3" height="3" fill="#F28B82"/>
    <!-- 红石火把 2 -->
    <rect x="14" y="9" width="3" height="7" fill="#6750A4"/>
    <rect x="14" y="8" width="3" height="3" fill="#F28B82"/>
    <!-- 底部红石连接线 -->
    <path d="M8 15h8v2H8v-2z" fill="#D96570"/>
  </svg>`,

  // 像素绿宝石对勾
  check: `<svg viewBox="0 0 24 24" width="16" height="16" fill="none">
    <path d="M19 5l2 2-11 11-5-5 2-2 3 3 9-9z" fill="#81C995"/>
  </svg>`,

  // 像素红石火把 / 闪电
  flash_on: `<svg viewBox="0 0 24 24" width="16" height="16" fill="none">
    <path d="M13 2h5l-6 8h5l-9 12 3-9H6l7-11z" fill="#FDD663"/>
  </svg>`,

  // 像素铁砧 / 锻造锤 (Build / Anvil)
  build: `<svg viewBox="0 0 24 24" width="16" height="16" fill="none">
    <path d="M4 4h16v4h-2v2h-3v4h3v2h2v4H4v-4h2v-2h3v-4H6V8H4V4z" fill="#9B72CB"/>
    <rect x="8" y="10" width="8" height="4" fill="#4285F4" opacity="0.6"/>
  </svg>`,

  // 像素绿宝石 (Gem)
  gem: `<svg viewBox="0 0 24 24" width="16" height="16" fill="none">
    <path d="M8 2h8v3h4v4h2v6h-2v4h-4v3H8v-3H4v-4H2v-6h2V5h4V2z" fill="#81C995"/>
    <path d="M9 5h6v2H9V5zm-3 4h12v2H6V9zm2 6h8v2H8v-2z" fill="#FFFFFF" opacity="0.4"/>
  </svg>`,

  // 像素末影箱 (Archive / Ender Chest)
  archive: `<svg viewBox="0 0 24 24" width="16" height="16" fill="none">
    <rect x="3" y="4" width="18" height="16" rx="2" fill="#1E1F20" stroke="#78D9EC" stroke-width="1.2"/>
    <!-- 末影之眼 -->
    <rect x="10" y="10" width="4" height="4" fill="#81C995"/>
    <rect x="11.5" y="10" width="1" height="4" fill="#131314"/>
  </svg>`,

  // 像素物品展示框 / 画作 (Image / Frame)
  image: `<svg viewBox="0 0 24 24" width="16" height="16" fill="none">
    <rect x="3" y="3" width="18" height="18" rx="2" fill="#28292A" stroke="#FDD663" stroke-width="1.2"/>
    <!-- 画布中像素小山与太阳 -->
    <rect x="6" y="6" width="3" height="3" fill="#D96570"/>
    <path d="M6 16l4-5 3 3 3-4 3 6H6z" fill="#4285F4"/>
  </svg>`,

  // 像素唱片 / 音符盒 (Audio / Disc)
  audio: `<svg viewBox="0 0 24 24" width="16" height="16" fill="none">
    <rect x="3" y="3" width="18" height="18" rx="3" fill="#1F2022" stroke="#9B72CB" stroke-width="1.2"/>
    <!-- 唱片中心色环与像素音符 -->
    <circle cx="12" cy="12" r="5" fill="#4285F4" opacity="0.4"/>
    <path d="M11 9h3v5h-3v-2h2v-2h-2V9z" fill="#F28B82"/>
  </svg>`,

  // 像素地貌阶梯层 (Layers / Slabs)
  layers: `<svg viewBox="0 0 24 24" width="16" height="16" fill="none">
    <path d="M12 2l9 5-9 5-9-5 9-5z" fill="#78D9EC"/>
    <path d="M3 11l9 5 9-5v2l-9 5-9-5v-2z" fill="#4285F4"/>
    <path d="M3 16l9 5 9-5v2l-9 5-9-5v-2z" fill="#9B72CB"/>
  </svg>`,

  // 像素附魔书 (Enchanted Book / File Code)
  file_code: `<svg viewBox="0 0 24 24" width="16" height="16" fill="none">
    <!-- 红色附魔书皮 -->
    <rect x="4" y="3" width="16" height="18" rx="2" fill="#D96570" stroke="#9B72CB" stroke-width="1.2"/>
    <!-- 金色书角与紫光缎带绑带 -->
    <path d="M4 3h4v4H4V3zm12 0h4v4h-4V3z" fill="#FDD663"/>
    <path d="M10 3h4v18h-4V3z" fill="#9B72CB" opacity="0.8"/>
    <!-- 像素书扣宝石 -->
    <rect x="11" y="10" width="2" height="4" fill="#78D9EC"/>
  </svg>`
};

if (typeof window !== 'undefined') {
  window.SVG_LIB = SVG_LIB;
}
