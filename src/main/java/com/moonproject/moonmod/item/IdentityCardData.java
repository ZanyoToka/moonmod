package com.moonproject.moonmod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 人格卡数据表：一张卡 = 一个「异界可能性」的具体人格。
 *
 * <p>设计要点：
 * <ul>
 *   <li>同一张卡只用一个物品 {@code moonproject:identity_card}，人格身份存在 NBT 里，
 *       以后加新人格只要在 {@link #BUILT_IN} 里加一行，不用新增物品、模型、贴图。</li>
 *   <li>数值全部是「基础值」，最终生效值 = 基础值 × {@code identity.cardPowerScale}
 *       （Config 可调，默认 1.0），保证数值不散落在代码里。</li>
 *   <li>等级沿用原著体系：0 级（灰）→ 00 级（青）→ 000 级（金）。</li>
 * </ul>
 */
public record IdentityCardData(
        String id,
        String display,
        int grade,
        double maxHealth,
        double attackDamage,
        double movementSpeed,
        double armor,
        double attackSpeed,
        String flavor
) {

    public static final int GRADE_ZERO = 0;
    public static final int GRADE_OO = 1;
    public static final int GRADE_OOO = 2;

    private static final Map<String, IdentityCardData> BY_ID = new LinkedHashMap<>();
    private static final Map<Integer, List<IdentityCardData>> BY_GRADE = new LinkedHashMap<>();
    private static final List<IdentityCardData> BUILT_IN = new ArrayList<>();

    static {
        // ── 0 级：都市底层，聊胜于无的加成 ──────────────────────────────
        add(new IdentityCardData("urban_fixer", "都市流浪收尾人", GRADE_ZERO,
                2.0D, 1.0D, 0.0D, 0.0D, 0.0D,
                "刀口舔血的散工，活过今天就算赢了。"));
        add(new IdentityCardData("rat", "鼠辈", GRADE_ZERO,
                0.0D, 1.5D, 0.01D, 0.0D, 0.0D,
                "在下水道里翻找明天的家伙，跑得比谁都快。"));
        add(new IdentityCardData("syndicate_member", "帮派杂兵", GRADE_ZERO,
                3.0D, 0.0D, 0.0D, 1.0D, 0.0D,
                "人多势众。人多，也意味着死得起。"));

        // ── 00 级：有点名号的收尾人 ────────────────────────────────────
        add(new IdentityCardData("heishou_member", "黑云会若众", GRADE_OO,
                2.0D, 3.0D, 0.0D, 0.0D, 0.0D,
                "刀比命贵，云纹比刀贵。"));
        add(new IdentityCardData("liu_association", "六协会成员", GRADE_OO,
                4.0D, 1.5D, 0.0D, 2.0D, 0.0D,
                "火焰不需要理由，秩序需要。"));
        add(new IdentityCardData("sword_pact_disciple", "剑契派门徒", GRADE_OO,
                1.0D, 2.0D, 0.0D, 1.0D, 0.08D,
                "剑鞘里装的是规矩，拔出来的是叛逆。"));

        // ── 000 级：都市顶点的传闻 ────────────────────────────────────
        add(new IdentityCardData("south4_manager", "南部第4科科长", GRADE_OOO,
                4.0D, 5.0D, 0.0D, 2.0D, 0.0D,
                "能坐在那张桌子后面的人，手上都不干净。"));
        add(new IdentityCardData("w_corp_cleaner", "W公司次元清扫要员", GRADE_OOO,
                2.0D, 3.0D, 0.03D, 4.0D, 0.10D,
                "空间被切开的时候，是没有声音的。"));
        add(new IdentityCardData("colors_fixed", "色彩级收尾人（仿）", GRADE_OOO,
                6.0D, 4.0D, 0.02D, 2.0D, 0.05D,
                "名字里有颜色，命里就没有了。"));
    }

    private static void add(IdentityCardData data) {
        BUILT_IN.add(data);
        BY_ID.put(data.id(), data);
        BY_GRADE.computeIfAbsent(data.grade(), key -> new ArrayList<>()).add(data);
    }

    /** 全部内置人格卡（只读）。 */
    public static List<IdentityCardData> all() {
        return Collections.unmodifiableList(BUILT_IN);
    }

    /** 按 id 查人格卡；未登记返回 null。 */
    @Nullable
    public static IdentityCardData byId(String id) {
        return id == null ? null : BY_ID.get(id);
    }

    /** 从物品栈的 NBT 还原人格卡数据；数据缺失（如被指令清过 NBT）时回落到 0 级第一张。 */
    public static IdentityCardData of(ItemStack stack) {
        String id = IdentityCardItem.getCardId(stack);
        IdentityCardData data = byId(id);
        if (data != null) {
            return data;
        }
        List<IdentityCardData> pool = BY_GRADE.get(IdentityCardItem.getCardGrade(stack));
        if (pool != null && !pool.isEmpty()) {
            return pool.get(0);
        }
        return BUILT_IN.get(0);
    }

    /** 按等级随机抽一张（抽取舱用）；该等级没有登记时回落到 0 级。 */
    public static IdentityCardData randomByGrade(RandomSource random, int grade) {
        List<IdentityCardData> pool = BY_GRADE.get(grade);
        if (pool == null || pool.isEmpty()) {
            pool = BY_GRADE.get(GRADE_ZERO);
        }
        if (pool == null || pool.isEmpty()) {
            return BUILT_IN.get(0);
        }
        return pool.get(random.nextInt(pool.size()));
    }

    /** 等级文字（0 级 / 00 级 / 000 级）。 */
    public String gradeLabel() {
        return switch (grade) {
            case GRADE_OOO -> "000级";
            case GRADE_OO -> "00级";
            default -> "0级";
        };
    }

    /** 等级配色：0 级灰、00 级青、000 级金。 */
    public ChatFormatting gradeColor() {
        return switch (grade) {
            case GRADE_OOO -> ChatFormatting.GOLD;
            case GRADE_OO -> ChatFormatting.AQUA;
            default -> ChatFormatting.GRAY;
        };
    }

    /** 属性效果是否为空（空效果只保留收藏/展示意义）。 */
    public boolean hasAttributes() {
        return maxHealth != 0.0D || attackDamage != 0.0D || movementSpeed != 0.0D
                || armor != 0.0D || attackSpeed != 0.0D;
    }
}
