// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.terasology.engine.rendering.assets.mesh.resource.GLAttributes;
import org.terasology.engine.rendering.assets.mesh.resource.IndexResource;
import org.terasology.engine.rendering.assets.mesh.resource.VertexAttributeBinding;
import org.terasology.engine.rendering.assets.mesh.resource.VertexResource;
import org.terasology.engine.rendering.assets.mesh.resource.VertexResourceBuilder;
import org.terasology.nui.Color;
import org.terasology.nui.Colorc;

public class StandardMeshData extends MeshData {
    public static final int VERTEX_INDEX = 0;
    public static final int NORMAL_INDEX = 1;
    public static final int UV0_INDEX = 2;
    public static final int UV1_INDEX = 3;
    public static final int COLOR0_INDEX = 4;
    public static final int LIGHT0_INDEX = 5;

    public final VertexResource positionBuffer;
    public final VertexAttributeBinding<Vector3fc, Vector3f> position;

    public final VertexResource normalBuffer;
    public final VertexAttributeBinding<Vector3fc, Vector3f> normal;

    public final VertexResource uv0Buffer;
    public final VertexAttributeBinding<Vector2fc, Vector2f> uv0;

    public final VertexResource uv1Buffer;
    public final VertexAttributeBinding<Vector2fc, Vector2f> uv1;

    public final VertexResource colorBuffer;
    public final VertexAttributeBinding<Colorc, Color> color0;

    public final VertexResource lightBuffer;
    public final VertexAttributeBinding<Vector3fc, Vector3f> light0;

    public final IndexResource indices;

    public StandardMeshData() {

        VertexResourceBuilder builder = new VertexResourceBuilder();
        position = builder.add(VERTEX_INDEX, GLAttributes.VECTOR_3_F_VERTEX_ATTRIBUTE);
        positionBuffer = builder.build();

        builder = new VertexResourceBuilder();
        normal = builder.add(NORMAL_INDEX, GLAttributes.VECTOR_3_F_VERTEX_ATTRIBUTE);
        normalBuffer = builder.build();

        builder = new VertexResourceBuilder();
        uv0 = builder.add(UV0_INDEX, GLAttributes.VECTOR_2_F_VERTEX_ATTRIBUTE);
        uv0Buffer = builder.build();

        builder = new VertexResourceBuilder();
        uv1 = builder.add(UV1_INDEX, GLAttributes.VECTOR_2_F_VERTEX_ATTRIBUTE);
        uv1Buffer = builder.build();

        builder = new VertexResourceBuilder();
        color0 = builder.add(COLOR0_INDEX, GLAttributes.COLOR_4_F_VERTEX_ATTRIBUTE);
        colorBuffer = builder.build();


        builder = new VertexResourceBuilder();
        light0 = builder.add(LIGHT0_INDEX, GLAttributes.VECTOR_3_F_VERTEX_ATTRIBUTE);
        lightBuffer = builder.build();

        this.indices = new IndexResource();
    }

    public void  reserve(int numVertices, int numIndices) {
        positionBuffer.reserveElements(numVertices);
        normalBuffer.reserveElements(numVertices);
        uv0Buffer.reserveElements(numVertices);
        uv1Buffer.reserveElements(numVertices);
        lightBuffer.reserveElements(numVertices);
        colorBuffer.reserveElements(numVertices);
        indices.reserveElements(numIndices);
    }

    public void  reallocate(int numVerts, int numIndices) {
        positionBuffer.reallocateElements(numVerts);
        normalBuffer.reallocateElements(numVerts);
        uv0Buffer.reallocateElements(numVerts);
        uv1Buffer.reallocateElements(numVerts);
        lightBuffer.reallocateElements(numVerts);
        colorBuffer.reallocateElements(numVerts);
        indices.reallocateElements(numIndices);

    }

    @Override
    public VertexAttributeBinding<Vector3fc, Vector3f> positions() {
        return position;
    }

    @Override
    public VertexResource[] vertexResources() {
        return new VertexResource[]{
                positionBuffer,
                normalBuffer,
                uv0Buffer,
                uv1Buffer,
                colorBuffer,
                lightBuffer
        };
    }

    @Override
    public IndexResource indexResource() {
        return indices;
    }

    @Override
    public StandardMeshData clone() {
        StandardMeshData meshData = new StandardMeshData();
        meshData.positionBuffer.copy(this.positionBuffer);
        meshData.normalBuffer.copy(this.normalBuffer);
        meshData.uv0Buffer.copy(this.uv0Buffer);
        meshData.uv1Buffer.copy(this.uv1Buffer);
        meshData.lightBuffer.copy(this.lightBuffer);
        meshData.colorBuffer.copy(this.colorBuffer);
        meshData.indices.copy(this.indices);
        return meshData;
    }
}
