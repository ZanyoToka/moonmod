# -*- coding: utf-8 -*-
"""生成《天杀星刀「阿赖耶识」设计规格书》（docx）。

用法：python generate_alaya_spec.py
输出：月亮计划模组-阿赖耶识设计规格.docx（与本脚本同目录 docs/）

本脚本只负责生成文档，不修改模组代码。改规格时改这里再重跑即可。
"""
import datetime
import os

from docx import Document
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Cm, Pt, RGBColor

EAST = "微软雅黑"
NAVY = RGBColor(0x1F, 0x3A, 0x5F)
NAVY2 = RGBColor(0x2E, 0x4A, 0x74)
DARK = RGBColor(0x26, 0x26, 0x26)
GRAY = RGBColor(0x66, 0x66, 0x66)
RED = RGBColor(0x9C, 0x2B, 0x2B)

OUT_DIR = os.path.dirname(os.path.abspath(__file__))
OUT_NAME = "月亮计划模组-阿赖耶识设计规格.docx"
TODAY = datetime.date.today().strftime("%Y-%m-%d")

doc = Document()


# ---------------------------------------------------------------- 样式与工具
def style_font(style, latin, east, size=None, bold=None, color=None):
    style.font.name = latin
    rpr = style.element.get_or_add_rPr()
    rfonts = rpr.find(qn("w:rFonts"))
    if rfonts is None:
        rfonts = OxmlElement("w:rFonts")
        rpr.append(rfonts)
    rfonts.set(qn("w:ascii"), latin)
    rfonts.set(qn("w:hAnsi"), latin)
    rfonts.set(qn("w:eastAsia"), east)
    if size is not None:
        style.font.size = Pt(size)
    if bold is not None:
        style.font.bold = bold
    if color is not None:
        style.font.color.rgb = color


def setup_styles():
    normal = doc.styles["Normal"]
    style_font(normal, EAST, EAST, size=10.5, color=DARK)
    normal.paragraph_format.space_after = Pt(4)
    normal.paragraph_format.line_spacing = 1.25
    for lvl, sz, col in (("Heading 1", 15, NAVY), ("Heading 2", 12.5, NAVY2),
                         ("Heading 3", 11, NAVY2)):
        try:
            style_font(doc.styles[lvl], EAST, EAST, size=sz, bold=True, color=col)
        except KeyError:
            pass
    try:
        style_font(doc.styles["Title"], EAST, EAST, size=22, bold=True, color=NAVY)
    except KeyError:
        pass
    for name in ("List Bullet", "List Bullet 2"):
        try:
            style_font(doc.styles[name], EAST, EAST, size=10.5)
        except KeyError:
            pass


def setup_page():
    for section in doc.sections:
        section.page_width = Cm(21.0)
        section.page_height = Cm(29.7)
        section.left_margin = Cm(2.5)
        section.right_margin = Cm(2.5)
        section.top_margin = Cm(2.4)
        section.bottom_margin = Cm(2.4)


def para(text, style=None, size=None, color=None, bold=False, italic=False):
    p = doc.add_paragraph(style=style)
    if isinstance(text, str):
        text = [(text, bold)]
    for seg in text:
        if isinstance(seg, str):
            seg = (seg, False)
        r = p.add_run(seg[0])
        r.bold = seg[1] if len(seg) > 1 else False
        if size is not None:
            r.font.size = Pt(size)
        if color is not None:
            r.font.color.rgb = color
        r.italic = italic
    return p


def note(text):
    """灰底提示行。"""
    return para(text, size=10, color=RED, bold=True)


def bullet(text, bold=False, level=1):
    style = "List Bullet" if level == 1 else "List Bullet 2"
    return para(text, style=style, bold=bold)


def h1(text):
    return doc.add_heading(text, level=1)


def h2(text):
    return doc.add_heading(text, level=2)


def h3(text):
    return doc.add_heading(text, level=3)


TWIPS_PER_CM = 567  # 1 cm = 566.93 twips


def _set_tbl_width(tblPr, total_twips):
    """写入 w:tblW，并按 OOXML 规定的子元素顺序插入。"""
    for el in tblPr.findall(qn("w:tblW")):
        tblPr.remove(el)
    tblW = OxmlElement("w:tblW")
    tblW.set(qn("w:type"), "dxa")
    tblW.set(qn("w:w"), str(total_twips))
    for tag in ("w:jc", "w:tblCellSpacing", "w:tblInd", "w:tblBorders", "w:shd",
                "w:tblLayout", "w:tblCellMar", "w:tblLook"):
        ref = tblPr.find(qn(tag))
        if ref is not None:
            ref.addprevious(tblW)
            return
    tblPr.append(tblW)


def _apply_fixed_widths(t, widths):
    """固定表格布局：写死 tblGrid 列宽 + tblW，避免渲染器按等宽网格排版。"""
    total = int(round(sum(widths) * TWIPS_PER_CM))
    t.autofit = False  # python-docx 会正确写入 w:tblLayout type="fixed"
    tblPr = t._tbl.tblPr
    _set_tbl_width(tblPr, total)
    grid = t._tbl.find(qn("w:tblGrid"))
    if grid is not None:
        for gc, w in zip(grid.findall(qn("w:gridCol")), widths):
            gc.set(qn("w:w"), str(int(round(w * TWIPS_PER_CM))))
    for row in t.rows:
        for i, w in enumerate(widths):
            row.cells[i].width = Cm(w)


def add_table(headers, rows, widths=None, font_size=9.5):
    t = doc.add_table(rows=1, cols=len(headers))
    t.style = "Table Grid"
    hdr = t.rows[0].cells
    for i, htext in enumerate(headers):
        p = hdr[i].paragraphs[0]
        r = p.add_run(htext)
        r.bold = True
        r.font.size = Pt(font_size)
        shd = OxmlElement("w:shd")
        shd.set(qn("w:val"), "clear")
        shd.set(qn("w:color"), "auto")
        shd.set(qn("w:fill"), "DCE6F1")
        hdr[i]._tc.get_or_add_tcPr().append(shd)
    for row in rows:
        cells = t.add_row().cells
        for i, v in enumerate(row):
            cells[i].text = ""
            p = cells[i].paragraphs[0]
            r = p.add_run(str(v))
            r.font.size = Pt(font_size)
    if widths:
        _apply_fixed_widths(t, widths)
    doc.add_paragraph()
    return t


# ---------------------------------------------------------------- 文档内容
setup_styles()
setup_page()

title = doc.add_heading("天杀星刀「阿赖耶识」设计规格书", level=0)
para("《月亮计划》MC 同人模组 · 角色专属武器线（5.2b）", size=11, color=GRAY)
doc.add_paragraph()

add_table(
    ["项目", "内容"],
    [
        ["文档版本", "v0.3（鞘击态面板 8→10；Q8/Q9 已拍板）"],
        ["生成日期", TODAY],
        ["目标平台", "Minecraft 1.20.1 + Forge 47.4.5 + Java 17"],
        ["模组 modid", "moonproject（包名 com.moonproject.moonmod）"],
        ["内容归属", "总纲 5.2b 神备武器线（角色专属觉醒武器）"],
        ["计划阶段", "Phase 2 武器打样 → Phase 3 接入记忆/侵蚀 → Phase 5 打磨"],
        ["文档性质", "设计规格（不含代码）。本文件批准后作为写码与画贴图的唯一依据"],
        ["关联文档", "SKILL.md（设计总纲）、月亮计划MC同人模组-规划文档.docx"],
    ],
    widths=[3.6, 12.4],
)

# ---------------------------------------------------------------- 1
h1("1. 文档目的与范围")
bullet("目的：把「阿赖耶识」从原作设定翻译成本模组可直接施工的规格——外形、数值、机制、技术方案、验收标准。")
bullet("范围：本文件只定义设计，不含代码。实现顺序与验收标准见第 7、8 章。")
bullet("依据：SKILL.md 总纲的技术栈（1.20.1 + Forge 47.x）、命名规范（第 7 节）、"
       "核心机制 4（SP 精神值）与机制 5（E.G.O 侵蚀）。")
bullet("暂不做：依赖异想体实体（Phase 4）的「斩断投影联系」真机制，本文件只预留接口。")
note("重要约定：本模组实际 modid 为 moonproject（仓库现状）。总纲 SKILL.md 旧文写作 pmoon，"
     "已按本次决定统一为 moonproject，本文件一律使用 moonproject。")

# ---------------------------------------------------------------- 2
h1("2. 原作考据")
h2("2.1 出处")
add_table(
    ["项目", "内容"],
    [
        ["正式名称", "天杀星刀·阿赖耶识（천살성도 아라야시키 / 天殺星刀アラヤ識）"],
        ["持有者", "《边狱公司》罪人 #4 良秀（Ryōshū）"],
        ["登场", "主线第九章「断ち切れない」（玩家通称「天杀良」形态）"],
        ["原作定位", "血脉兵器/星刀，既不是异想体提取的 E.G.O，也不是自我显现的神备"],
        ["核心描述", "达到「将所有存在与时间视为一体、连概念都能斩」的境界：每斩一刀削去对方的"
                     "存在，终局可抹消存在本身"],
        ["代价", "使用者的记忆被斩碎（原作明确副作用）"],
        ["时间要素", "良秀免疫时间系干扰，与「时间感知的血脉」设定一致"],
    ],
    widths=[3.2, 12.8],
)
para("考据来源（外部资料，仅供设定参考，不作为素材来源）：", bold=True)
bullet("萌娘百科·阿赖耶(边狱公司) —— https://zh.moegirl.org.cn/阿赖耶(边狱公司)")
bullet("边狱公司中文维基（灰机）·阿赖耶 —— https://limbuscompany.huijiwiki.com/p/63960")
bullet("namu wiki·都市(Project Moon 世界观) —— 天杀星刀·阿赖耶识条目")
bullet("NGA 讨论：良秀免疫时间系干扰的原理 —— https://ngabbs.com/read.php?tid=46250740")
para("说明：萌百与灰机的正文页有反爬拦截，本次考据以搜索摘要 + 日/韩文维基摘要交叉确认。"
     "用户提供的四条设定（概念斩断、无法愈合、记忆代价、时间血脉）与原作一致。",
     size=9.5, color=GRAY)

h2("2.2 与原作武器体系的关系（归类说明）")
bullet("原作三分：E.G.O 装备（从异想体提取，对应总纲 5.2a）、神备（角色自我显现，对应 5.2b）、"
       "血脉兵器/星刀（家族血脉传承，阿赖耶识属于此类）。")
bullet("本模组的处理（已拍板）：不新增类目，把阿赖耶识并入 5.2b 神备武器线，"
       "按「角色专属觉醒武器」的规格实现——即获取流程、绑定人格卡、首次觉醒解锁成就都沿用神备规则。")
bullet("同步要求：SKILL.md 5.2b 需新增本武器条目，并在表下注明「原作属星刀/血脉兵器，本模组并入本线」。")

h2("2.3 合规红线（沿用总纲第 2 节）")
bullet("非商业、免费发布；发布前核对 Project Moon 官方 Fan Content Policy 最新条款。")
bullet("名称、设定为致敬与再演绎；贴图、模型、音效全部自绘/自制或使用原版音效引用，绝不提取官方游戏文件。")
bullet("模组页注明「非官方同人作品，原作权利归 Project Moon 所有」，并标注年龄分级提示。")

# ---------------------------------------------------------------- 3
h1("3. 在模组中的定位")
h2("3.1 强度定位")
para("传说级（与 ALEPH 级 E.G.O 同级）。设计目标是「拔刀即碾压，收刀即凡人」："
     "鞘击态只是一把安全可用的日常武器；解放态在 30 秒内提供极高的攻击速度与面板伤害，"
     "并附带穿甲、无法愈合的斩痕与存在抹除。代价不是耐久，而是使用者的记忆被持续斩碎。")
add_table(
    ["武器", "来源/线", "等级", "面板攻击伤害", "特点与代价"],
    [
        ["悔恨 Regret", "5.2a E.G.O（已实现）", "—", "8（钻石级）", "基础武器，暂无特效"],
        ["拟态 Mimicry", "5.2a E.G.O", "ALEPH", "待定（高于悔恨）", "吸血；形态切换；击杀叠攻速"],
        ["薄暝 Twilight", "5.2a E.G.O", "ALEPH", "待定", "光暗双属性；蓄力剑气"],
        ["阿赖耶识", "5.2b 角色专属（本文件）", "传说/ALEPH", "10（鞘击）/ 15（解放）",
         "解放态 3.0 次/秒的极快连斩；概念斩断、无法愈合的焦黑斩痕、存在抹除；代价是记忆被斩碎"],
    ],
    widths=[2.8, 3.4, 2.2, 3.0, 4.6],
)

h2("3.2 获取方式")
para("正式流程（Phase 3 随神备觉醒系统一起上线）：", bold=True)
bullet("前置：装备「良秀」人格卡 + 持有《良秀之书》或《天杀星刀之书》（角色之书）。")
bullet("流程：在焚书炉焚烧角色之书，附加余烬 ×32 + 浓缩脑啡肽 ×8 → 触发神备觉醒事件 → 获得阿赖耶识。")
bullet("首次觉醒解锁成就「斩断存在之人」，并解锁图鉴条目（Patchouli）。")
para("打样期临时方案（Phase 2，焚书炉尚未实现）：", bold=True)
bullet("高成本工作台合成（临时配方，Phase 3 后删除）：E.G.O 核心 ×4 + 侵蚀结晶 ×2 + 下界合金锭 ×1 + 木棍 ×1。")
note("待拍板：是否接受这条临时配方；也可以改成「创造模式专属」直到 Phase 3 上线。")

h2("3.3 与机制条（侵蚀/SP）的关系")
bullet("侵蚀条（总纲机制 5）：阿赖耶识侵蚀增长更快，解放态每次命中 +2；侵蚀满触发腐蚀，"
       "伤害 +30% 但每次命中额外 -1 记忆（与「越用越失控」的气质一致）。")
bullet("SP（总纲机制 4）：Phase 3 SP 系统上线后，解放态收刀时扣除 10 SP，作为精神负担的第二重代价。")

# ---------------------------------------------------------------- 4
h1("4. 外形与美术规格")
h2("4.1 设计关键词")
para("白色半透明刀身 / 大太刀（野太刀）长度 / 焦黑斩痕 / 鞘击为主不轻易拔刀 / 冷冽·神秘·非人感。")

h2("4.2 推荐色板")
add_table(
    ["用途", "颜色", "色值（参考）", "说明"],
    [
        ["刃部主色", "冷白", "#E8ECF2", "不反光的哑白，避免像钻石剑"],
        ["刃部半透明层", "雾白", "#F2F6FF / 不透明度 45%", "做出「若隐若现」的质感"],
        ["刃口/刃纹", "浅钢蓝", "#C9D6E8", "1 像素宽的刃线，强化刀形识别度"],
        ["刀鞘主色", "近黑", "#1B1F2A", "吸收光线，衬托白刃"],
        ["鞘口/金属件", "冷灰蓝", "#6E7A8F", "目贯、鞘尻等细节"],
        ["焦黑斩痕", "焦褐黑", "#2A1D18（边缘 #6B3A1E）", "斩痕效果图标与粒子"],
        ["光晕粒子", "淡蓝白", "#A9C7FF", "解放态待机粒子"],
    ],
    widths=[3.0, 2.6, 4.6, 5.8],
)

h2("4.3 物品贴图规格")
bullet("尺寸 32×32 PNG，带 alpha 通道（半透明刃部必须靠 alpha 实现，不能用滤镜糊出来）。")
bullet("两态两张图：textures/item/alaya_vijnana.png（鞘击态：刀在鞘中，只露鞘）"
       "与 textures/item/alaya_vijnana_drawn.png（解放态：白刃出鞘）。")
bullet("构图：斜向 45° 持握，刀身占画面高度约 70%，大太刀比普通剑更长更细；"
       "鞘击态用刀鞘的深色块面 + 鞘口金属件，解放态用白刃 + 半透明刃部。")
bullet("状态切换：模型用 item/handheld 父级，通过 custom_model_data 谓词（1.20.1 的 "
       "ItemProperties.register + ItemPropertyFunction 返回 NBT 形态值）切换两张模型。")
note("打样期允许我程序生成的占位像素图（与现有 3 个物品同一套风格），正式贴图由你在 Aseprite 中重绘。")

h2("4.4 模型与动画")
bullet("Phase 2 打样：原版平面手持贴图即可（能玩优先）。")
bullet("Phase 5 精修：Blockbench 3D 手持模型（鞘 + 刀两个部件），GeckoLib 4.2 骨骼动画三套——"
       "拔刀（居合）、鞘击、收刀。")
bullet("解放态可以只做「贴图切换 + 粒子」，不做独立 3D 模型，控制工期。")

h2("4.5 粒子与音效")
add_table(
    ["时机", "粒子", "音效（先用原版 SoundEvents，后期可换自制 CC0）"],
    [
        ["切换解放态（拔刀）", "END_ROD 向上 12 个", "TRIDENT_THROW + ARMOR_EQUIP_NETHERITE"],
        ["解放态待机", "SOUL_FIRE_FLAME 每 10 tick 1 个", "SOUL_ESCAPE（低音量、长间隔）"],
        ["鞘击命中", "CRIT 少量", "PLAYER_ATTACK_SWEEP（音调压低）"],
        ["解放态命中", "END_ROD 爆发 + 目标身上 SMOKE", "TRIDENT_HIT（音调压低）"],
        ["斩痕复发", "目标身上 SQUID_INK 黑粒子", "SOUL_ESCAPE"],
        ["存在抹除（击杀）", "大范围 SMOKE + SOUL", "BEACON_DEACTIVATE（低沉）"],
    ],
    widths=[3.4, 4.2, 8.4],
)

# ---------------------------------------------------------------- 5
h1("5. 数值规格")
para("下表为打样默认值，全部写进 Config（总纲第 7 节：禁止硬编码）。"
     "v0.2 调整：解放态改为「极高攻速 + 高伤害」，同时把记忆代价从「按命中扣」改为"
     "「解放期间按时间烧」——否则攻速一提，代价就会失控。"
     "v0.3 调整：按拍板结果把鞘击态面板提到 10，解放态攻速维持 3.0 次/秒不再上调。")
add_table(
    ["项目", "鞘击态（封印，默认）", "解放态（拔刀）"],
    [
        ["形态说明", "刀锁在鞘中，以鞘击战斗，不轻易拔刀", "拔刀 30 秒，结束后自动收刀"],
        ["面板攻击伤害", "10（玩家基础 1 + 修正 9）", "15（玩家基础 1 + 修正 14）"],
        ["攻击速度修正", "-2.4（1.6 次/秒，与原版剑同级）", "-1.0（3.0 次/秒，约为原版剑的两倍速）"],
        ["理论 DPS", "约 16.0（原版剑约 12.8 的 1.25 倍）", "约 45，另加斩痕与穿甲收益"],
        ["护甲穿透", "无", "25%"],
        ["耐久消耗", "每次命中 1", "每次命中 2"],
        ["记忆代价", "无", "解放期间每 20 tick（1 秒）-1 级经验（打样期）"],
        ["命中附加", "缓慢 I（10 tick）+ 0.4 格击退", "焦黑斩痕（10 秒）"],
        ["最大耐久", "2500（需自定义 Tier 或覆写 getMaxDamage）", "同左"],
        ["附魔", "允许（锋利/耐久等常规附魔，上限同原版）", "同左"],
        ["切换冷却", "—", "3 秒；切换/丢弃物品时自动收刀"],
    ],
    widths=[3.0, 6.5, 6.5],
)
para("攻速换算与手感说明：", bold=True)
bullet("MC 的每秒攻击次数 = 4 + 攻速修正。-1.0 即 3.0 次/秒，原版剑是 -2.4（1.6 次/秒），"
       "所以解放态约为原版剑的 1.9 倍速。想更快就继续上调，修正 0 时达到 4.0 次/秒。")
bullet("攻击冷却仍然生效：连点快于 3.0 次/秒时，单次伤害会按冷却比例衰减，"
       "DPS 表按「每次都满蓄力命中」计算；Tooltip 会提示节奏。")
bullet("设定解释：解放态斩的是「存在」而不是实体，刀不再承受任何物理阻力，因此又快又重。"
       "鞘击态则仍是实打实的钝击，慢、稳、不烧记忆。")

# ---------------------------------------------------------------- 6
h1("6. 机制详规")
h2("6.1 双形态切换")
bullet("默认鞘击态。右键（不潜行）切换为解放态，持续 600 tick（30 秒）后自动收刀；解放态中再右键手动收刀。")
bullet("解放态 = 15 面板 + 3.0 次/秒的极快连斩，是本模组最猛烈的输出窗口；"
       "鞘击态为 10 面板 + 1.6 次/秒，比原版剑略强，作为安全可用的日常形态。")
bullet("冷却 3 秒；切换手持物品、丢弃物品、死亡、被缴械时立即收刀。")
bullet("状态存物品 NBT：Mode（0 鞘 / 1 拔）、DrawTicks（剩余 tick）、Erosion（侵蚀值）。")
bullet("表现：贴图切换 + 拔刀音效与粒子；解放态待机持续微弱粒子，暗示「非人感」。")

h2("6.2 焦黑斩痕（新状态效果 black_scar）")
bullet("类型：负面效果，无常规粒子（或极少量 SMOKE），有独立图标（焦黑刀痕）。")
bullet("持续掉血：每 40 tick（2 秒）造成 2 点伤害，持续 10 秒。")
bullet("无法愈合：目标带此效果时，取消其自然回血与治疗手段（拦截 Forge 的 LivingHealEvent），"
       "「即使暂时恢复，伤口也会重新出现」——因此设计为不拦截瞬时回血，而是让斩痕在持续期内持续压制。")
bullet("复发：只要 8 格内存在处于解放态的阿赖耶识，斩痕结束时立即重新施加（对应「再次靠近，伤口立即重现」）。")
bullet("死亡表现：带斩痕死亡的生物，掉落物被抹除（拦截 LivingDropsEvent 清空掉落），"
       "并播放黑色粒子——这是「概念抹除」的第一层表现。")
note("待拍板：斩痕能否被牛奶解除？建议可以（牛奶作为可获得的净化手段，避免玩家被 Boss 挂上后无解）。")

h2("6.3 概念斩断 / 概念抹除")
bullet("对普通生物：解放态击杀 → 掉落物被抹除（存在被削去，不留遗物）+ 亡灵类无法再被治疗转化/复活。")
bullet("对异想体（Phase 4 接口，本阶段只预留）：预留 IAbnormality 接口，命中使其「投影联系」层数 -1；"
       "层数归零则立即移除实体（斩断投影与潜意识之间的联系，使其失去现实载体）。")
bullet("表现层：命中时目标身上出现白色裂纹粒子；击杀瞬间地面留下短暂的焦黑痕迹（可用粒子近似）。")
note("待拍板：概念抹除对 Boss 是否允许直接秒杀？建议改为「对 Boss 只造成额外伤害 + 剥夺其召唤物」，"
     "避免 Boss 战失去意义。")

h2("6.4 记忆代价（使用者被斩碎的记忆）")
para("打样期实现（Phase 2，零新系统）：", bold=True)
bullet("解放期间每 20 tick（1 秒）扣除使用者 1 级经验（-1 级，不做跨级清零）："
       "30 秒全开 = -30 级。改成按时间烧而不是按命中扣，是为了让极快攻速不会把代价放大成灾难，"
       "同时让「这把刀拔多久」成为玩家每次都要做的决策。")
bullet("收刀即停止烧记忆；经验等级为 0 时无法再攻击（提示「你的记忆已经空了」），并强制收刀。")
para("正式实现（Phase 3，与 SP/侵蚀机制合并）：", bold=True)
bullet("新增玩家 Capability「记忆 Memory」（0~100）+ HUD 小条；解放期间每秒 -2，随时间缓慢回复（每 30 秒 +1）。")
bullet("记忆归零进入「空白」状态：无法解放阿赖耶识、失明与反胃各 5 秒、已装备人格卡的被动暂时失效。")
bullet("回复手段：镇定剂、书页（沿用总纲 5.1 的消耗品设计）。")
note("可选但高风险：空白状态下随机遗忘一条已解锁配方。需操作配方书，风险大，暂不建议纳入。")

h2("6.5 适格者限制（时间感知的血脉）")
bullet("设计规则：不满足适格条件时，无法切换解放态，右键提示「你无法感知时间——刀拒绝了你」。")
bullet("打样期：判定函数直接返回 true，由 Config 开关 requiresBloodline 控制（默认 false），"
       "先把流程跑通。")
bullet("Phase 3 正式版：读取人格卡「良秀」是否装备，或血脉觉醒进度是否达成（与人格系统一起实现）。")

h2("6.6 侵蚀条（与总纲机制 5 对齐）")
bullet("侵蚀值存物品 NBT，解放态每次命中 +1（3.0 次/秒时约 3 点/秒，防止极快攻速下瞬间爆条），"
       "闲置时每次 -1（每 5 秒结算一次）。")
bullet("侵蚀满触发腐蚀：伤害 +30%，但每次命中额外 -1 记忆，并持续掉血；"
       "ALEPH/传说级武器的腐蚀更剧烈（沿用总纲机制 5 的描述）。")
bullet("镇定剂可解除腐蚀状态。")

# ---------------------------------------------------------------- 7
h1("7. 技术实现方案")
h2("7.1 文件清单")
add_table(
    ["类型", "路径", "职责"],
    [
        ["新增", "item/AlayaVijnanaItem.java", "核心：双形态、记忆代价、命中附加、耐久与 Tier"],
        ["新增", "effect/ModEffects.java", "状态效果注册器（本模组第一个效果注册器）"],
        ["新增", "effect/BlackScarEffect.java", "焦黑斩痕效果逻辑"],
        ["新增", "event/CombatEvents.java", "命中、治疗拦截、掉落抹除、玩家 tick"],
        ["新增", "config/MoonProjectConfig.java", "数值集中管理（本模组第一个 Config）"],
        ["新增", "capability/MemoryCapability.java", "记忆条（Phase 3 才启用）"],
        ["新增", "models/item/alaya_vijnana.json、alaya_vijnana_drawn.json", "两态物品模型"],
        ["新增", "textures/item/alaya_vijnana.png、alaya_vijnana_drawn.png", "两态 32×32 贴图"],
        ["新增", "data/moonproject/recipes/alaya_vijnana.json", "打样期临时配方"],
        ["修改", "item/ModItems.java", "注册物品（物品注册表已存在）"],
        ["修改", "item/ModCreativeTab.java", "创造标签页加入阿赖耶识"],
        ["修改", "MoonProjectMod.java", "注册效果与 Config 初始化"],
        ["修改", "assets/moonproject/lang/zh_cn.json、en_us.json", "名称、提示文本、效果名"],
    ],
    widths=[1.6, 8.0, 6.4],
)
para("1.20.1 Forge 注意事项：", bold=True)
bullet("物品属性用 Forge 的 ItemAttributeModifiersEvent 或覆写 createAttributes（1.20.1 用后者，"
       "SwordItem 的构造是 (Tier, 攻击伤害修正, 攻速修正, Properties)）。")
bullet("自定义 Tier：为拿到 2500 耐久与特殊附魔性，建议定义 AlayaTier implements Tier，"
       "而不是拼凑原版 Tiers。")
bullet("状态切换用 ItemProperties.register + ItemPropertyFunction（读 NBT），"
       "不要用 1.20.5+ 的 DataComponent。")
bullet("治疗拦截用 LivingHealEvent（Forge 事件），掉落抹除用 LivingDropsEvent。")

h2("7.2 Config 数值表（打样默认值）")
add_table(
    ["配置键", "默认值", "说明"],
    [
        ["alaya.sheathedDamage", "9", "鞘击态攻击伤害修正（面板 10）"],
        ["alaya.drawnDamage", "14", "解放态攻击伤害修正（面板 15）"],
        ["alaya.sheathedAttackSpeed", "-2.4", "鞘击态攻速修正（= 1.6 次/秒）"],
        ["alaya.drawnAttackSpeed", "-1.0", "解放态攻速修正（= 3.0 次/秒，可上调到 0 即 4.0 次/秒）"],
        ["alaya.drawDurationTicks", "600", "解放态持续时间"],
        ["alaya.switchCooldownTicks", "60", "形态切换冷却"],
        ["alaya.memoryBurnIntervalTicks", "20", "解放期间每隔多少 tick 烧掉一次记忆"],
        ["alaya.memoryCostPerBurn", "1", "每次烧掉的记忆值（打样期 = 经验等级）"],
        ["alaya.armorPierce", "0.25", "解放态护甲穿透比例"],
        ["alaya.scarDamage", "2", "斩痕每次结算伤害"],
        ["alaya.scarIntervalTicks", "40", "斩痕结算间隔"],
        ["alaya.scarDurationTicks", "200", "斩痕持续时间"],
        ["alaya.scarReapplyRadius", "8.0", "斩痕复发判定半径"],
        ["alaya.erosionPerHit", "2", "解放态每次命中增加的侵蚀值"],
        ["alaya.requiresBloodline", "false", "是否启用适格者（时间血脉）门槛"],
        ["alaya.temporaryRecipe", "true", "是否启用打样期临时配方"],
    ],
    widths=[5.4, 2.6, 8.0],
)

h2("7.3 事件钩子")
add_table(
    ["事件", "处理内容"],
    [
        ["右键（UseItem）", "切换形态、写 NBT、播放音效与粒子、发提示消息"],
        ["LivingHurtEvent", "区分形态计算伤害与穿甲，解放态命中写斩痕（伤害与攻速由物品属性直接提供）"],
        ["LivingHealEvent", "目标带斩痕时取消治疗回复"],
        ["LivingDropsEvent", "目标带斩痕死亡时清空掉落物"],
        ["PlayerTickEvent", "解放态倒计时、按时间烧记忆、侵蚀衰减、斩痕复发检测（就近 8 格）"],
        ["ItemTooltipEvent", "显示当前形态、侵蚀值、记忆代价说明"],
    ],
    widths=[4.4, 11.6],
)

h2("7.4 实现顺序与验收")
add_table(
    ["步骤", "内容", "验收标准"],
    [
        ["步骤 1 灰模", "注册物品 + 两态贴图与模型 + 临时配方 + 语言 + 创造标签页",
         "创造模式能取出；能打怪；能切换形态（可无特效）"],
        ["步骤 2 核心机制", "解放态扣经验、焦黑斩痕效果、治疗拦截、掉落抹除、复发检测",
         "受伤目标治疗无效；靠近时斩痕复发；被斩杀的怪不掉落"],
        ["步骤 3 表现层", "粒子、音效、Tooltip、成就、后续 GeckoLib 动画",
         "手感与视觉达到设计描述；成就可解锁"],
    ],
    widths=[2.6, 7.4, 6.0],
)

h2("7.5 验证方式说明")
note("本模组的编译验证必须在你的电脑上完成：AI 运行环境无法执行 gradlew（需要联网拉取 Forge/Minecraft "
     "并写入用户目录的 Gradle 缓存）。因此 AI 交付的代码只能保证按 1.20.1 + Forge 47 API 编写，"
     "请你用 IDEA 打开工程执行 runClient 验证。")

# ---------------------------------------------------------------- 8
h1("8. 平衡与风险")
h2("8.1 平衡取向")
bullet("鞘击态是「能一直用」的形态：10 面板（原版剑为 8）、攻速与原版剑同为 1.6 次/秒，"
       "附加硬直，安全但没有爆发。")
bullet("解放态是「爽 30 秒」的形态：15 面板 + 3.0 次/秒，裸 DPS 约 45（原版剑的 3.5 倍），"
       "外加 25% 穿甲与无法愈合的斩痕，是全模组最高的输出窗口。")
bullet("记忆代价是长期成本：经验等级在 MC 里关联附魔，扣级会真正让玩家肉疼，符合原作「记忆被斩碎」的气质。")
bullet("克制手段不是削弱伤害，而是限制窗口：30 秒自动收刀 + 解放期间每秒烧 1 级经验 + 3 秒切换冷却，"
       "确保它是「爆发」而不是「常态」。")
h2("8.2 风险与规避")
add_table(
    ["风险", "说明", "规避"],
    [
        ["记忆代价失控", "极快攻速下若仍按命中扣级，30 秒能扣掉近百级，玩家会直接不敢拔刀",
         "改为按时间烧（每秒 1 级）：攻速以后怎么调都不牵动代价"],
        ["解放态 DPS 过高", "约 45 DPS，可能碾压全部 Boss 与精英怪",
         "限定在 30 秒窗口内；实测过强就先降面板，再考虑延长烧记忆间隔"],
        ["攻速与攻击冷却打架", "连点快于 3.0 次/秒时单次伤害按冷却衰减，玩家会觉得「打得快但不疼」",
         "Tooltip 明确节奏提示；实测后微调攻速修正（Config 可调）"],
        ["斩痕过强", "目标完全无法治疗，Boss 战碾压", "只对生物生效、持续 10 秒、可被牛奶解除"],
        ["治疗拦截不完整", "只拦到自然回血与药水，饥饿/信标等未覆盖", "逐个手段测试，接受「部分可治疗」的妥协"],
        ["存在抹除破坏掉落收益", "玩家打 Boss 反而拿不到战利品", "对 Boss 只剥夺召唤物与额外伤害；掉落抹除仅对普通生物"],
        ["切换形态的键位冲突", "右键在副手/方块上有其他含义", "用潜行 + 右键或先按住右键蓄力，见待拍板清单"],
        ["无法编译验证", "AI 侧不能跑构建", "代码保守写法、避免冷门 API；你在 IDEA 中验证后再进下一阶段"],
    ],
    widths=[3.2, 6.4, 6.4],
)

# ---------------------------------------------------------------- 9
h1("9. 待拍板问题清单")
add_table(
    ["编号", "问题", "建议"],
    [
        ["Q1", "解放态怎么触发？", "右键直接切换（简单直观）；备选：潜行 + 右键，避免与副手/方块交互冲突"],
        ["Q2", "打样期用经验等级代替「记忆」可以吗？", "可以，零新系统；Phase 3 换成独立记忆条"],
        ["Q3", "概念抹除对 Boss 是否允许？", "不允许秒杀，改为剥夺召唤物 + 额外伤害"],
        ["Q4", "斩痕能否被牛奶解除？", "可以，给玩家一条反制路径"],
        ["Q5", "打样期临时配方接受吗？", "接受；或改为 Phase 3 前仅创造模式可获得"],
        ["Q6", "音效先用原版还是自制？", "先用原版 SoundEvents（不占工期），Phase 5 换自制 CC0"],
        ["Q7", "鞘击态要不要格挡机制？", "可选；建议 Phase 5 再考虑，避免打样期铺太开"],
        ["Q8", "鞘击态的伤害要不要一起提高？", "**已拍板（v0.3）**：提到面板 10（修正 9），原为 8"],
        ["Q9", "3.0 次/秒够不够快？", "**已拍板（v0.3）**：3.0 次/秒即可，解放态攻速不再上调"],
    ],
    widths=[1.4, 6.4, 8.2],
)

# ---------------------------------------------------------------- 10
h1("10. 同步项与变更记录")
h2("10.1 本文件批准后需要同步的内容")
bullet("SKILL.md 5.2b：新增阿赖耶识条目，并注明归属说明。")
bullet("SKILL.md 第 1 节与 5.2b 标题：「神备觉醒武器（首批 4 把）」→「首批 5 把」。")
bullet("SKILL.md 命名规范：modid 由 pmoon 统一为 moonproject，包名 com.moonproject.moonmod。")
bullet("SKILL.md 第 10 节关联文件：新增本设计规格书。")
bullet("规划文档《月亮计划MC同人模组-规划文档.docx》：武器章节同步阿赖耶识（可由脚本复用重新生成）。")
h2("10.2 变更记录")
add_table(
    ["版本", "日期", "变更"],
    [
        ["v0.1", TODAY, "初稿：原作考据、归类、外形与色板、数值、机制详规、技术方案、风险与待拍板清单"],
        ["v0.2", TODAY, "解放态：面板 13→15、攻速 1.0→3.0 次/秒（DPS 13→45）；记忆代价由「每次命中扣」"
                        "改为「解放期间每秒扣」；侵蚀每击 +2→+1；同步 Config 键、事件钩子与平衡风险表"],
        ["v0.3", TODAY, "拍板落地：鞘击态面板 8→10（修正 7→9，DPS 12.8→16.0）；确认解放态 3.0 次/秒不再上调；"
                        "Q8/Q9 标记为已拍板"],
    ],
    widths=[2.0, 3.0, 11.0],
)

# ---------------------------------------------------------------- 附录
h1("附录 A：需求原文（用户提供，供追溯）")
para("外形描述：", bold=True)
bullet("阿赖耶识是一把大太刀（大太刀/野太刀尺寸）。")
bullet("刀身：白色、半透明的剑身，有一种「若隐若现」的质感，不属于任何常规金属材质。")
bullet("刀鞘：平时刀被锁在鞘中，刀鞘本身也被打磨得极为锋利，日常战斗以「鞘击」为主，不轻易拔刀。")
bullet("斩痕：被此刀斩中的部位，会留下如同烧伤一般的焦黑痕迹，且这种痕迹无法被常规手段消除。")
bullet("整体气质：刀身修长，白色半透明的刃部在光照下会有微弱的光泽感，整体设计偏冷冽、神秘、非人感。")
bullet("关键词：白色半透明刀身 / 大太刀 / 焦黑斩痕 / 鞘击战斗 / 冷冽神秘感。")
para("机制描述：", bold=True)
bullet("核心能力「概念斩断」：从概念层面斩断事物的「存在性」，被斩中的目标其「存在」被一并斩去。")
bullet("无法愈合：伤口即使被治疗暂时恢复，只要再次靠近阿赖耶识，伤口会立即重新出现。")
bullet("概念抹除：对异想体使用时，是斩断其投影与潜意识之间的联系，使异想体失去现实中的载体。")
bullet("代价：每次使用，使用者自身的记忆会被斩碎——不是「遗忘」，而是记忆从存在层面被切掉，无法常规找回。")
bullet("使用限制：必须拥有能感知时间的血脉以及对时间的先天感知力，否则根本无法使用此刀。")

out_path = os.path.join(OUT_DIR, OUT_NAME)
doc.save(out_path)
print("已生成：" + out_path)
