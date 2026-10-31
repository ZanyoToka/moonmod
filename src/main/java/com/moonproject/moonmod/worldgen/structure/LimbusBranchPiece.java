package com.moonproject.moonmod.worldgen.structure;

import com.moonproject.moonmod.block.ExpBurnerBlock;
import com.moonproject.moonmod.block.GoldenBoughShrineBlock;
import com.moonproject.moonmod.block.IdentityExtractorBlock;
import com.moonproject.moonmod.block.MeltdownSirenBlock;
import com.moonproject.moonmod.block.MephistophelesEngineBlock;
import com.moonproject.moonmod.block.MephistophelesSeatBlock;
import com.moonproject.moonmod.init.ModBlocks;
import com.moonproject.moonmod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * 边狱巴士自然生成结构切片 (极高细节版)：
 * 1. 地表：倾斜撞击坑、生体动力梅菲斯特客车残骸、对坐客席、工业站台与站牌
 * 2. 地下一层：总类层清算书库、风化书架、点燃的废墟焚书炉与清算宝箱
 * 3. 地下二层：都市重装防爆收容室、警报全开红石广播机、K公司纳米医疗喷淋站、人格共鸣舱
 * 4. 地下三层：溶洞晶簇群、四大方尖立柱、石英升华祭台、黄金枝基座(HAS_BOUGH=true)光之领域
 */
public class LimbusBranchPiece extends StructurePiece {
    private final BlockPos origin;

    public LimbusBranchPiece(BlockPos origin, BoundingBox box) {
        super(ModStructures.LIMBUS_BRANCH_PIECE.get(), 0, box);
        this.origin = origin;
        this.setOrientation(Direction.SOUTH);
    }

    public LimbusBranchPiece(CompoundTag tag) {
        super(ModStructures.LIMBUS_BRANCH_PIECE.get(), tag);
        this.origin = new BlockPos(tag.getInt("ox"), tag.getInt("oy"), tag.getInt("oz"));
    }

    public LimbusBranchPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        this(tag);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("ox", this.origin.getX());
        tag.putInt("oy", this.origin.getY());
        tag.putInt("oz", this.origin.getZ());
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                            RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
        int cx = origin.getX();
        int cy = origin.getY();
        int cz = origin.getZ();

        // 1. 地表脱轨梅菲斯特客车与月台 (cy ~ cy + 6)
        buildSurfaceCrashSite(level, box, random, cx, cy, cz);

        // 2. 地下一层：总类层文书清算书库 (cy - 7 ~ cy - 1)
        buildFloor1Archives(level, box, random, cx, cy, cz);

        // 3. 地下二层：收容与特遣走廊 (cy - 16 ~ cy - 8)
        buildFloor2Containment(level, box, random, cx, cy, cz);

        // 4. 地下三层：黄金枝共鸣圣殿 (cy - 25 ~ cy - 17)
        buildFloor3Sanctum(level, box, random, cx, cy, cz);
    }

    // ==========================================
    // 1. 地表脱轨梅菲斯特与站台细节
    // ==========================================
    private void buildSurfaceCrashSite(WorldGenLevel level, BoundingBox box, RandomSource random, int cx, int cy, int cz) {
        // 撕裂的大地撞击坑 (粗玄武岩、泥土、沙砾混合风化)
        for (int x = -5; x <= 5; x++) {
            for (int z = -6; z <= 6; z++) {
                if (Math.abs(x) + Math.abs(z) <= 9) {
                    BlockPos p = new BlockPos(cx + x, cy - 1, cz + z);
                    int r = random.nextInt(4);
                    BlockState dirtState = switch (r) {
                        case 0 -> Blocks.COARSE_DIRT.defaultBlockState();
                        case 1 -> Blocks.TUFF.defaultBlockState();
                        case 2 -> Blocks.GRAVEL.defaultBlockState();
                        default -> Blocks.ROOTED_DIRT.defaultBlockState();
                    };
                    setBlockSafe(level, box, p, dirtState);
                }
            }
        }

        // 站台立柱与锈蚀站牌
        setBlockSafe(level, box, new BlockPos(cx - 5, cy, cz - 3), Blocks.IRON_BARS.defaultBlockState());
        setBlockSafe(level, box, new BlockPos(cx - 5, cy + 1, cz - 3), Blocks.IRON_BARS.defaultBlockState());
        setBlockSafe(level, box, new BlockPos(cx - 5, cy + 2, cz - 3), Blocks.REDSTONE_LAMP.defaultBlockState());
        setBlockSafe(level, box, new BlockPos(cx - 5, cy + 3, cz - 3), Blocks.REDSTONE_BLOCK.defaultBlockState());
        // 站台破损长椅
        setBlockSafe(level, box, new BlockPos(cx - 5, cy, cz - 1), Blocks.DARK_OAK_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, Direction.EAST));
        setBlockSafe(level, box, new BlockPos(cx - 5, cy, cz), Blocks.DARK_OAK_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, Direction.EAST));

        // 车体框架（红色陶瓦 + 深板岩瓦 + 铁栏杆车窗）
        for (int x = -2; x <= 2; x++) {
            for (int z = -4; z <= 4; z++) {
                for (int y = 0; y <= 4; y++) {
                    BlockPos p = new BlockPos(cx + x, cy + y, cz + z);
                    if (y == 0) {
                        // 车底盘：磨制深板岩
                        setBlockSafe(level, box, p, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
                    } else if (y == 4) {
                        // 车顶天花板：深板岩瓦
                        setBlockSafe(level, box, p, Blocks.DEEPSLATE_TILES.defaultBlockState());
                    } else {
                        // 车身外壁
                        if (x == -2 || x == 2 || z == -4) {
                            if (y == 2 && z != -4 && (z % 2 == 0)) {
                                setBlockSafe(level, box, p, Blocks.IRON_BARS.defaultBlockState());
                            } else {
                                setBlockSafe(level, box, p, Blocks.RED_TERRACOTTA.defaultBlockState());
                            }
                        } else {
                            setBlockSafe(level, box, p, Blocks.AIR.defaultBlockState());
                        }
                    }
                }
            }
        }

        // 车轮（黑色混凝土与车轴）
        setBlockSafe(level, box, new BlockPos(cx - 2, cy, cz - 2), Blocks.BLACK_CONCRETE.defaultBlockState());
        setBlockSafe(level, box, new BlockPos(cx + 2, cy, cz - 2), Blocks.BLACK_CONCRETE.defaultBlockState());
        setBlockSafe(level, box, new BlockPos(cx - 2, cy, cz + 2), Blocks.BLACK_CONCRETE.defaultBlockState());
        setBlockSafe(level, box, new BlockPos(cx + 2, cy, cz + 2), Blocks.BLACK_CONCRETE.defaultBlockState());

        // 车头正中：【梅菲斯特引擎核心】(点亮状态，朝北)
        BlockPos enginePos = new BlockPos(cx, cy + 1, cz - 4);
        BlockState engineState = ModBlocks.MEPHISTOPHELES_ENGINE.get().defaultBlockState()
                .setValue(MephistophelesEngineBlock.FACING, Direction.NORTH)
                .setValue(MephistophelesEngineBlock.LIT, true);
        setBlockSafe(level, box, enginePos, engineState);

        // 车头前部生体咀嚼口与管线（血肉、哭泣黑曜石、红石血管）
        setBlockSafe(level, box, enginePos.north(), Blocks.CRYING_OBSIDIAN.defaultBlockState());
        setBlockSafe(level, box, enginePos.north().east(), Blocks.NETHER_WART_BLOCK.defaultBlockState());
        setBlockSafe(level, box, enginePos.north().west(), Blocks.NETHER_WART_BLOCK.defaultBlockState());
        setBlockSafe(level, box, enginePos.north(2), Blocks.MAGMA_BLOCK.defaultBlockState());
        setBlockSafe(level, box, enginePos.above(), Blocks.AMETHYST_BLOCK.defaultBlockState());

        // 车厢内对坐客席：【梅菲斯特客席】(左右各设对坐座位)
        setBlockSafe(level, box, new BlockPos(cx - 1, cy + 1, cz - 1),
                ModBlocks.MEPHISTOPHELES_SEAT.get().defaultBlockState().setValue(MephistophelesSeatBlock.FACING, Direction.EAST));
        setBlockSafe(level, box, new BlockPos(cx + 1, cy + 1, cz - 1),
                ModBlocks.MEPHISTOPHELES_SEAT.get().defaultBlockState().setValue(MephistophelesSeatBlock.FACING, Direction.WEST));
        setBlockSafe(level, box, new BlockPos(cx - 1, cy + 1, cz + 1),
                ModBlocks.MEPHISTOPHELES_SEAT.get().defaultBlockState().setValue(MephistophelesSeatBlock.FACING, Direction.EAST));
        setBlockSafe(level, box, new BlockPos(cx + 1, cy + 1, cz + 1),
                ModBlocks.MEPHISTOPHELES_SEAT.get().defaultBlockState().setValue(MephistophelesSeatBlock.FACING, Direction.WEST));

        // 车内天花板扶手管道与昏暗红石灯
        setBlockSafe(level, box, new BlockPos(cx, cy + 3, cz), Blocks.REDSTONE_LAMP.defaultBlockState());
        setBlockSafe(level, box, new BlockPos(cx, cy + 4, cz), Blocks.REDSTONE_BLOCK.defaultBlockState());
        setBlockSafe(level, box, new BlockPos(cx, cy + 3, cz - 2), Blocks.CHAIN.defaultBlockState());
        setBlockSafe(level, box, new BlockPos(cx, cy + 3, cz + 2), Blocks.CHAIN.defaultBlockState());

        // 车内储物箱
        BlockPos busChestPos = new BlockPos(cx + 1, cy + 1, cz + 3);
        setBlockSafe(level, box, busChestPos, Blocks.CHEST.defaultBlockState());
        fillBusChest(level, busChestPos);

        // 车尾地面破裂垂直竖井（通往地下一层）
        for (int y = cy; y >= cy - 2; y--) {
            setBlockSafe(level, box, new BlockPos(cx, y, cz + 3), Blocks.AIR.defaultBlockState());
            setBlockSafe(level, box, new BlockPos(cx - 1, y, cz + 3), Blocks.CHAIN.defaultBlockState());
        }
    }

    // ==========================================
    // 2. 地下一层：总类层清算书库细节
    // ==========================================
    private void buildFloor1Archives(WorldGenLevel level, BoundingBox box, RandomSource random, int cx, int cy, int cz) {
        int minY = cy - 7;
        int maxY = cy - 2;

        // 混风化石砖外墙
        for (int x = -6; x <= 6; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = -6; z <= 6; z++) {
                    BlockPos p = new BlockPos(cx + x, y, cz + z);
                    boolean isWall = (x == -6 || x == 6 || z == -6 || z == 6 || y == minY || y == maxY);
                    if (isWall) {
                        int r = random.nextInt(5);
                        BlockState wallState = switch (r) {
                            case 0 -> Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
                            case 1 -> Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
                            case 2 -> Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
                            default -> Blocks.STONE_BRICKS.defaultBlockState();
                        };
                        setBlockSafe(level, box, p, wallState);
                    } else {
                        setBlockSafe(level, box, p, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }

        // 地板棋盘格铺设
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                BlockState floor = ((x + z) % 2 == 0) ? Blocks.POLISHED_BLACKSTONE.defaultBlockState() : Blocks.DARK_OAK_PLANKS.defaultBlockState();
                setBlockSafe(level, box, new BlockPos(cx + x, minY, cz + z), floor);
            }
        }

        // 装订长桌：【总类层装订台】+ 讲台 + 铁砧
        setBlockSafe(level, box, new BlockPos(cx, minY + 1, cz), ModBlocks.BOOKBINDING_TABLE.get().defaultBlockState());
        setBlockSafe(level, box, new BlockPos(cx + 1, minY + 1, cz), Blocks.LECTERN.defaultBlockState());
        setBlockSafe(level, box, new BlockPos(cx - 1, minY + 1, cz), Blocks.CHIPPED_ANVIL.defaultBlockState());

        // 壁炉与【废墟焚书炉】(点亮状态，朝南)
        BlockPos burnerPos = new BlockPos(cx, minY + 1, cz - 5);
        BlockState burnerState = ModBlocks.EXP_BURNER.get().defaultBlockState()
                .setValue(ExpBurnerBlock.FACING, Direction.SOUTH)
                .setValue(ExpBurnerBlock.LIT, true);
        setBlockSafe(level, box, burnerPos, burnerState);
        setBlockSafe(level, box, burnerPos.above(), Blocks.HOPPER.defaultBlockState()); // 烟囱排气漏斗
        setBlockSafe(level, box, burnerPos.east(), Blocks.NETHER_BRICKS.defaultBlockState());
        setBlockSafe(level, box, burnerPos.west(), Blocks.NETHER_BRICKS.defaultBlockState());
        setBlockSafe(level, box, burnerPos.east().above(), Blocks.SOUL_LANTERN.defaultBlockState());

        // 书架群、蜘蛛网与清算宝箱
        setBlockSafe(level, box, new BlockPos(cx - 4, minY + 1, cz - 4), Blocks.BOOKSHELF.defaultBlockState());
        setBlockSafe(level, box, new BlockPos(cx - 4, minY + 2, cz - 4), Blocks.BOOKSHELF.defaultBlockState());
        setBlockSafe(level, box, new BlockPos(cx - 5, minY + 1, cz - 4), Blocks.BOOKSHELF.defaultBlockState());
        setBlockSafe(level, box, new BlockPos(cx - 4, minY + 3, cz - 4), Blocks.COBWEB.defaultBlockState());

        BlockPos libraryChestPos = new BlockPos(cx - 4, minY + 1, cz + 4);
        setBlockSafe(level, box, libraryChestPos, Blocks.CHEST.defaultBlockState());
        fillLibraryChest(level, libraryChestPos);

        // 连向二层的破损石阶
        for (int i = 0; i <= 3; i++) {
            setBlockSafe(level, box, new BlockPos(cx + 4, minY - i, cz + 2 + i), Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState());
            setBlockSafe(level, box, new BlockPos(cx + 4, minY - i + 1, cz + 2 + i), Blocks.AIR.defaultBlockState());
            setBlockSafe(level, box, new BlockPos(cx + 4, minY - i + 2, cz + 2 + i), Blocks.AIR.defaultBlockState());
        }
    }

    // ==========================================
    // 3. 地下二层：收容与特遣走廊细节
    // ==========================================
    private void buildFloor2Containment(WorldGenLevel level, BoundingBox box, RandomSource random, int cx, int cy, int cz) {
        int minY = cy - 16;
        int maxY = cy - 9;

        // 主走廊外壳（平滑玄武岩）
        fillBox(level, box, cx - 8, minY, cz - 4, cx + 8, maxY, cz + 4, Blocks.SMOOTH_BASALT.defaultBlockState(), Blocks.AIR.defaultBlockState());

        // 走廊警戒黄色陶瓦与黑色混凝土交错条纹
        for (int x = -7; x <= 7; x++) {
            BlockState stripe = (x % 2 == 0) ? Blocks.YELLOW_TERRACOTTA.defaultBlockState() : Blocks.BLACK_CONCRETE.defaultBlockState();
            setBlockSafe(level, box, new BlockPos(cx + x, minY, cz), stripe);
        }

        // 顶部工业通风电缆管线与【脑叶警报广播机】(全态激活)
        for (int x = -6; x <= 6; x += 2) {
            setBlockSafe(level, box, new BlockPos(cx + x, maxY - 1, cz), Blocks.IRON_BARS.defaultBlockState());
        }
        BlockPos sirenPos = new BlockPos(cx, maxY - 1, cz);
        setBlockSafe(level, box, sirenPos, ModBlocks.MELTDOWN_SIREN.get().defaultBlockState().setValue(MeltdownSirenBlock.ACTIVE, true));
        setBlockSafe(level, box, sirenPos.east(), Blocks.REDSTONE_LAMP.defaultBlockState());
        setBlockSafe(level, box, sirenPos.west(), Blocks.REDSTONE_LAMP.defaultBlockState());
        setBlockSafe(level, box, sirenPos.above(), Blocks.REDSTONE_BLOCK.defaultBlockState());

        // ────── 收容室：完全由【都市重装防爆壁】密闭，四角加固黑曜石 ──────
        int rxMin = cx - 7;
        int rxMax = cx - 2;
        int rzMin = cz + 4;
        int rzMax = cz + 9;
        fillBox(level, box, rxMin, minY, rzMin, rxMax, maxY, rzMax, ModBlocks.REINFORCED_PLATING.get().defaultBlockState(), Blocks.AIR.defaultBlockState());
        // 四角黑曜石加固
        setBlockSafe(level, box, new BlockPos(rxMin, minY, rzMin), Blocks.OBSIDIAN.defaultBlockState());
        setBlockSafe(level, box, new BlockPos(rxMax, minY, rzMin), Blocks.OBSIDIAN.defaultBlockState());
        setBlockSafe(level, box, new BlockPos(rxMin, minY, rzMax), Blocks.OBSIDIAN.defaultBlockState());
        setBlockSafe(level, box, new BlockPos(rxMax, minY, rzMax), Blocks.OBSIDIAN.defaultBlockState());

        // 双层观察窗：青色玻璃 + 铁栏杆防护
        setBlockSafe(level, box, new BlockPos(cx - 4, minY + 2, cz + 4), Blocks.CYAN_STAINED_GLASS.defaultBlockState());
        setBlockSafe(level, box, new BlockPos(cx - 5, minY + 2, cz + 4), Blocks.CYAN_STAINED_GLASS.defaultBlockState());
        setBlockSafe(level, box, new BlockPos(cx - 4, minY + 2, cz + 3), Blocks.IRON_BARS.defaultBlockState());
        setBlockSafe(level, box, new BlockPos(cx - 5, minY + 2, cz + 3), Blocks.IRON_BARS.defaultBlockState());

        // 观察室操作台：【脑啡肽提取终端】+ 控制拉杆
        setBlockSafe(level, box, new BlockPos(cx - 4, minY + 1, cz + 3), ModBlocks.ENKEPHALIN_EXTRACTOR.get().defaultBlockState());
        setBlockSafe(level, box, new BlockPos(cx - 5, minY + 1, cz + 3), Blocks.LEVER.defaultBlockState());

        // 收容室内核心：【异想体收容单元】悬浮于基座上
        int rMidX = (rxMin + rxMax) / 2;
        int rMidZ = (rzMin + rzMax) / 2;
        setBlockSafe(level, box, new BlockPos(rMidX, minY, rMidZ), Blocks.CRYING_OBSIDIAN.defaultBlockState());
        setBlockSafe(level, box, new BlockPos(rMidX, minY + 1, rMidZ), ModBlocks.CONTAINMENT_UNIT.get().defaultBlockState());
        setBlockSafe(level, box, new BlockPos(rMidX, minY + 2, rMidZ), Blocks.SOUL_LANTERN.defaultBlockState());

        // ────── 侧翼医疗站与人格整备室 ──────
        // 医疗站：【K公司再生安瓿机】+ 绿宝石环绕地板 + 纳米喷头(炼药锅)
        BlockPos kCorpPos = new BlockPos(cx + 5, minY + 1, cz - 3);
        setBlockSafe(level, box, kCorpPos, ModBlocks.KCORP_INJECTOR.get().defaultBlockState());
        setBlockSafe(level, box, kCorpPos.below(), Blocks.EMERALD_BLOCK.defaultBlockState());
        setBlockSafe(level, box, kCorpPos.above(), Blocks.CAULDRON.defaultBlockState());

        BlockPos medChestPos = new BlockPos(cx + 6, minY + 1, cz - 3);
        setBlockSafe(level, box, medChestPos, Blocks.CHEST.defaultBlockState());
        fillMedicalChest(level, medChestPos);

        // 人格整备舱：【人格抽取共鸣舱】+ 紫珀柱 + 海晶灯 + 紫水晶簇
        BlockPos extractorPos = new BlockPos(cx + 5, minY + 1, cz + 3);
        setBlockSafe(level, box, extractorPos, ModBlocks.IDENTITY_EXTRACTOR.get().defaultBlockState()
                .setValue(IdentityExtractorBlock.FACING, Direction.WEST));
        setBlockSafe(level, box, extractorPos.above(), Blocks.SEA_LANTERN.defaultBlockState());
        setBlockSafe(level, box, extractorPos.above(2), Blocks.AMETHYST_CLUSTER.defaultBlockState());

        // 垂直跌落锁链竖井（通向地下三层）
        for (int y = minY; y >= minY - 3; y--) {
            setBlockSafe(level, box, new BlockPos(cx, y, cz), Blocks.AIR.defaultBlockState());
            setBlockSafe(level, box, new BlockPos(cx, y, cz - 1), Blocks.CHAIN.defaultBlockState());
        }
    }

    // ==========================================
    // 4. 地下三层：黄金枝共鸣圣殿细节
    // ==========================================
    private void buildFloor3Sanctum(WorldGenLevel level, BoundingBox box, RandomSource random, int cx, int cy, int cz) {
        int minY = cy - 25;
        int maxY = cy - 18;

        // 巨型溶洞空间
        fillBox(level, box, cx - 8, minY, cz - 8, cx + 8, maxY, cz + 8, Blocks.DEEPSLATE_BRICKS.defaultBlockState(), Blocks.AIR.defaultBlockState());

        // 地貌浸染侵蚀：金块、生金块、菌光体与紫水晶地面
        for (int x = -7; x <= 7; x++) {
            for (int z = -7; z <= 7; z++) {
                int dist = Math.abs(x) + Math.abs(z);
                BlockPos p = new BlockPos(cx + x, minY, cz + z);
                if (dist <= 3) {
                    setBlockSafe(level, box, p, Blocks.GOLD_BLOCK.defaultBlockState());
                } else if (dist <= 5) {
                    setBlockSafe(level, box, p, (dist % 2 == 0) ? Blocks.RAW_GOLD_BLOCK.defaultBlockState() : Blocks.SHROOMLIGHT.defaultBlockState());
                } else {
                    setBlockSafe(level, box, p, Blocks.AMETHYST_BLOCK.defaultBlockState());
                }
            }
        }

        // 天花板倒挂紫水晶簇与钟乳滴水石
        for (int x = -6; x <= 6; x += 3) {
            for (int z = -6; z <= 6; z += 3) {
                if (Math.abs(x) + Math.abs(z) > 4) {
                    setBlockSafe(level, box, new BlockPos(cx + x, maxY - 1, cz + z), Blocks.POINTED_DRIPSTONE.defaultBlockState());
                    setBlockSafe(level, box, new BlockPos(cx + x + 1, maxY - 1, cz + z), Blocks.AMETHYST_CLUSTER.defaultBlockState());
                }
            }
        }

        // 四大守护立柱（由深板岩与金块交错筑成）
        buildPillar(level, box, cx - 4, minY + 1, cz - 4, maxY - 1);
        buildPillar(level, box, cx + 4, minY + 1, cz - 4, maxY - 1);
        buildPillar(level, box, cx - 4, minY + 1, cz + 4, maxY - 1);
        buildPillar(level, box, cx + 4, minY + 1, cz + 4, maxY - 1);

        // 平滑石英三级升华祭台
        for (int r = 2; r >= 0; r--) {
            int stepY = minY + 1 + (2 - r);
            for (int x = -r; x <= r; x++) {
                for (int z = -r; z <= r; z++) {
                    setBlockSafe(level, box, new BlockPos(cx + x, stepY, cz + z), Blocks.SMOOTH_QUARTZ.defaultBlockState());
                }
            }
        }

        // 祭台正顶端核心：【黄金枝基座】(插入黄金枝状态 HAS_BOUGH=true，面朝南)
        BlockPos boughPos = new BlockPos(cx, minY + 4, cz);
        BlockState boughState = ModBlocks.GOLDEN_BOUGH_SHRINE.get().defaultBlockState()
                .setValue(GoldenBoughShrineBlock.FACING, Direction.SOUTH)
                .setValue(GoldenBoughShrineBlock.HAS_BOUGH, true);
        setBlockSafe(level, box, boughPos, boughState);

        // 祭坛四周插满直插天际的光之源：【末地烛】
        setBlockSafe(level, box, new BlockPos(cx - 2, minY + 3, cz - 2), Blocks.END_ROD.defaultBlockState());
        setBlockSafe(level, box, new BlockPos(cx + 2, minY + 3, cz - 2), Blocks.END_ROD.defaultBlockState());
        setBlockSafe(level, box, new BlockPos(cx - 2, minY + 3, cz + 2), Blocks.END_ROD.defaultBlockState());
        setBlockSafe(level, box, new BlockPos(cx + 2, minY + 3, cz + 2), Blocks.END_ROD.defaultBlockState());
    }

    private void buildPillar(WorldGenLevel level, BoundingBox box, int x, int minY, int z, int maxY) {
        for (int y = minY; y <= maxY; y++) {
            BlockState pillarState = (y % 2 == 0) ? Blocks.DEEPSLATE_BRICK_WALL.defaultBlockState() : Blocks.GOLD_BLOCK.defaultBlockState();
            setBlockSafe(level, box, new BlockPos(x, y, z), pillarState);
        }
    }

    // ==========================================
    // 战利品箱填充逻辑
    // ==========================================
    private void fillBusChest(WorldGenLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ChestBlockEntity chest) {
            chest.setItem(0, new ItemStack(Items.COAL, 16));
            chest.setItem(1, new ItemStack(Items.RAW_IRON, 8));
            chest.setItem(2, new ItemStack(Items.REDSTONE, 12));
            chest.setItem(3, new ItemStack(Items.COOKED_BEEF, 5));
            chest.setItem(4, new ItemStack(Items.TORCH, 16));
        }
    }

    private void fillLibraryChest(WorldGenLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ChestBlockEntity chest) {
            chest.setItem(0, new ItemStack(Items.BOOK, 6));
            chest.setItem(1, new ItemStack(Items.PAPER, 16));
            chest.setItem(2, new ItemStack(Items.INK_SAC, 4));
            chest.setItem(3, new ItemStack(Items.FEATHER, 4));
            chest.setItem(4, new ItemStack(Items.GOLD_INGOT, 5));
            chest.setItem(5, new ItemStack(Items.LAPIS_LAZULI, 9));
            // 放入一本经验修补附魔书
            ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
            chest.setItem(6, enchantedBook);
        }
    }

    private void fillMedicalChest(WorldGenLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ChestBlockEntity chest) {
            chest.setItem(0, new ItemStack(Items.GOLDEN_APPLE, 2));
            chest.setItem(1, new ItemStack(Items.EMERALD, 4));
            chest.setItem(2, new ItemStack(Items.GLISTERING_MELON_SLICE, 6));
            chest.setItem(3, new ItemStack(Items.GLASS_BOTTLE, 8));
            chest.setItem(4, new ItemStack(Items.NETHER_WART, 3));
        }
    }

    private void fillBox(WorldGenLevel level, BoundingBox box, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, BlockState wall, BlockState inside) {
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos p = new BlockPos(x, y, z);
                    if (x == minX || x == maxX || y == minY || y == maxY || z == minZ || z == maxZ) {
                        setBlockSafe(level, box, p, wall);
                    } else {
                        setBlockSafe(level, box, p, inside);
                    }
                }
            }
        }
    }

    private void setBlockSafe(WorldGenLevel level, BoundingBox box, BlockPos pos, BlockState state) {
        if (box.isInside(pos)) {
            level.setBlock(pos, state, 2);
        }
    }
}
