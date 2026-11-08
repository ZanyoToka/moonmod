package com.moonproject.moonmod.client.bedrock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * 基岩版 (Bedrock Edition) 几何体方块单元 (Bedrock Cube)
 * 对应 Blockbench / Bedrock JSON 导出的 cubes 数组
 */
public class BedrockCube {
    public final float minX, minY, minZ;
    public final float maxX, maxY, maxZ;
    public final float u, v;
    public final float sizeX, sizeY, sizeZ;
    public final boolean mirror;

    public BedrockCube(float originX, float originY, float originZ,
                       float sizeX, float sizeY, float sizeZ,
                       float u, float v, float inflate, boolean mirror,
                       float texWidth, float texHeight) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.u = u;
        this.v = v;
        this.mirror = mirror;

        // Bedrock 坐标系到 Minecraft Java 渲染空间映射
        this.minX = originX - inflate;
        this.minY = originY - inflate;
        this.minZ = originZ - inflate;
        this.maxX = originX + sizeX + inflate;
        this.maxY = originY + sizeY + inflate;
        this.maxZ = originZ + sizeZ + inflate;
    }

    public void compile(PoseStack.Pose pose, VertexConsumer buffer, int packedLight, int packedOverlay,
                        float red, float green, float blue, float alpha, float texW, float texH) {
        Matrix4f poseMat = pose.pose();
        Matrix3f normalMat = pose.normal();

        // 6 个面的顶点与法线绘制
        // 下底面 (-Y)
        addQuad(poseMat, normalMat, buffer,
                new Vector3f(minX, minY, minZ),
                new Vector3f(maxX, minY, minZ),
                new Vector3f(maxX, minY, maxZ),
                new Vector3f(minX, minY, maxZ),
                (u + sizeZ + sizeX) / texW, v / texH,
                (u + sizeZ + sizeX + sizeX) / texW, (v + sizeZ) / texH,
                0, -1, 0, packedLight, packedOverlay, red, green, blue, alpha);

        // 上顶面 (+Y)
        addQuad(poseMat, normalMat, buffer,
                new Vector3f(maxX, maxY, minZ),
                new Vector3f(minX, maxY, minZ),
                new Vector3f(minX, maxY, maxZ),
                new Vector3f(maxX, maxY, maxZ),
                (u + sizeZ) / texW, v / texH,
                (u + sizeZ + sizeX) / texW, (v + sizeZ) / texH,
                0, 1, 0, packedLight, packedOverlay, red, green, blue, alpha);

        // 北面 (-Z, Front)
        addQuad(poseMat, normalMat, buffer,
                new Vector3f(maxX, maxY, minZ),
                new Vector3f(maxX, minY, minZ),
                new Vector3f(minX, minY, minZ),
                new Vector3f(minX, maxY, minZ),
                (u + sizeZ) / texW, (v + sizeZ) / texH,
                (u + sizeZ + sizeX) / texW, (v + sizeZ + sizeY) / texH,
                0, 0, -1, packedLight, packedOverlay, red, green, blue, alpha);

        // 南面 (+Z, Back)
        addQuad(poseMat, normalMat, buffer,
                new Vector3f(minX, maxY, maxZ),
                new Vector3f(minX, minY, maxZ),
                new Vector3f(maxX, minY, maxZ),
                new Vector3f(maxX, maxY, maxZ),
                (u + sizeZ + sizeX + sizeZ) / texW, (v + sizeZ) / texH,
                (u + sizeZ + sizeX + sizeZ + sizeX) / texW, (v + sizeZ + sizeY) / texH,
                0, 0, 1, packedLight, packedOverlay, red, green, blue, alpha);

        // 西面 (-X, Right)
        addQuad(poseMat, normalMat, buffer,
                new Vector3f(minX, maxY, minZ),
                new Vector3f(minX, minY, minZ),
                new Vector3f(minX, minY, maxZ),
                new Vector3f(minX, maxY, maxZ),
                u / texW, (v + sizeZ) / texH,
                (u + sizeZ) / texW, (v + sizeZ + sizeY) / texH,
                -1, 0, 0, packedLight, packedOverlay, red, green, blue, alpha);

        // 东面 (+X, Left)
        addQuad(poseMat, normalMat, buffer,
                new Vector3f(maxX, maxY, maxZ),
                new Vector3f(maxX, minY, maxZ),
                new Vector3f(maxX, minY, minZ),
                new Vector3f(maxX, maxY, minZ),
                (u + sizeZ + sizeX) / texW, (v + sizeZ) / texH,
                (u + sizeZ + sizeX + sizeZ) / texW, (v + sizeZ + sizeY) / texH,
                1, 0, 0, packedLight, packedOverlay, red, green, blue, alpha);
    }

    private void addQuad(Matrix4f poseMat, Matrix3f normalMat, VertexConsumer buffer,
                         Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4,
                         float u0, float v0, float u1, float v1,
                         float nx, float ny, float nz,
                         int packedLight, int packedOverlay,
                         float r, float g, float b, float a) {
        addVertex(poseMat, normalMat, buffer, p1.x(), p1.y(), p1.z(), u1, v0, nx, ny, nz, packedLight, packedOverlay, r, g, b, a);
        addVertex(poseMat, normalMat, buffer, p2.x(), p2.y(), p2.z(), u1, v1, nx, ny, nz, packedLight, packedOverlay, r, g, b, a);
        addVertex(poseMat, normalMat, buffer, p3.x(), p3.y(), p3.z(), u0, v1, nx, ny, nz, packedLight, packedOverlay, r, g, b, a);
        addVertex(poseMat, normalMat, buffer, p4.x(), p4.y(), p4.z(), u0, v0, nx, ny, nz, packedLight, packedOverlay, r, g, b, a);
    }

    private void addVertex(Matrix4f poseMat, Matrix3f normalMat, VertexConsumer buffer,
                           float x, float y, float z, float u, float v,
                           float nx, float ny, float nz,
                           int packedLight, int packedOverlay,
                           float r, float g, float b, float a) {
        buffer.vertex(poseMat, x, y, z)
                .color(r, g, b, a)
                .uv(u, v)
                .overlayCoords(packedOverlay)
                .uv2(packedLight)
                .normal(normalMat, nx, ny, nz)
                .endVertex();
    }
}
