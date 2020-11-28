/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.model;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;

import javax.annotation.Nullable;

import java.util.List;
import java.util.Random;

public class DelegatedModel implements BakedModel {
	protected final BakedModel originalModel;

	public DelegatedModel(BakedModel originalModel) {
		this.originalModel = originalModel;
	}

	@Override
	public boolean useAmbientOcclusion() {
		return originalModel.useAmbientOcclusion();
	}

	@Override
	public boolean hasDepth() {
		return originalModel.hasDepth();
	}

	@Override
	public boolean isSideLit() {
		return originalModel.isSideLit();
	}

	@Override
	public boolean isBuiltin() {
		return originalModel.isBuiltin();
	}

	@Override
	public Sprite getSprite() {
		return originalModel.getSprite();
	}

	@Override
	public ModelTransformation getTransformation() {
		return originalModel.getTransformation();
	}

	@Override
	public ModelOverrideList getOverrides() {
		return originalModel.getOverrides();
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
		return null;
	}
}
